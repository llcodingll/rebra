package com.rebra.controller;

import com.rebra.dto.response.KakaoTokenResponse;
import com.rebra.dto.response.LoginResponse;
import com.rebra.jwt.Token;
import com.rebra.service.KakaoOAuth2Service;
import com.rebra.util.CookieUtil;
import com.rebra.util.IdTokenValidator;
import static com.rebra.exception.ExceptionCode.*;
import com.rebra.exception.auth.AuthException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2/authorization/kakao")
@Tag(name = "OAuth2 API", description = "카카오 OAuth2 로그인 API")
public class KakaoOAuth2Controller {

    private static final String AUTHORIZATION_CODE_PARAM = "code";

    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    @Value("${kakao.client-id}")
    private String clientId;

    private final KakaoOAuth2Service kakaoOAuth2Service;

    @Operation(summary = "카카오 로그인 시작", description = "카카오 OAuth2 인증 페이지로 리다이렉트합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "카카오 인증 페이지로 리다이렉트")
    })
    @GetMapping
    public ResponseEntity<Void> getKakaoAuthorizationUrl(HttpSession session) {
        String authorizationUrl = kakaoOAuth2Service.buildKakaoAuthorizeUrlAndSaveNonceInSession(session);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", authorizationUrl)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> handleKakaoCallback(
            @RequestParam(AUTHORIZATION_CODE_PARAM) String code, HttpSession session, HttpServletResponse response) {

        log.info("Kakao OAuth2 callback received. authorization code: {}", code);

        if (code == null || code.trim().isEmpty()) {
            log.warn("인가코드가 빈 값으로 들어옴");
            throw AuthException.missingAuthorizationCode();
        }
        
        // 카카오 토큰 요청 (한 번만)
        KakaoTokenResponse kakaoTokenResponse = kakaoOAuth2Service.fetchKakaoTokenByAuthorizationCode(code);
        
        // ID토큰 검증
        boolean valid = IdTokenValidator.validateIdTokenClaims(kakaoTokenResponse.getIdToken(), session, clientId);
        if (!valid) {
            throw AuthException.invalidIdToken();
        }
        
        // sub 추출
        String kakaoSub = IdTokenValidator.getSub(kakaoTokenResponse.getIdToken());

        // 로그인 처리
        LoginResponse loginResponse = kakaoOAuth2Service.processLogin(kakaoTokenResponse, session);

        // 신규 회원 - 임시 토큰 생성 후 회원가입 페이지로
        if (loginResponse == null) {
            Token tempToken = kakaoOAuth2Service.generateTempTokenForSignup(kakaoSub);
            CookieUtil.addTempTokenCookie(response, tempToken.getToken());
            
            log.info("신규 회원 회원가입 페이지로 리다이렉트: kakaoSub={}", kakaoSub);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "/signup")
                    .build();
        }
        
        // 기존 회원 - Access Token 발급 후 메인 페이지로
        Token accessToken = kakaoOAuth2Service.issueAccessToken(loginResponse.getRefreshToken().getToken());
        CookieUtil.addRefreshTokenCookie(response, loginResponse.getRefreshToken().getToken());
        CookieUtil.addAccessTokenCookie(response, accessToken.getToken());
        
        log.info("기존 회원 로그인 성공: nickname={}", loginResponse.getNickname());
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendUrl + "/dashboard")
                .build();
    }

}