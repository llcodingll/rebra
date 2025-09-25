package com.rebra.config;

import org.springframework.context.ApplicationEvent;

/**
 * 세션 활동 업데이트 이벤트
 * 순환 의존성을 방지하기 위해 이벤트 기반으로 세션 활동을 업데이트
 */
public class SessionActivityUpdateEvent extends ApplicationEvent {

    private final String sessionId;
    private final String eventType; // "DATA_RECEIVED", "SUBSCRIPTION", etc.

    public SessionActivityUpdateEvent(Object source, String sessionId, String eventType) {
        super(source);
        this.sessionId = sessionId;
        this.eventType = eventType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return String.format("SessionActivityUpdateEvent{sessionId='%s', eventType='%s'}",
                            sessionId, eventType);
    }
}