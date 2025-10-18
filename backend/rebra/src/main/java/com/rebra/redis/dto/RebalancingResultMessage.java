package com.rebra.redis.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class RebalancingResultMessage {
    private Long jobId;
    private Long portfolioId;
    private String status;
    private String failReason;
    private Long totalSellAmount;
    private Long totalBuyAmount;
    private List<Map<String, Object>> trades;
    private LocalDateTime completedAt;
}
