package com.rebra.calculator.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

import static com.rebra.calculator.constant.BacktestConstants.TradingRules.AMOUNT_ROUNDING_MODE;

/**
 * 개별 거래 정보를 나타내는 도메인 클래스
 * 백테스트 과정에서 발생하는 매매 거래의 상세 정보를 저장한다.
 */
@Getter
@NoArgsConstructor
@ToString
public class Trade {
    
    /**
     * 거래 유형을 나타내는 열거형
     */
    public enum TradeType {
        BUY("매수", 1),
        SELL("매도", -1);
        
        private final String displayName;
        private final int multiplier; // 수량 계산에 사용할 배수
        
        TradeType(String displayName, int multiplier) {
            this.displayName = displayName;
            this.multiplier = multiplier;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getMultiplier() {
            return multiplier;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    /**
     * 거래 날짜
     */
    private LocalDate tradeDate;
    
    /**
     * 종목 코드
     */
    private String stockCode;
    
    
    /**
     * 거래 유형 (매수/매도)
     */
    private TradeType tradeType;
    
    /**
     * 거래 수량 (주)
     * 매수: 양수, 매도: 음수로 저장하지 않고, tradeType으로 구분
     */
    private int quantity;
    
    /**
     * 거래 당시 주가 (원)
     */
    private double price;
    
    /**
     * 거래 금액 (원)
     * quantity * price (수수료 제외)
     */
    private double amount;
    
    /**
     * 거래 수수료 (원)
     */
    private double fee;
    
    /**
     * 증권거래세 (원)
     * 매도 시에만 발생
     */
    private double tax;
    
    /**
     * 총 거래비용 (원)
     * fee + tax
     */
    private double totalCost;
    
    /**
     * 실제 거래 금액 (원)
     * 매수: amount + totalCost (현금 차감액)
     * 매도: amount - totalCost (현금 증가액)
     */
    private double netAmount;

    /**
     * Trade 생성자
     * 
     * @param tradeDate 거래 날짜
     * @param stockCode 종목 코드
     * @param tradeType 거래 유형
     * @param quantity 거래 수량 (양수)
     * @param price 거래 가격
     * @param fee 거래 수수료
     * @param tax 증권거래세 (매도 시에만)
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     */
    public Trade(LocalDate tradeDate, String stockCode, TradeType tradeType,
                 int quantity, double price, double fee, double tax) {
        validateParameters(tradeDate, stockCode, tradeType, quantity, price, fee, tax);
        
        this.tradeDate = tradeDate;
        this.stockCode = stockCode.trim().toUpperCase();
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.price = roundAmount(price);
        this.amount = roundAmount(quantity * price);
        this.fee = roundAmount(fee);
        this.tax = roundAmount(tax);
        this.totalCost = roundAmount(fee + tax);
        this.netAmount = calculateNetAmount();
    }

    /**
     * 매개변수 유효성 검증
     */
    private void validateParameters(LocalDate tradeDate, String stockCode, 
                                  TradeType tradeType, int quantity, double price, double fee, double tax) {
        if (tradeDate == null) {
            throw new IllegalArgumentException("거래 날짜는 필수입니다.");
        }
        
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다.");
        }
        
        if (tradeType == null) {
            throw new IllegalArgumentException("거래 유형은 필수입니다.");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("거래 수량은 양수여야 합니다: " + quantity);
        }
        
        if (price <= 0) {
            throw new IllegalArgumentException("거래 가격은 양수여야 합니다: " + price);
        }
        
        if (fee < 0) {
            throw new IllegalArgumentException("거래 수수료는 음수일 수 없습니다: " + fee);
        }
        
        if (tax < 0) {
            throw new IllegalArgumentException("증권거래세는 음수일 수 없습니다: " + tax);
        }
        
        // 매수일 때는 세금이 0이어야 함
        if (tradeType == TradeType.BUY && tax > 0) {
            throw new IllegalArgumentException("매수 거래에는 증권거래세가 부과되지 않습니다.");
        }
    }

    /**
     * 금액을 지정된 반올림 모드로 처리
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
     * 실제 거래 금액 계산
     * 매수: amount + totalCost (현금에서 차감되는 금액)
     * 매도: amount - totalCost (현금에 추가되는 금액)
     * 
     * @return 실제 거래 금액
     */
    private double calculateNetAmount() {
        if (tradeType == TradeType.BUY) {
            return roundAmount(amount + totalCost);
        } else {
            return roundAmount(amount - totalCost);
        }
    }

    /**
     * 거래가 매수인지 확인
     * 
     * @return 매수 거래면 true
     */
    public boolean isBuy() {
        return tradeType == TradeType.BUY;
    }

    /**
     * 거래가 매도인지 확인
     * 
     * @return 매도 거래면 true
     */
    public boolean isSell() {
        return tradeType == TradeType.SELL;
    }

    /**
     * 주식 수량 변화량 반환
     * 매수: +quantity, 매도: -quantity
     * 
     * @return 주식 수량 변화량
     */
    public int getSharesChange() {
        return quantity * tradeType.getMultiplier();
    }

    /**
     * 현금 변화량 반환
     * 매수: -netAmount (현금 차감), 매도: +netAmount (현금 증가)
     * 
     * @return 현금 변화량
     */
    public double getCashChange() {
        if (tradeType == TradeType.BUY) {
            return -netAmount;
        } else {
            return netAmount;
        }
    }

    /**
     * 거래의 수익률 계산 (매도 시에만 의미있음)
     * 매수가와 매도가를 비교하여 수익률 계산
     * 
     * @param buyPrice 매수 당시 가격
     * @return 수익률 (0.1 = 10%)
     * @throws IllegalArgumentException 매수 거래이거나 매수가가 잘못된 경우
     */
    public double calculateReturn(double buyPrice) {
        if (tradeType == TradeType.BUY) {
            throw new IllegalArgumentException("매수 거래에서는 수익률을 계산할 수 없습니다.");
        }
        
        if (buyPrice <= 0) {
            throw new IllegalArgumentException("매수 가격이 유효하지 않습니다: " + buyPrice);
        }
        
        // 세후 수익률 계산
        double grossProfit = (price - buyPrice) * quantity;
        double netProfit = grossProfit - totalCost;
        double investmentAmount = buyPrice * quantity;
        
        return netProfit / investmentAmount;
    }

    /**
     * 거래 수수료율 계산
     * 
     * @return 수수료율 (0.0015 = 0.15%)
     */
    public double getFeeRate() {
        return amount > 0 ? fee / amount : 0.0;
    }

    /**
     * 증권거래세율 계산
     * 
     * @return 세율 (0.003 = 0.3%)
     */
    public double getTaxRate() {
        return amount > 0 ? tax / amount : 0.0;
    }

    /**
     * 총 거래비용율 계산
     * 
     * @return 총 거래비용율 (수수료율 + 세율)
     */
    public double getTotalCostRate() {
        return getFeeRate() + getTaxRate();
    }

    /**
     * 거래의 복사본 생성
     * 
     * @return Trade 객체의 복사본
     */
    public Trade copy() {
        return new Trade(this.tradeDate, this.stockCode, this.tradeType,
                this.quantity, this.price, this.fee, this.tax);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Trade trade = (Trade) obj;
        return quantity == trade.quantity &&
                Double.compare(trade.price, price) == 0 &&
                Objects.equals(tradeDate, trade.tradeDate) &&
                Objects.equals(stockCode, trade.stockCode) &&
                tradeType == trade.tradeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeDate, stockCode, tradeType, quantity, price);
    }

    /**
     * 상세 거래 정보를 포함한 문자열 표현
     * 
     * @return 상세 거래 정보 문자열
     */
    public String toDetailedString() {
        return String.format("Trade{date=%s, %s %s %d주 @%.0f원, 금액=%.0f원, 비용=%.0f원, 실거래=%.0f원}",
                tradeDate, tradeType.getDisplayName(), stockCode, quantity, price, 
                amount, totalCost, netAmount);
    }

    /**
     * 간단한 거래 요약 문자열
     * 
     * @return 간단한 거래 요약
     */
    public String toSummaryString() {
        return String.format("%s %s %d주 %.0f원", 
                tradeType.getDisplayName(), stockCode, quantity, netAmount);
    }
}