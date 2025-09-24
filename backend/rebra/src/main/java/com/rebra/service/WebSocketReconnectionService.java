package com.rebra.service;

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
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * WebSocket 연결 안정성 및 재연결 관리 서비스
 * - 연결 상태 모니터링
 * - 하트비트 메커니즘
 * - 자동 재연결 로직
 * - 데이터 무결성 검증
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
    
    // 설정값
    private static final long HEARTBEAT_INTERVAL = 30000; // 30초
    private static final long SESSION_TIMEOUT = 120000;   // 2분
    private static final long RECONNECTION_DELAY = 5000;  // 5초
    private static final int MAX_RECONNECTION_ATTEMPTS = 3;
    private static final long USER_SUBSCRIPTION_EXPIRATION = 3600000; // 1시간 (사용자별 구독 만료 시간)

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

        public Map<String, String> getPriceSubscriptions() { return priceSubscriptions; }
        public Map<String, String> getOrderbookSubscriptions() { return orderbookSubscriptions; }
        public Map<String, Set<String>> getStockSubscriptions() { return stockSubscriptions; }
        public Map<String, SubscriptionHistory> getSubscriptionHistory() { return subscriptionHistory; }
        public Long getUserId() { return userId; }
        public int getReconnectionAttempts() { return reconnectionAttempts; }
        public LocalDateTime getLastBulkSubscription() { return lastBulkSubscription; }

        public void incrementReconnectionAttempts() { this.reconnectionAttempts++; }
        public void resetReconnectionAttempts() { this.reconnectionAttempts = 0; }
        public void updateLastBulkSubscription() { this.lastBulkSubscription = LocalDateTime.now(); }

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

        public String getStockCode() { return stockCode; }
        public String getDataType() { return dataType; }
        public LocalDateTime getSubscribedAt() { return subscribedAt; }
        public LocalDateTime getLastActivity() { return lastActivity; }

        public void setLastActivity(LocalDateTime lastActivity) {
            this.lastActivity = lastActivity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
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
     * 새 세션 등록 (새로고침 복구 지원)
     */
    public void registerSession(String sessionId, Long userId) {
        sessionActivity.put(sessionId, new LastActivity());
        sessionSubscriptions.put(sessionId, new SessionSubscriptions(userId));

        // 사용자별 구독 정보 확인 (새로고침 복구)
        Set<UserSubscriptionInfo> userSubscriptions = userActiveSubscriptions.get(userId);
        if (userSubscriptions != null && !userSubscriptions.isEmpty()) {
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

            log.info("WebSocket 세션 제거 및 구독 정리 시작 - SessionId: {}, EventUserId: {}, StoredUserId: {}, 실제사용UserId: {}, 체결가: {}개, 호가: {}개",
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
     * KIS 실시간 구독 해제
     */
    private void unsubscribeFromKisRealtime(Long userId, SessionSubscriptions subscriptions, String sessionId) {
        // 모든 체결가 구독 해제
        subscriptions.getPriceSubscriptions().keySet().forEach(stockCode -> {
            try {
                log.info("🔌 세션 해제로 인한 체결가 구독 해제 - SessionId: {}, UserId: {}, StockCode: {}",
                        sessionId, userId, stockCode);
                kisRealtimeService.stopPriceSubscription(userId, stockCode, sessionId);
            } catch (Exception e) {
                log.error("❌ 세션 해제 시 체결가 구독 해제 실패 - SessionId: {}, StockCode: {}", sessionId, stockCode, e);
            }
        });

        // 모든 호가 구독 해제
        subscriptions.getOrderbookSubscriptions().keySet().forEach(stockCode -> {
            try {
                log.info("🔌 세션 해제로 인한 호가 구독 해제 - SessionId: {}, UserId: {}, StockCode: {}",
                        sessionId, userId, stockCode);
                kisRealtimeService.stopOrderbookSubscription(userId, stockCode, sessionId);
            } catch (Exception e) {
                log.error("❌ 세션 해제 시 호가 구독 해제 실패 - SessionId: {}, StockCode: {}", sessionId, stockCode, e);
            }
        });
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