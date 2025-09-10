package com.rebra.entity;

import com.rebra.common.BaseEntity;
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
import jakarta.persistence.Table;
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

}