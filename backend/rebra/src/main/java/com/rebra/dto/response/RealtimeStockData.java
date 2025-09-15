package com.rebra.dto.response;

import com.youhogeon.finance.kis_api.api.realtime.H0STCNT0Data;
import com.youhogeon.finance.kis_api.api.realtime.H0STASP0Data;
import lombok.Builder;
import lombok.Getter;

/**
 * 실시간 주식 데이터 DTO (최적화된 버전)
 * - 실시간 메시지에는 필수 데이터만 포함
 * - Stock 기본 정보는 초기 구독 시에만 전송
 * - WebSocket 연결 정보는 제거 (프론트엔드가 관리)
 */
@Getter
@Builder
public class RealtimeStockData {

    private CurrentPriceData currentPrice;
    private OrderbookData orderbook;

    /**
     * 실시간 체결가 데이터 (경량화)
     */
    @Getter
    @Builder
    public static class CurrentPriceData {
        private String timestamp;
        private Object priceData; // H0STCNT0Data를 그대로 전달

        public static CurrentPriceData from(H0STCNT0Data data) {
            return CurrentPriceData.builder()
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .priceData(data)
                    .build();
        }
    }

    /**
     * 실시간 호가 데이터 (경량화)
     */
    @Getter
    @Builder
    public static class OrderbookData {
        private String timestamp;
        private Object orderbookData; // H0STASP0Data를 그대로 전달

        public static OrderbookData from(H0STASP0Data data) {
            return OrderbookData.builder()
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .orderbookData(data)
                    .build();
        }
    }
}