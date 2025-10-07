package com.rebra.rebalance.infrastructure.kafka.dto;

import com.rebra.rebalance.application.rebalancing.dto.RebalancingResultEvent;
import com.rebra.rebalance.domain.rebalancing.model.OrderRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class RebalancingResultMessage {
    private Long jobId;
    private Long portfolioId;
    private String status;             // ExecutionStatus enum 이름
    private String failReason;
    private Long totalSellAmount;
    private Long totalBuyAmount;
    private List<TradeRecordDto> trades;
    private LocalDateTime completedAt;

    public static RebalancingResultMessage from(RebalancingResultEvent event) {
        List<TradeRecordDto> tradeDtos = event.getTrades() == null ? List.of() :
                event.getTrades().stream()
                        .map(t -> TradeRecordDto.builder()
                                .stockCode(t.getStockCode())
                                .stockName(t.getStockName())
                                .orderType(t.getOrderType().name())
                                .quantity(t.getQuantity())
                                .price(t.getPrice())
                                .status(t.getStatus().name())
                                .kisOrderNumber(t.getKisOrderNumber())
                                .build())
                        .collect(Collectors.toList());

        return RebalancingResultMessage.builder()
                .jobId(event.getJobId())
                .portfolioId(event.getPortfolioId())
                .status(event.getStatus().name())
                .failReason(event.getFailReason())
                .totalSellAmount(event.getTotalSellAmount())
                .totalBuyAmount(event.getTotalBuyAmount())
                .trades(tradeDtos)
                .completedAt(event.getCompletedAt())
                .build();
    }
}
