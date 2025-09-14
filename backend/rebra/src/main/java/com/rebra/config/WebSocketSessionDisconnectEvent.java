package com.rebra.config;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * WebSocket 세션 연결 해제 이벤트
 */
@Getter
public class WebSocketSessionDisconnectEvent extends ApplicationEvent {

    private final String sessionId;
    private final String userId;

    public WebSocketSessionDisconnectEvent(Object source, String sessionId, String userId) {
        super(source);
        this.sessionId = sessionId;
        this.userId = userId;
    }
}