package com.rebra.calculator.util;

import com.rebra.calculator.enums.MissingPricePolicy;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 가격 데이터 처리를 위한 유틸리티 클래스
 * null 값 처리, 이전 가격 보간, 유효성 검증 등을 담당
 */
@Slf4j
public class PriceDataUtils {
    
    /**
     * null 값이 있는 가격 데이터에서 유효한 가격만 필터링하여 반환한다
     * 
     * @param priceData 원본 가격 데이터 (null 값 포함 가능)
     * @return 유효한 가격만 포함된 맵
     */
    public static Map<String, Double> filterValidPrices(Map<String, Double> priceData) {
        if (priceData == null) {
            return Map.of();
        }
        
        return priceData.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * 누락된 가격에 대해 지정된 정책에 따라 처리한다
     * 
     * @param currentPrices 현재 날짜의 가격 데이터 (null 값 포함 가능)
     * @param previousPricesCache 이전 가격 캐시
     * @param targetStockCodes 대상 종목 코드 목록
     * @param policy 누락 가격 처리 정책
     * @param currentDate 현재 날짜 (로깅용)
     * @return 처리된 가격 데이터
     */
    public static Map<String, Double> handleMissingPrices(
            Map<String, Double> currentPrices,
            Map<String, Double> previousPricesCache,
            List<String> targetStockCodes,
            MissingPricePolicy policy,
            LocalDate currentDate) {
        
        if (currentPrices == null) {
            currentPrices = Map.of();
        }
        
        Map<String, Double> processedPrices = new LinkedHashMap<>();
        List<String> missingStocks = new ArrayList<>();
        
        for (String stockCode : targetStockCodes) {
            Double currentPrice = currentPrices.get(stockCode);
            
            if (currentPrice != null && currentPrice > 0) {
                // 유효한 가격이 있는 경우
                processedPrices.put(stockCode, currentPrice);
                // 이전 가격 캐시 업데이트
                if (previousPricesCache != null) {
                    previousPricesCache.put(stockCode, currentPrice);
                }
            } else {
                // 가격이 누락된 경우
                missingStocks.add(stockCode);
                
                switch (policy) {
                    case SKIP_STOCK:
                        // 해당 종목 제외 (processedPrices에 추가하지 않음)
                        log.debug("종목 {} 가격 누락으로 제외 - 날짜: {}", stockCode, currentDate);
                        break;
                        
                    case USE_PREVIOUS:
                        // 이전 가격 사용
                        Double previousPrice = previousPricesCache != null ? 
                                previousPricesCache.get(stockCode) : null;
                        if (previousPrice != null && previousPrice > 0) {
                            processedPrices.put(stockCode, previousPrice);
                            log.debug("종목 {} 이전 가격 {} 사용 - 날짜: {}", 
                                    stockCode, previousPrice, currentDate);
                        } else {
                            log.warn("종목 {} 이전 가격도 없어 제외 - 날짜: {}", stockCode, currentDate);
                        }
                        break;
                        
                    case HALT_REBALANCING:
                        // 리밸런싱 중단을 위해 빈 맵 반환
                        log.info("종목 {} 가격 누락으로 리밸런싱 중단 - 날짜: {}", stockCode, currentDate);
                        return Map.of();
                        
                    default:
                        log.warn("알 수 없는 누락 가격 처리 정책: {}", policy);
                        break;
                }
            }
        }
        
        if (!missingStocks.isEmpty() && !processedPrices.isEmpty()) {
            log.info("가격 누락 종목 {} 개: {} - 날짜: {}, 정책: {}", 
                    missingStocks.size(), missingStocks, currentDate, policy.getDescription());
        }
        
        return processedPrices;
    }
    
    /**
     * 목표 비중을 유효한 종목들로 재분배한다
     * 
     * @param originalWeights 원본 목표 비중 맵
     * @param validStockCodes 유효한 종목 코드 목록
     * @return 재분배된 목표 비중 맵
     */
    public static Map<String, Double> redistributeWeights(
            Map<String, Double> originalWeights, 
            Set<String> validStockCodes) {
        
        if (originalWeights == null || validStockCodes == null || validStockCodes.isEmpty()) {
            return Map.of();
        }
        
        // 유효한 종목들의 원본 비중 합계 계산
        double totalValidWeight = originalWeights.entrySet().stream()
                .filter(entry -> validStockCodes.contains(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();
        
        if (totalValidWeight <= 0) {
            return Map.of();
        }
        
        // 비중 재분배 (합계가 1.0이 되도록 정규화)
        Map<String, Double> redistributedWeights = new LinkedHashMap<>();
        for (String stockCode : validStockCodes) {
            Double originalWeight = originalWeights.get(stockCode);
            if (originalWeight != null && originalWeight > 0) {
                double redistributedWeight = originalWeight / totalValidWeight;
                redistributedWeights.put(stockCode, redistributedWeight);
            }
        }
        
        log.debug("목표 비중 재분배 완료 - 유효 종목: {} → {}", 
                originalWeights.size(), redistributedWeights.size());
        
        return redistributedWeights;
    }
    
    /**
     * 특정 종목의 최근 유효한 가격을 찾는다
     * 
     * @param dailyPrices 전체 일별 가격 데이터
     * @param stockCode 종목 코드
     * @param endDate 검색 끝 날짜 (이 날짜부터 역순으로 검색)
     * @param maxDaysBack 최대 검색 일수
     * @return 최근 유효한 가격 (없으면 null)
     */
    public static Double findRecentValidPrice(
            Map<String, Map<String, Double>> dailyPrices,
            String stockCode,
            LocalDate endDate,
            int maxDaysBack) {
        
        if (dailyPrices == null || stockCode == null || endDate == null) {
            return null;
        }
        
        LocalDate searchDate = endDate;
        for (int i = 0; i < maxDaysBack; i++) {
            String dateKey = searchDate.toString();
            Map<String, Double> dayPrices = dailyPrices.get(dateKey);
            
            if (dayPrices != null) {
                Double price = dayPrices.get(stockCode);
                if (price != null && price > 0) {
                    log.debug("종목 {} 최근 유효 가격 {} 발견 - 날짜: {} ({} 일 전)", 
                            stockCode, price, searchDate, i);
                    return price;
                }
            }
            
            searchDate = searchDate.minusDays(1);
        }
        
        log.warn("종목 {} 최근 {} 일 내 유효한 가격을 찾을 수 없음", stockCode, maxDaysBack);
        return null;
    }
    
    /**
     * 전체 데이터의 가격 누락률을 계산한다
     * 
     * @param dailyPrices 일별 가격 데이터
     * @param targetStockCodes 대상 종목 목록
     * @return 종목별 누락률 맵 (0.0 ~ 1.0)
     */
    public static Map<String, Double> calculateMissingPriceRates(
            Map<String, Map<String, Double>> dailyPrices,
            List<String> targetStockCodes) {
        
        if (dailyPrices == null || targetStockCodes == null) {
            return Map.of();
        }
        
        int totalTradingDays = dailyPrices.size();
        Map<String, Double> missingRates = new LinkedHashMap<>();
        
        for (String stockCode : targetStockCodes) {
            long validDays = dailyPrices.values().stream()
                    .filter(dayPrices -> dayPrices.containsKey(stockCode))
                    .filter(dayPrices -> {
                        Double price = dayPrices.get(stockCode);
                        return price != null && price > 0;
                    })
                    .count();
            
            double missingRate = totalTradingDays > 0 ? 
                    (double) (totalTradingDays - validDays) / totalTradingDays : 0.0;
            missingRates.put(stockCode, missingRate);
        }
        
        return missingRates;
    }
    
    /**
     * 가격 데이터 품질 정보를 생성한다
     * 
     * @param dailyPrices 일별 가격 데이터
     * @param targetStockCodes 대상 종목 목록
     * @return 데이터 품질 정보 문자열
     */
    public static String generateDataQualityReport(
            Map<String, Map<String, Double>> dailyPrices,
            List<String> targetStockCodes) {
        
        if (dailyPrices == null || targetStockCodes == null) {
            return "데이터 품질 정보를 생성할 수 없습니다";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("=== 가격 데이터 품질 보고서 ===\n");
        report.append(String.format("전체 거래일: %d일\n", dailyPrices.size()));
        report.append(String.format("대상 종목: %d개\n", targetStockCodes.size()));
        
        Map<String, Double> missingRates = calculateMissingPriceRates(dailyPrices, targetStockCodes);
        
        report.append("\n종목별 데이터 완성도:\n");
        missingRates.forEach((stockCode, missingRate) -> {
            double completeness = (1.0 - missingRate) * 100;
            report.append(String.format("  %s: %.1f%% (누락률 %.1f%%)\n", 
                    stockCode, completeness, missingRate * 100));
        });
        
        // 전체 평균 완성도
        double avgCompleteness = missingRates.values().stream()
                .mapToDouble(rate -> 1.0 - rate)
                .average()
                .orElse(0.0) * 100;
        
        report.append(String.format("\n평균 데이터 완성도: %.1f%%", avgCompleteness));
        
        return report.toString();
    }
}