package com.rebra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리밸런싱 실행 결과 응답")
public class RebalancingExecutionResponse {

    @Schema(description = "실행 성공 여부")
    private boolean success;

    @Schema(description = "리밸런싱 주문 ID")
    private Long rebalancingOrderId;

    @Schema(description = "실행 시각")
    private LocalDateTime executionTime;

    @Schema(description = "총 매수 금액")
    private BigDecimal totalBuyAmount;

    @Schema(description = "총 매도 금액")
    private BigDecimal totalSellAmount;

    @Schema(description = "전체 포트폴리오 평가액")
    private BigDecimal totalPortfolioValue;

    @Schema(description = "개별 주문 결과")
    private List<OrderResult> orderResults;

    @Schema(description = "실패 사유 (실패 시)")
    private String failureReason;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 주문 결과")
    public static class OrderResult {

        @Schema(description = "종목 코드")
        private String stockCode;

        @Schema(description = "종목명")
        private String stockName;

        @Schema(description = "주문 유형 (BUY/SELL)")
        private String orderType;

        @Schema(description = "주문 수량")
        private Integer quantity;

        @Schema(description = "주문 가격")
        private BigDecimal price;

        @Schema(description = "주문 성공 여부")
        private boolean success;

        @Schema(description = "주문 ID (성공 시)")
        private String orderId;

        @Schema(description = "실패 사유 (실패 시)")
        private String errorMessage;
    }

    public static RebalancingExecutionResponse success(Long rebalancingOrderId, BigDecimal totalBuyAmount,
                                                       BigDecimal totalSellAmount, BigDecimal totalPortfolioValue,
                                                       List<OrderResult> orderResults) {
        return RebalancingExecutionResponse.builder()
                .success(true)
                .rebalancingOrderId(rebalancingOrderId)
                .executionTime(LocalDateTime.now())
                .totalBuyAmount(totalBuyAmount)
                .totalSellAmount(totalSellAmount)
                .totalPortfolioValue(totalPortfolioValue)
                .orderResults(orderResults)
                .build();
    }

    public static RebalancingExecutionResponse failure(String reason) {
        return RebalancingExecutionResponse.builder()
                .success(false)
                .executionTime(LocalDateTime.now())
                .failureReason(reason)
                .build();
    }
}