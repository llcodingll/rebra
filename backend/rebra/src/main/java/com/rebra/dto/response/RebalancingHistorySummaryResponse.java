package com.rebra.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class RebalancingHistorySummaryResponse {

    private PeriodInfo period;
    private Integer totalRebalances;
    private Long totalBuyAmount;
    private Long totalSellAmount;
    private Integer autoRebalances;
    private Integer manualRebalances;

    @Getter
    @Builder
    public static class PeriodInfo {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    public static RebalancingHistorySummaryResponse of(
            LocalDate startDate, LocalDate endDate,
            Integer totalRebalances, Long totalBuyAmount, Long totalSellAmount,
            Integer autoRebalances, Integer manualRebalances) {

        return RebalancingHistorySummaryResponse.builder()
                .period(PeriodInfo.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .build())
                .totalRebalances(totalRebalances)
                .totalBuyAmount(totalBuyAmount)
                .totalSellAmount(totalSellAmount)
                .autoRebalances(autoRebalances)
                .manualRebalances(manualRebalances)
                .build();
    }
}