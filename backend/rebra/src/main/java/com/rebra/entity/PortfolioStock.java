package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "portfolio_stock")
public class PortfolioStock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "target_weight", nullable = false)
    private BigDecimal targetWeight;

    @Column(name = "threshold_percentage", nullable = false)
    private BigDecimal thresholdPercentage;

    @Column(name = "min_weight", nullable = false)
    private BigDecimal minWeight;

    @Column(name = "max_weight", nullable = false)
    private BigDecimal maxWeight;

    @Column(nullable = false)
    private String status;

    @Builder
    public PortfolioStock(Portfolio portfolio, Stock stock, BigDecimal targetWeight,
                          BigDecimal thresholdPercentage, BigDecimal minWeight,
                          BigDecimal maxWeight, String status) {
        this.portfolio = portfolio;
        this.stock = stock;
        this.targetWeight = targetWeight;
        this.thresholdPercentage = thresholdPercentage;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.status = status;
    }
}