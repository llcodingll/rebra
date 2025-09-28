package com.rebra.dto.portfoliodata;

import lombok.Getter;

/**
 * 포트폴리오 수익률 계산 결과 데이터
 */
@Getter
public class PortfolioReturnData {
    private final Long totalPurchaseAmount;
    private final Long totalEvaluationAmount;
    private final Long returnAmount;
    private final Double returnRate;

    public PortfolioReturnData(Long totalPurchaseAmount, Long totalEvaluationAmount,
                               Long returnAmount, Double returnRate) {
        this.totalPurchaseAmount = totalPurchaseAmount;
        this.totalEvaluationAmount = totalEvaluationAmount;
        this.returnAmount = returnAmount;
        this.returnRate = returnRate;
    }
}