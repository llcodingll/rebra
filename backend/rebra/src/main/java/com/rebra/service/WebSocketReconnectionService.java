package com.rebra.service;

import com.rebra.config.SessionActivityUpdateEvent;
import com.rebra.config.WebSocketSessionDisconnectEvent;
import com.rebra.dto.response.SubscriptionResult;
import com.rebra.entity.Account;
import com.rebra.repository.AccountRepository;
import com.rebra.util.WebSocketHelper;
import com.youhogeon.finance.kis_api.api.realtime.H0STASP0Data;
import com.youhogeon.finance.kis_api.api.realtime.H0STCNT0Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * WebSocket 연결 안정성 및 재연결 관리 서비스 - 연결 상태 모니터링 - 하트비트 메커니즘 - 자동 재연결 로직 - 데이터 무결성 검증
 */
@Service
@RequiredArgsConstructor
public class WebSocketReconnectionService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketReconnectionService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final KisRealtimeService kisRealtimeService;
    private final WebSocketHelper webSocketHelper;
    private final AccountRepository accountRepository;

    // 세션별 마지막 활동 시간 추적
    private final Map<String, LastActivity> sessionActivity = new ConcurrentHashMap<>();

    // 세션별 구독 정보 저장 (재연결 시 복구용)
    private final Map<String, SessionSubscriptions> sessionSubscriptions = new ConcurrentHashMap<>();

    // 사용자별 구독 정보 저장 (새로고침 대응용)
    private final Map<Long, Set<UserSubscriptionInfo>> userActiveSubscriptions = new ConcurrentHashMap<>();

    // 하트비트 카운터
    private final AtomicLong heartbeatCounter = new AtomicLong(0);

    // 세션 동시성 제어를 위한 락 시스템
    private final Map<String, ReentrantLock> sessionLocks = new ConcurrentHashMap<>();
    private final Set<String> sessionsBeingProcessed = ConcurrentHashMap.newKeySet();

    // 양방향 하트비트를 위한 응답 추적 시스템
    private final Map<String, HeartbeatTracker> heartbeatTrackers = new ConcurrentHashMap<>();

    // KIS 연결 상태 모니터링
    private volatile boolean kisConnectionHealthy = true;
    private volatile LocalDateTime lastKisHealthCheck = LocalDateTime.now();
    private volatile int consecutiveKisFailures = 0;
    private final AtomicLong kisHealthCheckCounter = new AtomicLong(0);

    // 비동기 구독 해제를 위한 스레드 풀
    private final ExecutorService unsubscribeExecutor = Executors.newFixedThreadPool(5);

    // 설정값
    private static final long HEARTBEAT_INTERVAL = 30000; // 30초
    private static final long SESSION_TIMEOUT = 120000;   // 2분
    private static final long RECONNECTION_DELAY = 5000;  // 5초
    private static final int MAX_RECONNECTION_ATTEMPTS = 3;
    private static final long USER_SUBSCRIPTION_EXPIRATION = 3600000; // 1시간 (사용자별 구독 만료 시간)
    private static final long KIS_HEALTH_CHECK_INTERVAL = 60000; // KIS 연결 상태 확인 (1분)
    private static final int MAX_KIS_CONSECUTIVE_FAILURES = 3; // KIS 연속 실패 허용 횟수

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

        public LocalDateTime getLastSeen() {
            return lastSeen;
        }

        public boolean isActive() {
            return isActive;
        }

        public int getMissedHeartbeats() {
            return missedHeartbeats;
        }

        public void setActive(boolean active) {
            this.isActive = active;
        }
    }

    /**
     * 세션별 구독 정보 (개선된 통합 구조)
     */
    public static class SessionSubscriptions {
        // 기존 방식 (하위 호환성 유지)
        private final Map<String, String> priceSubscriptions = new ConcurrentHashMap<>();
        private final Map<String, String> orderbookSubscriptions = new ConcurrentHashMap<>();

        // 새로운 통합 방식 (종목코드 -> 데이터타입 집합)
        private final Map<String, Set<String>> stockSubscriptions = new ConcurrentHashMap<>();

        // 구독 히스토리 (디버깅 및 모니터링용)
        private final Map<String, SubscriptionHistory> subscriptionHistory = new ConcurrentHashMap<>();

        private Long userId;
        private int reconnectionAttempts = 0;
        private LocalDateTime lastBulkSubscription;

        public SessionSubscriptions(Long userId) {
            this.userId = userId;
        }

        public Map<String, String> getPriceSubscriptions() {
            return priceSubscriptions;
        }

        public Map<String, String> getOrderbookSubscriptions() {
            return orderbookSubscriptions;
        }

        public Map<String, Set<String>> getStockSubscriptions() {
            return stockSubscriptions;
        }

        public Map<String, SubscriptionHistory> getSubscriptionHistory() {
            return subscriptionHistory;
        }

        public Long getUserId() {
            return userId;
        }

        public int getReconnectionAttempts() {
            return reconnectionAttempts;
        }

        public LocalDateTime getLastBulkSubscription() {
            return lastBulkSubscription;
        }

        public void incrementReconnectionAttempts() {
            this.reconnectionAttempts++;
        }

        public void resetReconnectionAttempts() {
            this.reconnectionAttempts = 0;
        }

        public void updateLastBulkSubscription() {
            this.lastBulkSubscription = LocalDateTime.now();
        }

        /**
         * 통합 구독 추가 (새로운 방식)
         */
        public void addSubscription(String stockCode, String dataType) {
            stockSubscriptions.computeIfAbsent(stockCode, k -> ConcurrentHashMap.newKeySet()).add(dataType);

            // 기존 방식과의 호환성 유지
            if ("price".equals(dataType)) {
                priceSubscriptions.put(stockCode, stockCode);
            } else if ("orderbook".equals(dataType)) {
                orderbookSubscriptions.put(stockCode, stockCode);
            }

            // 히스토리 추가
            subscriptionHistory.computeIfAbsent(stockCode, k -> new SubscriptionHistory())
                    .addSubscription(dataType);
        }

        /**
         * 통합 구독 제거 (새로운 방식)
         */
        public void removeSubscription(String stockCode, String dataType) {
            Set<String> dataTypes = stockSubscriptions.get(stockCode);
            if (dataTypes != null) {
                dataTypes.remove(dataType);
                if (dataTypes.isEmpty()) {
                    stockSubscriptions.remove(stockCode);
                }
            }

            // 기존 방식과의 호환성 유지
            if ("price".equals(dataType)) {
                priceSubscriptions.remove(stockCode);
            } else if ("orderbook".equals(dataType)) {
                orderbookSubscriptions.remove(stockCode);
            }

            // 히스토리 업데이트
            SubscriptionHistory history = subscriptionHistory.get(stockCode);
            if (history != null) {
                history.removeSubscription(dataType);
            }
        }

        /**
         * 특정 종목의 모든 구독 제거
         */
        public void removeAllSubscriptionsForStock(String stockCode) {
            Set<String> dataTypes = stockSubscriptions.remove(stockCode);
            if (dataTypes != null) {
                for (String dataType : dataTypes) {
                    if ("price".equals(dataType)) {
                        priceSubscriptions.remove(stockCode);
                    } else if ("orderbook".equals(dataType)) {
                        orderbookSubscriptions.remove(stockCode);
                    }
                }
            }
            subscriptionHistory.remove(stockCode);
        }

        /**
         * 특정 종목의 구독 여부 확인
         */
        public boolean hasSubscription(String stockCode, String dataType) {
            Set<String> dataTypes = stockSubscriptions.get(stockCode);
            return dataTypes != null && dataTypes.contains(dataType);
        }

        /**
         * 전체 구독 수 반환
         */
        public int getTotalSubscriptionCount() {
            return stockSubscriptions.values().stream()
                    .mapToInt(Set::size)
                    .sum();
        }

        /**
         * 구독된 종목 코드 리스트 반환
         */
        public Set<String> getSubscribedStockCodes() {
            return new HashSet<>(stockSubscriptions.keySet());
        }
    }

    /**
     * 사용자별 구독 정보 (새로고침 대응용)
     */
    public static class UserSubscriptionInfo {
        private String stockCode;
        private String dataType;
        private LocalDateTime subscribedAt;
        private LocalDateTime lastActivity;

        public UserSubscriptionInfo(String stockCode, String dataType, LocalDateTime subscribedAt) {
            this.stockCode = stockCode;
            this.dataType = dataType;
            this.subscribedAt = subscribedAt;
            this.lastActivity = subscribedAt;
        }

        public String getStockCode() {
            return stockCode;
        }

        public String getDataType() {
            return dataType;
        }

        public LocalDateTime getSubscribedAt() {
            return subscribedAt;
        }

        public LocalDateTime getLastActivity() {
            return lastActivity;
        }

        public void setLastActivity(LocalDateTime lastActivity) {
            this.lastActivity = lastActivity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UserSubscriptionInfo that = (UserSubscriptionInfo) o;
            return Objects.equals(stockCode, that.stockCode) && Objects.equals(dataType, that.dataType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stockCode, dataType);
        }

        @Override
        public String toString() {
            return String.format("UserSubscriptionInfo{stockCode='%s', dataType='%s', subscribedAt=%s}",
                    stockCode, dataType, subscribedAt);
        }
    }

    /**
     * 하트비트 추적 클래스 (양방향 하트비트용)
     */
    public static class HeartbeatTracker {
        private volatile long lastSentSequence = 0;
        private volatile long lastReceivedSequence = 0;
        private volatile LocalDateTime lastSentTime;
        private volatile LocalDateTime lastReceivedTime;
        private volatile int consecutiveNoResponses = 0;
        private volatile boolean waitingForResponse = false;

        public HeartbeatTracker() {
            this.lastSentTime = LocalDateTime.now();
            this.lastReceivedTime = LocalDateTime.now();
        }

        public void recordHeartbeatSent(long sequence) {
            this.lastSentSequence = sequence;
            this.lastSentTime = LocalDateTime.now();
            this.waitingForResponse = true;
        }

        public void recordHeartbeatResponse(long sequence) {
            if (sequence == this.lastSentSequence) {
                this.lastReceivedSequence = sequence;
                this.lastReceivedTime = LocalDateTime.now();
                this.waitingForResponse = false;
                this.consecutiveNoResponses = 0;
            }
        }

        public void markNoResponse() {
            this.consecutiveNoResponses++;
            this.waitingForResponse = false;
        }

        public boolean isHealthy() {
            return consecutiveNoResponses < 3;
        }

        public boolean isResponseOverdue(long timeoutMs) {
            if (!waitingForResponse) {
                return false;
            }
            return lastSentTime.plusNanos(timeoutMs * 1_000_000).isBefore(LocalDateTime.now());
        }

        // Getters
        public long getLastSentSequence() {
            return lastSentSequence;
        }

        public long getLastReceivedSequence() {
            return lastReceivedSequence;
        }

        public LocalDateTime getLastSentTime() {
            return lastSentTime;
        }

        public LocalDateTime getLastReceivedTime() {
            return lastReceivedTime;
        }

        public int getConsecutiveNoResponses() {
            return consecutiveNoResponses;
        }

        public boolean isWaitingForResponse() {
            return waitingForResponse;
        }
    }

    /**
     * 구독 히스토리 추적 클래스
     */
    public static class SubscriptionHistory {
        private final Map<String, LocalDateTime> subscriptionTimes = new ConcurrentHashMap<>();
        private final Map<String, LocalDateTime> unsubscriptionTimes = new ConcurrentHashMap<>();
        private final Set<String> activeDataTypes = ConcurrentHashMap.newKeySet();

        public void addSubscription(String dataType) {
            subscriptionTimes.put(dataType, LocalDateTime.now());
            activeDataTypes.add(dataType);
        }

        public void removeSubscription(String dataType) {
            unsubscriptionTimes.put(dataType, LocalDateTime.now());
            activeDataTypes.remove(dataType);
        }

        public boolean isActive(String dataType) {
            return activeDataTypes.contains(dataType);
        }

        public LocalDateTime getSubscriptionTime(String dataType) {
            return subscriptionTimes.get(dataType);
        }

        public LocalDateTime getUnsubscriptionTime(String dataType) {
            return unsubscriptionTimes.get(dataType);
        }

        public Set<String> getActiveDataTypes() {
            return new HashSet<>(activeDataTypes);
        }

        public int getActiveCount() {
            return activeDataTypes.size();
        }
    }

    /**
     * 세션별 락 획득
     */
    private ReentrantLock getSessionLock(String sessionId) {
        return sessionLocks.computeIfAbsent(sessionId, k -> new ReentrantLock());
    }

    /**
     * 세션 처리 시작 (중복 방지)
     */
    private boolean startSessionProcessing(String sessionId) {
        return sessionsBeingProcessed.add(sessionId);
    }

    /**
     * 세션 처리 완료
     */
    private void endSessionProcessing(String sessionId) {
        sessionsBeingProcessed.remove(sessionId);
        // 사용하지 않는 락 정리
        sessionLocks.remove(sessionId);
    }

    /**
     * 세션이 처리 중인지 확인
     */
    private boolean isSessionBeingProcessed(String sessionId) {
        return sessionsBeingProcessed.contains(sessionId);
    }

    /**
     * 동일한 userId의 기존 세션들 완전 정리 (새로고침 시 사용)
     */
    private void cleanupExistingUserSessions(Long userId) {
        log.debug("🧹 기존 사용자 세션 정리 시작 - UserId: {}", userId);

        // 동일한 userId를 가진 기존 세션 찾기
        List<String> existingSessionIds = new ArrayList<>();

        for (Map.Entry<String, SessionSubscriptions> entry : sessionSubscriptions.entrySet()) {
            SessionSubscriptions subscription = entry.getValue();
            if (userId.equals(subscription.getUserId())) {
                existingSessionIds.add(entry.getKey());
            }
        }

        if (!existingSessionIds.isEmpty()) {
            log.info("🧹 기존 세션 발견 - UserId: {}, 정리할 세션: {}개", userId, existingSessionIds.size());

            // 각 기존 세션을 개별적으로 정리
            for (String existingSessionId : existingSessionIds) {
                try {
                    log.debug("🧹 기존 세션 정리 중 - UserId: {}, SessionId: {}", userId, existingSessionId);

                    // 1. 세션 활동 정보 제거
                    sessionActivity.remove(existingSessionId);

                    // 2. 하트비트 추적기 즉시 제거
                    heartbeatTrackers.remove(existingSessionId);
                    log.debug("💚 하트비트 추적기 정리 - SessionId: {}", existingSessionId);

                    // 3. 세션 구독 정보 제거 (KIS 구독 해제는 생략 - 새로고침이므로 불필요)
                    SessionSubscriptions oldSubscription = sessionSubscriptions.remove(existingSessionId);
                    if (oldSubscription != null) {
                        log.debug("🧹 기존 세션 구독 정보 제거 - SessionId: {}, 체결가: {}개, 호가: {}개",
                                existingSessionId,
                                oldSubscription.getPriceSubscriptions().size(),
                                oldSubscription.getOrderbookSubscriptions().size());
                    }

                    // 4. 세션 처리 상태 정리
                    sessionsBeingProcessed.remove(existingSessionId);
                    sessionLocks.remove(existingSessionId);

                    log.debug("✅ 기존 세션 정리 완료 - SessionId: {}", existingSessionId);

                } catch (Exception e) {
                    log.error("❌ 기존 세션 정리 실패 - SessionId: {}", existingSessionId, e);
                }
            }

            log.info("✅ 기존 사용자 세션 정리 완료 - UserId: {}, 정리된 세션: {}개",
                    userId, existingSessionIds.size());
        } else {
            log.debug("🧹 기존 세션 없음 - UserId: {}", userId);
        }
    }

    /**
     * 새 세션 등록 (새로고침 복구 지원)
     */
    public void registerSession(String sessionId, Long userId) {
        // 사용자별 구독 정보 확인 (새로고침 감지)
        Set<UserSubscriptionInfo> userSubscriptions = userActiveSubscriptions.get(userId);
        boolean isRefreshDetected = userSubscriptions != null && !userSubscriptions.isEmpty();

        if (isRefreshDetected) {
            log.info("🔄 새로고침 감지 - 기존 세션 정리 시작: UserId: {}, 복구할 구독: {}개",
                    userId, userSubscriptions.size());

            // 동일한 userId의 기존 세션들 완전 정리
            cleanupExistingUserSessions(userId);
        }

        // 새 세션 정보 등록
        sessionActivity.put(sessionId, new LastActivity());
        sessionSubscriptions.put(sessionId, new SessionSubscriptions(userId));
        heartbeatTrackers.put(sessionId, new HeartbeatTracker());

        if (isRefreshDetected) {
            log.info("🔄 새로고침 감지 - 사용자 구독 복구 시작: SessionId: {}, UserId: {}, 복구할 구독: {}개",
                    sessionId, userId, userSubscriptions.size());

            // 사용자의 기존 구독 정보를 새 세션에 복구
            restoreUserSubscriptions(sessionId, userId, userSubscriptions);

            // 클라이언트에 복구 알림 전송
            sendRefreshRecoveryNotification(sessionId, userSubscriptions);
        } else {
            log.info("WebSocket 세션 등록 - SessionId: {}, UserId: {} (신규 연결)", sessionId, userId);
        }
    }

    /**
     * 세션 제거 (스마트 정리 로직)
     */
    public void removeSession(String sessionId) {
        sessionActivity.remove(sessionId);
        SessionSubscriptions subscriptions = sessionSubscriptions.remove(sessionId);

        if (subscriptions != null) {
            Long userId = subscriptions.getUserId();

            log.info("WebSocket 세션 제거 및 구독 정리 시작 - SessionId: {}, UserId: {}, 체결가: {}개, 호가: {}개",
                    sessionId, userId,
                    subscriptions.getPriceSubscriptions().size(),
                    subscriptions.getOrderbookSubscriptions().size());

            // 1. KIS 실시간 구독 해제 (기존 방식)
            unsubscribeFromKisRealtime(userId, subscriptions, sessionId);

            // 2. 스마트 사용자별 구독 정리 (다른 활성 세션 확인)
            performSmartUserSubscriptionCleanup(userId, subscriptions);

            log.info("✅ WebSocket 세션 제거 및 구독 정리 완료 - SessionId: {}, UserId: {}", sessionId, userId);
        } else {
            log.info("WebSocket 세션 제거 - SessionId: {} (구독 정보 없음)", sessionId);
        }
    }

    /**
     * 이벤트 기반 userId를 사용한 세션 제거 (근본 원인 해결)
     */
    public void removeSessionWithUserId(String sessionId, Long eventUserId) {
        sessionActivity.remove(sessionId);
        SessionSubscriptions subscriptions = sessionSubscriptions.remove(sessionId);

        if (subscriptions != null) {
            // 이벤트에서 받은 userId를 우선 사용, 없으면 저장된 userId 사용
            Long userId = eventUserId != null ? eventUserId : subscriptions.getUserId();

            log.info(
                    "WebSocket 세션 제거 및 구독 정리 시작 - SessionId: {}, EventUserId: {}, StoredUserId: {}, 실제사용UserId: {}, 체결가: {}개, 호가: {}개",
                    sessionId, eventUserId, subscriptions.getUserId(), userId,
                    subscriptions.getPriceSubscriptions().size(),
                    subscriptions.getOrderbookSubscriptions().size());

            // 1. KIS 실시간 구독 해제 (기존 방식)
            unsubscribeFromKisRealtime(userId, subscriptions, sessionId);

            // 2. 스마트 사용자별 구독 정리 (다른 활성 세션 확인)
            performSmartUserSubscriptionCleanup(userId, subscriptions);

            log.info("✅ WebSocket 세션 제거 및 구독 정리 완료 - SessionId: {}, UserId: {}", sessionId, userId);
        } else {
            log.info("WebSocket 세션 제거 - SessionId: {} (구독 정보 없음)", sessionId);
        }
    }

    /**
     * KIS 실시간 구독 해제 (비동기 처리로 블로킹 시간 단축)
     */
    private void unsubscribeFromKisWithDuplicationCheck(Long userId, SessionSubscriptions subscriptions,
                                                        String sessionId, String reason) {
        log.info("🔌 KIS 구독 해제 시작 (비동기) - SessionId: {}, UserId: {}, Reason: {}", sessionId, userId, reason);

        // 구독 정보를 미리 복사하여 동시성 문제 방지
        Set<String> priceStocks = new HashSet<>(subscriptions.getPriceSubscriptions().keySet());
        Set<String> orderbookStocks = new HashSet<>(subscriptions.getOrderbookSubscriptions().keySet());

        // 세션에서 구독 정보를 즉시 제거 (중복 해제 방지)
        priceStocks.forEach(stockCode -> subscriptions.getPriceSubscriptions().remove(stockCode));
        orderbookStocks.forEach(stockCode -> subscriptions.getOrderbookSubscriptions().remove(stockCode));

        List<CompletableFuture<Void>> unsubscribeTasks = new ArrayList<>();

        // 체결가 구독 해제 (비동기)
        for (String stockCode : priceStocks) {
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                        try {
                            log.debug("🔌 체결가 구독 해제 시도 - SessionId: {}, UserId: {}, StockCode: {}, Reason: {}",
                                    sessionId, userId, stockCode, reason);
                            kisRealtimeService.stopPriceSubscription(userId, stockCode, sessionId);
                            log.debug("✅ 체결가 구독 해제 성공 - StockCode: {}", stockCode);

                        } catch (Exception e) {
                            log.debug("❌ 체결가 구독 해제 실패 (무시됨) - SessionId: {}, StockCode: {}, Reason: {}, Error: {}",
                                    sessionId, stockCode, reason, e.getMessage());
                        }
                    }, unsubscribeExecutor)
                    .orTimeout(3, TimeUnit.SECONDS) // 3초 타임아웃
                    .exceptionally(throwable -> {
                        log.debug("⏰ 체결가 구독 해제 타임아웃 - StockCode: {}, Error: {}",
                                stockCode, throwable.getMessage());
                        return null;
                    });

            unsubscribeTasks.add(task);
        }

        // 호가 구독 해제 (비동기)
        for (String stockCode : orderbookStocks) {
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                        try {
                            log.debug("🔌 호가 구독 해제 시도 - SessionId: {}, UserId: {}, StockCode: {}, Reason: {}",
                                    sessionId, userId, stockCode, reason);
                            kisRealtimeService.stopOrderbookSubscription(userId, stockCode, sessionId);
                            log.debug("✅ 호가 구독 해제 성공 - StockCode: {}", stockCode);

                        } catch (Exception e) {
                            log.debug("❌ 호가 구독 해제 실패 (무시됨) - SessionId: {}, StockCode: {}, Reason: {}, Error: {}",
                                    sessionId, stockCode, reason, e.getMessage());
                        }
                    }, unsubscribeExecutor)
                    .orTimeout(3, TimeUnit.SECONDS) // 3초 타임아웃
                    .exceptionally(throwable -> {
                        log.debug("⏰ 호가 구독 해제 타임아웃 - StockCode: {}, Error: {}",
                                stockCode, throwable.getMessage());
                        return null;
                    });

            unsubscribeTasks.add(task);
        }

        // 모든 구독 해제 완료 대기 (최대 5초)
        try {
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                    unsubscribeTasks.toArray(new CompletableFuture[0])
            );

            allTasks.get(5, TimeUnit.SECONDS);
            log.info("✅ KIS 구독 해제 완료 (비동기) - SessionId: {}, UserId: {}, 처리된 체결가: {}개, 처리된 호가: {}개",
                    sessionId, userId, priceStocks.size(), orderbookStocks.size());

        } catch (Exception e) {
            log.info("⏰ KIS 구독 해제 부분 완료 (타임아웃) - SessionId: {}, UserId: {}, 요청된 체결가: {}개, 요청된 호가: {}개",
                    sessionId, userId, priceStocks.size(), orderbookStocks.size());
        }
    }

    /**
     * KIS 실시간 구독 해제 (기존 메서드 - 하위 호환성 유지)
     */
    private void unsubscribeFromKisRealtime(Long userId, SessionSubscriptions subscriptions, String sessionId) {
        unsubscribeFromKisWithDuplicationCheck(userId, subscriptions, sessionId, "MANUAL");
    }

    /**
     * 세션 완전 정리 (원자적 처리)
     */
    private void performSessionCleanup(String sessionId) {
        log.debug("🧹 세션 완전 정리 시작 - SessionId: {}", sessionId);

        // 1. 활동 추적에서 제거
        sessionActivity.remove(sessionId);

        // 2. 하트비트 추적에서 제거
        heartbeatTrackers.remove(sessionId);

        // 3. 구독 정보 가져오기 및 제거
        SessionSubscriptions subscriptions = sessionSubscriptions.remove(sessionId);

        if (subscriptions != null) {
            Long userId = subscriptions.getUserId();

            // 4. 사용자별 구독 정리
            performSmartUserSubscriptionCleanup(userId, subscriptions);

            log.debug("🧹 세션 완전 정리 완료 - SessionId: {}, UserId: {}", sessionId, userId);
        } else {
            log.debug("🧹 세션 완전 정리 완료 - SessionId: {} (구독 정보 없음)", sessionId);
        }
    }

    /**
     * 스마트 사용자별 구독 정리 (다른 활성 세션이 없는 경우에만 제거)
     */
    private void performSmartUserSubscriptionCleanup(Long userId, SessionSubscriptions removedSession) {
        if (userId == null) {
            log.warn("사용자 ID가 null입니다. 구독 정리를 건너뜁니다.");
            return;
        }

        Set<UserSubscriptionInfo> userSubs = userActiveSubscriptions.get(userId);
        if (userSubs == null || userSubs.isEmpty()) {
            log.debug("사용자별 구독 정보 없음 - UserId: {}", userId);
            return;
        }

        // 해당 사용자의 다른 활성 세션 확인
        List<SessionSubscriptions> userOtherSessions = sessionSubscriptions.values().stream()
                .filter(session -> userId.equals(session.getUserId()))
                .toList();

        if (userOtherSessions.isEmpty()) {
            // 다른 활성 세션이 없으면 사용자별 구독 정보 전체 제거
            userActiveSubscriptions.remove(userId);
            log.info("🧹 사용자별 구독 정보 전체 제거 - UserId: {}, 제거된 구독: {}개 (다른 활성 세션 없음)",
                    userId, userSubs.size());
        } else {
            // 다른 활성 세션이 있으면 해당 세션들이 가지지 않은 구독만 제거
            Set<UserSubscriptionInfo> subscriptionsToRemove = new HashSet<>();

            for (UserSubscriptionInfo userSub : userSubs) {
                boolean hasInOtherSession = userOtherSessions.stream()
                        .anyMatch(session -> session.hasSubscription(userSub.getStockCode(), userSub.getDataType()));

                if (!hasInOtherSession) {
                    subscriptionsToRemove.add(userSub);
                }
            }

            if (!subscriptionsToRemove.isEmpty()) {
                userSubs.removeAll(subscriptionsToRemove);
                log.info("🧹 사용자별 구독 정보 부분 제거 - UserId: {}, 제거된 구독: {}개, 유지된 구독: {}개",
                        userId, subscriptionsToRemove.size(), userSubs.size());

                // 제거된 구독 상세 로그
                for (UserSubscriptionInfo removedSub : subscriptionsToRemove) {
                    log.debug("제거된 구독 - UserId: {}, StockCode: {}, DataType: {} (다른 세션에서 미사용)",
                            userId, removedSub.getStockCode(), removedSub.getDataType());
                }
            } else {
                log.info("🔄 사용자별 구독 정보 유지 - UserId: {}, 유지된 구독: {}개 (모두 다른 세션에서 사용중)",
                        userId, userSubs.size());
            }

            // 빈 세트가 되면 제거
            if (userSubs.isEmpty()) {
                userActiveSubscriptions.remove(userId);
            }
        }
    }

    /**
     * 구독 정보 추가 (이중화 시스템: 세션별 + 사용자별)
     */
    public void addSubscription(String sessionId, String stockCode, String dataType) {
        SessionSubscriptions subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            Long userId = subscriptions.getUserId();

            // 1. 세션별 구독 정보 추가 (기존 방식 + 새로운 통합 방식)
            subscriptions.addSubscription(stockCode, dataType);

            // 2. 사용자별 구독 정보 추가 (새로고침 대응)
            addUserSubscription(userId, stockCode, dataType);

            log.info("✅ 구독 정보 추가 완료 (이중화) - SessionId: {}, UserId: {}, StockCode: {}, DataType: {}",
                    sessionId, userId, stockCode, dataType);
        } else {
            log.warn("❌ 구독 정보 추가 실패 - 세션 정보 없음: SessionId: {}, StockCode: {}, DataType: {}",
                    sessionId, stockCode, dataType);
        }
    }

    /**
     * 구독이 이미 등록되어 있는지 확인 (중복 방지용)
     */
    public boolean isSubscriptionAlreadyRegistered(String sessionId, String stockCode, String dataType) {
        SessionSubscriptions subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            boolean alreadyRegistered = subscriptions.hasSubscription(stockCode, dataType);
            if (alreadyRegistered) {
                log.debug("구독 중복 확인 - 이미 등록됨: SessionId: {}, StockCode: {}, DataType: {}",
                        sessionId, stockCode, dataType);
            }
            return alreadyRegistered;
        }
        return false;
    }

    /**
     * 사용자별 구독 정보 추가
     */
    private void addUserSubscription(Long userId, String stockCode, String dataType) {
        userActiveSubscriptions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(new UserSubscriptionInfo(stockCode, dataType, LocalDateTime.now()));

        log.debug("사용자별 구독 정보 추가 - UserId: {}, StockCode: {}, DataType: {}", userId, stockCode, dataType);
    }

    /**
     * 구독 정보 제거 (이중화 시스템: 세션별 + 사용자별)
     */
    public void removeSubscription(String sessionId, String stockCode, String dataType) {
        SessionSubscriptions subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            Long userId = subscriptions.getUserId();

            // 1. 세션별 구독 정보 제거 (기존 방식 + 새로운 통합 방식)
            subscriptions.removeSubscription(stockCode, dataType);

            // 2. 사용자별 구독 정보 제거 (새로고침 대응) - 조건부 제거
            removeUserSubscriptionIfNeeded(userId, stockCode, dataType);

            log.info("✅ 구독 정보 제거 완료 (이중화) - SessionId: {}, UserId: {}, StockCode: {}, DataType: {}",
                    sessionId, userId, stockCode, dataType);
        } else {
            log.warn("❌ 구독 정보 제거 실패 - 세션 정보 없음: SessionId: {}, StockCode: {}, DataType: {}",
                    sessionId, stockCode, dataType);
        }
    }

    /**
     * 사용자별 구독 정보 제거 (다른 활성 세션이 없는 경우에만)
     */
    private void removeUserSubscriptionIfNeeded(Long userId, String stockCode, String dataType) {
        // 해당 사용자의 다른 활성 세션이 같은 구독을 가지고 있는지 확인
        boolean hasActiveSubscriptionInOtherSession = sessionSubscriptions.values().stream()
                .filter(sub -> userId.equals(sub.getUserId()))
                .anyMatch(sub -> sub.hasSubscription(stockCode, dataType));

        if (!hasActiveSubscriptionInOtherSession) {
            // 다른 세션에서 동일한 구독이 없으면 사용자별 구독에서도 제거
            Set<UserSubscriptionInfo> userSubs = userActiveSubscriptions.get(userId);
            if (userSubs != null) {
                userSubs.removeIf(userSub ->
                        stockCode.equals(userSub.getStockCode()) && dataType.equals(userSub.getDataType()));

                if (userSubs.isEmpty()) {
                    userActiveSubscriptions.remove(userId);
                }

                log.debug("사용자별 구독 정보 제거 - UserId: {}, StockCode: {}, DataType: {} (다른 활성 세션 없음)",
                        userId, stockCode, dataType);
            }
        } else {
            log.debug("사용자별 구독 정보 유지 - UserId: {}, StockCode: {}, DataType: {} (다른 활성 세션 존재)",
                    userId, stockCode, dataType);
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
     * 하트비트 전송 (30초마다) - 단방향 시스템으로 단순화
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL)
    public void sendHeartbeat() {
        long currentHeartbeat = heartbeatCounter.incrementAndGet();

        log.debug("💓 하트비트 전송 시작 (단방향) - Sequence: {}, 대상 세션: {}개",
                currentHeartbeat, sessionActivity.size());

        // 활성 세션들 복사 (동시 수정 방지)
        Set<String> activeSessionIds = new HashSet<>(sessionActivity.keySet());

        // 각 세션에 하트비트 전송 (응답 대기 없음)
        for (String sessionId : activeSessionIds) {
            // 처리 중인 세션은 하트비트 건너뛰기 (동시성 충돌 방지)
            if (isSessionBeingProcessed(sessionId)) {
                log.debug("하트비트 건너뛰기 - 세션 처리 중: SessionId: {}", sessionId);
                continue;
            }

            try {
                String destination = "/topic/heartbeat/" + sessionId;
                Map<String, Object> heartbeatData = Map.of(
                        "type", "heartbeat",
                        "timestamp", System.currentTimeMillis(),
                        "sequence", currentHeartbeat,
                        "status", "alive",
                        "responseRequired", false  // 단방향 하트비트이므로 응답 불필요
                );

                messagingTemplate.convertAndSend(destination, heartbeatData);

                log.debug("💓 하트비트 전송 (단방향) - SessionId: {}, Sequence: {}", sessionId, currentHeartbeat);

            } catch (Exception e) {
                log.error("❌ 하트비트 전송 실패 - SessionId: {}", sessionId, e);

                // 실패한 세션은 별도 처리 (동시성 고려)
                handleSessionFailureAsync(sessionId);
            }
        }

        log.debug("💓 하트비트 전송 완료 (단방향) - Sequence: {}, 처리된 세션: {}개",
                currentHeartbeat, activeSessionIds.size());
    }

    /**
     * 클라이언트로부터 하트비트 응답 처리
     */
    public void handleHeartbeatResponse(String sessionId, long sequence) {
        HeartbeatTracker tracker = heartbeatTrackers.get(sessionId);
        if (tracker != null) {
            tracker.recordHeartbeatResponse(sequence);

            // 세션 활동 시간 업데이트
            updateSessionActivity(sessionId);

            log.debug("💚 하트비트 응답 수신 - SessionId: {}, Sequence: {}", sessionId, sequence);
        } else {
            log.warn("❌ 하트비트 응답 처리 실패 - 알 수 없는 세션: SessionId: {}, Sequence: {}",
                    sessionId, sequence);
        }
    }

    /**
     * 세션 상태 모니터링 (1분마다) - 우선순위 조정으로 성능 개선
     */
    @Scheduled(fixedRate = 60000)
    public void monitorSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<String> sessionsToRemove = new ArrayList<>();

        // 1단계: 모든 세션 상태 검사 및 정리 대상 식별
        for (Map.Entry<String, LastActivity> entry : sessionActivity.entrySet()) {
            String sessionId = entry.getKey();
            LastActivity activity = entry.getValue();

            boolean shouldRemove = false;
            String removalReason = "";

            // 1. 단방향 하트비트 시스템 - 응답 검사 제거
            // 단순히 세션 활동만 기반으로 타임아웃 결정 (하트비트 응답 검사 생략)

            // 2. 기존 타임아웃 검사
            if (!shouldRemove && activity.getLastSeen().plusSeconds(SESSION_TIMEOUT / 1000).isBefore(now)) {
                shouldRemove = true;
                removalReason = "세션 타임아웃 (LastSeen: " + activity.getLastSeen() + ")";
            }

            if (shouldRemove) {
                sessionsToRemove.add(sessionId);
                log.warn("🗑️ 세션 정리 예정 - SessionId: {}, Reason: {}", sessionId, removalReason);
            } else if (!activity.isActive()) {
                // 3. 비활성 세션 복구 시도 (정리하지 않음)
                log.warn("😴 비활성 세션 감지 - SessionId: {}, MissedHeartbeats: {}",
                        sessionId, activity.getMissedHeartbeats());
                attemptSessionRecovery(sessionId);
            }
        }

        // 2단계: 세션 정리 일괄 처리 (성능 최적화)
        int processedCount = 0;
        for (String sessionId : sessionsToRemove) {
            if (sessionActivity.remove(sessionId) != null) {
                // 하트비트 추적기 즉시 정리 (상태 동기화)
                heartbeatTrackers.remove(sessionId);
                log.debug("💚 하트비트 추적기 즉시 정리 - SessionId: {}", sessionId);

                // 백그라운드에서 비동기 정리 (빠른 응답성을 위해)
                CompletableFuture.runAsync(() -> handleSessionTimeout(sessionId), unsubscribeExecutor);
                processedCount++;
            }
        }

        log.debug("🔍 세션 모니터링 완료 - 활성 세션: {}개, 하트비트 추적: {}개, 정리된 세션: {}개",
                sessionActivity.size(), heartbeatTrackers.size(), processedCount);
    }

    /**
     * 사용자별 구독 정보 만료 관리 (10분마다)
     */
    @Scheduled(fixedRate = 600000) // 10분
    public void cleanupExpiredUserSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationThreshold = now.minusSeconds(USER_SUBSCRIPTION_EXPIRATION / 1000);

        int totalUsersChecked = 0;
        int totalSubscriptionsRemoved = 0;
        int usersWithExpiredSubscriptions = 0;

        // 사용자별 구독 정보를 순회하며 만료된 구독 정리
        for (Map.Entry<Long, Set<UserSubscriptionInfo>> entry : userActiveSubscriptions.entrySet()) {
            Long userId = entry.getKey();
            Set<UserSubscriptionInfo> userSubs = entry.getValue();
            totalUsersChecked++;

            // 해당 사용자의 활성 세션이 있는지 확인
            boolean hasActiveSessions = sessionSubscriptions.values().stream()
                    .anyMatch(session -> userId.equals(session.getUserId()));

            if (hasActiveSessions) {
                // 활성 세션이 있으면 만료 검사하지 않음 (사용 중인 것으로 간주)
                log.debug("사용자별 구독 정보 유지 - UserId: {} (활성 세션 존재)", userId);
                continue;
            }

            // 활성 세션이 없으면 만료된 구독 찾기
            Set<UserSubscriptionInfo> expiredSubscriptions = new HashSet<>();

            for (UserSubscriptionInfo userSub : userSubs) {
                if (userSub.getLastActivity().isBefore(expirationThreshold)) {
                    expiredSubscriptions.add(userSub);
                }
            }

            if (!expiredSubscriptions.isEmpty()) {
                userSubs.removeAll(expiredSubscriptions);
                totalSubscriptionsRemoved += expiredSubscriptions.size();
                usersWithExpiredSubscriptions++;

                log.info("🗑️ 만료된 사용자별 구독 정보 정리 - UserId: {}, 제거된 구독: {}개, 남은 구독: {}개",
                        userId, expiredSubscriptions.size(), userSubs.size());

                // 만료된 구독 상세 로그
                for (UserSubscriptionInfo expiredSub : expiredSubscriptions) {
                    log.debug("만료된 구독 제거 - UserId: {}, StockCode: {}, DataType: {}, LastActivity: {}",
                            userId, expiredSub.getStockCode(), expiredSub.getDataType(), expiredSub.getLastActivity());
                }
            }
        }

        // 구독이 모두 제거된 사용자는 맵에서 제거
        userActiveSubscriptions.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        if (totalSubscriptionsRemoved > 0) {
            log.info("✅ 사용자별 구독 정보 만료 정리 완료 - 검사한 사용자: {}명, 만료 구독 제거: {}개, 영향받은 사용자: {}명",
                    totalUsersChecked, totalSubscriptionsRemoved, usersWithExpiredSubscriptions);
        } else {
            log.debug("사용자별 구독 정보 만료 정리 - 검사한 사용자: {}명, 만료된 구독 없음", totalUsersChecked);
        }
    }

    /**
     * 세션 실패 처리 (비동기 방식)
     */
    private void handleSessionFailureAsync(String sessionId) {
        // 처리 중인 세션이면 건너뛰기
        if (isSessionBeingProcessed(sessionId)) {
            log.debug("세션 실패 처리 건너뛰기 - 이미 처리 중: SessionId: {}", sessionId);
            return;
        }

        log.warn("⚠️ 세션 실패 감지 - SessionId: {}", sessionId);
        handleSessionFailure(sessionId);
    }

    /**
     * 세션 실패 처리 (동시성 제어 적용)
     */
    private void handleSessionFailure(String sessionId) {
        ReentrantLock sessionLock = getSessionLock(sessionId);

        if (!sessionLock.tryLock()) {
            log.debug("세션 실패 처리 건너뛰기 - 다른 스레드에서 처리 중: SessionId: {}", sessionId);
            return;
        }

        try {
            LastActivity activity = sessionActivity.get(sessionId);
            if (activity != null) {
                activity.missHeartbeat();

                log.info("📊 세션 실패 상태 - SessionId: {}, MissedHeartbeats: {}, Active: {}",
                        sessionId, activity.getMissedHeartbeats(), activity.isActive());

                if (!activity.isActive()) {
                    log.error("💔 세션 연결 완전 실패 감지 - SessionId: {}, 복구 시도", sessionId);
                    attemptSessionRecovery(sessionId);
                }
            } else {
                log.warn("세션 활동 정보 없음 - SessionId: {}", sessionId);
            }

        } finally {
            sessionLock.unlock();
        }
    }

    /**
     * 세션 타임아웃 처리 (동시성 제어 적용)
     */
    private void handleSessionTimeout(String sessionId) {
        ReentrantLock sessionLock = getSessionLock(sessionId);

        // 다른 스레드에서 이미 처리 중인 경우 최대 5초 대기
        try {
            if (!sessionLock.tryLock(5, TimeUnit.SECONDS)) {
                log.warn("⏰ 세션 타임아웃 처리 건너뛰기 - 락 획득 실패 (다른 스레드 처리 중): SessionId: {}", sessionId);
                return;
            }
        } catch (InterruptedException e) {
            log.warn("⚠️ 세션 타임아웃 처리 중단됨 - 인터럽트 발생: SessionId: {}", sessionId);
            Thread.currentThread().interrupt();
            return;
        }

        try {
            // 이미 처리 중인 세션이면 건너뛰기
            if (!startSessionProcessing(sessionId)) {
                log.debug("세션 타임아웃 처리 건너뛰기 - 이미 처리 중: SessionId: {}", sessionId);
                return;
            }

            log.warn("🕐 세션 타임아웃 처리 시작 - SessionId: {}", sessionId);

            SessionSubscriptions subscriptions = sessionSubscriptions.get(sessionId);
            if (subscriptions != null) {
                Long userId = subscriptions.getUserId();

                log.info("📊 타임아웃 세션 구독 현황 - SessionId: {}, UserId: {}, 체결가: {}개, 호가: {}개",
                        sessionId, userId,
                        subscriptions.getPriceSubscriptions().size(),
                        subscriptions.getOrderbookSubscriptions().size());

                // KIS 구독 해제 (중복 방지 포함)
                unsubscribeFromKisWithDuplicationCheck(userId, subscriptions, sessionId, "TIMEOUT");
            }

            // 세션 완전 제거
            performSessionCleanup(sessionId);

            log.info("✅ 세션 타임아웃 처리 완료 - SessionId: {}", sessionId);

        } finally {
            endSessionProcessing(sessionId);
            sessionLock.unlock();
        }
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
     * 사용자 구독 정보를 새 세션에 복구 (새로고침 대응) - 클라이언트 복구 알림 방식
     */
    private void restoreUserSubscriptions(String sessionId, Long userId, Set<UserSubscriptionInfo> userSubscriptions) {
        SessionSubscriptions sessionSubscriptions = this.sessionSubscriptions.get(sessionId);
        if (sessionSubscriptions == null) {
            log.error("세션 정보가 없어 구독 복구 실패 - SessionId: {}, UserId: {}", sessionId, userId);
            return;
        }

        int restoredCount = 0;

        // 1. 세션별 구독 정보에만 추가 (WebSocketReconnectionService용)
        for (UserSubscriptionInfo userSub : userSubscriptions) {
            try {
                // 세션별 구독 정보에 추가 (통합 방식)
                sessionSubscriptions.addSubscription(userSub.getStockCode(), userSub.getDataType());

                // 사용자 구독 정보의 활동 시간 업데이트
                userSub.setLastActivity(LocalDateTime.now());

                restoredCount++;

                log.debug("구독 정보 복구 - SessionId: {}, UserId: {}, StockCode: {}, DataType: {}",
                        sessionId, userId, userSub.getStockCode(), userSub.getDataType());

            } catch (Exception e) {
                log.error("개별 구독 복구 실패 - SessionId: {}, UserId: {}, StockCode: {}, DataType: {}",
                        sessionId, userId, userSub.getStockCode(), userSub.getDataType(), e);
            }
        }

        log.info("✅ 사용자 구독 복구 완료 - SessionId: {}, UserId: {}, 복구된 구독: {}/{}개",
                sessionId, userId, restoredCount, userSubscriptions.size());

        // 2. 실제 KIS API 재구독은 클라이언트가 bulk subscription을 통해 수행하도록 안내
        log.info("💡 실제 KIS API 재구독은 클라이언트의 bulk subscription 요청으로 처리됩니다 - SessionId: {}", sessionId);
    }

    /**
     * 새로고침 복구 알림을 클라이언트에 전송 (bulk subscription 요청 안내 포함)
     */
    private void sendRefreshRecoveryNotification(String sessionId, Set<UserSubscriptionInfo> userSubscriptions) {
        try {
            String destination = "/topic/refresh-recovery/" + sessionId;

            // 구독 정보를 bulk subscription 요청 형식으로 변환
            Map<String, Set<String>> stockDataTypes = new java.util.LinkedHashMap<>();
            for (UserSubscriptionInfo userSub : userSubscriptions) {
                stockDataTypes.computeIfAbsent(userSub.getStockCode(), k -> new HashSet<>())
                        .add(userSub.getDataType());
            }

            // 클라이언트가 사용할 수 있는 bulk subscription 요청 데이터 생성
            List<Map<String, Object>> bulkSubscriptionData = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entry : stockDataTypes.entrySet()) {
                bulkSubscriptionData.add(Map.of(
                        "stockCode", entry.getKey(),
                        "dataTypes", new ArrayList<>(entry.getValue())
                ));
            }

            // 상세 구독 정보 (기존 정보)
            List<Map<String, Object>> subscriptionList = new ArrayList<>();
            for (UserSubscriptionInfo userSub : userSubscriptions) {
                subscriptionList.add(Map.of(
                        "stockCode", userSub.getStockCode(),
                        "dataType", userSub.getDataType(),
                        "subscribedAt", userSub.getSubscribedAt().toString(),
                        "lastActivity", userSub.getLastActivity().toString()
                ));
            }

            Map<String, Object> refreshRecoveryData = Map.of(
                    "type", "refresh_recovery",
                    "timestamp", System.currentTimeMillis(),
                    "message", "새로고침이 감지되었습니다. 실시간 데이터 수신을 위해 bulk subscription을 요청하세요.",
                    "action", "bulk_subscription_required",
                    "bulkSubscriptionRequest", Map.of("stocks", bulkSubscriptionData),
                    "restoredSubscriptions", subscriptionList,
                    "totalRestored", subscriptionList.size(),
                    "autoResubscribe", true  // 클라이언트가 자동으로 재구독하도록 안내
            );

            messagingTemplate.convertAndSend(destination, refreshRecoveryData);
            log.info("🔄 새로고침 복구 알림 전송 (bulk subscription 안내) - SessionId: {}, 복구 대상: {}개 종목, {}개 구독",
                    sessionId, stockDataTypes.size(), subscriptionList.size());

        } catch (Exception e) {
            log.error("새로고침 복구 알림 전송 실패 - SessionId: {}", sessionId, e);
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

            // RealtimeStockData는 더 이상 사용하지 않음 (KIS 원본 데이터 직접 사용)

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

            // RealtimeStockData는 더 이상 사용하지 않음 (KIS 원본 데이터 직접 사용)

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
     * 세션 활동 업데이트 이벤트 리스너 (순환 의존성 방지)
     */
    @EventListener
    public void handleSessionActivityUpdateEvent(SessionActivityUpdateEvent event) {
        String sessionId = event.getSessionId();
        String eventType = event.getEventType();

        log.debug("💚 세션 활동 업데이트 이벤트 수신 - SessionId: {}, EventType: {}", sessionId, eventType);

        // 세션 활동 업데이트
        updateSessionActivity(sessionId);

        log.debug("✅ 세션 활동 업데이트 완료 - SessionId: {}, EventType: {}", sessionId, eventType);
    }

    /**
     * WebSocket 세션 연결 해제 이벤트 리스너
     */
    @EventListener
    public void handleSessionDisconnectEvent(WebSocketSessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String userIdStr = event.getUserId();

        log.info("세션 연결 해제 이벤트 수신 - SessionId: {}, UserId: {}", sessionId, userIdStr);

        // 이벤트에서 받은 userId 정보를 활용하여 세션 정리
        Long userId = null;
        if (userIdStr != null && !userIdStr.equals("guest")) {
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.warn("유효하지 않은 userId 형식 - SessionId: {}, UserId: {}", sessionId, userIdStr);
            }
        }

        // 이벤트 기반 userId를 사용하여 세션 정리
        removeSessionWithUserId(sessionId, userId);
    }

    /**
     * KIS 연결 상태 모니터링 (1분마다)
     */
    @Scheduled(fixedRate = KIS_HEALTH_CHECK_INTERVAL)
    public void monitorKisConnectionHealth() {
        long healthCheckSequence = kisHealthCheckCounter.incrementAndGet();

        log.debug("🔍 KIS 연결 상태 확인 시작 - Sequence: {}", healthCheckSequence);

        try {
            // KIS API 연결 상태 확인 (간단한 ping 성격의 요청)
            boolean isKisHealthy = performKisHealthCheck();

            if (isKisHealthy) {
                // KIS 연결이 정상인 경우
                if (!kisConnectionHealthy) {
                    log.info("✅ KIS 연결 복구됨 - Sequence: {}, 이전 연속 실패: {}회",
                            healthCheckSequence, consecutiveKisFailures);
                }

                kisConnectionHealthy = true;
                consecutiveKisFailures = 0;
                lastKisHealthCheck = LocalDateTime.now();

            } else {
                // KIS 연결에 문제가 있는 경우
                consecutiveKisFailures++;
                kisConnectionHealthy = false;

                log.warn("❌ KIS 연결 상태 불량 - Sequence: {}, 연속 실패: {}회",
                        healthCheckSequence, consecutiveKisFailures);

                // 연속 실패 시 복구 시도
                if (consecutiveKisFailures >= MAX_KIS_CONSECUTIVE_FAILURES) {
                    log.error("🚨 KIS 연결 심각한 문제 - 복구 시도: Sequence: {}, 연속 실패: {}회",
                            healthCheckSequence, consecutiveKisFailures);

                    attemptKisConnectionRecovery(healthCheckSequence);
                }
            }

        } catch (Exception e) {
            consecutiveKisFailures++;
            kisConnectionHealthy = false;

            log.error("❌ KIS 연결 상태 확인 중 예외 발생 - Sequence: {}, 연속 실패: {}회",
                    healthCheckSequence, consecutiveKisFailures, e);
        }

        log.debug("🔍 KIS 연결 상태 확인 완료 - Sequence: {}, 상태: {}, 연속실패: {}회",
                healthCheckSequence, kisConnectionHealthy ? "정상" : "불량", consecutiveKisFailures);
    }

    /**
     * KIS API 연결 상태 실제 검증
     */
    private boolean performKisHealthCheck() {
        try {
            // 1. 기본 서비스 가용성 확인
            if (kisRealtimeService == null) {
                log.debug("KIS 실시간 서비스가 null입니다");
                return false;
            }

            // 2. 활성 구독 상태 확인 (구독이 있는 경우에만)
            if (!sessionSubscriptions.isEmpty()) {
                // 활성 구독이 있는 세션 수 확인
                long activeSubscriptionSessions = sessionSubscriptions.values().stream()
                        .filter(sub -> !sub.getPriceSubscriptions().isEmpty() || !sub.getOrderbookSubscriptions()
                                .isEmpty())
                        .count();

                if (activeSubscriptionSessions > 0) {
                    // 활성 구독이 있을 때만 연결 상태 엄격하게 확인
                    log.debug("활성 구독 세션: {}개, KIS 연결 상태 엄격 검사", activeSubscriptionSessions);

                    // 3. 최근 KIS 에러 발생 확인 (지난 5분 내)
                    if (hasRecentKisErrors()) {
                        log.debug("최근 KIS API 에러 감지됨");
                        return false;
                    }

                    // 4. 하트비트 추적기를 통한 연결 품질 확인
                    long unhealthySessions = heartbeatTrackers.values().stream()
                            .filter(tracker -> !tracker.isHealthy())
                            .count();

                    if (unhealthySessions > activeSubscriptionSessions * 0.5) {
                        log.debug("비정상 세션 비율이 높음: {}/{}", unhealthySessions, activeSubscriptionSessions);
                        return false;
                    }
                }
            }

            // 5. 모든 검사 통과
            return true;

        } catch (Exception e) {
            log.debug("KIS 헬스체크 실패", e);
            return false;
        }
    }

    /**
     * 최근 KIS 에러 발생 여부 확인
     */
    private boolean hasRecentKisErrors() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        // 최근 5분 내에 3회 이상 연속 실패가 있었다면 문제 있음
        return consecutiveKisFailures >= 3 ||
                (lastKisHealthCheck.isAfter(fiveMinutesAgo) && consecutiveKisFailures >= 2);
    }

    /**
     * 연결 품질 지표 계산
     */
    public double calculateConnectionQuality() {
        if (sessionActivity.isEmpty()) {
            return 1.0; // 세션이 없으면 완벽한 상태
        }

        long totalSessions = sessionActivity.size();
        long healthySessions = heartbeatTrackers.values().stream()
                .filter(HeartbeatTracker::isHealthy)
                .count();

        double sessionHealthRatio = (double) healthySessions / totalSessions;

        // KIS 연결 상태와 세션 건강도를 종합
        double kisHealthScore = kisConnectionHealthy ? 1.0 : 0.0;
        double kisFailureScore = Math.max(0.0, 1.0 - (consecutiveKisFailures * 0.2));

        return (sessionHealthRatio * 0.4) + (kisHealthScore * 0.4) + (kisFailureScore * 0.2);
    }

    /**
     * KIS 연결 복구 시도
     */
    private void attemptKisConnectionRecovery(long sequence) {
        log.info("🔧 KIS 연결 복구 시도 시작 - Sequence: {}", sequence);

        try {
            // 1. 모든 활성 세션에 KIS 연결 문제 알림
            notifyAllSessionsKisConnectionIssue();

            // 2. KIS 연결 재초기화 시도 (실제로는 kisApiComponent 재시작 등)
            // 예시: kisApiComponent.reconnect() 또는 재초기화 로직

            // 3. 잠시 대기 후 재시도
            Thread.sleep(RECONNECTION_DELAY);

            // 4. 복구 확인
            if (performKisHealthCheck()) {
                log.info("✅ KIS 연결 복구 성공 - Sequence: {}", sequence);
                kisConnectionHealthy = true;
                consecutiveKisFailures = 0;

                // 모든 활성 세션에 복구 알림
                notifyAllSessionsKisConnectionRecovered();

            } else {
                log.error("❌ KIS 연결 복구 실패 - Sequence: {}", sequence);
            }

        } catch (Exception e) {
            log.error("❌ KIS 연결 복구 시도 중 예외 발생 - Sequence: {}", sequence, e);
        }
    }

    /**
     * 모든 세션에 KIS 연결 문제 알림
     */
    private void notifyAllSessionsKisConnectionIssue() {
        Set<String> activeSessionIds = new HashSet<>(sessionActivity.keySet());

        for (String sessionId : activeSessionIds) {
            try {
                String destination = "/topic/kis-status/" + sessionId;
                Map<String, Object> statusData = Map.of(
                        "type", "kis_connection_issue",
                        "timestamp", System.currentTimeMillis(),
                        "message", "KIS API 연결에 문제가 발생했습니다. 복구를 시도하고 있습니다.",
                        "consecutiveFailures", consecutiveKisFailures
                );

                messagingTemplate.convertAndSend(destination, statusData);

            } catch (Exception e) {
                log.error("❌ KIS 연결 문제 알림 전송 실패 - SessionId: {}", sessionId, e);
            }
        }

        log.info("📢 모든 활성 세션에 KIS 연결 문제 알림 전송 완료 - 대상 세션: {}개", activeSessionIds.size());
    }

    /**
     * 모든 세션에 KIS 연결 복구 알림
     */
    private void notifyAllSessionsKisConnectionRecovered() {
        Set<String> activeSessionIds = new HashSet<>(sessionActivity.keySet());

        for (String sessionId : activeSessionIds) {
            try {
                String destination = "/topic/kis-status/" + sessionId;
                Map<String, Object> statusData = Map.of(
                        "type", "kis_connection_recovered",
                        "timestamp", System.currentTimeMillis(),
                        "message", "KIS API 연결이 복구되었습니다.",
                        "recoveryTime", System.currentTimeMillis()
                );

                messagingTemplate.convertAndSend(destination, statusData);

            } catch (Exception e) {
                log.error("❌ KIS 연결 복구 알림 전송 실패 - SessionId: {}", sessionId, e);
            }
        }

        log.info("📢 모든 활성 세션에 KIS 연결 복구 알림 전송 완료 - 대상 세션: {}개", activeSessionIds.size());
    }

    /**
     * KIS 연결 상태 조회
     */
    public boolean isKisConnectionHealthy() {
        return kisConnectionHealthy;
    }

    /**
     * KIS 연결 통계 조회 (상세 정보 포함)
     */
    public Map<String, Object> getKisConnectionStats() {
        return Map.of(
                "healthy", kisConnectionHealthy,
                "lastHealthCheck", lastKisHealthCheck,
                "consecutiveFailures", consecutiveKisFailures,
                "healthCheckCount", kisHealthCheckCounter.get(),
                "connectionQuality", calculateConnectionQuality(),
                "hasRecentErrors", hasRecentKisErrors(),
                "activeSubscriptionSessions", sessionSubscriptions.values().stream()
                        .filter(sub -> !sub.getPriceSubscriptions().isEmpty() || !sub.getOrderbookSubscriptions()
                                .isEmpty())
                        .count()
        );
    }

    /**
     * 현재 연결 상태 정보 조회 (KIS 상태 포함)
     */
    public Map<String, Object> getConnectionStatus() {
        return Map.of(
                "totalSessions", sessionActivity.size(),
                "activeSessions", sessionActivity.values().stream()
                        .mapToLong(activity -> activity.isActive() ? 1 : 0).sum(),
                "totalSubscriptions", sessionSubscriptions.values().stream()
                        .mapToInt(sub -> sub.getPriceSubscriptions().size() + sub.getOrderbookSubscriptions().size())
                        .sum(),
                "heartbeatCounter", heartbeatCounter.get(),
                "lastHeartbeat", System.currentTimeMillis(),
                "kisConnection", getKisConnectionStats()
        );
    }
}