package com.rebra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.dto.request.BulkSubscriptionRequest;
import com.rebra.dto.request.BulkUnsubscriptionRequest;
import com.rebra.dto.request.StockSubscription;
import com.rebra.dto.response.BulkSubscriptionResponse;
import com.rebra.service.WebSocketReconnectionService;
import com.rebra.util.WebSocketHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RealtimeController 테스트
 * 주요 테스트 영역:
 * 1. 개별 구독/해제 기능
 * 2. 일괄 구독/해제 기능
 * 3. 세션 관리 및 에러 처리
 * 4. 병렬 처리 성능
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class RealtimeControllerTest {

    @Autowired
    private RealtimeController realtimeController;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private WebSocketHelper webSocketHelper;

    @MockBean
    private WebSocketReconnectionService reconnectionService;

    private ObjectMapper objectMapper;
    private SimpMessageHeaderAccessor headerAccessor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionId("test-session-123");
        headerAccessor.setSessionAttributes(new java.util.HashMap<>());
        headerAccessor.getSessionAttributes().put("userId", 1L);
    }

    @Test
    @DisplayName("일괄 구독 요청 - 성공 케이스")
    void testBulkSubscriptionSuccess() {
        // Given
        BulkSubscriptionRequest request = createBulkSubscriptionRequest();

        when(reconnectionService.getSessionSubscriptions(anyString()))
            .thenReturn(new WebSocketReconnectionService.SessionSubscriptions(1L));

        doNothing().when(webSocketHelper).sendBulkSubscriptionResponse(
            anyString(), any(BulkSubscriptionResponse.class));

        // When
        assertDoesNotThrow(() -> {
            realtimeController.subscribeBulkRequest(request, headerAccessor);
        });

        // Then
        verify(webSocketHelper, times(1)).sendBulkSubscriptionResponse(
            eq("test-session-123"), any(BulkSubscriptionResponse.class));
    }

    @Test
    @DisplayName("일괄 구독 요청 - 잘못된 종목코드")
    void testBulkSubscriptionInvalidStockCode() {
        // Given
        BulkSubscriptionRequest request = new BulkSubscriptionRequest();
        StockSubscription invalidStock = new StockSubscription();
        invalidStock.setStockCode("INVALID"); // 잘못된 종목코드
        invalidStock.setDataTypes(Arrays.asList("price"));
        request.setStocks(Arrays.asList(invalidStock));

        when(reconnectionService.getSessionSubscriptions(anyString()))
            .thenReturn(new WebSocketReconnectionService.SessionSubscriptions(1L));

        doNothing().when(webSocketHelper).sendBulkSubscriptionResponse(
            anyString(), any(BulkSubscriptionResponse.class));

        // When & Then
        assertDoesNotThrow(() -> {
            realtimeController.subscribeBulkRequest(request, headerAccessor);
        });

        // 에러 응답이 전송되는지 확인
        verify(webSocketHelper, times(1)).sendBulkSubscriptionResponse(
            eq("test-session-123"), any(BulkSubscriptionResponse.class));
    }

    @Test
    @DisplayName("일괄 구독 요청 - 빈 리스트")
    void testBulkSubscriptionEmptyList() {
        // Given
        BulkSubscriptionRequest request = new BulkSubscriptionRequest();
        request.setStocks(Arrays.asList()); // 빈 리스트

        when(reconnectionService.getSessionSubscriptions(anyString()))
            .thenReturn(new WebSocketReconnectionService.SessionSubscriptions(1L));

        doNothing().when(webSocketHelper).sendBulkSubscriptionResponse(
            anyString(), any(BulkSubscriptionResponse.class));

        // When & Then
        assertDoesNotThrow(() -> {
            realtimeController.subscribeBulkRequest(request, headerAccessor);
        });

        verify(webSocketHelper, times(1)).sendBulkSubscriptionResponse(
            eq("test-session-123"), any(BulkSubscriptionResponse.class));
    }

    @Test
    @DisplayName("일괄 구독 해제 요청 - 성공 케이스")
    void testBulkUnsubscriptionSuccess() {
        // Given
        BulkUnsubscriptionRequest request = createBulkUnsubscriptionRequest();

        WebSocketReconnectionService.SessionSubscriptions sessionSubs =
            new WebSocketReconnectionService.SessionSubscriptions(1L);
        // 기존 구독 상태 설정
        sessionSubs.addSubscription("005930", "price");
        sessionSubs.addSubscription("000660", "orderbook");

        when(reconnectionService.getSessionSubscriptions(anyString()))
            .thenReturn(sessionSubs);

        doNothing().when(webSocketHelper).sendBulkUnsubscriptionResponse(
            anyString(), any());

        // When
        assertDoesNotThrow(() -> {
            realtimeController.unsubscribeBulkRequest(request, headerAccessor);
        });

        // Then
        verify(webSocketHelper, times(1)).sendBulkUnsubscriptionResponse(
            eq("test-session-123"), any());
    }

    @Test
    @DisplayName("세션 없음 에러 처리")
    void testSessionNotFound() {
        // Given
        BulkSubscriptionRequest request = createBulkSubscriptionRequest();

        when(reconnectionService.getSessionSubscriptions(anyString()))
            .thenReturn(null); // 세션이 없는 경우

        doNothing().when(webSocketHelper).sendError(anyString(), anyString());

        // When
        assertDoesNotThrow(() -> {
            realtimeController.subscribeBulkRequest(request, headerAccessor);
        });

        // Then
        verify(webSocketHelper, times(1)).sendError(
            eq("test-session-123"), contains("세션을 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("대량 구독 요청 처리 성능 테스트")
    void testBulkSubscriptionPerformance() {
        // Given - 50개 종목 구독 요청
        BulkSubscriptionRequest request = createLargeBulkSubscriptionRequest(50);

        when(reconnectionService.getSessionSubscriptions(anyString()))
            .thenReturn(new WebSocketReconnectionService.SessionSubscriptions(1L));

        doNothing().when(webSocketHelper).sendBulkSubscriptionResponse(
            anyString(), any(BulkSubscriptionResponse.class));

        // When
        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> {
            realtimeController.subscribeBulkRequest(request, headerAccessor);
        });
        long endTime = System.currentTimeMillis();

        // Then - 5초 이내 처리되어야 함
        long processingTime = endTime - startTime;
        assertTrue(processingTime < 5000,
            "대량 구독 처리 시간이 5초를 초과했습니다: " + processingTime + "ms");

        verify(webSocketHelper, times(1)).sendBulkSubscriptionResponse(
            eq("test-session-123"), any(BulkSubscriptionResponse.class));
    }

    @Test
    @DisplayName("구독 상태 추적 테스트")
    void testSubscriptionTracking() {
        // Given
        WebSocketReconnectionService.SessionSubscriptions sessionSubs =
            new WebSocketReconnectionService.SessionSubscriptions(1L);

        // When
        sessionSubs.addSubscription("005930", "price");
        sessionSubs.addSubscription("005930", "orderbook");
        sessionSubs.addSubscription("000660", "price");

        // Then
        assertTrue(sessionSubs.hasSubscription("005930", "price"));
        assertTrue(sessionSubs.hasSubscription("005930", "orderbook"));
        assertTrue(sessionSubs.hasSubscription("000660", "price"));
        assertFalse(sessionSubs.hasSubscription("000660", "orderbook"));

        assertEquals(3, sessionSubs.getTotalSubscriptionCount());
        assertEquals(2, sessionSubs.getSubscribedStockCodes().size());

        // 구독 해제 테스트
        sessionSubs.removeSubscription("005930", "price");
        assertFalse(sessionSubs.hasSubscription("005930", "price"));
        assertTrue(sessionSubs.hasSubscription("005930", "orderbook"));
        assertEquals(2, sessionSubs.getTotalSubscriptionCount());
    }

    // 헬퍼 메서드들
    private BulkSubscriptionRequest createBulkSubscriptionRequest() {
        BulkSubscriptionRequest request = new BulkSubscriptionRequest();

        StockSubscription stock1 = new StockSubscription();
        stock1.setStockCode("005930"); // 삼성전자
        stock1.setDataTypes(Arrays.asList("price", "orderbook"));

        StockSubscription stock2 = new StockSubscription();
        stock2.setStockCode("000660"); // SK하이닉스
        stock2.setDataTypes(Arrays.asList("price"));

        request.setStocks(Arrays.asList(stock1, stock2));
        return request;
    }

    private BulkUnsubscriptionRequest createBulkUnsubscriptionRequest() {
        BulkUnsubscriptionRequest request = new BulkUnsubscriptionRequest();

        BulkUnsubscriptionRequest.StockUnsubscription stock1 =
            new BulkUnsubscriptionRequest.StockUnsubscription();
        stock1.setStockCode("005930");
        stock1.setDataTypes(Arrays.asList("price"));

        BulkUnsubscriptionRequest.StockUnsubscription stock2 =
            new BulkUnsubscriptionRequest.StockUnsubscription();
        stock2.setStockCode("000660");
        stock2.setDataTypes(Arrays.asList("all"));

        request.setStocks(Arrays.asList(stock1, stock2));
        return request;
    }

    private BulkSubscriptionRequest createLargeBulkSubscriptionRequest(int stockCount) {
        BulkSubscriptionRequest request = new BulkSubscriptionRequest();
        List<StockSubscription> stocks = new java.util.ArrayList<>();

        for (int i = 0; i < stockCount; i++) {
            StockSubscription stock = new StockSubscription();
            stock.setStockCode(String.format("%06d", i + 1)); // 000001, 000002, ...
            stock.setDataTypes(Arrays.asList("price", "orderbook"));
            stocks.add(stock);
        }

        request.setStocks(stocks);
        return request;
    }
}