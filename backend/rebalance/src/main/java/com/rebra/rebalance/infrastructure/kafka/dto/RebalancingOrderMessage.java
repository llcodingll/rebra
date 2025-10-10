package com.rebra.rebalance.infrastructure.kafka.dto;

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
    private String accountNumber;   // 복호화된 평문
    private String appKey;          // 복호화된 평문
    private String appSecret;       // 복호화된 평문
    private String accountType;     // AccountType enum 이름
    private String executionType;   // ExecutionType enum 이름
    private String strategy;        // RebalancingStrategy enum 이름
    private Double safeAssetRatio;
    private List<StockTargetDto> targets;
    private LocalDateTime triggeredAt;
}
