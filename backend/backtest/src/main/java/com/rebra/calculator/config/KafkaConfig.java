package com.rebra.calculator.config;

import com.rebra.calculator.dto.BacktestRequest;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.micrometer.MicrometerConsumerListener;
import org.springframework.kafka.support.micrometer.MicrometerProducerListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 설정을 담당하는 Configuration 클래스
 * Consumer와 Producer의 설정을 정의하고 필요한 Bean들을 생성한다.
 * 
 * 주요 설정:
 * - JSON 직렬화/역직렬화
 * - 에러 처리 및 재시도 설정
 * - 동시성 및 성능 최적화
 * - 메시지 처리 확인(ACK) 설정
 */
@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {

    private final MeterRegistry meterRegistry;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Kafka Consumer 설정을 생성한다
     * Micrometer 리스너와 Factory 리스너를 포함한다
     * 
     * @return Consumer 설정 맵
     */
    @Bean
    public ConsumerFactory<String, BacktestRequest> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // 기본 연결 설정
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // JSON 역직렬화 설정
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BacktestRequest.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.rebra.calculator.dto");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        
        // 성능 및 안정성 설정
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 처음부터 읽기
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 수동 커밋
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30초
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10초
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 600000); // 10분 (백테스트 처리 시간 고려)
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1); // 한 번에 하나씩 처리
        
        // 메모리 사용량 제한
        configProps.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 52428800); // 50MB
        configProps.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1048576); // 1MB
        
        log.info("Kafka Consumer 설정 완료 - BootstrapServers: {}, GroupId: {}", 
                bootstrapServers, groupId);
        
        DefaultKafkaConsumerFactory<String, BacktestRequest> factory = 
                new DefaultKafkaConsumerFactory<>(configProps);
        
        // Micrometer 리스너 추가 (메트릭 수집)
        factory.addListener(new MicrometerConsumerListener<>(
            meterRegistry,
            Collections.singletonList(Tag.of("service", "backtest-calculator"))
        ));
        
        // Factory 리스너 추가 (Consumer 생성/제거 이벤트 모니터링)
        factory.addListener(new ConsumerFactory.Listener<String, BacktestRequest>() {
            @Override
            public void consumerAdded(String id, org.apache.kafka.clients.consumer.Consumer<String, BacktestRequest> consumer) {
                log.info("Kafka Consumer 생성됨 - ID: {}, Client ID: {}", id, 
                    consumer.metrics().entrySet().stream()
                        .filter(entry -> "client-id".equals(entry.getKey().name()))
                        .findFirst()
                        .map(entry -> entry.getValue().toString())
                        .orElse("unknown"));
            }
            
            @Override
            public void consumerRemoved(String id, org.apache.kafka.clients.consumer.Consumer<String, BacktestRequest> consumer) {
                log.info("Kafka Consumer 제거됨 - ID: {}", id);
            }
        });
        
        return factory;
    }

    /**
     * Kafka Producer 설정을 생성한다
     * 
     * @return Producer 설정을 포함한 ProducerFactory
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // 기본 연결 설정
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // 성능 최적화 설정
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // 모든 replica 확인
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // 3회 재시도
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000); // 1초 간격
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB 배치
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10); // 10ms 대기
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip"); // GZIP 압축
        
        // 버퍼 및 메모리 설정
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864); // 64MB
        configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10485760); // 10MB
        
        // 전송 보장 설정
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 300000); // 5분
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000); // 1분
        
        log.info("Kafka Producer 설정 완료 - BootstrapServers: {}", bootstrapServers);
        
        DefaultKafkaProducerFactory<String, Object> factory = 
                new DefaultKafkaProducerFactory<>(configProps);
        
        // Micrometer 리스너 추가 (메트릭 수집)
        factory.addListener(new MicrometerProducerListener<>(
            meterRegistry,
            Collections.singletonList(Tag.of("service", "backtest-calculator"))
        ));
        
        // Factory 리스너 추가 (Producer 생성/제거 이벤트 모니터링)
        factory.addListener(new ProducerFactory.Listener<String, Object>() {
            @Override
            public void producerAdded(String id, org.apache.kafka.clients.producer.Producer<String, Object> producer) {
                log.info("Kafka Producer 생성됨 - ID: {}, Client ID: {}", id,
                    producer.metrics().entrySet().stream()
                        .filter(entry -> "client-id".equals(entry.getKey().name()))
                        .findFirst()
                        .map(entry -> entry.getValue().toString())
                        .orElse("unknown"));
            }
            
            @Override
            public void producerRemoved(String id, org.apache.kafka.clients.producer.Producer<String, Object> producer) {
                log.info("Kafka Producer 제거됨 - ID: {}", id);
            }
        });
        
        return factory;
    }

    /**
     * Kafka Template을 생성한다
     * 메시지 전송에 사용되는 고수준 API
     * 
     * @return KafkaTemplate 인스턴스
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        
        // 전송 성공/실패 처리는 BacktestKafkaProducer에서 개별적으로 처리
        // ProducerListener는 더 이상 사용하지 않고 CompletableFuture를 통한 비동기 처리 방식 사용
        
        return template;
    }

    /**
     * Kafka Listener Container Factory를 생성한다
     * Consumer의 동작 방식을 설정한다
     * 
     * @return ConcurrentKafkaListenerContainerFactory 인스턴스
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BacktestRequest> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BacktestRequest> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // 동시성 설정 (CPU 코어 수만큼 컨슈머 생성)
        int concurrency = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        factory.setConcurrency(concurrency);
        
        // 수동 ACK 설정
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // 에러 처리 설정
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler(
            (consumerRecord, exception) -> {
                log.error("Kafka 메시지 처리 중 복구 불가능한 오류 발생 - Topic: {}, Partition: {}, Offset: {}", 
                        consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), exception);
            },
            new org.springframework.util.backoff.FixedBackOff(5000L, 3) // 5초 간격으로 3회 재시도
        ));
        
        // 배치 처리 비활성화 (개별 메시지 처리)
        factory.setBatchListener(false);
        
        log.info("Kafka Listener Container Factory 설정 완료 - Concurrency: {}", concurrency);
        
        return factory;
    }

    /**
     * Kafka Admin 설정을 생성한다
     * 토픽 생성 및 관리에 사용
     * 
     * @return KafkaAdmin 인스턴스
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(org.apache.kafka.clients.admin.AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configs.put(org.apache.kafka.clients.admin.AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 60000);
        
        KafkaAdmin admin = new KafkaAdmin(configs);
        admin.setFatalIfBrokerNotAvailable(false); // Broker 없어도 애플리케이션 시작
        
        log.info("Kafka Admin 설정 완료");
        
        return admin;
    }

    /**
     * 필요한 토픽들을 자동으로 생성한다
     * 개발 환경에서 편의를 위한 설정
     */
    @Bean
    public org.springframework.kafka.core.KafkaAdmin.NewTopics topics(
            @Value("${kafka.topics.backtest-request}") String requestTopic,
            @Value("${kafka.topics.backtest-result}") String resultTopic) {
        
        int numPartitions = Math.max(2, Runtime.getRuntime().availableProcessors()); // 최소 2개 파티션
        short replicationFactor = 1; // 개발환경용
        
        org.apache.kafka.clients.admin.NewTopic requestTopicConfig = 
                new org.apache.kafka.clients.admin.NewTopic(requestTopic, numPartitions, replicationFactor);
        
        org.apache.kafka.clients.admin.NewTopic resultTopicConfig = 
                new org.apache.kafka.clients.admin.NewTopic(resultTopic, numPartitions, replicationFactor);
        
        // 토픽별 추가 설정
        Map<String, String> topicConfigs = new HashMap<>();
        topicConfigs.put("retention.ms", "604800000"); // 7일 보관
        topicConfigs.put("cleanup.policy", "delete");
        topicConfigs.put("compression.type", "gzip");
        
        requestTopicConfig.configs(topicConfigs);
        resultTopicConfig.configs(topicConfigs);
        
        log.info("Kafka 토픽 자동 생성 설정 완료 - 요청토픽: {}, 결과토픽: {}, 파티션: {}", 
                requestTopic, resultTopic, numPartitions);
        
        return new org.springframework.kafka.core.KafkaAdmin.NewTopics(
                requestTopicConfig, resultTopicConfig);
    }

}