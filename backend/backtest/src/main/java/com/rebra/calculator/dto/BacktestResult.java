package com.rebra.calculator.dto;

import java.util.List;

/**
 * 백테스트 실행 결과를 담는 클래스
 */
public class BacktestResult {
    private BacktestSummaryDto summary;
    private List<BacktestDetailDto> details;
    private double initialValue;

    public BacktestResult() {}

    public BacktestResult(BacktestSummaryDto summary, List<BacktestDetailDto> details, double initialValue) {
        this.summary = summary;
        this.details = details;
        this.initialValue = initialValue;
    }

    public BacktestSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(BacktestSummaryDto summary) {
        this.summary = summary;
    }

    public List<BacktestDetailDto> getDetails() {
        return details;
    }

    public void setDetails(List<BacktestDetailDto> details) {
        this.details = details;
    }

    public double getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(double initialValue) {
        this.initialValue = initialValue;
    }
}