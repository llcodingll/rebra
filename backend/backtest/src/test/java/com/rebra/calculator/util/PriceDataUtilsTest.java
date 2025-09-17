package com.rebra.calculator.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PriceDataUtils 유틸리티 클래스 단위 테스트
 * 가격 데이터 처리 및 검증 로직을 테스트한다.
 */
class PriceDataUtilsTest {

    @Nested
    @DisplayName("유효한 가격 필터링 테스트")
    class ValidPriceFilteringTest {

        @Test
        @DisplayName("정상적인 가격 데이터 필터링")
        void filterValidPricesSuccess() {
            // given
            Map<String, Double> prices = Map.of(
                "005930", 50000.0,
                "000660", 80000.0,
                "035420", 200000.0
            );

            // when
            Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(prices);

            // then
            assertThat(validPrices).hasSize(3);
            assertThat(validPrices).containsExactlyEntriesOf(prices);
        }

        @Test
        @DisplayName("null 값 포함 가격 데이터 필터링")
        void filterPricesWithNullValues() {
            // given
            Map<String, Double> prices = new HashMap<>();
            prices.put("005930", 50000.0);
            prices.put("000660", null);        // null 값
            prices.put("035420", 200000.0);

            // when
            Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(prices);

            // then
            assertThat(validPrices).hasSize(2);
            assertThat(validPrices).containsKeys("005930", "035420");
            assertThat(validPrices).doesNotContainKey("000660");
        }

        @Test
        @DisplayName("0 이하 가격 데이터 필터링")
        void filterPricesWithZeroOrNegativeValues() {
            // given
            Map<String, Double> prices = Map.of(
                "005930", 50000.0,
                "000660", 0.0,        // 0 값
                "035420", -1000.0,    // 음수 값
                "005380", 150000.0
            );

            // when
            Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(prices);

            // then
            assertThat(validPrices).hasSize(2);
            assertThat(validPrices).containsKeys("005930", "005380");
            assertThat(validPrices).doesNotContainKeys("000660", "035420");
        }

        @Test
        @DisplayName("빈 맵 필터링")
        void filterEmptyPrices() {
            // given
            Map<String, Double> emptyPrices = Collections.emptyMap();

            // when
            Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(emptyPrices);

            // then
            assertThat(validPrices).isEmpty();
        }

        @Test
        @DisplayName("null 맵 필터링")
        void filterNullPrices() {
            // when
            Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(null);

            // then
            assertThat(validPrices).isEmpty();
        }

        @Test
        @DisplayName("모든 가격이 유효하지 않은 경우")
        void filterAllInvalidPrices() {
            // given
            Map<String, Double> invalidPrices = new HashMap<>();
            invalidPrices.put("005930", null);
            invalidPrices.put("000660", 0.0);
            invalidPrices.put("035420", -1000.0);

            // when
            Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(invalidPrices);

            // then
            assertThat(validPrices).isEmpty();
        }
    }

    @Nested
    @DisplayName("목표 비중 재분배 테스트")
    class WeightRedistributionTest {

        @Test
        @DisplayName("정상적인 비중 재분배")
        void redistributeWeightsNormally() {
            // given
            Map<String, Double> originalWeights = Map.of(
                "005930", 0.4,  // 40%
                "000660", 0.3,  // 30%
                "035420", 0.3   // 30%
            );
            Set<String> validStockCodes = Set.of("005930", "000660", "035420");

            // when
            Map<String, Double> redistributedWeights = PriceDataUtils.redistributeWeights(originalWeights, validStockCodes);

            // then
            assertThat(redistributedWeights).hasSize(3);
            assertThat(redistributedWeights.get("005930")).isCloseTo(0.4, within(0.0001));
            assertThat(redistributedWeights.get("000660")).isCloseTo(0.3, within(0.0001));
            assertThat(redistributedWeights.get("035420")).isCloseTo(0.3, within(0.0001));
        }

        @Test
        @DisplayName("일부 종목 제외 시 비중 재분배")
        void redistributeWeightsWithExcludedStock() {
            // given
            Map<String, Double> originalWeights = Map.of(
                "005930", 0.4,  // 40%
                "000660", 0.3,  // 30%
                "035420", 0.3   // 30%
            );
            Set<String> validStockCodes = Set.of("005930", "000660"); // NAVER 제외

            // when
            Map<String, Double> redistributedWeights = PriceDataUtils.redistributeWeights(originalWeights, validStockCodes);

            // then
            assertThat(redistributedWeights).hasSize(2);
            assertThat(redistributedWeights.get("005930")).isCloseTo(0.571, within(0.001)); // 40/70
            assertThat(redistributedWeights.get("000660")).isCloseTo(0.429, within(0.001)); // 30/70
            assertThat(redistributedWeights).doesNotContainKey("035420");
        }

        @Test
        @DisplayName("빈 유효 종목 코드로 재분배")
        void redistributeWeightsWithEmptyValidCodes() {
            // given
            Map<String, Double> originalWeights = Map.of(
                "005930", 0.4,
                "000660", 0.3,
                "035420", 0.3
            );
            Set<String> emptyValidCodes = Collections.emptySet();

            // when
            Map<String, Double> redistributedWeights = PriceDataUtils.redistributeWeights(originalWeights, emptyValidCodes);

            // then
            assertThat(redistributedWeights).isEmpty();
        }

        @Test
        @DisplayName("null 입력으로 재분배")
        void redistributeWeightsWithNullInputs() {
            // given
            Set<String> validStockCodes = Set.of("005930", "000660");

            // when
            Map<String, Double> result1 = PriceDataUtils.redistributeWeights(null, validStockCodes);
            Map<String, Double> result2 = PriceDataUtils.redistributeWeights(Map.of("005930", 0.5), null);

            // then
            assertThat(result1).isEmpty();
            assertThat(result2).isEmpty();
        }
    }

    @Nested
    @DisplayName("최근 유효 가격 검색 테스트")
    class RecentValidPriceSearchTest {

        @Test
        @DisplayName("정상적인 최근 가격 검색")
        void findRecentValidPriceNormally() {
            // given
            Map<String, Map<String, Double>> dailyPrices = new HashMap<>();
            dailyPrices.put("2023-01-01", Map.of("005930", 50000.0, "000660", 80000.0));
            dailyPrices.put("2023-01-02", Map.of("005930", 51000.0, "000660", 81000.0));
            dailyPrices.put("2023-01-03", Map.of("005930", 52000.0, "000660", 82000.0));

            LocalDate endDate = LocalDate.of(2023, 1, 3);

            // when
            Double recentPrice = PriceDataUtils.findRecentValidPrice(dailyPrices, "005930", endDate, 5);

            // then
            assertThat(recentPrice).isEqualTo(52000.0);
        }

        @Test
        @DisplayName("누락된 날짜에서 이전 유효 가격 검색")
        void findRecentValidPriceWithMissingDates() {
            // given
            Map<String, Map<String, Double>> dailyPrices = new HashMap<>();
            dailyPrices.put("2023-01-01", Map.of("005930", 50000.0));
            // 2023-01-02, 2023-01-03 누락
            dailyPrices.put("2023-01-04", Map.of("005930", 53000.0));

            LocalDate endDate = LocalDate.of(2023, 1, 3);

            // when
            Double recentPrice = PriceDataUtils.findRecentValidPrice(dailyPrices, "005930", endDate, 5);

            // then
            assertThat(recentPrice).isEqualTo(50000.0); // 2일 전 가격
        }

        @Test
        @DisplayName("null 값이 있는 날짜에서 이전 유효 가격 검색")
        void findRecentValidPriceWithNullValues() {
            // given
            Map<String, Map<String, Double>> dailyPrices = new HashMap<>();
            Map<String, Double> day1Prices = new HashMap<>();
            day1Prices.put("005930", 50000.0);
            day1Prices.put("000660", 80000.0);
            
            Map<String, Double> day2Prices = new HashMap<>();
            day2Prices.put("005930", null); // 거래정지
            day2Prices.put("000660", 81000.0);
            
            Map<String, Double> day3Prices = new HashMap<>();
            day3Prices.put("005930", 52000.0);
            day3Prices.put("000660", 82000.0);

            dailyPrices.put("2023-01-01", day1Prices);
            dailyPrices.put("2023-01-02", day2Prices);
            dailyPrices.put("2023-01-03", day3Prices);

            LocalDate endDate = LocalDate.of(2023, 1, 2);

            // when
            Double recentPrice = PriceDataUtils.findRecentValidPrice(dailyPrices, "005930", endDate, 5);

            // then
            assertThat(recentPrice).isEqualTo(50000.0); // 1일 전 유효 가격
        }

        @Test
        @DisplayName("검색 기간 내 유효 가격이 없는 경우")
        void findRecentValidPriceNotFound() {
            // given
            Map<String, Map<String, Double>> dailyPrices = new HashMap<>();
            dailyPrices.put("2022-12-30", Map.of("005930", 50000.0)); // 너무 이전

            LocalDate endDate = LocalDate.of(2023, 1, 3);

            // when
            Double recentPrice = PriceDataUtils.findRecentValidPrice(dailyPrices, "005930", endDate, 2);

            // then
            assertThat(recentPrice).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 종목코드 검색")
        void findRecentValidPriceForNonExistentStock() {
            // given
            Map<String, Map<String, Double>> dailyPrices = Map.of(
                "2023-01-01", Map.of("005930", 50000.0)
            );

            LocalDate endDate = LocalDate.of(2023, 1, 1);

            // when
            Double recentPrice = PriceDataUtils.findRecentValidPrice(dailyPrices, "999999", endDate, 5);

            // then
            assertThat(recentPrice).isNull();
        }

        @Test
        @DisplayName("null 입력으로 가격 검색")
        void findRecentValidPriceWithNullInputs() {
            // given
            Map<String, Map<String, Double>> dailyPrices = Map.of(
                "2023-01-01", Map.of("005930", 50000.0)
            );

            // when & then
            assertThat(PriceDataUtils.findRecentValidPrice(null, "005930", LocalDate.now(), 5)).isNull();
            assertThat(PriceDataUtils.findRecentValidPrice(dailyPrices, null, LocalDate.now(), 5)).isNull();
            assertThat(PriceDataUtils.findRecentValidPrice(dailyPrices, "005930", null, 5)).isNull();
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class RealUsageScenarioTest {

        @Test
        @DisplayName("리밸런싱을 위한 가격 데이터 전처리")
        void preprocessPriceDataForRebalancing() {
            // given - 실제 받은 가격 데이터 (일부 null, 0값 포함)
            Map<String, Double> rawPrices = new HashMap<>();
            rawPrices.put("005930", 50000.0);
            rawPrices.put("000660", null);     // 거래정지
            rawPrices.put("035420", 200000.0);
            rawPrices.put("005380", 0.0);      // 상장폐지
            rawPrices.put("068270", 85000.0);

            // when - 유효한 가격만 필터링
            Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(rawPrices);

            // then
            assertThat(validPrices).hasSize(3);
            assertThat(validPrices).containsKeys("005930", "035420", "068270");
            assertThat(validPrices).doesNotContainKeys("000660", "005380");
        }

        @Test
        @DisplayName("거래정지 종목 제외 후 비중 재조정")
        void rebalanceAfterTradingSuspension() {
            // given - 원래 목표 비중
            Map<String, Double> originalWeights = Map.of(
                "005930", 0.3,  // 삼성전자 30%
                "000660", 0.3,  // SK하이닉스 30% (거래정지)
                "035420", 0.4   // NAVER 40%
            );

            // 거래정지로 인한 유효 종목만 남음
            Set<String> validStockCodes = Set.of("005930", "035420");

            // when - 비중 재분배
            Map<String, Double> redistributedWeights = PriceDataUtils.redistributeWeights(originalWeights, validStockCodes);

            // then - 거래 가능한 종목들로만 100% 재분배
            assertThat(redistributedWeights).hasSize(2);
            assertThat(redistributedWeights.get("005930")).isCloseTo(0.429, within(0.001)); // 30/70
            assertThat(redistributedWeights.get("035420")).isCloseTo(0.571, within(0.001)); // 40/70
            
            // 총합이 1.0인지 확인
            double totalWeight = redistributedWeights.values().stream().mapToDouble(Double::doubleValue).sum();
            assertThat(totalWeight).isCloseTo(1.0, within(0.0001));
        }

        @Test
        @DisplayName("과거 가격 데이터 기반 포트폴리오 가치 산정")
        void calculatePortfolioValueWithHistoricalPrices() {
            // given - 과거 3일간의 가격 데이터
            Map<String, Map<String, Double>> historicalPrices = new HashMap<>();
            historicalPrices.put("2023-01-01", Map.of("005930", 50000.0, "000660", 80000.0));
            historicalPrices.put("2023-01-02", Map.of("005930", 51000.0, "000660", 81000.0));
            
            // 최신일 데이터 누락 가정
            LocalDate valuationDate = LocalDate.of(2023, 1, 3);

            // when - 가장 최근 유효 가격 찾기
            Double samsungPrice = PriceDataUtils.findRecentValidPrice(historicalPrices, "005930", valuationDate, 5);
            Double skhynixPrice = PriceDataUtils.findRecentValidPrice(historicalPrices, "000660", valuationDate, 5);

            // then - 최근 유효 가격으로 산정
            assertThat(samsungPrice).isEqualTo(51000.0); // 1일 전 가격
            assertThat(skhynixPrice).isEqualTo(81000.0); // 1일 전 가격

            // 포트폴리오 가치 계산 (보유 수량 가정)
            int samsungShares = 100;
            int skhynixShares = 50;
            double portfolioValue = (samsungPrice * samsungShares) + (skhynixPrice * skhynixShares);
            
            assertThat(portfolioValue).isEqualTo(9150000.0); // 510만 + 405만
        }
    }
}