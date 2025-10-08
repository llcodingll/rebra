package com.rebra.entity;

import com.rebra.common.BaseEntity;
import com.rebra.enums.ExecutionType;
import com.rebra.enums.TransactionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(
        name = "rebalancing_order_seq_generator",
        sequenceName = "rebalancing_order_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rebalancing_order_seq_generator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "total_buy_amount", nullable = false)
    private Long totalBuyAmount;

    @Column(name = "total_sell_amount", nullable = false)
    private Long totalSellAmount;

    @Column(name = "rebalancing_date", nullable = false)
    private LocalDateTime rebalancingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type", nullable = false)
    private ExecutionType executionType;

    @Column(name = "cumulative_return")
    private Double cumulativeReturn; // 누적 수익률 (기준: 1.0)

    @Column(name = "total_portfolio_value")
    private Long totalPortfolioValue; // 리밸런싱 시점의 포트폴리오 총 평가액

    @OneToMany(mappedBy = "rebalancingOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TradeRecord> tradeRecords = new ArrayList<>();

    @Builder
    public RebalancingOrder(Portfolio portfolio, Long totalBuyAmount, Long totalSellAmount,
                            LocalDateTime rebalancingDate, TransactionStatus status, ExecutionType executionType,
                            Double cumulativeReturn, Long totalPortfolioValue) {
        this.portfolio = portfolio;
        this.totalBuyAmount = totalBuyAmount;
        this.totalSellAmount = totalSellAmount;
        this.rebalancingDate = rebalancingDate;
        this.status = status;
        this.executionType = executionType;
        this.cumulativeReturn = cumulativeReturn;
        this.totalPortfolioValue = totalPortfolioValue;
    }

    public void updateStatus(TransactionStatus status) {
        this.status = status;
    }

    public void updateAmounts(Long totalBuyAmount, Long totalSellAmount) {
        this.totalBuyAmount = totalBuyAmount;
        this.totalSellAmount = totalSellAmount;
    }
}