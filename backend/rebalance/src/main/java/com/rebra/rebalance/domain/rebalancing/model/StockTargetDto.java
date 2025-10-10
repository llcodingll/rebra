package com.rebra.rebalance.domain.rebalancing.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockTargetDto {
    private String stockCode;
    private String stockName;
    private Double targetWeight;        // 목표 비중 (0.0 ~ 1.0)
    private Double thresholdPercentage; // 임계값 (상대적 %)
}
