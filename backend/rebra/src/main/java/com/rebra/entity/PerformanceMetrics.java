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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "performance_metrics")
public class PerformanceMetrics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "total_value", nullable = false)
    private double totalValue;

    @Column(name = "is_rebalanced", nullable = false)
    private boolean isRebalanced;

    @Column(name = "is_sold", nullable = false)
    private boolean isSold;

    @Column(name = "is_bought", nullable = false)
    private boolean isBought;

    @Column(name = "is_composition_changed", nullable = false)
    private boolean isCompositionChanged;

    @Builder
    public PerformanceMetrics(Portfolio portfolio, LocalDate metricDate, double totalValue,
                              boolean isRebalanced, boolean isSold, boolean isBought,
                              boolean isCompositionChanged) {
        this.portfolio = portfolio;
        this.metricDate = metricDate;
        this.totalValue = totalValue;
        this.isRebalanced = isRebalanced;
        this.isSold = isSold;
        this.isBought = isBought;
        this.isCompositionChanged = isCompositionChanged;
    }

    /**
     * 성과 메트릭 정보 업데이트 (일일 수집용)
     */
    public void updateDailyMetrics(double totalValue, boolean isRebalanced,
                                  boolean isSold, boolean isBought) {
        this.totalValue = totalValue;
        this.isRebalanced = isRebalanced;
        this.isSold = isSold;
        this.isBought = isBought;
    }
}