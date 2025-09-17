package com.rebra.calculator.kafka;

import com.rebra.calculator.dto.BacktestDetailDto;
import com.rebra.calculator.dto.BacktestResponse;
import com.rebra.calculator.dto.BacktestSummaryDto;
import com.rebra.calculator.enums.BacktestStatus;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * BacktestKafkaProducer 단위 테스트
 * Kafka 메시지 전송 로직을 검증한다.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"backtest-result"}, 
               bootstrapServersProperty = "kafka.bootstrap-servers")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BacktestKafkaProducerTest {

    @Autowired
    private BacktestKafkaProducer backtestKafkaProducer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, BacktestResponse> consumer;
    private String topic = "backtest-result";

    @BeforeEach
    void setUp() {
        // 테스트용 Kafka Consumer 설정
        Map<String, Object> consumerProps = new HashMap<>(
            KafkaTestUtils.consumerProps("test-group", "false", 
                embeddedKafkaBroker));
        
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BacktestResponse.class);

        ConsumerFactory<String, BacktestResponse> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singletonList(topic));
    }

    @Nested
    @DisplayName("성공적인 백테스트 결과 전송")
    class SuccessfulResultSending {

        @Test
        @DisplayName("완료된 백테스트 결과 전송")
        void sendCompletedBacktestResult() {
            // given
            BacktestResponse successResponse = createSuccessResponse();

            // when
            backtestKafkaProducer.sendBacktestResult(successResponse);

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                consumer.poll(Duration.ofSeconds(10));
            
            assertThat(records).isNotEmpty();
            
            ConsumerRecord<String, BacktestResponse> record = records.iterator().next();
            BacktestResponse receivedResponse = record.value();
            
            assertThat(receivedResponse).isNotNull();
            assertThat(receivedResponse.getBacktestId()).isEqualTo(successResponse.getBacktestId());
            assertThat(receivedResponse.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
            assertThat(receivedResponse.getSummary()).isNotNull();
            assertThat(receivedResponse.getDetails()).isNotEmpty();
            assertThat(receivedResponse.getCalculationTimeMs()).isPositive();
        }

        @Test
        @DisplayName("상세한 백테스트 결과 데이터 전송")
        void sendDetailedBacktestResult() {
            // given
            BacktestResponse detailedResponse = createDetailedSuccessResponse();

            // when
            backtestKafkaProducer.sendBacktestResult(detailedResponse);

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                consumer.poll(Duration.ofSeconds(10));
            
            assertThat(records).isNotEmpty();
            
            BacktestResponse receivedResponse = records.iterator().next().value();
            
            // 요약 정보 검증
            BacktestSummaryDto summary = receivedResponse.getSummary();
            assertThat(summary.getFinalValue()).isPositive();
            assertThat(summary.getTotalReturn()).isNotNull();
            assertThat(summary.getBuyHoldReturn()).isNotNull();
            assertThat(summary.getRebalancingCount()).isGreaterThanOrEqualTo(0);
            assertThat(summary.getTotalFee()).isGreaterThanOrEqualTo(0);
            
            // 상세 정보 검증
            List<BacktestDetailDto> details = receivedResponse.getDetails();
            assertThat(details).hasSize(3);
            
            for (BacktestDetailDto detail : details) {
                assertThat(detail.getPeriodDate()).isNotNull();
                assertThat(detail.getPortfolioValue()).isPositive();
                assertThat(detail.getPeriodReturn()).isNotNull();
                assertThat(detail.getIsRebalanced()).isNotNull();
            }
        }

        @Test
        @DisplayName("여러 백테스트 결과 연속 전송")
        void sendMultipleBacktestResults() {
            // given
            int resultCount = 3;
            List<BacktestResponse> responses = new ArrayList<>();
            
            for (int i = 1; i <= resultCount; i++) {
                BacktestResponse response = createSuccessResponse();
                response.setBacktestId((long) i);
                responses.add(response);
            }

            // when
            for (BacktestResponse response : responses) {
                backtestKafkaProducer.sendBacktestResult(response);
            }

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                consumer.poll(Duration.ofSeconds(10));
            
            assertThat(records).hasSize(resultCount);
            
            Set<Long> receivedIds = new HashSet<>();
            for (ConsumerRecord<String, BacktestResponse> record : records) {
                receivedIds.add(record.value().getBacktestId());
            }
            
            assertThat(receivedIds).containsExactlyInAnyOrder(1L, 2L, 3L);
        }
    }

    @Nested
    @DisplayName("실패한 백테스트 결과 전송")
    class FailedResultSending {

        @Test
        @DisplayName("실패 상태 백테스트 결과 전송")
        void sendFailedBacktestResult() {
            // given
            BacktestResponse failureResponse = BacktestResponse.failure(
                1L, "계산 중 오류 발생", 500L);

            // when
            backtestKafkaProducer.sendBacktestResult(failureResponse);

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                consumer.poll(Duration.ofSeconds(10));
            
            assertThat(records).isNotEmpty();
            
            BacktestResponse receivedResponse = records.iterator().next().value();
            
            assertThat(receivedResponse.getBacktestId()).isEqualTo(1L);
            assertThat(receivedResponse.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(receivedResponse.getErrorMessage()).isEqualTo("계산 중 오류 발생");
            assertThat(receivedResponse.getCalculationTimeMs()).isEqualTo(500L);
            assertThat(receivedResponse.getSummary()).isNull();
            assertThat(receivedResponse.getDetails()).isNull();
        }

        @Test
        @DisplayName("다양한 에러 메시지 전송")
        void sendVariousErrorMessages() {
            // given
            List<String> errorMessages = Arrays.asList(
                "잘못된 요청 데이터",
                "가격 정보가 없습니다",
                "계산 중 예외 발생: NullPointerException",
                "백테스트 기간이 너무 짧습니다"
            );

            // when & then
            for (int i = 0; i < errorMessages.size(); i++) {
                String errorMessage = errorMessages.get(i);
                Long backtestId = (long) (i + 1);
                
                BacktestResponse failureResponse = BacktestResponse.failure(
                    backtestId, errorMessage, 300L + i * 100);
                
                backtestKafkaProducer.sendBacktestResult(failureResponse);
            }

            ConsumerRecords<String, BacktestResponse> records = 
                consumer.poll(Duration.ofSeconds(10));
            
            assertThat(records).hasSize(errorMessages.size());
            
            Map<Long, String> receivedErrors = new HashMap<>();
            for (ConsumerRecord<String, BacktestResponse> record : records) {
                BacktestResponse response = record.value();
                receivedErrors.put(response.getBacktestId(), response.getErrorMessage());
            }
            
            for (int i = 0; i < errorMessages.size(); i++) {
                Long backtestId = (long) (i + 1);
                assertThat(receivedErrors.get(backtestId)).isEqualTo(errorMessages.get(i));
            }
        }
    }

    @Nested
    @DisplayName("메시지 전송 에러 처리")
    class MessageSendingErrorHandling {

        @Test
        @DisplayName("null 응답 전송 시 예외 처리")
        void sendNullResponse() {
            // when & then
            assertThatThrownBy(() -> backtestKafkaProducer.sendBacktestResult(null))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("잘못된 백테스트 ID로 전송")
        void sendWithInvalidBacktestId() {
            // given
            BacktestResponse responseWithNullId = createSuccessResponse();
            responseWithNullId.setBacktestId(null);

            // when
            backtestKafkaProducer.sendBacktestResult(responseWithNullId);

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                consumer.poll(Duration.ofSeconds(10));
            
            if (!records.isEmpty()) {
                BacktestResponse receivedResponse = records.iterator().next().value();
                assertThat(receivedResponse.getBacktestId()).isNull();
            }
        }

        @Test
        @DisplayName("매우 큰 데이터 전송")
        void sendLargeDataResponse() {
            // given
            BacktestResponse largeResponse = createLargeDataResponse();

            // when
            backtestKafkaProducer.sendBacktestResult(largeResponse);

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                consumer.poll(Duration.ofSeconds(10));
            
            assertThat(records).isNotEmpty();
            
            BacktestResponse receivedResponse = records.iterator().next().value();
            assertThat(receivedResponse.getDetails()).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("메시지 전송 성능 테스트")
    class MessageSendingPerformanceTest {

        @Test
        @DisplayName("대량 메시지 전송 성능")
        void sendManyMessagesPerformance() {
            // given
            int messageCount = 100;
            List<BacktestResponse> responses = new ArrayList<>();
            
            for (int i = 1; i <= messageCount; i++) {
                BacktestResponse response = createSuccessResponse();
                response.setBacktestId((long) i);
                responses.add(response);
            }

            // when
            long startTime = System.currentTimeMillis();
            
            for (BacktestResponse response : responses) {
                backtestKafkaProducer.sendBacktestResult(response);
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            // then
            assertThat(totalTime).isLessThan(5000); // 5초 이내 완료
            
            // 일부 메시지가 전송되었는지 확인
            ConsumerRecords<String, BacktestResponse> records = 
                consumer.poll(Duration.ofSeconds(10));
            
            assertThat(records.count()).isGreaterThan(0);
        }

        @Test
        @DisplayName("동시 전송 처리")
        void concurrentMessageSending() throws InterruptedException {
            // given
            int threadCount = 5;
            int messagesPerThread = 10;
            List<Thread> threads = new ArrayList<>();

            // when
            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                Thread thread = new Thread(() -> {
                    for (int i = 0; i < messagesPerThread; i++) {
                        BacktestResponse response = createSuccessResponse();
                        response.setBacktestId((long) (threadId * messagesPerThread + i + 1));
                        backtestKafkaProducer.sendBacktestResult(response);
                    }
                });
                threads.add(thread);
                thread.start();
            }

            // 모든 스레드 완료 대기
            for (Thread thread : threads) {
                thread.join();
            }

            // then
            ConsumerRecords<String, BacktestResponse> records = 
                consumer.poll(Duration.ofSeconds(10));
            
            assertThat(records.count()).isGreaterThan(0);
            assertThat(records.count()).isLessThanOrEqualTo(threadCount * messagesPerThread);
        }
    }

    // ===== Helper Methods =====

    private BacktestResponse createSuccessResponse() {
        BacktestSummaryDto summary = new BacktestSummaryDto();
        summary.setFinalValue(11000000.0);
        summary.setTotalReturn(0.1);
        summary.setBuyHoldReturn(0.08);
        summary.setRebalancingCount(5);
        summary.setTotalFee(15000.0);
        summary.setMaxDrawdown(-0.05);
        summary.setVolatility(0.15);
        summary.setSharpeRatio(0.8);

        List<BacktestDetailDto> details = Arrays.asList(
            createDetailDto(LocalDate.of(2023, 1, 2), 10000000.0, 0.0, false),
            createDetailDto(LocalDate.of(2023, 1, 15), 10500000.0, 0.05, true),
            createDetailDto(LocalDate.of(2023, 1, 31), 11000000.0, 0.048, false)
        );

        return BacktestResponse.success(1L, summary, details, 1500L);
    }

    private BacktestResponse createDetailedSuccessResponse() {
        BacktestSummaryDto summary = new BacktestSummaryDto();
        summary.setFinalValue(12500000.0);
        summary.setTotalReturn(0.25);
        summary.setBuyHoldReturn(0.20);
        summary.setRebalancingCount(8);
        summary.setTotalFee(25000.0);
        summary.setTotalBorrowingCost(5000.0);
        summary.setMaxBorrowingAmount(500000.0);
        summary.setMinCashBalance(-300000.0);
        summary.setMaxDrawdown(-0.08);
        summary.setVolatility(0.18);
        summary.setSharpeRatio(1.2);
        summary.setPeriodGrowthRate(0.025);
        summary.setTimeWeightedReturn(0.23);

        List<BacktestDetailDto> details = Arrays.asList(
            createDetailDto(LocalDate.of(2023, 1, 2), 10000000.0, 0.0, false),
            createDetailDto(LocalDate.of(2023, 1, 15), 10800000.0, 0.08, true),
            createDetailDto(LocalDate.of(2023, 1, 31), 12500000.0, 0.157, true)
        );

        return BacktestResponse.success(2L, summary, details, 2300L);
    }

    private BacktestResponse createLargeDataResponse() {
        BacktestSummaryDto summary = new BacktestSummaryDto();
        summary.setFinalValue(15000000.0);
        summary.setTotalReturn(0.5);
        summary.setBuyHoldReturn(0.4);
        summary.setRebalancingCount(50);
        summary.setTotalFee(75000.0);

        List<BacktestDetailDto> details = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2023, 1, 2);
        
        for (int i = 0; i < 1000; i++) {
            LocalDate date = startDate.plusDays(i);
            double value = 10000000.0 + (i * 5000);
            double periodReturn = i == 0 ? 0.0 : 0.0005 * (1 + Math.sin(i * 0.1));
            boolean isRebalanced = i % 20 == 0 && i > 0;
            
            details.add(createDetailDto(date, value, periodReturn, isRebalanced));
        }

        return BacktestResponse.success(3L, summary, details, 5000L);
    }

    private BacktestDetailDto createDetailDto(LocalDate date, double portfolioValue, 
                                            double periodReturn, boolean isRebalanced) {
        BacktestDetailDto detail = new BacktestDetailDto();
        detail.setPeriodDate(date);
        detail.setPortfolioValue(portfolioValue);
        detail.setPeriodReturn(periodReturn);
        detail.setIsRebalanced(isRebalanced);
        detail.setCashBalance(portfolioValue * 0.05); // 5% 현금 가정
        detail.setDailyBorrowingInterest(0.0);
        detail.setCumulativeReturn((portfolioValue - 10000000.0) / 10000000.0);
        detail.setTotalBuyAmount(isRebalanced ? 1000000.0 : 0.0);
        detail.setTotalSellAmount(isRebalanced ? 800000.0 : 0.0);
        return detail;
    }
}