package com.rebra.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;

class CookieUtilTest {

    @Test
    @DisplayName("액세스 토큰 쿠키 생성")
    void addAccessTokenCookie_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String tokenValue = "access-token-value";

        CookieUtil.addAccessTokenCookie(response, tokenValue);

        String cookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("accessToken=" + tokenValue));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("SameSite=None"));
        assertTrue(cookieHeader.contains("Max-Age=900")); // 15분 = 900초
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키 생성")
    void addRefreshTokenCookie_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String tokenValue = "refresh-token-value";

        CookieUtil.addRefreshTokenCookie(response, tokenValue);

        String cookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("refreshToken=" + tokenValue));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("SameSite=None"));
        assertTrue(cookieHeader.contains("Max-Age=604800")); // 7일 = 604800초
    }

    @Test
    @DisplayName("임시 토큰 쿠키 생성")
    void addTempTokenCookie_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String tokenValue = "temp-token-value";

        CookieUtil.addTempTokenCookie(response, tokenValue);

        String cookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("tempToken=" + tokenValue));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("SameSite=None"));
        assertTrue(cookieHeader.contains("Max-Age=1800")); // 30분 = 1800초
    }

    @Test
    @DisplayName("액세스 토큰 쿠키 삭제")
    void deleteAccessTokenCookie_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieUtil.deleteAccessTokenCookie(response);

        String cookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("accessToken="));
        assertTrue(cookieHeader.contains("Max-Age=0"));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Secure"));
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키 삭제")
    void deleteRefreshTokenCookie_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieUtil.deleteRefreshTokenCookie(response);

        String cookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("refreshToken="));
        assertTrue(cookieHeader.contains("Max-Age=0"));
    }

    @Test
    @DisplayName("임시 토큰 쿠키 삭제")
    void deleteTempTokenCookie_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieUtil.deleteTempTokenCookie(response);

        String cookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains("tempToken="));
        assertTrue(cookieHeader.contains("Max-Age=0"));
    }

    @Test
    @DisplayName("커스텀 쿠키 생성")
    void addCookie_WithCustomValues_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String name = "customCookie";
        String value = "customValue";
        int maxAge = 3600; // 1시간

        CookieUtil.addCookie(response, name, value, maxAge);

        String cookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains(name + "=" + value));
        assertTrue(cookieHeader.contains("Max-Age=" + maxAge));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("SameSite=None"));
        assertTrue(cookieHeader.contains("Path=/"));
    }

    @Test
    @DisplayName("커스텀 쿠키 삭제")
    void deleteCookie_WithCustomName_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String cookieName = "customCookie";

        CookieUtil.deleteCookie(response, cookieName);

        String cookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(cookieHeader);
        assertTrue(cookieHeader.contains(cookieName + "="));
        assertTrue(cookieHeader.contains("Max-Age=0"));
    }

    @Test
    @DisplayName("쿠키 상수값 확인")
    void cookieConstants_HaveCorrectValues() {
        assertEquals("accessToken", CookieUtil.ACCESS_TOKEN_COOKIE_NAME);
        assertEquals(900, CookieUtil.ACCESS_TOKEN_EXPIRE_SECONDS); // 15분
        assertEquals("refreshToken", CookieUtil.REFRESH_TOKEN_COOKIE_NAME);
        assertEquals(604800, CookieUtil.REFRESH_TOKEN_EXPIRE_SECONDS); // 7일
        assertEquals("tempToken", CookieUtil.TEMP_TOKEN_COOKIE_NAME);
        assertEquals(1800, CookieUtil.TEMP_TOKEN_EXPIRE_SECONDS); // 30분
    }
}