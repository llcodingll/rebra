package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.config.SessionActivityUpdateEvent;
import com.rebra.entity.Account;
import com.rebra.util.WebSocketHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisRealtimeService {

    private final KisApiComponent kisApiComponent;
    private final WebSocketHelper webSocketHelper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 체결가 구독 시작
     */
    public void startPriceSubscription(Account account, String stockCode, String sessionId) {
        Long userId = account.getUser().getId();
        try {
            log.info("▶️ 체결가 구독 시작 - userId={}, stockCode={}, sessionId={}", userId, stockCode, sessionId);

            kisApiComponent.startPriceSubscription(
                    account,
                    stockCode,
                    data -> {
                        log.info("📡 KIS 체결가 데이터 수신 - userId={}, stockCode={}", userId, stockCode);
                        // H0STCNT0Data 원본 데이터를 직접 전달 (변환 없이)
                        if (data != null) {
                            log.info("📊 체결가 원본 데이터 전송 시작 - userId={}, stockCode={}",
                                    userId, stockCode);
                            webSocketHelper.broadcastPriceData(userId, stockCode, data, sessionId);
                            log.info("📤 체결가 데이터 전송 완료 - userId={}, stockCode={}", userId, stockCode);

                            // 세션 활동 업데이트 - 이벤트 발행 (순환 의존성 방지)
                            eventPublisher.publishEvent(new SessionActivityUpdateEvent(this, sessionId, "PRICE_DATA_RECEIVED"));
                            log.debug("💚 체결가 데이터 수신 시 세션 활동 업데이트 이벤트 발행 - sessionId={}", sessionId);
                        } else {
                            log.warn("❌ 체결가 데이터가 null - userId={}, stockCode={}", userId, stockCode);
                        }
                    }
            );

        } catch (Exception e) {
            log.error("체결가 구독 실패 - userId={}, stockCode={}", userId, stockCode, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 호가 구독 시작
     */
    public void startOrderbookSubscription(Account account, String stockCode, String sessionId) {
        Long userId = account.getUser().getId();
        try {
            log.info("▶️ 호가 구독 시작 - userId={}, stockCode={}, sessionId={}", userId, stockCode, sessionId);

            kisApiComponent.startOrderbookSubscription(
                    account,
                    stockCode,
                    data -> {
                        log.info("📡 KIS 호가 데이터 수신 - userId={}, stockCode={}", userId, stockCode);
                        // H0STASP0Data 원본 데이터를 직접 전달 (변환 없이)
                        if (data != null) {
                            log.info("📊 호가 원본 데이터 전송 시작 - userId={}, stockCode={}",
                                    userId, stockCode);
                            webSocketHelper.broadcastOrderbookData(userId, stockCode, data, sessionId);
                            log.info("📤 호가 데이터 전송 완료 - userId={}, stockCode={}", userId, stockCode);

                            // 세션 활동 업데이트 - 이벤트 발행 (순환 의존성 방지)
                            eventPublisher.publishEvent(new SessionActivityUpdateEvent(this, sessionId, "ORDERBOOK_DATA_RECEIVED"));
                            log.debug("💚 호가 데이터 수신 시 세션 활동 업데이트 이벤트 발행 - sessionId={}", sessionId);
                        } else {
                            log.warn("❌ 호가 데이터가 null - userId={}, stockCode={}", userId, stockCode);
                        }
                    }
            );

        } catch (Exception e) {
            log.error("호가 구독 실패 - userId={}, stockCode={}", userId, stockCode, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 구독 해제
     */
    public void stopPriceSubscription(Long userId, String stockCode, String sessionId) {
        log.info("⏹ 체결가 구독 해제 - userId={}, stockCode={}, sessionId={}", userId, stockCode, sessionId);
        kisApiComponent.stopSubscription(userId, stockCode, "price");
    }

    public void stopOrderbookSubscription(Long userId, String stockCode, String sessionId) {
        log.info("⏹ 호가 구독 해제 - userId={}, stockCode={}, sessionId={}", userId, stockCode, sessionId);
        kisApiComponent.stopSubscription(userId, stockCode, "orderbook");
    }
}
