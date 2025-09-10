package com.rebra.calculator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.rebra.calculator.constant.BacktestConstants.TradingFees.*;
import static com.rebra.calculator.constant.BacktestConstants.BorrowingCosts.*;
import static com.rebra.calculator.constant.BacktestConstants.TradingRules.AMOUNT_ROUNDING_MODE;

/**
 * 거래비용 및 차입비용 계산을 담당하는 서비스 클래스
 * 매수/매도 수수료, 증권거래세, 차입 이자 등을 계산한다.
 */
@Slf4j
@Service
public class FeeCalculatorService {

    /**
     * 매수 거래의 총 비용을 계산한다
     * 매수 시에는 수수료만 부과되고 세금은 없다
     * 
     * @param quantity 매수 수량
     * @param price 매수 가격
     * @return 매수 총 비용 (거래금액 + 수수료)
     * @throws IllegalArgumentException 잘못된 매개변수
     */
    public double calculateBuyTotalCost(int quantity, double price) {
        validateTradeParameters(quantity, price);
        
        double tradeAmount = quantity * price;
        double fee = calculateBuyFee(tradeAmount);
        double totalCost = tradeAmount + fee;
        
        log.debug("매수 비용 계산 - 수량: {}, 가격: {:.0f}, 거래금액: {:.0f}, 수수료: {:.0f}, 총비용: {:.0f}", 
                quantity, price, tradeAmount, fee, totalCost);
        
        return roundAmount(totalCost);
    }

    /**
     * 매도 거래의 순수익을 계산한다
     * 매도 시에는 수수료와 증권거래세가 모두 부과된다
     * 
     * @param quantity 매도 수량
     * @param price 매도 가격
     * @return 매도 순수익 (거래금액 - 수수료 - 세금)
     * @throws IllegalArgumentException 잘못된 매개변수
     */
    public double calculateSellNetProceeds(int quantity, double price) {
        validateTradeParameters(quantity, price);
        
        double tradeAmount = quantity * price;
        double fee = calculateSellFee(tradeAmount);
        double tax = calculateSecuritiesTransactionTax(tradeAmount);
        double netProceeds = tradeAmount - fee - tax;
        
        log.debug("매도 수익 계산 - 수량: {}, 가격: {:.0f}, 거래금액: {:.0f}, 수수료: {:.0f}, 세금: {:.0f}, 순수익: {:.0f}", 
                quantity, price, tradeAmount, fee, tax, netProceeds);
        
        return roundAmount(netProceeds);
    }

    /**
     * 매수 수수료를 계산한다
     * 
     * @param tradeAmount 거래 금액
     * @return 매수 수수료
     */
    public double calculateBuyFee(double tradeAmount) {
        if (tradeAmount <= 0) {
            throw new IllegalArgumentException("거래 금액은 양수여야 합니다: " + tradeAmount);
        }
        
        double fee = tradeAmount * BUY_FEE_RATE;
        double finalFee = Math.max(fee, MINIMUM_FEE);
        
        return roundAmount(finalFee);
    }

    /**
     * 매도 수수료를 계산한다
     * 
     * @param tradeAmount 거래 금액
     * @return 매도 수수료
     */
    public double calculateSellFee(double tradeAmount) {
        if (tradeAmount <= 0) {
            throw new IllegalArgumentException("거래 금액은 양수여야 합니다: " + tradeAmount);
        }
        
        double fee = tradeAmount * SELL_FEE_RATE;
        double finalFee = Math.max(fee, MINIMUM_FEE);
        
        return roundAmount(finalFee);
    }

    /**
     * 증권거래세를 계산한다
     * 매도 시에만 부과되는 세금
     * 
     * @param tradeAmount 거래 금액
     * @return 증권거래세
     */
    public double calculateSecuritiesTransactionTax(double tradeAmount) {
        if (tradeAmount <= 0) {
            throw new IllegalArgumentException("거래 금액은 양수여야 합니다: " + tradeAmount);
        }
        
        double tax = tradeAmount * SECURITIES_TRANSACTION_TAX_RATE;
        return roundAmount(tax);
    }

    /**
     * 일일 차입 이자를 계산한다
     * 
     * @param borrowingAmount 차입 금액
     * @return 일일 차입 이자
     * @throws IllegalArgumentException 잘못된 차입 금액
     */
    public double calculateDailyBorrowingInterest(double borrowingAmount) {
        if (borrowingAmount < 0) {
            throw new IllegalArgumentException("차입 금액은 음수일 수 없습니다: " + borrowingAmount);
        }
        
        if (borrowingAmount == 0) {
            return 0.0; // 차입이 없으면 이자도 없음
        }
        
        double dailyInterest = borrowingAmount * DAILY_BORROWING_RATE;
        
        log.debug("일일 차입 이자 계산 - 차입금: {:.0f}, 이자율: {:.6f}, 일일이자: {:.0f}", 
                borrowingAmount, DAILY_BORROWING_RATE, dailyInterest);
        
        return roundAmount(dailyInterest);
    }

    /**
     * 연간 차입 이자를 계산한다
     * 특정 차입 금액을 1년간 유지할 경우의 총 이자
     * 
     * @param borrowingAmount 차입 금액
     * @return 연간 차입 이자
     */
    public double calculateAnnualBorrowingInterest(double borrowingAmount) {
        if (borrowingAmount < 0) {
            throw new IllegalArgumentException("차입 금액은 음수일 수 없습니다: " + borrowingAmount);
        }
        
        if (borrowingAmount == 0) {
            return 0.0;
        }
        
        double annualInterest = borrowingAmount * ANNUAL_BORROWING_RATE;
        return roundAmount(annualInterest);
    }

    /**
     * 차입 기간에 따른 총 이자를 계산한다
     * 
     * @param borrowingAmount 차입 금액
     * @param days 차입 기간 (일)
     * @return 총 차입 이자
     * @throws IllegalArgumentException 잘못된 매개변수
     */
    public double calculateTotalBorrowingInterest(double borrowingAmount, int days) {
        if (borrowingAmount < 0) {
            throw new IllegalArgumentException("차입 금액은 음수일 수 없습니다: " + borrowingAmount);
        }
        
        if (days < 0) {
            throw new IllegalArgumentException("차입 기간은 음수일 수 없습니다: " + days);
        }
        
        if (borrowingAmount == 0 || days == 0) {
            return 0.0;
        }
        
        double periodInterest = borrowingAmount * DAILY_BORROWING_RATE * days;
        
        log.debug("기간별 차입 이자 계산 - 차입금: {:.0f}, 기간: {}일, 이자율: {:.6f}, 총이자: {:.0f}", 
                borrowingAmount, days, DAILY_BORROWING_RATE, periodInterest);
        
        return roundAmount(periodInterest);
    }

    /**
     * 주간 차입 이자를 계산한다 (7일간)
     * 임계치 기반 리밸런싱에서 사용
     * 
     * @param borrowingAmount 차입 금액
     * @return 주간 차입 이자
     * @throws IllegalArgumentException 잘못된 차입 금액
     */
    public double calculateWeeklyBorrowingInterest(double borrowingAmount) {
        return calculateTotalBorrowingInterest(borrowingAmount, 7);
    }

    /**
     * 월간 차입 이자를 계산한다 (30일간)
     * 주기 기반 리밸런싱에서 사용
     * 
     * @param borrowingAmount 차입 금액
     * @return 월간 차입 이자
     * @throws IllegalArgumentException 잘못된 차입 금액
     */
    public double calculateMonthlyBorrowingInterest(double borrowingAmount) {
        return calculateTotalBorrowingInterest(borrowingAmount, 30);
    }

    /**
     * 리밸런싱에 필요한 총 거래비용을 추정한다
     * 매수와 매도 거래가 모두 발생한다고 가정하고 계산
     * 
     * @param totalTradeAmount 총 거래 예상 금액 (매수 + 매도)
     * @return 예상 총 거래비용
     */
    public double estimateRebalancingCost(double totalTradeAmount) {
        if (totalTradeAmount < 0) {
            throw new IllegalArgumentException("거래 금액은 음수일 수 없습니다: " + totalTradeAmount);
        }
        
        if (totalTradeAmount == 0) {
            return 0.0;
        }
        
        // 매수와 매도가 절반씩 발생한다고 가정
        double buyAmount = totalTradeAmount / 2.0;
        double sellAmount = totalTradeAmount / 2.0;
        
        double buyFee = calculateBuyFee(buyAmount);
        double sellFee = calculateSellFee(sellAmount);
        double tax = calculateSecuritiesTransactionTax(sellAmount);
        
        double totalCost = buyFee + sellFee + tax;
        
        log.debug("리밸런싱 비용 추정 - 총거래금액: {:.0f}, 매수비용: {:.0f}, 매도비용: {:.0f}, 세금: {:.0f}, 총비용: {:.0f}", 
                totalTradeAmount, buyFee, sellFee, tax, totalCost);
        
        return roundAmount(totalCost);
    }

    /**
     * 특정 거래의 수익률을 계산한다 (세후 기준)
     * 
     * @param buyPrice 매수 가격
     * @param sellPrice 매도 가격
     * @param quantity 거래 수량
     * @return 세후 수익률 (0.1 = 10%)
     * @throws IllegalArgumentException 잘못된 매개변수
     */
    public double calculateTradeReturn(double buyPrice, double sellPrice, int quantity) {
        validateTradeParameters(quantity, buyPrice);
        
        if (sellPrice <= 0) {
            throw new IllegalArgumentException("매도 가격은 양수여야 합니다: " + sellPrice);
        }
        
        // 매수 비용 (거래금액 + 수수료)
        double buyAmount = buyPrice * quantity;
        double buyCost = buyAmount + calculateBuyFee(buyAmount);
        
        // 매도 수익 (거래금액 - 수수료 - 세금)
        double sellAmount = sellPrice * quantity;
        double sellProceeds = calculateSellNetProceeds(quantity, sellPrice);
        
        // 수익률 계산
        double profit = sellProceeds - buyCost;
        double returnRate = profit / buyCost;
        
        log.debug("거래 수익률 계산 - 매수비용: {:.0f}, 매도수익: {:.0f}, 순이익: {:.0f}, 수익률: {:.2f}%", 
                buyCost, sellProceeds, profit, returnRate * 100);
        
        return returnRate;
    }

    /**
     * 현재 차입 상황에서 포트폴리오 가치 대비 차입 비율을 계산한다
     * 
     * @param borrowingAmount 현재 차입 금액
     * @param portfolioValue 포트폴리오 총 가치
     * @return 차입 비율 (0.1 = 10%)
     */
    public double calculateBorrowingRatio(double borrowingAmount, double portfolioValue) {
        if (borrowingAmount < 0) {
            throw new IllegalArgumentException("차입 금액은 음수일 수 없습니다: " + borrowingAmount);
        }
        
        if (portfolioValue <= 0) {
            return borrowingAmount > 0 ? Double.MAX_VALUE : 0.0;
        }
        
        return borrowingAmount / Math.abs(portfolioValue);
    }

    /**
     * 거래 매개변수의 유효성을 검증한다
     * 
     * @param quantity 거래 수량
     * @param price 거래 가격
     * @throws IllegalArgumentException 잘못된 매개변수
     */
    private void validateTradeParameters(int quantity, double price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("거래 수량은 양수여야 합니다: " + quantity);
        }
        
        if (price <= 0) {
            throw new IllegalArgumentException("거래 가격은 양수여야 합니다: " + price);
        }
    }

    /**
     * 금액을 지정된 반올림 모드로 처리한다
     * 
     * @param amount 반올림할 금액
     * @return 반올림된 금액
     */
    private double roundAmount(double amount) {
        return BigDecimal.valueOf(amount)
                .setScale(0, AMOUNT_ROUNDING_MODE)
                .doubleValue();
    }

    /**
     * 수수료율 정보를 반환한다
     * 외부에서 수수료 정보를 조회할 때 사용
     * 
     * @return 수수료율 정보 문자열
     */
    public String getFeeRateInfo() {
        return String.format("매수수수료: %.3f%%, 매도수수료: %.3f%%, 증권거래세: %.1f%%, 연차입이자: %.1f%%",
                BUY_FEE_RATE * 100, SELL_FEE_RATE * 100, 
                SECURITIES_TRANSACTION_TAX_RATE * 100, ANNUAL_BORROWING_RATE * 100);
    }

    /**
     * 차입 이자율 정보를 반환한다
     * 
     * @return 차입 이자율 정보 문자열
     */
    public String getBorrowingRateInfo() {
        return String.format("연이자율: %.2f%%, 일이자율: %.6f%%", 
                ANNUAL_BORROWING_RATE * 100, DAILY_BORROWING_RATE * 100);
    }
}