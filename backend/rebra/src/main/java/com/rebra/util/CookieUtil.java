package com.rebra.util;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final int ACCESS_TOKEN_EXPIRE_SECONDS = 60 * 15; // 15분
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final int REFRESH_TOKEN_EXPIRE_SECONDS = 60 * 60 * 24 * 7; // 7일
    public static final String TEMP_TOKEN_COOKIE_NAME = "tempToken";
    public static final int TEMP_TOKEN_EXPIRE_SECONDS = 60 * 30; // 30분

    // 상수를 활용한 편의 메서드들
    public static void addAccessTokenCookie(HttpServletResponse response, String value) {
        addCookie(response, ACCESS_TOKEN_COOKIE_NAME, value, ACCESS_TOKEN_EXPIRE_SECONDS);
    }

    public static void addRefreshTokenCookie(HttpServletResponse response, String value) {
        addCookie(response, REFRESH_TOKEN_COOKIE_NAME, value, REFRESH_TOKEN_EXPIRE_SECONDS);
    }

    public static void addTempTokenCookie(HttpServletResponse response, String value) {
        addCookie(response, TEMP_TOKEN_COOKIE_NAME, value, TEMP_TOKEN_EXPIRE_SECONDS);
    }

    // 범용 메서드 (필요시 커스텀 값 사용 가능)
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofSeconds(maxAge))
                // 도메인 설정시: .domain("domain.com")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // 각 토큰별 삭제 메서드
    public static void deleteAccessTokenCookie(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE_NAME);
    }

    public static void deleteRefreshTokenCookie(HttpServletResponse response) {
        deleteCookie(response, REFRESH_TOKEN_COOKIE_NAME);
    }

    public static void deleteTempTokenCookie(HttpServletResponse response) {
        deleteCookie(response, TEMP_TOKEN_COOKIE_NAME);
    }

    // 범용 삭제 메서드
    public static void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}