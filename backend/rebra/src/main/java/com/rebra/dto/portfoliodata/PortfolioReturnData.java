package com.rebra.dto.portfoliodata;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * 포트폴리오 수익률 계산 결과 데이터
 */
@Getter
public class PortfolioReturnData {
    private final BigDecimal totalPurchaseAmount;
    private final BigDecimal totalEvaluationAmount;
    private final BigDecimal returnAmount;
    private final BigDecimal returnRate;

    public PortfolioReturnData(BigDecimal totalPurchaseAmount, BigDecimal totalEvaluationAmount,
                               BigDecimal returnAmount, BigDecimal returnRate) {
        this.totalPurchaseAmount = totalPurchaseAmount;
        this.totalEvaluationAmount = totalEvaluationAmount;
        this.returnAmount = returnAmount;
        this.returnRate = returnRate;
    }
}