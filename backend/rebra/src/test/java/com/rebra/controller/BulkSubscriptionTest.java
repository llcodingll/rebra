package com.rebra.controller;

import com.rebra.dto.request.BulkSubscriptionRequest;
import com.rebra.dto.request.StockSubscription;
import com.rebra.service.WebSocketReconnectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 일괄 구독 비즈니스 로직 테스트
 */
class BulkSubscriptionTest {

    @Test
    @DisplayName("BulkSubscriptionRequest 유효성 검증 - 성공")
    void testBulkSubscriptionRequestValidation() {
        // Given
        BulkSubscriptionRequest request = new BulkSubscriptionRequest();

        StockSubscription stock1 = new StockSubscription();
        stock1.setStockCode("005930");
        stock1.setDataTypes(Arrays.asList("price", "orderbook"));

        StockSubscription stock2 = new StockSubscription();
        stock2.setStockCode("000660");
        stock2.setDataTypes(Arrays.asList("price"));

        request.setStocks(Arrays.asList(stock1, stock2));

        // When & Then
        assertTrue(request.isValid());
        assertEquals(3, request.getTotalSubscriptionCount());
        assertEquals(2, request.getStockCodesForDataType("price").size());
        assertEquals(1, request.getStockCodesForDataType("orderbook").size());
    }

    @Test
    @DisplayName("SessionSubscriptions 통합 관리 테스트")
    void testSessionSubscriptionsManagement() {
        // Given
        WebSocketReconnectionService.SessionSubscriptions sessionSubs =
            new WebSocketReconnectionService.SessionSubscriptions(1L);

        // When - 구독 추가
        sessionSubs.addSubscription("005930", "price");
        sessionSubs.addSubscription("005930", "orderbook");
        sessionSubs.addSubscription("000660", "price");

        // Then - 구독 상태 확인
        assertTrue(sessionSubs.hasSubscription("005930", "price"));
        assertTrue(sessionSubs.hasSubscription("005930", "orderbook"));
        assertTrue(sessionSubs.hasSubscription("000660", "price"));
        assertFalse(sessionSubs.hasSubscription("000660", "orderbook"));

        assertEquals(3, sessionSubs.getTotalSubscriptionCount());
        assertEquals(2, sessionSubs.getSubscribedStockCodes().size());

        // When - 구독 해제
        sessionSubs.removeSubscription("005930", "price");

        // Then - 해제 후 상태 확인
        assertFalse(sessionSubs.hasSubscription("005930", "price"));
        assertTrue(sessionSubs.hasSubscription("005930", "orderbook"));
        assertEquals(2, sessionSubs.getTotalSubscriptionCount());
        assertEquals(2, sessionSubs.getSubscribedStockCodes().size());

        // When - 종목 전체 해제
        sessionSubs.removeAllSubscriptionsForStock("005930");

        // Then - 전체 해제 후 상태 확인
        assertFalse(sessionSubs.hasSubscription("005930", "orderbook"));
        assertEquals(1, sessionSubs.getTotalSubscriptionCount());
        assertEquals(1, sessionSubs.getSubscribedStockCodes().size());
    }

    @Test
    @DisplayName("StockSubscription 유효성 검증")
    void testStockSubscriptionValidation() {
        // Given - 유효한 구독
        StockSubscription validStock = new StockSubscription();
        validStock.setStockCode("005930");
        validStock.setDataTypes(Arrays.asList("price", "orderbook"));

        // When & Then
        assertTrue(validStock.isValid());
        assertEquals(2, validStock.getImplementedDataTypes().size());
        assertTrue(validStock.hasDataType("price"));
        assertFalse(validStock.hasDataType("invalid"));

        // Given - 잘못된 종목코드
        StockSubscription invalidStock = new StockSubscription();
        invalidStock.setStockCode("INVALID");
        invalidStock.setDataTypes(Arrays.asList("price"));

        // When & Then
        assertFalse(invalidStock.isValid());
    }

    @Test
    @DisplayName("구독 히스토리 추적 테스트")
    void testSubscriptionHistory() {
        // Given
        WebSocketReconnectionService.SubscriptionHistory history =
            new WebSocketReconnectionService.SubscriptionHistory();

        // When
        history.addSubscription("price");
        history.addSubscription("orderbook");

        // Then
        assertTrue(history.isActive("price"));
        assertTrue(history.isActive("orderbook"));
        assertEquals(2, history.getActiveCount());
        assertNotNull(history.getSubscriptionTime("price"));

        // When - 구독 해제
        history.removeSubscription("price");

        // Then
        assertFalse(history.isActive("price"));
        assertTrue(history.isActive("orderbook"));
        assertEquals(1, history.getActiveCount());
        assertNotNull(history.getUnsubscriptionTime("price"));
    }
}