package com.rebra.dto.response;

import com.youhogeon.finance.kis_api.api.realtime.H0STCNT0Data;
import com.youhogeon.finance.kis_api.api.realtime.H0STASP0Data;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RealtimeStockData {
    
    private CurrentPriceData currentPrice;
    private OrderbookData orderbook;
    
    @Getter
    @Builder
    public static class CurrentPriceData {
        private String stockCode;
        private String timestamp;
        private Object priceData; // H0STCNT0Data를 그대로 전달
        
        public static CurrentPriceData from(String stockCode, H0STCNT0Data data) {
            return CurrentPriceData.builder()
                    .stockCode(stockCode)
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .priceData(data)
                    .build();
        }
    }
    
    @Getter
    @Builder
    public static class OrderbookData {
        private String stockCode;
        private String timestamp;
        private Object orderbookData; // H0STASP0Data를 그대로 전달
        
        public static OrderbookData from(String stockCode, H0STASP0Data data) {
            return OrderbookData.builder()
                    .stockCode(stockCode)
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .orderbookData(data)
                    .build();
        }
    }
}