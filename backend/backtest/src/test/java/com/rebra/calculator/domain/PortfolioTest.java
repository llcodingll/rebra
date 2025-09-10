package com.rebra.calculator.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Portfolio 도메인 클래스 단위 테스트
 * 포트폴리오의 핵심 기능들을 검증한다.
 */
class PortfolioTest {

    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio();
        // 테스트용 초기 가치 설정
        portfolio.setInitialValue(10000000.0); // 1천만원 상당의 주식으로 시작
    }

    @Test
    @DisplayName("포트폴리오 초기 생성 테스트")
    void createPortfolio() {
        // then
        assertThat(portfolio.getCash()).isZero(); // 초기 현금은 0
        assertThat(portfolio.getInitialValue()).isEqualTo(10000000.0);
        assertThat(portfolio.getHoldingStockCodes()).isEmpty();
        assertThat(portfolio.getTotalTradingCost()).isZero();
        assertThat(portfolio.getTotalBorrowingCost()).isZero();
        assertThat(portfolio.isBorrowing()).isFalse();
    }

    @Test
    @DisplayName("주식 매수 테스트")
    void buyStock() {
        // given
        String stockCode = "005930";
        int quantity = 100;
        double price = 60000.0;
        double fee = 90.0; // 수수료
        LocalDate date = LocalDate.now();

        // when
        Trade trade = portfolio.buyStock(stockCode, quantity, price, fee, date);

        // then
        assertThat(trade.getTradeType()).isEqualTo(Trade.TradeType.BUY);
        assertThat(trade.getQuantity()).isEqualTo(quantity);
        assertThat(trade.getPrice()).isEqualTo(price);
        assertThat(portfolio.getHoldings(stockCode)).isEqualTo(quantity);
        
        // 초기 현금이 0이므로 매수 후 현금은 음수가 됨 (차입)
        double expectedCash = 0.0 - (quantity * price) - fee;
        assertThat(portfolio.getCash()).isEqualTo(expectedCash);
        assertThat(portfolio.getTotalTradingCost()).isEqualTo(fee);
    }

    @Test
    @DisplayName("주식 매도 테스트")
    void sellStock() {
        // given - 먼저 주식 매수
        String stockCode = "005930";
        int buyQuantity = 100;
        double buyPrice = 60000.0;
        double buyFee = 90.0;
        LocalDate date = LocalDate.now();
        
        portfolio.buyStock(stockCode, buyQuantity, buyPrice, buyFee, date);
        
        // 매도
        int sellQuantity = 50;
        double sellPrice = 65000.0;
        double sellFee = 48.75;
        double tax = 97.5;

        // when
        Trade trade = portfolio.sellStock(stockCode, sellQuantity, sellPrice, sellFee, tax, date);

        // then
        assertThat(trade.getTradeType()).isEqualTo(Trade.TradeType.SELL);
        assertThat(trade.getQuantity()).isEqualTo(sellQuantity);
        assertThat(portfolio.getHoldings(stockCode)).isEqualTo(buyQuantity - sellQuantity);
        
        double sellAmount = sellQuantity * sellPrice;
        double netProceeds = sellAmount - sellFee - tax;
        double expectedCash = 0.0 - (buyQuantity * buyPrice) - buyFee + netProceeds;
        
        assertThat(portfolio.getCash()).isCloseTo(expectedCash, within(1.0)); // 반올림으로 인한 차이 허용
        assertThat(portfolio.getTotalTradingCost()).isCloseTo(buyFee + sellFee + tax, within(1.0)); // 반올림으로 인한 차이 허용
    }

    @Test
    @DisplayName("포트폴리오 총 가치 계산 테스트")
    void getTotalValue() {
        // given
        String stockCode1 = "005930";
        String stockCode2 = "000660";
        
        // 주식 매수
        portfolio.buyStock(stockCode1, 100, 60000.0, 90.0, LocalDate.now());
        portfolio.buyStock(stockCode2, 50, 80000.0, 60.0, LocalDate.now());
        
        // 현재 가격 정보
        Map<String, Double> currentPrices = Map.of(
            stockCode1, 65000.0,  // 5천원 상승
            stockCode2, 85000.0   // 5천원 상승
        );

        // when
        double totalValue = portfolio.getTotalValue(currentPrices);

        // then
        double stock1Value = 100 * 65000.0; // 650만원
        double stock2Value = 50 * 85000.0;  // 425만원
        double expectedTotal = portfolio.getCash() + stock1Value + stock2Value;
        
        assertThat(totalValue).isEqualTo(expectedTotal);
    }

    @Test
    @DisplayName("현재 비중 계산 테스트")
    void getCurrentWeights() {
        // given
        String stockCode = "005930";
        // 초기 보유 주식으로 설정 (거래 없이)
        portfolio.setInitialHolding(stockCode, 100);
        
        Map<String, Double> currentPrices = Map.of(stockCode, 60000.0);

        // when
        Map<String, Double> weights = portfolio.getCurrentWeights(currentPrices);

        // then
        double totalValue = portfolio.getTotalValue(currentPrices);
        double stockValue = 100 * 60000.0;
        double expectedWeight = stockValue / totalValue;
        
        assertThat(weights.get(stockCode)).isCloseTo(expectedWeight, within(0.001));
    }

    @Test
    @DisplayName("차입 이자 계산 및 차감 테스트")
    void calculateAndDeductBorrowingCost() {
        // given - 현금을 음수로 만들기 (차입 상황)
        String stockCode = "005930";
        LocalDate date = LocalDate.now();
        
        // 초기 현금이 0이므로 매수하면 바로 차입 발생
        portfolio.buyStock(stockCode, 200, 60000.0, 180.0, date);
        
        double initialCash = portfolio.getCash();
        assertThat(initialCash).isNegative(); // 차입 상태 확인

        // when - 1일 경과 가정
        double periodInterest = portfolio.calculateAndDeductBorrowingCost(date, 1);

        // then
        assertThat(periodInterest).isPositive();
        assertThat(portfolio.getCash()).isLessThan(initialCash); // 이자만큼 더 차감
        assertThat(portfolio.getTotalBorrowingCost()).isEqualTo(periodInterest);
        assertThat(portfolio.isBorrowing()).isTrue();
        assertThat(portfolio.getCurrentBorrowingAmount()).isPositive();
    }

    @Test
    @DisplayName("매도 시 보유 수량 부족 예외 처리 테스트")
    void sellMoreThanHolding() {
        // given
        String stockCode = "005930";
        LocalDate date = LocalDate.now();
        
        portfolio.buyStock(stockCode, 50, 60000.0, 45.0, date);

        // when & then
        assertThatThrownBy(() -> 
            portfolio.sellStock(stockCode, 100, 60000.0, 90.0, 180.0, date)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("보유 수량이 부족합니다");
    }

    @Test
    @DisplayName("초기 가치 설정 및 누적 수익률 계산 테스트")
    void initialValueAndCumulativeReturn() {
        // given
        String stockCode = "005930";
        LocalDate date = LocalDate.now();
        
        // 초기 보유 주식 설정 (테스트용)
        portfolio.setInitialHolding(stockCode, 100);
        
        // 현재 가격 정보
        Map<String, Double> currentPrices = Map.of(stockCode, 65000.0);
        
        // when
        double cumulativeReturn = portfolio.getCumulativeReturn(currentPrices);
        
        // then
        assertThat(portfolio.getInitialValue()).isEqualTo(10000000.0);
        assertThat(cumulativeReturn).isNotNull();
        
        // 잘못된 초기 가치 설정 시 예외 발생
        Portfolio newPortfolio = new Portfolio();
        assertThatThrownBy(() -> newPortfolio.setInitialValue(-1000000.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("초기 포트폴리오 가치는 양수여야 합니다");
    }

    @Test
    @DisplayName("포트폴리오 복사 테스트")
    void copyPortfolio() {
        // given
        String stockCode = "005930";
        LocalDate date = LocalDate.now();
        
        portfolio.buyStock(stockCode, 100, 60000.0, 90.0, date);
        portfolio.calculateAndDeductBorrowingCost(date, 1); // 차입 기록 생성 (1일 경과)

        // when
        Portfolio copied = portfolio.copy();

        // then
        assertThat(copied.getCash()).isEqualTo(portfolio.getCash());
        assertThat(copied.getHoldings(stockCode)).isEqualTo(portfolio.getHoldings(stockCode));
        assertThat(copied.getTotalTradingCost()).isEqualTo(portfolio.getTotalTradingCost());
        assertThat(copied.getTotalBorrowingCost()).isEqualTo(portfolio.getTotalBorrowingCost());
        assertThat(copied.getInitialValue()).isEqualTo(portfolio.getInitialValue());
        
        // 복사본 수정이 원본에 영향을 주지 않는지 확인
        copied.buyStock("000660", 10, 80000.0, 12.0, date);
        assertThat(portfolio.getHoldings("000660")).isZero();
    }

    @Test
    @DisplayName("통계 정보 업데이트 테스트")
    void updateStatistics() {
        // given
        String stockCode = "005930";
        LocalDate date = LocalDate.now();

        // when - 여러 거래 실행
        portfolio.buyStock(stockCode, 100, 60000.0, 90.0, date);
        portfolio.sellStock(stockCode, 50, 65000.0, 48.75, 97.5, date);
        
        // 차입 발생을 위해 큰 금액 매수 (초기 현금 0이므로 바로 차입)
        portfolio.buyStock(stockCode, 200, 100000.0, 300.0, date);
        portfolio.calculateAndDeductBorrowingCost(date, 7); // 7일 경과 가정

        // then
        assertThat(portfolio.getMaxBorrowingAmount()).isPositive();
        assertThat(portfolio.getMinCashBalance()).isNegative();
        assertThat(portfolio.getTotalTradingCost()).isPositive();
        assertThat(portfolio.getTotalBorrowingCost()).isPositive();
    }
}