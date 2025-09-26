package com.rebra.dto.response;

import com.rebra.entity.RebalancingOrder;
import com.rebra.enums.ExecutionType;
import com.rebra.enums.TransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RebalancingHistoryResponse {

    private Long orderId;
    private ExecutionType executionType;
    private Integer totalStocks;
    private Long totalBuyAmount;
    private Long totalSellAmount;
    private Long totalPortfolioValue;
    private TransactionStatus status;
    private LocalDateTime executedAt;

    public static RebalancingHistoryResponse from(RebalancingOrder order, Integer totalStocks) {
        return RebalancingHistoryResponse.builder()
                .orderId(order.getId())
                .executionType(order.getExecutionType())
                .totalStocks(totalStocks)
                .totalBuyAmount(order.getTotalBuyAmount())
                .totalSellAmount(order.getTotalSellAmount())
                .totalPortfolioValue(order.getTotalPortfolioValue())
                .status(order.getStatus())
                .executedAt(order.getRebalancingDate())
                .build();
    }
}