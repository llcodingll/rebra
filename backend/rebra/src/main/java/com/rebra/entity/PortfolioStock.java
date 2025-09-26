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

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "target_weight")
    private Double targetWeight;

    @Column(name = "threshold_percentage")
    private Double thresholdPercentage;

    @Column
    private String status;

    @Builder
    public PortfolioStock(Portfolio portfolio, String stockCode, Double targetWeight,
                          Double thresholdPercentage, String status) {
        this.portfolio = portfolio;
        this.stockCode = stockCode;
        this.targetWeight = targetWeight;
        this.thresholdPercentage = thresholdPercentage;
        this.status = status;
    }

    public void updateSettings(Double targetWeight, Double thresholdPercentage) {
        this.targetWeight = targetWeight;
        this.thresholdPercentage = thresholdPercentage;
    }
}