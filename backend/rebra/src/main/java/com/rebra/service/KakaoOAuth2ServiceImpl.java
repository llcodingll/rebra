package com.rebra.service;
import static com.rebra.util.NonceUtil.*;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import com.rebra.dto.TempToken;
import com.rebra.dto.response.KakaoTokenResponse;
import com.rebra.dto.response.LoginResponse;
import com.rebra.entity.RefreshToken;
import com.rebra.entity.User;
import com.rebra.jwt.Token;
import com.rebra.jwt.TokenProvider;
import com.rebra.repository.RefreshTokenRepository;
import com.rebra.repository.UserRepository;
import com.rebra.util.IdTokenValidator;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuth2ServiceImpl implements KakaoOAuth2Service {

    private static final String KAKAO_AUTHORIZE_BASE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String RESPONSE_TYPE_CODE = "code";
    private static final String QUERY_PARAM_GRANT_TYPE = "authorization_code";
    private static final String ERROR_TOKEN_ISSUE_FAIL = "카카오 토큰 발급 실패";
    private static final String ERROR_IDTOKEN_INVALID = "id_token 검증 실패";

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-url}")
    private String redirectUri;

    @Value("${kakao.token-url}")
    private String tokenUrl;

    private final WebClient webClient;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public String buildKakaoAuthorizeUrlAndSaveNonceInSession(HttpSession session) {
        String nonce = generateNonce();
        saveNonce(session, nonce);

        return fromUriString(KAKAO_AUTHORIZE_BASE_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", RESPONSE_TYPE_CODE)
                .queryParam("nonce", nonce)
                .build()
                .toUriString();
    }

    @Override
    public LoginResponse processLogin(KakaoTokenResponse kakaoTokenResponse, HttpSession session) {
        log.info("로그인 처리 시작");

        String kakaoSub = IdTokenValidator.getSub(kakaoTokenResponse.getIdToken());
        Optional<User> existingUser = findUserBySub(kakaoSub);
        
        // 신규 회원 - null 반환
        if (existingUser.isEmpty()) {
            return null;
        }
        
        // 기존 회원 - 로그인 처리
        User user = existingUser.get();
        // 멀티 디바이스 미지원 - 기존 토큰 모두 삭제
        deleteAllRefreshTokensByUser(user);
        // 새로운 리프레시 토큰 발급
        Token refreshJwtToken = tokenProvider.generateRefreshToken(user);
        saveRefreshTokenForUser(user, refreshJwtToken);
        return new LoginResponse(refreshJwtToken, user.getNickname());
    }


    @Override
    public Token issueAccessToken(String refreshTokenValue) {
        if (!isValidRefreshTokenInput(refreshTokenValue)) {
            throw new IllegalArgumentException("Refresh token이 유효하지 않습니다.");
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByRefreshToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token이 존재하지 않습니다."));

        if (refreshTokenEntity.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token이 만료되었습니다.");
        }

        User user = refreshTokenEntity.getUser();
        return tokenProvider.generateAccessToken(user);
    }

    @Override
    public Token issueAccessTokenByValidRefreshToken(String refreshTokenValue) {
        if (!isValidRefreshTokenInput(refreshTokenValue)) {
            return null;
        }

        return refreshTokenRepository.findByRefreshToken(refreshTokenValue)
                .filter(rt -> rt.getExpirationDate().isAfter(LocalDateTime.now()))
                .filter(rt -> rt.getUser() != null)
                .map(RefreshToken::getUser)
                .map(tokenProvider::generateAccessToken)
                .orElse(null);
    }
    
    // RTR 적용 토큰 갱신
    @Override
    public Token[] refreshTokensWithRotation(String oldRefreshToken) {
        RefreshToken oldToken = refreshTokenRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token이 존재하지 않습니다."));
        
        if (oldToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token이 만료되었습니다.");
        }
        
        User user = oldToken.getUser();
        
        // 기존 토큰 삭제 (RTR)
        refreshTokenRepository.delete(oldToken);
        
        // 새로운 토큰들 발급
        Token newAccessToken = tokenProvider.generateAccessToken(user);
        Token newRefreshToken = tokenProvider.generateRefreshToken(user);
        saveRefreshTokenForUser(user, newRefreshToken);
        
        return new Token[]{newAccessToken, newRefreshToken};
    }

    private Optional<User> findUserBySub(String kakaoSub) {
        return userRepository.findBySub(kakaoSub);
    }
    
    public User createUserWithKakaoSub(String kakaoSub, String nickname) {
        User user = User.builder()
                .sub(kakaoSub)
                .nickname(nickname)
                .build();
        return userRepository.save(user);
    }
    
    public Token generateTempTokenForSignup(String kakaoSub) {
        TempToken tempToken = new TempToken(kakaoSub);
        return tokenProvider.generateTempToken(tempToken);
    }

    public void saveRefreshTokenForUser(User user, Token refreshToken) {
        int expireMinutes = tokenProvider.getRefreshTokenExpireMinutes();
        refreshTokenRepository.save(RefreshToken.builder()
                .refreshToken(refreshToken.getToken())
                .user(user)
                .expirationDate(LocalDateTime.now().plusMinutes(expireMinutes))
                .build());
    }

    private boolean isValidRefreshTokenInput(String refreshTokenValue) {
        return refreshTokenValue != null && !refreshTokenValue.isBlank() && tokenProvider.validateToken(
                refreshTokenValue);
    }

    public void deleteAllRefreshTokensByUser(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(user);
        refreshTokenRepository.deleteAll(tokens);
    }

    @Override
    public KakaoTokenResponse fetchKakaoTokenByAuthorizationCode(String code) {
        log.info("토큰 요청 파라미터: grant_type=authorization_code, client_id={}, redirect_uri={}, code={}",
                clientId, redirectUri, code);

        KakaoTokenResponse response = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", QUERY_PARAM_GRANT_TYPE)
                        .with("client_id", clientId)
                        .with("redirect_uri", redirectUri)
                        .with("code", code)
                        .with("client_secret", clientSecret))
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();

        log.info("토큰 응답: {}", response);
        if (response == null) {
            throw new RuntimeException(ERROR_TOKEN_ISSUE_FAIL);
        }
        return response;
    }

}