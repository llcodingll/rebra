package com.rebra.calculator.kafka;

import com.rebra.calculator.dto.BacktestRequest;
import com.rebra.calculator.dto.BacktestResponse;
import com.rebra.calculator.dto.BacktestStockDto;
import com.rebra.calculator.enums.BacktestStatus;
import com.rebra.calculator.enums.RebalancingType;
import com.rebra.calculator.service.BacktestCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BacktestKafkaListener 단위 테스트
 * Kafka 메시지 수신 및 처리 로직을 검증한다.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"backtest-request", "backtest-result"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BacktestKafkaListenerTest {

    @MockitoSpyBean
    private BacktestKafkaListener backtestKafkaListener;

    @MockitoBean
    private BacktestCalculatorService backtestCalculatorService;

    @MockitoBean
    private BacktestKafkaProducer backtestKafkaProducer;

    private BacktestRequest validRequest;
    private Acknowledgment mockAcknowledgment;

    @BeforeEach
    void setUp() {
        validRequest = createValidBacktestRequest();
        mockAcknowledgment = mock(Acknowledgment.class);
    }

    @Nested
    @DisplayName("정상적인 백테스트 요청 처리")
    class SuccessfulRequestHandling {

        @Test
        @DisplayName("유효한 백테스트 요청 처리")
        void handleValidBacktestRequest() throws InterruptedException {
            // given
            BacktestResponse successResponse = BacktestResponse.success(
                validRequest.getBacktestId(),
                mock(com.rebra.calculator.dto.BacktestSummaryDto.class),
                Collections.emptyList(),
                1000L
            );
            
            when(backtestCalculatorService.executeBacktest(any(BacktestRequest.class)))
                .thenReturn(successResponse);

            // when
            backtestKafkaListener.handleBacktestRequest(
                validRequest, 0, 100L, System.currentTimeMillis(), mockAcknowledgment);

            // then
            // 비동기 처리를 위한 대기
            Thread.sleep(1000);

            verify(backtestCalculatorService, times(1)).executeBacktest(validRequest);
            verify(backtestKafkaProducer, times(1)).sendBacktestResult(successResponse);
            verify(mockAcknowledgment, times(1)).acknowledge();
        }

        @Test
        @DisplayName("백테스트 계산 성공 후 결과 전송")
        void successfulCalculationAndResultSending() throws InterruptedException {
            // given
            BacktestResponse successResponse = BacktestResponse.success(
                validRequest.getBacktestId(),
                mock(com.rebra.calculator.dto.BacktestSummaryDto.class),
                Collections.emptyList(),
                1500L
            );
            
            when(backtestCalculatorService.executeBacktest(any(BacktestRequest.class)))
                .thenReturn(successResponse);

            // when
            backtestKafkaListener.handleBacktestRequest(
                validRequest, 0, 200L, System.currentTimeMillis(), mockAcknowledgment);

            // then
            Thread.sleep(1000);

            ArgumentCaptor<BacktestResponse> responseCaptor = ArgumentCaptor.forClass(BacktestResponse.class);
            verify(backtestKafkaProducer).sendBacktestResult(responseCaptor.capture());
            
            BacktestResponse capturedResponse = responseCaptor.getValue();
            assertThat(capturedResponse.getBacktestId()).isEqualTo(validRequest.getBacktestId());
            assertThat(capturedResponse.getStatus()).isEqualTo(BacktestStatus.COMPLETED);
        }

        @Test
        @DisplayName("동시 다중 요청 처리")
        void handleMultipleRequestsConcurrently() throws InterruptedException {
            // given
            int requestCount = 3;
            List<BacktestRequest> requests = new ArrayList<>();
            
            for (int i = 1; i <= requestCount; i++) {
                BacktestRequest request = createValidBacktestRequest();
                request.setBacktestId((long) i);
                requests.add(request);
            }

            when(backtestCalculatorService.executeBacktest(any(BacktestRequest.class)))
                .thenAnswer(invocation -> {
                    BacktestRequest req = invocation.getArgument(0);
                    return BacktestResponse.success(req.getBacktestId(), 
                        mock(com.rebra.calculator.dto.BacktestSummaryDto.class),
                        Collections.emptyList(), 800L);
                });

            // when
            for (BacktestRequest request : requests) {
                backtestKafkaListener.handleBacktestRequest(
                    request, 0, 300L, System.currentTimeMillis(), mockAcknowledgment);
            }

            // then
            Thread.sleep(2000); // 모든 비동기 작업 완료 대기

            verify(backtestCalculatorService, times(requestCount)).executeBacktest(any(BacktestRequest.class));
            verify(backtestKafkaProducer, times(requestCount)).sendBacktestResult(any(BacktestResponse.class));
            verify(mockAcknowledgment, times(requestCount)).acknowledge();
        }
    }

    @Nested
    @DisplayName("요청 검증 및 에러 처리")
    class RequestValidationAndErrorHandling {

        @Test
        @DisplayName("null 요청 처리")
        void handleNullRequest() {
            // when
            backtestKafkaListener.handleBacktestRequest(
                null, 0, 400L, System.currentTimeMillis(), mockAcknowledgment);

            // then
            verify(backtestCalculatorService, never()).executeBacktest(any());
            verify(mockAcknowledgment, times(1)).acknowledge();
            verify(backtestKafkaProducer, times(1)).sendBacktestResult(any(BacktestResponse.class));
        }

        @Test
        @DisplayName("잘못된 백테스트 ID")
        void handleInvalidBacktestId() {
            // given
            validRequest.setBacktestId(null);

            // when
            backtestKafkaListener.handleBacktestRequest(
                validRequest, 0, 500L, System.currentTimeMillis(), mockAcknowledgment);

            // then
            verify(backtestCalculatorService, never()).executeBacktest(any());
            verify(mockAcknowledgment, times(1)).acknowledge();
            
            ArgumentCaptor<BacktestResponse> responseCaptor = ArgumentCaptor.forClass(BacktestResponse.class);
            verify(backtestKafkaProducer).sendBacktestResult(responseCaptor.capture());
            
            BacktestResponse errorResponse = responseCaptor.getValue();
            assertThat(errorResponse.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(errorResponse.getErrorMessage()).contains("잘못된 요청 데이터");
        }

        @Test
        @DisplayName("빈 종목 목록")
        void handleEmptyStockList() {
            // given
            validRequest.setStocks(Collections.emptyList());

            // when
            backtestKafkaListener.handleBacktestRequest(
                validRequest, 0, 600L, System.currentTimeMillis(), mockAcknowledgment);

            // then
            verify(backtestCalculatorService, never()).executeBacktest(any());
            verify(mockAcknowledgment, times(1)).acknowledge();
            
            ArgumentCaptor<BacktestResponse> responseCaptor = ArgumentCaptor.forClass(BacktestResponse.class);
            verify(backtestKafkaProducer).sendBacktestResult(responseCaptor.capture());
            
            BacktestResponse errorResponse = responseCaptor.getValue();
            assertThat(errorResponse.getStatus()).isEqualTo(BacktestStatus.FAILED);
        }

        @Test
        @DisplayName("초기 보유 주식 없음")
        void handleNoInitialHoldings() {
            // given - 모든 종목의 보유량을 0으로 설정
            validRequest.getStocks().forEach(stock -> stock.setShares(0));

            // when
            backtestKafkaListener.handleBacktestRequest(
                validRequest, 0, 700L, System.currentTimeMillis(), mockAcknowledgment);

            // then
            verify(backtestCalculatorService, never()).executeBacktest(any());
            verify(mockAcknowledgment, times(1)).acknowledge();
        }

        @Test
        @DisplayName("가격 데이터 없음")
        void handleMissingPriceData() {
            // given
            validRequest.setDailyPrices(Collections.emptyMap());

            // when
            backtestKafkaListener.handleBacktestRequest(
                validRequest, 0, 800L, System.currentTimeMillis(), mockAcknowledgment);

            // then
            verify(backtestCalculatorService, never()).executeBacktest(any());
            verify(mockAcknowledgment, times(1)).acknowledge();
        }
    }

    @Nested
    @DisplayName("백테스트 실행 중 에러 처리")
    class CalculationErrorHandling {

        @Test
        @DisplayName("백테스트 계산 중 예외 발생")
        void handleCalculationException() throws InterruptedException {
            // given
            when(backtestCalculatorService.executeBacktest(any(BacktestRequest.class)))
                .thenThrow(new RuntimeException("계산 중 오류 발생"));

            // when
            backtestKafkaListener.handleBacktestRequest(
                validRequest, 0, 900L, System.currentTimeMillis(), mockAcknowledgment);

            // then
            Thread.sleep(1000);

            verify(backtestCalculatorService, times(1)).executeBacktest(validRequest);
            verify(mockAcknowledgment, times(1)).acknowledge();
            
            ArgumentCaptor<BacktestResponse> responseCaptor = ArgumentCaptor.forClass(BacktestResponse.class);
            verify(backtestKafkaProducer).sendBacktestResult(responseCaptor.capture());
            
            BacktestResponse errorResponse = responseCaptor.getValue();
            assertThat(errorResponse.getStatus()).isEqualTo(BacktestStatus.FAILED);
            assertThat(errorResponse.getErrorMessage()).contains("계산 중 오류 발생");
        }

        @Test
        @DisplayName("결과 전송 중 예외 발생")
        void handleResultSendingException() throws InterruptedException {
            // given
            BacktestResponse successResponse = BacktestResponse.success(
                validRequest.getBacktestId(),
                mock(com.rebra.calculator.dto.BacktestSummaryDto.class),
                Collections.emptyList(),
                1200L
            );
            
            when(backtestCalculatorService.executeBacktest(any(BacktestRequest.class)))
                .thenReturn(successResponse);
            
            doThrow(new RuntimeException("결과 전송 실패"))
                .when(backtestKafkaProducer).sendBacktestResult(any(BacktestResponse.class));

            // when
            backtestKafkaListener.handleBacktestRequest(
                validRequest, 0, 1000L, System.currentTimeMillis(), mockAcknowledgment);

            // then
            Thread.sleep(1000);

            verify(backtestCalculatorService, times(1)).executeBacktest(validRequest);
            verify(backtestKafkaProducer, times(1)).sendBacktestResult(successResponse);
            verify(mockAcknowledgment, times(1)).acknowledge();
        }

        @Test
        @DisplayName("비동기 처리 중 예외 발생")
        void handleAsynchronousException() throws InterruptedException {
            // given
            when(backtestCalculatorService.executeBacktest(any(BacktestRequest.class)))
                .thenAnswer(invocation -> {
                    Thread.sleep(100); // 짧은 지연
                    throw new RuntimeException("비동기 처리 중 오류");
                });

            // when
            backtestKafkaListener.handleBacktestRequest(
                validRequest, 0, 1100L, System.currentTimeMillis(), mockAcknowledgment);

            // then
            Thread.sleep(1000);

            verify(backtestCalculatorService, times(1)).executeBacktest(validRequest);
            verify(mockAcknowledgment, times(1)).acknowledge();
            
            // 예외 발생 시에도 에러 응답 전송
            ArgumentCaptor<BacktestResponse> responseCaptor = ArgumentCaptor.forClass(BacktestResponse.class);
            verify(backtestKafkaProducer, atLeastOnce()).sendBacktestResult(responseCaptor.capture());
        }
    }

    @Nested
    @DisplayName("스레드 풀 관리 테스트")
    class ThreadPoolManagementTest {

        @Test
        @DisplayName("활성 작업 수 조회")
        void getActiveTaskCount() {
            // when
            int activeCount = backtestKafkaListener.getActiveTaskCount();

            // then
            assertThat(activeCount).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("대기 중인 작업 수 조회")
        void getQueuedTaskCount() {
            // when
            int queuedCount = backtestKafkaListener.getQueuedTaskCount();

            // then
            assertThat(queuedCount).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("스레드 풀 상태 로깅")
        void logThreadPoolStatus() {
            // when & then
            assertThatCode(() -> backtestKafkaListener.logThreadPoolStatus())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("다중 요청 처리 시 스레드 풀 상태")
        void threadPoolStatusWithMultipleRequests() throws InterruptedException {
            // given
            when(backtestCalculatorService.executeBacktest(any(BacktestRequest.class)))
                .thenAnswer(invocation -> {
                    Thread.sleep(500); // 처리 시간 시뮬레이션
                    return BacktestResponse.success(1L, 
                        mock(com.rebra.calculator.dto.BacktestSummaryDto.class),
                        Collections.emptyList(), 500L);
                });

            // when
            for (int i = 0; i < 3; i++) {
                BacktestRequest request = createValidBacktestRequest();
                request.setBacktestId((long) i + 1);
                backtestKafkaListener.handleBacktestRequest(
                    request, 0, 1200L + i, System.currentTimeMillis(), mockAcknowledgment);
            }

            // 잠시 대기 후 스레드 풀 상태 확인
            Thread.sleep(100);
            int activeCount = backtestKafkaListener.getActiveTaskCount();

            // then
            assertThat(activeCount).isGreaterThan(0); // 처리 중인 작업이 있어야 함

            // 모든 작업 완료 대기
            Thread.sleep(2000);
        }
    }

    // ===== Helper Methods =====

    private BacktestRequest createValidBacktestRequest() {
        BacktestRequest request = new BacktestRequest();
        request.setBacktestId(1L);
        request.setStartDate(LocalDate.of(2023, 1, 2));
        request.setEndDate(LocalDate.of(2023, 1, 31));
        request.setRebalancingType(RebalancingType.THRESHOLD);
        
        // 종목 설정
        List<BacktestStockDto> stocks = Arrays.asList(
            createBacktestStock("005930", "삼성전자", 40, 100),
            createBacktestStock("000660", "SK하이닉스", 30, 50),
            createBacktestStock("035420", "NAVER", 30, 25)
        );
        request.setStocks(stocks);
        
        // 가격 데이터 설정
        Map<String, Map<String, Double>> priceData = new HashMap<>();
        Map<String, Double> dayPrices = Map.of(
            "005930", 50000.0,
            "000660", 80000.0,
            "035420", 200000.0
        );
        priceData.put("2023-01-02", dayPrices);
        priceData.put("2023-01-31", dayPrices);
        request.setDailyPrices(priceData);
        
        return request;
    }

    private BacktestStockDto createBacktestStock(String stockCode, String stockName, 
                                               int weight, int shares) {
        BacktestStockDto stock = new BacktestStockDto();
        stock.setStockCode(stockCode);
        stock.setWeight(weight);
        stock.setShares(shares);
        stock.setThresholdPercentage(5.0);
        return stock;
    }
}