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
    private BigDecimal totalValue;

    @Column(name = "cumulative_return", nullable = false)
    private BigDecimal cumulativeReturn;

    @Column(name = "daily_return", nullable = false)
    private BigDecimal dailyReturn;

    @Builder
    public PerformanceMetrics(Portfolio portfolio, LocalDate metricDate, BigDecimal totalValue,
                              BigDecimal cumulativeReturn, BigDecimal dailyReturn) {
        this.portfolio = portfolio;
        this.metricDate = metricDate;
        this.totalValue = totalValue;
        this.cumulativeReturn = cumulativeReturn;
        this.dailyReturn = dailyReturn;
    }
}