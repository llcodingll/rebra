package com.rebra.calculator.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

/**
 * FeeCalculatorService 단위 테스트
 * 거래비용 및 차입비용 계산 로직의 정확성을 검증한다.
 */
@SpringBootTest
class FeeCalculatorServiceTest {

    @Autowired
    private FeeCalculatorService feeCalculatorService;

    @Test
    @DisplayName("매수 수수료 계산 테스트")
    void calculateBuyFee() {
        // given
        double tradeAmount = 1000000.0; // 100만원
        
        // when
        double fee = feeCalculatorService.calculateBuyFee(tradeAmount);
        
        // then
        double expectedFee = tradeAmount * 0.00015; // 0.015%
        assertThat(fee).isEqualTo(expectedFee);
    }

    @Test
    @DisplayName("매도 수수료 계산 테스트")
    void calculateSellFee() {
        // given
        double tradeAmount = 1000000.0; // 100만원
        
        // when
        double fee = feeCalculatorService.calculateSellFee(tradeAmount);
        
        // then
        double expectedFee = tradeAmount * 0.00015; // 0.015%
        assertThat(fee).isEqualTo(expectedFee);
    }

    @Test
    @DisplayName("증권거래세 계산 테스트")
    void calculateSecuritiesTransactionTax() {
        // given
        double tradeAmount = 1000000.0; // 100만원
        
        // when
        double tax = feeCalculatorService.calculateSecuritiesTransactionTax(tradeAmount);
        
        // then
        double expectedTax = tradeAmount * 0.003; // 0.3%
        assertThat(tax).isEqualTo(expectedTax);
    }

    @Test
    @DisplayName("매수 총 비용 계산 테스트")
    void calculateBuyTotalCost() {
        // given
        int quantity = 100;
        double price = 10000.0;
        
        // when
        double totalCost = feeCalculatorService.calculateBuyTotalCost(quantity, price);
        
        // then
        double tradeAmount = quantity * price; // 100만원
        double expectedFee = tradeAmount * 0.00015; // 150원
        double expectedTotal = tradeAmount + expectedFee; // 1,000,150원
        assertThat(totalCost).isEqualTo(expectedTotal);
    }

    @Test
    @DisplayName("매도 순수익 계산 테스트")
    void calculateSellNetProceeds() {
        // given
        int quantity = 100;
        double price = 10000.0;
        
        // when
        double netProceeds = feeCalculatorService.calculateSellNetProceeds(quantity, price);
        
        // then
        double tradeAmount = quantity * price; // 100만원
        double expectedFee = tradeAmount * 0.00015; // 150원
        double expectedTax = tradeAmount * 0.003; // 3000원
        double expectedNet = tradeAmount - expectedFee - expectedTax; // 996,850원
        assertThat(netProceeds).isEqualTo(expectedNet);
    }

    @Test
    @DisplayName("일일 차입 이자 계산 테스트")
    void calculateDailyBorrowingInterest() {
        // given
        double borrowingAmount = 1000000.0; // 100만원 차입
        
        // when
        double dailyInterest = feeCalculatorService.calculateDailyBorrowingInterest(borrowingAmount);
        
        // then
        double expectedInterest = borrowingAmount * (0.045 / 365); // 연 4.5%의 일일 이자
        assertThat(dailyInterest).isCloseTo(expectedInterest, within(1.0)); // 반올림으로 인한 차이 허용
    }

    @Test
    @DisplayName("연간 차입 이자 계산 테스트")
    void calculateAnnualBorrowingInterest() {
        // given
        double borrowingAmount = 1000000.0; // 100만원 차입
        
        // when
        double annualInterest = feeCalculatorService.calculateAnnualBorrowingInterest(borrowingAmount);
        
        // then
        double expectedInterest = borrowingAmount * 0.045; // 45,000원
        assertThat(annualInterest).isEqualTo(expectedInterest);
    }

    @Test
    @DisplayName("기간별 총 차입 이자 계산 테스트")
    void calculateTotalBorrowingInterest() {
        // given
        double borrowingAmount = 1000000.0; // 100만원 차입
        int days = 30; // 30일
        
        // when
        double totalInterest = feeCalculatorService.calculateTotalBorrowingInterest(borrowingAmount, days);
        
        // then
        double expectedTotal = borrowingAmount * (0.045 / 365) * days;
        assertThat(totalInterest).isCloseTo(expectedTotal, within(10.0)); // 반올림으로 인한 차이 허용
    }

    @Test
    @DisplayName("주간 차입 이자 계산 테스트 (7일)")
    void calculateWeeklyBorrowingInterest() {
        // given
        double borrowingAmount = 1000000.0; // 100만원 차입
        
        // when
        double weeklyInterest = feeCalculatorService.calculateWeeklyBorrowingInterest(borrowingAmount);
        
        // then
        double expectedTotal = borrowingAmount * (0.045 / 365) * 7;
        assertThat(weeklyInterest).isCloseTo(expectedTotal, within(5.0)); // 반올림으로 인한 차이 허용
    }

    @Test
    @DisplayName("월간 차입 이자 계산 테스트 (30일)")
    void calculateMonthlyBorrowingInterest() {
        // given
        double borrowingAmount = 1000000.0; // 100만원 차입
        
        // when
        double monthlyInterest = feeCalculatorService.calculateMonthlyBorrowingInterest(borrowingAmount);
        
        // then
        double expectedTotal = borrowingAmount * (0.045 / 365) * 30;
        assertThat(monthlyInterest).isCloseTo(expectedTotal, within(10.0)); // 반올림으로 인한 차이 허용
    }

    @Test
    @DisplayName("거래 수익률 계산 테스트")
    void calculateTradeReturn() {
        // given
        double buyPrice = 10000.0;
        double sellPrice = 11000.0; // 10% 상승
        int quantity = 100;
        
        // when
        double returnRate = feeCalculatorService.calculateTradeReturn(buyPrice, sellPrice, quantity);
        
        // then
        // 매수비용 = 1,000,000 + 150(수수료) = 1,000,150
        // 매도수익 = 1,100,000 - 165(수수료) - 3,300(세금) = 1,096,535
        // 순이익 = 1,096,535 - 1,000,150 = 96,385
        // 수익률 = 96,385 / 1,000,150 ≈ 0.0964 (9.64%)
        assertThat(returnRate).isBetween(0.09, 0.10); // 약 9.6%
    }

    @Test
    @DisplayName("잘못된 입력값에 대한 예외 처리 테스트")
    void invalidInputsThrowException() {
        // 음수 거래량
        assertThatThrownBy(() -> feeCalculatorService.calculateBuyTotalCost(-1, 10000))
                .isInstanceOf(IllegalArgumentException.class);
        
        // 음수 가격
        assertThatThrownBy(() -> feeCalculatorService.calculateBuyTotalCost(100, -10000))
                .isInstanceOf(IllegalArgumentException.class);
        
        // 음수 차입 금액
        assertThatThrownBy(() -> feeCalculatorService.calculateDailyBorrowingInterest(-100000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("차입 비율 계산 테스트")
    void calculateBorrowingRatio() {
        // given
        double borrowingAmount = 500000.0; // 50만원 차입
        double portfolioValue = 2000000.0; // 200만원 포트폴리오
        
        // when
        double ratio = feeCalculatorService.calculateBorrowingRatio(borrowingAmount, portfolioValue);
        
        // then
        double expectedRatio = 0.25; // 25%
        assertThat(ratio).isEqualTo(expectedRatio);
    }

    @Test
    @DisplayName("수수료율 정보 반환 테스트")
    void getFeeRateInfo() {
        // when
        String feeInfo = feeCalculatorService.getFeeRateInfo();
        
        // then
        assertThat(feeInfo).isNotNull();
        assertThat(feeInfo).contains("0.015%"); // 매수/매도 수수료
        assertThat(feeInfo).contains("0.3%");   // 증권거래세
        assertThat(feeInfo).contains("4.5%");   // 연차입이자
    }

    @Test
    @DisplayName("차입 이자율 정보 반환 테스트")
    void getBorrowingRateInfo() {
        // when
        String borrowingInfo = feeCalculatorService.getBorrowingRateInfo();
        
        // then
        assertThat(borrowingInfo).isNotNull();
        assertThat(borrowingInfo).contains("4.50%"); // 연이자율
        assertThat(borrowingInfo).contains("일이자율"); // 일이자율 포함
    }
}