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
            "AND ro.rebalancingDate BETWEEN :startDate AND :endDate " +
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
           "AND ro.rebalancingDate BETWEEN :startDate AND :endDate " +
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
           "AND ro.rebalancingDate BETWEEN :startDate AND :endDate")
    Integer countByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 리밸런싱 통계 - 총 매수 금액
     */
    @Query("SELECT COALESCE(SUM(ro.totalBuyAmount), 0) FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
           "AND ro.rebalancingDate BETWEEN :startDate AND :endDate")
    Long sumTotalBuyAmountByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 리밸런싱 통계 - 총 매도 금액
     */
    @Query("SELECT COALESCE(SUM(ro.totalSellAmount), 0) FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
           "AND ro.rebalancingDate BETWEEN :startDate AND :endDate")
    Long sumTotalSellAmountByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 자동 리밸런싱 개수
     */
    @Query("SELECT COUNT(ro) FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
           "AND ro.executionType = 'AUTO' " +
           "AND ro.rebalancingDate BETWEEN :startDate AND :endDate")
    Integer countAutoRebalancingByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 수동 리밸런싱 개수
     */
    @Query("SELECT COUNT(ro) FROM RebalancingOrder ro WHERE ro.portfolio.id = :portfolioId " +
           "AND ro.executionType = 'MANUAL' " +
           "AND ro.rebalancingDate BETWEEN :startDate AND :endDate")
    Integer countManualRebalancingByPortfolioIdWithDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 포트폴리오별 최근 리밸런싱 주문 조회
     */
    Optional<RebalancingOrder> findTopByPortfolioIdOrderByRebalancingDateDesc(Long portfolioId);

    /**
     * 특정 날짜의 리밸런싱 주문과 거래 기록을 함께 조회
     *
     * @param portfolioId 포트폴리오 ID
     * @param startOfDay 조회 날짜 시작 시간 (00:00:00)
     * @param endOfDay 조회 날짜 종료 시간 (다음날 00:00:00)
     * @return TradeRecord와 함께 조회된 RebalancingOrder
     */
    @Query("SELECT ro FROM RebalancingOrder ro " +
           "JOIN FETCH ro.tradeRecords " +
           "WHERE ro.portfolio.id = :portfolioId " +
           "AND ro.rebalancingDate >= :startOfDay " +
           "AND ro.rebalancingDate < :endOfDay")
    Optional<RebalancingOrder> findByPortfolioIdAndDateWithTrades(
            @Param("portfolioId") Long portfolioId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}