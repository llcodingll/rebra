package com.rebra.calculator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Trade 도메인 클래스 단위 테스트
 * 거래 정보의 생성, 검증, 계산 로직을 테스트한다.
 */
class TradeTest {

    @Nested
    @DisplayName("Trade 객체 생성 테스트")
    class TradeCreationTest {

        @Test
        @DisplayName("매수 거래 생성")
        void createBuyTrade() {
            // given
            String stockCode = "005930";
            int quantity = 100;
            double price = 50000.0;
            double fee = 75.0;
            LocalDate tradeDate = LocalDate.of(2023, 1, 15);

            // when
            Trade buyTrade = new Trade(tradeDate, stockCode, Trade.TradeType.BUY, quantity, price, fee, 0.0);

            // then
            assertThat(buyTrade.getStockCode()).isEqualTo(stockCode);
            assertThat(buyTrade.getTradeType()).isEqualTo(Trade.TradeType.BUY);
            assertThat(buyTrade.getQuantity()).isEqualTo(quantity);
            assertThat(buyTrade.getPrice()).isEqualTo(price);
            assertThat(buyTrade.getFee()).isEqualTo(fee);
            assertThat(buyTrade.getTax()).isZero(); // 매수에는 세금 없음
            assertThat(buyTrade.getTradeDate()).isEqualTo(tradeDate);
            assertThat(buyTrade.getAmount()).isEqualTo(quantity * price);
            assertThat(buyTrade.isBuy()).isTrue();
            assertThat(buyTrade.isSell()).isFalse();
        }

        @Test
        @DisplayName("매도 거래 생성")
        void createSellTrade() {
            // given
            String stockCode = "005930";
            int quantity = 50;
            double price = 52000.0;
            double fee = 39.0;
            double tax = 78.0;
            LocalDate tradeDate = LocalDate.of(2023, 1, 20);

            // when
            Trade sellTrade = new Trade(tradeDate, stockCode, Trade.TradeType.SELL, quantity, price, fee, tax);

            // then
            assertThat(sellTrade.getStockCode()).isEqualTo(stockCode);
            assertThat(sellTrade.getTradeType()).isEqualTo(Trade.TradeType.SELL);
            assertThat(sellTrade.getQuantity()).isEqualTo(quantity);
            assertThat(sellTrade.getPrice()).isEqualTo(price);
            assertThat(sellTrade.getFee()).isEqualTo(fee);
            assertThat(sellTrade.getTax()).isEqualTo(tax);
            assertThat(sellTrade.getTradeDate()).isEqualTo(tradeDate);
            assertThat(sellTrade.getAmount()).isEqualTo(quantity * price);
            assertThat(sellTrade.isBuy()).isFalse();
            assertThat(sellTrade.isSell()).isTrue();
        }

        @Test
        @DisplayName("null 종목코드로 거래 생성 시 예외")
        void createTradeWithNullStockCode() {
            // when & then
            assertThatThrownBy(() -> 
                new Trade(LocalDate.now(), null, Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종목 코드는 필수입니다");
        }

        @Test
        @DisplayName("빈 종목코드로 거래 생성 시 예외")
        void createTradeWithEmptyStockCode() {
            // when & then
            assertThatThrownBy(() -> 
                new Trade(LocalDate.now(), "", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종목 코드는 필수입니다");
        }

        @Test
        @DisplayName("0 이하 수량으로 거래 생성 시 예외")
        void createTradeWithZeroOrNegativeQuantity() {
            // when & then
            assertThatThrownBy(() -> 
                new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 0, 50000.0, 75.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("거래 수량은 양수여야 합니다");

            assertThatThrownBy(() -> 
                new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, -10, 50000.0, 75.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("거래 수량은 양수여야 합니다");
        }

        @Test
        @DisplayName("0 이하 가격으로 거래 생성 시 예외")
        void createTradeWithZeroOrNegativePrice() {
            // when & then
            assertThatThrownBy(() -> 
                new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 100, 0.0, 75.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("거래 가격은 양수여야 합니다");

            assertThatThrownBy(() -> 
                new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 100, -1000.0, 75.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("거래 가격은 양수여야 합니다");
        }

        @Test
        @DisplayName("음수 수수료로 거래 생성 시 예외")
        void createTradeWithNegativeFee() {
            // when & then
            assertThatThrownBy(() -> 
                new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 100, 50000.0, -10.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("거래 수수료는 음수일 수 없습니다");
        }

        @Test
        @DisplayName("음수 세금으로 매도 거래 생성 시 예외")
        void createSellTradeWithNegativeTax() {
            // when & then
            assertThatThrownBy(() -> 
                new Trade(LocalDate.now(), "005930", Trade.TradeType.SELL, 100, 50000.0, 75.0, -5.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("증권거래세는 음수일 수 없습니다");
        }

        @Test
        @DisplayName("null 거래일로 거래 생성 시 예외")
        void createTradeWithNullTradeDate() {
            // when & then
            assertThatThrownBy(() -> 
                new Trade(null, "005930", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("거래 날짜는 필수입니다");
        }
    }

    @Nested
    @DisplayName("거래 금액 계산 테스트")
    class TradeAmountCalculationTest {

        @Test
        @DisplayName("매수 거래 금액 계산")
        void calculateBuyTradeAmount() {
            // given
            int quantity = 100;
            double price = 50000.0;
            Trade buyTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, quantity, price, 75.0, 0.0);

            // when
            double amount = buyTrade.getAmount();

            // then
            assertThat(amount).isEqualTo(5000000.0); // 100 * 50,000
        }

        @Test
        @DisplayName("매도 거래 금액 계산")
        void calculateSellTradeAmount() {
            // given
            int quantity = 50;
            double price = 52000.0;
            Trade sellTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.SELL, quantity, price, 39.0, 78.0);

            // when
            double amount = sellTrade.getAmount();

            // then
            assertThat(amount).isEqualTo(2600000.0); // 50 * 52,000
        }

        @Test
        @DisplayName("소수점 가격 거래 금액 계산")
        void calculateTradeAmountWithDecimalPrice() {
            // given
            int quantity = 100;
            double price = 1234.56;
            Trade trade = new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, quantity, price, 1.85, 0.0);

            // when
            double amount = trade.getAmount();

            // then
            assertThat(amount).isEqualTo(123456.0); // 100 * 1234.56
        }

        @Test
        @DisplayName("대량 거래 금액 계산")
        void calculateLargeTradeAmount() {
            // given
            int quantity = 10000;
            double price = 100000.0;
            Trade trade = new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, quantity, price, 1500.0, 0.0);

            // when
            double amount = trade.getAmount();

            // then
            assertThat(amount).isEqualTo(1000000000.0); // 10억원
        }
    }

    @Nested
    @DisplayName("총 거래비용 계산 테스트")
    class TotalTradingCostTest {

        @Test
        @DisplayName("매수 거래 총 비용 계산")
        void calculateBuyTradeTotalCost() {
            // given
            Trade buyTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0);

            // when
            double totalCost = buyTrade.getTotalCost();

            // then
            assertThat(totalCost).isEqualTo(75.0); // 수수료만
        }

        @Test
        @DisplayName("매도 거래 총 비용 계산")
        void calculateSellTradeTotalCost() {
            // given
            Trade sellTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.SELL, 50, 52000.0, 39.0, 78.0);

            // when
            double totalCost = sellTrade.getTotalCost();

            // then
            assertThat(totalCost).isEqualTo(117.0); // 수수료 + 세금
        }

        @Test
        @DisplayName("수수료 0인 경우 총 비용")
        void calculateTotalCostWithZeroFee() {
            // given
            Trade trade = new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 100, 50000.0, 0.0, 0.0);

            // when
            double totalCost = trade.getTotalCost();

            // then
            assertThat(totalCost).isZero();
        }
    }

    @Nested
    @DisplayName("순 거래 금액 계산 테스트")
    class NetTradeAmountTest {

        @Test
        @DisplayName("매수 거래 순 금액 (지출)")
        void calculateBuyTradeNetAmount() {
            // given
            Trade buyTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0);

            // when
            double netAmount = buyTrade.getNetAmount();

            // then
            assertThat(netAmount).isEqualTo(-5000075.0); // -(거래금액 + 수수료)
        }

        @Test
        @DisplayName("매도 거래 순 금액 (수입)")
        void calculateSellTradeNetAmount() {
            // given
            Trade sellTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.SELL, 50, 52000.0, 39.0, 78.0);

            // when
            double netAmount = sellTrade.getNetAmount();

            // then
            assertThat(netAmount).isEqualTo(2599883.0); // 거래금액 - 수수료 - 세금
        }

        @Test
        @DisplayName("비용이 없는 거래의 순 금액")
        void calculateNetAmountWithNoCost() {
            // given
            Trade buyTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 100, 50000.0, 0.0, 0.0);
            Trade sellTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.SELL, 50, 52000.0, 0.0, 0.0);

            // when & then
            assertThat(buyTrade.getNetAmount()).isEqualTo(-5000000.0);
            assertThat(sellTrade.getNetAmount()).isEqualTo(2600000.0);
        }
    }

    @Nested
    @DisplayName("거래 유형 판별 테스트")
    class TradeTypeIdentificationTest {

        @Test
        @DisplayName("매수 거래 유형 확인")
        void identifyBuyTradeType() {
            // given
            Trade buyTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0);

            // when & then
            assertThat(buyTrade.isBuy()).isTrue();
            assertThat(buyTrade.isSell()).isFalse();
            assertThat(buyTrade.getTradeType()).isEqualTo(Trade.TradeType.BUY);
        }

        @Test
        @DisplayName("매도 거래 유형 확인")
        void identifySellTradeType() {
            // given
            Trade sellTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.SELL, 50, 52000.0, 39.0, 78.0);

            // when & then
            assertThat(sellTrade.isBuy()).isFalse();
            assertThat(sellTrade.isSell()).isTrue();
            assertThat(sellTrade.getTradeType()).isEqualTo(Trade.TradeType.SELL);
        }
    }

    @Nested
    @DisplayName("거래 비교 및 정렬 테스트")
    class TradeComparisonTest {

        @Test
        @DisplayName("거래 날짜순 정렬")
        void sortTradesByDate() {
            // given
            Trade trade1 = new Trade(LocalDate.of(2023, 1, 15), "005930", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0);
            Trade trade2 = new Trade(LocalDate.of(2023, 1, 10), "005930", Trade.TradeType.SELL, 50, 52000.0, 39.0, 78.0);
            Trade trade3 = new Trade(LocalDate.of(2023, 1, 20), "000660", Trade.TradeType.BUY, 30, 80000.0, 36.0, 0.0);

            // when
            java.util.List<Trade> trades = java.util.Arrays.asList(trade1, trade2, trade3);
            trades.sort(java.util.Comparator.comparing(Trade::getTradeDate));

            // then
            assertThat(trades.get(0)).isEqualTo(trade2); // 1월 10일
            assertThat(trades.get(1)).isEqualTo(trade1); // 1월 15일
            assertThat(trades.get(2)).isEqualTo(trade3); // 1월 20일
        }

        @Test
        @DisplayName("거래 금액순 정렬")
        void sortTradesByAmount() {
            // given
            Trade smallTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 10, 50000.0, 7.5, 0.0);     // 50만원
            Trade mediumTrade = new Trade(LocalDate.now(), "000660", Trade.TradeType.BUY, 20, 80000.0, 24.0, 0.0);   // 160만원
            Trade largeTrade = new Trade(LocalDate.now(), "035420", Trade.TradeType.BUY, 15, 200000.0, 45.0, 0.0);   // 300만원

            // when
            java.util.List<Trade> trades = java.util.Arrays.asList(largeTrade, smallTrade, mediumTrade);
            trades.sort(java.util.Comparator.comparing(Trade::getAmount));

            // then
            assertThat(trades.get(0)).isEqualTo(smallTrade);
            assertThat(trades.get(1)).isEqualTo(mediumTrade);
            assertThat(trades.get(2)).isEqualTo(largeTrade);
        }

        @Test
        @DisplayName("Trade 객체 equals 및 hashCode")
        void tradeEqualsAndHashCode() {
            // given
            LocalDate tradeDate = LocalDate.of(2023, 1, 15);
            Trade trade1 = new Trade(tradeDate, "005930", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0);
            Trade trade2 = new Trade(tradeDate, "005930", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0);
            Trade trade3 = new Trade(tradeDate, "000660", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0);

            // when & then
            assertThat(trade1).isEqualTo(trade2);
            assertThat(trade1.hashCode()).isEqualTo(trade2.hashCode());
            assertThat(trade1).isNotEqualTo(trade3);
        }
    }

    @Nested
    @DisplayName("거래 정보 문자열 표현 테스트")
    class TradeStringRepresentationTest {

        @Test
        @DisplayName("매수 거래 toString")
        void buyTradeToString() {
            // given
            Trade buyTrade = new Trade(LocalDate.of(2023, 1, 15), "005930", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0);

            // when
            String tradeString = buyTrade.toString();

            // then
            assertThat(tradeString).contains("BUY");
            assertThat(tradeString).contains("005930");
            assertThat(tradeString).contains("100");
            assertThat(tradeString).contains("50000");
            assertThat(tradeString).contains("2023-01-15");
        }

        @Test
        @DisplayName("매도 거래 toString")
        void sellTradeToString() {
            // given
            Trade sellTrade = new Trade(LocalDate.of(2023, 1, 20), "005930", Trade.TradeType.SELL, 50, 52000.0, 39.0, 78.0);

            // when
            String tradeString = sellTrade.toString();

            // then
            assertThat(tradeString).contains("SELL");
            assertThat(tradeString).contains("005930");
            assertThat(tradeString).contains("50");
            assertThat(tradeString).contains("52000");
            assertThat(tradeString).contains("2023-01-20");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class RealUsageScenarioTest {

        @Test
        @DisplayName("리밸런싱 거래 시나리오")
        void rebalancingTradeScenario() {
            // given - 포트폴리오 리밸런싱으로 인한 거래들
            LocalDate rebalancingDate = LocalDate.of(2023, 1, 31);
            
            // 삼성전자 일부 매도 (비중 감소)
            Trade samsungSell = new Trade(rebalancingDate, "005930", Trade.TradeType.SELL, 30, 55000.0, 24.75, 49.5);
            
            // SK하이닉스 매수 (비중 증가)
            Trade skhynixBuy = new Trade(rebalancingDate, "000660", Trade.TradeType.BUY, 20, 85000.0, 25.5, 0.0);

            // when - 거래들의 순 현금 흐름 계산
            double netCashFlow = samsungSell.getNetAmount() + skhynixBuy.getNetAmount();

            // then
            assertThat(samsungSell.isSell()).isTrue();
            assertThat(skhynixBuy.isBuy()).isTrue();
            assertThat(samsungSell.getAmount()).isEqualTo(1650000.0); // 30 * 55,000
            assertThat(skhynixBuy.getAmount()).isEqualTo(1700000.0);  // 20 * 85,000
            
            // 매도로 현금 유입, 매수로 현금 유출
            assertThat(samsungSell.getNetAmount()).isPositive();
            assertThat(skhynixBuy.getNetAmount()).isNegative();
            assertThat(netCashFlow).isNegative(); // 전체적으로 현금 유출
        }

        @Test
        @DisplayName("거래비용 분석 시나리오")
        void tradingCostAnalysisScenario() {
            // given - 다양한 규모의 거래들
            Trade smallTrade = new Trade(LocalDate.now(), "005930", Trade.TradeType.BUY, 10, 50000.0, 7.5, 0.0);      // 50만원 거래
            Trade mediumTrade = new Trade(LocalDate.now(), "000660", Trade.TradeType.SELL, 50, 80000.0, 60.0, 120.0); // 400만원 거래
            Trade largeTrade = new Trade(LocalDate.now(), "035420", Trade.TradeType.BUY, 100, 200000.0, 300.0, 0.0);  // 2천만원 거래

            // when - 거래비용률 계산
            double smallTradeCostRate = smallTrade.getTotalCost() / smallTrade.getAmount();
            double mediumTradeCostRate = mediumTrade.getTotalCost() / mediumTrade.getAmount();
            double largeTradeCostRate = largeTrade.getTotalCost() / largeTrade.getAmount();

            // then - 거래 규모가 클수록 비용률이 낮아야 함
            assertThat(smallTradeCostRate).isGreaterThan(mediumTradeCostRate);
            assertThat(largeTradeCostRate).isLessThan(0.002); // 0.2% 미만
            
            // 모든 거래비용이 합리적인 범위 내
            assertThat(smallTradeCostRate).isLessThan(0.01);  // 1% 미만
            assertThat(mediumTradeCostRate).isLessThan(0.01); // 1% 미만
            assertThat(largeTradeCostRate).isLessThan(0.01);  // 1% 미만
        }

        @Test
        @DisplayName("월별 거래 내역 집계 시나리오")
        void monthlyTradingSummaryScenario() {
            // given - 1월 한 달간의 거래들
            java.util.List<Trade> januaryTrades = java.util.Arrays.asList(
                new Trade(LocalDate.of(2023, 1, 3), "005930", Trade.TradeType.BUY, 100, 50000.0, 75.0, 0.0),
                new Trade(LocalDate.of(2023, 1, 15), "005930", Trade.TradeType.SELL, 20, 52000.0, 15.6, 31.2),
                new Trade(LocalDate.of(2023, 1, 20), "000660", Trade.TradeType.BUY, 30, 80000.0, 36.0, 0.0),
                new Trade(LocalDate.of(2023, 1, 31), "000660", Trade.TradeType.SELL, 10, 82000.0, 12.3, 24.6)
            );

            // when - 월별 집계 계산
            double totalBuyAmount = januaryTrades.stream()
                .filter(Trade::isBuy)
                .mapToDouble(Trade::getAmount)
                .sum();
                
            double totalSellAmount = januaryTrades.stream()
                .filter(Trade::isSell)
                .mapToDouble(Trade::getAmount)
                .sum();
                
            double totalFees = januaryTrades.stream()
                .mapToDouble(Trade::getFee)
                .sum();
                
            double totalTax = januaryTrades.stream()
                .mapToDouble(Trade::getTax)
                .sum();

            // then
            assertThat(totalBuyAmount).isEqualTo(7400000.0); // 500만 + 240만
            assertThat(totalSellAmount).isEqualTo(1860000.0); // 104만 + 82만
            assertThat(totalFees).isEqualTo(138.9); // 75 + 15.6 + 36 + 12.3
            assertThat(totalTax).isEqualTo(55.8);   // 31.2 + 24.6
        }
    }
}