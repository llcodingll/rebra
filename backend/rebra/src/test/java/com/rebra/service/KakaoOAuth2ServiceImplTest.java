package com.rebra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

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
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

class KakaoOAuth2ServiceImplTest {

    private static final String DUMMY_CLIENT_ID = "dummy-client-id";
    private static final String DUMMY_CLIENT_SECRET = "dummy-client-secret";
    private static final String DUMMY_REDIRECT_URI = "dummy-redirect-uri";
    private static final String DUMMY_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    private static final String VALID_ID_TOKEN = "valid-id-token";
    private static final String INVALID_ID_TOKEN = "invalid-id-token";

    private static final String VALID_NONCE = "valid-nonce";
    private static final String INVALID_NONCE = "invalid-nonce";

    private static final String USER_SUB_NEW = "sub123";
    private static final String USER_SUB_EXISTING = "sub456";

    private static final String NICKNAME_NEW_USER = "홍길동";
    private static final String NICKNAME_EXISTING_USER = "기존사용자";

    private static final String TOKEN_ACCESS_SAMPLE = "jwt-accesstoken-sample";
    private static final String TOKEN_REFRESH_SAMPLE = "jwt-refreshtoken-sample";
    private static final String TOKEN_TEMP_SAMPLE = "jwt-temptoken-sample";

    private static final String REFRESH_TOKEN_VALID = "jwt-valid-refresh-token";
    private static final String REFRESH_TOKEN_INVALID = "invalid-refresh-token";
    private static final String REFRESH_TOKEN_MISSING = "jwt-missing-token";
    private static final String REFRESH_TOKEN_EXPIRED = "jwt-expired-token";

    private static final Long USER_ID_1 = 1L;

    @InjectMocks
    private KakaoOAuth2ServiceImpl kakaoOAuth2Service;

    @Mock
    private WebClient webClient;

    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RequestBodySpec requestBodySpec;

    @Mock
    private ResponseSpec responseSpec;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private HttpSession httpSession;

    @BeforeEach
    void setUp() {
        openMocks(this);

        ReflectionTestUtils.setField(kakaoOAuth2Service, "clientId", DUMMY_CLIENT_ID);
        ReflectionTestUtils.setField(kakaoOAuth2Service, "clientSecret", DUMMY_CLIENT_SECRET);
        ReflectionTestUtils.setField(kakaoOAuth2Service, "redirectUri", DUMMY_REDIRECT_URI);
        ReflectionTestUtils.setField(kakaoOAuth2Service, "tokenUrl", DUMMY_TOKEN_URL);

        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn(new Token(TOKEN_ACCESS_SAMPLE));
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn(new Token(TOKEN_REFRESH_SAMPLE));
        when(tokenProvider.generateTempToken(any(TempToken.class))).thenReturn(new Token(TOKEN_TEMP_SAMPLE));
    }

    @Test
    @DisplayName("신규 사용자 로그인 시 null 반환 (회원가입 필요)")
    void processLogin_NewUser_ReturnsNull() {
        KakaoTokenResponse tokenResponse = createKakaoTokenResponse(VALID_ID_TOKEN);

        given(userRepository.findBySub(USER_SUB_NEW)).willReturn(Optional.empty());

        try (MockedStatic<IdTokenValidator> mockedIdToken = mockStatic(IdTokenValidator.class)) {
            mockedIdToken.when(() -> IdTokenValidator.getSub(VALID_ID_TOKEN)).thenReturn(USER_SUB_NEW);

            LoginResponse response = kakaoOAuth2Service.processLogin(tokenResponse, httpSession);

            assertNull(response);
        }
    }

    @Test
    @DisplayName("기존 사용자 로그인 시 LoginResponse 반환")
    void processLogin_ExistingUser_ReturnsLoginResponse() {
        KakaoTokenResponse tokenResponse = createKakaoTokenResponse(VALID_ID_TOKEN);
        User existingUser = createUser(USER_ID_1, USER_SUB_EXISTING, NICKNAME_EXISTING_USER);

        given(userRepository.findBySub(USER_SUB_EXISTING)).willReturn(Optional.of(existingUser));

        try (MockedStatic<IdTokenValidator> mockedIdToken = mockStatic(IdTokenValidator.class)) {
            mockedIdToken.when(() -> IdTokenValidator.getSub(VALID_ID_TOKEN)).thenReturn(USER_SUB_EXISTING);

            LoginResponse response = kakaoOAuth2Service.processLogin(tokenResponse, httpSession);

            assertNotNull(response);
            assertEquals(NICKNAME_EXISTING_USER, response.getNickname());
            assertEquals(TOKEN_REFRESH_SAMPLE, response.getRefreshToken().getToken());
            verify(refreshTokenRepository).deleteAll(any());
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Test
    @DisplayName("임시 토큰 생성 성공")
    void generateTempTokenForSignup_Success() {
        Token token = kakaoOAuth2Service.generateTempTokenForSignup(USER_SUB_NEW);

        assertNotNull(token);
        assertEquals(TOKEN_TEMP_SAMPLE, token.getToken());
        verify(tokenProvider).generateTempToken(any(TempToken.class));
    }

    @Test
    @DisplayName("카카오 사용자 생성 성공")
    void createUserWithKakaoSub_Success() {
        User savedUser = createUser(USER_ID_1, USER_SUB_NEW, NICKNAME_NEW_USER);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        User result = kakaoOAuth2Service.createUserWithKakaoSub(USER_SUB_NEW, NICKNAME_NEW_USER);

        assertNotNull(result);
        assertEquals(USER_SUB_NEW, result.getSub());
        assertEquals(NICKNAME_NEW_USER, result.getNickname());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰 발급 성공")
    void issueAccessToken_ValidRefreshToken_Success() {
        User user = createUser(USER_ID_1, USER_SUB_EXISTING, NICKNAME_EXISTING_USER);
        RefreshToken refreshTokenEntity = createRefreshToken(REFRESH_TOKEN_VALID, user, LocalDateTime.now().plusDays(1));

        when(tokenProvider.validateToken(REFRESH_TOKEN_VALID)).thenReturn(true);
        when(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN_VALID)).thenReturn(Optional.of(refreshTokenEntity));

        Token result = kakaoOAuth2Service.issueAccessToken(REFRESH_TOKEN_VALID);

        assertNotNull(result);
        assertEquals(TOKEN_ACCESS_SAMPLE, result.getToken());
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 액세스 토큰 발급 실패")
    void issueAccessToken_InvalidRefreshToken_ThrowsException() {
        when(tokenProvider.validateToken(REFRESH_TOKEN_INVALID)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> kakaoOAuth2Service.issueAccessToken(REFRESH_TOKEN_INVALID));

        assertEquals("Refresh token이 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 액세스 토큰 발급 실패")
    void issueAccessToken_TokenNotFound_ThrowsException() {
        when(tokenProvider.validateToken(REFRESH_TOKEN_MISSING)).thenReturn(true);
        when(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN_MISSING)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> kakaoOAuth2Service.issueAccessToken(REFRESH_TOKEN_MISSING));

        assertEquals("Refresh token이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로 액세스 토큰 발급 실패")
    void issueAccessToken_ExpiredToken_ThrowsException() {
        User user = createUser(USER_ID_1, USER_SUB_EXISTING, NICKNAME_EXISTING_USER);
        RefreshToken refreshTokenEntity = createRefreshToken(REFRESH_TOKEN_EXPIRED, user, LocalDateTime.now().minusMinutes(1));

        when(tokenProvider.validateToken(REFRESH_TOKEN_EXPIRED)).thenReturn(true);
        when(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN_EXPIRED)).thenReturn(Optional.of(refreshTokenEntity));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> kakaoOAuth2Service.issueAccessToken(REFRESH_TOKEN_EXPIRED));

        assertEquals("Refresh token이 만료되었습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("RTR 토큰 갱신 성공")
    void refreshTokensWithRotation_Success() {
        User user = createUser(USER_ID_1, USER_SUB_EXISTING, NICKNAME_EXISTING_USER);
        RefreshToken oldToken = createRefreshToken(REFRESH_TOKEN_VALID, user, LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN_VALID)).thenReturn(Optional.of(oldToken));
        when(tokenProvider.generateAccessToken(user)).thenReturn(new Token(TOKEN_ACCESS_SAMPLE));
        when(tokenProvider.generateRefreshToken(user)).thenReturn(new Token(TOKEN_REFRESH_SAMPLE));

        Token[] result = kakaoOAuth2Service.refreshTokensWithRotation(REFRESH_TOKEN_VALID);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(TOKEN_ACCESS_SAMPLE, result[0].getToken());
        assertEquals(TOKEN_REFRESH_SAMPLE, result[1].getToken());
        
        verify(refreshTokenRepository).delete(oldToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("RTR 토큰 갱신 - 존재하지 않는 토큰")
    void refreshTokensWithRotation_TokenNotFound_ThrowsException() {
        when(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN_MISSING)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> kakaoOAuth2Service.refreshTokensWithRotation(REFRESH_TOKEN_MISSING));

        assertEquals("Refresh token이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("RTR 토큰 갱신 - 만료된 토큰")
    void refreshTokensWithRotation_ExpiredToken_ThrowsException() {
        User user = createUser(USER_ID_1, USER_SUB_EXISTING, NICKNAME_EXISTING_USER);
        RefreshToken expiredToken = createRefreshToken(REFRESH_TOKEN_EXPIRED, user, LocalDateTime.now().minusMinutes(1));

        when(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN_EXPIRED)).thenReturn(Optional.of(expiredToken));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> kakaoOAuth2Service.refreshTokensWithRotation(REFRESH_TOKEN_EXPIRED));

        assertEquals("Refresh token이 만료되었습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사용자의 모든 리프레시 토큰 삭제")
    void deleteAllRefreshTokensByUser_Success() {
        User user = createUser(USER_ID_1, USER_SUB_EXISTING, NICKNAME_EXISTING_USER);
        RefreshToken token1 = createRefreshToken("token1", user, LocalDateTime.now().plusDays(1));
        RefreshToken token2 = createRefreshToken("token2", user, LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findAllByUser(user)).thenReturn(Arrays.asList(token1, token2));

        kakaoOAuth2Service.deleteAllRefreshTokensByUser(user);

        verify(refreshTokenRepository).findAllByUser(user);
        verify(refreshTokenRepository).deleteAll(Arrays.asList(token1, token2));
    }

    @Test
    @DisplayName("카카오 토큰 요청 성공")
    void fetchKakaoTokenByAuthorizationCode_Success() {
        KakaoTokenResponse tokenResponse = createKakaoTokenResponse(VALID_ID_TOKEN);
        mockKakaoTokenRequest(tokenResponse);

        KakaoTokenResponse result = kakaoOAuth2Service.fetchKakaoTokenByAuthorizationCode("valid-code");

        assertNotNull(result);
        assertEquals(VALID_ID_TOKEN, result.getIdToken());
        assertEquals("Bearer", result.getTokenType());
    }

    @Test
    @DisplayName("카카오 토큰 요청 실패 - null 응답")
    void fetchKakaoTokenByAuthorizationCode_NullResponse_ThrowsException() {
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.body(any(BodyInserter.class))).willReturn(requestBodySpec);
        given(requestBodySpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(KakaoTokenResponse.class)).willReturn(Mono.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> kakaoOAuth2Service.fetchKakaoTokenByAuthorizationCode("invalid-code"));

        assertEquals("카카오 토큰 발급 실패", exception.getMessage());
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰 발급 - issueAccessTokenByValidRefreshToken")
    void issueAccessTokenByValidRefreshToken_ValidToken_Success() {
        User user = createUser(USER_ID_1, USER_SUB_EXISTING, NICKNAME_EXISTING_USER);
        RefreshToken refreshTokenEntity = createRefreshToken(REFRESH_TOKEN_VALID, user, LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByRefreshToken(REFRESH_TOKEN_VALID)).thenReturn(Optional.of(refreshTokenEntity));
        when(tokenProvider.validateToken(REFRESH_TOKEN_VALID)).thenReturn(true);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn(new Token(TOKEN_ACCESS_SAMPLE));

        Token result = kakaoOAuth2Service.issueAccessTokenByValidRefreshToken(REFRESH_TOKEN_VALID);

        assertNotNull(result);
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰 - issueAccessTokenByValidRefreshToken null 반환")
    void issueAccessTokenByValidRefreshToken_InvalidToken_ReturnsNull() {
        Token result = kakaoOAuth2Service.issueAccessTokenByValidRefreshToken(null);
        assertNull(result);

        result = kakaoOAuth2Service.issueAccessTokenByValidRefreshToken("");
        assertNull(result);

        result = kakaoOAuth2Service.issueAccessTokenByValidRefreshToken("  ");
        assertNull(result);
    }

    private User createUser(Long id, String sub, String nickname) {
        User user = User.builder()
                .sub(sub)
                .nickname(nickname)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private RefreshToken createRefreshToken(String token, User user, LocalDateTime expiration) {
        RefreshToken refreshToken = new RefreshToken();
        ReflectionTestUtils.setField(refreshToken, "refreshToken", token);
        ReflectionTestUtils.setField(refreshToken, "user", user);
        ReflectionTestUtils.setField(refreshToken, "expirationDate", expiration);
        return refreshToken;
    }

    private KakaoTokenResponse createKakaoTokenResponse(String idToken) {
        return new KakaoTokenResponse("Bearer", "access-token", idToken, 3600, "refresh-token", 604800, "openid");
    }

    private void mockKakaoTokenRequest(KakaoTokenResponse tokenResponse) {
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.body(any(BodyInserter.class))).willReturn(requestBodySpec);
        given(requestBodySpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(KakaoTokenResponse.class)).willReturn(Mono.just(tokenResponse));
    }
}