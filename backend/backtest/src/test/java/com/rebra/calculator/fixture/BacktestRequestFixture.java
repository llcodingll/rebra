package com.rebra.calculator.fixture;

import com.rebra.calculator.dto.BacktestRequest;
import com.rebra.calculator.dto.BacktestStockDto;
import com.rebra.calculator.enums.RebalancingPeriod;
import com.rebra.calculator.enums.RebalancingType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

/**
 * 백테스트 요청 테스트 데이터 생성기
 * 테스트에서 사용할 다양한 백테스트 요청 객체를 생성한다.
 */
@Component
public class BacktestRequestFixture {

    /**
     * 기본적인 백테스트 요청 생성
     */
    public BacktestRequest createDefault() {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(1L);
        request.setStartDate(LocalDate.of(2023, 1, 2));
        request.setEndDate(LocalDate.of(2023, 1, 31));
        request.setRebalancingType(RebalancingType.THRESHOLD);
        request.setStocks(createDefaultStocks());
        request.setDailyPrices(createDefaultPriceData(request.getStartDate(), request.getEndDate()));
        
        return request;
    }

    /**
     * 임계값 기반 리밸런싱 요청 생성
     */
    public BacktestRequest createThresholdBased(Long backtestId, LocalDate startDate, LocalDate endDate, double threshold) {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(backtestId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setRebalancingType(RebalancingType.THRESHOLD);
        
        List<BacktestStockDto> stocks = createDefaultStocks();
        stocks.forEach(stock -> stock.setThresholdPercentage(threshold));
        request.setStocks(stocks);
        request.setDailyPrices(createDefaultPriceData(startDate, endDate));
        
        return request;
    }

    /**
     * 주기적 리밸런싱 요청 생성
     */
    public BacktestRequest createPeriodic(Long backtestId, LocalDate startDate, LocalDate endDate, RebalancingPeriod period) {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(backtestId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setRebalancingType(RebalancingType.PERIODIC);
        request.setRebalancingPeriod(period);
        request.setStocks(createDefaultStocks());
        request.setDailyPrices(createDefaultPriceData(startDate, endDate));
        
        return request;
    }

    /**
     * 복잡한 포트폴리오 백테스트 요청 생성
     */
    public BacktestRequest createComplex(Long backtestId, LocalDate startDate, LocalDate endDate) {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(backtestId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setRebalancingType(RebalancingType.THRESHOLD);
        request.setStocks(createComplexStocks());
        request.setDailyPrices(createVolatilePriceData(startDate, endDate));
        
        return request;
    }

    /**
     * 높은 변동성 백테스트 요청 생성
     */
    public BacktestRequest createHighVolatility(Long backtestId, LocalDate startDate, LocalDate endDate) {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(backtestId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setRebalancingType(RebalancingType.THRESHOLD);
        
        List<BacktestStockDto> stocks = createDefaultStocks();
        stocks.forEach(stock -> stock.setThresholdPercentage(2.0)); // 낮은 임계값으로 민감한 리밸런싱
        request.setStocks(stocks);
        request.setDailyPrices(createVolatilePriceData(startDate, endDate));
        
        return request;
    }

    /**
     * 장기간 백테스트 요청 생성
     */
    public BacktestRequest createLongTerm(Long backtestId) {
        LocalDate startDate = LocalDate.of(2020, 1, 2);
        LocalDate endDate = LocalDate.of(2023, 12, 29);
        
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(backtestId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setRebalancingType(RebalancingType.PERIODIC);
        request.setRebalancingPeriod(RebalancingPeriod.QUARTERLY);
        request.setStocks(createDefaultStocks());
        request.setDailyPrices(createLongTermPriceData(startDate, endDate));
        
        return request;
    }

    /**
     * 단기간 백테스트 요청 생성
     */
    public BacktestRequest createShortTerm(Long backtestId) {
        LocalDate startDate = LocalDate.of(2023, 1, 2);
        LocalDate endDate = LocalDate.of(2023, 1, 13); // 약 2주
        
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(backtestId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setRebalancingType(RebalancingType.THRESHOLD);
        request.setStocks(createDefaultStocks());
        request.setDailyPrices(createDefaultPriceData(startDate, endDate));
        
        return request;
    }

    /**
     * 차입 발생 유도 백테스트 요청 생성
     */
    public BacktestRequest createBorrowingInducing(Long backtestId) {
        BacktestRequest request = createDefault();
        request.setBacktestId(backtestId);
        
        // 높은 가중치로 설정하여 차입 유도
        List<BacktestStockDto> stocks = Arrays.asList(
            createStock("005930", "삼성전자", 60, 200, 5.0),
            createStock("000660", "SK하이닉스", 50, 100, 5.0),
            createStock("035420", "NAVER", 40, 60, 5.0)
        );
        request.setStocks(stocks);
        
        return request;
    }

    /**
     * 잘못된 데이터 백테스트 요청 생성
     */
    public BacktestRequest createInvalid() {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(null); // 잘못된 ID
        request.setStartDate(LocalDate.of(2023, 12, 31));
        request.setEndDate(LocalDate.of(2023, 1, 1)); // 시작일이 종료일보다 늦음
        request.setRebalancingType(RebalancingType.THRESHOLD);
        request.setStocks(Collections.emptyList()); // 빈 종목 목록
        request.setDailyPrices(Collections.emptyMap()); // 빈 가격 데이터
        
        return request;
    }

    /**
     * 불완전한 가격 데이터 백테스트 요청 생성
     */
    public BacktestRequest createWithIncompletePriceData(Long backtestId) {
        BacktestRequest request = createDefault();
        request.setBacktestId(backtestId);
        
        // 시작일 가격만 있고 나머지는 누락
        Map<String, Map<String, Double>> incompletePrices = new HashMap<>();
        incompletePrices.put(request.getStartDate().toString(), Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0
        ));
        request.setDailyPrices(incompletePrices);
        
        return request;
    }

    /**
     * 사용자 정의 종목 리스트로 백테스트 요청 생성
     */
    public BacktestRequest createWithCustomStocks(Long backtestId, LocalDate startDate, LocalDate endDate, 
                                                List<BacktestStockDto> stocks) {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(backtestId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setRebalancingType(RebalancingType.THRESHOLD);
        request.setStocks(stocks);
        request.setDailyPrices(createCustomPriceData(startDate, endDate, stocks));
        
        return request;
    }

    // ===== Private Helper Methods =====

    private List<BacktestStockDto> createDefaultStocks() {
        return Arrays.asList(
            createStock("005930", "삼성전자", 40, 100, 5.0),
            createStock("000660", "SK하이닉스", 30, 50, 5.0),
            createStock("035420", "NAVER", 30, 25, 5.0)
        );
    }

    private List<BacktestStockDto> createComplexStocks() {
        return Arrays.asList(
            createStock("005930", "삼성전자", 25, 100, 3.0),
            createStock("000660", "SK하이닉스", 20, 50, 4.0),
            createStock("035420", "NAVER", 20, 25, 5.0),
            createStock("005380", "현대차", 15, 30, 6.0),
            createStock("000270", "기아", 10, 40, 7.0),
            createStock("051910", "LG화학", 10, 15, 8.0)
        );
    }

    private BacktestStockDto createStock(String stockCode, String stockName, int weight, int shares, double threshold) {
        BacktestStockDto stock = new BacktestStockDto();
        stock.setStockCode(stockCode);
        stock.setWeight(weight);
        stock.setShares(shares);
        stock.setThresholdPercentage(threshold);
        return stock;
    }

    private Map<String, Map<String, Double>> createDefaultPriceData(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0
        );
        
        return createPriceDataWithVariation(startDate, endDate, basePrices, 0.05); // ±5% 변동
    }

    private Map<String, Map<String, Double>> createVolatilePriceData(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0,
            "005380", 150000.0,
            "000270", 70000.0,
            "051910", 500000.0
        );
        
        return createPriceDataWithVariation(startDate, endDate, basePrices, 0.15); // ±15% 큰 변동
    }

    private Map<String, Map<String, Double>> createLongTermPriceData(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> basePrices = Map.of(
            "005930", 40000.0, // 시작가를 낮게 설정하여 장기 상승 트렌드 시뮬레이션
            "000660", 60000.0,
            "035420", 150000.0
        );
        
        return createTrendingPriceData(startDate, endDate, basePrices);
    }

    private Map<String, Map<String, Double>> createCustomPriceData(LocalDate startDate, LocalDate endDate, 
                                                                  List<BacktestStockDto> stocks) {
        Map<String, Double> basePrices = new HashMap<>();
        for (BacktestStockDto stock : stocks) {
            basePrices.put(stock.getStockCode(), 50000.0 + (Math.random() * 100000)); // 5만~15만원 범위
        }
        
        return createPriceDataWithVariation(startDate, endDate, basePrices, 0.08); // ±8% 변동
    }

    private Map<String, Map<String, Double>> createPriceDataWithVariation(LocalDate startDate, LocalDate endDate, 
                                                                         Map<String, Double> basePrices, double maxVariation) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue; // 주말 제외
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                double variation = 1.0 + ((Math.random() - 0.5) * 2 * maxVariation);
                double price = entry.getValue() * variation;
                dayPrices.put(entry.getKey(), Math.round(price * 100) / 100.0); // 소수점 2자리로 반올림
            }
            priceData.put(date.toString(), dayPrices);
        }
        
        return priceData;
    }

    private Map<String, Map<String, Double>> createTrendingPriceData(LocalDate startDate, LocalDate endDate, 
                                                                    Map<String, Double> basePrices) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        long totalDays = startDate.until(endDate).getDays();
        long currentDay = 0;
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue;
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                // 장기 상승 트렌드 + 일일 변동
                double trendMultiplier = 1.0 + (currentDay / (double) totalDays) * 0.5; // 최대 50% 상승
                double dailyVariation = 1.0 + ((Math.random() - 0.5) * 0.1); // ±5% 일일 변동
                double price = entry.getValue() * trendMultiplier * dailyVariation;
                dayPrices.put(entry.getKey(), Math.round(price * 100) / 100.0);
            }
            priceData.put(date.toString(), dayPrices);
            currentDay++;
        }
        
        return priceData;
    }
}