package com.rebra.controller;

import com.rebra.component.KisApiComponent;
import com.rebra.entity.Account;
import com.rebra.repository.AccountRepository;
import com.rebra.service.KisRealtimeService;
import com.rebra.util.WebSocketHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RealtimeController {

    private final KisRealtimeService kisRealtimeService;
    private final KisApiComponent kisApiComponent;
    private final AccountRepository accountRepository;
    private final WebSocketHelper webSocketHelper;

    /**
     * 체결가 구독 요청
     * 클라이언트: SEND("/app/subscribe/{userId}/{stockCode}/price")
     */
    @MessageMapping("/subscribe/{userId}/{stockCode}/price")
    public void subscribePriceRequest(@DestinationVariable Long userId,
                                      @DestinationVariable String stockCode,
                                      SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("📡 체결가 구독 요청 - userId={}, stockCode={}, sessionId={}", userId, stockCode, sessionId);

        try {
            log.info("📊 계좌 조회 시작 - userId: {}", userId);

            // 먼저 해당 유저의 모든 계좌를 확인
            var allAccounts = accountRepository.findByUserId(userId);
            log.info("📋 사용자 {}의 전체 계좌 수: {}", userId, allAccounts.size());

            if (!allAccounts.isEmpty()) {
                log.info("📝 전체 계좌 목록:");
                for (var acc : allAccounts) {
                    log.info("  - 계좌ID: {}, 연결상태: {}, 생성일: {}, 브로커: {}",
                        acc.getId(), acc.isConnected(), acc.getCreatedAt(), acc.getBrokerName());
                }
            }

            // 연결된 계좌만 확인
            var connectedAccounts = allAccounts.stream()
                .filter(Account::isConnected)
                .toList();
            log.info("🔗 사용자 {}의 연결된 계좌 수: {}", userId, connectedAccounts.size());

            if (!connectedAccounts.isEmpty()) {
                log.info("✅ 연결된 계좌 목록:");
                for (var acc : connectedAccounts) {
                    log.info("  - 계좌ID: {}, 브로커: {}, 생성일: {}",
                        acc.getId(), acc.getBrokerName(), acc.getCreatedAt());
                }
            }

            // 사용자의 첫 번째 활성 계좌 조회
            Account account = accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true)
                    .orElseThrow(() -> new RuntimeException(
                        String.format("활성화된 계좌를 찾을 수 없습니다. userId: %d, 전체계좌: %d개, 연결된계좌: %d개",
                            userId, allAccounts.size(), connectedAccounts.size())
                    ));

            kisRealtimeService.startPriceSubscription(account, stockCode, sessionId);

            // 구독 성공 알림 전송
            webSocketHelper.sendSubscriptionStarted(userId, stockCode, "price");

        } catch (Exception e) {
            log.error("체결가 구독 처리 실패 - userId={}, stockCode={}", userId, stockCode, e);
            webSocketHelper.sendStockError(userId, stockCode, "price", "구독 실패", e.getMessage());
        }
    }

    /**
     * 호가 구독 요청
     * 클라이언트: SEND("/app/subscribe/{userId}/{stockCode}/orderbook")
     */
    @MessageMapping("/subscribe/{userId}/{stockCode}/orderbook")
    public void subscribeOrderbookRequest(@DestinationVariable Long userId,
                                          @DestinationVariable String stockCode,
                                          SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("📡 호가 구독 요청 - userId={}, stockCode={}, sessionId={}", userId, stockCode, sessionId);

        try {
            log.info("📊 계좌 조회 시작 - userId: {}", userId);

            // 먼저 해당 유저의 모든 계좌를 확인
            var allAccounts = accountRepository.findByUserId(userId);
            log.info("📋 사용자 {}의 전체 계좌 수: {}", userId, allAccounts.size());

            if (!allAccounts.isEmpty()) {
                log.info("📝 전체 계좌 목록:");
                for (var acc : allAccounts) {
                    log.info("  - 계좌ID: {}, 연결상태: {}, 생성일: {}, 브로커: {}",
                        acc.getId(), acc.isConnected(), acc.getCreatedAt(), acc.getBrokerName());
                }
            }

            // 연결된 계좌만 확인
            var connectedAccounts = allAccounts.stream()
                .filter(Account::isConnected)
                .toList();
            log.info("🔗 사용자 {}의 연결된 계좌 수: {}", userId, connectedAccounts.size());

            if (!connectedAccounts.isEmpty()) {
                log.info("✅ 연결된 계좌 목록:");
                for (var acc : connectedAccounts) {
                    log.info("  - 계좌ID: {}, 브로커: {}, 생성일: {}",
                        acc.getId(), acc.getBrokerName(), acc.getCreatedAt());
                }
            }

            Account account = accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true)
                    .orElseThrow(() -> new RuntimeException(
                        String.format("활성화된 계좌를 찾을 수 없습니다. userId: %d, 전체계좌: %d개, 연결된계좌: %d개",
                            userId, allAccounts.size(), connectedAccounts.size())
                    ));

            kisRealtimeService.startOrderbookSubscription(account, stockCode, sessionId);

            webSocketHelper.sendSubscriptionStarted(userId, stockCode, "orderbook");

        } catch (Exception e) {
            log.error("호가 구독 처리 실패 - userId={}, stockCode={}", userId, stockCode, e);
            webSocketHelper.sendStockError(userId, stockCode, "orderbook", "구독 실패", e.getMessage());
        }
    }

    /**
     * 구독 해제 요청
     * 클라이언트: SEND("/app/unsubscribe/{userId}/{stockCode}") with payload="price" or "orderbook"
     */
    @MessageMapping("/unsubscribe/{userId}/{stockCode}")
    public void unsubscribe(@DestinationVariable Long userId,
                            @DestinationVariable String stockCode,
                            String dataType,
                            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        log.info("🔌 구독 해제 요청 - userId={}, stockCode={}, type={}, sessionId={}", userId, stockCode, dataType, sessionId);

        if ("price".equals(dataType)) {
            kisRealtimeService.stopPriceSubscription(userId, stockCode, sessionId);
            webSocketHelper.sendSubscriptionStopped(userId, stockCode, "price");
        } else if ("orderbook".equals(dataType)) {
            kisRealtimeService.stopOrderbookSubscription(userId, stockCode, sessionId);
            webSocketHelper.sendSubscriptionStopped(userId, stockCode, "orderbook");
        }
    }
}
