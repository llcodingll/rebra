package com.rebra.rebalance.infrastructure.kafka;

import com.rebra.rebalance.application.rebalancing.dto.RebalancingResultEvent;
import com.rebra.rebalance.exception.ErrorCode;
import com.rebra.rebalance.exception.RebalancingException;
import com.rebra.rebalance.infrastructure.kafka.dto.RebalancingResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RebalancingResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.rebalancing-results}")
    private String topic;

    // 동기 전송 (완료 시, 10초 타임아웃)
    public void sendSync(RebalancingResultEvent event) {
        try {
            kafkaTemplate.send(topic,
                    event.getPortfolioId().toString(),
                    RebalancingResultMessage.from(event))
                    .get(10, TimeUnit.SECONDS);
            log.info("결과 produce 완료 jobId={} status={}", event.getJobId(), event.getStatus());
        } catch (Exception e) {
            log.error("결과 produce 실패 jobId={}", event.getJobId(), e);
            throw new RebalancingException(ErrorCode.KIS_API_ERROR, "결과 produce 실패");
        }
    }

    // 비동기 전송 (실패·cutoff 등 빠른 처리)
    public void send(RebalancingResultEvent event) {
        kafkaTemplate.send(topic,
                event.getPortfolioId().toString(),
                RebalancingResultMessage.from(event));
    }
}
