package com.rebra.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import com.rebra.dto.response.TokenRefreshResponse;
import com.rebra.security.CustomUserDetails;
import com.rebra.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private static final String VALID_ACCESS_TOKEN = "valid.access.token";
    private static final String EXPIRED_ACCESS_TOKEN = "expired.access.token";
    private static final String INVALID_ACCESS_TOKEN = "invalid.access.token";
    private static final String VALID_REFRESH_TOKEN = "valid.refresh.token";

    private TokenProvider tokenProvider;
    private TokenService tokenService;
    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        tokenProvider = mock(TokenProvider.class);
        tokenService = mock(TokenService.class);
        filter = new JwtAuthenticationFilter(tokenProvider, tokenService);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("Authorization 헤더의 유효한 토큰으로 인증 성공")
    void doFilter_ValidTokenInHeader_AuthenticationSet() throws ServletException, IOException {
        addAuthorizationHeader(VALID_ACCESS_TOKEN);
        mockValidToken(VALID_ACCESS_TOKEN, 1L);

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationSet(1L);
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("쿠키의 유효한 토큰으로 인증 성공")
    void doFilter_ValidTokenInCookie_AuthenticationSet() throws ServletException, IOException {
        addAccessTokenCookie(VALID_ACCESS_TOKEN);
        mockValidToken(VALID_ACCESS_TOKEN, 2L);

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationSet(2L);
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("헤더와 쿠키 모두 있을 때 헤더 토큰 우선 사용")
    void doFilter_TokensInHeaderAndCookie_HeaderTokenUsed() throws ServletException, IOException {
        addAuthorizationHeader(VALID_ACCESS_TOKEN);
        addAccessTokenCookie("cookie.token");
        mockValidToken(VALID_ACCESS_TOKEN, 10L);

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationSet(10L);
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("만료된 액세스 토큰으로 RTR 자동 갱신 성공")
    void doFilter_ExpiredAccessToken_AutoRefreshSuccess() throws ServletException, IOException {
        addAccessTokenCookie(EXPIRED_ACCESS_TOKEN);
        addRefreshTokenCookie(VALID_REFRESH_TOKEN);
        
        Token newAccessToken = new Token("new.access.token");
        Token newRefreshToken = new Token("new.refresh.token");
        TokenRefreshResponse tokenResponse = TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

        when(tokenProvider.validateToken(EXPIRED_ACCESS_TOKEN))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));
        when(tokenService.refreshTokensWithRotation(VALID_REFRESH_TOKEN)).thenReturn(tokenResponse);
        when(tokenProvider.getUserIdFromToken("new.access.token")).thenReturn(3L);
        when(tokenProvider.getUsernameFromToken("new.access.token")).thenReturn("testuser");

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationSet(3L);
        // 새 토큰이 응답 쿠키에 설정되었는지 확인
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("만료된 액세스 토큰으로 RTR 갱신 실패")
    void doFilter_ExpiredAccessToken_AutoRefreshFails() throws ServletException, IOException {
        addAccessTokenCookie(EXPIRED_ACCESS_TOKEN);
        addRefreshTokenCookie("invalid.refresh.token");

        when(tokenProvider.validateToken(EXPIRED_ACCESS_TOKEN))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));
        when(tokenService.refreshTokensWithRotation(anyString()))
                .thenThrow(new RuntimeException("Refresh token expired"));

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationNotSet();
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("서명이 잘못된 토큰으로 인증 실패")
    void doFilter_InvalidSignatureToken_AuthenticationNotSet() throws ServletException, IOException {
        addAccessTokenCookie(INVALID_ACCESS_TOKEN);

        when(tokenProvider.validateToken(INVALID_ACCESS_TOKEN))
                .thenThrow(new SignatureException("Invalid signature"));

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationNotSet();
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("토큰 없이 요청 시 인증 없이 진행")
    void doFilter_NoToken_ProceedWithoutAuthentication() throws ServletException, IOException {
        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationNotSet();
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("Bearer 접두사 없는 Authorization 헤더 무시")
    void doFilter_AuthHeaderWithoutBearer_IgnoredAndProceed() throws ServletException, IOException {
        request.addHeader(AUTH_HEADER, "NotBearer " + VALID_ACCESS_TOKEN);

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationNotSet();
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("빈 Authorization 헤더 무시")
    void doFilter_EmptyAuthHeader_IgnoredAndProceed() throws ServletException, IOException {
        request.addHeader(AUTH_HEADER, "");

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationNotSet();
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("만료된 토큰이지만 리프레시 토큰이 없을 때")
    void doFilter_ExpiredTokenNoRefreshToken_AuthenticationNotSet() throws ServletException, IOException {
        addAccessTokenCookie(EXPIRED_ACCESS_TOKEN);
        // 리프레시 토큰 쿠키 없음

        when(tokenProvider.validateToken(EXPIRED_ACCESS_TOKEN))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationNotSet();
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("필터 처리 중 예외 발생 시에도 필터 체인 계속 진행")
    void doFilter_ExceptionDuringProcessing_ContinueFilterChain() throws ServletException, IOException {
        addAccessTokenCookie(INVALID_ACCESS_TOKEN);
        
        when(tokenProvider.validateToken(INVALID_ACCESS_TOKEN))
                .thenThrow(new RuntimeException("Unexpected error"));

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationNotSet();
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("getUserIdFromToken 호출 시 예외 발생")
    void doFilter_GetUserIdThrowsException_AuthenticationNotSet() throws ServletException, IOException {
        addAccessTokenCookie(VALID_ACCESS_TOKEN);

        when(tokenProvider.validateToken(VALID_ACCESS_TOKEN)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(VALID_ACCESS_TOKEN))
                .thenThrow(new RuntimeException("Cannot extract user ID"));

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationNotSet();
        verifyFilterChainCalled();
    }

    @Test
    @DisplayName("RTR 성공 후 새 토큰으로 getUserId 호출 시 예외 발생")
    void doFilter_RTRSuccessButGetUserIdFails_AuthenticationNotSet() throws ServletException, IOException {
        addAccessTokenCookie(EXPIRED_ACCESS_TOKEN);
        addRefreshTokenCookie(VALID_REFRESH_TOKEN);

        Token newAccessToken = new Token("new.access.token");
        Token newRefreshToken = new Token("new.refresh.token");
        TokenRefreshResponse tokenResponse = TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

        when(tokenProvider.validateToken(EXPIRED_ACCESS_TOKEN))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));
        when(tokenService.refreshTokensWithRotation(VALID_REFRESH_TOKEN)).thenReturn(tokenResponse);
        when(tokenProvider.getUserIdFromToken("new.access.token"))
                .thenThrow(new RuntimeException("Cannot extract user ID"));
        when(tokenProvider.getUsernameFromToken("new.access.token")).thenReturn("testuser");

        filter.doFilterInternal(request, response, filterChain);

        assertAuthenticationNotSet();
        verifyFilterChainCalled();
    }

    private void addAuthorizationHeader(String token) {
        request.addHeader(AUTH_HEADER, BEARER_PREFIX + token);
    }

    private void addAccessTokenCookie(String token) {
        request.setCookies(new Cookie(ACCESS_TOKEN_COOKIE, token));
    }

    private void addRefreshTokenCookie(String token) {
        Cookie[] existingCookies = request.getCookies();
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, token);
        
        if (existingCookies == null) {
            request.setCookies(refreshCookie);
        } else {
            Cookie[] newCookies = new Cookie[existingCookies.length + 1];
            System.arraycopy(existingCookies, 0, newCookies, 0, existingCookies.length);
            newCookies[existingCookies.length] = refreshCookie;
            request.setCookies(newCookies);
        }
    }

    private void mockValidToken(String token, Long userId) {
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("testuser");
    }

    private void assertAuthenticationSet(Long expectedUserId) {
        assertNotNull(getContext().getAuthentication(), "Authentication should be set");
        Object principal = getContext().getAuthentication().getPrincipal();
        assertNotNull(principal, "Principal should not be null");
        
        if (principal instanceof CustomUserDetails userDetails) {
            assertEquals(expectedUserId, userDetails.getUserId());
        } else {
            fail("Principal should be an instance of CustomUserDetails, but was: " + principal.getClass().getName());
        }
    }

    private void assertAuthenticationNotSet() {
        assertNull(getContext().getAuthentication(), "Authentication should not be set");
    }

    private void verifyFilterChainCalled() throws ServletException, IOException {
        verify(filterChain).doFilter(request, response);
    }
}