package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "portfolio_id", nullable = false)
    private Long portfolioId;

    @Column(name = "stock_id", nullable = false)
    private Long stockId;

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
    public PortfolioStock(Long portfolioId, Long stockId, BigDecimal targetWeight,
                          BigDecimal thresholdPercentage, BigDecimal minWeight,
                          BigDecimal maxWeight, String status) {
        this.portfolioId = portfolioId;
        this.stockId = stockId;
        this.targetWeight = targetWeight;
        this.thresholdPercentage = thresholdPercentage;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.status = status;
    }
}