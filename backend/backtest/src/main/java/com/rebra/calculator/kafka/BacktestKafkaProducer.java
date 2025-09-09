package com.rebra.calculator.kafka;

import com.rebra.calculator.dto.BacktestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 백테스트 결과를 전송하는 Kafka 프로듀서 클래스
 * 계산 완료된 백테스트 결과를 메인 서버로 전송한다.
 * 
 * 주요 기능:
 * - 백테스트 계산 결과 메시지 전송
 * - 전송 성공/실패 처리
 * - 재시도 로직 (Kafka 설정 기반)
 * - 전송 통계 및 모니터링
 * - 비동기 전송 지원
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BacktestKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 백테스트 결과 토픽명
     */
    @Value("${kafka.topics.backtest-result}")
    private String backtestResultTopic;

    /**
     * 전송 통계를 위한 카운터
     */
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final AtomicLong totalSentBytes = new AtomicLong(0);

    /**
     * 백테스트 결과를 메인 서버로 전송한다
     * 
     * @param response 전송할 백테스트 결과
     * @throws RuntimeException 전송 실패 시
     */
    public void sendBacktestResult(BacktestResponse response) {
        if (response == null) {
            log.error("전송할 백테스트 결과가 null입니다");
            throw new IllegalArgumentException("백테스트 결과가 null입니다");
        }

        if (response.getBacktestId() == null) {
            log.error("백테스트 ID가 null입니다");
            throw new IllegalArgumentException("백테스트 ID가 null입니다");
        }

        long sendStartTime = System.currentTimeMillis();
        String messageKey = generateMessageKey(response);

        try {
            log.debug("백테스트 결과 전송 시작 - ID: {}, 상태: {}, 토픽: {}", 
                    response.getBacktestId(), response.getStatus(), backtestResultTopic);

            // 비동기 전송
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(backtestResultTopic, messageKey, response);

            // 전송 결과 처리
            future.thenAccept(result -> {
                long sendTime = System.currentTimeMillis() - sendStartTime;
                handleSendSuccess(response, result, sendTime);
            }).exceptionally(ex -> {
                long sendTime = System.currentTimeMillis() - sendStartTime;
                handleSendFailure(response, ex, sendTime);
                return null;
            });

            log.debug("백테스트 결과 전송 요청 완료 - ID: {}", response.getBacktestId());

        } catch (Exception e) {
            long sendTime = System.currentTimeMillis() - sendStartTime;
            log.error("백테스트 결과 전송 중 예외 발생 - ID: {}, 소요시간: {}ms", 
                    response.getBacktestId(), sendTime, e);
            
            failureCount.incrementAndGet();
            throw new RuntimeException("백테스트 결과 전송 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 백테스트 결과를 동기적으로 전송한다
     * 테스트나 특별한 경우에 사용
     * 
     * @param response 전송할 백테스트 결과
     * @return 전송 결과
     * @throws RuntimeException 전송 실패 시
     */
    public SendResult<String, Object> sendBacktestResultSync(BacktestResponse response) {
        if (response == null || response.getBacktestId() == null) {
            throw new IllegalArgumentException("유효하지 않은 백테스트 결과");
        }

        long sendStartTime = System.currentTimeMillis();
        String messageKey = generateMessageKey(response);

        try {
            log.debug("백테스트 결과 동기 전송 시작 - ID: {}", response.getBacktestId());

            SendResult<String, Object> result = kafkaTemplate.send(
                backtestResultTopic, messageKey, response).get();

            long sendTime = System.currentTimeMillis() - sendStartTime;
            handleSendSuccess(response, result, sendTime);

            return result;

        } catch (Exception e) {
            long sendTime = System.currentTimeMillis() - sendStartTime;
            log.error("백테스트 결과 동기 전송 실패 - ID: {}, 소요시간: {}ms", 
                    response.getBacktestId(), sendTime, e);
            
            failureCount.incrementAndGet();
            throw new RuntimeException("백테스트 결과 동기 전송 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 메시지 키를 생성한다
     * 같은 백테스트 ID는 같은 파티션으로 전송되도록 함
     * 
     * @param response 백테스트 결과
     * @return 메시지 키
     */
    private String generateMessageKey(BacktestResponse response) {
        // 백테스트 ID를 키로 사용하여 같은 ID는 같은 파티션으로 전송
        return String.format("backtest_%d", response.getBacktestId());
    }

    /**
     * 전송 성공 처리
     * 
     * @param response 전송된 백테스트 결과
     * @param result 전송 결과
     * @param sendTime 전송 소요 시간
     */
    private void handleSendSuccess(BacktestResponse response, SendResult<String, Object> result, long sendTime) {
        successCount.incrementAndGet();
        
        // 메시지 크기 추정 (정확하지 않지만 모니터링 목적)
        long estimatedSize = estimateMessageSize(response);
        totalSentBytes.addAndGet(estimatedSize);

        log.info("백테스트 결과 전송 성공 - ID: {}, 파티션: {}, 오프셋: {}, " +
                "전송시간: {}ms, 예상크기: {}bytes, 상태: {}",
                response.getBacktestId(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset(),
                sendTime,
                estimatedSize,
                response.getStatus());

        // 성공률 로깅 (100건마다)
        if (successCount.get() % 100 == 0) {
            logTransmissionStatistics();
        }
    }

    /**
     * 전송 실패 처리
     * 
     * @param response 전송 실패한 백테스트 결과
     * @param throwable 발생한 예외
     * @param sendTime 전송 시도 소요 시간
     */
    private void handleSendFailure(BacktestResponse response, Throwable throwable, long sendTime) {
        failureCount.incrementAndGet();

        log.error("백테스트 결과 전송 실패 - ID: {}, 시도시간: {}ms, " +
                "성공: {}, 실패: {}, 성공률: {:.2f}%",
                response.getBacktestId(),
                sendTime,
                successCount.get(),
                failureCount.get(),
                getSuccessRate(),
                throwable);

        // 실패 시 추가 로직 (알림, 재시도 등)이 필요하다면 여기에 구현
        // 현재는 Kafka의 기본 재시도 메커니즘에 의존
    }

    /**
     * 메시지 크기를 추정한다
     * 정확한 직렬화 크기는 아니지만 모니터링 목적으로 사용
     * 
     * @param response 백테스트 결과
     * @return 예상 메시지 크기 (bytes)
     */
    private long estimateMessageSize(BacktestResponse response) {
        long size = 1000; // 기본 메타데이터 크기

        // 상세 기록 크기 추정
        if (response.getDetails() != null) {
            size += response.getDetails().size() * 200; // 상세 기록당 약 200bytes
        }

        // 거래 기록 크기 추정
        if (response.getRebalancingHistory() != null) {
            size += response.getRebalancingHistory().size() * 300; // 거래 기록당 약 300bytes
        }

        // 차입 기록은 제거됨

        return size;
    }

    /**
     * 전송 통계를 로깅한다
     */
    public void logTransmissionStatistics() {
        long total = successCount.get() + failureCount.get();
        
        log.info("백테스트 결과 전송 통계 - " +
                "성공: {}, 실패: {}, 총전송: {}, 성공률: {:.2f}%, " +
                "총전송량: {}KB",
                successCount.get(),
                failureCount.get(),
                total,
                getSuccessRate(),
                totalSentBytes.get() / 1024);
    }

    /**
     * 전송 성공률을 계산한다
     * 
     * @return 성공률 (0-100)
     */
    public double getSuccessRate() {
        long total = successCount.get() + failureCount.get();
        if (total == 0) return 0.0;
        return (double) successCount.get() / total * 100.0;
    }

    /**
     * 전송 성공 건수를 반환한다
     * 
     * @return 성공 건수
     */
    public long getSuccessCount() {
        return successCount.get();
    }

    /**
     * 전송 실패 건수를 반환한다
     * 
     * @return 실패 건수
     */
    public long getFailureCount() {
        return failureCount.get();
    }

    /**
     * 총 전송량을 반환한다
     * 
     * @return 총 전송량 (bytes)
     */
    public long getTotalSentBytes() {
        return totalSentBytes.get();
    }

    /**
     * 전송 통계를 초기화한다
     */
    public void resetStatistics() {
        successCount.set(0);
        failureCount.set(0);
        totalSentBytes.set(0);
        log.info("백테스트 결과 전송 통계가 초기화되었습니다");
    }

    /**
     * Kafka 템플릿의 상태를 확인한다
     * 
     * @return Kafka 연결 상태
     */
    public boolean isKafkaAvailable() {
        try {
            // Kafka 클러스터 메타데이터 조회를 통한 연결 상태 확인
            kafkaTemplate.getProducerFactory().createProducer().partitionsFor(backtestResultTopic);
            return true;
        } catch (Exception e) {
            log.warn("Kafka 연결 상태 확인 실패", e);
            return false;
        }
    }

    /**
     * 프로듀서 설정 정보를 로깅한다
     */
    public void logProducerConfiguration() {
        log.info("백테스트 Kafka 프로듀서 설정 - " +
                "결과토픽: {}, Kafka연결상태: {}",
                backtestResultTopic,
                isKafkaAvailable() ? "연결됨" : "연결안됨");
    }
}