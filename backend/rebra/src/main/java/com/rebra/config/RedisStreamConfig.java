package com.rebra.config;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
public class RedisStreamConfig {

    @Value("${rebalancing.stream.prefix}")
    private String streamPrefix;

    @Value("${rebalancing.stream.partition-count}")
    private int partitionCount;

    @Value("${rebalancing.stream.result-stream}")
    private String resultStream;

    @Value("${rebalancing.stream.consumer-group}")
    private String consumerGroup;

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisStreamConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisStreamTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    @PostConstruct
    public void initStreamsAndGroups() {
        try (var conn = redisConnectionFactory.getConnection()) {
            var streamCommands = conn.streamCommands();

            // 결과 수신 스트림 컨슈머 그룹 생성
            createGroupIfAbsent(streamCommands, resultStream, consumerGroup);
        } catch (Exception e) {
            log.warn("Redis Stream 초기화 실패 (서버 시작 시 Redis 미연결 가능): {}", e.getMessage());
        }
    }

    private void createGroupIfAbsent(
            org.springframework.data.redis.connection.RedisStreamCommands streamCommands,
            String streamKey,
            String group) {
        try {
            streamCommands.xGroupCreate(
                    streamKey.getBytes(),
                    group,
                    ReadOffset.from("0"),
                    true);
            log.info("Redis Stream 컨슈머 그룹 생성: stream={} group={}", streamKey, group);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.debug("Redis Stream 컨슈머 그룹 이미 존재: stream={} group={}", streamKey, group);
            } else {
                log.warn("Redis Stream 컨슈머 그룹 생성 실패: stream={} group={} error={}", streamKey, group, e.getMessage());
            }
        }
    }
}
