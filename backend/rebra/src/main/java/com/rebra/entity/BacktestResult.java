package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "backtest_results")
public class BacktestResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backtest_record_id", nullable = false)
    private BacktestRecord backtestRecord;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal finalValue;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal totalReturn;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal buyHoldReturn;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal excessReturn;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal periodGrowthRate;

    @Column(nullable = false)
    private Integer rebalancingCount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalBorrowingCost;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxBorrowingAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal minCashBalance;

    @Column(precision = 10, scale = 6)
    private BigDecimal maxDrawdown;

    @Column(precision = 10, scale = 6)
    private BigDecimal volatility;

    @Column(precision = 10, scale = 6)
    private BigDecimal sharpeRatio;

    @Column(precision = 10, scale = 6)
    private BigDecimal timeWeightedReturn;

    // 수익률 계산 헬퍼 메서드
    public BigDecimal getTotalReturnPercentage() {
        return totalReturn.multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getBuyHoldReturnPercentage() {
        return buyHoldReturn.multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getExcessReturnPercentage() {
        return excessReturn.multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getAnnualizedReturnPercentage() {
        return periodGrowthRate.multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getTotalBorrowingCostSafe() {
        return totalBorrowingCost != null ? totalBorrowingCost : BigDecimal.ZERO;
    }

    public BigDecimal getMaxDrawdownPercentage() {
        return maxDrawdown != null ? maxDrawdown.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    public BigDecimal getVolatilityPercentage() {
        return volatility != null ? volatility.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    // 성과 요약 정보
    public String getPerformanceSummary() {
        return String.format("총 수익률: %.2f%%, 초과 수익률: %.2f%%, 연환산 수익률: %.2f%%, 총 비용: %,.0f원",
                getTotalReturnPercentage(),
                getExcessReturnPercentage(),
                getAnnualizedReturnPercentage(),
                getTotalCost());
    }

    // 총 비용 계산
    public BigDecimal getTotalCost() {
        BigDecimal tradingCost = totalFee != null ? totalFee : BigDecimal.ZERO;
        BigDecimal borrowingCost = totalBorrowingCost != null ? totalBorrowingCost : BigDecimal.ZERO;
        return tradingCost.add(borrowingCost);
    }

}