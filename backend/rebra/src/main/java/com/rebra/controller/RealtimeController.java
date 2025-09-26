package com.rebra.controller;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.request.BulkSubscriptionRequest;
import com.rebra.dto.request.BulkUnsubscriptionRequest;
import com.rebra.dto.request.StockSubscription;
import com.rebra.dto.response.BulkSubscriptionResponse;
import com.rebra.dto.response.SubscriptionResult;
import com.rebra.entity.Account;
import com.rebra.repository.AccountRepository;
import com.rebra.service.KisRealtimeService;
import com.rebra.service.WebSocketReconnectionService;
import com.rebra.util.WebSocketHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RealtimeController {

    private static final Logger log = LoggerFactory.getLogger(RealtimeController.class);

    private final KisRealtimeService kisRealtimeService;
    private final KisApiComponent kisApiComponent;
    private final AccountRepository accountRepository;
    private final WebSocketHelper webSocketHelper;
    private final WebSocketReconnectionService webSocketReconnectionService;

    // 병렬 처리를 위한 ExecutorService (최대 10개 스레드)
    private final ExecutorService bulkSubscriptionExecutor = Executors.newFixedThreadPool(10);

    // 개별 체결가 구독 메서드 제거됨 - bulk 구독 방식 사용

    // 개별 호가 구독 메서드 제거됨 - bulk 구독 방식 사용

    // 개별 구독 해제 메서드 제거됨 - bulk 구독 해제 방식 사용

    /**
     * 일괄 구독 요청 클라이언트: SEND("/app/subscribe/bulk", {stocks: [...]})
     * <p>
     * 사용 예시: { "stocks": [ {"stockCode": "005930", "dataTypes": ["price", "orderbook"]}, {"stockCode": "000660",
     * "dataTypes": ["price"]} ] }
     */
    @MessageMapping("/subscribe/bulk")
    public void subscribeBulkRequest(@Payload BulkSubscriptionRequest request,
                                     SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        // 세션에서 userId 추출
        Long userId = webSocketHelper.getAuthenticatedUserId(headerAccessor);
        if (userId == null) {
            log.warn("Websocket 세션에 userId 없음 - sessionId: {}", sessionId);
            BulkSubscriptionResponse errorResponse = BulkSubscriptionResponse.failure(
                    "인증 실패: 로그인이 필요합니다", sessionId);
            webSocketHelper.sendBulkSubscriptionResponse(userId, errorResponse);
            return;
        }


        // 입력 검증
        if (!validateBulkSubscriptionRequest(request, userId)) {
            return;
        }

        try {
            // 사용자 계좌 조회
            Account account = getUserAccount(userId);

            // 비동기 병렬 처리로 구독 요청 실행
            CompletableFuture.supplyAsync(() -> processBulkSubscription(request, account, sessionId, userId),
                            bulkSubscriptionExecutor)
                    .thenAccept(response -> {
                        // 구독 성공 알림 전송
                        webSocketHelper.sendBulkSubscriptionResponse(userId, response);

                        // 성공한 구독들을 재연결 서비스에 등록
                        registerSuccessfulSubscriptions(response, sessionId);

                    })
                    .exceptionally(throwable -> {
                        BulkSubscriptionResponse errorResponse = BulkSubscriptionResponse.failure(
                                "서버 오류: " + throwable.getMessage(), sessionId);
                        webSocketHelper.sendBulkSubscriptionResponse(userId, errorResponse);
                        return null;
                    });

        } catch (Exception e) {
            BulkSubscriptionResponse errorResponse = BulkSubscriptionResponse.failure(
                    "초기 처리 실패: " + e.getMessage(), sessionId);
            webSocketHelper.sendBulkSubscriptionResponse(userId, errorResponse);
        }
    }

    /**
     * 일괄 구독 해제 요청 클라이언트: SEND("/app/unsubscribe/bulk", {stocks: [...]})
     */
    @MessageMapping("/unsubscribe/bulk")
    public void unsubscribeBulkRequest(@Payload BulkUnsubscriptionRequest request,
                                       SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        // 세션에서 userId 추출
        Long userId = webSocketHelper.getAuthenticatedUserId(headerAccessor);
        if (userId == null) {
            log.warn("Websocket 세션에 userId 없음 - sessionId: {}", sessionId);
            webSocketHelper.sendBulkUnsubscriptionError(userId, "인증 실패: 로그인이 필요합니다");
            return;
        }


        try {
            List<SubscriptionResult> results = new ArrayList<>();

            // 각 종목별로 구독 해제 처리
            for (var stockUnsub : request.getStocks()) {
                String stockCode = stockUnsub.getStockCode();

                for (String dataType : stockUnsub.getDataTypes()) {
                    if ("all".equals(dataType)) {
                        // 모든 타입 해제
                        results.addAll(unsubscribeAllDataTypes(userId, stockCode, sessionId));
                    } else {
                        // 특정 타입 해제
                        SubscriptionResult result = unsubscribeSingleDataType(userId, stockCode, dataType, sessionId);
                        results.add(result);
                    }
                }
            }

            // 결과 전송
            int successCount = (int) results.stream().filter(SubscriptionResult::isSuccess).count();
            int failureCount = results.size() - successCount;


            webSocketHelper.sendBulkUnsubscriptionResponse(userId, results, successCount, failureCount);

        } catch (Exception e) {
            webSocketHelper.sendBulkUnsubscriptionError(userId, "서버 오류: " + e.getMessage());
        }
    }

    /**
     * 일괄 구독 요청 검증
     */
    private boolean validateBulkSubscriptionRequest(BulkSubscriptionRequest request, Long userId) {
        if (request == null || request.getStocks() == null || request.getStocks().isEmpty()) {
            BulkSubscriptionResponse errorResponse = BulkSubscriptionResponse.failure(
                    "요청이 비어있습니다", null);
            webSocketHelper.sendBulkSubscriptionResponse(userId, errorResponse);
            return false;
        }

        if (!request.isValid()) {
            BulkSubscriptionResponse errorResponse = BulkSubscriptionResponse.failure(
                    "잘못된 요청입니다", null);
            webSocketHelper.sendBulkSubscriptionResponse(userId, errorResponse);
            return false;
        }

        // 중복 구독 검증
        if (request.hasDuplicateSubscriptions()) {
            BulkSubscriptionResponse errorResponse = BulkSubscriptionResponse.failure(
                    "중복된 구독 요청이 있습니다", null);
            webSocketHelper.sendBulkSubscriptionResponse(userId, errorResponse);
            return false;
        }

        // 구독 수 제한 검증 (100개)
        if (request.getTotalSubscriptionCount() > 100) {
            BulkSubscriptionResponse errorResponse = BulkSubscriptionResponse.failure(
                    "구독 요청 수가 너무 많습니다 (최대 100개)", null);
            webSocketHelper.sendBulkSubscriptionResponse(userId, errorResponse);
            return false;
        }

        return true;
    }

    /**
     * 사용자 계좌 조회
     */
    private Account getUserAccount(Long userId) {
        var allAccounts = accountRepository.findByUserId(userId);

        var connectedAccounts = allAccounts.stream()
                .filter(Account::isConnected)
                .toList();

        return accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true)
                .orElseThrow(() -> new RuntimeException(
                        String.format("활성화된 계좌를 찾을 수 없습니다. userId: %d, 전체계좌: %d개, 연결된계좌: %d개",
                                userId, allAccounts.size(), connectedAccounts.size())));
    }

    /**
     * 일괄 구독 처리 (비동기)
     */
    private BulkSubscriptionResponse processBulkSubscription(BulkSubscriptionRequest request,
                                                             Account account, String sessionId, Long userId) {
        List<SubscriptionResult> results = new ArrayList<>();


        List<StockSubscription> stocksList = request.getStocks();

        // 각 종목별로 구독 처리                                           │
        for (int i = 0; i < stocksList.size(); i++) {
            StockSubscription stock = stocksList.get(i);
            String stockCode = stock.getStockCode();

            // 구현된 데이터 타입만 처리

            for (String dataType : stock.getImplementedDataTypes()) {
                try {
                    processIndividualSubscription(account, stockCode, dataType, sessionId);
                    results.add(SubscriptionResult.success(stockCode, dataType,
                            "구독이 성공했습니다"));


                } catch (Exception e) {
                    results.add(SubscriptionResult.failure(stockCode, dataType, e));
                }
            }

            // 구현되지 않은 데이터 타입에 대한 알림
            for (String dataType : stock.getUnimplementedDataTypes()) {
                results.add(SubscriptionResult.failure(stockCode, dataType,
                        "아직 구현되지 않은 데이터 타입입니다", "NOT_IMPLEMENTED"));
            }
        }

        return BulkSubscriptionResponse.success(results, sessionId);
    }

    /**
     * 개별 구독 처리
     */
    private void processIndividualSubscription(Account account, String stockCode,
                                               String dataType, String sessionId) {
        switch (dataType) {
            case "price":
                kisRealtimeService.startPriceSubscription(account, stockCode, sessionId);
                break;
            case "orderbook":
                kisRealtimeService.startOrderbookSubscription(account, stockCode, sessionId);
                break;
            default:
                throw new IllegalArgumentException("지원되지 않는 데이터 타입: " + dataType);
        }
    }

    /**
     * 성공한 구독들을 재연결 서비스에 등록 (중복 방지 포함)
     */
    private void registerSuccessfulSubscriptions(BulkSubscriptionResponse response, String sessionId) {
        for (SubscriptionResult result : response.getSuccessfulResults()) {
            // 새로고침 복구가 아닌 경우에만 등록 (중복 방지)
            if (!webSocketReconnectionService.isSubscriptionAlreadyRegistered(sessionId,
                    result.getStockCode(), result.getDataType())) {
                webSocketReconnectionService.addSubscription(sessionId,
                        result.getStockCode(), result.getDataType());
            } else {
            }
        }
    }

    /**
     * 모든 데이터 타입 구독 해제
     */
    private List<SubscriptionResult> unsubscribeAllDataTypes(Long userId, String stockCode, String sessionId) {
        List<SubscriptionResult> results = new ArrayList<>();

        // 현재 구현된 모든 데이터 타입 해제
        for (String dataType : StockSubscription.getAllImplementedDataTypes()) {
            SubscriptionResult result = unsubscribeSingleDataType(userId, stockCode, dataType, sessionId);
            results.add(result);
        }

        return results;
    }

    /**
     * 단일 데이터 타입 구독 해제
     */
    private SubscriptionResult unsubscribeSingleDataType(Long userId, String stockCode,
                                                         String dataType, String sessionId) {
        try {
            switch (dataType) {
                case "price":
                    kisRealtimeService.stopPriceSubscription(userId, stockCode, sessionId);
                    webSocketReconnectionService.removeSubscription(sessionId, stockCode, "price");
                    break;
                case "orderbook":
                    kisRealtimeService.stopOrderbookSubscription(userId, stockCode, sessionId);
                    webSocketReconnectionService.removeSubscription(sessionId, stockCode, "orderbook");
                    break;
                default:
                    return SubscriptionResult.failure(stockCode, dataType, "지원되지 않는 데이터 타입");
            }

            return SubscriptionResult.success(stockCode, dataType, "구독 해제가 성공했습니다");

        } catch (Exception e) {
            return SubscriptionResult.failure(stockCode, dataType, e);
        }
    }

    /**
     * 클라이언트 하트비트 응답 처리 클라이언트: SEND("/app/heartbeat/response", {sequence: 123})
     */
    @MessageMapping("/heartbeat/response")
    public void handleHeartbeatResponse(@Payload Map<String, Object> response,
                                        SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        try {
            Object sequenceObj = response.get("sequence");
            if (sequenceObj != null) {
                long sequence = Long.parseLong(sequenceObj.toString());
                webSocketReconnectionService.handleHeartbeatResponse(sessionId, sequence);

            } else {
            }
        } catch (Exception e) {
        }
    }
}
