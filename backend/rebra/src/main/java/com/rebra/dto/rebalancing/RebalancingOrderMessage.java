package com.rebra.dto.rebalancing;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RebalancingOrderMessage {

    private Long jobId;
    private Long portfolioId;
    private Long userId;
    private Long accountId;
    private String accountNumber;   // 복호화된 평문
    private String appKey;          // 복호화된 평문
    private String appSecret;       // 복호화된 평문
    private String accountType;
    private String executionType;
    private String strategy;
    private List<StockTargetDto> targets;
    private LocalDateTime triggeredAt;

    @Data
    @Builder
    public static class StockTargetDto {
        private String stockCode;
        private String stockName;
        private Double targetWeight;
        private Double thresholdPercentage;
    }
}
