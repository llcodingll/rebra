package com.rebra.rebalance.infrastructure.kafka.dto;

import com.rebra.rebalance.application.rebalancing.dto.RebalancingResultEvent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RebalancingResultMessage {
    private Long jobId;
    private Long portfolioId;
    private String status;
    private String failReason;
    private Long totalSellAmount;
    private Long totalBuyAmount;
    private List<TradeRecordDto> trades;
    private LocalDateTime completedAt;

    public static RebalancingResultMessage from(RebalancingResultEvent event) {
        return RebalancingResultMessage.builder()
                .jobId(event.getJobId())
                .portfolioId(event.getPortfolioId())
                .status(event.getStatus().name())
                .failReason(event.getFailReason())
                .totalSellAmount(event.getTotalSellAmount())
                .totalBuyAmount(event.getTotalBuyAmount())
                .trades(event.getTrades() != null ? event.getTrades() : List.of())
                .completedAt(event.getCompletedAt())
                .build();
    }
}
