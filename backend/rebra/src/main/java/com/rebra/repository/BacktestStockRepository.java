package com.rebra.repository;

import com.rebra.entity.BacktestRecord;
import com.rebra.entity.BacktestStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BacktestStockRepository extends JpaRepository<BacktestStock, Long> {

    /**
     * 백테스트 기록에 포함된 주식 목록 조회
     */
    List<BacktestStock> findByBacktestRecord(BacktestRecord backtestRecord);

    /**
     * 백테스트 기록에 포함된 주식 목록 조회 (Stock 정보 포함)
     */
    @Query("SELECT bs FROM BacktestStock bs " +
           "JOIN FETCH bs.stock " +
           "WHERE bs.backtestRecord = :backtestRecord " +
           "ORDER BY bs.targetWeight DESC")
    List<BacktestStock> findByBacktestRecordWithStock(@Param("backtestRecord") BacktestRecord backtestRecord);

    /**
     * 백테스트 ID로 주식 목록 조회 (Stock 정보 포함)
     */
    @Query("SELECT bs FROM BacktestStock bs " +
           "JOIN FETCH bs.stock " +
           "WHERE bs.backtestRecord.id = :backtestId " +
           "ORDER BY bs.targetWeight DESC")
    List<BacktestStock> findByBacktestRecordIdWithStock(@Param("backtestId") Long backtestId);

    /**
     * 특정 주식이 포함된 백테스트 기록 수 조회
     */
    @Query("SELECT COUNT(bs) FROM BacktestStock bs WHERE bs.stock.stockCode = :stockCode")
    long countByStockCode(@Param("stockCode") String stockCode);

    /**
     * 사용자의 백테스트에서 특정 주식 사용 빈도 조회
     */
    @Query("SELECT COUNT(bs) FROM BacktestStock bs " +
           "WHERE bs.backtestRecord.user.id = :userId " +
           "AND bs.stock.stockCode = :stockCode")
    long countByUserIdAndStockCode(@Param("userId") Long userId, @Param("stockCode") String stockCode);

    /**
     * 백테스트에서 가장 많이 사용된 주식 목록 (Top N)
     */
    @Query("SELECT bs.stock.stockCode, bs.stock.stockName, COUNT(bs) as usage_count " +
           "FROM BacktestStock bs " +
           "GROUP BY bs.stock.stockCode, bs.stock.stockName " +
           "ORDER BY COUNT(bs) DESC")
    List<Object[]> findMostUsedStocks();
}