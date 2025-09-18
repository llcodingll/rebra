package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.entity.Account;
import com.rebra.util.WebSocketHelper;
import com.youhogeon.finance.kis_api.api.realtime.H0STASP0Data;
import com.youhogeon.finance.kis_api.api.realtime.H0STCNT0Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisRealtimeService {

    private final KisApiComponent kisApiComponent;
    private final WebSocketHelper webSocketHelper;

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
                    data -> webSocketHelper.broadcastPriceData(userId, stockCode, data)
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
                    data -> webSocketHelper.broadcastOrderbookData(userId, stockCode, data)
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
