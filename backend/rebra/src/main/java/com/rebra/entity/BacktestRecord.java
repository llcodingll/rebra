package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "backtest_records",
    indexes = {
        @Index(name = "idx_backtest_user", columnList = "user_id"),
        @Index(name = "idx_backtest_status", columnList = "status"),
        @Index(name = "idx_backtest_user_created", columnList = "user_id, created_at")
    })
public class BacktestRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String testName;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RebalancingType rebalancingType;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private RebalancingPeriod rebalancingPeriod;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BacktestStatus status;

    @Column(length = 500)
    private String errorMessage;

    @OneToMany(mappedBy = "backtestRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BacktestStock> backtestStocks = new ArrayList<>();

    // 백테스트 결과 필드들 (완료 후 업데이트)
    @Column(precision = 15, scale = 2)
    private BigDecimal initialCapital;

    @Column(precision = 15, scale = 2)
    private BigDecimal finalValue;

    @Column(precision = 10, scale = 6)
    private BigDecimal totalReturn;

    @Column(precision = 10, scale = 6)
    private BigDecimal buyHoldReturn;

    @Column(precision = 10, scale = 6)
    private BigDecimal excessReturn;

    @Column(precision = 10, scale = 6)
    private BigDecimal periodGrowthRate;

    @Column
    private Integer rebalancingCount;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalBorrowingCost;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxBorrowingAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal minCashBalance;

    @Column(precision = 10, scale = 6)
    private BigDecimal maxDrawdown;

    @Column(precision = 10, scale = 6)
    private BigDecimal volatility;

    @Column(precision = 10, scale = 6)
    private BigDecimal sharpeRatio;

    @Column(precision = 10, scale = 6)
    private BigDecimal timeWeightedReturn;


    public enum BacktestStatus {
        PENDING,    // 요청됨
        PROCESSING, // 계산 중
        COMPLETED,  // 완료
        FAILED      // 실패
    }

    public enum RebalancingType {
        THRESHOLD,  // 임계값 기반
        PERIODIC    // 주기적
    }

    public enum RebalancingPeriod {
        MONTHLY,    // 월말
        QUARTERLY   // 분기말
    }

    // 상태 변경 메서드
    public void updateStatus(BacktestStatus newStatus) {
        this.status = newStatus;
    }

    public void updateStatus(BacktestStatus newStatus, String errorMessage) {
        this.status = newStatus;
        this.errorMessage = errorMessage;
    }

    // 백테스트 기간 계산
    public long getPeriodDays() {
        return startDate.until(endDate).getDays() + 1;
    }

    // 진행 중인지 확인
    public boolean isInProgress() {
        return status == BacktestStatus.PENDING || status == BacktestStatus.PROCESSING;
    }

    // 완료되었는지 확인
    public boolean isCompleted() {
        return status == BacktestStatus.COMPLETED;
    }

    // 실패했는지 확인
    public boolean isFailed() {
        return status == BacktestStatus.FAILED;
    }

    // 백테스트 주식 추가
    public void addBacktestStock(BacktestStock backtestStock) {
        backtestStocks.add(backtestStock);
    }

    // 백테스트 주식 목록 조회
    public List<BacktestStock> getBacktestStocks() {
        return new ArrayList<>(backtestStocks);
    }

    // 포트폴리오 종목 수
    public int getStockCount() {
        return backtestStocks.size();
    }

    // 포트폴리오 요약 정보
    public String getPortfolioSummary() {
        return String.format("백테스트 '%s': %d개 종목, %s ~ %s",
                testName,
                getStockCount(),
                startDate,
                endDate);
    }

    // 백테스트 결과 업데이트
    public void updateResults(BigDecimal finalValue, BigDecimal totalReturn, BigDecimal buyHoldReturn,
                            BigDecimal excessReturn, BigDecimal periodGrowthRate, Integer rebalancingCount,
                            BigDecimal totalFee, BigDecimal totalBorrowingCost, BigDecimal maxBorrowingAmount,
                            BigDecimal minCashBalance, BigDecimal maxDrawdown, BigDecimal volatility,
                            BigDecimal sharpeRatio, BigDecimal timeWeightedReturn) {
        this.finalValue = finalValue;
        this.totalReturn = totalReturn;
        this.buyHoldReturn = buyHoldReturn;
        this.excessReturn = excessReturn;
        this.periodGrowthRate = periodGrowthRate;
        this.rebalancingCount = rebalancingCount;
        this.totalFee = totalFee;
        this.totalBorrowingCost = totalBorrowingCost;
        this.maxBorrowingAmount = maxBorrowingAmount;
        this.minCashBalance = minCashBalance;
        this.maxDrawdown = maxDrawdown;
        this.volatility = volatility;
        this.sharpeRatio = sharpeRatio;
        this.timeWeightedReturn = timeWeightedReturn;
    }

    // 결과 존재 여부 확인
    public boolean hasResults() {
        return finalValue != null && totalReturn != null;
    }

    // 수익률 계산 헬퍼 메서드들 (BacktestResult에서 이동)
    public BigDecimal getTotalReturnPercentage() {
        return totalReturn != null ? totalReturn.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    public BigDecimal getBuyHoldReturnPercentage() {
        return buyHoldReturn != null ? buyHoldReturn.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    public BigDecimal getExcessReturnPercentage() {
        return excessReturn != null ? excessReturn.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    public BigDecimal getAnnualizedReturnPercentage() {
        return periodGrowthRate != null ? periodGrowthRate.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalBorrowingCostSafe() {
        return totalBorrowingCost != null ? totalBorrowingCost : BigDecimal.ZERO;
    }

    public BigDecimal getMaxDrawdownPercentage() {
        return maxDrawdown != null ? maxDrawdown.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    public BigDecimal getVolatilityPercentage() {
        return volatility != null ? volatility.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    // 성과 요약 정보
    public String getPerformanceSummary() {
        if (!hasResults()) {
            return "결과 없음";
        }
        return String.format("총 수익률: %.2f%%, 초과 수익률: %.2f%%, 연환산 수익률: %.2f%%, 총 비용: %,.0f원",
                getTotalReturnPercentage(),
                getExcessReturnPercentage(),
                getAnnualizedReturnPercentage(),
                getTotalCost());
    }

    // 총 비용 계산
    public BigDecimal getTotalCost() {
        BigDecimal tradingCost = totalFee != null ? totalFee : BigDecimal.ZERO;
        BigDecimal borrowingCost = totalBorrowingCost != null ? totalBorrowingCost : BigDecimal.ZERO;
        return tradingCost.add(borrowingCost);
    }

}