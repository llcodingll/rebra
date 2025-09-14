package com.rebra.calculator.domain;

import com.rebra.calculator.context.BacktestContext;
import com.rebra.calculator.strategy.RebalancingStrategy;
import com.rebra.calculator.util.PriceDataUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static com.rebra.calculator.constant.BacktestConstants.TradingRules.*;
import static com.rebra.calculator.constant.BacktestConstants.BorrowingCosts.DAILY_BORROWING_RATE;

/**
 * 포트폴리오의 현재 상태를 관리하는 도메인 클래스
 * 보유 주식, 현금, 차입 정보 등을 포함하며 백테스트의 핵심 상태를 담당한다.
 */
@Slf4j
@Getter
public class Portfolio {
    
    /**
     * 현금 잔액 (원)
     * 음수 가능 (차입 상태를 나타냄)
     */
    private double cash;
    
    /**
     * 주식 보유 현황
     * Key: 종목코드, Value: 보유 수량
     */
    private final Map<String, Integer> holdings;
    
    /**
     * 목표 종목 정보
     * Key: 종목코드, Value: 종목 정보 (목표 비중, 임계값 포함)
     */
    private final Map<String, Stock> targetStocks;
    
    /**
     * 자동 비중 재조정 활성화 여부
     * 종목이 제거될 때 나머지 종목들의 비중을 자동으로 재조정할지 결정
     */
    private boolean autoRebalance;
    
    
    /**
     * 총 거래비용 누적액 (원)
     * 수수료 + 세금의 합계
     */
    private double totalTradingCost;
    
    /**
     * 총 차입비용 누적액 (원)
     * 음수 현금에 대한 일일 이자 누적액
     */
    private double totalBorrowingCost;
    
    
    
    /**
     * 최대 차입 금액 (원)
     * 백테스트 기간 중 발생한 최대 차입 금액
     */
    private double maxBorrowingAmount;
    
    /**
     * 최소 현금 잔액 (원)
     * 백테스트 기간 중 최소 현금 잔액 (음수 포함)
     */
    private double minCashBalance;

    /**
     * 초기 포트폴리오 가치 (원)
     * 백테스트 시작 시점의 포트폴리오 총 가치
     */
    private double initialValue;
    

    /**
     * Portfolio 생성자
     * 초기 보유 주식 기반으로 시작하므로 초기 현금은 항상 0으로 시작
     */
    public Portfolio() {
        this.cash = 0.0;
        this.holdings = new HashMap<>();
        this.targetStocks = new HashMap<>();
        this.autoRebalance = true; // 기본값: 자동 비중 재조정 활성화
        this.totalTradingCost = 0.0;
        this.totalBorrowingCost = 0.0;
        this.maxBorrowingAmount = 0.0;
        this.minCashBalance = 0.0;
        this.initialValue = 0.0; // 초기 구성 완료 후 설정
        
        log.info("포트폴리오 생성 - 초기 현금: 0원");
    }

    /**
     * 초기 보유 주식을 설정한다 (거래 없이 직접 설정)
     * 백테스트 시작 시 초기 포트폴리오 구성용
     * 
     * @param stockCode 종목 코드
     * @param quantity 보유 수량
     * @throws IllegalArgumentException 잘못된 매개변수
     */
    public void setInitialHolding(String stockCode, int quantity) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("종목 코드가 유효하지 않습니다: " + stockCode);
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("보유 수량은 양수여야 합니다: " + quantity);
        }
        
        // 보유 주식 설정
        holdings.put(stockCode.trim().toUpperCase(), quantity);
        
        log.debug("초기 보유 주식 설정: {} - {}주", stockCode, quantity);
    }

    /**
     * 주식을 매수한다
     * 현금이 부족해도 차입하여 매수 진행
     * 
     * @param stockCode 종목 코드
     * @param quantity 매수 수량
     * @param price 매수 가격
     * @param fee 수수료
     * @param date 거래 날짜
     * @return 생성된 거래 기록
     * @throws IllegalArgumentException 잘못된 매개변수
     */
    public Trade buyStock(String stockCode, int quantity, double price, 
                         double fee, LocalDate date) {
        validateTradeParameters(stockCode, quantity, price, fee, date);
        
        // 거래 생성 (매수에는 세금 없음)
        Trade trade = new Trade(date, stockCode, Trade.TradeType.BUY, 
                              quantity, price, fee, 0.0);
        
        // 보유 주식 수량 업데이트
        int currentHoldings = holdings.getOrDefault(stockCode, 0);
        holdings.put(stockCode, currentHoldings + quantity);
        
        // 현금 차감 (음수 가능)
        cash -= trade.getNetAmount();
        cash = roundAmount(cash);
        
        // 통계 업데이트
        totalTradingCost += trade.getTotalCost();
        updateCashStatistics();
        
        
        log.debug("매수 실행 - {}: {}주 @{:.0f}원, 현금잔액: {:.0f}원", 
                stockCode, quantity, price, cash);
        
        return trade;
    }

    /**
     * 주식을 매도한다
     * 
     * @param stockCode 종목 코드
     * @param quantity 매도 수량
     * @param price 매도 가격
     * @param fee 수수료
     * @param tax 증권거래세
     * @param date 거래 날짜
     * @return 생성된 거래 기록
     * @throws IllegalArgumentException 잘못된 매개변수 또는 보유 수량 부족
     */
    public Trade sellStock(String stockCode, int quantity, double price, 
                          double fee, double tax, LocalDate date) {
        validateTradeParameters(stockCode, quantity, price, fee, date);
        
        // 보유 수량 확인
        int currentHoldings = holdings.getOrDefault(stockCode, 0);
        if (currentHoldings < quantity) {
            throw new IllegalArgumentException(
                String.format("보유 수량이 부족합니다. 보유: %d주, 매도 요청: %d주", currentHoldings, quantity));
        }
        
        // 거래 생성
        Trade trade = new Trade(date, stockCode, Trade.TradeType.SELL, 
                              quantity, price, fee, tax);
        
        // 보유 주식 수량 업데이트
        int newHoldings = currentHoldings - quantity;
        if (newHoldings == 0) {
            holdings.remove(stockCode);
        } else {
            holdings.put(stockCode, newHoldings);
        }
        
        // 현금 증가
        cash += trade.getNetAmount();
        cash = roundAmount(cash);
        
        // 통계 업데이트
        totalTradingCost += trade.getTotalCost();
        updateCashStatistics();
        
        
        log.debug("매도 실행 - {}: {}주 @{:.0f}원, 현금잔액: {:.0f}원", 
                stockCode, quantity, price, cash);
        
        return trade;
    }

    /**
     * 경과 기간에 따른 차입 이자를 계산하고 차감한다
     * 현금이 음수인 경우에만 실행
     * 
     * @param currentDate 계산 날짜
     * @param daysPassed 경과 일수
     * @return 발생한 차입 이자 (발생하지 않으면 0)
     */
    public double calculateAndDeductBorrowingCost(LocalDate currentDate, int daysPassed) {
        if (cash >= 0) {
            return 0.0; // 차입이 없으면 이자도 없음
        }
        
        if (daysPassed <= 0) {
            throw new IllegalArgumentException("경과 일수는 양수여야 합니다: " + daysPassed);
        }
        
        double borrowingAmount = Math.abs(cash);
        
        // 복리 계산: 원금 × ((1 + 일일이자율)^일수 - 1)
        double compoundInterest = borrowingAmount * (Math.pow(1 + DAILY_BORROWING_RATE, daysPassed) - 1);
        double periodInterest = roundAmount(compoundInterest);
        
        // 차입 이자 차감
        cash -= periodInterest;
        cash = roundAmount(cash);
        
        // 통계 업데이트
        totalBorrowingCost += periodInterest;
        updateBorrowingStatistics(borrowingAmount);
        updateCashStatistics();
        
        
        log.debug("차입 이자 발생 - 날짜: {}, 차입금: {:.0f}원, 경과일수: {}일, 기간이자: {:.0f}원, 누적이자: {:.0f}원", 
                currentDate, borrowingAmount, daysPassed, periodInterest, totalBorrowingCost);
        
        return periodInterest;
    }

    /**
     * 특정 종목의 보유 수량을 반환한다
     * 
     * @param stockCode 종목 코드
     * @return 보유 수량 (보유하지 않으면 0)
     */
    public int getHoldings(String stockCode) {
        return holdings.getOrDefault(stockCode, 0);
    }

    /**
     * 현재 포트폴리오의 총 가치를 계산한다
     * null 가격 처리 정책에 따라 이전 가격 사용 또는 제외
     * 
     * @param currentPrices 현재 주가 정보 (종목코드 -> 주가, null 값 포함 가능)
     * @return 총 포트폴리오 가치 (현금 + 주식 평가액)
     */
    public double getTotalValue(Map<String, Double> currentPrices) {
        if (currentPrices == null || currentPrices.isEmpty()) {
            return cash; // 주식 가격 정보가 없으면 현금만 반환
        }
        
        
        double stockValue = 0.0;
        
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            String stockCode = entry.getKey();
            int quantity = entry.getValue();
            
            Double price = currentPrices.get(stockCode);
            if (price != null && price > 0) {
                stockValue += quantity * price;
                log.trace("종목 {} 가치 계산: {}주 × {:.0f}원 = {:.0f}원", 
                        stockCode, quantity, price, quantity * price);
            } else {
                log.debug("종목 {}의 유효한 가격을 찾을 수 없어 가치 계산에서 제외", stockCode);
            }
        }
        
        return roundAmount(cash + stockValue);
    }

    /**
     * 각 종목별 현재 비중을 계산한다
     * null 가격 처리 정책에 따라 이전 가격 사용 또는 0으로 처리
     * 
     * @param currentPrices 현재 주가 정보
     * @return 종목별 비중 맵 (종목코드 -> 비중)
     */
    public Map<String, Double> getCurrentWeights(Map<String, Double> currentPrices) {
        double totalValue = getTotalValue(currentPrices);
        Map<String, Double> weights = new HashMap<>();
        
        if (totalValue <= 0) {
            // 포트폴리오 가치가 0 이하면 모든 비중을 0으로 설정
            for (String stockCode : holdings.keySet()) {
                weights.put(stockCode, 0.0);
            }
            return weights;
        }
        
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            String stockCode = entry.getKey();
            int quantity = entry.getValue();
            
            Double price = currentPrices.get(stockCode);
            if (price != null && price > 0) {
                double stockValue = quantity * price;
                double weight = stockValue / totalValue;
                weights.put(stockCode, roundWeight(weight));
            } else {
                weights.put(stockCode, 0.0);
                log.debug("종목 {}의 유효한 가격이 없어 비중을 0으로 설정", stockCode);
            }
        }
        
        return weights;
    }

    /**
     * 보유 중인 모든 종목 코드를 반환한다
     * 
     * @return 종목 코드 집합
     */
    public Set<String> getHoldingStockCodes() {
        return new HashSet<>(holdings.keySet());
    }

    /**
     * 차입 상태인지 확인한다
     * 
     * @return 현금이 음수면 true
     */
    public boolean isBorrowing() {
        return cash < 0;
    }

    /**
     * 현재 차입 금액을 반환한다
     * 
     * @return 차입 금액 (차입 중이 아니면 0)
     */
    public double getCurrentBorrowingAmount() {
        return cash < 0 ? Math.abs(cash) : 0.0;
    }


    /**
     * 목표 종목을 추가한다 (기존 호환성 메서드)
     * 
     * @param stock 추가할 종목 정보 (목표 비중, 임계값 포함)
     * @throws IllegalArgumentException 종목이 null이거나 이미 존재하는 경우
     */
    public void addTargetStock(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("종목 정보는 null일 수 없습니다");
        }
        
        String stockCode = stock.getStockCode();
        if (targetStocks.containsKey(stockCode)) {
            throw new IllegalArgumentException("이미 존재하는 종목입니다: " + stockCode);
        }
        
        targetStocks.put(stockCode, stock.copy());
        
        // 자동 재조정이 활성화되어 있으면 비중 재계산
        if (autoRebalance) {
            adjustWeights();
        }
        
        // 현재 전체 가중치로 목표 비중 계산
        int totalWeight = getTotalOriginalWeight();
        
        log.debug("목표 종목 추가: {} (원본가중치: {}, 목표비중: {:.2f}%, 임계값: {:.2f}%)", 
                stockCode, stock.getOriginalWeight(), 
                totalWeight > 0 ? stock.getTargetWeightPercentage(totalWeight) : 0.0, 
                stock.getThresholdPercentage());
    }

    /**
     * BacktestStockDto를 사용하여 목표 종목을 추가한다
     * 
     * @param stockDto 백테스트 종목 DTO (원본 가중치와 임계값 포함)
     * @throws IllegalArgumentException 종목이 null이거나 이미 존재하는 경우
     */
    public void addTargetStock(com.rebra.calculator.dto.BacktestStockDto stockDto) {
        if (stockDto == null) {
            throw new IllegalArgumentException("종목 DTO는 null일 수 없습니다");
        }
        
        String stockCode = stockDto.getStockCode();
        if (targetStocks.containsKey(stockCode)) {
            throw new IllegalArgumentException("이미 존재하는 종목입니다: " + stockCode);
        }
        
        // Stock 객체 생성 (원본 가중치, 임계값, 초기수량)
        int initialQuantity = stockDto.getShares() != null ? stockDto.getShares() : 0;
        Stock stock = new Stock(stockCode, stockDto.getWeight(), stockDto.getThresholdPercentageValue(), initialQuantity);
        
        targetStocks.put(stockCode, stock);
        
        // 자동 재조정이 활성화되어 있으면 원본 가중치 기반 재계산
        if (autoRebalance) {
            adjustWeights();
        }
        
        // 현재 전체 가중치로 목표 비중 계산
        int totalWeight = getTotalOriginalWeight();
        
        log.debug("목표 종목 추가 (DTO 기반): {} (원본가중치: {}, 목표비중: {:.2f}%, 임계값: {:.2f}%)", 
                stockCode, stockDto.getWeight(), 
                totalWeight > 0 ? stock.getTargetWeightPercentage(totalWeight) : 0.0,
                stockDto.getThresholdPercentageValue());
    }

    /**
     * 목표 종목을 제거한다
     * 자동 재조정이 활성화된 경우 나머지 종목들의 비중을 재조정한다
     * 
     * @param stockCode 제거할 종목 코드
     * @return 제거된 종목 정보 (제거되지 않았으면 null)
     */
    public Stock removeTargetStock(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다");
        }
        
        Stock removedStock = targetStocks.remove(stockCode.trim().toUpperCase());
        if (removedStock == null) {
            return null;
        }
        
        // 제거 전 전체 가중치 계산 (제거될 종목 포함)
        int totalWeightBefore = getTotalOriginalWeight() + removedStock.getOriginalWeight();
        
        log.info("목표 종목 제거: {} (원본가중치: {}, 제거전비중: {:.2f}%)", 
                stockCode, removedStock.getOriginalWeight(),
                totalWeightBefore > 0 ? ((double) removedStock.getOriginalWeight() / totalWeightBefore) * 100 : 0.0);
        
        // 자동 재조정이 활성화되어 있고 제거할 종목이 있으면 비중 재조정
        if (autoRebalance && !targetStocks.isEmpty()) {
            // 원본 가중치 기반으로는 별도 조정이 필요하지 않음 (자동으로 정규화됨)
            log.info("원본 가중치 기반 비중은 자동으로 재정규화됩니다");
        }
        
        return removedStock;
    }

    /**
     * 종목 제거 후 나머지 종목들의 비중을 재조정한다
     * 원본 가중치가 있으면 원본 기준으로, 없으면 현재 비중을 비례 배분
     * 
     * @param removedWeight 제거된 종목의 비중 (참고용)
     */
    private void adjustWeightsAfterRemoval(double removedWeight) {
        if (targetStocks.isEmpty()) {
            return;
        }
        
        log.info("종목 제거 후 비중 재조정 시작 - 제거된 비중: {:.2f}%, 남은 종목수: {}", 
                removedWeight * 100, targetStocks.size());
        
        // 원본 가중치가 있으면 원본 기준으로 재정규화
        boolean hasOriginalWeights = targetStocks.values().stream()
                .anyMatch(Stock::hasOriginalWeight);
        
        if (hasOriginalWeights) {
            // 원본 가중치 기반에서는 별도 조정 불필요 (자동 정규화)
            log.info("원본 가중치 기반으로 비중이 자동 재정규화됩니다");
        } else {
            // 원본 가중치가 없으면 기존 로직 사용 (비례 배분)
            adjustWeightsByProportionalDistribution(removedWeight);
        }
    }
    
    /**
     * 제거된 비중을 나머지 종목들에게 비례적으로 분배 (레거시 메서드)
     * 원본 가중치 기반 시스템에서는 더 이상 사용하지 않음
     * 
     * @param removedWeight 제거된 종목의 비중 (사용되지 않음)
     */
    private void adjustWeightsByProportionalDistribution(double removedWeight) {
        // 원본 가중치 기반 시스템에서는 자동으로 재정규화되므로 별도 작업 불필요
        log.debug("원본 가중치 기반에서는 비례 배분이 자동으로 처리됩니다");
    }

    /**
     * 목표 종목들의 원본 가중치 유효성을 검증한다
     * 원본 가중치 기반 시스템에서는 별도 조정이 필요하지 않음
     */
    public void adjustWeights() {
        if (targetStocks.isEmpty()) {
            return;
        }
        
        // 원본 가중치 유효성 검증
        boolean allHaveOriginalWeights = validateOriginalWeights();
        
        if (!allHaveOriginalWeights) {
            log.warn("일부 종목에 원본 가중치가 없습니다. 이는 예상되지 않은 상황입니다.");
            targetStocks.values().forEach(stock -> {
                if (!stock.hasOriginalWeight()) {
                    log.warn("원본 가중치 누락 종목: {}", stock.getStockCode());
                }
            });
        }
        
        int totalWeight = getTotalOriginalWeight();
        log.debug("현재 전체 원본 가중치: {}, 종목수: {}", totalWeight, targetStocks.size());
    }
    
    /**
     * 원본 가중치의 유효성을 검증한다
     * 모든 종목에 원본 가중치가 설정되어 있는지 확인
     * 
     * @return 모든 종목이 유효한 원본 가중치를 가지고 있으면 true
     */
    private boolean validateOriginalWeights() {
        return targetStocks.values().stream()
                .allMatch(Stock::hasOriginalWeight);
    }
    
    /**
     * 현재 목표 비중 기준으로 정규화 (원본 가중치가 없는 경우)
     */
    private void normalizeByCurrentWeights() {
        // 원본 가중치 기반 시스템에서는 자동으로 정규화되므로 별도 작업 불필요
        log.debug("원본 가중치 기반에서는 정규화가 자동으로 처리됩니다");
        return;
        
        // 나머지 코드는 더 이상 필요하지 않음
        /*
        if (totalWeight <= 0) {
            log.warn("총 비중이 0입니다. 비중 재조정을 건너뜁니다.");
            return;
        }
        
        log.info("현재 비중 기반 정규화 시작 - 현재 총 비중: {:.2f}%", totalWeight * 100);
        
        // 각 종목의 비중을 정규화
        for (Stock stock : targetStocks.values()) {
            double currentWeight = stock.getTargetWeight();
            double normalizedWeight = currentWeight / totalWeight;
            
            stock.setTargetWeight(normalizedWeight);
            
            log.debug("종목 {} 비중 정규화: {:.2f}% → {:.2f}%", 
                    stock.getStockCode(), currentWeight * 100, normalizedWeight * 100);
        }
        
        log.info("현재 비중 기반 정규화 완료 - 종목수: {}", targetStocks.size());
        */
    }

    /**
     * 목표 종목 목록을 반환한다 (복사본)
     * 
     * @return 목표 종목 목록의 복사본
     */
    public Map<String, Stock> getTargetStocks() {
        Map<String, Stock> copy = new HashMap<>();
        targetStocks.forEach((code, stock) -> copy.put(code, stock.copy()));
        return copy;
    }

    /**
     * 특정 종목의 목표 종목 정보를 반환한다
     * 
     * @param stockCode 종목 코드
     * @return 목표 종목 정보 (없으면 null)
     */
    public Stock getTargetStock(String stockCode) {
        if (stockCode == null) {
            return null;
        }
        
        Stock stock = targetStocks.get(stockCode.trim().toUpperCase());
        return stock != null ? stock.copy() : null;
    }

    /**
     * 자동 비중 재조정 활성화 여부를 설정한다
     * 
     * @param autoRebalance 자동 재조정 활성화 여부
     */
    public void setAutoRebalance(boolean autoRebalance) {
        this.autoRebalance = autoRebalance;
        log.debug("자동 비중 재조정 설정 변경: {}", autoRebalance ? "활성화" : "비활성화");
    }

    /**
     * 자동 비중 재조정 활성화 여부를 반환한다
     * 
     * @return 자동 재조정 활성화 여부
     */
    public boolean isAutoRebalance() {
        return autoRebalance;
    }

    /**
     * 목표 종목들의 목표 비중을 업데이트한다
     * 유효한 가격을 가진 종목들만으로 비중을 재계산하여 정규화
     * 
     * @param validPrices 유효한 가격 정보 (null이나 음수 가격 제외)
     */
    public void updateTargetWeights(Map<String, Double> validPrices) {
        if (targetStocks.isEmpty() || validPrices == null || validPrices.isEmpty()) {
            log.debug("목표 종목이 없거나 유효한 가격 정보가 없어 목표 비중 업데이트를 건너뜁니다");
            return;
        }
        
        // 유효한 가격을 가진 종목들만 필터링
        List<Stock> validStocks = targetStocks.values().stream()
                .filter(stock -> validPrices.containsKey(stock.getStockCode()))
                .toList();
        
        if (validStocks.isEmpty()) {
            log.warn("유효한 가격을 가진 목표 종목이 없습니다");
            // 모든 종목의 목표 비중을 0으로 설정
            targetStocks.values().forEach(stock -> stock.setTargetWeight(0.0));
            return;
        }
        
        // 유효한 종목들의 원본 가중치 합계 계산 (한 번만)
        int totalValidWeight = validStocks.stream()
                .mapToInt(Stock::getOriginalWeight)
                .sum();
        
        if (totalValidWeight <= 0) {
            log.warn("유효한 종목들의 원본 가중치 합계가 0 이하입니다: {}", totalValidWeight);
            targetStocks.values().forEach(stock -> stock.setTargetWeight(0.0));
            return;
        }
        
        // 각 종목의 목표 비중 업데이트
        for (Stock stock : targetStocks.values()) {
            if (validPrices.containsKey(stock.getStockCode())) {
                // 유효한 종목: 정규화된 목표 비중 계산
                double normalizedWeight = (double) stock.getOriginalWeight() / totalValidWeight;
                stock.setTargetWeight(normalizedWeight);
                log.trace("종목 {} 목표 비중 업데이트: {:.2f}%", 
                        stock.getStockCode(), normalizedWeight * 100);
            } else {
                // 유효하지 않은 종목: 목표 비중 0
                stock.setTargetWeight(0.0);
                log.trace("종목 {} 목표 비중 업데이트: 0.0% (가격 정보 없음)", stock.getStockCode());
            }
        }
        
        log.info("목표 비중 업데이트 완료 - 유효 종목: {}개, 전체 종목: {}개", 
                validStocks.size(), targetStocks.size());
    }

    /**
     * 리밸런싱이 필요한지 판단한다
     * 백테스트 컨텍스트의 정보를 활용하여 리밸런싱 전략에 따라 판단
     * 
     * @param context 백테스트 컨텍스트
     * @param currentDate 현재 날짜
     * @param lastRebalancingDate 마지막 리밸런싱 날짜
     * @return 리밸런싱이 필요하면 true
     */
    public boolean shouldRebalance(Map<String, Double> currentPrices, BacktestContext context, LocalDate currentDate, LocalDate lastRebalancingDate) {
        if (targetStocks.isEmpty()) {
            log.debug("목표 종목이 설정되지 않아 리밸런싱을 건너뜁니다");
            return false;
        }
        
        RebalancingStrategy rebalancingStrategy = context.getRebalancingStrategy();
        if (rebalancingStrategy == null) {
            log.warn("리밸런싱 전략이 설정되지 않았습니다");
            return false;
        }
        
        if (currentPrices == null) {
            log.warn("날짜 {}의 가격 정보를 찾을 수 없습니다", currentDate);
            return false;
        }
        
        // 유효한 가격 정보로 목표 비중 업데이트
        Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(currentPrices);
        updateTargetWeights(validPrices);
        
        return rebalancingStrategy.shouldRebalance(currentPrices, context, currentDate, this, lastRebalancingDate);
    }

    /**
     * 리밸런싱이 필요한 목표 종목들을 반환한다
     * 
     * @param context 백테스트 컨텍스트
     * @return 리밸런싱이 필요한 종목 리스트
     */
    public List<Stock> getStocksNeedingRebalancing(BacktestContext context) {
        if (targetStocks.isEmpty()) {
            return new ArrayList<>();
        }
        
        RebalancingStrategy rebalancingStrategy = context.getRebalancingStrategy();
        if (rebalancingStrategy == null) {
            return new ArrayList<>();
        }
        return rebalancingStrategy.getStocksNeedingRebalancing(context, this);
    }

    /**
     * 목표 종목들의 원본 가중치를 검증한다
     * 모든 종목이 유효한 원본 가중치를 가지고 있는지 확인
     * 
     * @return 모든 종목이 유효한 원본 가중치를 가지고 있으면 true
     */
    public boolean validateTargetWeights() {
        if (targetStocks.isEmpty()) {
            return true;
        }
        
        // 모든 종목이 유효한 원본 가중치를 가지고 있는지 확인
        boolean allHaveOriginalWeights = validateOriginalWeights();
        int totalOriginalWeight = getTotalOriginalWeight();
        
        boolean isValid = allHaveOriginalWeights && totalOriginalWeight > 0;
        
        if (!isValid) {
            if (!allHaveOriginalWeights) {
                log.warn("일부 종목에 원본 가중치가 누락되었습니다");
            }
            if (totalOriginalWeight <= 0) {
                log.warn("전체 원본 가중치가 0 이하입니다: {}", totalOriginalWeight);
            }
        } else {
            log.debug("원본 가중치 검증 완료 - 전체 가중치: {}, 종목수: {}", 
                    totalOriginalWeight, targetStocks.size());
        }
        
        return isValid;
    }

    /**
     * 목표 종목 개수를 반환한다
     * 
     * @return 설정된 목표 종목 개수
     */
    public int getTargetStockCount() {
        return targetStocks.size();
    }

    /**
     * 모든 목표 종목의 원본 가중치 합을 계산한다
     * 
     * @return 전체 원본 가중치 합
     */
    public int getTotalOriginalWeight() {
        return targetStocks.values().stream()
                .mapToInt(Stock::getOriginalWeight)
                .sum();
    }

    /**
     * 특정 종목이 목표 종목으로 설정되어 있는지 확인한다
     * 
     * @param stockCode 종목 코드
     * @return 목표 종목이면 true
     */
    public boolean isTargetStock(String stockCode) {
        if (stockCode == null) {
            return false;
        }
        return targetStocks.containsKey(stockCode.trim().toUpperCase());
    }







    /**
     * 초기 포트폴리오 가치를 설정한다
     * 초기 보유 주식 구성 완료 후 한 번만 호출되어야 함
     * 
     * @param initialValue 초기 포트폴리오 가치
     * @throws IllegalArgumentException 초기 가치가 음수인 경우
     * @throws IllegalStateException 이미 초기 가치가 설정된 경우
     */
    public void setInitialValue(double initialValue) {
        if (initialValue <= 0) {
            throw new IllegalArgumentException("초기 포트폴리오 가치는 양수여야 합니다: " + initialValue);
        }
        
        if (this.initialValue > 0) {
            throw new IllegalStateException("초기 가치는 이미 설정되었습니다: " + this.initialValue);
        }
        
        this.initialValue = roundAmount(initialValue);
        
        log.info("초기 포트폴리오 가치 설정 - {:.0f}원", this.initialValue);
    }

    /**
     * 초기 포트폴리오 가치를 반환한다
     * 
     * @return 초기 포트폴리오 가치
     */
    public double getInitialValue() {
        return initialValue;
    }

    /**
     * 현재 포트폴리오의 누적 수익률을 계산한다
     * 
     * @param currentPrices 현재 주가 정보
     * @return 누적 수익률 (예: 0.1 = 10% 수익)
     */
    public double getCumulativeReturn(Map<String, Double> currentPrices) {
        if (initialValue <= 0) {
            throw new IllegalStateException("초기 가치가 설정되지 않았습니다");
        }
        
        double currentValue = getTotalValue(currentPrices);
        return (currentValue - initialValue) / initialValue;
    }

    /**
     * 거래 매개변수 유효성 검증
     */
    private void validateTradeParameters(String stockCode, int quantity, double price, 
                                       double fee, LocalDate date) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다.");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("거래 수량은 양수여야 합니다: " + quantity);
        }
        
        if (price <= 0) {
            throw new IllegalArgumentException("가격은 양수여야 합니다: " + price);
        }
        
        if (fee < 0) {
            throw new IllegalArgumentException("수수료는 음수일 수 없습니다: " + fee);
        }
        
        if (date == null) {
            throw new IllegalArgumentException("거래 날짜는 필수입니다.");
        }
    }

    /**
     * 현금 통계 업데이트
     */
    private void updateCashStatistics() {
        if (cash < minCashBalance) {
            minCashBalance = cash;
        }
    }

    /**
     * 차입 통계 업데이트
     */
    private void updateBorrowingStatistics(double borrowingAmount) {
        if (borrowingAmount > maxBorrowingAmount) {
            maxBorrowingAmount = borrowingAmount;
        }
    }

    /**
     * 금액을 반올림한다
     */
    private double roundAmount(double amount) {
        return BigDecimal.valueOf(amount)
                .setScale(0, AMOUNT_ROUNDING_MODE)
                .doubleValue();
    }

    /**
     * 비중을 정규화한다
     */
    private double roundWeight(double weight) {
        return BigDecimal.valueOf(weight)
                .setScale(WEIGHT_PRECISION, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 포트폴리오의 복사본을 생성한다
     * 깊은 복사를 수행하여 독립적인 객체를 반환한다
     * 
     * @return Portfolio 객체의 복사본
     */
    public Portfolio copy() {
        Portfolio copy = new Portfolio();
        copy.cash = this.cash;
        copy.holdings.clear();
        copy.holdings.putAll(this.holdings);
        copy.targetStocks.clear();
        // Stock 객체도 깊은 복사
        this.targetStocks.forEach((code, stock) -> copy.targetStocks.put(code, stock.copy()));
        copy.autoRebalance = this.autoRebalance;
        copy.totalTradingCost = this.totalTradingCost;
        copy.totalBorrowingCost = this.totalBorrowingCost;
        copy.maxBorrowingAmount = this.maxBorrowingAmount;
        copy.minCashBalance = this.minCashBalance;
        copy.initialValue = this.initialValue;
        
        return copy;
    }

    /**
     * 포트폴리오 상태를 상세히 출력한다
     * 
     * @param currentPrices 현재 주가 정보
     * @return 상세 상태 문자열
     */
    public String toDetailedString(Map<String, Double> currentPrices) {
        StringBuilder sb = new StringBuilder();
        sb.append("Portfolio Status:\n");
        sb.append(String.format("  현금: %,.0f원\n", cash));
        sb.append(String.format("  총 가치: %,.0f원\n", getTotalValue(currentPrices)));
        sb.append(String.format("  총 거래비용: %,.0f원\n", totalTradingCost));
        sb.append(String.format("  총 차입비용: %,.0f원\n", totalBorrowingCost));
        sb.append(String.format("  최대 차입금: %,.0f원\n", maxBorrowingAmount));
        sb.append(String.format("  최소 현금: %,.0f원\n", minCashBalance));
        sb.append("  보유 종목:\n");
        
        holdings.forEach((code, quantity) -> {
            Double price = currentPrices.get(code);
            if (price != null) {
                double value = quantity * price;
                double weight = getTotalValue(currentPrices) > 0 ? 
                    value / getTotalValue(currentPrices) * 100 : 0;
                sb.append(String.format("    %s: %d주 (%,.0f원, %.1f%%)\n", 
                    code, quantity, value, weight));
            }
        });
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("Portfolio{cash=%.0f, holdings=%d종목, totalTradingCost=%.0f, totalBorrowingCost=%.0f}", 
                cash, holdings.size(), totalTradingCost, totalBorrowingCost);
    }
}