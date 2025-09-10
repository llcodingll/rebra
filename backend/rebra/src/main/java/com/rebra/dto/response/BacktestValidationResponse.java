package com.rebra.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class BacktestValidationResponse {

    private boolean isValid;
    private ValidationSummary summary;
    private List<StockValidation> stockValidations;
    private List<String> warnings;
    private List<String> errors;

    @Getter
    @Builder
    public static class ValidationSummary {
        private LocalDate actualStartDate;
        private LocalDate actualEndDate;
        private int totalTradingDays;
        private int expectedRebalancingCount;
        private double overallDataCoverage;
        private int validStockCount;
        private int totalStockCount;
        private List<LocalDate> rebalancingDates;
    }

    @Getter
    @Builder
    public static class StockValidation {
        private String ticker;
        private String name;
        private boolean isValid;
        private LocalDate dataStartDate;
        private LocalDate dataEndDate;
        private double dataCoverage;
        private long dataCount;
        private String issue;
        private String suggestion;
    }

    public static BacktestValidationResponse success(ValidationSummary summary, 
                                                   List<StockValidation> stockValidations,
                                                   List<String> warnings) {
        return BacktestValidationResponse.builder()
                .isValid(true)
                .summary(summary)
                .stockValidations(stockValidations)
                .warnings(warnings)
                .build();
    }

    public static BacktestValidationResponse failure(List<String> errors,
                                                   List<StockValidation> stockValidations) {
        return BacktestValidationResponse.builder()
                .isValid(false)
                .errors(errors)
                .stockValidations(stockValidations)
                .build();
    }
}