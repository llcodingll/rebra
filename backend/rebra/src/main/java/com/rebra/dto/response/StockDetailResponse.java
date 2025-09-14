package com.rebra.dto.response;

import com.rebra.entity.Stock;
import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter
@Builder
public class StockDetailResponse {
    
    private StockInfo stock;
    private RealtimeStockData realtime;
    private WebSocketInfo webSocketInfo; // WebSocket 채널 정보
    
    @Getter
    @Builder
    public static class StockInfo {
        private Long id;
        private String stockCode;
        private String stockName;
        private String stockType;
        private Boolean isActive;
        
        public static StockInfo from(Stock stock) {
            return StockInfo.builder()
                    .id(stock.getId())
                    .stockCode(stock.getStockCode())
                    .stockName(stock.getStockName())
                    .stockType(stock.getStockType())
                    .isActive(stock.getIsActive())
                    .build();
        }
    }
    
    @Getter
    @Builder
    public static class WebSocketInfo {
        private String priceChannel;    // 체결가 채널
        private String orderbookChannel; // 호가 채널
        private String endpoint;         // WebSocket 엔드포인트
        
        public static WebSocketInfo create(Long userId, String stockCode) {
            return WebSocketInfo.builder()
                    .priceChannel("/topic/stock/" + userId + "/" + stockCode + "/price")
                    .orderbookChannel("/topic/stock/" + userId + "/" + stockCode + "/orderbook")
                    .endpoint("/ws")
                    .build();
        }
    }
    
    // 기존 방식 (실시간 데이터 포함)
    public static StockDetailResponse of(Stock stock, RealtimeStockData realtimeData) {
        return StockDetailResponse.builder()
                .stock(StockInfo.from(stock))
                .realtime(realtimeData)
                .build();
    }
    
    // 하이브리드 방식 (WebSocket 정보 포함)
    public static StockDetailResponse ofWithWebSocketInfo(Stock stock, Long userId, String stockCode) {
        return StockDetailResponse.builder()
                .stock(StockInfo.from(stock))
                .webSocketInfo(WebSocketInfo.create(userId, stockCode))
                .build();
    }
}