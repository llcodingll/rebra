package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
@Table(name = "backtest_stocks",
    indexes = {
        @Index(name = "idx_backtest_stock_record", columnList = "backtest_record_id"),
        @Index(name = "idx_backtest_stock_stock", columnList = "stock_id"),
        @Index(name = "idx_backtest_stock_record_stock", columnList = "backtest_record_id, stock_id")
    })
public class BacktestStock extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "backtest_stock_seq_generator",
        sequenceName = "backtest_stock_seq",
        allocationSize = 100
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "backtest_stock_seq_generator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backtest_record_id", nullable = false)
    private BacktestRecord backtestRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Double targetWeight;

    @Column
    private Double thresholdPercentage;

    @Column
    private Integer shares;

    // 편의 메서드들
    public String getStockCode() {
        return stock != null ? stock.getStockCode() : null;
    }

    public String getStockName() {
        return stock != null ? stock.getStockName() : null;
    }

    public String getStockType() {
        return stock != null ? stock.getStockType() : null;
    }

    public Double getTargetWeightPercentage() {
        return targetWeight != null ? targetWeight : 0.0;
    }

    public Double getThresholdPercentageSafe() {
        return thresholdPercentage != null ? thresholdPercentage : 0.0;
    }

    public Integer getSharesSafe() {
        return shares != null ? shares : 0;
    }

    // 백테스트 주식 정보 요약
    public String getStockSummary() {
        return String.format("%s(%s): 목표비중 %.1f%%, 보유수량 %d주",
                getStockName(),
                getStockCode(),
                getTargetWeightPercentage(),
                getSharesSafe());
    }

    // 임계값 기반 리밸런싱 여부 확인
    public boolean isThresholdBased() {
        return thresholdPercentage != null && thresholdPercentage > 0.0;
    }
}