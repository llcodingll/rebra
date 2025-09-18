package com.rebra.calculator.kafka;

import com.rebra.calculator.dto.BacktestRequest;
import com.rebra.calculator.dto.BacktestResponse;
import com.rebra.calculator.enums.BacktestStatus;
import com.rebra.calculator.service.BacktestCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 백테스트 요청을 처리하는 Kafka 리스너 클래스
 * 메인 서버로부터 전달되는 백테스트 계산 요청을 수신하고 처리한다.
 * 
 * 주요 기능:
 * - 백테스트 요청 메시지 수신
 * - 비동기 백테스트 계산 처리
 * - 계산 완료 후 결과 전송
 * - 오류 처리 및 로깅
 * - 메시지 처리 확인(ACK) 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BacktestKafkaListener {

    private final BacktestCalculatorService backtestCalculatorService;
    private final BacktestKafkaProducer backtestKafkaProducer;
    
    /**
     * 백테스트 계산을 위한 전용 스레드 풀
     * CPU 집약적인 계산 작업을 위해 별도의 스레드 풀 사용
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(), 
            r -> {
                Thread thread = new Thread(r);
                thread.setName("backtest-calculator-" + thread.threadId());
                thread.setDaemon(false);
                return thread;
            });

    /**
     * 백테스트 요청 메시지를 처리한다
     * 
     * @param request 백테스트 계산 요청
     * @param partition 메시지가 수신된 파티션
     * @param offset 메시지 오프셋
     * @param timestamp 메시지 타임스탬프
     * @param acknowledgment 메시지 처리 확인
     */
    @KafkaListener(
        topics = "${kafka.topics.backtest-request}",
        groupId = "backtest-calculator",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleBacktestRequest(
            @Payload BacktestRequest request,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {

        long messageReceivedTime = System.currentTimeMillis();
        
        try {
            log.info("백테스트 요청 수신 - ID: {}, Partition: {}, Offset: {}, 지연시간: {}ms",
                    request.getBacktestId(), partition, offset, 
                    messageReceivedTime - timestamp);

            // 요청 기본 검증
            if (!isValidRequest(request)) {
                log.error("잘못된 백테스트 요청 - ID: {}", request.getBacktestId());
                sendErrorResponse(request.getBacktestId(), "잘못된 요청 데이터", messageReceivedTime);
                acknowledgment.acknowledge();
                return;
            }

            // 비동기 백테스트 처리
            CompletableFuture.supplyAsync(() -> {
                try {
                    log.info("백테스트 계산 시작 - ID: {}, Thread: {}", 
                            request.getBacktestId(), Thread.currentThread().getName());

                    // 백테스트 실행
                    BacktestResponse response = backtestCalculatorService.executeBacktest(request);
                    
                    log.info("백테스트 계산 완료 - ID: {}, 상태: {}", 
                            request.getBacktestId(), response.getStatus());
                    
                    return response;

                } catch (Exception e) {
                    log.error("백테스트 계산 중 오류 발생 - ID: {}", request.getBacktestId(), e);
                    
                    long calculationTime = System.currentTimeMillis() - messageReceivedTime;
                    return BacktestResponse.failure(request.getBacktestId(), 
                            "계산 중 오류 발생: " + e.getMessage(), calculationTime);
                }
            }, executorService)
            .thenAccept(response -> {
                try {
                    // 결과 전송
                    backtestKafkaProducer.sendBacktestResult(response);
                    
                    log.info("백테스트 결과 전송 완료 - ID: {}, 총 소요시간: {}ms",
                            response.getBacktestId(), 
                            System.currentTimeMillis() - messageReceivedTime);

                } catch (Exception e) {
                    log.error("백테스트 결과 전송 중 오류 발생 - ID: {}", response.getBacktestId(), e);
                }
            })
            .exceptionally(throwable -> {
                log.error("백테스트 처리 중 예외 발생 - ID: {}", request.getBacktestId(), throwable);
                
                // 예외 발생 시에도 오류 응답 전송
                sendErrorResponse(request.getBacktestId(), 
                        "처리 중 예외 발생: " + throwable.getMessage(), messageReceivedTime);
                return null;
            });

            // 메시지 수신 확인 (비동기 처리 시작 후 즉시 ACK)
            acknowledgment.acknowledge();
            
            log.debug("백테스트 요청 처리 시작됨 - ID: {}, 비동기 처리 중", request.getBacktestId());

        } catch (Exception e) {
            log.error("백테스트 요청 처리 중 오류 발생 - ID: {}", 
                    request != null ? request.getBacktestId() : "UNKNOWN", e);
            
            // 동기 오류 발생 시 오류 응답 전송
            if (request != null) {
                sendErrorResponse(request.getBacktestId(), 
                        "요청 처리 중 오류 발생: " + e.getMessage(), messageReceivedTime);
            }
            
            // 오류 발생 시에도 ACK (메시지 재처리 방지)
            acknowledgment.acknowledge();
        }
    }

    /**
     * 백테스트 요청의 기본 유효성을 검사한다
     * 
     * @param request 백테스트 요청
     * @return 유효한 요청이면 true
     */
    private boolean isValidRequest(BacktestRequest request) {
        if (request == null) {
            log.error("백테스트 요청이 null입니다");
            return false;
        }

        if (request.getBacktestId() == null || request.getBacktestId() <= 0) {
            log.error("백테스트 ID가 유효하지 않습니다: {}", request.getBacktestId());
            return false;
        }

        if (request.getStocks() == null || request.getStocks().isEmpty()) {
            log.error("종목 목록이 비어있습니다");
            return false;
        }
        
        // 초기 보유 주식 검증
        boolean hasValidHoldings = request.getStocks().stream()
                .anyMatch(stock -> stock.getSafeShares() > 0);
        
        if (!hasValidHoldings) {
            log.error("유효한 초기 보유 주식이 없습니다");
            return false;
        }

        if (request.getDailyPrices() == null || request.getDailyPrices().isEmpty()) {
            log.error("일별 가격 데이터가 비어있습니다");
            return false;
        }

        return true;
    }

    /**
     * 오류 응답을 전송한다
     * 
     * @param backtestId 백테스트 ID
     * @param errorMessage 오류 메시지
     * @param startTime 처리 시작 시간
     */
    private void sendErrorResponse(Long backtestId, String errorMessage, long startTime) {
        try {
            long calculationTime = System.currentTimeMillis() - startTime;
            BacktestResponse errorResponse = BacktestResponse.failure(backtestId, errorMessage, calculationTime);
            
            backtestKafkaProducer.sendBacktestResult(errorResponse);
            
            log.info("오류 응답 전송 완료 - ID: {}, 오류: {}", backtestId, errorMessage);

        } catch (Exception e) {
            log.error("오류 응답 전송 중 추가 오류 발생 - ID: {}", backtestId, e);
        }
    }

    /**
     * 스레드 풀 상태를 로깅한다
     * 모니터링 및 디버깅 목적
     */
    public void logThreadPoolStatus() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
            
            log.info("백테스트 계산 스레드 풀 상태 - " +
                    "활성스레드: {}, 풀크기: {}, 최대풀크기: {}, " +
                    "대기중작업: {}, 완료된작업: {}, 총제출작업: {}",
                    tpe.getActiveCount(),
                    tpe.getPoolSize(),
                    tpe.getMaximumPoolSize(),
                    tpe.getQueue().size(),
                    tpe.getCompletedTaskCount(),
                    tpe.getTaskCount());
        }
    }

    /**
     * 현재 처리 중인 백테스트 요청 수를 반환한다
     * 
     * @return 처리 중인 요청 수
     */
    public int getActiveTaskCount() {
        if (executorService instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executorService).getActiveCount();
        }
        return 0;
    }

    /**
     * 대기 중인 백테스트 요청 수를 반환한다
     * 
     * @return 대기 중인 요청 수
     */
    public int getQueuedTaskCount() {
        if (executorService instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executorService).getQueue().size();
        }
        return 0;
    }

    /**
     * 리스너 종료 시 리소스 정리
     */
    @jakarta.annotation.PreDestroy
    public void shutdown() {
        log.info("백테스트 Kafka 리스너 종료 중...");
        
        executorService.shutdown();
        
        try {
            if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                log.warn("백테스트 계산 스레드 풀이 정상 종료되지 않아 강제 종료합니다");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("백테스트 계산 스레드 풀 종료 중 인터럽트 발생", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("백테스트 Kafka 리스너 종료 완료");
    }
}