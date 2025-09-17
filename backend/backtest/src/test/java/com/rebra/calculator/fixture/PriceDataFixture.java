package com.rebra.calculator.fixture;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 가격 데이터 테스트 픽스처
 * 테스트에서 사용할 다양한 가격 데이터를 생성한다.
 */
@Component
public class PriceDataFixture {

    // 기본 종목 코드와 베이스 가격
    private static final Map<String, Double> DEFAULT_BASE_PRICES = Map.of(
        "005930", 50000.0,   // 삼성전자
        "000660", 80000.0,   // SK하이닉스
        "035420", 200000.0,  // NAVER
        "005380", 150000.0,  // 현대차
        "000270", 70000.0,   // 기아
        "051910", 500000.0   // LG화학
    );

    /**
     * 안정적인 가격 데이터 생성 (±2% 변동)
     */
    public Map<String, Map<String, Double>> createStablePrices(LocalDate startDate, LocalDate endDate) {
        return createPriceData(startDate, endDate, DEFAULT_BASE_PRICES, 0.02, PricePattern.STABLE);
    }

    /**
     * 변동성이 큰 가격 데이터 생성 (±15% 변동)
     */
    public Map<String, Map<String, Double>> createVolatilePrices(LocalDate startDate, LocalDate endDate) {
        return createPriceData(startDate, endDate, DEFAULT_BASE_PRICES, 0.15, PricePattern.VOLATILE);
    }

    /**
     * 상승 트렌드 가격 데이터 생성
     */
    public Map<String, Map<String, Double>> createUpTrendPrices(LocalDate startDate, LocalDate endDate) {
        return createPriceData(startDate, endDate, DEFAULT_BASE_PRICES, 0.08, PricePattern.UP_TREND);
    }

    /**
     * 하락 트렌드 가격 데이터 생성
     */
    public Map<String, Map<String, Double>> createDownTrendPrices(LocalDate startDate, LocalDate endDate) {
        return createPriceData(startDate, endDate, DEFAULT_BASE_PRICES, 0.08, PricePattern.DOWN_TREND);
    }

    /**
     * 사이드웨이 (횡보) 가격 데이터 생성
     */
    public Map<String, Map<String, Double>> createSidewaysPrices(LocalDate startDate, LocalDate endDate) {
        return createPriceData(startDate, endDate, DEFAULT_BASE_PRICES, 0.05, PricePattern.SIDEWAYS);
    }

    /**
     * 커스텀 종목과 베이스 가격으로 가격 데이터 생성
     */
    public Map<String, Map<String, Double>> createCustomPrices(LocalDate startDate, LocalDate endDate, 
                                                               Map<String, Double> basePrices, double volatility) {
        return createPriceData(startDate, endDate, basePrices, volatility, PricePattern.STABLE);
    }

    /**
     * 일부 종목에 null 값이 포함된 가격 데이터 생성 (거래정지 시뮬레이션)
     */
    public Map<String, Map<String, Double>> createPricesWithNullValues(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = createStablePrices(startDate, endDate);
        
        // 중간 기간에 일부 종목을 null로 설정 (거래정지)
        LocalDate suspensionStart = startDate.plusDays(5);
        LocalDate suspensionEnd = startDate.plusDays(10);
        String suspendedStock = "000660"; // SK하이닉스 거래정지
        
        for (LocalDate date = suspensionStart; !date.isAfter(suspensionEnd); date = date.plusDays(1)) {
            String dateStr = date.toString();
            if (priceData.containsKey(dateStr)) {
                Map<String, Double> dayPrices = new HashMap<>(priceData.get(dateStr));
                dayPrices.put(suspendedStock, null);
                priceData.put(dateStr, dayPrices);
            }
        }
        
        return priceData;
    }

    /**
     * 일부 종목에 0 가격이 포함된 데이터 생성 (상장폐지 시뮬레이션)
     */
    public Map<String, Map<String, Double>> createPricesWithZeroValues(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = createStablePrices(startDate, endDate);
        
        // 중간부터 일부 종목을 0으로 설정 (상장폐지)
        LocalDate delistingDate = startDate.plusDays(10);
        String delistedStock = "035420"; // NAVER 상장폐지
        
        for (LocalDate date = delistingDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dateStr = date.toString();
            if (priceData.containsKey(dateStr)) {
                Map<String, Double> dayPrices = new HashMap<>(priceData.get(dateStr));
                dayPrices.put(delistedStock, 0.0);
                priceData.put(dateStr, dayPrices);
            }
        }
        
        return priceData;
    }

    /**
     * 급등주 가격 데이터 생성 (일부 종목 급등)
     */
    public Map<String, Map<String, Double>> createSurgePrices(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = createStablePrices(startDate, endDate);
        
        // 중간 지점에서 특정 종목 급등
        LocalDate surgeDate = startDate.plusDays(7);
        String surgeStock = "035420"; // NAVER 급등
        double surgeMultiplier = 1.5; // 50% 급등
        
        for (LocalDate date = surgeDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dateStr = date.toString();
            if (priceData.containsKey(dateStr)) {
                Map<String, Double> dayPrices = new HashMap<>(priceData.get(dateStr));
                Double currentPrice = dayPrices.get(surgeStock);
                if (currentPrice != null) {
                    dayPrices.put(surgeStock, currentPrice * surgeMultiplier);
                }
                priceData.put(dateStr, dayPrices);
            }
        }
        
        return priceData;
    }

    /**
     * 급락 가격 데이터 생성 (시장 크래시 시뮬레이션)
     */
    public Map<String, Map<String, Double>> createCrashPrices(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = createStablePrices(startDate, endDate);
        
        // 중간 지점에서 전체 시장 급락
        LocalDate crashDate = startDate.plusDays(10);
        double crashMultiplier = 0.7; // 30% 급락
        
        for (LocalDate date = crashDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dateStr = date.toString();
            if (priceData.containsKey(dateStr)) {
                Map<String, Double> dayPrices = priceData.get(dateStr).entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() != null ? entry.getValue() * crashMultiplier : null,
                        (e1, e2) -> e1,
                        HashMap::new
                    ));
                priceData.put(dateStr, dayPrices);
            }
        }
        
        return priceData;
    }

    /**
     * 리밸런싱 유도 가격 데이터 생성 (특정 임계값 초과하도록)
     */
    public Map<String, Map<String, Double>> createRebalancingInducingPrices(LocalDate startDate, LocalDate endDate, 
                                                                           double thresholdPercentage) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        
        // 첫날은 베이스 가격
        priceData.put(startDate.toString(), new HashMap<>(DEFAULT_BASE_PRICES));
        
        // 둘째날부터 임계값을 초과하는 변동 적용
        double changeRate = thresholdPercentage + 0.02; // 임계값 + 2% 추가
        
        for (LocalDate date = startDate.plusDays(1); !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue;
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : DEFAULT_BASE_PRICES.entrySet()) {
                // 종목별로 다른 방향의 변동 (일부는 상승, 일부는 하락)
                double multiplier = entry.getKey().equals("005930") ? (1 + changeRate) : (1 - changeRate);
                dayPrices.put(entry.getKey(), entry.getValue() * multiplier);
            }
            priceData.put(date.toString(), dayPrices);
        }
        
        return priceData;
    }

    /**
     * 단일 종목 가격 데이터 생성
     */
    public Map<LocalDate, Double> createSingleStockPrices(String stockCode, LocalDate startDate, LocalDate endDate, 
                                                          double basePrice, double volatility) {
        Map<LocalDate, Double> stockPrices = new LinkedHashMap<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue;
            
            double variation = 1.0 + ((Math.random() - 0.5) * 2 * volatility);
            double price = basePrice * variation;
            stockPrices.put(date, Math.round(price * 100) / 100.0);
        }
        
        return stockPrices;
    }

    /**
     * 빈 가격 데이터 생성 (에러 테스트용)
     */
    public Map<String, Map<String, Double>> createEmptyPrices() {
        return Collections.emptyMap();
    }

    /**
     * 불완전한 가격 데이터 생성 (시작일 또는 종료일 누락)
     */
    public Map<String, Map<String, Double>> createIncompletePrices(LocalDate startDate, LocalDate endDate, 
                                                                  boolean missingStart, boolean missingEnd) {
        Map<String, Map<String, Double>> priceData = createStablePrices(startDate, endDate);
        
        if (missingStart) {
            priceData.remove(startDate.toString());
        }
        
        if (missingEnd) {
            priceData.remove(endDate.toString());
        }
        
        return priceData;
    }

    // ===== Private Helper Methods =====

    private Map<String, Map<String, Double>> createPriceData(LocalDate startDate, LocalDate endDate, 
                                                             Map<String, Double> basePrices, double volatility, 
                                                             PricePattern pattern) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        long totalDays = startDate.until(endDate).getDays();
        long currentDay = 0;
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue; // 주말 제외
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                double price = calculatePrice(entry.getValue(), currentDay, totalDays, volatility, pattern);
                dayPrices.put(entry.getKey(), Math.round(price * 100) / 100.0); // 소수점 2자리
            }
            priceData.put(date.toString(), dayPrices);
            currentDay++;
        }
        
        return priceData;
    }

    private double calculatePrice(double basePrice, long currentDay, long totalDays, double volatility, PricePattern pattern) {
        double trendMultiplier = 1.0;
        double progress = totalDays > 0 ? (double) currentDay / totalDays : 0;
        
        switch (pattern) {
            case UP_TREND:
                trendMultiplier = 1.0 + (progress * 0.3); // 최대 30% 상승
                break;
            case DOWN_TREND:
                trendMultiplier = 1.0 - (progress * 0.25); // 최대 25% 하락
                break;
            case VOLATILE:
                trendMultiplier = 1.0 + Math.sin(progress * Math.PI * 4) * 0.1; // 4번의 큰 파동
                break;
            case SIDEWAYS:
                trendMultiplier = 1.0 + Math.sin(progress * Math.PI * 8) * 0.02; // 8번의 작은 파동
                break;
            case STABLE:
            default:
                trendMultiplier = 1.0;
                break;
        }
        
        // 일일 랜덤 변동 추가
        double dailyVariation = 1.0 + ((Math.random() - 0.5) * 2 * volatility);
        
        return basePrice * trendMultiplier * dailyVariation;
    }

    /**
     * 가격 패턴 열거형
     */
    public enum PricePattern {
        STABLE,      // 안정적
        VOLATILE,    // 변동성 큼
        UP_TREND,    // 상승 트렌드
        DOWN_TREND,  // 하락 트렌드
        SIDEWAYS     // 횡보
    }

    /**
     * 월말 기준 가격 데이터 생성 (주기적 리밸런싱 테스트용)
     */
    public Map<String, Map<String, Double>> createMonthEndFocusedPrices(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = createStablePrices(startDate, endDate);
        
        // 월말에만 큰 변동 적용
        for (Map.Entry<String, Map<String, Double>> entry : priceData.entrySet()) {
            LocalDate date = LocalDate.parse(entry.getKey());
            
            // 월말인지 확인
            if (date.equals(date.withDayOfMonth(date.lengthOfMonth()))) {
                Map<String, Double> dayPrices = new HashMap<>(entry.getValue());
                
                // 월말에 10% 변동 적용
                for (Map.Entry<String, Double> priceEntry : dayPrices.entrySet()) {
                    Double price = priceEntry.getValue();
                    if (price != null) {
                        double adjustedPrice = price * (1.0 + ((Math.random() - 0.5) * 0.2)); // ±10%
                        dayPrices.put(priceEntry.getKey(), Math.round(adjustedPrice * 100) / 100.0);
                    }
                }
                
                priceData.put(entry.getKey(), dayPrices);
            }
        }
        
        return priceData;
    }

    /**
     * 특정 날짜에 가격 데이터 추가
     */
    public void addPriceForDate(Map<String, Map<String, Double>> priceData, LocalDate date, 
                               Map<String, Double> prices) {
        priceData.put(date.toString(), new HashMap<>(prices));
    }

    /**
     * 특정 날짜의 특정 종목 가격 수정
     */
    public void updateStockPrice(Map<String, Map<String, Double>> priceData, LocalDate date, 
                                String stockCode, Double newPrice) {
        String dateStr = date.toString();
        if (priceData.containsKey(dateStr)) {
            Map<String, Double> dayPrices = new HashMap<>(priceData.get(dateStr));
            dayPrices.put(stockCode, newPrice);
            priceData.put(dateStr, dayPrices);
        }
    }
}