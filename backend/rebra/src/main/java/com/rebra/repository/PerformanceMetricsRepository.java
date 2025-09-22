package com.rebra.repository;

import com.rebra.entity.PerformanceMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceMetricsRepository extends JpaRepository<PerformanceMetrics, Long> {

    /**
     * 포트폴리오별 성과 메트릭 조회 (최신순)
     */
    List<PerformanceMetrics> findByPortfolioIdOrderByMetricDateDesc(Long portfolioId);

    /**
     * 포트폴리오의 특정 날짜 성과 메트릭 조회
     */
    Optional<PerformanceMetrics> findByPortfolioIdAndMetricDate(Long portfolioId, LocalDate metricDate);

    /**
     * 포트폴리오의 특정 기간 성과 메트릭 조회 (날짜 오름차순)
     */
    @Query("SELECT pm FROM PerformanceMetrics pm WHERE pm.portfolio.id = :portfolioId " +
           "AND pm.metricDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pm.metricDate ASC")
    List<PerformanceMetrics> findByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 특정 날짜의 모든 포트폴리오 성과 메트릭 조회
     */
    List<PerformanceMetrics> findByMetricDate(LocalDate metricDate);

    /**
     * 포트폴리오의 특정 날짜 성과 메트릭 존재 여부 확인
     */
    boolean existsByPortfolioIdAndMetricDate(Long portfolioId, LocalDate metricDate);

    /**
     * 포트폴리오의 최근 성과 메트릭 조회
     */
    Optional<PerformanceMetrics> findTopByPortfolioIdOrderByMetricDateDesc(Long portfolioId);

    /**
     * 포트폴리오별 성과 메트릭 개수 조회
     */
    long countByPortfolioId(Long portfolioId);

    /**
     * 특정 기간 동안 리밸런싱이 실행된 날짜들 조회
     */
    @Query("SELECT pm FROM PerformanceMetrics pm WHERE pm.portfolio.id = :portfolioId " +
           "AND pm.isRebalanced = true " +
           "AND pm.metricDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pm.metricDate ASC")
    List<PerformanceMetrics> findRebalancingDates(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 특정 기간 동안 매수가 실행된 날짜들 조회
     */
    @Query("SELECT pm FROM PerformanceMetrics pm WHERE pm.portfolio.id = :portfolioId " +
           "AND pm.isBought = true " +
           "AND pm.metricDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pm.metricDate ASC")
    List<PerformanceMetrics> findBuyingDates(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 특정 기간 동안 매도가 실행된 날짜들 조회
     */
    @Query("SELECT pm FROM PerformanceMetrics pm WHERE pm.portfolio.id = :portfolioId " +
           "AND pm.isSold = true " +
           "AND pm.metricDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pm.metricDate ASC")
    List<PerformanceMetrics> findSellingDates(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}