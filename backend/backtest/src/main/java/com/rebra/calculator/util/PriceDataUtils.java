package com.rebra.calculator.util;

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
    
}