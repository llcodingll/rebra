package com.rebra.service;

import com.rebra.config.WebSocketSessionDisconnectEvent;
import com.rebra.dto.response.RealtimeStockData;
import com.youhogeon.finance.kis_api.api.realtime.H0STASP0Data;
import com.youhogeon.finance.kis_api.api.realtime.H0STCNT0Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * WebSocket 연결 안정성 및 재연결 관리 서비스
 * - 연결 상태 모니터링
 * - 하트비트 메커니즘
 * - 자동 재연결 로직
 * - 데이터 무결성 검증
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketReconnectionService {

    private final SimpMessagingTemplate messagingTemplate;
    private final KisRealtimeService kisRealtimeService;

    // 세션별 마지막 활동 시간 추적
    private final Map<String, LastActivity> sessionActivity = new ConcurrentHashMap<>();
    
    // 세션별 구독 정보 저장 (재연결 시 복구용)
    private final Map<String, SessionSubscriptions> sessionSubscriptions = new ConcurrentHashMap<>();
    
    // 하트비트 카운터
    private final AtomicLong heartbeatCounter = new AtomicLong(0);
    
    // 설정값
    private static final long HEARTBEAT_INTERVAL = 30000; // 30초
    private static final long SESSION_TIMEOUT = 120000;   // 2분
    private static final long RECONNECTION_DELAY = 5000;  // 5초
    private static final int MAX_RECONNECTION_ATTEMPTS = 3;

    /**
     * 세션 활동 정보
     */
    public static class LastActivity {
        private LocalDateTime lastSeen;
        private boolean isActive;
        private int missedHeartbeats;
        
        public LastActivity() {
            this.lastSeen = LocalDateTime.now();
            this.isActive = true;
            this.missedHeartbeats = 0;
        }
        
        public void updateActivity() {
            this.lastSeen = LocalDateTime.now();
            this.isActive = true;
            this.missedHeartbeats = 0;
        }
        
        public void missHeartbeat() {
            this.missedHeartbeats++;
            if (this.missedHeartbeats > 3) {
                this.isActive = false;
            }
        }

        public LocalDateTime getLastSeen() { return lastSeen; }
        public boolean isActive() { return isActive; }
        public int getMissedHeartbeats() { return missedHeartbeats; }
        public void setActive(boolean active) { this.isActive = active; }
    }

    /**
     * 세션별 구독 정보
     */
    public static class SessionSubscriptions {
        private final Map<String, String> priceSubscriptions = new ConcurrentHashMap<>();
        private final Map<String, String> orderbookSubscriptions = new ConcurrentHashMap<>();
        private Long userId;
        private int reconnectionAttempts = 0;
        
        public SessionSubscriptions(Long userId) {
            this.userId = userId;
        }

        public Map<String, String> getPriceSubscriptions() { return priceSubscriptions; }
        public Map<String, String> getOrderbookSubscriptions() { return orderbookSubscriptions; }
        public Long getUserId() { return userId; }
        public int getReconnectionAttempts() { return reconnectionAttempts; }
        public void incrementReconnectionAttempts() { this.reconnectionAttempts++; }
        public void resetReconnectionAttempts() { this.reconnectionAttempts = 0; }
    }

    /**
     * 새 세션 등록
     */
    public void registerSession(String sessionId, Long userId) {
        sessionActivity.put(sessionId, new LastActivity());
        sessionSubscriptions.put(sessionId, new SessionSubscriptions(userId));
        log.info("WebSocket 세션 등록 - SessionId: {}, UserId: {}", sessionId, userId);
    }

    /**
     * 세션 제거
     */
    public void removeSession(String sessionId) {
        sessionActivity.remove(sessionId);
        SessionSubscriptions subscriptions = sessionSubscriptions.remove(sessionId);
        
        if (subscriptions != null) {
            log.info("WebSocket 세션 제거 및 구독 정리 - SessionId: {}, 체결가: {}개, 호가: {}개", 
                    sessionId, 
                    subscriptions.getPriceSubscriptions().size(),
                    subscriptions.getOrderbookSubscriptions().size());
        }
    }

    /**
     * 구독 정보 추가
     */
    public void addSubscription(String sessionId, String stockCode, String dataType) {
        SessionSubscriptions subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            if ("price".equals(dataType)) {
                subscriptions.getPriceSubscriptions().put(stockCode, dataType);
            } else if ("orderbook".equals(dataType)) {
                subscriptions.getOrderbookSubscriptions().put(stockCode, dataType);
            }
            log.debug("구독 정보 추가 - SessionId: {}, StockCode: {}, DataType: {}", sessionId, stockCode, dataType);
        }
    }

    /**
     * 구독 정보 제거
     */
    public void removeSubscription(String sessionId, String stockCode, String dataType) {
        SessionSubscriptions subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            if ("price".equals(dataType)) {
                subscriptions.getPriceSubscriptions().remove(stockCode);
            } else if ("orderbook".equals(dataType)) {
                subscriptions.getOrderbookSubscriptions().remove(stockCode);
            }
            log.debug("구독 정보 제거 - SessionId: {}, StockCode: {}, DataType: {}", sessionId, stockCode, dataType);
        }
    }

    /**
     * 세션 활동 업데이트
     */
    public void updateSessionActivity(String sessionId) {
        LastActivity activity = sessionActivity.get(sessionId);
        if (activity != null) {
            activity.updateActivity();
        }
    }

    /**
     * 하트비트 전송 (30초마다)
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL)
    public void sendHeartbeat() {
        long currentHeartbeat = heartbeatCounter.incrementAndGet();
        
        // 활성 세션들에게 하트비트 전송
        sessionActivity.keySet().forEach(sessionId -> {
            try {
                String destination = "/topic/heartbeat/" + sessionId;
                Map<String, Object> heartbeatData = Map.of(
                    "type", "heartbeat",
                    "timestamp", System.currentTimeMillis(),
                    "sequence", currentHeartbeat,
                    "status", "alive"
                );
                
                messagingTemplate.convertAndSend(destination, heartbeatData);
                log.debug("하트비트 전송 - SessionId: {}, Sequence: {}", sessionId, currentHeartbeat);
                
            } catch (Exception e) {
                log.error("하트비트 전송 실패 - SessionId: {}", sessionId, e);
                handleSessionFailure(sessionId);
            }
        });
    }

    /**
     * 세션 상태 모니터링 (1분마다)
     */
    @Scheduled(fixedRate = 60000)
    public void monitorSessions() {
        LocalDateTime now = LocalDateTime.now();
        
        sessionActivity.entrySet().removeIf(entry -> {
            String sessionId = entry.getKey();
            LastActivity activity = entry.getValue();
            
            // 타임아웃된 세션 정리
            if (activity.getLastSeen().plusSeconds(SESSION_TIMEOUT / 1000).isBefore(now)) {
                log.warn("세션 타임아웃 - SessionId: {}, LastSeen: {}", sessionId, activity.getLastSeen());
                handleSessionTimeout(sessionId);
                return true;
            }
            
            // 비활성 세션 체크
            if (!activity.isActive()) {
                log.warn("비활성 세션 감지 - SessionId: {}, MissedHeartbeats: {}", 
                        sessionId, activity.getMissedHeartbeats());
                attemptSessionRecovery(sessionId);
            }
            
            return false;
        });
    }

    /**
     * 세션 실패 처리
     */
    private void handleSessionFailure(String sessionId) {
        LastActivity activity = sessionActivity.get(sessionId);
        if (activity != null) {
            activity.missHeartbeat();
            
            if (!activity.isActive()) {
                log.error("세션 연결 실패 감지 - SessionId: {}", sessionId);
                attemptSessionRecovery(sessionId);
            }
        }
    }

    /**
     * 세션 타임아웃 처리
     */
    private void handleSessionTimeout(String sessionId) {
        SessionSubscriptions subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            Long userId = subscriptions.getUserId();
            
            // 모든 구독 해제
            subscriptions.getPriceSubscriptions().keySet().forEach(stockCode -> {
                try {
                    kisRealtimeService.stopPriceSubscription(userId, stockCode, sessionId);
                } catch (Exception e) {
                    log.error("타임아웃 세션 체결가 구독 해제 실패 - SessionId: {}, StockCode: {}", sessionId, stockCode, e);
                }
            });
            
            subscriptions.getOrderbookSubscriptions().keySet().forEach(stockCode -> {
                try {
                    kisRealtimeService.stopOrderbookSubscription(userId, stockCode, sessionId);
                } catch (Exception e) {
                    log.error("타임아웃 세션 호가 구독 해제 실패 - SessionId: {}, StockCode: {}", sessionId, stockCode, e);
                }
            });
        }
        
        removeSession(sessionId);
    }

    /**
     * 세션 복구 시도
     */
    private void attemptSessionRecovery(String sessionId) {
        SessionSubscriptions subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions == null || subscriptions.getReconnectionAttempts() >= MAX_RECONNECTION_ATTEMPTS) {
            log.error("세션 복구 한계 초과 - SessionId: {}, Attempts: {}", 
                    sessionId, subscriptions != null ? subscriptions.getReconnectionAttempts() : 0);
            handleSessionTimeout(sessionId);
            return;
        }

        subscriptions.incrementReconnectionAttempts();
        log.info("세션 복구 시도 - SessionId: {}, Attempt: {}/{}", 
                sessionId, subscriptions.getReconnectionAttempts(), MAX_RECONNECTION_ATTEMPTS);

        // 재연결 지연
        try {
            Thread.sleep(RECONNECTION_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // 구독 복구
        recoverSubscriptions(sessionId, subscriptions);
    }

    /**
     * 구독 복구
     */
    private void recoverSubscriptions(String sessionId, SessionSubscriptions subscriptions) {
        Long userId = subscriptions.getUserId();
        
        try {
            // 체결가 구독 복구
            for (String stockCode : subscriptions.getPriceSubscriptions().keySet()) {
                log.info("체결가 구독 복구 - SessionId: {}, StockCode: {}", sessionId, stockCode);
                // 실제 구독 복구는 클라이언트에서 재구독 요청을 통해 처리
                sendRecoveryNotification(sessionId, stockCode, "price");
            }
            
            // 호가 구독 복구
            for (String stockCode : subscriptions.getOrderbookSubscriptions().keySet()) {
                log.info("호가 구독 복구 - SessionId: {}, StockCode: {}", sessionId, stockCode);
                sendRecoveryNotification(sessionId, stockCode, "orderbook");
            }
            
            subscriptions.resetReconnectionAttempts();
            
            // 세션 활동 복구
            LastActivity activity = sessionActivity.get(sessionId);
            if (activity != null) {
                activity.setActive(true);
                activity.updateActivity();
            }
            
        } catch (Exception e) {
            log.error("구독 복구 실패 - SessionId: {}", sessionId, e);
        }
    }

    /**
     * 클라이언트에 복구 알림 전송
     */
    private void sendRecoveryNotification(String sessionId, String stockCode, String dataType) {
        try {
            String destination = "/topic/recovery/" + sessionId;
            Map<String, Object> recoveryData = Map.of(
                "type", "recovery_needed",
                "stockCode", stockCode,
                "dataType", dataType,
                "timestamp", System.currentTimeMillis(),
                "message", "연결이 복구되었습니다. 구독을 재시작하세요."
            );
            
            messagingTemplate.convertAndSend(destination, recoveryData);
            log.info("복구 알림 전송 - SessionId: {}, StockCode: {}, DataType: {}", sessionId, stockCode, dataType);
            
        } catch (Exception e) {
            log.error("복구 알림 전송 실패 - SessionId: {}", sessionId, e);
        }
    }

    /**
     * 데이터 무결성 검증
     */
    public boolean validateDataIntegrity(String stockCode, String dataType, Object data) {
        try {
            if (data == null) {
                log.warn("데이터 무결성 검증 실패 - null 데이터: StockCode: {}, DataType: {}", stockCode, dataType);
                return false;
            }
            
            // 기본 데이터 형식 검증
            if ("price".equals(dataType)) {
                // 체결가 데이터 검증 로직
                return validatePriceData(stockCode, data);
            } else if ("orderbook".equals(dataType)) {
                // 호가 데이터 검증 로직
                return validateOrderbookData(stockCode, data);
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("데이터 무결성 검증 중 오류 - StockCode: {}, DataType: {}", stockCode, dataType, e);
            return false;
        }
    }

    /**
     * 체결가 데이터 무결성 검증
     */
    private boolean validatePriceData(String stockCode, Object data) {
        try {
            if (data instanceof H0STCNT0Data) {
                H0STCNT0Data priceData = (H0STCNT0Data) data;
                return validateH0STCNT0Data(stockCode, priceData);
            }

            // RealtimeStockData.CurrentPriceData 형태인 경우
            if (data instanceof RealtimeStockData.CurrentPriceData) {
                RealtimeStockData.CurrentPriceData currentPriceData =
                    (RealtimeStockData.CurrentPriceData) data;

                // 주식코드 검증
                if (!stockCode.equals(currentPriceData.getStockCode())) {
                    log.warn("체결가 데이터 주식코드 불일치 - Expected: {}, Actual: {}",
                            stockCode, currentPriceData.getStockCode());
                    return false;
                }

                // 타임스탬프 검증 (null이면 안됨)
                if (currentPriceData.getTimestamp() == null || currentPriceData.getTimestamp().isEmpty()) {
                    log.warn("체결가 데이터 타임스탬프 누락 - StockCode: {}", stockCode);
                    return false;
                }

                // 내부 priceData 재귀 검증
                return validatePriceData(stockCode, currentPriceData.getPriceData());
            }

            // 기타 형태는 일단 통과
            log.debug("알 수 없는 체결가 데이터 형태 - StockCode: {}, DataType: {}",
                    stockCode, data != null ? data.getClass().getSimpleName() : "null");
            return true;

        } catch (Exception e) {
            log.error("체결가 데이터 검증 중 오류 - StockCode: {}", stockCode, e);
            return false;
        }
    }

    /**
     * H0STCNT0Data (KIS 체결가 데이터) 검증
     */
    private boolean validateH0STCNT0Data(String stockCode, H0STCNT0Data data) {
        try {
            // 기본적인 데이터 구조 검증
            if (data == null) {
                log.warn("H0STCNT0Data가 null - StockCode: {}", stockCode);
                return false;
            }

            // KIS API 라이브러리의 데이터 구조를 모르므로 기본적인 검증만 수행
            // 추후 실제 필드명이 확인되면 더 구체적으로 검증 가능

            // toString()이 비어있거나 null이면 이상한 데이터로 판단
            String dataString = data.toString();
            if (dataString == null || dataString.trim().isEmpty() || "null".equals(dataString)) {
                log.warn("H0STCNT0Data 내용이 비어있음 - StockCode: {}", stockCode);
                return false;
            }

            log.debug("H0STCNT0Data 검증 성공 - StockCode: {}, DataLength: {}",
                    stockCode, dataString.length());
            return true;

        } catch (Exception e) {
            log.error("H0STCNT0Data 검증 중 오류 - StockCode: {}", stockCode, e);
            return false;
        }
    }

    /**
     * 호가 데이터 무결성 검증
     */
    private boolean validateOrderbookData(String stockCode, Object data) {
        try {
            if (data instanceof H0STASP0Data) {
                H0STASP0Data orderbookData = (H0STASP0Data) data;
                return validateH0STASP0Data(stockCode, orderbookData);
            }

            // RealtimeStockData.OrderbookData 형태인 경우
            if (data instanceof RealtimeStockData.OrderbookData) {
                RealtimeStockData.OrderbookData currentOrderbookData =
                    (RealtimeStockData.OrderbookData) data;

                // 주식코드 검증
                if (!stockCode.equals(currentOrderbookData.getStockCode())) {
                    log.warn("호가 데이터 주식코드 불일치 - Expected: {}, Actual: {}",
                            stockCode, currentOrderbookData.getStockCode());
                    return false;
                }

                // 타임스탬프 검증 (null이면 안됨)
                if (currentOrderbookData.getTimestamp() == null || currentOrderbookData.getTimestamp().isEmpty()) {
                    log.warn("호가 데이터 타임스탬프 누락 - StockCode: {}", stockCode);
                    return false;
                }

                // 내부 orderbookData 재귀 검증
                return validateOrderbookData(stockCode, currentOrderbookData.getOrderbookData());
            }

            // 기타 형태는 일단 통과
            log.debug("알 수 없는 호가 데이터 형태 - StockCode: {}, DataType: {}",
                    stockCode, data != null ? data.getClass().getSimpleName() : "null");
            return true;

        } catch (Exception e) {
            log.error("호가 데이터 검증 중 오류 - StockCode: {}", stockCode, e);
            return false;
        }
    }

    /**
     * H0STASP0Data (KIS 호가 데이터) 검증
     */
    private boolean validateH0STASP0Data(String stockCode, H0STASP0Data data) {
        try {
            // 기본적인 데이터 구조 검증
            if (data == null) {
                log.warn("H0STASP0Data가 null - StockCode: {}", stockCode);
                return false;
            }

            // KIS API 라이브러리의 데이터 구조를 모르므로 기본적인 검증만 수행
            // 추후 실제 필드명이 확인되면 더 구체적으로 검증 가능

            // toString()이 비어있거나 null이면 이상한 데이터로 판단
            String dataString = data.toString();
            if (dataString == null || dataString.trim().isEmpty() || "null".equals(dataString)) {
                log.warn("H0STASP0Data 내용이 비어있음 - StockCode: {}", stockCode);
                return false;
            }

            // 호가 데이터는 일반적으로 매수/매도 호가가 여러 단계로 구성됨
            // 매우 짧은 문자열(< 10자)이면 데이터가 제대로 없는 것으로 판단
            if (dataString.length() < 10) {
                log.warn("H0STASP0Data 내용이 너무 짧음 - StockCode: {}, DataLength: {}",
                        stockCode, dataString.length());
                return false;
            }

            log.debug("H0STASP0Data 검증 성공 - StockCode: {}, DataLength: {}",
                    stockCode, dataString.length());
            return true;

        } catch (Exception e) {
            log.error("H0STASP0Data 검증 중 오류 - StockCode: {}", stockCode, e);
            return false;
        }
    }

    /**
     * WebSocket 세션 연결 해제 이벤트 리스너
     */
    @EventListener
    public void handleSessionDisconnectEvent(WebSocketSessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String userId = event.getUserId();

        log.info("세션 연결 해제 이벤트 수신 - SessionId: {}, UserId: {}", sessionId, userId);

        // 세션 정리 수행 (기존 removeSession 로직)
        removeSession(sessionId);
    }

    /**
     * 현재 연결 상태 정보 조회
     */
    public Map<String, Object> getConnectionStatus() {
        return Map.of(
            "totalSessions", sessionActivity.size(),
            "activeSessions", sessionActivity.values().stream()
                .mapToLong(activity -> activity.isActive() ? 1 : 0).sum(),
            "totalSubscriptions", sessionSubscriptions.values().stream()
                .mapToInt(sub -> sub.getPriceSubscriptions().size() + sub.getOrderbookSubscriptions().size()).sum(),
            "heartbeatCounter", heartbeatCounter.get(),
            "lastHeartbeat", System.currentTimeMillis()
        );
    }
}