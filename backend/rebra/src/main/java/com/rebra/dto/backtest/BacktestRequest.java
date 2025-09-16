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
     * 리밸런싱 수행 날짜 목록
     * PERIODIC 타입에서만 사용되며, 해당 날짜에만 리밸런싱 실행
     * THRESHOLD 타입에서는 null (매일 임계값 확인)
     */
    @JsonProperty("rebalancing_dates")
    private List<LocalDate> rebalancingDates;

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
     * 요청 정보를 요약한 문자열을 반환한다
     * 
     * @return 요약 정보
     */
    public String getSummary() {
        return String.format("백테스트 ID: %d, 기간: %s ~ %s (%d일), 종목: %d개, 리밸런싱: %s",
                backtestId, startDate, endDate, getBacktestPeriodDays(),
                stocks != null ? stocks.size() : 0, rebalancingType);
    }

}