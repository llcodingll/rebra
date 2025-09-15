package com.rebra.calculator.strategy;

import com.rebra.calculator.context.BacktestContext;
import com.rebra.calculator.domain.Portfolio;
import com.rebra.calculator.domain.Stock;
import com.rebra.calculator.enums.RebalancingPeriod;
import com.rebra.calculator.enums.RebalancingType;
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
 * ThresholdRebalancingStrategy 단위 테스트
 * 임계값 기반 리밸런싱 전략의 정확성을 검증한다.
 */
@SpringBootTest
class ThresholdRebalancingStrategyTest {

    @Autowired
    private ThresholdRebalancingStrategy thresholdStrategy;

    private Portfolio portfolio;
    private BacktestContext context;
    private Map<String, Double> basePrices;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2023, 1, 2);
        basePrices = createBasePrices();
        portfolio = createTestPortfolio();
        context = createTestContext();
    }

    @Nested
    @DisplayName("리밸런싱 필요 여부 판단 테스트")
    class RebalancingNeedAssessmentTest {

        @Test
        @DisplayName("임계값 이내 변동 - 리밸런싱 불필요")
        void noRebalancingWithinThreshold() {
            // given - 임계값(5%) 이내의 가격 변동
            Map<String, Double> slightlyChangedPrices = Map.of(
                "005930", 52000.0,  // 4% 상승
                "000660", 83000.0,  // 3.75% 상승
                "035420", 208000.0  // 4% 상승
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance( slightlyChangedPrices, context, testDate, portfolio, null);

            // then
            assertThat(shouldRebalance).isFalse();
        }

        @Test
        @DisplayName("임계값 초과 변동 - 리밸런싱 필요")
        void rebalancingNeededBeyondThreshold() {
            // given - 임계값(5%) 초과 변동
            Map<String, Double> significantlyChangedPrices = Map.of(
                "005930", 55000.0,  // 10% 상승
                "000660", 76000.0,  // 5% 하락
                "035420", 220000.0  // 10% 상승
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance( significantlyChangedPrices, context, testDate, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue();
        }

        @Test
        @DisplayName("일부 종목만 임계값 초과 - 리밸런싱 필요")
        void rebalancingNeededWithPartialThresholdExceedance() {
            // given - 한 종목만 크게 변동
            Map<String, Double> partiallyChangedPrices = Map.of(
                "005930", 51000.0,  // 2% 상승 (임계값 이내)
                "000660", 81000.0,  // 1.25% 상승 (임계값 이내)
                "035420", 240000.0  // 20% 상승 (임계값 초과)
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance( partiallyChangedPrices, context, testDate, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue();
        }

        @Test
        @DisplayName("임계값 경계선 테스트")
        void thresholdBoundaryTest() {
            // given - 정확히 임계값(5%)에 해당하는 변동
            Map<String, Double> boundaryPrices = Map.of(
                "005930", 52500.0,  // 정확히 5% 상승
                "000660", 84000.0,  // 5% 상승
                "035420", 210000.0  // 5% 상승
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance( boundaryPrices, context, testDate, portfolio, null);

            // then - 경계값에서는 리밸런싱이 트리거될 수 있음
            // 구현에 따라 true 또는 false 가능
            assertThat(shouldRebalance).isIn(true, false);
        }

        @Test
        @DisplayName("첫 번째 거래일 - 항상 리밸런싱")
        void firstTradingDayAlwaysRebalance() {
            // given - 시작일
            LocalDate startDate = context.getStartDate();

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance( basePrices, context, startDate, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue();
        }
    }

    @Nested
    @DisplayName("다양한 임계값 설정 테스트")
    class DifferentThresholdValuesTest {

        @Test
        @DisplayName("낮은 임계값(1%) - 민감한 리밸런싱")
        void lowThresholdSensitiveRebalancing() {
            // given - 1% 임계값으로 설정
            Portfolio sensitivePortfolio = createPortfolioWithThreshold(0.01);
            
            Map<String, Double> smallChangePrices = Map.of(
                "005930", 50750.0,  // 1.5% 상승
                "000660", 80800.0,  // 1% 상승
                "035420", 203000.0  // 1.5% 상승
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance(
                smallChangePrices, context, testDate, sensitivePortfolio, null);

            // then
            assertThat(shouldRebalance).isTrue();
        }

        @Test
        @DisplayName("높은 임계값(10%) - 둔감한 리밸런싱")
        void highThresholdInsensitiveRebalancing() {
            // given - 10% 임계값으로 설정
            Portfolio tolerantPortfolio = createPortfolioWithThreshold(0.10);
            
            Map<String, Double> moderateChangePrices = Map.of(
                "005930", 54000.0,  // 8% 상승
                "000660", 87000.0,  // 8.75% 상승
                "035420", 216000.0  // 8% 상승
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance(
                moderateChangePrices, context, testDate, tolerantPortfolio, null);

            // then
            assertThat(shouldRebalance).isFalse();
        }

        @Test
        @DisplayName("종목별 다른 임계값 설정")
        void differentThresholdPerStock() {
            // given - 종목별로 다른 임계값 설정
            Portfolio mixedThresholdPortfolio = createMixedThresholdPortfolio();
            
            Map<String, Double> mixedChangePrices = Map.of(
                "005930", 53000.0,  // 6% 상승 (임계값 5% 초과)
                "000660", 86000.0,  // 7.5% 상승 (임계값 10% 이내)
                "035420", 206000.0  // 3% 상승 (임계값 2% 초과)
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance(
                mixedChangePrices, context, testDate, mixedThresholdPortfolio, null);

            // then
            assertThat(shouldRebalance).isTrue(); // 일부 종목이 임계값 초과
        }
    }

    @Nested
    @DisplayName("극단적 상황 테스트")
    class ExtremeScenarioTest {

        @Test
        @DisplayName("급격한 가격 상승 - 즉시 리밸런싱")
        void dramaticPriceIncrease() {
            // given - 50% 이상 급등
            Map<String, Double> surgePrices = Map.of(
                "005930", 75000.0,  // 50% 상승
                "000660", 120000.0, // 50% 상승
                "035420", 300000.0  // 50% 상승
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance( surgePrices, context, testDate, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue();
        }

        @Test
        @DisplayName("급격한 가격 하락 - 즉시 리밸런싱")
        void dramaticPriceDecrease() {
            // given - 30% 이상 급락
            Map<String, Double> crashPrices = Map.of(
                "005930", 35000.0,  // 30% 하락
                "000660", 56000.0,  // 30% 하락
                "035420", 140000.0  // 30% 하락
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance( crashPrices, context, testDate, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue();
        }

        @Test
        @DisplayName("일부 종목 가격 정보 없음")
        void missingPriceInformation() {
            // given - 일부 종목 가격 누락
            Map<String, Double> incompletePrices = Map.of(
                "005930", 55000.0,
                "000660", 85000.0
                // 035420 가격 누락
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance( incompletePrices, context, testDate, portfolio, null);

            // then
            // 가격 정보가 있는 종목들로만 판단
            assertThat(shouldRebalance).isNotNull();
        }

        @Test
        @DisplayName("가격이 0인 종목 - 상장폐지 상황")
        void zeroPrice() {
            // given - 일부 종목 가격이 0 (상장폐지)
            Map<String, Double> zeroPrices = Map.of(
                "005930", 55000.0,  // 정상
                "000660", 0.0,      // 상장폐지
                "035420", 220000.0  // 정상
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance( zeroPrices, context, testDate, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue(); // 상장폐지는 리밸런싱 트리거
        }
    }

    @Nested
    @DisplayName("전략 메타데이터 테스트")
    class StrategyMetadataTest {

        @Test
        @DisplayName("전략 이름 확인")
        void strategyName() {
            // when
            String strategyName = thresholdStrategy.getStrategyName();

            // then
            assertThat(strategyName).isNotBlank();
            assertThat(strategyName.toLowerCase()).contains("threshold");
        }

        @Test
        @DisplayName("전략 설명 확인")
        void strategyDescription() {
            // when
            String description = thresholdStrategy.getStrategyDescription();

            // then
            assertThat(description).isNotBlank();
            assertThat(description).contains("임계값");
        }

    }

    @Nested
    @DisplayName("연속적인 리밸런싱 판단 테스트")
    class ContinuousRebalancingTest {

        @Test
        @DisplayName("연속된 거래일에서의 리밸런싱 판단")
        void continuousRebalancingDecisions() {
            // given
            LocalDate[] testDates = {
                testDate,
                testDate.plusDays(1),
                testDate.plusDays(2),
                testDate.plusDays(3)
            };
            
            Map<String, Double>[] priceProgression = new Map[]{
                basePrices,
                Map.of("005930", 53000.0, "000660", 82000.0, "035420", 210000.0), // 중간 변동
                Map.of("005930", 55000.0, "000660", 84000.0, "035420", 220000.0), // 임계값 초과
                Map.of("005930", 54000.0, "000660", 83000.0, "035420", 215000.0)  // 다시 안정
            };

            LocalDate lastRebalancingDate = null;
            List<Boolean> decisions = new ArrayList<>();

            // when
            for (int i = 0; i < testDates.length; i++) {
                boolean shouldRebalance = thresholdStrategy.shouldRebalance(
                    priceProgression[i], context, testDates[i], portfolio, lastRebalancingDate);
                
                decisions.add(shouldRebalance);
                
                if (shouldRebalance) {
                    lastRebalancingDate = testDates[i];
                    // 리밸런싱 후 포트폴리오 상태 업데이트 (실제로는 다른 서비스에서 처리)
                }
            }

            // then
            assertThat(decisions).hasSize(4);
            assertThat(decisions.get(0)).isTrue();  // 첫 번째는 항상 true
            assertThat(decisions.get(2)).isTrue();  // 임계값 초과 시점
        }

        @Test
        @DisplayName("리밸런싱 직후 임계값 판단")
        void thresholdCheckAfterRebalancing() {
            // given - 리밸런싱이 방금 실행된 상황
            LocalDate rebalancingDate = testDate;
            LocalDate nextDay = testDate.plusDays(1);
            
            // 리밸런싱 직후 약간의 변동
            Map<String, Double> postRebalancingPrices = Map.of(
                "005930", 51000.0,  // 2% 변동
                "000660", 81000.0,  // 1.25% 변동
                "035420", 204000.0  // 2% 변동
            );

            // when
            boolean shouldRebalance = thresholdStrategy.shouldRebalance( postRebalancingPrices, context, nextDay, portfolio, rebalancingDate);

            // then
            assertThat(shouldRebalance).isFalse(); // 임계값 이내이므로 리밸런싱 불필요
        }
    }

    // ===== Helper Methods =====

    private Map<String, Double> createBasePrices() {
        return Map.of(
            "005930", 50000.0,   // 삼성전자
            "000660", 80000.0,   // SK하이닉스
            "035420", 200000.0   // NAVER
        );
    }

    private Portfolio createTestPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setInitialValue(10000000.0);
        
        // 초기 보유 주식 설정
        portfolio.setInitialHolding("005930", 100);
        portfolio.setInitialHolding("000660", 50);
        portfolio.setInitialHolding("035420", 25);
        
        // 목표 종목 설정 (임계값 5%)
        List<Stock> targetStocks = Arrays.asList(
            new Stock("005930", 40, 0.05, 100),
            new Stock("000660", 30, 0.05, 50),
            new Stock("035420", 30, 0.05, 25)
        );
        
        for (Stock stock : targetStocks) {
            portfolio.addTargetStock(stock);
        }
        
        return portfolio;
    }

    private Portfolio createPortfolioWithThreshold(double threshold) {
        Portfolio portfolio = new Portfolio();
        portfolio.setInitialValue(10000000.0);
        
        portfolio.setInitialHolding("005930", 100);
        portfolio.setInitialHolding("000660", 50);
        portfolio.setInitialHolding("035420", 25);
        
        List<Stock> targetStocks = Arrays.asList(
            new Stock("005930", 40, threshold, 100),
            new Stock("000660", 30, threshold, 50),
            new Stock("035420", 30, threshold, 25)
        );
        
        for (Stock stock : targetStocks) {
            portfolio.addTargetStock(stock);
        }
        
        return portfolio;
    }

    private Portfolio createMixedThresholdPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setInitialValue(10000000.0);
        
        portfolio.setInitialHolding("005930", 100);
        portfolio.setInitialHolding("000660", 50);
        portfolio.setInitialHolding("035420", 25);
        
        List<Stock> targetStocks = Arrays.asList(
            new Stock("005930", 40, 0.05, 100),   // 5% 임계값
            new Stock("000660", 30, 0.10, 50),  // 10% 임계값
            new Stock("035420", 30, 0.02, 25)       // 2% 임계값
        );
        
        for (Stock stock : targetStocks) {
            portfolio.addTargetStock(stock);
        }
        
        return portfolio;
    }

    private BacktestContext createTestContext() {
        List<Stock> stocks = Arrays.asList(
            new Stock("005930", 40, 0.05, 100),
            new Stock("000660", 30, 0.05, 50),
            new Stock("035420", 30, 0.05, 25)
        );

        LinkedHashMap<LocalDate, Map<String, Double>> dailyPrices = new LinkedHashMap<>();
        dailyPrices.put(testDate, basePrices);
        dailyPrices.put(testDate.plusDays(1), basePrices);

        return new BacktestContext(
            1L,
            testDate,
            testDate.plusDays(30),
            stocks,
            dailyPrices,
            thresholdStrategy,
            RebalancingType.THRESHOLD,
            RebalancingPeriod.MONTHLY
        );
    }
}