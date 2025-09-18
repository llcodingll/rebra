package com.rebra.repository;

import com.rebra.entity.TradeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRecordRepository extends JpaRepository<TradeRecord, Long> {

    /**
     * 리밸런싱 주문별 거래 기록 조회
     */
    List<TradeRecord> findByRebalancingOrderIdOrderByTradeDate(Long rebalancingOrderId);

    /**
     * 리밸런싱 주문별 거래 기록 개수 조회
     */
    @Query("SELECT COUNT(tr) FROM TradeRecord tr WHERE tr.rebalancingOrder.id = :rebalancingOrderId")
    Integer countByRebalancingOrderId(@Param("rebalancingOrderId") Long rebalancingOrderId);

    /**
     * 포트폴리오별 전체 거래 기록 조회
     */
    @Query("SELECT tr FROM TradeRecord tr WHERE tr.rebalancingOrder.portfolio.id = :portfolioId " +
           "ORDER BY tr.tradeDate DESC")
    List<TradeRecord> findByPortfolioIdOrderByTradeDate(@Param("portfolioId") Long portfolioId);

    /**
     * 특정 종목의 거래 기록 조회
     */
    @Query("SELECT tr FROM TradeRecord tr WHERE tr.rebalancingOrder.portfolio.id = :portfolioId " +
           "AND tr.stockCode = :stockCode ORDER BY tr.tradeDate DESC")
    List<TradeRecord> findByPortfolioIdAndStockCodeOrderByTradeDate(
            @Param("portfolioId") Long portfolioId,
            @Param("stockCode") String stockCode);

    /**
     * 리밸런싱 주문의 매수 거래 기록 조회
     */
    @Query("SELECT tr FROM TradeRecord tr WHERE tr.rebalancingOrder.id = :rebalancingOrderId " +
           "AND tr.tradeType = 'BUY' ORDER BY tr.tradeDate")
    List<TradeRecord> findBuyRecordsByRebalancingOrderId(@Param("rebalancingOrderId") Long rebalancingOrderId);

    /**
     * 리밸런싱 주문의 매도 거래 기록 조회
     */
    @Query("SELECT tr FROM TradeRecord tr WHERE tr.rebalancingOrder.id = :rebalancingOrderId " +
           "AND tr.tradeType = 'SELL' ORDER BY tr.tradeDate")
    List<TradeRecord> findSellRecordsByRebalancingOrderId(@Param("rebalancingOrderId") Long rebalancingOrderId);
}