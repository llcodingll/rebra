package com.rebra.calculator.service;

import com.rebra.calculator.context.BacktestContext;
import com.rebra.calculator.domain.Portfolio;
import com.rebra.calculator.domain.Stock;
import com.rebra.calculator.domain.Trade;
import com.rebra.calculator.enums.RebalancingPeriod;
import com.rebra.calculator.enums.RebalancingType;
import com.rebra.calculator.strategy.ThresholdRebalancingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PortfolioManagerService 단위 테스트
 * 포트폴리오 관리 로직의 정확성을 검증한다.
 */
@SpringBootTest
class PortfolioManagerServiceTest {

    @Autowired
    private PortfolioManagerService portfolioManagerService;

    @Autowired
    private FeeCalculatorService feeCalculatorService;

    @Autowired
    private ThresholdRebalancingStrategy thresholdStrategy;

    private List<Stock> testStocks;
    private Map<String, Double> testPrices;
    private LocalDate testDate;
    private BacktestContext testContext;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2023, 1, 2);
        testStocks = createTestStocks();
        testPrices = createTestPrices();
        testContext = createTestContext();
    }

    @Nested
    @DisplayName("초기 포트폴리오 구성 테스트")
    class InitialPortfolioCreationTest {

        @Test
        @DisplayName("정상적인 초기 포트폴리오 구성")
        void createInitialPortfolioSuccess() {
            // when
            Portfolio portfolio = portfolioManagerService.createInitialPortfolio(
                testStocks, testPrices, testDate);

            // then
            assertThat(portfolio).isNotNull();
            assertThat(portfolio.getInitialValue()).isPositive();
            assertThat(portfolio.getCash()).isZero(); // 초기 현금은 0
            assertThat(portfolio.getHoldingStockCodes()).hasSize(3);
            
            // 각 종목별 보유량 확인
            assertThat(portfolio.getHoldings("005930")).isEqualTo(100);
            assertThat(portfolio.getHoldings("000660")).isEqualTo(50);
            assertThat(portfolio.getHoldings("035420")).isEqualTo(25);
            
            // 초기 포트폴리오 가치 계산 확인
            double expectedValue = (100 * 50000) + (50 * 80000) + (25 * 200000);
            assertThat(portfolio.getInitialValue()).isEqualTo(expectedValue);
        }

        @Test
        @DisplayName("목표 종목 설정 확인")
        void targetStocksConfiguration() {
            // when
            Portfolio portfolio = portfolioManagerService.createInitialPortfolio(
                testStocks, testPrices, testDate);

            // then
            Map<String, Stock> targetStocks = portfolio.getTargetStocks();
            assertThat(targetStocks).hasSize(3);
            
            // 목표 비중 확인
            assertThat(targetStocks.get("005930").getOriginalWeight()).isEqualTo(40);
            assertThat(targetStocks.get("000660").getOriginalWeight()).isEqualTo(30);
            assertThat(targetStocks.get("035420").getOriginalWeight()).isEqualTo(30);
            
            // 임계값 확인
            for (Stock stock : targetStocks.values()) {
                assertThat(stock.getThresholdPercentage()).isEqualTo(0.05); // 5%
            }
        }

        @Test
        @DisplayName("빈 종목 목록으로 포트폴리오 생성 실패")
        void createPortfolioWithEmptyStocks() {
            // when & then
            assertThatThrownBy(() -> 
                portfolioManagerService.createInitialPortfolio(
                    Collections.emptyList(), testPrices, testDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종목 목록이 비어있습니다");
        }

        @Test
        @DisplayName("가격 정보 없음으로 포트폴리오 생성 실패")
        void createPortfolioWithoutPrices() {
            // when & then
            assertThatThrownBy(() -> 
                portfolioManagerService.createInitialPortfolio(
                    testStocks, Collections.emptyMap(), testDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("초기 가격 정보가 없습니다");
        }

        @Test
        @DisplayName("잘못된 날짜로 포트폴리오 생성 실패")
        void createPortfolioWithInvalidDate() {
            // when & then
            assertThatThrownBy(() -> 
                portfolioManagerService.createInitialPortfolio(
                    testStocks, testPrices, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("시작 날짜가 없습니다");
        }

        @Test
        @DisplayName("원본 가중치 합이 0인 경우 실패")
        void createPortfolioWithZeroTotalWeight() {
            // given
            List<Stock> zeroWeightStocks = testStocks.stream()
                .map(stock -> {
                    Stock newStock = new Stock(stock.getStockCode(), 0, stock.getThresholdPercentage(), stock.getInitialQuantity());
                    return newStock;
                })
                .toList();

            // when & then
            assertThatThrownBy(() -> 
                portfolioManagerService.createInitialPortfolio(
                    zeroWeightStocks, testPrices, testDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원본 가중치의 합이 0 이하입니다");
        }
    }

    @Nested
    @DisplayName("리밸런싱 실행 테스트")
    class RebalancingExecutionTest {

        private Portfolio portfolio;

        @BeforeEach
        void setUpPortfolio() {
            portfolio = portfolioManagerService.createInitialPortfolio(
                testStocks, testPrices, testDate);
        }

        @Test
        @DisplayName("정상적인 리밸런싱 실행")
        void executeRebalancingSuccess() {
            // given
            Map<String, Double> newPrices = Map.of(
                "005930", 55000.0,  // 10% 상승
                "000660", 72000.0,  // 10% 하락
                "035420", 220000.0  // 10% 상승
            );

            // when
            List<Trade> trades = portfolioManagerService.executeRebalancing(
                portfolio, newPrices, testContext, testDate.plusDays(1));

            // then
            assertThat(trades).isNotNull();
            // 가격 변동으로 인한 비중 변화가 있다면 리밸런싱 거래 발생 가능
            
            // 거래가 발생했다면 매수/매도 검증
            if (!trades.isEmpty()) {
                long buyTrades = trades.stream().filter(Trade::isBuy).count();
                long sellTrades = trades.stream().filter(Trade::isSell).count();
                
                assertThat(buyTrades + sellTrades).isEqualTo(trades.size());
                
                // 모든 거래가 유효한 수량과 가격을 가져야 함
                for (Trade trade : trades) {
                    assertThat(trade.getQuantity()).isPositive();
                    assertThat(trade.getPrice()).isPositive();
                    assertThat(trade.getAmount()).isPositive();
                    assertThat(trade.getFee()).isGreaterThanOrEqualTo(0);
                }
            }
        }

        @Test
        @DisplayName("리밸런싱 필요없는 경우")
        void noRebalancingNeeded() {
            // given - 가격 변동이 임계값 이내
            Map<String, Double> slightlyChangedPrices = Map.of(
                "005930", 50500.0,  // 1% 상승
                "000660", 80800.0,  // 1% 상승
                "035420", 202000.0  // 1% 상승
            );

            // when
            List<Trade> trades = portfolioManagerService.executeRebalancing(
                portfolio, slightlyChangedPrices, testContext, testDate.plusDays(1));

            // then
            // 임계값(5%) 이내의 변동이므로 리밸런싱이 발생하지 않을 수 있음
            assertThat(trades).isNotNull();
        }

        @Test
        @DisplayName("일부 종목 가격 정보 없음")
        void rebalancingWithMissingPrices() {
            // given - 일부 종목 가격 누락
            Map<String, Double> incompletePrices = Map.of(
                "005930", 55000.0,
                "000660", 72000.0
                // 035420 가격 누락
            );

            // when
            List<Trade> trades = portfolioManagerService.executeRebalancing(
                portfolio, incompletePrices, testContext, testDate.plusDays(1));

            // then
            assertThat(trades).isNotNull();
            // 유효한 가격이 있는 종목들만으로 리밸런싱 수행
        }

        @Test
        @DisplayName("잘못된 매개변수로 리밸런싱 실행 실패")
        void rebalancingWithInvalidParameters() {
            // when & then
            assertThatThrownBy(() -> 
                portfolioManagerService.executeRebalancing(
                    null, testPrices, testContext, testDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포트폴리오가 null입니다");

            assertThatThrownBy(() -> 
                portfolioManagerService.executeRebalancing(
                    portfolio, null, testContext, testDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("현재 가격 정보가 없습니다");

            assertThatThrownBy(() -> 
                portfolioManagerService.executeRebalancing(
                    portfolio, testPrices, null, testDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("백테스트 컨텍스트가 null입니다");
        }
    }

    @Nested
    @DisplayName("바이앤홀드 계산 테스트")
    class BuyAndHoldCalculationTest {

        @Test
        @DisplayName("정상적인 바이앤홀드 가치 계산")
        void calculateBuyAndHoldValueSuccess() {
            // given
            Map<String, Integer> initialQuantities = Map.of(
                "005930", 100,
                "000660", 50,
                "035420", 25
            );
            
            Map<String, Double> initialPrices = Map.of(
                "005930", 50000.0,
                "000660", 80000.0,
                "035420", 200000.0
            );
            
            Map<String, Double> finalPrices = Map.of(
                "005930", 60000.0,  // 20% 상승
                "000660", 88000.0,  // 10% 상승
                "035420", 180000.0  // 10% 하락
            );

            // when
            double buyHoldValue = portfolioManagerService.calculateBuyAndHoldValue(
                initialQuantities, initialPrices, finalPrices);

            // then
            double expectedValue = (100 * 60000) + (50 * 88000) + (25 * 180000);
            assertThat(buyHoldValue).isEqualTo(expectedValue);
        }

        @Test
        @DisplayName("일부 종목 가격 정보 없는 경우")
        void buyAndHoldWithMissingPrices() {
            // given
            Map<String, Integer> initialQuantities = Map.of(
                "005930", 100,
                "000660", 50,
                "035420", 25
            );
            
            Map<String, Double> initialPrices = Map.of(
                "005930", 50000.0,
                "000660", 80000.0
                // 035420 가격 누락
            );
            
            Map<String, Double> finalPrices = Map.of(
                "005930", 60000.0,
                "000660", 88000.0
                // 035420 가격 누락
            );

            // when
            double buyHoldValue = portfolioManagerService.calculateBuyAndHoldValue(
                initialQuantities, initialPrices, finalPrices);

            // then
            // 유효한 종목들만 계산됨
            double expectedValue = (100 * 60000) + (50 * 88000);
            assertThat(buyHoldValue).isEqualTo(expectedValue);
        }

        @Test
        @DisplayName("잘못된 매개변수로 바이앤홀드 계산 실패")
        void buyAndHoldWithInvalidParameters() {
            Map<String, Double> validPrices = Map.of("005930", 50000.0);

            // when & then
            assertThatThrownBy(() -> 
                portfolioManagerService.calculateBuyAndHoldValue(
                    null, validPrices, validPrices))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("초기 보유 종목 정보가 없습니다");

            assertThatThrownBy(() -> 
                portfolioManagerService.calculateBuyAndHoldValue(
                    Map.of("005930", 100), null, validPrices))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("초기 가격 정보가 없습니다");
        }
    }

    @Nested
    @DisplayName("성과 지표 계산 테스트")
    class PerformanceMetricsTest {

        @Test
        @DisplayName("최대 낙폭 계산")
        void calculateMaxDrawdown() {
            // given - 포트폴리오 가치 변화 (상승 후 하락)
            List<Double> portfolioValues = Arrays.asList(
                10000000.0,  // 시작
                11000000.0,  // 10% 상승
                12000000.0,  // 20% 상승 (고점)
                10800000.0,  // 10% 하락
                9600000.0,   // 20% 하락 (최대 낙폭)
                10200000.0   // 회복
            );

            // when
            double maxDrawdown = portfolioManagerService.calculateMaxDrawdown(portfolioValues);

            // then
            // 고점(12M) 대비 최저점(9.6M)의 낙폭: (9.6-12)/12 = -20%
            assertThat(maxDrawdown).isCloseTo(-0.2, within(0.01));
        }

        @Test
        @DisplayName("변동성 계산")
        void calculateVolatility() {
            // given - 일별 수익률
            List<Double> periodReturns = Arrays.asList(
                0.01, -0.005, 0.015, -0.02, 0.008, -0.012, 0.018
            );
            int periodsPerYear = 252; // 연간 거래일

            // when
            double volatility = portfolioManagerService.calculateVolatility(periodReturns, periodsPerYear);

            // then
            assertThat(volatility).isPositive();
            assertThat(volatility).isLessThan(1.0); // 100% 미만이어야 합리적
        }

        @Test
        @DisplayName("샤프 비율 계산")
        void calculateSharpeRatio() {
            // given
            double portfolioReturn = 0.12; // 12% 연수익률
            double volatility = 0.15;      // 15% 변동성
            double riskFreeRate = 0.02;    // 2% 무위험수익률

            // when
            double sharpeRatio = portfolioManagerService.calculateSharpeRatio(
                portfolioReturn, volatility, riskFreeRate);

            // then
            double expectedSharpe = (0.12 - 0.02) / 0.15; // 0.67
            assertThat(sharpeRatio).isCloseTo(expectedSharpe, within(0.01));
        }

        @Test
        @DisplayName("샤프 비율 계산 - 기본 무위험수익률")
        void calculateSharpeRatioWithDefaultRiskFreeRate() {
            // given
            double portfolioReturn = 0.10;
            double volatility = 0.12;

            // when
            double sharpeRatio = portfolioManagerService.calculateSharpeRatio(portfolioReturn, volatility);

            // then
            // 기본 무위험수익률 2% 사용
            double expectedSharpe = (0.10 - 0.02) / 0.12;
            assertThat(sharpeRatio).isCloseTo(expectedSharpe, within(0.01));
        }

        @Test
        @DisplayName("변동성이 0인 경우 샤프 비율")
        void sharpeRatioWithZeroVolatility() {
            // when
            double sharpeRatio = portfolioManagerService.calculateSharpeRatio(0.05, 0.0);

            // then
            assertThat(sharpeRatio).isZero(); // 변동성이 0이면 샤프비율도 0
        }
    }

    @Nested
    @DisplayName("초기 포트폴리오 가치 계산 테스트")
    class InitialPortfolioValueCalculationTest {

        @Test
        @DisplayName("정상적인 초기 가치 계산")
        void calculateInitialValueSuccess() {
            // when
            double initialValue = portfolioManagerService.calculateInitialPortfolioValue(
                testStocks, testPrices);

            // then
            double expectedValue = (100 * 50000) + (50 * 80000) + (25 * 200000);
            assertThat(initialValue).isEqualTo(expectedValue);
        }

        @Test
        @DisplayName("일부 종목 수량이 0인 경우")
        void calculateInitialValueWithZeroQuantity() {
            // given
            List<Stock> stocksWithZero = Arrays.asList(
                new Stock("005930", 40, 0.05, 100),
                new Stock("000660", 30, 0.05, 0), // 수량 0
                new Stock("035420", 30, 0.05, 25)
            );

            // when
            double initialValue = portfolioManagerService.calculateInitialPortfolioValue(
                stocksWithZero, testPrices);

            // then
            // 수량이 0인 종목은 제외
            double expectedValue = (100 * 50000) + (25 * 200000);
            assertThat(initialValue).isEqualTo(expectedValue);
        }

        @Test
        @DisplayName("일부 종목 가격 정보 없는 경우")
        void calculateInitialValueWithMissingPrice() {
            // given
            Map<String, Double> incompletePrices = Map.of(
                "005930", 50000.0,
                "000660", 80000.0
                // 035420 가격 누락
            );

            // when
            double initialValue = portfolioManagerService.calculateInitialPortfolioValue(
                testStocks, incompletePrices);

            // then
            // 가격 정보가 없는 종목은 제외
            double expectedValue = (100 * 50000) + (50 * 80000);
            assertThat(initialValue).isEqualTo(expectedValue);
        }
    }

    // ===== Helper Methods =====

    private List<Stock> createTestStocks() {
        return Arrays.asList(
            new Stock("005930", 40, 0.05, 100),      // 40% 비중, 100주
            new Stock("000660", 30, 0.05, 50),     // 30% 비중, 50주
            new Stock("035420", 30, 0.05, 25)          // 30% 비중, 25주
        );
    }

    private Map<String, Double> createTestPrices() {
        return Map.of(
            "005930", 50000.0,   // 삼성전자
            "000660", 80000.0,   // SK하이닉스
            "035420", 200000.0   // NAVER
        );
    }

    private BacktestContext createTestContext() {
        // 간단한 테스트용 컨텍스트 생성
        LinkedHashMap<LocalDate, Map<String, Double>> dailyPrices = new LinkedHashMap<>();
        dailyPrices.put(testDate, testPrices);
        dailyPrices.put(testDate.plusDays(1), testPrices);

        return new BacktestContext(
            1L,
            testDate,
            testDate.plusDays(10),
            testStocks,
            dailyPrices,
            thresholdStrategy,
            RebalancingType.THRESHOLD,
            RebalancingPeriod.MONTHLY
        );
    }
}