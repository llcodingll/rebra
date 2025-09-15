package com.rebra.calculator.integration;

import com.rebra.calculator.dto.*;
import com.rebra.calculator.enums.BacktestStatus;
import com.rebra.calculator.enums.RebalancingPeriod;
import com.rebra.calculator.enums.RebalancingType;
import com.rebra.calculator.service.BacktestCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * 백테스트 시스템 성능 테스트
 * 내부 계산 로직의 성능을 검증한다.
 */
@SpringBootTest
class BacktestIntegrationTest {

    @Autowired
    private BacktestCalculatorService backtestCalculatorService;

    @BeforeEach
    void setUp() {
        // 테스트 환경 초기화 (필요시)
    }


    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTest {

        @Test
        @DisplayName("대용량 데이터 백테스트 성능")
        void largeDataBacktestPerformance() {
            // given
            BacktestRequest largeRequest = createLargeDataBacktestRequest();
            long startTime = System.currentTimeMillis();

            // when - BacktestCalculatorService 직접 호출
            BacktestResponse response = backtestCalculatorService.executeBacktest(largeRequest);
            
            // then
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            // 결과 검증
            assertThat(response).isNotNull();
            
            // 실패 시 에러 메시지 출력
            if (response.getStatus() == BacktestStatus.FAILED) {
                System.out.println("백테스트 실패 원인: " + response.getErrorMessage());
                System.out.println("요청 검증 결과: " + largeRequest.isValid());
            }
            
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            assertThat(response.getCalculationTimeMs()).isLessThan(30000); // 30초 이내
            assertThat(totalTime).isLessThan(60000); // 전체 1분 이내
            
            // 대용량 데이터 특성 확인
            assertThat(response.getDetails().size()).isGreaterThan(100);
            
            // 성능 정보 출력
            System.out.println("백테스트 성능 테스트 결과:");
            System.out.println("- 계산 시간: " + response.getCalculationTimeMs() + "ms");
            System.out.println("- 전체 시간: " + totalTime + "ms");
            System.out.println("- 처리된 거래일: " + response.getDetails().size() + "일");
            System.out.println("- 리밸런싱 횟수: " + response.getSummary().getRebalancingCount() + "회");
        }

        @Test
        @DisplayName("다중 백테스트 순차 처리 성능 테스트")
        void multipleBacktestSequentialPerformance() {
            // given
            int requestCount = 5;
            List<BacktestRequest> requests = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            
            for (int i = 1; i <= requestCount; i++) {
                BacktestRequest request = createSimpleBacktestRequest();
                request.setBacktestId((long) i);
                requests.add(request);
            }

            // when - 순차적으로 처리
            List<BacktestResponse> responses = new ArrayList<>();
            for (BacktestRequest request : requests) {
                BacktestResponse response = backtestCalculatorService.executeBacktest(request);
                responses.add(response);
            }
            
            // then
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            assertThat(responses).hasSize(requestCount);
            assertThat(totalTime).isLessThan(40000); // 40초 이내
            
            // 모든 응답이 성공적으로 처리되었는지 확인
            for (BacktestResponse response : responses) {
                assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            }
            
            // 성능 정보 출력
            System.out.println("다중 백테스트 순차 처리 성능:");
            System.out.println("- 처리 건수: " + requestCount + "건");
            System.out.println("- 총 소요 시간: " + totalTime + "ms");
            System.out.println("- 평균 처리 시간: " + (totalTime / requestCount) + "ms/건");
        }
    }

    // ===== Helper Methods =====

    private BacktestRequest createSimpleBacktestRequest() {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(1L);
        request.setStartDate(LocalDate.of(2023, 1, 2));
        request.setEndDate(LocalDate.of(2023, 1, 31));
        request.setRebalancingType(RebalancingType.THRESHOLD);
        
        List<BacktestStockDto> stocks = List.of(
            createBacktestStockWithThreshold("005930", "삼성전자", 40, 100, 0.50),  // 20% 임계값
            createBacktestStockWithThreshold("000660", "SK하이닉스", 30, 50, 0.50),
            createBacktestStockWithThreshold("035420", "NAVER", 30, 25, 0.50)
        );
        request.setStocks(stocks);
        request.setDailyPrices(createDivergentPriceData(request.getStartDate(), request.getEndDate()));
        
        return request;
    }

    private BacktestRequest createLargeDataBacktestRequest() {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(6L);
        request.setStartDate(LocalDate.of(2022, 1, 3));
        request.setEndDate(LocalDate.of(2022, 12, 30)); // 1년간
        request.setRebalancingType(RebalancingType.PERIODIC);
        request.setRebalancingPeriod(RebalancingPeriod.MONTHLY);
        
        List<BacktestStockDto> stocks = List.of(
            createBacktestStockWithThreshold("005930", "삼성전자", 25, 100, 0.03),  // 3% 임계값
            createBacktestStockWithThreshold("000660", "SK하이닉스", 25, 50, 0.03),
            createBacktestStockWithThreshold("035420", "NAVER", 25, 25, 0.03),
            createBacktestStockWithThreshold("005380", "현대차", 25, 30, 0.03)
        );
        request.setStocks(stocks);
        request.setDailyPrices(createRealisticPriceData(request.getStartDate(), request.getEndDate()));
        
        return request;
    }

    private BacktestStockDto createBacktestStock(String stockCode, String stockName, int weight, int shares) {
        BacktestStockDto stock = new BacktestStockDto();
        stock.setStockCode(stockCode);
        stock.setWeight(weight);
        stock.setShares(shares);
        stock.setThresholdPercentage(0.05); // 5% = 0.05 (0.0 ~ 1.0 범위)
        return stock;
    }
    
    private BacktestStockDto createBacktestStockWithThreshold(String stockCode, String stockName, int weight, int shares, double thresholdPercentage) {
        BacktestStockDto stock = new BacktestStockDto();
        stock.setStockCode(stockCode);
        stock.setWeight(weight);
        stock.setShares(shares);
        stock.setThresholdPercentage(thresholdPercentage);
        return stock;
    }

    private Map<String, Map<String, Double>> createSimplePriceData(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0,
            "005380", 150000.0
        );
        
        // 종목별 독립적인 Random 시드
        Map<String, Random> stockRandoms = Map.of(
            "005930", new Random(1001),
            "000660", new Random(1002), 
            "035420", new Random(1003),
            "005380", new Random(1004)
        );
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue; // 주말 제외
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                String stockCode = entry.getKey();
                Random random = stockRandoms.get(stockCode);
                
                // 종목별 서로 다른 변동률 적용
                double variation = switch (stockCode) {
                    case "005930" -> 0.98 + (random.nextDouble() * 0.04);  // ±2%
                    case "000660" -> 0.95 + (random.nextDouble() * 0.10);  // ±5%  
                    case "035420" -> 0.93 + (random.nextDouble() * 0.14);  // ±7%
                    case "005380" -> 0.96 + (random.nextDouble() * 0.08);  // ±4%
                    default -> 0.95 + (random.nextDouble() * 0.10);
                };
                
                dayPrices.put(stockCode, entry.getValue() * variation);
            }
            priceData.put(date.toString(), dayPrices);
        }
        
        return priceData;
    }

    private Map<String, Map<String, Double>> createVolatilePriceData(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0,
            "005380", 150000.0
        );
        
        Map<String, Double> currentPrices = new HashMap<>(basePrices);
        Random globalRandom = new Random(2024);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue;
            
            Map<String, Double> dayPrices = new HashMap<>();
            
            // 급등/급락 이벤트 (5% 확률)
            boolean isEventDay = globalRandom.nextDouble() < 0.05;
            
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                String stockCode = entry.getKey();
                double currentPrice = currentPrices.get(stockCode);
                
                double variation;
                if (isEventDay) {
                    // 급등/급락 이벤트 (-15% ~ +15%)
                    variation = 0.85 + (globalRandom.nextDouble() * 0.30);
                } else {
                    // 종목별 고변동성 패턴
                    variation = switch (stockCode) {
                        case "005930" -> 0.90 + (globalRandom.nextDouble() * 0.20);  // ±10%
                        case "000660" -> 0.80 + (globalRandom.nextDouble() * 0.40);  // ±20%
                        case "035420" -> 0.75 + (globalRandom.nextDouble() * 0.50);  // ±25%
                        case "005380" -> 0.85 + (globalRandom.nextDouble() * 0.30);  // ±15%
                        default -> 0.80 + (globalRandom.nextDouble() * 0.40);
                    };
                }
                
                double newPrice = currentPrice * variation;
                currentPrices.put(stockCode, newPrice);
                dayPrices.put(stockCode, newPrice);
            }
            priceData.put(date.toString(), dayPrices);
        }
        
        return priceData;
    }

    private Map<String, Map<String, Double>> createLargePriceData(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,  // 삼성전자
            "000660", 80000.0,  // SK하이닉스
            "035420", 200000.0, // NAVER
            "005380", 150000.0  // 현대차
        );
        
        // 종목별 누적 가격 추적
        Map<String, Double> currentPrices = new HashMap<>(basePrices);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue; // 주말 제외
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                String stockCode = entry.getKey();
                double currentPrice = currentPrices.get(stockCode);
                
                // 종목별 서로 다른 변동 패턴 적용
                double variation = getStockSpecificVariation(stockCode, date);
                double newPrice = currentPrice * variation;
                
                currentPrices.put(stockCode, newPrice);
                dayPrices.put(stockCode, newPrice);
            }
            priceData.put(date.toString(), dayPrices);
        }
        
        return priceData;
    }
    
    /**
     * 리밸런싱 유도를 위한 실제적인 가격 데이터 생성
     * 종목별 독립적인 변동성과 트렌드를 적용
     */
    private Map<String, Map<String, Double>> createRealisticPriceData(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,  // 삼성전자 - 안정적
            "000660", 80000.0,  // SK하이닉스 - 변동성 높음
            "035420", 200000.0, // NAVER - 성장주
            "005380", 150000.0  // 현대차 - 경기민감주
        );
        
        Map<String, Double> currentPrices = new HashMap<>(basePrices);
        Random random = new Random(42); // 일관된 결과를 위한 시드
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue;
            
            Map<String, Double> dayPrices = new HashMap<>();
            
            // 삼성전자: 안정적 +0.1% 트렌드, 낮은 변동성
            double samsungVariation = 1.001 + (random.nextGaussian() * 0.015);
            currentPrices.put("005930", currentPrices.get("005930") * samsungVariation);
            
            // SK하이닉스: 높은 변동성, 주기적 패턴
            double skVariation = 1.0 + (random.nextGaussian() * 0.03) + 
                               Math.sin(date.getDayOfYear() * 0.1) * 0.01;
            currentPrices.put("000660", currentPrices.get("000660") * skVariation);
            
            // NAVER: 성장 트렌드 +0.2%, 중간 변동성
            double naverVariation = 1.002 + (random.nextGaussian() * 0.025);
            currentPrices.put("035420", currentPrices.get("035420") * naverVariation);
            
            // 현대차: 경기 민감, 변동성 높음
            double hyundaiVariation = 1.0 + (random.nextGaussian() * 0.035) + 
                                    (Math.random() < 0.05 ? (random.nextBoolean() ? 0.05 : -0.05) : 0);
            currentPrices.put("005380", currentPrices.get("005380") * hyundaiVariation);
            
            // 현재 가격들을 dayPrices에 복사
            dayPrices.putAll(currentPrices);
            priceData.put(date.toString(), dayPrices);
        }
        
        return priceData;
    }
    
    /**
     * 리밸런싱을 강제로 유도하는 가격 데이터 생성
     * 종목 간 큰 격차를 만들어 임계값 초과를 보장
     */
    private Map<String, Map<String, Double>> createDivergentPriceData(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0,
            "005380", 150000.0
        );
        
        Map<String, Double> currentPrices = new HashMap<>(basePrices);
        long totalDays = startDate.until(endDate).getDays();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue;
            
            Map<String, Double> dayPrices = new HashMap<>();
            long daysPassed = startDate.until(date).getDays();
            double progress = (double) daysPassed / totalDays;
            
            // 삼성전자: 급등 (+50% over period)
            double samsungMultiplier = 1.0 + (progress * 0.5) + (Math.random() * 0.02 - 0.01);
            dayPrices.put("005930", basePrices.get("005930") * samsungMultiplier);
            
            // SK하이닉스: 급락 (-30% over period)  
            double skMultiplier = 1.0 - (progress * 0.3) + (Math.random() * 0.02 - 0.01);
            dayPrices.put("000660", basePrices.get("000660") * skMultiplier);
            
            // NAVER: 극심한 변동 (sine wave with growing amplitude)
            double naverMultiplier = 1.0 + Math.sin(progress * Math.PI * 4) * (0.2 * progress) + 
                                   (Math.random() * 0.03 - 0.015);
            dayPrices.put("035420", basePrices.get("035420") * naverMultiplier);
            
            // 현대차: 안정적 (작은 변동)
            double hyundaiMultiplier = 1.0 + (Math.random() * 0.01 - 0.005);
            dayPrices.put("005380", basePrices.get("005380") * hyundaiMultiplier);
            
            priceData.put(date.toString(), dayPrices);
        }
        
        return priceData;
    }
    
    /**
     * 종목별 고유한 변동 패턴을 반환
     */
    private double getStockSpecificVariation(String stockCode, LocalDate date) {
        Random random = new Random(stockCode.hashCode() + date.hashCode());
        
        return switch (stockCode) {
            case "005930" -> 0.995 + (random.nextDouble() * 0.01);  // 삼성전자: 안정적
            case "000660" -> 0.98 + (random.nextDouble() * 0.04);   // SK하이닉스: 변동성
            case "035420" -> 0.97 + (random.nextDouble() * 0.06);   // NAVER: 높은 변동성
            case "005380" -> 0.985 + (random.nextDouble() * 0.03);  // 현대차: 중간 변동성
            default -> 0.99 + (random.nextDouble() * 0.02);
        };
    }
}