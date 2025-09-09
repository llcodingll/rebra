package com.rebra.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rebra.dto.response.KakaoTokenResponse;
import com.rebra.dto.response.LoginResponse;
import com.rebra.jwt.Token;
import com.rebra.service.KakaoOAuth2Service;
import com.rebra.util.IdTokenValidator;
import com.rebra.exception.GlobalExceptionHandler;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.frontend-url=http://localhost:3000",
    "kakao.client-id=test-client-id"
})
class KakaoOAuth2ControllerTest {

    private MockMvc mockMvc;

    @Mock
    private KakaoOAuth2Service kakaoOAuth2Service;

    @InjectMocks
    private KakaoOAuth2Controller kakaoOAuth2Controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(kakaoOAuth2Controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Set properties via reflection
        ReflectionTestUtils.setField(kakaoOAuth2Controller, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(kakaoOAuth2Controller, "clientId", "test-client-id");
    }

    private KakaoTokenResponse createKakaoTokenResponse(String idToken) {
        return new KakaoTokenResponse("Bearer", "kakao-access-token", idToken, 3600, "kakao-refresh-token", 604800, "openid");
    }

    @Test
    @DisplayName("카카오 인증 URL 생성 및 리다이렉트")
    void getKakaoAuthorizationUrl_ReturnsRedirect() throws Exception {
        String authorizationUrl = "https://kauth.kakao.com/oauth/authorize?client_id=test&redirect_uri=test&response_type=code&nonce=test123";

        given(kakaoOAuth2Service.buildKakaoAuthorizeUrlAndSaveNonceInSession(any(HttpSession.class)))
                .willReturn(authorizationUrl);

        mockMvc.perform(get("/oauth2/authorization/kakao"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", authorizationUrl));
    }

    @Test
    @DisplayName("카카오 콜백 - 신규 사용자 (회원가입 페이지로 리다이렉트)")
    void handleKakaoCallback_NewUser_RedirectsToSignup() throws Exception {
        String code = "authorization-code";
        String idToken = "valid-id-token";
        String kakaoSub = "new-user-sub";
        
        KakaoTokenResponse tokenResponse = createKakaoTokenResponse(idToken);
        Token tempToken = new Token("temp-token-value");

        given(kakaoOAuth2Service.fetchKakaoTokenByAuthorizationCode(eq(code))).willReturn(tokenResponse);
        given(kakaoOAuth2Service.processLogin(eq(tokenResponse), any(HttpSession.class))).willReturn(null);
        given(kakaoOAuth2Service.generateTempTokenForSignup(eq(kakaoSub))).willReturn(tempToken);

        try (MockedStatic<IdTokenValidator> mockedValidator = mockStatic(IdTokenValidator.class)) {
            mockedValidator.when(() -> IdTokenValidator.validateIdTokenClaims(
                    eq(idToken), any(HttpSession.class), eq("test-client-id"))).thenReturn(true);
            mockedValidator.when(() -> IdTokenValidator.getSub(idToken)).thenReturn(kakaoSub);

            mockMvc.perform(get("/oauth2/authorization/kakao/callback")
                    .param("code", code))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", "http://localhost:3000/signup"))
                    .andExpect(cookie().value("tempToken", "temp-token-value"));
        }
    }

    @Test
    @DisplayName("카카오 콜백 - 기존 사용자 (메인 페이지로 리다이렉트)")
    void handleKakaoCallback_ExistingUser_RedirectsToMain() throws Exception {
        String code = "authorization-code";
        String idToken = "valid-id-token";
        String kakaoSub = "existing-user-sub";
        
        KakaoTokenResponse tokenResponse = createKakaoTokenResponse(idToken);
        Token refreshToken = new Token("refresh-token-value");
        Token accessToken = new Token("access-token-value");
        LoginResponse loginResponse = new LoginResponse(refreshToken, "기존사용자");

        given(kakaoOAuth2Service.fetchKakaoTokenByAuthorizationCode(eq(code))).willReturn(tokenResponse);
        given(kakaoOAuth2Service.processLogin(eq(tokenResponse), any(HttpSession.class))).willReturn(loginResponse);
        given(kakaoOAuth2Service.issueAccessToken(eq("refresh-token-value"))).willReturn(accessToken);

        try (MockedStatic<IdTokenValidator> mockedValidator = mockStatic(IdTokenValidator.class)) {
            mockedValidator.when(() -> IdTokenValidator.validateIdTokenClaims(
                    eq(idToken), any(HttpSession.class), eq("test-client-id"))).thenReturn(true);
            mockedValidator.when(() -> IdTokenValidator.getSub(idToken)).thenReturn(kakaoSub);

            mockMvc.perform(get("/oauth2/authorization/kakao/callback")
                    .param("code", code))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", "http://localhost:3000/main"))
                    .andExpect(cookie().value("refreshToken", "refresh-token-value"))
                    .andExpect(cookie().value("accessToken", "access-token-value"));
        }
    }

    @Test
    @DisplayName("카카오 콜백 - 인가코드 누락")
    void handleKakaoCallback_MissingCode_ThrowsException() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/kakao/callback"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카카오 콜백 - ID 토큰 검증 실패")
    void handleKakaoCallback_InvalidIdToken_ThrowsException() throws Exception {
        String code = "authorization-code";
        String idToken = "invalid-id-token";
        
        KakaoTokenResponse tokenResponse = createKakaoTokenResponse(idToken);

        given(kakaoOAuth2Service.fetchKakaoTokenByAuthorizationCode(eq(code))).willReturn(tokenResponse);

        try (MockedStatic<IdTokenValidator> mockedValidator = mockStatic(IdTokenValidator.class)) {
            mockedValidator.when(() -> IdTokenValidator.validateIdTokenClaims(
                    eq(idToken), any(HttpSession.class), eq("test-client-id"))).thenReturn(false);

            mockMvc.perform(get("/oauth2/authorization/kakao/callback")
                    .param("code", code))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("카카오 콜백 - 빈 인가코드")
    void handleKakaoCallback_EmptyCode_ThrowsException() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/kakao/callback")
                .param("code", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카카오 콜백 - 공백 인가코드")
    void handleKakaoCallback_BlankCode_ThrowsException() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/kakao/callback")
                .param("code", "   "))
                .andExpect(status().isBadRequest());
    }
}