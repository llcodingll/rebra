package com.rebra.calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rebra.calculator.enums.RebalancingPeriod;
import com.rebra.calculator.enums.RebalancingType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

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
     * OHLCV 히스토리 데이터
     * 백테스트 기간 중 필요한 모든 주가 데이터
     */
    @JsonProperty("ohlcv_data")
    private List<OhlcvDataDto> ohlcvData;

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
            
            // OHLCV 데이터 검증
            if (ohlcvData == null || ohlcvData.isEmpty()) {
                return false;
            }
            
            // 모든 종목의 OHLCV 데이터가 있는지 확인
            List<String> stockCodes = stocks.stream()
                    .map(BacktestStockDto::getStockCode)
                    .distinct()
                    .toList();
            
            List<String> ohlcvStockCodes = ohlcvData.stream()
                    .map(OhlcvDataDto::getStockCode)
                    .distinct()
                    .toList();
            
            for (String stockCode : stockCodes) {
                if (!ohlcvStockCodes.contains(stockCode)) {
                    return false; // 누락된 종목이 있음
                }
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
     * 특정 종목의 OHLCV 데이터를 필터링하여 반환한다
     * 
     * @param stockCode 종목 코드
     * @return 해당 종목의 OHLCV 데이터 리스트
     */
    public List<OhlcvDataDto> getOhlcvDataForStock(String stockCode) {
        if (ohlcvData == null || stockCode == null) {
            return List.of();
        }
        
        return ohlcvData.stream()
                .filter(data -> stockCode.equals(data.getStockCode()))
                .sorted((a, b) -> a.getTradeDate().compareTo(b.getTradeDate()))
                .toList();
    }

    /**
     * 백테스트 기간 내의 모든 거래일을 반환한다
     * OHLCV 데이터에서 거래일 목록을 추출
     * 
     * @return 정렬된 거래일 리스트
     */
    public List<LocalDate> getTradingDates() {
        if (ohlcvData == null) {
            return List.of();
        }
        
        return ohlcvData.stream()
                .map(OhlcvDataDto::getTradeDate)
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * 특정 날짜의 모든 종목 가격 정보를 맵 형태로 반환한다
     * 
     * @param date 조회할 날짜
     * @return 종목코드 -> 종가 매핑
     */
    public java.util.Map<String, Double> getPricesForDate(LocalDate date) {
        if (ohlcvData == null || date == null) {
            return java.util.Map.of();
        }
        
        return ohlcvData.stream()
                .filter(data -> date.equals(data.getTradeDate()))
                .collect(java.util.stream.Collectors.toMap(
                        OhlcvDataDto::getStockCode,
                        OhlcvDataDto::getClosePrice,
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));
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

    /**
     * 디버그용 상세 정보를 반환한다
     * 
     * @return 상세 정보 문자열
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSummary()).append("\n");
        
        if (stocks != null) {
            sb.append("종목 목록:\n");
            java.util.Map<String, Double> normalizedWeights = getNormalizedWeights();
            stocks.forEach(stock -> 
                sb.append(String.format("  - %s: 가중치 %d → 목표비중 %.1f%%, 임계값 %.1f%%\n",
                    stock.getStockCode(),
                    stock.getWeight(),
                    normalizedWeights.getOrDefault(stock.getStockCode(), 0.0) * 100, 
                    stock.getThresholdPercentageValue()))
            );
        }
        
        if (ohlcvData != null) {
            sb.append(String.format("OHLCV 데이터: 총 %d건\n", ohlcvData.size()));
        }
        
        return sb.toString();
    }
}