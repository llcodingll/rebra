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
    @Query(value = "SELECT * FROM rebalancing_order ro " +
            "WHERE ro.portfolio_id = :portfolioId " +
            "AND ro.rebalancing_date BETWEEN :startDate AND :endDate " +
            "ORDER BY ro.rebalancing_date DESC",
            nativeQuery = true)
    Page<RebalancingOrder> findByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 포트폴리오별 리밸런싱 주문 전체 목록 조회 (그래프용) - 날짜 오름차순 (시계열)
     */
    @Query(value = "SELECT * FROM rebalancing_order ro WHERE ro.portfolio_id = :portfolioId " +
           "AND ro.rebalancing_date BETWEEN :startDate AND :endDate " +
           "ORDER BY ro.rebalancing_date ASC",
           nativeQuery = true)
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
    @Query(value = "SELECT COUNT(*) FROM rebalancing_order ro WHERE ro.portfolio_id = :portfolioId " +
           "AND ro.rebalancing_date BETWEEN :startDate AND :endDate",
           nativeQuery = true)
    Integer countByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 리밸런싱 통계 - 총 매수 금액
     */
    @Query(value = "SELECT COALESCE(SUM(ro.total_buy_amount), 0) FROM rebalancing_order ro WHERE ro.portfolio_id = :portfolioId " +
           "AND ro.rebalancing_date BETWEEN :startDate AND :endDate",
           nativeQuery = true)
    BigDecimal sumTotalBuyAmountByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 리밸런싱 통계 - 총 매도 금액
     */
    @Query(value = "SELECT COALESCE(SUM(ro.total_sell_amount), 0) FROM rebalancing_order ro WHERE ro.portfolio_id = :portfolioId " +
           "AND ro.rebalancing_date BETWEEN :startDate AND :endDate",
           nativeQuery = true)
    BigDecimal sumTotalSellAmountByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 자동 리밸런싱 개수
     */
    @Query(value = "SELECT COUNT(*) FROM rebalancing_order ro WHERE ro.portfolio_id = :portfolioId " +
           "AND ro.execution_type = 'AUTO' " +
           "AND ro.rebalancing_date BETWEEN :startDate AND :endDate",
           nativeQuery = true)
    Integer countAutoRebalancingByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 수동 리밸런싱 개수
     */
    @Query(value = "SELECT COUNT(*) FROM rebalancing_order ro WHERE ro.portfolio_id = :portfolioId " +
           "AND ro.execution_type = 'MANUAL' " +
           "AND ro.rebalancing_date BETWEEN :startDate AND :endDate",
           nativeQuery = true)
    Integer countManualRebalancingByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 포트폴리오별 최근 리밸런싱 주문 조회
     */
    Optional<RebalancingOrder> findTopByPortfolioIdOrderByRebalancingDateDesc(Long portfolioId);
}