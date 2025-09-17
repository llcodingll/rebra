package com.rebra.dto.response;

import com.rebra.entity.Stock;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주식 상세 정보 응답")
public class StockDetailResponse {

    private StockInfo stock;
    private WebSocketInfo webSocketInfo; // WebSocket 채널 정보

    @Schema(description = "보유 정보 (보유하지 않은 경우 null)")
    private HoldingInfo holdingInfo;
    
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
                    .priceChannel("/user/queue/stock/price")
                    .orderbookChannel("/user/queue/stock/orderbook")
                    .endpoint("/ws")
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "보유 정보")
    public static class HoldingInfo {

        @Schema(description = "보유 수량", example = "100")
        private String holdingQuantity;

        @Schema(description = "매입 금액", example = "7000000")
        private String purchaseAmount;

        @Schema(description = "매입 평균 가격", example = "70000")
        private String averagePrice;

        @Schema(description = "현재 평가 금액", example = "7100000")
        private String currentValue;

        @Schema(description = "평가 손익 금액", example = "100000")
        private String profitLoss;

        @Schema(description = "평가 손익률", example = "1.43")
        private String profitLossRate;
    }

    // WebSocket 정보 포함 방식 (기존 호환성)
    public static StockDetailResponse ofWithWebSocketInfo(Stock stock, Long userId, String stockCode) {
        return StockDetailResponse.builder()
                .stock(StockInfo.from(stock))
                .webSocketInfo(WebSocketInfo.create(userId, stockCode))
                .build();
    }

    // 보유 정보 포함 방식 (새로운 기능)
    public static StockDetailResponse ofWithHoldingInfo(Stock stock, Long userId, String stockCode, HoldingInfo holdingInfo) {
        return StockDetailResponse.builder()
                .stock(StockInfo.from(stock))
                .webSocketInfo(WebSocketInfo.create(userId, stockCode))
                .holdingInfo(holdingInfo)
                .build();
    }
}