package com.rebra.rebalance.application.rebalancing.dto;

import com.rebra.rebalance.domain.rebalancing.model.ExecutionStatus;
import com.rebra.rebalance.domain.rebalancing.model.OrderRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RebalancingResultEvent {

    private Long jobId;
    private Long portfolioId;
    private ExecutionStatus status;
    private String failReason;
    private Long totalSellAmount;
    private Long totalBuyAmount;
    private List<OrderRecord> trades;
    private LocalDateTime completedAt;

    public static RebalancingResultEvent completed(Long jobId, Long portfolioId,
                                                    List<OrderRecord> trades) {
        long sellAmount = trades.stream()
                .filter(t -> t.getOrderType().name().equals("SELL")
                          && t.getStatus().name().equals("COMPLETED"))
                .mapToLong(t -> t.getPrice() != null ? t.getPrice() * t.getQuantity() : 0L)
                .sum();
        long buyAmount = trades.stream()
                .filter(t -> t.getOrderType().name().equals("BUY")
                          && t.getStatus().name().equals("COMPLETED"))
                .mapToLong(t -> t.getPrice() != null ? t.getPrice() * t.getQuantity() : 0L)
                .sum();

        return RebalancingResultEvent.builder()
                .jobId(jobId)
                .portfolioId(portfolioId)
                .status(ExecutionStatus.COMPLETED)
                .totalSellAmount(sellAmount)
                .totalBuyAmount(buyAmount)
                .trades(trades)
                .completedAt(LocalDateTime.now())
                .build();
    }

    public static RebalancingResultEvent failed(Long jobId, Long portfolioId, String reason) {
        return RebalancingResultEvent.builder()
                .jobId(jobId)
                .portfolioId(portfolioId)
                .status(ExecutionStatus.FAILED)
                .failReason(reason)
                .totalSellAmount(0L)
                .totalBuyAmount(0L)
                .trades(List.of())
                .completedAt(LocalDateTime.now())
                .build();
    }
}
