package com.rebra.rebalance.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.rebalance.application.rebalancing.dto.RebalancingResultEvent;
import com.rebra.rebalance.exception.ErrorCode;
import com.rebra.rebalance.exception.RebalancingException;
import com.rebra.rebalance.infrastructure.redis.dto.RebalancingResultMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RebalancingResultProducer {

    private final RedisTemplate<String, String> redisStreamTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rebalancing.stream.result-stream}")
    private String resultStream;

    public RebalancingResultProducer(RedisTemplate<String, String> redisStreamTemplate,
                                     ObjectMapper objectMapper) {
        this.redisStreamTemplate = redisStreamTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendSync(RebalancingResultEvent event) {
        try {
            xAdd(event);
            log.info("결과 produce 완료 jobId={} status={}", event.getJobId(), event.getStatus());
        } catch (Exception e) {
            log.error("결과 produce 실패 jobId={}", event.getJobId(), e);
            throw new RebalancingException(ErrorCode.KIS_API_ERROR, "결과 produce 실패");
        }
    }

    public void send(RebalancingResultEvent event) {
        try {
            xAdd(event);
        } catch (Exception e) {
            log.error("결과 produce 실패 jobId={}", event.getJobId(), e);
        }
    }

    private void xAdd(RebalancingResultEvent event) throws Exception {
        RebalancingResultMessage message = RebalancingResultMessage.from(event);
        String payload = objectMapper.writeValueAsString(message);

        Map<String, String> fields = new HashMap<>();
        fields.put("payload", payload);
        fields.put("jobId", event.getJobId().toString());
        fields.put("portfolioId", event.getPortfolioId().toString());

        redisStreamTemplate.opsForStream().add(resultStream, fields);
    }
}
