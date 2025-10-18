package com.rebra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.entity.RebalancingOrder;
import com.rebra.entity.TradeRecord;
import com.rebra.enums.TransactionStatus;
import com.rebra.kafka.dto.RebalancingResultMessage;
import com.rebra.repository.OutboxEventRepository;
import com.rebra.repository.RebalancingOrderRepository;
import com.rebra.repository.TradeRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RebalancingResultConsumer {

    private final RebalancingOrderRepository rebalancingOrderRepository;
    private final TradeRecordRepository tradeRecordRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final RedisTemplate<String, String> redisStreamTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rebalancing.stream.result-stream}")
    private String resultStream;

    @Value("${rebalancing.stream.consumer-group}")
    private String consumerGroup;

    @Value("${rebalancing.stream.instance-id}")
    private String instanceId;

    public RebalancingResultConsumer(RebalancingOrderRepository rebalancingOrderRepository,
                                     TradeRecordRepository tradeRecordRepository,
                                     OutboxEventRepository outboxEventRepository,
                                     RedisTemplate<String, String> redisStreamTemplate,
                                     ObjectMapper objectMapper) {
        this.rebalancingOrderRepository = rebalancingOrderRepository;
        this.tradeRecordRepository = tradeRecordRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.redisStreamTemplate = redisStreamTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        String consumerName = instanceId + ":rebra:result:0";
        Thread.ofVirtual()
                .name("rebalancing-result-consumer")
                .start(() -> consumeLoop(consumerName));
        log.info("RebalancingResultConsumer 시작: consumer={}", consumerName);
    }

    private void consumeLoop(String consumerName) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<MapRecord<String, Object, Object>> records = redisStreamTemplate.opsForStream()
                        .read(Consumer.from(consumerGroup, consumerName),
                                StreamReadOptions.empty().block(Duration.ofSeconds(2)).count(10),
                                StreamOffset.create(resultStream, ReadOffset.lastConsumed()));

                if (records == null || records.isEmpty()) continue;

                for (MapRecord<String, Object, Object> record : records) {
                    processRecord(record, consumerName);
                }
            } catch (Exception e) {
                if (Thread.currentThread().isInterrupted()) break;
                log.error("Result consumer 루프 오류: {}", e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ie) { break; }
            }
        }
    }

    @Transactional
    public void processRecord(MapRecord<String, Object, Object> record, String consumerName) {
        String payload = (String) record.getValue().get("payload");
        Long jobId = null;

        try {
            RebalancingResultMessage message = objectMapper.readValue(payload, RebalancingResultMessage.class);
            jobId = message.getJobId();
            log.info("리밸런싱 결과 수신 jobId={} status={}", jobId, message.getStatus());

            final Long capturedJobId = jobId;
            RebalancingOrder order = rebalancingOrderRepository
                    .findById(jobId)
                    .orElseThrow(() -> new IllegalStateException("RebalancingOrder 없음 jobId=" + capturedJobId));

            if ("COMPLETED".equals(message.getStatus())) {
                order.updateStatus(TransactionStatus.COMPLETED);
                order.updateAmounts(message.getTotalBuyAmount(), message.getTotalSellAmount());

                List<TradeRecord> records = buildTradeRecords(order, message.getTrades());
                if (!records.isEmpty()) {
                    tradeRecordRepository.saveAll(records);
                }
            } else {
                order.updateStatus(TransactionStatus.FAILED);
            }

            rebalancingOrderRepository.save(order);
            outboxEventRepository.markConsumedByRebalancingOrderId(jobId);
            redisStreamTemplate.opsForStream().acknowledge(resultStream, consumerGroup, record.getId());

        } catch (Exception e) {
            log.error("결과 처리 실패 jobId={}", jobId, e);
            redisStreamTemplate.opsForStream().acknowledge(resultStream, consumerGroup, record.getId());
        }
    }

    @SuppressWarnings("unchecked")
    private List<TradeRecord> buildTradeRecords(RebalancingOrder order, List<Map<String, Object>> trades) {
        List<TradeRecord> result = new ArrayList<>();
        if (trades == null) return result;

        for (Map<String, Object> t : trades) {
            if (!"COMPLETED".equals(t.get("status"))) continue;

            result.add(TradeRecord.builder()
                    .rebalancingOrder(order)
                    .stockCode((String) t.get("stockCode"))
                    .stockName(t.getOrDefault("stockName", t.get("stockCode")).toString())
                    .tradeType((String) t.get("orderType"))
                    .tradeDate(LocalDateTime.now())
                    .executedShares(toInt(t.get("quantity")))
                    .executedPrice(toLong(t.get("price")))
                    .totalAmount(toLong(t.get("price")) * toInt(t.get("quantity")))
                    .status(TransactionStatus.COMPLETED)
                    .orderNumber(t.getOrDefault("kisOrderNumber", "").toString())
                    .reason("리밸런싱")
                    .build());
        }
        return result;
    }

    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }
}
