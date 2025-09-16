package com.rebra.calculator.context;

import com.rebra.calculator.domain.Stock;
import com.rebra.calculator.enums.RebalancingPeriod;
import com.rebra.calculator.enums.RebalancingType;
import com.rebra.calculator.strategy.RebalancingStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 백테스트 실행 컨텍스트
 * 도메인 로직에 필요한 데이터만 포함하며, BacktestRequest 참조를 제거하여 완전 분리
 */
public class BacktestContext {
    // 기본 정보
    private final Long backtestId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    
    // 도메인 객체
    private final List<Stock> stocks;
    private final LinkedHashMap<LocalDate, Map<String, Double>> dailyPrices;
    private final RebalancingStrategy rebalancingStrategy;
    
    // 비즈니스 로직에 필요한 추가 정보
    private final RebalancingType rebalancingType;
    private final RebalancingPeriod rebalancingPeriod;
    private final List<LocalDate> rebalancingDates; // PERIODIC용 리밸런싱 날짜
    
    // stocks 필드만 사용하여 모든 정보 관리 (초기 수량, 가중치, 임계값 등)
    
    /**
     * BacktestContext 생성자
     */
    public BacktestContext(Long backtestId, LocalDate startDate, LocalDate endDate,
                          List<Stock> stocks, LinkedHashMap<LocalDate, Map<String, Double>> dailyPrices,
                          RebalancingStrategy rebalancingStrategy, RebalancingType rebalancingType,
                          RebalancingPeriod rebalancingPeriod, List<LocalDate> rebalancingDates) {
        this.backtestId = backtestId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.stocks = stocks;
        this.dailyPrices = dailyPrices;
        this.rebalancingStrategy = rebalancingStrategy;
        this.rebalancingType = rebalancingType;
        this.rebalancingPeriod = rebalancingPeriod;
        this.rebalancingDates = rebalancingDates;
        
        // Stock 객체가 모든 정보를 포함하므로 추가 저장 불필요
    }
    
    // Getter 메서드들
    public Long getBacktestId() { return backtestId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<Stock> getStocks() { return stocks; }
    public LinkedHashMap<LocalDate, Map<String, Double>> getDailyPrices() { return dailyPrices; }
    public RebalancingStrategy getRebalancingStrategy() { return rebalancingStrategy; }
    public RebalancingType getRebalancingType() { return rebalancingType; }
    public RebalancingPeriod getRebalancingPeriod() { return rebalancingPeriod; }
    public List<LocalDate> getRebalancingDates() { return rebalancingDates; }
    
    // 초기 수량을 Stock 리스트에서 추출하는 헬퍼 메서드
    public Map<String, Integer> getInitialQuantities() {
        return stocks.stream()
                .collect(Collectors.toMap(
                        Stock::getStockCode,
                        Stock::getInitialQuantity
                ));
    }
    
    // 헬퍼 메서드들
    public Map<String, Double> getPricesForDate(LocalDate date) {
        return dailyPrices.get(date);
    }
    
    public boolean isValidDate(LocalDate date) {
        return date != null && !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    public long getBacktestPeriodDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    
    public LocalDate getLastDate() {
        return dailyPrices.keySet().stream()
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new RuntimeException("가격 데이터가 없습니다"));
    }
    
    /**
     * 특정 날짜가 리밸런싱 날짜인지 확인한다
     * THRESHOLD: 항상 false 반환 (매일 임계값 체크)
     * PERIODIC: rebalancingDates에 포함된 날짜만 true
     */
    public boolean isRebalancingDate(LocalDate date) {
        if (rebalancingType == RebalancingType.THRESHOLD) {
            return false; // THRESHOLD는 매일 체크하므로 여기서 false
        } else if (rebalancingType == RebalancingType.PERIODIC) {
            return rebalancingDates != null && rebalancingDates.contains(date);
        }
        return false;
    }
}