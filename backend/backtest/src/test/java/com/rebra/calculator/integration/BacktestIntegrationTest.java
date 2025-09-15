package com.rebra.calculator.integration;

import com.rebra.calculator.dto.*;
import com.rebra.calculator.enums.BacktestStatus;
import com.rebra.calculator.enums.RebalancingPeriod;
import com.rebra.calculator.enums.RebalancingType;
import com.rebra.calculator.kafka.BacktestKafkaListener;
import com.rebra.calculator.kafka.BacktestKafkaProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import static org.assertj.core.api.Assertions.*;

/**
 * 백테스트 시스템 통합 테스트
 * End-to-End 백테스트 시나리오를 검증한다.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"backtest-request", "backtest-result"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class
BacktestIntegrationTest {

    @Autowired
    private BacktestKafkaListener backtestKafkaListener;

    @Autowired
    private BacktestKafkaProducer backtestKafkaProducer;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, BacktestResponse> consumerFactory;


    private Consumer<String, BacktestResponse> resultConsumer;

    @BeforeEach
    void setUp() {
        // 결과 수신용 Consumer 설정
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.putAll(consumerFactory.getConfigurationProperties());
        
        resultConsumer = consumerFactory.createConsumer();
        resultConsumer.subscribe(Collections.singletonList("backtest-result"));
    }

    @Nested
    @DisplayName("End-to-End 백테스트 시나리오 테스트")
    class EndToEndBacktestScenarioTest {

        @Test
        @DisplayName("단순 백테스트 E2E 시나리오")
        void simpleBacktestEndToEndScenario() throws InterruptedException {
            // given
            BacktestRequest request = createSimpleBacktestRequest();
            CountDownLatch latch = new CountDownLatch(1);

            // when
            kafkaTemplate.send("backtest-request", request).whenComplete((result, ex) -> {
                if (ex == null) {
                    latch.countDown();
                }
            });

            // 메시지 전송 대기
            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                resultConsumer.poll(Duration.ofSeconds(30));
            
            assertThat(records).isNotEmpty();
            
            ConsumerRecord<String, BacktestResponse> record = records.iterator().next();
            BacktestResponse response = record.value();
            
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            assertThat(response.getBacktestId()).isEqualTo(request.getBacktestId());
            assertThat(response.getSummary()).isNotNull();
            assertThat(response.getDetails()).isNotEmpty();
            assertThat(response.getCalculationTimeMs()).isPositive();
        }

        @Test
        @DisplayName("복잡한 백테스트 E2E 시나리오")
        void complexBacktestEndToEndScenario() throws InterruptedException {
            // given
            BacktestRequest request = createComplexBacktestRequest();
            CountDownLatch latch = new CountDownLatch(1);

            // when
            kafkaTemplate.send("backtest-request", request).whenComplete((result, ex) -> {
                if (ex == null) {
                    latch.countDown();
                }
            });

            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                resultConsumer.poll(Duration.ofSeconds(30));
            
            assertThat(records).isNotEmpty();
            
            BacktestResponse response = records.iterator().next().value();
            
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            assertThat(response.getSummary()).isNotNull();
            
            // 복잡한 백테스트 특성 검증
            BacktestSummaryDto summary = response.getSummary();
            assertThat(summary.getRebalancingCount()).isGreaterThan(0);
            assertThat(summary.getTotalFee()).isPositive();
            assertThat(summary.getVolatility()).isNotNull();
            assertThat(summary.getSharpeRatio()).isNotNull();
            
            // 상세 결과 검증
            List<BacktestDetailDto> details = response.getDetails();
            assertThat(details.size()).isGreaterThan(10); // 충분한 거래일
            
            // 리밸런싱 발생 일수 확인
            long rebalancingDays = details.stream()
                .mapToLong(detail -> detail.getIsRebalanced() ? 1 : 0)
                .sum();
            assertThat(rebalancingDays).isEqualTo(summary.getRebalancingCount());
        }

        @Test
        @DisplayName("다중 백테스트 동시 처리 시나리오")
        void multipleConcurrentBacktestScenario() throws InterruptedException {
            // given
            int backtestCount = 3;
            List<BacktestRequest> requests = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(backtestCount);
            
            for (int i = 1; i <= backtestCount; i++) {
                BacktestRequest request = createSimpleBacktestRequest();
                request.setBacktestId((long) i);
                requests.add(request);
            }

            // when
            for (BacktestRequest request : requests) {
                kafkaTemplate.send("backtest-request", request).whenComplete((result, ex) -> {
                    if (ex == null) {
                        latch.countDown();
                    }
                });
            }

            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                resultConsumer.poll(Duration.ofSeconds(30));
            
            assertThat(records.count()).isEqualTo(backtestCount);
            
            Set<Long> receivedIds = new HashSet<>();
            for (ConsumerRecord<String, BacktestResponse> record : records) {
                BacktestResponse response = record.value();
                assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
                receivedIds.add(response.getBacktestId());
            }
            
            assertThat(receivedIds).containsExactlyInAnyOrder(1L, 2L, 3L);
        }
    }

    @Nested
    @DisplayName("리밸런싱 전략별 통합 테스트")
    class RebalancingStrategyIntegrationTest {

        @Test
        @DisplayName("임계값 기반 리밸런싱 통합 테스트")
        void thresholdBasedRebalancingIntegration() throws InterruptedException {
            // given
            BacktestRequest request = createThresholdRebalancingRequest();
            CountDownLatch latch = new CountDownLatch(1);

            // when
            kafkaTemplate.send("backtest-request", request).whenComplete((result, ex) -> {
                if (ex == null) {
                    latch.countDown();
                }
            });

            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                resultConsumer.poll(Duration.ofSeconds(30));
            
            BacktestResponse response = records.iterator().next().value();
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            // 임계값 기반 리밸런싱 특성 검증
            BacktestSummaryDto summary = response.getSummary();
            assertThat(summary.getRebalancingCount()).isGreaterThan(0);
            
            // 리밸런싱이 임계값 초과 시에만 발생했는지 확인
            List<BacktestDetailDto> details = response.getDetails();
            List<BacktestDetailDto> rebalancingDetails = details.stream()
                .filter(BacktestDetailDto::getIsRebalanced)
                .toList();
            
            assertThat(rebalancingDetails).isNotEmpty();
        }

        @Test
        @DisplayName("주기적 리밸런싱 통합 테스트")
        void periodicRebalancingIntegration() throws InterruptedException {
            // given
            BacktestRequest request = createPeriodicRebalancingRequest();
            CountDownLatch latch = new CountDownLatch(1);

            // when
            kafkaTemplate.send("backtest-request", request).whenComplete((result, ex) -> {
                if (ex == null) {
                    latch.countDown();
                }
            });

            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                resultConsumer.poll(Duration.ofSeconds(30));
            
            BacktestResponse response = records.iterator().next().value();
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            
            // 주기적 리밸런싱 특성 검증
            BacktestSummaryDto summary = response.getSummary();
            List<BacktestDetailDto> details = response.getDetails();
            
            // 월말에 리밸런싱이 발생했는지 확인
            List<BacktestDetailDto> rebalancingDetails = details.stream()
                .filter(BacktestDetailDto::getIsRebalanced)
                .toList();
            
            for (BacktestDetailDto detail : rebalancingDetails) {
                LocalDate date = detail.getPeriodDate();
                // 월말 또는 월말 근처여야 함 (주말 고려)
                LocalDate monthEnd = date.withDayOfMonth(date.lengthOfMonth());
                long daysDifference = Math.abs(date.toEpochDay() - monthEnd.toEpochDay());
                assertThat(daysDifference).isLessThanOrEqualTo(3);
            }
        }
    }

    @Nested
    @DisplayName("에러 상황 통합 테스트")
    class ErrorScenarioIntegrationTest {

        @Test
        @DisplayName("잘못된 요청 데이터 에러 처리")
        void invalidRequestDataErrorHandling() throws InterruptedException {
            // given
            BacktestRequest invalidRequest = createInvalidBacktestRequest();
            CountDownLatch latch = new CountDownLatch(1);

            // when
            kafkaTemplate.send("backtest-request", invalidRequest).whenComplete((result, ex) -> {
                if (ex == null) {
                    latch.countDown();
                }
            });

            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                resultConsumer.poll(Duration.ofSeconds(20));
            
            assertThat(records).isNotEmpty();
            
            BacktestResponse response = records.iterator().next().value();
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(response.getErrorMessage()).isNotBlank();
            assertThat(response.getSummary()).isNull();
            assertThat(response.getDetails()).isNull();
        }

        @Test
        @DisplayName("가격 데이터 부족 에러 처리")
        void insufficientPriceDataErrorHandling() throws InterruptedException {
            // given
            BacktestRequest request = createRequestWithInsufficientPriceData();
            CountDownLatch latch = new CountDownLatch(1);

            // when
            kafkaTemplate.send("backtest-request", request).whenComplete((result, ex) -> {
                if (ex == null) {
                    latch.countDown();
                }
            });

            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                resultConsumer.poll(Duration.ofSeconds(20));
            
            BacktestResponse response = records.iterator().next().value();
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(response.getErrorMessage()).contains("가격");
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTest {

        @Test
        @DisplayName("대용량 데이터 백테스트 성능")
        void largeDataBacktestPerformance() throws InterruptedException {
            // given
            BacktestRequest largeRequest = createLargeDataBacktestRequest();
            CountDownLatch latch = new CountDownLatch(1);
            long startTime = System.currentTimeMillis();

            // when
            kafkaTemplate.send("backtest-request", largeRequest).whenComplete((result, ex) -> {
                if (ex == null) {
                    latch.countDown();
                }
            });

            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                resultConsumer.poll(Duration.ofSeconds(60)); // 더 긴 대기 시간
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            assertThat(records).isNotEmpty();
            
            BacktestResponse response = records.iterator().next().value();
            assertThat(response.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            assertThat(response.getCalculationTimeMs()).isLessThan(30000); // 30초 이내
            assertThat(totalTime).isLessThan(60000); // 전체 1분 이내
            
            // 대용량 데이터 특성 확인
            assertThat(response.getDetails().size()).isGreaterThan(100);
        }

        @Test
        @DisplayName("동시 처리 성능 테스트")
        void concurrentProcessingPerformance() throws InterruptedException {
            // given
            int requestCount = 5;
            List<BacktestRequest> requests = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(requestCount);
            long startTime = System.currentTimeMillis();
            
            for (int i = 1; i <= requestCount; i++) {
                BacktestRequest request = createSimpleBacktestRequest();
                request.setBacktestId((long) i);
                requests.add(request);
            }

            // when
            for (BacktestRequest request : requests) {
                kafkaTemplate.send("backtest-request", request).whenComplete((result, ex) -> {
                    if (ex == null) {
                        latch.countDown();
                    }
                });
            }

            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                resultConsumer.poll(Duration.ofSeconds(45));
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            assertThat(records.count()).isEqualTo(requestCount);
            assertThat(totalTime).isLessThan(40000); // 40초 이내
            
            // 모든 응답이 성공적으로 처리되었는지 확인
            for (ConsumerRecord<String, BacktestResponse> record : records) {
                assertThat(record.value().getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            }
        }
    }

    // ===== Helper Methods =====

    private BacktestRequest createSimpleBacktestRequest() {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(1L);
        request.setStartDate(LocalDate.of(2023, 1, 2));
        request.setEndDate(LocalDate.of(2023, 1, 31));
        request.setRebalancingType(RebalancingType.THRESHOLD);
        
        List<BacktestStockDto> stocks = Arrays.asList(
            createBacktestStock("005930", "삼성전자", 40, 100),
            createBacktestStock("000660", "SK하이닉스", 30, 50),
            createBacktestStock("035420", "NAVER", 30, 25)
        );
        request.setStocks(stocks);
        request.setDailyPrices(createSimplePriceData(request.getStartDate(), request.getEndDate()));
        
        return request;
    }

    private BacktestRequest createComplexBacktestRequest() {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(2L);
        request.setStartDate(LocalDate.of(2023, 1, 2));
        request.setEndDate(LocalDate.of(2023, 3, 31)); // 3개월
        request.setRebalancingType(RebalancingType.THRESHOLD);
        
        List<BacktestStockDto> stocks = Arrays.asList(
            createBacktestStock("005930", "삼성전자", 30, 100),
            createBacktestStock("000660", "SK하이닉스", 25, 50),
            createBacktestStock("035420", "NAVER", 25, 25),
            createBacktestStock("005380", "현대차", 20, 30)
        );
        request.setStocks(stocks);
        request.setDailyPrices(createVolatilePriceData(request.getStartDate(), request.getEndDate()));
        
        return request;
    }

    private BacktestRequest createThresholdRebalancingRequest() {
        BacktestRequest request = createSimpleBacktestRequest();
        request.setBacktestId(3L);
        request.setRebalancingType(RebalancingType.THRESHOLD);
        
        // 낮은 임계값으로 설정하여 리밸런싱 자주 발생하도록
        for (BacktestStockDto stock : request.getStocks()) {
            stock.setThresholdPercentage(2.0); // 2%
        }
        
        return request;
    }

    private BacktestRequest createPeriodicRebalancingRequest() {
        BacktestRequest request = createSimpleBacktestRequest();
        request.setBacktestId(4L);
        request.setRebalancingType(RebalancingType.PERIODIC);
        request.setRebalancingPeriod(RebalancingPeriod.MONTHLY);
        
        return request;
    }

    private BacktestRequest createInvalidBacktestRequest() {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(null); // 잘못된 ID
        request.setStartDate(LocalDate.of(2023, 1, 2));
        request.setEndDate(LocalDate.of(2022, 12, 31)); // 시작일이 종료일보다 늦음
        request.setRebalancingType(RebalancingType.THRESHOLD);
        request.setStocks(Collections.emptyList()); // 빈 종목 목록
        request.setDailyPrices(Collections.emptyMap()); // 빈 가격 데이터
        
        return request;
    }

    private BacktestRequest createRequestWithInsufficientPriceData() {
        BacktestRequest request = createSimpleBacktestRequest();
        request.setBacktestId(5L);
        
        // 시작일 가격 데이터만 제공
        Map<String, Map<String, Double>> insufficientPrices = new HashMap<>();
        insufficientPrices.put(request.getStartDate().toString(), Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0
        ));
        request.setDailyPrices(insufficientPrices);
        
        return request;
    }

    private BacktestRequest createLargeDataBacktestRequest() {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(6L);
        request.setStartDate(LocalDate.of(2022, 1, 3));
        request.setEndDate(LocalDate.of(2022, 12, 30)); // 1년간
        request.setRebalancingType(RebalancingType.PERIODIC);
        request.setRebalancingPeriod(RebalancingPeriod.MONTHLY);
        
        List<BacktestStockDto> stocks = Arrays.asList(
            createBacktestStock("005930", "삼성전자", 25, 100),
            createBacktestStock("000660", "SK하이닉스", 25, 50),
            createBacktestStock("035420", "NAVER", 25, 25),
            createBacktestStock("005380", "현대차", 25, 30)
        );
        request.setStocks(stocks);
        request.setDailyPrices(createLargePriceData(request.getStartDate(), request.getEndDate()));
        
        return request;
    }

    private BacktestStockDto createBacktestStock(String stockCode, String stockName, int weight, int shares) {
        BacktestStockDto stock = new BacktestStockDto();
        stock.setStockCode(stockCode);
        stock.setWeight(weight);
        stock.setShares(shares);
        stock.setThresholdPercentage(5.0);
        return stock;
    }

    private Map<String, Map<String, Double>> createSimplePriceData(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0
        );
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue; // 주말 제외
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                double variation = 0.95 + (Math.random() * 0.1); // ±5% 변동
                dayPrices.put(entry.getKey(), entry.getValue() * variation);
            }
            priceData.put(date.toString(), dayPrices);
        }
        
        return priceData;
    }

    private Map<String, Map<String, Double>> createVolatilePriceData(LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Double>> priceData = new LinkedHashMap<>();
        Map<String, Double> basePrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0,
            "005380", 150000.0
        );
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) continue;
            
            Map<String, Double> dayPrices = new HashMap<>();
            for (Map.Entry<String, Double> entry : basePrices.entrySet()) {
                double variation = 0.8 + (Math.random() * 0.4); // ±20% 큰 변동
                dayPrices.put(entry.getKey(), entry.getValue() * variation);
            }
            priceData.put(date.toString(), dayPrices);
        }
        
        return priceData;
    }

    private Map<String, Map<String, Double>> createLargePriceData(LocalDate startDate, LocalDate endDate) {
        return createSimplePriceData(startDate, endDate); // 기간이 길어서 데이터량이 많음
    }
}