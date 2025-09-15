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
 * PeriodicRebalancingStrategy 단위 테스트
 * 주기적 리밸런싱 전략의 정확성을 검증한다.
 */
@SpringBootTest
class PeriodicRebalancingStrategyTest {

    @Autowired
    private PeriodicRebalancingStrategy periodicStrategy;

    private Portfolio portfolio;
    private BacktestContext context;
    private Map<String, Double> testPrices;
    private LocalDate testStartDate;

    @BeforeEach
    void setUp() {
        testStartDate = LocalDate.of(2023, 1, 2); // 월요일
        testPrices = createTestPrices();
        portfolio = createTestPortfolio();
        context = createTestContext();
    }

    @Nested
    @DisplayName("월말 리밸런싱 테스트")
    class MonthlyRebalancingTest {

        @BeforeEach
        void setUpMonthly() {
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.MONTHLY);
        }

        @Test
        @DisplayName("월말 날짜에서 리밸런싱 실행")
        void rebalanceOnMonthEnd() {
            // given - 1월 31일 (월말)
            LocalDate monthEnd = LocalDate.of(2023, 1, 31);

            // when
            boolean shouldRebalance = periodicStrategy.shouldRebalance(
                testPrices, context, monthEnd, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue();
        }

        @Test
        @DisplayName("월말이 아닌 날짜에서 리밸런싱 미실행")
        void noRebalanceOnNonMonthEnd() {
            // given - 1월 15일 (월 중간)
            LocalDate midMonth = LocalDate.of(2023, 1, 15);

            // when
            boolean shouldRebalance = periodicStrategy.shouldRebalance(
                testPrices, context, midMonth, portfolio, null);

            // then
            assertThat(shouldRebalance).isFalse();
        }

        @Test
        @DisplayName("다양한 월말 날짜 테스트")
        void variousMonthEndDates() {
            // given - 다양한 월의 월말
            List<LocalDate> monthEnds = Arrays.asList(
                LocalDate.of(2023, 1, 31),  // 1월 31일
                LocalDate.of(2023, 2, 28),  // 2월 28일 (평년)
                LocalDate.of(2023, 3, 31),  // 3월 31일
                LocalDate.of(2023, 4, 30),  // 4월 30일
                LocalDate.of(2023, 6, 30),  // 6월 30일
                LocalDate.of(2023, 9, 30),  // 9월 30일
                LocalDate.of(2023, 12, 31)  // 12월 31일
            );

            // when & then
            for (LocalDate monthEnd : monthEnds) {
                boolean shouldRebalance = periodicStrategy.shouldRebalance(
                    testPrices, context, monthEnd, portfolio, null);
                
                assertThat(shouldRebalance)
                    .as("월말 %s에서 리밸런싱 실행되어야 함", monthEnd)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("윤년 2월 29일 테스트")
        void leapYearFebruaryEnd() {
            // given - 2024년 2월 29일 (윤년)
            LocalDate leapYearFeb = LocalDate.of(2024, 2, 29);

            // when
            boolean shouldRebalance = periodicStrategy.shouldRebalance(
                testPrices, context, leapYearFeb, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue();
        }

        @Test
        @DisplayName("첫 번째 거래일은 항상 리밸런싱")
        void firstTradingDayAlwaysRebalance() {
            // given - 백테스트 시작일
            LocalDate startDate = context.getStartDate();

            // when
            boolean shouldRebalance = periodicStrategy.shouldRebalance(
                testPrices, context, startDate, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue();
        }

        @Test
        @DisplayName("월말 이후 연속 거래일 테스트")
        void consecutiveDaysAfterMonthEnd() {
            // given
            LocalDate monthEnd = LocalDate.of(2023, 1, 31);
            LocalDate nextDay = LocalDate.of(2023, 2, 1);
            LocalDate dayAfter = LocalDate.of(2023, 2, 2);

            // when & then
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, monthEnd, portfolio, null)).isTrue();
            
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, nextDay, portfolio, monthEnd)).isFalse();
            
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, dayAfter, portfolio, monthEnd)).isFalse();
        }
    }

    @Nested
    @DisplayName("분기말 리밸런싱 테스트")
    class QuarterlyRebalancingTest {

        @BeforeEach
        void setUpQuarterly() {
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.QUARTERLY);
        }

        @Test
        @DisplayName("분기말 날짜에서 리밸런싱 실행")
        void rebalanceOnQuarterEnd() {
            // given - 분기말 날짜들
            List<LocalDate> quarterEnds = Arrays.asList(
                LocalDate.of(2023, 3, 31),  // 1분기 말
                LocalDate.of(2023, 6, 30),  // 2분기 말
                LocalDate.of(2023, 9, 30),  // 3분기 말
                LocalDate.of(2023, 12, 31)  // 4분기 말
            );

            // when & then
            for (LocalDate quarterEnd : quarterEnds) {
                boolean shouldRebalance = periodicStrategy.shouldRebalance(
                    testPrices, context, quarterEnd, portfolio, null);
                
                assertThat(shouldRebalance)
                    .as("분기말 %s에서 리밸런싱 실행되어야 함", quarterEnd)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("분기말이 아닌 날짜에서 리밸런싱 미실행")
        void noRebalanceOnNonQuarterEnd() {
            // given - 분기말이 아닌 날짜들
            List<LocalDate> nonQuarterEnds = Arrays.asList(
                LocalDate.of(2023, 1, 31),  // 1월 말 (분기말 아님)
                LocalDate.of(2023, 2, 28),  // 2월 말 (분기말 아님)
                LocalDate.of(2023, 4, 30),  // 4월 말 (분기말 아님)
                LocalDate.of(2023, 5, 31),  // 5월 말 (분기말 아님)
                LocalDate.of(2023, 7, 31),  // 7월 말 (분기말 아님)
                LocalDate.of(2023, 8, 31),  // 8월 말 (분기말 아님)
                LocalDate.of(2023, 10, 31), // 10월 말 (분기말 아님)
                LocalDate.of(2023, 11, 30)  // 11월 말 (분기말 아님)
            );

            // when & then
            for (LocalDate nonQuarterEnd : nonQuarterEnds) {
                boolean shouldRebalance = periodicStrategy.shouldRebalance(
                    testPrices, context, nonQuarterEnd, portfolio, null);
                
                assertThat(shouldRebalance)
                    .as("분기말이 아닌 %s에서 리밸런싱 미실행되어야 함", nonQuarterEnd)
                    .isFalse();
            }
        }

        @Test
        @DisplayName("분기 중간 날짜 테스트")
        void midQuarterDates() {
            // given - 분기 중간 날짜들
            List<LocalDate> midQuarterDates = Arrays.asList(
                LocalDate.of(2023, 2, 15),  // 1분기 중간
                LocalDate.of(2023, 5, 15),  // 2분기 중간
                LocalDate.of(2023, 8, 15),  // 3분기 중간
                LocalDate.of(2023, 11, 15)  // 4분기 중간
            );

            // when & then
            for (LocalDate midQuarter : midQuarterDates) {
                boolean shouldRebalance = periodicStrategy.shouldRebalance(
                    testPrices, context, midQuarter, portfolio, null);
                
                assertThat(shouldRebalance).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("리밸런싱 주기 설정 테스트")
    class RebalancingPeriodConfigurationTest {

        @Test
        @DisplayName("월별 주기 설정")
        void setMonthlyPeriod() {
            // when
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.MONTHLY);

            // then
            assertThat(periodicStrategy.getRebalancingPeriod()).isEqualTo(RebalancingPeriod.MONTHLY);
        }

        @Test
        @DisplayName("분기별 주기 설정")
        void setQuarterlyPeriod() {
            // when
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.QUARTERLY);

            // then
            assertThat(periodicStrategy.getRebalancingPeriod()).isEqualTo(RebalancingPeriod.QUARTERLY);
        }

        @Test
        @DisplayName("null 주기 설정 시 기본값")
        void setNullPeriodDefaultsToMonthly() {
            // when
            periodicStrategy.setRebalancingPeriod(null);

            // then
            assertThat(periodicStrategy.getRebalancingPeriod()).isEqualTo(RebalancingPeriod.MONTHLY);
        }

        @Test
        @DisplayName("주기 변경 후 동작 확인")
        void behaviorAfterPeriodChange() {
            // given
            LocalDate monthEnd = LocalDate.of(2023, 1, 31);
            LocalDate quarterEnd = LocalDate.of(2023, 3, 31);

            // when & then - 월별로 설정 후 테스트
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.MONTHLY);
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, monthEnd, portfolio, null)).isTrue();

            // 분기별로 변경 후 테스트
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.QUARTERLY);
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, monthEnd, portfolio, null)).isFalse();
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, quarterEnd, portfolio, null)).isTrue();
        }
    }

    @Nested
    @DisplayName("리밸런싱 이력 관리 테스트")
    class RebalancingHistoryTest {

        @Test
        @DisplayName("이전 리밸런싱 날짜 고려 - 월별")
        void considerPreviousRebalancingDateMonthly() {
            // given
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.MONTHLY);
            
            LocalDate firstMonthEnd = LocalDate.of(2023, 1, 31);
            LocalDate secondMonthEnd = LocalDate.of(2023, 2, 28);
            LocalDate thirdMonthEnd = LocalDate.of(2023, 3, 31);

            // when & then
            // 첫 번째 월말
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, firstMonthEnd, portfolio, null)).isTrue();

            // 두 번째 월말 (이전 리밸런싱 날짜 고려)
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, secondMonthEnd, portfolio, firstMonthEnd)).isTrue();

            // 세 번째 월말
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, thirdMonthEnd, portfolio, secondMonthEnd)).isTrue();
        }

        @Test
        @DisplayName("이전 리밸런싱 날짜 고려 - 분기별")
        void considerPreviousRebalancingDateQuarterly() {
            // given
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.QUARTERLY);
            
            LocalDate firstQuarterEnd = LocalDate.of(2023, 3, 31);
            LocalDate secondQuarterEnd = LocalDate.of(2023, 6, 30);
            LocalDate monthEndBetween = LocalDate.of(2023, 4, 30);

            // when & then
            // 첫 번째 분기말
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, firstQuarterEnd, portfolio, null)).isTrue();

            // 분기 사이의 월말 (리밸런싱 안 함)
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, monthEndBetween, portfolio, firstQuarterEnd)).isFalse();

            // 두 번째 분기말
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, secondQuarterEnd, portfolio, firstQuarterEnd)).isTrue();
        }

        @Test
        @DisplayName("같은 기간 내 중복 리밸런싱 방지")
        void preventDuplicateRebalancingInSamePeriod() {
            // given
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.MONTHLY);
            
            LocalDate monthEnd = LocalDate.of(2023, 1, 31);
            LocalDate dayAfterMonthEnd = LocalDate.of(2023, 2, 1);

            // when & then
            // 월말에 리밸런싱
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, monthEnd, portfolio, null)).isTrue();

            // 다음 날에는 리밸런싱 안 함
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, context, dayAfterMonthEnd, portfolio, monthEnd)).isFalse();
        }
    }

    @Nested
    @DisplayName("극단적 상황 테스트")
    class ExtremeScenarioTest {

        @Test
        @DisplayName("백테스트 시작일이 월말인 경우")
        void backtestStartsOnMonthEnd() {
            // given
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.MONTHLY);
            LocalDate startDate = LocalDate.of(2023, 1, 31); // 월말 시작
            
            BacktestContext monthEndContext = createContextWithStartDate(startDate);

            // when
            boolean shouldRebalance = periodicStrategy.shouldRebalance(
                testPrices, monthEndContext, startDate, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue(); // 시작일이므로 항상 true
        }

        @Test
        @DisplayName("백테스트 시작일이 분기말인 경우")
        void backtestStartsOnQuarterEnd() {
            // given
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.QUARTERLY);
            LocalDate startDate = LocalDate.of(2023, 3, 31); // 분기말 시작
            
            BacktestContext quarterEndContext = createContextWithStartDate(startDate);

            // when
            boolean shouldRebalance = periodicStrategy.shouldRebalance(
                testPrices, quarterEndContext, startDate, portfolio, null);

            // then
            assertThat(shouldRebalance).isTrue(); // 시작일이므로 항상 true
        }

        @Test
        @DisplayName("매우 짧은 백테스트 기간")
        void veryShortBacktestPeriod() {
            // given
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.MONTHLY);
            
            LocalDate startDate = LocalDate.of(2023, 1, 2);
            LocalDate endDate = LocalDate.of(2023, 1, 5); // 3일간
            
            BacktestContext shortContext = createContextWithPeriod(startDate, endDate);

            // when & then
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, shortContext, startDate, portfolio, null)).isTrue();
            
            assertThat(periodicStrategy.shouldRebalance(
                testPrices, shortContext, LocalDate.of(2023, 1, 3), portfolio, startDate)).isFalse();
        }

        @Test
        @DisplayName("매우 긴 백테스트 기간")
        void veryLongBacktestPeriod() {
            // given
            periodicStrategy.setRebalancingPeriod(RebalancingPeriod.QUARTERLY);
            
            LocalDate startDate = LocalDate.of(2020, 1, 2);
            LocalDate endDate = LocalDate.of(2023, 12, 31); // 4년간
            
            BacktestContext longContext = createContextWithPeriod(startDate, endDate);

            // when & then - 몇 개 분기말 테스트
            List<LocalDate> quarterEnds = Arrays.asList(
                LocalDate.of(2020, 3, 31),
                LocalDate.of(2021, 6, 30),
                LocalDate.of(2022, 9, 30),
                LocalDate.of(2023, 12, 31)
            );

            for (LocalDate quarterEnd : quarterEnds) {
                assertThat(periodicStrategy.shouldRebalance(
                    testPrices, longContext, quarterEnd, portfolio, null)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("전략 메타데이터 테스트")
    class StrategyMetadataTest {

        @Test
        @DisplayName("전략 이름 확인")
        void strategyName() {
            // when
            String strategyName = periodicStrategy.getStrategyName();

            // then
            assertThat(strategyName).isNotBlank();
            assertThat(strategyName.toLowerCase()).contains("periodic");
        }

        @Test
        @DisplayName("전략 설명 확인")
        void strategyDescription() {
            // when
            String description = periodicStrategy.getStrategyDescription();

            // then
            assertThat(description).isNotBlank();
            assertThat(description).contains("주기");
        }

    }

    // ===== Helper Methods =====

    private Map<String, Double> createTestPrices() {
        return Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0
        );
    }

    private Portfolio createTestPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setInitialValue(10000000.0);
        
        portfolio.setInitialHolding("005930", 100);
        portfolio.setInitialHolding("000660", 50);
        portfolio.setInitialHolding("035420", 25);
        
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

    private BacktestContext createTestContext() {
        return createContextWithPeriod(testStartDate, testStartDate.plusMonths(3));
    }

    private BacktestContext createContextWithStartDate(LocalDate startDate) {
        return createContextWithPeriod(startDate, startDate.plusMonths(3));
    }

    private BacktestContext createContextWithPeriod(LocalDate startDate, LocalDate endDate) {
        List<Stock> stocks = Arrays.asList(
            new Stock("005930", 40, 0.05, 100),
            new Stock("000660", 30, 0.05, 50),
            new Stock("035420", 30, 0.05, 25)
        );

        LinkedHashMap<LocalDate, Map<String, Double>> dailyPrices = new LinkedHashMap<>();
        
        // 테스트 기간의 매일 가격 데이터 생성 (간단히 동일한 가격)
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyPrices.put(current, testPrices);
            current = current.plusDays(1);
        }

        return new BacktestContext(
            1L,
            startDate,
            endDate,
            stocks,
            dailyPrices,
            periodicStrategy,
            RebalancingType.PERIODIC,
            RebalancingPeriod.MONTHLY
        );
    }
}