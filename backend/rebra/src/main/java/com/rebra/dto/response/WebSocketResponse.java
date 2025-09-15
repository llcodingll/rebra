package com.rebra.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 표준 WebSocket 메시지 응답 형식
 * websocket_style.md의 인터뷰 시스템과 동일한 구조 적용
 */
@Getter
@Builder
public class WebSocketResponse {

    /**
     * 메시지 타입 (예: "price-update", "orderbook-update", "stock-info")
     */
    private String type;

    /**
     * 실제 데이터 페이로드
     */
    private Object data;

    /**
     * 메시지 타임스탬프
     */
    private Long timestamp;

    /**
     * 정적 팩토리 메서드
     */
    public static WebSocketResponse of(String type, Object data) {
        return WebSocketResponse.builder()
                .type(type)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // 주식 관련 메시지 타입 상수
    public static class MessageType {
        public static final String PRICE_UPDATE = "price-update";
        public static final String ORDERBOOK_UPDATE = "orderbook-update";
        public static final String STOCK_INFO = "stock-info";
        public static final String SUBSCRIPTION_STARTED = "subscription-started";
        public static final String SUBSCRIPTION_STOPPED = "subscription-stopped";
        public static final String ERROR = "error";
    }
}