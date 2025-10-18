package com.rebra.rebalance.application.rebalancing.dto;

import com.rebra.rebalance.domain.rebalancing.model.ExecutionStatus;
import com.rebra.rebalance.domain.rebalancing.model.OrderRecord;
import com.rebra.rebalance.domain.rebalancing.model.OrderStatus;
import com.rebra.rebalance.domain.rebalancing.model.OrderType;
import com.rebra.rebalance.infrastructure.redis.dto.TradeRecordDto;
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
    private List<TradeRecordDto> trades;
    private LocalDateTime completedAt;

    public static RebalancingResultEvent completed(Long jobId, Long portfolioId,
                                                    List<OrderRecord> trades) {
        long sellAmount = trades.stream()
                .filter(t -> t.getOrderType() == OrderType.SELL
                          && t.getStatus() == OrderStatus.COMPLETED)
                .mapToLong(t -> t.getPrice() != null ? t.getPrice() * t.getQuantity() : 0L)
                .sum();
        long buyAmount = trades.stream()
                .filter(t -> t.getOrderType() == OrderType.BUY
                          && t.getStatus() == OrderStatus.COMPLETED)
                .mapToLong(t -> t.getPrice() != null ? t.getPrice() * t.getQuantity() : 0L)
                .sum();

        List<TradeRecordDto> tradeDtos = trades.stream()
                .map(t -> TradeRecordDto.builder()
                        .stockCode(t.getStockCode())
                        .stockName(t.getStockName())
                        .orderType(t.getOrderType().name())
                        .quantity(t.getQuantity())
                        .price(t.getPrice())
                        .status(t.getStatus().name())
                        .kisOrderNumber(t.getKisOrderNumber())
                        .build())
                .toList();

        return RebalancingResultEvent.builder()
                .jobId(jobId)
                .portfolioId(portfolioId)
                .status(ExecutionStatus.COMPLETED)
                .totalSellAmount(sellAmount)
                .totalBuyAmount(buyAmount)
                .trades(tradeDtos)
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
