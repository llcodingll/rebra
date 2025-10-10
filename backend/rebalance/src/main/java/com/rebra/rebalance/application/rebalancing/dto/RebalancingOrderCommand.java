package com.rebra.rebalance.application.rebalancing.dto;

import com.rebra.rebalance.domain.rebalancing.model.AccountType;
import com.rebra.rebalance.domain.rebalancing.model.ExecutionType;
import com.rebra.rebalance.domain.rebalancing.model.RebalancingStrategy;
import com.rebra.rebalance.infrastructure.kafka.dto.RebalancingOrderMessage;
import com.rebra.rebalance.domain.rebalancing.model.StockTargetDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RebalancingOrderCommand {

    private Long jobId;
    private Long portfolioId;
    private String accountNumber;
    private String appKey;
    private String appSecret;
    private AccountType accountType;
    private ExecutionType executionType;
    private RebalancingStrategy strategy;
    private Double safeAssetRatio;
    private List<StockTargetDto> targets;
    private LocalDateTime triggeredAt;

    public static RebalancingOrderCommand from(RebalancingOrderMessage msg) {
        return RebalancingOrderCommand.builder()
                .jobId(msg.getJobId())
                .portfolioId(msg.getPortfolioId())
                .accountNumber(msg.getAccountNumber())
                .appKey(msg.getAppKey())
                .appSecret(msg.getAppSecret())
                .accountType(AccountType.valueOf(msg.getAccountType()))
                .executionType(ExecutionType.valueOf(msg.getExecutionType()))
                .strategy(RebalancingStrategy.valueOf(msg.getStrategy()))
                .safeAssetRatio(msg.getSafeAssetRatio())
                .targets(msg.getTargets())
                .triggeredAt(msg.getTriggeredAt())
                .build();
    }
}
