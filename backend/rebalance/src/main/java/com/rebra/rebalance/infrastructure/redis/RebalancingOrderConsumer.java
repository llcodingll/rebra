package com.rebra.rebalance.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.rebalance.application.rebalancing.RebalancingApplicationService;
import com.rebra.rebalance.application.rebalancing.dto.RebalancingOrderCommand;
import com.rebra.rebalance.exception.IdempotencyViolationException;
import com.rebra.rebalance.exception.RebalancingCutoffException;
import com.rebra.rebalance.exception.RebalancingException;
import com.rebra.rebalance.infrastructure.redis.dto.RebalancingOrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RebalancingOrderConsumer implements SmartLifecycle {

    private final RebalancingApplicationService applicationService;
    private final RedisTemplate<String, String> redisStreamTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rebalancing.stream.prefix}")
    private String streamPrefix;

    @Value("${rebalancing.stream.consumer-group}")
    private String consumerGroup;

    @Value("${rebalancing.stream.instance-id}")
    private String instanceId;

    @Value("${rebalancing.stream.assigned-partitions}")
    private String assignedPartitionsRaw;

    private List<Integer> assignedPartitions;
    private volatile boolean running = false;
    private final List<Thread> consumerThreads = new ArrayList<>();

    public RebalancingOrderConsumer(RebalancingApplicationService applicationService,
                                    RedisTemplate<String, String> redisStreamTemplate,
                                    ObjectMapper objectMapper) {
        this.applicationService = applicationService;
        this.redisStreamTemplate = redisStreamTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void start() {
        assignedPartitions = Arrays.stream(assignedPartitionsRaw.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();

        running = true;
        for (int partition : assignedPartitions) {
            reclaimStale(partition);
            int p = partition;
            Thread thread = Thread.ofVirtual()
                    .name("rebalancing-consumer-" + partition)
                    .start(() -> consumeLoop(p));
            consumerThreads.add(thread);
        }
        log.info("RebalancingOrderConsumer 시작: 파티션={}", assignedPartitions);
    }

    @Override
    public void stop(Runnable callback) {
        running = false;
        consumerThreads.forEach(Thread::interrupt);
        log.info("RebalancingOrderConsumer 종료 중...");
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1;
    }

    @Scheduled(fixedDelay = 60000)
    public void periodicReclaim() {
        if (assignedPartitions == null) return;
        assignedPartitions.forEach(this::reclaimStale);
    }

    private void consumeLoop(int partition) {
        String streamKey = streamPrefix + partition;
        String consumerName = instanceId + ":rebalancing:executor:" + partition;

        while (!Thread.currentThread().isInterrupted() && running) {
            try {
                List<MapRecord<String, Object, Object>> records = redisStreamTemplate.opsForStream()
                        .read(Consumer.from(consumerGroup, consumerName),
                                StreamReadOptions.empty().block(Duration.ofSeconds(2)).count(1),
                                StreamOffset.create(streamKey, ReadOffset.lastConsumed()));

                if (records == null || records.isEmpty()) continue;

                for (MapRecord<String, Object, Object> record : records) {
                    processRecord(record, streamKey, consumerName);
                }
            } catch (Exception e) {
                if (Thread.currentThread().isInterrupted() || !running) break;
                log.error("Consumer 루프 오류 partition={}: {}", partition, e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ie) { break; }
            }
        }
        log.info("Consumer 루프 종료 partition={}", partition);
    }

    private void processRecord(MapRecord<String, Object, Object> record, String streamKey, String consumerName) {
        Map<Object, Object> fields = record.getValue();
        String payload = (String) fields.get("payload");

        if (payload == null) {
            log.warn("payload 필드 누락 recordId={}", record.getId());
            redisStreamTemplate.opsForStream().acknowledge(streamKey, consumerGroup, record.getId());
            return;
        }

        Long jobId = null;
        try {
            RebalancingOrderMessage message = objectMapper.readValue(payload, RebalancingOrderMessage.class);
            jobId = message.getJobId();
            log.info("리밸런싱 메시지 수신 jobId={} portfolioId={} consumer={}",
                    jobId, message.getPortfolioId(), consumerName);

            applicationService.execute(RebalancingOrderCommand.from(message));

            redisStreamTemplate.opsForStream().acknowledge(streamKey, consumerGroup, record.getId());
            log.info("리밸런싱 처리 완료 및 XACK jobId={}", jobId);

        } catch (IdempotencyViolationException e) {
            log.info("중복 consume 스킵 jobId={}", jobId);
            redisStreamTemplate.opsForStream().acknowledge(streamKey, consumerGroup, record.getId());
        } catch (RebalancingCutoffException e) {
            log.warn("15:30 cutoff 초과 jobId={}", jobId);
            redisStreamTemplate.opsForStream().acknowledge(streamKey, consumerGroup, record.getId());
        } catch (RebalancingException e) {
            // ACK 없음 → PEL에 남음 → periodicReclaim(XAUTOCLAIM)에서 재처리
            log.error("리밸런싱 실패 jobId={} code={} msg={}",
                    jobId, e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 오류 jobId={} recordId={}", jobId, record.getId(), e);
        }
    }

    private void reclaimStale(int partition) {
        String streamKey = streamPrefix + partition;
        String consumerName = instanceId + ":rebalancing:executor:" + partition;

        try {
            byte[] keyBytes = streamKey.getBytes(StandardCharsets.UTF_8);
            List<ByteRecord> claimed = redisStreamTemplate.execute(
                    (RedisCallback<List<ByteRecord>>) conn ->
                            conn.streamCommands().xAutoClaim(
                                    keyBytes, consumerGroup, consumerName,
                                    Duration.ofMinutes(10), RecordId.of("0-0")));

            if (claimed == null || claimed.isEmpty()) return;

            log.info("XAUTOCLAIM: partition={} 회수 {}건", partition, claimed.size());
            for (ByteRecord record : claimed) {
                Map<Object, Object> fields = new LinkedHashMap<>();
                record.getValue().forEach((k, v) -> fields.put(
                        new String(k, StandardCharsets.UTF_8),
                        new String(v, StandardCharsets.UTF_8)));
                MapRecord<String, Object, Object> mapRecord = StreamRecords.newRecord()
                        .in(streamKey)
                        .withId(record.getId())
                        .ofMap(fields);
                processRecord(mapRecord, streamKey, consumerName);
            }
        } catch (Exception e) {
            log.warn("XAUTOCLAIM 실패 partition={}: {}", partition, e.getMessage());
        }
    }
}
