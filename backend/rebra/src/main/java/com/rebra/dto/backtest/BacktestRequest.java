package com.rebra.dto.backtest;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rebra.enums.RebalancingPeriod;
import com.rebra.enums.RebalancingType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 백테스트 계산 요청을 나타내는 DTO 클래스
 * 메인 서버에서 Kafka를 통해 전달되는 백테스트 계산 요청 정보를 담는다.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class BacktestRequest {
    
    /**
     * 백테스트 기록 ID
     * 메인 서버의 BACKTEST_RECORD 테이블 PK
     */
    @JsonProperty("backtest_id")
    private Long backtestId;
    
    /**
     * 백테스트 시작일
     * 백테스트를 시작할 날짜
     */
    @JsonProperty("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    /**
     * 백테스트 종료일
     * 백테스트를 종료할 날짜
     */
    @JsonProperty("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    
    /**
     * 리밸런싱 유형
     * THRESHOLD(임계값 기반) 또는 PERIODIC(주기적)
     */
    @JsonProperty("rebalancing_type")
    private RebalancingType rebalancingType;
    
    /**
     * 리밸런싱 주기
     * MONTHLY(월말), QUARTERLY(분기말) 등
     * PERIODIC 타입에서만 사용됨
     */
    @JsonProperty("rebalancing_period")
    private RebalancingPeriod rebalancingPeriod;
    
    /**
     * 백테스트 대상 종목 목록
     * 각 종목의 목표 비중과 임계값 정보를 포함
     */
    @JsonProperty("stocks")
    private List<BacktestStockDto> stocks;
    
    /**
     * 일별 종목 가격 데이터
     * 날짜별로 각 종목의 가격 정보를 맵 형태로 저장
     * 키: 날짜 문자열 (yyyy-MM-dd)
     * 값: 종목코드별 가격 맵 (거래정지/상장폐지 시 null)
     */
    @JsonProperty("daily_prices")
    private Map<String, Map<String, Double>> dailyPrices;

    /**
     * 요청 데이터의 유효성을 검증한다
     * 
     * @return 유효한 요청이면 true
     */
    public boolean isValid() {
        try {
            // 필수 필드 검증
            if (backtestId == null || backtestId <= 0) {
                return false;
            }
            
            if (startDate == null || endDate == null) {
                return false;
            }
            
            if (startDate.isAfter(endDate)) {
                return false;
            }
            
            if (rebalancingType == null) {
                return false;
            }
            
            // PERIODIC 타입인 경우 rebalancingPeriod 필수
            if (rebalancingType == RebalancingType.PERIODIC && rebalancingPeriod == null) {
                return false;
            }
            
            // 종목 목록 검증
            if (stocks == null || stocks.isEmpty()) {
                return false;
            }
            
            // 가중치 합계 검증 (0보다 커야 함)
            int totalWeight = stocks.stream()
                    .mapToInt(BacktestStockDto::getWeight)
                    .sum();
            
            if (totalWeight <= 0) {
                return false;
            }
            
            // 일별 가격 데이터 검증
            if (dailyPrices == null || dailyPrices.isEmpty()) {
                return false;
            }
            
            // 시작일과 종료일 데이터 존재 여부 확인
            String startDateStr = startDate.toString();
            String endDateStr = endDate.toString();
            
            if (!dailyPrices.containsKey(startDateStr) || !dailyPrices.containsKey(endDateStr)) {
                return false;
            }
            
            // 모든 종목의 최소 데이터 존재 여부 확인
            List<String> stockCodes = stocks.stream()
                    .map(BacktestStockDto::getStockCode)
                    .distinct()
                    .toList();
            
            boolean hasMinimumData = dailyPrices.values().stream()
                    .anyMatch(dayPrices -> stockCodes.stream()
                            .anyMatch(stockCode -> dayPrices.containsKey(stockCode) && dayPrices.get(stockCode) != null));
            
            if (!hasMinimumData) {
                return false; // 어떤 날짜에도 유효한 종목 데이터가 없음
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 백테스트 기간을 일 단위로 계산한다
     * 
     * @return 백테스트 기간 (일)
     */
    public long getBacktestPeriodDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return startDate.until(endDate).getDays() + 1;
    }

    /**
     * 특정 종목의 가격 데이터를 날짜순으로 정렬하여 반환한다
     * 
     * @param stockCode 종목 코드
     * @return 해당 종목의 날짜별 가격 맵 (날짜 오름차순)
     */
    public Map<LocalDate, Double> getPricesForStock(String stockCode) {
        if (dailyPrices == null || stockCode == null) {
            return Map.of();
        }
        
        return dailyPrices.entrySet().stream()
                .filter(entry -> entry.getValue().containsKey(stockCode))
                .filter(entry -> entry.getValue().get(stockCode) != null)
                .collect(Collectors.toMap(
                        entry -> LocalDate.parse(entry.getKey()),
                        entry -> entry.getValue().get(stockCode),
                        (existing, replacement) -> existing
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));
    }

    /**
     * 백테스트 기간 내의 모든 거래일을 반환한다
     * dailyPrices 맵에서 날짜 키를 추출하여 정렬
     * 
     * @return 정렬된 거래일 리스트
     */
    public List<LocalDate> getTradingDates() {
        if (dailyPrices == null) {
            return List.of();
        }
        
        return dailyPrices.keySet().stream()
                .map(LocalDate::parse)
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .sorted()
                .toList();
    }

    /**
     * 특정 날짜의 모든 종목 가격 정보를 맵 형태로 반환한다
     * 
     * @param date 조회할 날짜
     * @return 종목코드 -> 종가 매핑 (null 값 포함)
     */
    public Map<String, Double> getPricesForDate(LocalDate date) {
        if (dailyPrices == null || date == null) {
            return Map.of();
        }
        
        String dateStr = date.toString();
        return dailyPrices.getOrDefault(dateStr, Map.of());
    }

    /**
     * 요청 정보를 요약한 문자열을 반환한다
     * 
     * @return 요약 정보
     */
    public String getSummary() {
        return String.format("백테스트 ID: %d, 기간: %s ~ %s (%d일), 종목: %d개, 리밸런싱: %s",
                backtestId, startDate, endDate, getBacktestPeriodDays(),
                stocks != null ? stocks.size() : 0, rebalancingType);
    }

    /**
     * 가중치를 비중으로 정규화한다
     * 
     * @return 정규화된 종목별 목표 비중 맵 (종목코드 -> 비중)
     */
    public java.util.Map<String, Double> getNormalizedWeights() {
        if (stocks == null || stocks.isEmpty()) {
            return java.util.Map.of();
        }
        
        // 전체 가중치 합계 계산
        int totalWeight = stocks.stream()
                .mapToInt(BacktestStockDto::getWeight)
                .sum();
        
        if (totalWeight == 0) {
            return java.util.Map.of();
        }
        
        // 각 종목의 비중 계산
        return stocks.stream()
                .collect(java.util.stream.Collectors.toMap(
                    BacktestStockDto::getStockCode,
                    stock -> (double) stock.getWeight() / totalWeight,
                    (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));
    }

    /**
     * 특정 종목의 정규화된 목표 비중을 반환한다
     * 
     * @param stockCode 종목 코드
     * @return 정규화된 목표 비중 (0.0 ~ 1.0)
     */
    public double getNormalizedWeight(String stockCode) {
        return getNormalizedWeights().getOrDefault(stockCode, 0.0);
    }
}