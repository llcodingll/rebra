package com.rebra.config;

import com.rebra.dto.backtest.BacktestRequest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Kafka 테스트 설정
 * KafkaTemplate을 모킹하여 메시지 발송/수신을 시뮬레이션
 */
@TestConfiguration
public class TestKafkaConfig {

    @MockBean
    @Primary
    private KafkaTemplate<String, Object> kafkaTemplate;

    // 캡처된 메시지들을 저장
    private final List<BacktestRequest> sentRequests = new ArrayList<>();
    private final List<Map<String, Object>> receivedResults = new ArrayList<>();

    /**
     * Kafka 모킹 설정
     * 메시지 발송 시 캡처하여 검증 가능하도록 함
     */
    public void setupKafkaMocking() {
        // BacktestRequest 발송 캡처
        doAnswer(invocation -> {
            String topic = invocation.getArgument(0);
            Object message = invocation.getArgument(1);
            
            if ("backtest-request".equals(topic) && message instanceof BacktestRequest) {
                sentRequests.add((BacktestRequest) message);
            }
            
            // CompletableFuture mock 반환
            return mock(org.springframework.util.concurrent.ListenableFuture.class);
        }).when(kafkaTemplate).send(eq("backtest-request"), any(BacktestRequest.class));

        // ProducerRecord를 사용하는 send 메서드도 모킹
        doAnswer(invocation -> {
            ProducerRecord<String, Object> record = invocation.getArgument(0);
            
            if ("backtest-request".equals(record.topic()) && 
                record.value() instanceof BacktestRequest) {
                sentRequests.add((BacktestRequest) record.value());
            }
            
            return mock(org.springframework.util.concurrent.ListenableFuture.class);
        }).when(kafkaTemplate).send(any(ProducerRecord.class));
    }

    /**
     * 백테스트 결과 수신 시뮬레이션
     * 실제 Kafka 리스너 대신 직접 서비스 메서드 호출
     */
    public Map<String, Object> simulateBacktestResult(Long backtestId) {
        Map<String, Object> result = new HashMap<>();
        result.put("backtest_id", backtestId);
        result.put("status", "COMPLETED");
        result.put("total_return", 15.5);
        result.put("annualized_return", 12.3);
        result.put("volatility", 18.2);
        result.put("sharpe_ratio", 0.67);
        result.put("max_drawdown", -8.5);
        result.put("details_json", createSampleDetailsJson());
        
        receivedResults.add(result);
        return result;
    }

    /**
     * 테스트용 상세 결과 JSON 생성
     */
    private String createSampleDetailsJson() {
        return """
        [
            {
                "date": "2024-01-02",
                "totalValue": 10000000.0,
                "cashBalance": 1000000.0,
                "stockValue": 9000000.0,
                "dailyReturn": 0.0,
                "cumulativeReturn": 0.0,
                "rebalanced": true,
                "rebalancingReason": "INITIAL_INVESTMENT",
                "holdings": [
                    {
                        "stockCode": "005930",
                        "stockName": "삼성전자",
                        "shares": 40,
                        "currentPrice": 75000.0,
                        "marketValue": 3000000.0,
                        "weight": 0.33,
                        "targetWeight": 0.33
                    },
                    {
                        "stockCode": "000660",
                        "stockName": "SK하이닉스",
                        "shares": 21,
                        "currentPrice": 140000.0,
                        "marketValue": 2940000.0,
                        "weight": 0.33,
                        "targetWeight": 0.33
                    },
                    {
                        "stockCode": "051910",
                        "stockName": "LG화학",
                        "shares": 7,
                        "currentPrice": 420000.0,
                        "marketValue": 2940000.0,
                        "weight": 0.33,
                        "targetWeight": 0.33
                    }
                ]
            },
            {
                "date": "2024-01-31",
                "totalValue": 10150000.0,
                "cashBalance": 1050000.0,
                "stockValue": 9100000.0,
                "dailyReturn": 0.5,
                "cumulativeReturn": 1.5,
                "rebalanced": true,
                "rebalancingReason": "PERIODIC_REBALANCING_MONTHLY",
                "holdings": [
                    {
                        "stockCode": "005930",
                        "stockName": "삼성전자",
                        "shares": 41,
                        "currentPrice": 75500.0,
                        "marketValue": 3095500.0,
                        "weight": 0.33,
                        "targetWeight": 0.33
                    },
                    {
                        "stockCode": "000660",
                        "stockName": "SK하이닉스",
                        "shares": 22,
                        "currentPrice": 141000.0,
                        "marketValue": 3102000.0,
                        "weight": 0.33,
                        "targetWeight": 0.33
                    },
                    {
                        "stockCode": "051910",
                        "stockName": "LG화학",
                        "shares": 7,
                        "currentPrice": 425000.0,
                        "marketValue": 2975000.0,
                        "weight": 0.33,
                        "targetWeight": 0.33
                    }
                ]
            }
        ]
        """;
    }

    /**
     * THRESHOLD 타입 백테스트 결과 시뮬레이션
     */
    public Map<String, Object> simulateThresholdBacktestResult(Long backtestId) {
        Map<String, Object> result = new HashMap<>();
        result.put("backtest_id", backtestId);
        result.put("status", "COMPLETED");
        result.put("total_return", 18.2);
        result.put("annualized_return", 14.1);
        result.put("volatility", 16.8);
        result.put("sharpe_ratio", 0.84);
        result.put("max_drawdown", -6.2);
        result.put("details_json", createThresholdDetailsJson());
        
        receivedResults.add(result);
        return result;
    }

    /**
     * THRESHOLD 타입용 상세 결과 JSON 생성
     */
    private String createThresholdDetailsJson() {
        return """
        [
            {
                "date": "2024-01-02",
                "totalValue": 10000000.0,
                "cashBalance": 1000000.0,
                "stockValue": 9000000.0,
                "dailyReturn": 0.0,
                "cumulativeReturn": 0.0,
                "rebalanced": true,
                "rebalancingReason": "INITIAL_INVESTMENT",
                "holdings": [
                    {
                        "stockCode": "005930",
                        "stockName": "삼성전자",
                        "shares": 40,
                        "currentPrice": 75000.0,
                        "marketValue": 3000000.0,
                        "weight": 0.33,
                        "targetWeight": 0.33
                    }
                ]
            },
            {
                "date": "2024-01-15",
                "totalValue": 10180000.0,
                "cashBalance": 1080000.0,
                "stockValue": 9100000.0,
                "dailyReturn": 1.2,
                "cumulativeReturn": 1.8,
                "rebalanced": true,
                "rebalancingReason": "THRESHOLD_EXCEEDED_005930",
                "holdings": [
                    {
                        "stockCode": "005930",
                        "stockName": "삼성전자",
                        "shares": 41,
                        "currentPrice": 76000.0,
                        "marketValue": 3116000.0,
                        "weight": 0.34,
                        "targetWeight": 0.33
                    }
                ]
            }
        ]
        """;
    }

    /**
     * 실패한 백테스트 결과 시뮬레이션
     */
    public Map<String, Object> simulateFailedBacktestResult(Long backtestId, String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        result.put("backtest_id", backtestId);
        result.put("status", "FAILED");
        result.put("error_message", errorMessage);
        
        receivedResults.add(result);
        return result;
    }

    /**
     * 발송된 BacktestRequest 목록 반환
     */
    public List<BacktestRequest> getSentRequests() {
        return new ArrayList<>(sentRequests);
    }

    /**
     * 마지막으로 발송된 BacktestRequest 반환
     */
    public BacktestRequest getLastSentRequest() {
        return sentRequests.isEmpty() ? null : sentRequests.get(sentRequests.size() - 1);
    }

    /**
     * 수신된 백테스트 결과 목록 반환
     */
    public List<Map<String, Object>> getReceivedResults() {
        return new ArrayList<>(receivedResults);
    }

    /**
     * 마지막으로 수신된 백테스트 결과 반환
     */
    public Map<String, Object> getLastReceivedResult() {
        return receivedResults.isEmpty() ? null : receivedResults.get(receivedResults.size() - 1);
    }

    /**
     * 특정 백테스트 ID의 요청 검색
     */
    public BacktestRequest findRequestByBacktestId(Long backtestId) {
        return sentRequests.stream()
                .filter(req -> req.getBacktestId().equals(backtestId))
                .findFirst()
                .orElse(null);
    }

    /**
     * KafkaTemplate 발송 메서드 호출 횟수 검증
     */
    public void verifyMessageSent(int expectedCount) {
        verify(kafkaTemplate, times(expectedCount)).send(eq("backtest-request"), any(BacktestRequest.class));
    }

    /**
     * 특정 백테스트 ID로 메시지가 발송되었는지 검증
     */
    public void verifyMessageSentForBacktest(Long backtestId) {
        ArgumentCaptor<BacktestRequest> captor = ArgumentCaptor.forClass(BacktestRequest.class);
        verify(kafkaTemplate, atLeastOnce()).send(eq("backtest-request"), captor.capture());
        
        boolean found = captor.getAllValues().stream()
                .anyMatch(req -> req.getBacktestId().equals(backtestId));
        
        if (!found) {
            throw new AssertionError("백테스트 ID " + backtestId + "에 대한 Kafka 메시지가 발송되지 않았습니다.");
        }
    }

    /**
     * Mock 객체 반환
     */
    public KafkaTemplate<String, Object> getKafkaTemplate() {
        return kafkaTemplate;
    }

    /**
     * 모든 캡처된 데이터 초기화
     */
    public void clearCapturedData() {
        sentRequests.clear();
        receivedResults.clear();
        reset(kafkaTemplate);
        setupKafkaMocking(); // 모킹 다시 설정
    }
}