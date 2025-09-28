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


        try {
            String token = extractTokenFromRequest(request);
            Long userId = null;
            String username = null;


            if (token != null) {
                try {
                    if (tokenProvider.validateToken(token)) {
                        userId = tokenProvider.getUserIdFromToken(token);
                        username = tokenProvider.getUsernameFromToken(token);


                        attributes.put("userId", userId);
                        attributes.put("username", username);
                        attributes.put("authenticated", true);
                    } else {
                        attributes.put("authenticated", false);
                    }
                } catch (Exception e) {
                    attributes.put("authenticated", false);
                }
            } else {
                // ✅ 수정: 토큰이 없어도 연결 허용 (게스트 모드)
                attributes.put("authenticated", false);
            }

            // 기본 정보 저장
            attributes.put("connectTime", System.currentTimeMillis());
            attributes.put("remoteAddress", request.getRemoteAddress());

            // ✅ 인증 여부와 관계없이 연결 허용
            boolean authenticated = (Boolean) attributes.getOrDefault("authenticated", false);
            return true;

        } catch (Exception e) {
            log.error("WebSocket 핸드셰이크 중 예외 발생", e);
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

        // 1. Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return token;
        }

        // 2. 쿼리 파라미터에서 토큰 추출
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    String token = param.substring(6);
                    return token;
                }
            }
        }

        // 3. 쿠키에서 토큰 추출
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String cookieToken = extractTokenFromCookie(servletRequest);
            if (cookieToken != null) {
                return cookieToken;
            }
        }

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