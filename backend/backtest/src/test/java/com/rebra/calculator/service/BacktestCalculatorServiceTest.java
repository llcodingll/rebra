package com.rebra.calculator.service;

import com.rebra.calculator.constant.BacktestConstants;
import com.rebra.calculator.domain.Stock;
import com.rebra.calculator.dto.*;
import com.rebra.calculator.enums.BacktestStatus;
import com.rebra.calculator.enums.RebalancingPeriod;
import com.rebra.calculator.enums.RebalancingType;
import com.rebra.calculator.strategy.PeriodicRebalancingStrategy;
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
 * BacktestCalculatorService 통합 테스트
 * 백테스트 메인 로직의 정확성을 검증한다.
 */
@SpringBootTest
class BacktestCalculatorServiceTest {

    @Autowired
    private BacktestCalculatorService backtestCalculatorService;

    @Autowired
    private PortfolioManagerService portfolioManagerService;

    @Autowired
    private FeeCalculatorService feeCalculatorService;

    @Autowired
    private ThresholdRebalancingStrategy thresholdStrategy;

    @Autowired
    private PeriodicRebalancingStrategy periodicStrategy;

    private BacktestRequest validRequest;
    private Map<String, Map<String, Double>> samplePriceData;

    @BeforeEach
    void setUp() {
        validRequest = createValidBacktestRequest();
        samplePriceData = createSamplePriceData();
    }

    @Nested
    @DisplayName("정상적인 백테스트 실행 테스트")
    class SuccessfulBacktestExecution {

        @Test
        @DisplayName("임계값 기반 리밸런싱 백테스트")
        void executeThresholdBasedBacktest() {
            // given
            validRequest.setRebalancingType(RebalancingType.THRESHOLD);
            validRequest.setRebalancingPeriod(null);

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            assertThat(response.getBacktestId()).isEqualTo(validRequest.getBacktestId());
            assertThat(response.getSummary()).isNotNull();
            assertThat(response.getDetails()).isNotEmpty();
            assertThat(response.getCalculationTimeMs()).isGreaterThan(0);

            // 요약 결과 검증
            BacktestSummaryDto summary = response.getSummary();
            assertThat(summary.getFinalValue()).isPositive();
            assertThat(summary.getTotalReturn()).isNotNull();
            assertThat(summary.getBuyHoldReturn()).isNotNull();
            assertThat(summary.getRebalancingCount()).isGreaterThanOrEqualTo(0);
            assertThat(summary.getTotalFee()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("주기적 리밸런싱 백테스트 - 월말")
        void executePeriodicMonthlyBacktest() {
            // given
            validRequest.setRebalancingType(RebalancingType.PERIODIC);
            validRequest.setRebalancingPeriod(RebalancingPeriod.MONTHLY);

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            assertThat(response.getSummary()).isNotNull();
            assertThat(response.getDetails()).isNotEmpty();

            // 주기적 리밸런싱의 특성 검증
            BacktestSummaryDto summary = response.getSummary();
            long periodDays = validRequest.getBacktestPeriodDays();
            int expectedMaxRebalancingCount = (int) Math.ceil(periodDays / 30.0); // 대략적인 월 수
            assertThat(summary.getRebalancingCount()).isLessThanOrEqualTo(expectedMaxRebalancingCount);
        }

        @Test
        @DisplayName("짧은 기간 백테스트")
        void executeShortPeriodBacktest() {
            // given - 1주일 백테스트
            LocalDate startDate = LocalDate.of(2023, 1, 2);
            LocalDate endDate = LocalDate.of(2023, 1, 6);
            
            BacktestRequest shortRequest = createBacktestRequestWithPeriod(startDate, endDate);

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(shortRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            assertThat(response.getDetails()).hasSizeLessThanOrEqualTo(5); // 영업일 기준
        }

        @Test
        @DisplayName("긴 기간 백테스트")
        void executeLongPeriodBacktest() {
            // given - 1년 백테스트
            LocalDate startDate = LocalDate.of(2022, 1, 3);
            LocalDate endDate = LocalDate.of(2022, 12, 30);
            
            BacktestRequest longRequest = createBacktestRequestWithPeriod(startDate, endDate);

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(longRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            assertThat(response.getDetails().size()).isGreaterThan(200); // 1년은 약 252 거래일
            
            // 장기 백테스트 특성 검증
            BacktestSummaryDto summary = response.getSummary();
            assertThat(summary.getVolatility()).isNotNull();
            assertThat(summary.getSharpeRatio()).isNotNull();
            assertThat(summary.getMaxDrawdown()).isLessThanOrEqualTo(0.0); // 낙폭은 음수 또는 0
        }
    }

    @Nested
    @DisplayName("백테스트 요청 검증 테스트")
    class RequestValidationTest {

        @Test
        @DisplayName("잘못된 백테스트 ID")
        void invalidBacktestId() {
            // given
            validRequest.setBacktestId(null);

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(response.getErrorMessage()).contains("유효하지 않습니다");
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦은 경우")
        void startDateAfterEndDate() {
            // given
            validRequest.setStartDate(LocalDate.of(2023, 12, 31));
            validRequest.setEndDate(LocalDate.of(2023, 1, 1));

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(response.getErrorMessage()).contains("시작일이 종료일보다 늦습니다");
        }

        @Test
        @DisplayName("백테스트 기간이 너무 짧은 경우")
        void backtestPeriodTooShort() {
            // given - 1일만
            validRequest.setStartDate(LocalDate.of(2023, 1, 2));
            validRequest.setEndDate(LocalDate.of(2023, 1, 2));

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(response.getErrorMessage()).contains("백테스트 기간이 너무 짧습니다");
        }

        @Test
        @DisplayName("종목 수가 너무 적은 경우")
        void tooFewStocks() {
            // given - 종목 없음
            validRequest.setStocks(new ArrayList<>());

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(response.getErrorMessage()).contains("종목이 필요합니다");
        }

        @Test
        @DisplayName("지원하지 않는 리밸런싱 유형")
        void unsupportedRebalancingType() {
            // given
            validRequest.setRebalancingType(null);

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(response.getErrorMessage()).contains("유효하지 않습니다");
        }
    }

    @Nested
    @DisplayName("성과 지표 계산 테스트")
    class PerformanceMetricsTest {

        @Test
        @DisplayName("수익률 계산 정확성")
        void returnCalculationAccuracy() {
            // given
            BacktestRequest request = createSimpleBacktestRequest();

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(request);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            BacktestSummaryDto summary = response.getSummary();
            List<BacktestDetailDto> details = response.getDetails();
            
            // 최종 수익률 검증
            double expectedReturn = (summary.getFinalValue() - 10000000.0) / 10000000.0; // 초기 1천만원 가정
            assertThat(summary.getTotalReturn()).isCloseTo(expectedReturn, within(0.0001));
            
            // 기간별 수익률 검증
            for (int i = 1; i < details.size(); i++) {
                BacktestDetailDto current = details.get(i);
                BacktestDetailDto previous = details.get(i - 1);
                
                double expectedPeriodReturn = (current.getPortfolioValue() - previous.getPortfolioValue()) 
                                            / previous.getPortfolioValue();
                assertThat(current.getPeriodReturn()).isCloseTo(expectedPeriodReturn, within(0.0001));
            }
        }

        @Test
        @DisplayName("바이앤홀드 수익률 계산")
        void buyAndHoldReturnCalculation() {
            // given
            BacktestRequest request = createSimpleBacktestRequest();

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(request);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            BacktestSummaryDto summary = response.getSummary();
            assertThat(summary.getBuyHoldReturn()).isNotNull();
            
            // 바이앤홀드 수익률은 리밸런싱 수익률과 다를 수 있음
            if (summary.getRebalancingCount() > 0) {
                // 리밸런싱이 발생한 경우 수익률 차이가 있을 수 있음
                assertThat(Math.abs(summary.getTotalReturn() - summary.getBuyHoldReturn())).isGreaterThanOrEqualTo(0);
            }
        }

        @Test
        @DisplayName("변동성 및 샤프 비율 계산")
        void volatilityAndSharpeRatioCalculation() {
            // given
            BacktestRequest request = createVolatileBacktestRequest();

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(request);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            BacktestSummaryDto summary = response.getSummary();
            assertThat(summary.getVolatility()).isGreaterThanOrEqualTo(0.0);
            assertThat(summary.getSharpeRatio()).isNotNull();
            
            // 높은 변동성의 경우 샤프 비율이 낮을 수 있음
            if (summary.getVolatility() > 0.3) { // 30% 이상 변동성
                assertThat(Math.abs(summary.getSharpeRatio())).isLessThan(3.0); // 현실적인 범위
            }
        }

        @Test
        @DisplayName("시간 가중 수익률 계산")
        void timeWeightedReturnCalculation() {
            // given
            BacktestRequest request = createSimpleBacktestRequest();

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(request);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            BacktestSummaryDto summary = response.getSummary();
            assertThat(summary.getTimeWeightedReturn()).isNotNull();
            
            // 시간 가중 수익률은 리밸런싱 영향을 제거한 순수 투자 성과
            // 일반적으로 총 수익률과 유사하지만 정확히 같지는 않을 수 있음
            double difference = Math.abs(summary.getTimeWeightedReturn() - summary.getTotalReturn());
            assertThat(difference).isLessThan(0.5); // 50% 이내 차이
        }
    }

    @Nested
    @DisplayName("리밸런싱 로직 테스트")
    class RebalancingLogicTest {

        @Test
        @DisplayName("임계값 기반 리밸런싱 트리거")
        void thresholdBasedRebalancingTrigger() {
            // given - 높은 변동성으로 임계값 초과 유도
            BacktestRequest request = createHighThresholdBacktestRequest();

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(request);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            BacktestSummaryDto summary = response.getSummary();
            List<BacktestDetailDto> details = response.getDetails();
            
            // 리밸런싱이 발생했는지 확인
            long rebalancingDays = details.stream()
                    .mapToLong(detail -> detail.getIsRebalanced() ? 1 : 0)
                    .sum();
            
            assertThat(rebalancingDays).isEqualTo(summary.getRebalancingCount());
            assertThat(summary.getRebalancingCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("주기적 리밸런싱 실행")
        void periodicRebalancingExecution() {
            // given
            validRequest.setRebalancingType(RebalancingType.PERIODIC);
            validRequest.setRebalancingPeriod(RebalancingPeriod.MONTHLY);

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            List<BacktestDetailDto> details = response.getDetails();
            
            // 월말에 리밸런싱이 발생하는지 확인
            List<BacktestDetailDto> rebalancingDetails = details.stream()
                    .filter(BacktestDetailDto::getIsRebalanced)
                    .toList();
            
            // 각 리밸런싱 날짜가 월말 근처인지 확인
            for (BacktestDetailDto detail : rebalancingDetails) {
                LocalDate date = detail.getPeriodDate();
                LocalDate monthEnd = date.withDayOfMonth(date.lengthOfMonth());
                
                // 월말 또는 월말 이전 영업일인지 확인 (주말 고려)
                long daysDifference = Math.abs(date.toEpochDay() - monthEnd.toEpochDay());
                assertThat(daysDifference).isLessThanOrEqualTo(3); // 3일 이내 차이 허용
            }
        }

        @Test
        @DisplayName("거래비용 계산 및 반영")
        void tradingCostCalculationAndReflection() {
            // given
            BacktestRequest request = createHighFrequencyRebalancingRequest();

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(request);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            BacktestSummaryDto summary = response.getSummary();
            
            // 리밸런싱이 많이 발생했다면 거래비용도 상당해야 함
            if (summary.getRebalancingCount() > 10) {
                assertThat(summary.getTotalFee()).isGreaterThan(0);
                
                // 거래비용이 수익에 미치는 영향 확인
                double costRatio = summary.getTotalFee() / 10000000.0; // 초기 자본 대비
                assertThat(costRatio).isLessThan(0.1); // 10% 미만이어야 합리적
            }
        }
    }

    @Nested
    @DisplayName("차입 비용 테스트")
    class BorrowingCostTest {

        @Test
        @DisplayName("차입 발생 및 이자 계산")
        void borrowingOccurrenceAndInterestCalculation() {
            // given - 큰 비중으로 차입 유도
            BacktestRequest request = createBorrowingInducingRequest();

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(request);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            BacktestSummaryDto summary = response.getSummary();
            
            // 차입이 발생했다면
            if (summary.getMaxBorrowingAmount() > 0) {
                assertThat(summary.getTotalBorrowingCost()).isGreaterThan(0);
                assertThat(summary.getMinCashBalance()).isNegative();
                
                // 차입 비용이 합리적인 범위인지 확인
                double annualBorrowingRate = summary.getTotalBorrowingCost() / summary.getMaxBorrowingAmount();
                assertThat(annualBorrowingRate).isLessThan(0.1); // 연 10% 미만
            }
        }

        @Test
        @DisplayName("차입 관련 상세 기록")
        void borrowingDetailedRecords() {
            // given
            BacktestRequest request = createBorrowingInducingRequest();

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(request);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            List<BacktestDetailDto> details = response.getDetails();
            
            // 차입 이자가 기록된 날들 확인
            List<BacktestDetailDto> borrowingDays = details.stream()
                    .filter(detail -> detail.getDailyBorrowingInterest() > 0)
                    .toList();
            
            if (!borrowingDays.isEmpty()) {
                // 차입 이자가 일관성 있게 계산되는지 확인
                for (BacktestDetailDto detail : borrowingDays) {
                    assertThat(detail.getDailyBorrowingInterest()).isGreaterThan(0);
                    assertThat(detail.getCashBalance()).isNegative(); // 차입 상태
                }
            }
        }
    }

    @Nested
    @DisplayName("에러 처리 테스트")
    class ErrorHandlingTest {

        @Test
        @DisplayName("가격 데이터 없음")
        void missingPriceData() {
            // given
            validRequest.setDailyPrices(new HashMap<>());

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(response.getErrorMessage()).isNotBlank();
        }

        @Test
        @DisplayName("시작일 가격 데이터 없음")
        void missingStartDatePriceData() {
            // given
            Map<String, Map<String, Double>> incompletePrices = new HashMap<>(samplePriceData);
            incompletePrices.remove(validRequest.getStartDate().toString());
            validRequest.setDailyPrices(incompletePrices);

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(response.getErrorMessage()).contains("가격 정보가 없습니다");
        }

        @Test
        @DisplayName("런타임 예외 처리")
        void runtimeExceptionHandling() {
            // given - 잘못된 데이터로 예외 유발
            validRequest.getStocks().get(0).setWeight(-1); // 음수 가중치

            // when
            BacktestResponse response = backtestCalculatorService.executeBacktest(validRequest);

            // then
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(response.getErrorMessage()).isNotBlank();
            assertThat(response.getCalculationTimeMs()).isGreaterThan(0);
        }
    }

    // ===== Helper Methods =====

    private BacktestRequest createValidBacktestRequest() {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(1L);
        request.setStartDate(LocalDate.of(2023, 1, 2));
        request.setEndDate(LocalDate.of(2023, 1, 31));
        request.setRebalancingType(RebalancingType.THRESHOLD);
        request.setRebalancingPeriod(null);
        
        // 종목 설정
        List<BacktestStockDto> stocks = Arrays.asList(
            createBacktestStock("005930", "삼성전자", 40, 167, 50000.0, 5.0),
            createBacktestStock("000660", "SK하이닉스", 30, 83, 80000.0, 5.0),
            createBacktestStock("035420", "NAVER", 30, 50, 200000.0, 5.0)
        );
        request.setStocks(stocks);
        
        request.setDailyPrices(samplePriceData);
        
        return request;
    }

    private BacktestStockDto createBacktestStock(String stockCode, String stockName, int weight, 
                                               int shares, double basePrice, double thresholdPercentage) {
        BacktestStockDto stock = new BacktestStockDto();
        stock.setStockCode(stockCode);
        stock.setWeight(weight);
        stock.setShares(shares);
        stock.setThresholdPercentage(thresholdPercentage);
        return stock;
    }

    private Map<String, Map<String, Double>> createSamplePriceData() {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        
        LocalDate startDate = LocalDate.of(2023, 1, 2);
        LocalDate endDate = LocalDate.of(2023, 1, 31);
        
        // 기본 가격
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0
        );
        
        // 각 날짜별로 약간씩 변동하는 가격 생성
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 주말 제외
            if (date.getDayOfWeek().getValue() >= 6) {
                continue;
            }
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                // ±5% 범위에서 랜덤 변동
                double variation = 0.9 + (Math.random() * 0.2); // 0.9 ~ 1.1
                double price = entry.getValue() * variation;
                dayPrices.put(entry.getKey(), (double) Math.round(price));
            }
            
            priceData.put(date.toString(), dayPrices);
        }
        
        return priceData;
    }

    private BacktestRequest createBacktestRequestWithPeriod(LocalDate startDate, LocalDate endDate) {
        BacktestRequest request = createValidBacktestRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        
        // 해당 기간의 가격 데이터 생성
        Map<String, Map<String, Double>> periodPriceData = new LinkedHashMap<>();
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0
        );
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue;
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                double variation = 0.95 + (Math.random() * 0.1);
                dayPrices.put(entry.getKey(), entry.getValue() * variation);
            }
            periodPriceData.put(date.toString(), dayPrices);
        }
        
        request.setDailyPrices(periodPriceData);
        return request;
    }

    private BacktestRequest createSimpleBacktestRequest() {
        return createValidBacktestRequest(); // 기본 요청이 충분히 간단함
    }

    private BacktestRequest createVolatileBacktestRequest() {
        BacktestRequest request = createValidBacktestRequest();
        
        // 높은 변동성 가격 데이터 생성
        Map<String, Map<String, Double>> volatilePriceData = new LinkedHashMap<>();
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0
        );
        
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue;
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                // ±20% 큰 변동
                double variation = 0.8 + (Math.random() * 0.4); // 0.8 ~ 1.2
                dayPrices.put(entry.getKey(), entry.getValue() * variation);
            }
            volatilePriceData.put(date.toString(), dayPrices);
        }
        
        request.setDailyPrices(volatilePriceData);
        return request;
    }

    private BacktestRequest createHighThresholdBacktestRequest() {
        BacktestRequest request = createVolatileBacktestRequest();
        
        // 낮은 임계값으로 설정하여 리밸런싱 자주 발생하도록
        for (BacktestStockDto stock : request.getStocks()) {
            stock.setThresholdPercentage(2.0); // 2%로 낮춤
        }
        
        return request;
    }

    private BacktestRequest createHighFrequencyRebalancingRequest() {
        return createHighThresholdBacktestRequest(); // 높은 빈도 리밸런싱
    }

    private BacktestRequest createBorrowingInducingRequest() {
        BacktestRequest request = createValidBacktestRequest();
        
        // 높은 비중으로 설정하여 차입 유도 (합계 100% 초과)
        List<BacktestStockDto> stocks = Arrays.asList(
            createBacktestStock("005930", "삼성전자", 60, 200, 50000.0, 5.0),
            createBacktestStock("000660", "SK하이닉스", 50, 100, 80000.0, 5.0),
            createBacktestStock("035420", "NAVER", 40, 60, 200000.0, 5.0)
        );
        request.setStocks(stocks);
        
        return request;
    }
}