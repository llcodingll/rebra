package com.rebra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import jakarta.annotation.PostConstruct;

/**
 * WebSocket 버퍼 크기 설정을 위한 Configuration
 * KIS API 대용량 실시간 데이터 처리를 위해 버퍼 크기 증가
 */
@Slf4j
@Configuration
public class WebSocketBufferConfig implements WebSocketConfigurer {

    @PostConstruct
    public void logWebSocketConfig() {
        // 시스템 속성으로 Tomcat WebSocket 버퍼 크기 설정
        System.setProperty("org.apache.tomcat.websocket.textBufferSize", "1048576"); // 1MB
        System.setProperty("org.apache.tomcat.websocket.binaryBufferSize", "1048576"); // 1MB
        System.setProperty("org.apache.tomcat.websocket.SESSION_TIMEOUT", "1800000"); // 30분 (ms)
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 기본적으로 별도 핸들러 등록은 필요하지 않음
        // Spring의 STOMP 설정이 이미 WebSocketConfig에서 처리됨
    }

    /**
     * WebSocket HandshakeHandler에 버퍼 크기 설정 적용
     */
    public DefaultHandshakeHandler createHandshakeHandler() {
        DefaultHandshakeHandler handshakeHandler = new DefaultHandshakeHandler();

        // Jetty 사용 시 (필요한 경우) - 메서드가 존재하지 않으므로 주석 처리
        // try {
        //     JettyRequestUpgradeStrategy upgradeStrategy = new JettyRequestUpgradeStrategy();
        //     handshakeHandler.setRequestUpgradeStrategy(upgradeStrategy);
        //     log.debug("Jetty WebSocket upgrade strategy configured");
        // } catch (Exception e) {
        //     log.debug("Jetty not available, using default upgrade strategy");
        // }

        return handshakeHandler;
    }
}