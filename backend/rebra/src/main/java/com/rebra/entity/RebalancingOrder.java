package com.rebra.entity;

import com.rebra.common.BaseEntity;
import com.rebra.enums.ExecutionType;
import com.rebra.enums.TransactionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "rebalancing_order")
public class RebalancingOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "total_buy_amount", nullable = false)
    private BigDecimal totalBuyAmount;

    @Column(name = "total_sell_amount", nullable = false)
    private BigDecimal totalSellAmount;

    @Column(name = "rebalancing_date", nullable = false)
    private LocalDateTime rebalancingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type", nullable = false)
    private ExecutionType executionType;

    @Column(name = "cumulative_return")
    private BigDecimal cumulativeReturn; // 누적 수익률 (기준: 100%)

    @Column(name = "total_portfolio_value")
    private BigDecimal totalPortfolioValue; // 리밸런싱 시점의 포트폴리오 총 평가액

    @Builder
    public RebalancingOrder(Portfolio portfolio, BigDecimal totalBuyAmount, BigDecimal totalSellAmount,
                            LocalDateTime rebalancingDate, TransactionStatus status, ExecutionType executionType,
                            BigDecimal cumulativeReturn, BigDecimal totalPortfolioValue) {
        this.portfolio = portfolio;
        this.totalBuyAmount = totalBuyAmount;
        this.totalSellAmount = totalSellAmount;
        this.rebalancingDate = rebalancingDate;
        this.status = status;
        this.executionType = executionType;
        this.cumulativeReturn = cumulativeReturn;
        this.totalPortfolioValue = totalPortfolioValue;
    }
}