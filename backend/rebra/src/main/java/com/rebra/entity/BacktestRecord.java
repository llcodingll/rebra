package com.rebra.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rebra.common.BaseEntity;
import com.rebra.dto.response.BacktestDetailResponse;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Slf4j
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
    @SequenceGenerator(
        name = "backtest_record_seq_generator",
        sequenceName = "backtest_record_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "backtest_record_seq_generator")
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
    @Column
    private Integer initialCapital;

    @Column
    private Integer finalValue;

    @Column
    private Double totalReturn;

    @Column
    private Double buyHoldReturn;

    @Column
    private Double excessReturn;

    @Column
    private Double periodGrowthRate;

    @Column
    private Integer rebalancingCount;

    @Column
    private Integer totalFee;

    @Column
    private Integer totalBorrowingCost;

    @Column
    private Integer maxBorrowingAmount;

    @Column
    private Integer minCashBalance;

    @Column
    private Double maxDrawdown;

    @Column
    private Double volatility;

    @Column
    private Double sharpeRatio;

    @Column
    private Double timeWeightedReturn;

    // 일별 상세 정보를 JSON으로 저장
    @Column(columnDefinition = "TEXT")
    private String detailsJson;

    // 런타임 캐시
    @Transient
    private List<BacktestDetailResponse> detailsCache;

    @Transient
    private static final ObjectMapper objectMapper = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }


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
    public void updateResults(Integer finalValue, Double totalReturn, Double buyHoldReturn,
                            Double excessReturn, Double periodGrowthRate, Integer rebalancingCount,
                            Integer totalFee, Integer totalBorrowingCost, Integer maxBorrowingAmount,
                            Integer minCashBalance, Double maxDrawdown, Double volatility,
                            Double sharpeRatio, Double timeWeightedReturn) {
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
    public Double getTotalReturnPercentage() {
        return totalReturn != null ? totalReturn * 100 : 0.0;
    }

    public Double getBuyHoldReturnPercentage() {
        return buyHoldReturn != null ? buyHoldReturn * 100 : 0.0;
    }

    public Double getExcessReturnPercentage() {
        return excessReturn != null ? excessReturn * 100 : 0.0;
    }

    public Double getAnnualizedReturnPercentage() {
        return periodGrowthRate != null ? periodGrowthRate * 100 : 0.0;
    }

    public Integer getTotalBorrowingCostSafe() {
        return totalBorrowingCost != null ? totalBorrowingCost : 0;
    }

    public Double getMaxDrawdownPercentage() {
        return maxDrawdown != null ? maxDrawdown * 100 : 0.0;
    }

    public Double getVolatilityPercentage() {
        return volatility != null ? volatility * 100 : 0.0;
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
    public Integer getTotalCost() {
        Integer tradingCost = totalFee != null ? totalFee : 0;
        Integer borrowingCost = totalBorrowingCost != null ? totalBorrowingCost : 0;
        return tradingCost + borrowingCost;
    }

    // JSON 상세 정보 관련 메서드
    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
        this.detailsCache = null; // 캐시 무효화
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public List<BacktestDetailResponse> getDetails() {
        if (detailsCache == null && detailsJson != null && !detailsJson.trim().isEmpty()) {
            try {
                detailsCache = objectMapper.readValue(detailsJson, new TypeReference<List<BacktestDetailResponse>>() {});
                log.info("백테스트 상세 정보 역직렬화 성공: 리스트 크기={}", detailsCache.size());
            } catch (Exception e) {
                log.error("백테스트 상세 정보 역직렬화 실패: JSON 길이={}, 에러={}", 
                        detailsJson.length(), e.getMessage(), e);
                log.debug("파싱 실패한 JSON 시작 부분: {}", 
                        detailsJson.substring(0, Math.min(200, detailsJson.length())));
                detailsCache = new ArrayList<>();
            }
        }
        return detailsCache != null ? detailsCache : new ArrayList<>();
    }

    public void setDetails(List<BacktestDetailResponse> details) {
        this.detailsCache = details;
        if (details != null) {
            try {
                this.detailsJson = objectMapper.writeValueAsString(details);
                log.info("백테스트 상세 정보 JSON 직렬화 성공: 리스트 크기={}, JSON 길이={}", 
                        details.size(), this.detailsJson.length());
            } catch (Exception e) {
                log.error("백테스트 상세 정보 JSON 직렬화 실패: 리스트 크기={}", 
                        details.size(), e);
                this.detailsJson = null;
            }
        } else {
            this.detailsJson = null;
        }
    }

    // 상세 정보 존재 여부 확인
    public boolean hasDetails() {
        return detailsJson != null && !detailsJson.trim().isEmpty();
    }

}