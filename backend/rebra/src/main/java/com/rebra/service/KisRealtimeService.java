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
    
    // Redis 캐싱 제거 - 프론트엔드에서 실시간 데이터 관리
    // 실시간 데이터는 WebSocket을 통해 직접 클라이언트로 전달


    /**
     * WebSocket을 통한 실시간 체결가 구독 시작
     * 프론트엔드에서 /topic/stock/{userId}/{stockCode}/price 채널로 데이터 수신
     */
    public void startPriceSubscription(Account account, String stockCode, String sessionId) {
        try {
            Long userId = account.getUser().getId();
            log.info("WebSocket 체결가 구독 시작 - UserId: {}, StockCode: {}, SessionId: {}", 
                    userId, stockCode, sessionId);
            
            // KIS API Component를 통해 실시간 체결가 구독 시작
            kisApiComponent.startPriceSubscription(
                userId, 
                account.getId(), 
                stockCode, 
                account.getAccountType(),
                data -> broadcastPriceData(userId, stockCode, data)
            );
            
        } catch (Exception e) {
            log.error("WebSocket 체결가 구독 실패 - UserId: {}, StockCode: {}", 
                    account.getUser().getId(), stockCode, e);
            throw new RuntimeException("실시간 체결가 구독에 실패했습니다.", e);
        }
    }

    /**
     * WebSocket을 통한 실시간 호가 구독 시작
     * 프론트엔드에서 /topic/stock/{userId}/{stockCode}/orderbook 채널로 데이터 수신
     */
    public void startOrderbookSubscription(Account account, String stockCode, String sessionId) {
        try {
            Long userId = account.getUser().getId();
            log.info("WebSocket 호가 구독 시작 - UserId: {}, StockCode: {}, SessionId: {}", 
                    userId, stockCode, sessionId);
            
            // KIS API Component를 통해 실시간 호가 구독 시작
            kisApiComponent.startOrderbookSubscription(
                userId, 
                account.getId(), 
                stockCode, 
                account.getAccountType(),
                data -> broadcastOrderbookData(userId, stockCode, data)
            );
            
        } catch (Exception e) {
            log.error("WebSocket 호가 구독 실패 - UserId: {}, StockCode: {}", 
                    account.getUser().getId(), stockCode, e);
            throw new RuntimeException("실시간 호가 구독에 실패했습니다.", e);
        }
    }

    /**
     * WebSocket 구독 해제
     */
    public void stopPriceSubscription(Long userId, String stockCode, String sessionId) {
        try {
            log.info("WebSocket 체결가 구독 해제 - UserId: {}, StockCode: {}, SessionId: {}", 
                    userId, stockCode, sessionId);
            
            // KIS API Component를 통해 구독 해제 (참조 카운팅 자동 처리)
            kisApiComponent.stopSubscription(userId, stockCode, "price");
            
        } catch (Exception e) {
            log.error("WebSocket 체결가 구독 해제 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
        }
    }
    
    public void stopOrderbookSubscription(Long userId, String stockCode, String sessionId) {
        try {
            log.info("WebSocket 호가 구독 해제 - UserId: {}, StockCode: {}, SessionId: {}", 
                    userId, stockCode, sessionId);
            
            // KIS API Component를 통해 구독 해제 (참조 카운팅 자동 처리)
            kisApiComponent.stopSubscription(userId, stockCode, "orderbook");
            
        } catch (Exception e) {
            log.error("WebSocket 호가 구독 해제 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
        }
    }
    
    /**
     * KIS WebSocket에서 수신한 체결가 데이터를 프론트엔드로 전달
     */
    private void broadcastPriceData(Long userId, String stockCode, H0STCNT0Data data) {
        // KIS에서 받은 데이터를 그대로 프론트엔드에 전달
        webSocketHelper.broadcastPriceData(userId, stockCode, data);
    }

    /**
     * KIS WebSocket에서 수신한 호가 데이터를 프론트엔드로 전달
     */
    private void broadcastOrderbookData(Long userId, String stockCode, H0STASP0Data data) {
        // KIS에서 받은 데이터를 그대로 프론트엔드에 전달
        webSocketHelper.broadcastOrderbookData(userId, stockCode, data);
    }

}