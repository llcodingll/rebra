package com.rebra.component;

import com.rebra.entity.OutboxEvent;
import com.rebra.entity.OutboxStatus;
import com.rebra.repository.OutboxEventRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OutboxEventRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final RedisTemplate<String, String> redisStreamTemplate;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Value("${rebalancing.stream.prefix}")
    private String streamPrefix;

    @Value("${rebalancing.stream.partition-count}")
    private int partitionCount;

    public OutboxEventRelay(OutboxEventRepository outboxEventRepository,
                            RedisTemplate<String, String> redisStreamTemplate,
                            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.outboxEventRepository = outboxEventRepository;
        this.redisStreamTemplate = redisStreamTemplate;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void init() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("redis");
        cb.getEventPublisher().onStateTransition(event -> {
            if (event.getStateTransition().getToState() == CircuitBreaker.State.CLOSED) {
                log.info("Redis CB CLOSED 전이 → PUBLISHED 전체 INIT 리셋");
                outboxEventRepository.resetAllPublishedToInit();
            }
        });
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void relay() {
        List<OutboxEvent> events = outboxEventRepository
                .findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.INIT);

        if (events.isEmpty()) return;

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("redis");

        for (OutboxEvent event : events) {
            try {
                cb.executeRunnable(() -> xAdd(event));
                event.markPublished();
                log.info("Outbox 발행 완료 outboxId={} portfolioId={}",
                        event.getId(), event.getPortfolioId());
            } catch (CallNotPermittedException e) {
                log.warn("Redis CB open, INIT 유지 outboxId={}", event.getId());
                break;
            } catch (Exception e) {
                log.error("Outbox 발행 실패 outboxId={}", event.getId(), e);
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void watchdog() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        int count = outboxEventRepository.resetStalePublishedToInit(threshold);
        if (count > 0) {
            log.warn("Watchdog: stale PUBLISHED → INIT 리셋 {}건 (10분 초과)", count);
        }
    }

    private void xAdd(OutboxEvent event) {
        int partition = Math.abs(event.getPortfolioId().hashCode()) % partitionCount;
        String streamKey = streamPrefix + partition;

        Map<String, String> fields = new HashMap<>();
        fields.put("payload", event.getPayload());
        fields.put("jobId", event.getRebalancingOrderId().toString());
        fields.put("portfolioId", event.getPortfolioId().toString());

        redisStreamTemplate.opsForStream().add(streamKey, fields);
    }
}
