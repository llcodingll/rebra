package com.rebra.config;

import com.rebra.jwt.TokenProvider;
import com.rebra.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * WebSocket 연결 시 인증 처리를 위한 핸드셰이크 인터셉터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        log.info("🔗 WebSocket 핸드셰이크 시작 - URI: {}", request.getURI());
        log.info("🔗 요청 헤더: {}", request.getHeaders());

        try {
            String token = extractTokenFromRequest(request);
            Long userId = null;
            String username = null;

            log.info("🔑 추출된 토큰: {}", token != null ?
                    "토큰 길이 " + token.length() + ", 앞 20자: " + token.substring(0, Math.min(20, token.length())) + "..." :
                    "토큰 없음");

            if (token != null) {
                try {
                    log.info("🔍 JWT 토큰 검증 시작...");
                    if (tokenProvider.validateToken(token)) {
                        userId = tokenProvider.getUserIdFromToken(token);
                        username = tokenProvider.getUsernameFromToken(token);

                        log.info("✅ WebSocket 인증 성공 - UserId: {}, Username: {}", userId, username);

                        attributes.put("userId", userId);
                        attributes.put("username", username);
                        attributes.put("authenticated", true);
                    } else {
                        log.warn("❌ WebSocket 토큰 검증 실패");
                        attributes.put("authenticated", false);
                    }
                } catch (Exception e) {
                    log.warn("❌ WebSocket JWT 처리 중 오류: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                    log.debug("JWT 처리 상세 오류", e);
                    attributes.put("authenticated", false);
                }
            } else {
                // ✅ 수정: 토큰이 없어도 연결 허용 (게스트 모드)
                log.warn("⚠️ 토큰이 제공되지 않았습니다. 게스트로 연결합니다.");
                attributes.put("authenticated", false);
            }

            // 기본 정보 저장
            attributes.put("connectTime", System.currentTimeMillis());
            attributes.put("remoteAddress", request.getRemoteAddress());

            // ✅ 인증 여부와 관계없이 연결 허용
            boolean authenticated = (Boolean) attributes.getOrDefault("authenticated", false);
            if (authenticated) {
                log.info("🎯 WebSocket 연결 허용(인증됨) - URI: {}, UserId: {}", request.getURI(), userId);
            } else {
                log.info("🎯 WebSocket 연결 허용(게스트) - URI: {}", request.getURI());
            }
            return true;

        } catch (Exception e) {
            log.error("💥 WebSocket 핸드셰이크 중 예외 발생: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            log.warn("❌ WebSocket 연결 거부 - 핸드셰이크 처리 중 예외 발생");
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 핸드셰이크 실패 - URI: {}, Error: {}", request.getURI(), exception.getMessage());
        } else {
            log.info("WebSocket 핸드셰이크 완료 - URI: {}", request.getURI());
        }
    }

    /**
     * 요청에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(ServerHttpRequest request) {
        log.info("🔍 토큰 추출 시작...");

        // 1. Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeaders().getFirst("Authorization");
        log.info("🔑 Authorization 헤더: {}", authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("✅ Authorization 헤더에서 토큰 추출 성공 (길이: {})", token.length());
            return token;
        }

        // 2. 쿼리 파라미터에서 토큰 추출
        String query = request.getURI().getQuery();
        log.info("🔍 쿼리 파라미터: {}", query);
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    String token = param.substring(6);
                    log.info("✅ 쿼리 파라미터에서 토큰 추출 성공 (길이: {})", token.length());
                    return token;
                }
            }
        }

        // 3. 쿠키에서 토큰 추출
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String cookieToken = extractTokenFromCookie(servletRequest);
            if (cookieToken != null) {
                log.info("✅ 쿠키에서 토큰 추출 성공 (길이: {})", cookieToken.length());
                return cookieToken;
            }
        }

        log.warn("❌ 모든 방법으로 토큰 추출 실패");
        return null;
    }

    /**
     * 쿠키에서 JWT 토큰 추출
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (CookieUtil.ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}