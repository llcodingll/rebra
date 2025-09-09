package com.rebra.calculator.domain;

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
     * 거래 기록 목록
     * 발생한 모든 거래의 상세 기록
     */
    private final List<Trade> tradeHistory;
    
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
        this.totalTradingCost = 0.0;
        this.totalBorrowingCost = 0.0;
        this.tradeHistory = new ArrayList<>();
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
        
        // 기록 추가
        tradeHistory.add(trade);
        
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
        
        // 기록 추가
        tradeHistory.add(trade);
        
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
        double periodInterest = roundAmount(borrowingAmount * DAILY_BORROWING_RATE * daysPassed);
        
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
     * 
     * @param currentPrices 현재 주가 정보 (종목코드 -> 주가)
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
            } else {
                log.warn("종목 {}의 가격 정보가 없거나 유효하지 않습니다: {}", stockCode, price);
            }
        }
        
        return roundAmount(cash + stockValue);
    }

    /**
     * 각 종목별 현재 비중을 계산한다
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
        copy.totalTradingCost = this.totalTradingCost;
        copy.totalBorrowingCost = this.totalBorrowingCost;
        copy.maxBorrowingAmount = this.maxBorrowingAmount;
        copy.minCashBalance = this.minCashBalance;
        copy.initialValue = this.initialValue;
        
        
        copy.tradeHistory.clear();  
        this.tradeHistory.forEach(trade -> copy.tradeHistory.add(trade.copy()));
        
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