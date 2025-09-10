package com.rebra.dto.response;

import com.rebra.entity.BacktestDetail;
import com.rebra.entity.BacktestRecord;
import com.rebra.entity.BacktestStock;
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

    // 포트폴리오 구성 종목
    private List<BacktestStockResponse> portfolioStocks;

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

        public static BacktestSummaryResponse from(BacktestRecord record) {
            return BacktestSummaryResponse.builder()
                    .finalValue(record.getFinalValue())
                    .totalReturn(record.getTotalReturn())
                    .buyHoldReturn(record.getBuyHoldReturn())
                    .excessReturn(record.getExcessReturn())
                    .periodGrowthRate(record.getPeriodGrowthRate())
                    .rebalancingCount(record.getRebalancingCount())
                    .totalFee(record.getTotalFee())
                    .totalBorrowingCost(record.getTotalBorrowingCost())
                    .maxBorrowingAmount(record.getMaxBorrowingAmount())
                    .minCashBalance(record.getMinCashBalance())
                    .maxDrawdown(record.getMaxDrawdown())
                    .volatility(record.getVolatility())
                    .sharpeRatio(record.getSharpeRatio())
                    .timeWeightedReturn(record.getTimeWeightedReturn())
                    .totalReturnPercentage(record.getTotalReturnPercentage())
                    .buyHoldReturnPercentage(record.getBuyHoldReturnPercentage())
                    .excessReturnPercentage(record.getExcessReturnPercentage())
                    .annualizedReturnPercentage(record.getAnnualizedReturnPercentage())
                    .maxDrawdownPercentage(record.getMaxDrawdownPercentage())
                    .volatilityPercentage(record.getVolatilityPercentage())
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

    @Getter
    @Builder
    public static class BacktestStockResponse {
        private String stockCode;
        private String stockName;
        private String stockType;
        private BigDecimal targetWeight;
        private BigDecimal thresholdPercentage;
        private Integer finalShares;
        private Boolean isActive;

        public static BacktestStockResponse from(BacktestStock backtestStock) {
            return BacktestStockResponse.builder()
                    .stockCode(backtestStock.getStockCode())
                    .stockName(backtestStock.getStockName())
                    .stockType(backtestStock.getStockType())
                    .targetWeight(backtestStock.getTargetWeight())
                    .thresholdPercentage(backtestStock.getThresholdPercentage())
                    .finalShares(backtestStock.getShares())
                    .isActive(backtestStock.getStock().getIsActive())
                    .build();
        }
    }

    public static BacktestResultResponse from(BacktestRecord record, List<BacktestDetail> details, List<BacktestStock> portfolioStocks) {
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
                .summary(record.hasResults() ? BacktestSummaryResponse.from(record) : null)
                .details(details != null ? details.stream()
                        .map(BacktestDetailResponse::from)
                        .toList() : null)
                .portfolioStocks(portfolioStocks != null ? portfolioStocks.stream()
                        .map(BacktestStockResponse::from)
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

    // 포트폴리오 종목 수
    public int getPortfolioStockCount() {
        return portfolioStocks != null ? portfolioStocks.size() : 0;
    }

    // 포트폴리오 요약 정보
    public String getPortfolioSummary() {
        if (portfolioStocks == null || portfolioStocks.isEmpty()) {
            return "포트폴리오 구성 정보 없음";
        }
        
        return portfolioStocks.stream()
                .map(stock -> String.format("%s(%.1f%%)", 
                        stock.getStockCode(), 
                        stock.getTargetWeight()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("정보 없음");
    }

}