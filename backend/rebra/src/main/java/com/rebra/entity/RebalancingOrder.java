package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "portfolio_id", nullable = false)
    private Long portfolioId;

    @Column(name = "total_buy_amount", nullable = false)
    private BigDecimal totalBuyAmount;

    @Column(name = "total_sell_amount", nullable = false)
    private BigDecimal totalSellAmount;

    @Column(name = "execution_status", nullable = false)
    private String executionStatus;

    @Column(name = "rebalancing_date", nullable = false)
    private LocalDateTime rebalancingDate;

    @Column(nullable = false)
    private String status;

    @Builder
    public RebalancingOrder(Long portfolioId, BigDecimal totalBuyAmount, BigDecimal totalSellAmount,
                            String executionStatus, LocalDateTime rebalancingDate, String status) {
        this.portfolioId = portfolioId;
        this.totalBuyAmount = totalBuyAmount;
        this.totalSellAmount = totalSellAmount;
        this.executionStatus = executionStatus;
        this.rebalancingDate = rebalancingDate;
        this.status = status;
    }
}