package com.rebra.rebalance.infrastructure.redis.dto;

import com.rebra.rebalance.domain.rebalancing.model.StockTargetDto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class RebalancingOrderMessage {
    private Long jobId;
    private Long portfolioId;
    private Long userId;
    private Long accountId;
    private String accountNumber;
    private String appKey;
    private String appSecret;
    private String accountType;
    private String executionType;
    private String strategy;
    private List<StockTargetDto> targets;
    private LocalDateTime triggeredAt;
}
