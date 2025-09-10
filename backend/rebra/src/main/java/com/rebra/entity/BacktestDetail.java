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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "backtest_details",
    indexes = {
        @Index(name = "idx_detail_record", columnList = "backtest_record_id"),
        @Index(name = "idx_detail_record_date", columnList = "backtest_record_id, period_date")
    })
public class BacktestDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backtest_record_id", nullable = false)
    private BacktestRecord backtestRecord;

    @Column(nullable = false)
    private LocalDate periodDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal portfolioValue;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal periodReturn;

    @Column(nullable = false)
    private Boolean isRebalanced;

    @Column(precision = 15, scale = 2)
    private BigDecimal cashBalance;

    @Column(precision = 10, scale = 2)
    private BigDecimal dailyBorrowingInterest;

    @Column(precision = 10, scale = 6)
    private BigDecimal cumulativeReturn;

    @Column(precision = 10, scale = 6)
    private BigDecimal buyHoldReturn;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalBuyAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalSellAmount;

    // 수익률 관련 헬퍼 메서드
    public BigDecimal getPeriodReturnPercentage() {
        return periodReturn.multiply(BigDecimal.valueOf(100));
    }

    // 리밸런싱 여부 확인
    public boolean wasRebalanced() {
        return isRebalanced != null && isRebalanced;
    }

    // 차입 상태 확인
    public boolean isBorrowing() {
        return cashBalance != null && cashBalance.compareTo(BigDecimal.ZERO) < 0;
    }

    // 안전한 현금 잔액 반환
    public BigDecimal getSafeCashBalance() {
        return cashBalance != null ? cashBalance : BigDecimal.ZERO;
    }

    // 차입 금액 계산
    public BigDecimal getBorrowingAmount() {
        return isBorrowing() ? cashBalance.abs() : BigDecimal.ZERO;
    }

    // 누적 수익률 퍼센트
    public BigDecimal getCumulativeReturnPercentage() {
        return cumulativeReturn != null ? cumulativeReturn.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    // 바이앤홀드 수익률 퍼센트
    public BigDecimal getBuyHoldReturnPercentage() {
        return buyHoldReturn != null ? buyHoldReturn.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    // 기간별 수익 정보 요약
    public String getPeriodSummary() {
        String rebalanceInfo = wasRebalanced() ? " (리밸런싱 실행)" : "";
        String borrowingInfo = isBorrowing() ? String.format(" [차입: %,.0f원]", getBorrowingAmount()) : "";
        return String.format("%s: 포트폴리오 가치 %,.0f원, 기간 수익률 %.2f%%%s%s",
                periodDate,
                portfolioValue,
                getPeriodReturnPercentage(),
                rebalanceInfo,
                borrowingInfo);
    }

}