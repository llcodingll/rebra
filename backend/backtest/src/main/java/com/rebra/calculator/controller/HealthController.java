package com.rebra.calculator.controller;

import com.rebra.calculator.kafka.BacktestKafkaListener;
import com.rebra.calculator.kafka.BacktestKafkaProducer;
import com.rebra.calculator.service.FeeCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 서버 상태 확인을 위한 헬스 체크 컨트롤러
 * 백테스트 계산 서버의 상태, 성능 지표, Kafka 연결 상태 등을 제공한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final BacktestKafkaListener kafkaListener;
    private final BacktestKafkaProducer kafkaProducer;
    private final FeeCalculatorService feeCalculatorService;

    /**
     * 기본 헬스 체크
     * 서버가 정상 작동하는지 확인
     * 
     * @return 서버 상태 정보
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("service", "Rebra Backtest Calculator Server");
            health.put("version", "1.0.0");
            
            // 기본 시스템 정보
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> system = new HashMap<>();
            system.put("availableProcessors", runtime.availableProcessors());
            system.put("maxMemory", formatBytes(runtime.maxMemory()));
            system.put("totalMemory", formatBytes(runtime.totalMemory()));
            system.put("freeMemory", formatBytes(runtime.freeMemory()));
            system.put("usedMemory", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
            
            health.put("system", system);
            
            log.debug("헬스 체크 요청 처리 완료");
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("헬스 체크 중 오류 발생", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * 상세 서버 상태 정보
     * Kafka 연결 상태, 처리 통계 등 포함
     * 
     * @return 상세 상태 정보
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            status.put("status", "UP");
            status.put("timestamp", LocalDateTime.now());
            
            // Kafka 관련 상태
            Map<String, Object> kafka = new HashMap<>();
            kafka.put("available", kafkaProducer.isKafkaAvailable());
            kafka.put("successCount", kafkaProducer.getSuccessCount());
            kafka.put("failureCount", kafkaProducer.getFailureCount());
            kafka.put("successRate", String.format("%.2f%%", kafkaProducer.getSuccessRate()));
            kafka.put("totalSentBytes", formatBytes(kafkaProducer.getTotalSentBytes()));
            
            status.put("kafka", kafka);
            
            // 백테스트 처리 상태
            Map<String, Object> backtest = new HashMap<>();
            backtest.put("activeCalculations", kafkaListener.getActiveTaskCount());
            backtest.put("queuedRequests", kafkaListener.getQueuedTaskCount());
            
            status.put("backtest", backtest);
            
            // 수수료 정보
            Map<String, Object> fees = new HashMap<>();
            fees.put("feeRates", feeCalculatorService.getFeeRateInfo());
            fees.put("borrowingRates", feeCalculatorService.getBorrowingRateInfo());
            
            status.put("configuration", fees);
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("상세 헬스 체크 중 오류 발생", e);
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            return ResponseEntity.status(503).body(status);
        }
    }

    /**
     * 메트릭스 정보 제공
     * 모니터링 시스템에서 사용할 수 있는 수치 정보
     * 
     * @return 메트릭스 정보
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // JVM 메트릭스
            Runtime runtime = Runtime.getRuntime();
            metrics.put("jvm.memory.used", runtime.totalMemory() - runtime.freeMemory());
            metrics.put("jvm.memory.free", runtime.freeMemory());
            metrics.put("jvm.memory.total", runtime.totalMemory());
            metrics.put("jvm.memory.max", runtime.maxMemory());
            metrics.put("jvm.processors", runtime.availableProcessors());
            
            // Kafka 메트릭스
            metrics.put("kafka.messages.sent.success", kafkaProducer.getSuccessCount());
            metrics.put("kafka.messages.sent.failure", kafkaProducer.getFailureCount());
            metrics.put("kafka.messages.sent.bytes", kafkaProducer.getTotalSentBytes());
            metrics.put("kafka.success.rate", kafkaProducer.getSuccessRate());
            
            // 백테스트 메트릭스
            metrics.put("backtest.calculations.active", kafkaListener.getActiveTaskCount());
            metrics.put("backtest.requests.queued", kafkaListener.getQueuedTaskCount());
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("메트릭스 조회 중 오류 발생", e);
            return ResponseEntity.status(503).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 서버 통계 초기화
     * 관리자용 엔드포인트
     * 
     * @return 초기화 결과
     */
    @GetMapping("/reset-stats")
    public ResponseEntity<Map<String, Object>> resetStatistics() {
        try {
            kafkaProducer.resetStatistics();
            kafkaListener.logThreadPoolStatus();
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "통계가 초기화되었습니다");
            result.put("timestamp", LocalDateTime.now());
            
            log.info("서버 통계 초기화됨");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("통계 초기화 중 오류 발생", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 시스템 정보 제공
     * 
     * @return 시스템 정보
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> systemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // JVM 정보
            info.put("java.version", System.getProperty("java.version"));
            info.put("java.vendor", System.getProperty("java.vendor"));
            info.put("os.name", System.getProperty("os.name"));
            info.put("os.arch", System.getProperty("os.arch"));
            info.put("os.version", System.getProperty("os.version"));
            
            // 애플리케이션 정보
            info.put("spring.profiles.active", System.getProperty("spring.profiles.active", "default"));
            info.put("server.port", System.getProperty("server.port", "8081"));
            
            // 런타임 정보
            Runtime runtime = Runtime.getRuntime();
            info.put("processors", runtime.availableProcessors());
            info.put("uptime", System.currentTimeMillis());
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            log.error("시스템 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 바이트 단위를 읽기 쉬운 형태로 변환
     * 
     * @param bytes 바이트 수
     * @return 포맷된 문자열 (예: "10.5 MB")
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}