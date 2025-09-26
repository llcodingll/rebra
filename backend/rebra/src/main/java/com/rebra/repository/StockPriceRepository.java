package com.rebra.repository;

import com.rebra.entity.Stock;
import com.rebra.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    /**
     * 특정 종목의 특정 날짜 주가 조회
     */
    Optional<StockPrice> findByTickerAndDate(String ticker, LocalDate date);

    /**
     * 특정 종목의 특정 날짜 데이터 존재 여부 확인
     */
    boolean existsByTickerAndDate(String ticker, LocalDate date);

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
           "FROM (SELECT sp.ticker as ticker, MIN(sp.date) as minDate FROM StockPrice sp WHERE sp.ticker IN :tickers GROUP BY sp.ticker) earliest, " +
           "     (SELECT sp.ticker as ticker, MAX(sp.date) as maxDate FROM StockPrice sp WHERE sp.ticker IN :tickers GROUP BY sp.ticker) latest")
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

    /**
     * 특정 월의 마지막 거래일 조회
     */
    @Query("SELECT MAX(sp.date) FROM StockPrice sp " +
           "WHERE YEAR(sp.date) = :year AND MONTH(sp.date) = :month")
    Optional<LocalDate> findLastTradingDayOfMonth(@Param("year") int year, @Param("month") int month);

    /**
     * 기간 내 모든 월말 거래일 목록 조회
     */
    @Query("SELECT DISTINCT " +
           "(SELECT MAX(sp2.date) FROM StockPrice sp2 " +
           " WHERE YEAR(sp2.date) = YEAR(sp.date) AND MONTH(sp2.date) = MONTH(sp.date)) " +
           "FROM StockPrice sp " +
           "WHERE sp.date BETWEEN :startDate AND :endDate " +
           "ORDER BY 1 ASC")
    List<LocalDate> findMonthEndTradingDates(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);

    /**
     * 기간 내 분기말 거래일 목록 조회 (3,6,9,12월)
     */
    @Query("SELECT DISTINCT " +
           "(SELECT MAX(sp2.date) FROM StockPrice sp2 " +
           " WHERE YEAR(sp2.date) = YEAR(sp.date) AND MONTH(sp2.date) = MONTH(sp.date)) " +
           "FROM StockPrice sp " +
           "WHERE sp.date BETWEEN :startDate AND :endDate " +
           "AND MONTH(sp.date) IN (3, 6, 9, 12) " +
           "ORDER BY 1 ASC")
    List<LocalDate> findQuarterEndTradingDates(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);

    /**
     * 종목별 데이터 존재 기간과 건수 조회 (데이터 가용성 확인용)
     */
    @Query("SELECT sp.ticker, MIN(sp.date), MAX(sp.date), COUNT(sp) " +
           "FROM StockPrice sp " +
           "WHERE sp.ticker IN :tickers " +
           "GROUP BY sp.ticker")
    List<Object[]> findDataAvailabilityByTickers(@Param("tickers") List<String> tickers);

    /**
     * 종목명과 날짜로 주가 조회 (FSS API 백테스트용)
     */
    Optional<StockPrice> findByNameAndDate(String name, LocalDate date);

    /**
     * 종목명 부분 검색과 날짜로 주가 조회 (FSS API 백테스트용)
     */
    List<StockPrice> findByNameContainingAndDate(String name, LocalDate date);

    /**
     * Stock 엔티티 기반 조회 메소드들
     */
    Long countByStock(Stock stock);

    List<StockPrice> findByStockAndDateBetweenOrderByDateAsc(
        Stock stock, LocalDate startDate, LocalDate endDate
    );

    Optional<StockPrice> findByStockAndDate(Stock stock, LocalDate date);

    /**
     * Stock별 데이터 존재 여부 확인
     */
    boolean existsByStockAndDate(Stock stock, LocalDate date);

    Long countByStockAndDateBetween(Stock stock, LocalDate startDate, LocalDate endDate);

    /**
     * Stock별 최신/최오래된 데이터 조회
     */
    Optional<StockPrice> findTopByStockOrderByDateDesc(Stock stock);

    Optional<StockPrice> findTopByStockOrderByDateAsc(Stock stock);

    /**
     * 배치 조회를 위한 메소드 (N+1 문제 해결)
     */
    List<StockPrice> findByTickerAndDateIn(String ticker, List<LocalDate> dates);

    /**
     * 중복 체크 후 StockPrice 저장 (개선된 버전 - 배치 조회 사용)
     * 이미 존재하는 데이터는 제외하고 새로운 데이터만 저장
     */
    default List<StockPrice> saveAllWithBatchCheck(List<StockPrice> stockPrices) {
        if (stockPrices.isEmpty()) {
            return Collections.emptyList();
        }
        
        String ticker = stockPrices.get(0).getTicker();
        List<LocalDate> dates = stockPrices.stream()
            .map(StockPrice::getDate)
            .distinct()
            .collect(Collectors.toList());
        
        // 1번의 쿼리로 기존 데이터 조회
        Set<LocalDate> existingDates = findByTickerAndDateIn(ticker, dates).stream()
            .map(StockPrice::getDate)
            .collect(Collectors.toSet());
        
        // 메모리에서 필터링
        List<StockPrice> newPrices = stockPrices.stream()
            .filter(sp -> !existingDates.contains(sp.getDate()))
            .collect(Collectors.toList());
        
        // 신규 데이터만 저장
        if (!newPrices.isEmpty()) {
            return saveAll(newPrices);
        }
        return Collections.emptyList();
    }
    
    /**
     * 기존 메소드 (하위 호환성 유지)
     * @deprecated saveAllWithBatchCheck 사용 권장
     */
    @Deprecated
    default List<StockPrice> saveAllWithDuplicateCheck(List<StockPrice> stockPrices) {
        return saveAllWithBatchCheck(stockPrices);
    }

}