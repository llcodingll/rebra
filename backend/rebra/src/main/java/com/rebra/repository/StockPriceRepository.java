package com.rebra.repository;

import com.rebra.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    /**
     * 특정 종목의 특정 날짜 주가 조회
     */
    Optional<StockPrice> findByTickerAndDate(String ticker, LocalDate date);

    /**
     * 특정 종목의 기간별 주가 조회 (날짜 오름차순)
     */
    List<StockPrice> findByTickerAndDateBetweenOrderByDateAsc(String ticker, LocalDate startDate, LocalDate endDate);

    /**
     * 여러 종목의 기간별 주가 조회 (백테스트용)
     */
    @Query("SELECT sp FROM StockPrice sp WHERE sp.ticker IN :tickers AND sp.date BETWEEN :startDate AND :endDate ORDER BY sp.date ASC, sp.ticker ASC")
    List<StockPrice> findByTickersAndDateRange(@Param("tickers") List<String> tickers, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    /**
     * 특정 날짜의 모든 종목 주가 조회
     */
    List<StockPrice> findByDateOrderByTickerAsc(LocalDate date);

    /**
     * 특정 종목의 최신 주가 조회
     */
    Optional<StockPrice> findTopByTickerOrderByDateDesc(String ticker);

    /**
     * 특정 종목의 가장 오래된 주가 조회
     */
    Optional<StockPrice> findTopByTickerOrderByDateAsc(String ticker);

    /**
     * 특정 종목의 데이터 존재 기간 조회
     */
    @Query("SELECT MIN(sp.date) FROM StockPrice sp WHERE sp.ticker = :ticker")
    Optional<LocalDate> findEarliestDateByTicker(@Param("ticker") String ticker);

    @Query("SELECT MAX(sp.date) FROM StockPrice sp WHERE sp.ticker = :ticker")
    Optional<LocalDate> findLatestDateByTicker(@Param("ticker") String ticker);

    /**
     * 백테스트 가능한 공통 기간 조회 (모든 종목이 데이터를 가지고 있는 기간)
     */
    @Query("SELECT MAX(earliest.minDate) as commonStart, MIN(latest.maxDate) as commonEnd " +
           "FROM (SELECT sp.ticker, MIN(sp.date) as minDate FROM StockPrice sp WHERE sp.ticker IN :tickers GROUP BY sp.ticker) earliest, " +
           "     (SELECT sp.ticker, MAX(sp.date) as maxDate FROM StockPrice sp WHERE sp.ticker IN :tickers GROUP BY sp.ticker) latest")
    List<Object[]> findCommonDateRange(@Param("tickers") List<String> tickers);

    /**
     * 특정 기간 내 거래일 목록 조회 (중복 제거)
     */
    @Query("SELECT DISTINCT sp.date FROM StockPrice sp WHERE sp.date BETWEEN :startDate AND :endDate ORDER BY sp.date ASC")
    List<LocalDate> findTradingDatesBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    /**
     * 종목별 데이터 건수 조회 (데이터 품질 확인용)
     */
    @Query("SELECT sp.ticker, COUNT(sp) FROM StockPrice sp WHERE sp.ticker IN :tickers GROUP BY sp.ticker")
    List<Object[]> countByTickers(@Param("tickers") List<String> tickers);

}