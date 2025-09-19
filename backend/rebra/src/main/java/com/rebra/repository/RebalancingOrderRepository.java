package com.rebra.repository;

import com.rebra.entity.RebalancingOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 최적화 대상
 */
@Repository
public interface RebalancingOrderRepository extends JpaRepository<RebalancingOrder, Long> {

    /**
     * 포트폴리오별 리밸런싱 주문 목록 조회 (페이지네이션) - 최신순
     */
    @Query("SELECT ro FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR ro.rebalancingDate >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR ro.rebalancingDate <= :endDate) " +
            "ORDER BY ro.rebalancingDate DESC")
    Page<RebalancingOrder> findByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 포트폴리오별 리밸런싱 주문 전체 목록 조회 (그래프용) - 날짜 오름차순 (시계열)
     */
    @Query("SELECT ro FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR ro.rebalancingDate >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR ro.rebalancingDate <= :endDate) " +
           "ORDER BY ro.rebalancingDate ASC")
    List<RebalancingOrder> findAllByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 포트폴리오의 특정 리밸런싱 주문 조회 (소유자 검증 포함)
     */
    Optional<RebalancingOrder> findByIdAndPortfolioId(Long id, Long portfolioId);

    /**
     * 기간별 리밸런싱 통계 - 총 개수
     */
    @Query("SELECT COUNT(ro) FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR ro.rebalancingDate >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR ro.rebalancingDate <= :endDate)")
    Integer countByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 리밸런싱 통계 - 총 매수 금액
     */
    @Query("SELECT COALESCE(SUM(ro.totalBuyAmount), 0) FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR ro.rebalancingDate >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR ro.rebalancingDate <= :endDate)")
    BigDecimal sumTotalBuyAmountByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 리밸런싱 통계 - 총 매도 금액
     */
    @Query("SELECT COALESCE(SUM(ro.totalSellAmount), 0) FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR ro.rebalancingDate >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR ro.rebalancingDate <= :endDate)")
    BigDecimal sumTotalSellAmountByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 자동 리밸런싱 개수
     */
    @Query("SELECT COUNT(ro) FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
           "AND ro.executionType = 'AUTO' " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR ro.rebalancingDate >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR ro.rebalancingDate <= :endDate)")
    Integer countAutoRebalancingByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 수동 리밸런싱 개수
     */
    @Query("SELECT COUNT(ro) FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
           "AND ro.executionType = 'MANUAL' " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR ro.rebalancingDate >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR ro.rebalancingDate <= :endDate)")
    Integer countManualRebalancingByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 포트폴리오별 최근 리밸런싱 주문 조회
     */
    Optional<RebalancingOrder> findTopByPortfolioIdOrderByRebalancingDateDesc(Long portfolioId);
}