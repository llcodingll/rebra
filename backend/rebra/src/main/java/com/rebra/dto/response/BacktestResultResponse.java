package com.rebra.dto.response;

import com.rebra.entity.BacktestDetail;
import com.rebra.entity.BacktestRecord;
import com.rebra.entity.BacktestResult;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BacktestResultResponse {

    private Long id;
    private String testName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BacktestRecord.RebalancingType rebalancingType;
    private BacktestRecord.RebalancingPeriod rebalancingPeriod;
    private BacktestRecord.BacktestStatus status;
    private LocalDateTime createdAt;
    private String errorMessage;

    // 요약 결과
    private BacktestSummaryResponse summary;

    // 기간별 상세 결과
    private List<BacktestDetailResponse> details;

    @Getter
    @Builder
    public static class BacktestSummaryResponse {
        private BigDecimal finalValue;
        private BigDecimal totalReturn;
        private BigDecimal buyHoldReturn;
        private BigDecimal excessReturn;
        private BigDecimal periodGrowthRate;
        private Integer rebalancingCount;
        private BigDecimal totalFee;
        private BigDecimal totalBorrowingCost;
        private BigDecimal maxBorrowingAmount;
        private BigDecimal minCashBalance;
        private BigDecimal maxDrawdown;
        private BigDecimal volatility;
        private BigDecimal sharpeRatio;
        private BigDecimal timeWeightedReturn;

        // 퍼센트로 변환된 수치들
        private BigDecimal totalReturnPercentage;
        private BigDecimal buyHoldReturnPercentage;
        private BigDecimal excessReturnPercentage;
        private BigDecimal annualizedReturnPercentage;
        private BigDecimal maxDrawdownPercentage;
        private BigDecimal volatilityPercentage;

        public static BacktestSummaryResponse from(BacktestResult result) {
            return BacktestSummaryResponse.builder()
                    .finalValue(result.getFinalValue())
                    .totalReturn(result.getTotalReturn())
                    .buyHoldReturn(result.getBuyHoldReturn())
                    .excessReturn(result.getExcessReturn())
                    .periodGrowthRate(result.getPeriodGrowthRate())
                    .rebalancingCount(result.getRebalancingCount())
                    .totalFee(result.getTotalFee())
                    .totalBorrowingCost(result.getTotalBorrowingCost())
                    .maxBorrowingAmount(result.getMaxBorrowingAmount())
                    .minCashBalance(result.getMinCashBalance())
                    .maxDrawdown(result.getMaxDrawdown())
                    .volatility(result.getVolatility())
                    .sharpeRatio(result.getSharpeRatio())
                    .timeWeightedReturn(result.getTimeWeightedReturn())
                    .totalReturnPercentage(result.getTotalReturnPercentage())
                    .buyHoldReturnPercentage(result.getBuyHoldReturnPercentage())
                    .excessReturnPercentage(result.getExcessReturnPercentage())
                    .annualizedReturnPercentage(result.getAnnualizedReturnPercentage())
                    .maxDrawdownPercentage(result.getMaxDrawdownPercentage())
                    .volatilityPercentage(result.getVolatilityPercentage())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class BacktestDetailResponse {
        private LocalDate periodDate;
        private BigDecimal portfolioValue;
        private BigDecimal periodReturn;
        private BigDecimal periodReturnPercentage;
        private Boolean isRebalanced;
        private BigDecimal cashBalance;
        private BigDecimal dailyBorrowingInterest;
        private BigDecimal cumulativeReturn;
        private BigDecimal buyHoldReturn;
        private BigDecimal totalBuyAmount;
        private BigDecimal totalSellAmount;
        
        // 퍼센트로 변환된 수치들
        private BigDecimal cumulativeReturnPercentage;
        private BigDecimal buyHoldReturnPercentage;

        public static BacktestDetailResponse from(BacktestDetail detail) {
            return BacktestDetailResponse.builder()
                    .periodDate(detail.getPeriodDate())
                    .portfolioValue(detail.getPortfolioValue())
                    .periodReturn(detail.getPeriodReturn())
                    .periodReturnPercentage(detail.getPeriodReturnPercentage())
                    .isRebalanced(detail.getIsRebalanced())
                    .cashBalance(detail.getCashBalance())
                    .dailyBorrowingInterest(detail.getDailyBorrowingInterest())
                    .cumulativeReturn(detail.getCumulativeReturn())
                    .buyHoldReturn(detail.getBuyHoldReturn())
                    .totalBuyAmount(detail.getTotalBuyAmount())
                    .totalSellAmount(detail.getTotalSellAmount())
                    .cumulativeReturnPercentage(detail.getCumulativeReturnPercentage())
                    .buyHoldReturnPercentage(detail.getBuyHoldReturnPercentage())
                    .build();
        }
    }

    public static BacktestResultResponse from(BacktestRecord record, BacktestResult result, List<BacktestDetail> details) {
        return BacktestResultResponse.builder()
                .id(record.getId())
                .testName(record.getTestName())
                .startDate(record.getStartDate())
                .endDate(record.getEndDate())
                .rebalancingType(record.getRebalancingType())
                .rebalancingPeriod(record.getRebalancingPeriod())
                .status(record.getStatus())
                .createdAt(record.getCreatedAt())
                .errorMessage(record.getErrorMessage())
                .summary(result != null ? BacktestSummaryResponse.from(result) : null)
                .details(details != null ? details.stream()
                        .map(BacktestDetailResponse::from)
                        .toList() : null)
                .build();
    }

    // 백테스트 성공 여부
    public boolean isSuccessful() {
        return status == BacktestRecord.BacktestStatus.COMPLETED && summary != null;
    }

    // 기간 정보
    public long getPeriodDays() {
        return startDate.until(endDate).getDays() + 1;
    }

}