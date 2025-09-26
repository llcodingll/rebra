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

            kisApiComponent.startPriceSubscription(
                    account,
                    stockCode,
                    data -> {
                        // H0STCNT0Data 원본 데이터를 직접 전달 (변환 없이)
                        if (data != null) {
                            webSocketHelper.broadcastPriceData(userId, stockCode, data, sessionId);

                            // 세션 활동 업데이트 - 이벤트 발행 (순환 의존성 방지)
                            eventPublisher.publishEvent(new SessionActivityUpdateEvent(this, sessionId, "PRICE_DATA_RECEIVED"));
                        } else {
                        }
                    }
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 호가 구독 시작
     */
    public void startOrderbookSubscription(Account account, String stockCode, String sessionId) {
        Long userId = account.getUser().getId();
        try {

            kisApiComponent.startOrderbookSubscription(
                    account,
                    stockCode,
                    data -> {
                        // H0STASP0Data 원본 데이터를 직접 전달 (변환 없이)
                        if (data != null) {
                            webSocketHelper.broadcastOrderbookData(userId, stockCode, data, sessionId);

                            // 세션 활동 업데이트 - 이벤트 발행 (순환 의존성 방지)
                            eventPublisher.publishEvent(new SessionActivityUpdateEvent(this, sessionId, "ORDERBOOK_DATA_RECEIVED"));
                        } else {
                        }
                    }
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 구독 해제
     */
    public void stopPriceSubscription(Long userId, String stockCode, String sessionId) {
        kisApiComponent.stopSubscription(userId, stockCode, "price");
    }

    public void stopOrderbookSubscription(Long userId, String stockCode, String sessionId) {
        kisApiComponent.stopSubscription(userId, stockCode, "orderbook");
    }
}
