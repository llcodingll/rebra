package com.rebra.component;

import com.rebra.entity.OutboxEvent;
import com.rebra.entity.OutboxStatus;
import com.rebra.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventRelay {

    private final OutboxEventRepository outboxEventRepository;

    @Qualifier("rebalancingOrdersKafkaTemplate")
    private final KafkaTemplate<String, Object> rebalancingOrdersKafkaTemplate;

    @Value("${kafka.topics.rebalancing-orders:rebalancing-orders}")
    private String rebalancingOrdersTopic;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void relay() {
        List<OutboxEvent> events = outboxEventRepository
                .findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (events.isEmpty()) return;

        for (OutboxEvent event : events) {
            try {
                rebalancingOrdersKafkaTemplate
                        .send(rebalancingOrdersTopic,
                                event.getPortfolioId().toString(),
                                event.getPayload())
                        .get(5, TimeUnit.SECONDS);
                event.markPublished();
                log.info("Outbox 발행 완료 outboxId={} portfolioId={}",
                        event.getId(), event.getPortfolioId());
            } catch (Exception e) {
                log.error("Outbox 발행 실패 outboxId={}", event.getId(), e);
                event.markFailed();
            }
        }
    }
}
