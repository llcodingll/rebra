package com.rebra.rebalance.infrastructure.kafka;

import com.rebra.rebalance.application.rebalancing.RebalancingApplicationService;
import com.rebra.rebalance.application.rebalancing.dto.RebalancingOrderCommand;
import com.rebra.rebalance.exception.IdempotencyViolationException;
import com.rebra.rebalance.exception.RebalancingCutoffException;
import com.rebra.rebalance.exception.RebalancingException;
import com.rebra.rebalance.infrastructure.kafka.dto.RebalancingOrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RebalancingOrderConsumer {

    private final RebalancingApplicationService applicationService;

    @KafkaListener(
            topics = "${kafka.topics.rebalancing-orders}",
            groupId = "rebalance-executor",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload RebalancingOrderMessage message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("리밸런싱 메시지 수신 jobId={} portfolioId={} partition={} offset={}",
                message.getJobId(), message.getPortfolioId(), partition, offset);

        try {
            applicationService.execute(RebalancingOrderCommand.from(message));
        } catch (IdempotencyViolationException e) {
            log.info("중복 consume 스킵 jobId={}", message.getJobId());
        } catch (RebalancingCutoffException e) {
            log.warn("15:30 cutoff 초과 jobId={}", message.getJobId());
        } catch (RebalancingException e) {
            log.error("리밸런싱 실패 jobId={} code={} msg={}",
                    message.getJobId(), e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 오류 jobId={}", message.getJobId(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
