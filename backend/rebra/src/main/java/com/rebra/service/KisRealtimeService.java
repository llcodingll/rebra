package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.realtime.OptimizedOrderbookData;
import com.rebra.dto.realtime.OptimizedPriceData;
import com.rebra.entity.Account;
import com.rebra.util.RealtimeDataTransformer;
import com.rebra.util.WebSocketHelper;
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
                    data -> {
                        log.info("📡 KIS 체결가 데이터 수신 - userId={}, stockCode={}", userId, stockCode);
                        OptimizedPriceData optimizedData = RealtimeDataTransformer.transformPriceData(data, stockCode);
                        if (optimizedData != null) {
                            log.info("📊 체결가 데이터 변환 성공, 전송 시작 - userId={}, stockCode={}, price={}",
                                    userId, stockCode, optimizedData.getStckPrpr());
                            webSocketHelper.broadcastPriceData(userId, stockCode, optimizedData);
                            log.info("📤 체결가 데이터 전송 완료 - userId={}, stockCode={}", userId, stockCode);
                        } else {
                            log.warn("❌ 체결가 데이터 변환 실패 - userId={}, stockCode={}", userId, stockCode);
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
                        OptimizedOrderbookData optimizedData = RealtimeDataTransformer.transformOrderbookData(data, stockCode);
                        if (optimizedData != null) {
                            log.info("📊 호가 데이터 변환 성공, 전송 시작 - userId={}, stockCode={}, ask1={}, bid1={}",
                                    userId, stockCode, optimizedData.getAskp1(), optimizedData.getBidp1());
                            webSocketHelper.broadcastOrderbookData(userId, stockCode, optimizedData);
                            log.info("📤 호가 데이터 전송 완료 - userId={}, stockCode={}", userId, stockCode);
                        } else {
                            log.warn("❌ 호가 데이터 변환 실패 - userId={}, stockCode={}", userId, stockCode);
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
