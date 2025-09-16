package com.rebra.controller;

import com.rebra.entity.Account;
import com.rebra.repository.AccountRepository;
import com.rebra.service.KisRealtimeService;
import com.rebra.service.WebSocketReconnectionService;
import com.rebra.util.WebSocketHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RealtimeController {

    private final KisRealtimeService kisRealtimeService;
    private final AccountRepository accountRepository;
    private final WebSocketReconnectionService reconnectionService;
    private final WebSocketHelper webSocketHelper;

    /**
     * 사용자별 실시간 체결가 구독 요청 처리
     */
    @MessageMapping("/subscribe/{userId}/{stockCode}/price")
    public void subscribePriceRequest(@DestinationVariable Long userId,
                                      @DestinationVariable String stockCode,
                                      SimpMessageHeaderAccessor headerAccessor) {
        subscribePriceData(userId, stockCode, headerAccessor);
    }

    /**
     * 사용자별 실시간 체결가 구독
     */
    @SubscribeMapping("/topic/stock/{userId}/{stockCode}/price")
    public void subscribePriceData(@DestinationVariable Long userId,
                                   @DestinationVariable String stockCode,
                                   SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        log.info("실시간 체결가 구독 시작 - UserId: {}, StockCode: {}, SessionId: {}",
                userId, stockCode, sessionId);

        try {
            // 세션 등록 및 활동 추적
            reconnectionService.registerSession(sessionId, userId);
            reconnectionService.updateSessionActivity(sessionId);

            // 사용자의 첫 번째 활성 계좌 정보 조회
            Account account = accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true)
                    .orElseThrow(() -> new IllegalArgumentException("활성화된 계좌를 찾을 수 없습니다."));

            // KIS WebSocket 구독 시작
            kisRealtimeService.startPriceSubscription(account, stockCode, sessionId);

            // 구독 정보 추적
            reconnectionService.addSubscription(sessionId, stockCode, "price");

            // 구독 성공 응답 1회 전송
            webSocketHelper.sendSubscriptionStarted(userId, stockCode, "price");

        } catch (Exception e) {
            log.error("실시간 체결가 구독 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
        }
    }

    /**
     * 사용자별 실시간 호가 구독 요청 처리
     */
    @MessageMapping("/subscribe/{userId}/{stockCode}/orderbook")
    public void subscribeOrderbookRequest(@DestinationVariable Long userId,
                                          @DestinationVariable String stockCode,
                                          SimpMessageHeaderAccessor headerAccessor) {
        subscribeOrderbookData(userId, stockCode, headerAccessor);
    }

    /**
     * 사용자별 실시간 호가 구독
     */
    @SubscribeMapping("/topic/stock/{userId}/{stockCode}/orderbook")
    public void subscribeOrderbookData(@DestinationVariable Long userId,
                                       @DestinationVariable String stockCode,
                                       SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        log.info("실시간 호가 구독 시작 - UserId: {}, StockCode: {}, SessionId: {}",
                userId, stockCode, sessionId);

        try {
            // 세션 활동 추적
            reconnectionService.updateSessionActivity(sessionId);

            // 사용자의 첫 번째 활성 계좌 정보 조회
            Account account = accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true)
                    .orElseThrow(() -> new IllegalArgumentException("활성화된 계좌를 찾을 수 없습니다."));

            // KIS WebSocket 구독 시작
            kisRealtimeService.startOrderbookSubscription(account, stockCode, sessionId);

            // 구독 정보 추적
            reconnectionService.addSubscription(sessionId, stockCode, "orderbook");

            // 구독 성공 응답 1회 전송
            webSocketHelper.sendSubscriptionStarted(userId, stockCode, "orderbook");

        } catch (Exception e) {
            log.error("실시간 호가 구독 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
        }
    }

    /**
     * 실시간 구독 해제 메시지 핸들러
     */
    @MessageMapping("/unsubscribe/{userId}/{stockCode}")
    public void unsubscribe(@DestinationVariable Long userId,
                            @DestinationVariable String stockCode,
                            @Payload String dataType,
                            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        log.info("실시간 구독 해제 - UserId: {}, StockCode: {}, Type: {}, SessionId: {}",
                userId, stockCode, dataType, sessionId);

        // 세션 활동 추적
        reconnectionService.updateSessionActivity(sessionId);

        // KIS WebSocket 구독 해제
        if ("price".equals(dataType)) {
            kisRealtimeService.stopPriceSubscription(userId, stockCode, sessionId);
            reconnectionService.removeSubscription(sessionId, stockCode, "price");
        } else if ("orderbook".equals(dataType)) {
            kisRealtimeService.stopOrderbookSubscription(userId, stockCode, sessionId);
            reconnectionService.removeSubscription(sessionId, stockCode, "orderbook");
        }
    }
}