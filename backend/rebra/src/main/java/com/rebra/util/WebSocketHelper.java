package com.rebra.util;

import com.rebra.dto.realtime.OptimizedOrderbookData;
import com.rebra.dto.realtime.OptimizedPriceData;
import com.rebra.dto.response.BulkSubscriptionResponse;
import com.rebra.dto.response.SubscriptionResult;
import com.rebra.dto.response.WebSocketResponse;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * WebSocket 메시지 전송 및 관리를 위한 유틸리티 클래스
 * Backend → Frontend 통신을 위한 표준화된 메시지 처리
 */
@Component
@RequiredArgsConstructor
public class WebSocketHelper {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHelper.class);

    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket 채널 상수 - 자원 분리로 경로 단순화
    private static final String STOCK_PRICE_QUEUE_TEMPLATE = "/queue/stock/%s/price";
    private static final String STOCK_ORDERBOOK_QUEUE_TEMPLATE = "/queue/stock/%s/orderbook";
    private static final String USER_QUEUE = "/queue/user";
    private static final String BULK_SUBSCRIPTION_QUEUE = "/queue/bulk/subscription";
    private static final String BULK_UNSUBSCRIPTION_QUEUE = "/queue/bulk/unsubscription";

    // ==================== 주식 실시간 데이터 전송 ====================

    /**
     * 주식 실시간 체결가 데이터 브로드캐스트 (최적화된 데이터 사용)
     */
    public void broadcastPriceData(Long userId, String stockCode, OptimizedPriceData priceData) {
        try {
            if (priceData == null) {
                log.warn("체결가 데이터가 null입니다 - UserId: {}, StockCode: {}", userId, stockCode);
                return;
            }

            // 사용자별 개인 전송을 위한 queue 경로 (convertAndSendToUser가 /user 프리픽스 자동 추가)
            String queuePath = String.format(STOCK_PRICE_QUEUE_TEMPLATE, stockCode);
            log.info("📍 체결가 데이터 전송 경로 - Path: /user{}, UserId: {}", queuePath, userId);

            messagingTemplate.convertAndSendToUser(userId.toString(), queuePath, priceData);

            log.info("📤 체결가 데이터 개인 전송 - UserId: {}, StockCode: {}, Price: {}, Path: /user{}",
                     userId, stockCode, priceData.getStckPrpr(), queuePath);

        } catch (Exception e) {
            log.error("체결가 데이터 개인 전송 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
        }
    }

    /**
     * 주식 실시간 호가 데이터 브로드캐스트 (최적화된 데이터 사용)
     */
    public void broadcastOrderbookData(Long userId, String stockCode, OptimizedOrderbookData orderbookData) {
        try {
            if (orderbookData == null) {
                log.warn("호가 데이터가 null입니다 - UserId: {}, StockCode: {}", userId, stockCode);
                return;
            }

            // 사용자별 개인 전송을 위한 queue 경로 (convertAndSendToUser가 /user 프리픽스 자동 추가)
            String queuePath = String.format(STOCK_ORDERBOOK_QUEUE_TEMPLATE, stockCode);
            log.info("📍 호가 데이터 전송 경로 - Path: /user{}, UserId: {}", queuePath, userId);

            messagingTemplate.convertAndSendToUser(userId.toString(), queuePath, orderbookData);

            log.info("📤 호가 데이터 개인 전송 - UserId: {}, StockCode: {}, Ask1: {}, Bid1: {}, Path: /user{}",
                     userId, stockCode, orderbookData.getAskp1(), orderbookData.getBidp1(), queuePath);

        } catch (Exception e) {
            log.error("호가 데이터 개인 전송 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
        }
    }

    /**
     * 구독 시작 알림 전송
     */
    public void sendSubscriptionStarted(Long userId, String stockCode, String dataType) {
        try {
            String queuePath = "price".equals(dataType) ?
                String.format(STOCK_PRICE_QUEUE_TEMPLATE, stockCode) :
                String.format(STOCK_ORDERBOOK_QUEUE_TEMPLATE, stockCode);

            SubscriptionStatusData statusData = SubscriptionStatusData.builder()
                    .stockCode(stockCode)
                    .dataType(dataType)
                    .status("started")
                    .message("실시간 " + getDataTypeKorean(dataType) + " 구독이 시작되었습니다.")
                    .timestamp(System.currentTimeMillis())
                    .build();

            WebSocketResponse response = WebSocketResponse.of(
                    WebSocketResponse.MessageType.SUBSCRIPTION_STARTED,
                    statusData
            );

            messagingTemplate.convertAndSendToUser(userId.toString(), queuePath, response);
            log.info("구독 시작 알림 - UserId: {}, StockCode: {}, Type: {}, Path: /user{}", userId, stockCode, dataType, queuePath);

        } catch (Exception e) {
            log.error("구독 시작 알림 실패 - UserId: {}, StockCode: {}, Type: {}", userId, stockCode, dataType, e);
        }
    }

    /**
     * 구독 중지 알림 전송
     */
    public void sendSubscriptionStopped(Long userId, String stockCode, String dataType) {
        try {
            String queuePath = "price".equals(dataType) ?
                String.format(STOCK_PRICE_QUEUE_TEMPLATE, stockCode) :
                String.format(STOCK_ORDERBOOK_QUEUE_TEMPLATE, stockCode);

            SubscriptionStatusData statusData = SubscriptionStatusData.builder()
                    .stockCode(stockCode)
                    .dataType(dataType)
                    .status("stopped")
                    .message("실시간 " + getDataTypeKorean(dataType) + " 구독이 중지되었습니다.")
                    .timestamp(System.currentTimeMillis())
                    .build();

            WebSocketResponse response = WebSocketResponse.of(
                    WebSocketResponse.MessageType.SUBSCRIPTION_STOPPED,
                    statusData
            );

            messagingTemplate.convertAndSendToUser(userId.toString(), queuePath, response);
            log.info("구독 중지 알림 - UserId: {}, StockCode: {}, Type: {}, Path: /user{}", userId, stockCode, dataType, queuePath);

        } catch (Exception e) {
            log.error("구독 중지 알림 실패 - UserId: {}, StockCode: {}, Type: {}", userId, stockCode, dataType, e);
        }
    }

    // ==================== 개인 메시지 전송 ====================

    /**
     * 개인 사용자에게 메시지 전송
     */
    public void sendPersonalMessage(Long userId, String messageType, Object responseData) {
        try {
            log.debug("개인 메시지 전송 - UserId: {}, Type: {}", userId, messageType);

            WebSocketResponse payload = WebSocketResponse.of(messageType, responseData);

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    USER_QUEUE,
                    payload
            );
        } catch (Exception e) {
            log.error("개인 메시지 전송 실패 - UserId: {}, Type: {}", userId, messageType, e);
        }
    }

    // ==================== 에러 처리 ====================

    /**
     * 개인 사용자에게 에러 메시지 전송
     */
    public void sendErrorToUser(Long userId, String error, String message) {
        if (userId != null) {
            ErrorResponseData errorResponse = ErrorResponseData.builder()
                    .error(error)
                    .message(message)
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendPersonalMessage(userId, WebSocketResponse.MessageType.ERROR, errorResponse);
        }
    }

    /**
     * ExceptionCode를 활용한 에러 전송
     */
    public void sendErrorToUser(Long userId, ExceptionCode exceptionCode, String detailMessage) {
        sendErrorToUser(userId, exceptionCode.getMessage(), detailMessage);
    }

    public void sendErrorToUser(Long userId, ExceptionCode exceptionCode) {
        sendErrorToUser(userId, exceptionCode.getMessage(), exceptionCode.getMessage());
    }

    /**
     * 특정 주식 채널에 에러 메시지 전송
     */
    public void sendStockError(Long userId, String stockCode, String dataType, String error, String message) {
        try {
            String queuePath = "price".equals(dataType) ?
                String.format(STOCK_PRICE_QUEUE_TEMPLATE, stockCode) :
                String.format(STOCK_ORDERBOOK_QUEUE_TEMPLATE, stockCode);

            StockErrorData errorResponse = StockErrorData.builder()
                    .error(error)
                    .message(message)
                    .stockCode(stockCode)
                    .dataType(dataType)
                    .timestamp(System.currentTimeMillis())
                    .build();

            WebSocketResponse payload = WebSocketResponse.of(WebSocketResponse.MessageType.ERROR, errorResponse);

            if (userId != null) {
                messagingTemplate.convertAndSendToUser(userId.toString(), queuePath, payload);
                log.error("주식 채널 에러 전송 - UserId: {}, StockCode: {}, Type: {}, Error: {}, Path: /user{}",
                        userId, stockCode, dataType, error, queuePath);
            } else {
                log.warn("주식 채널 에러 전송 실패 - UserId가 null입니다. StockCode: {}, Type: {}, Error: {}",
                        stockCode, dataType, error);
            }
        } catch (Exception e) {
            log.error("주식 채널 에러 전송 실패 - UserId: {}, StockCode: {}, Type: {}",
                    userId, stockCode, dataType, e);
        }
    }

    /**
     * ExceptionCode로 주식 채널 에러 전송
     */
    public void sendStockError(Long userId, String stockCode, String dataType, ExceptionCode exceptionCode) {
        sendStockError(userId, stockCode, dataType, exceptionCode.getMessage(), exceptionCode.getMessage());
    }

    // ==================== 인증 관련 ====================

    /**
     * WebSocket 세션에서 인증된 사용자 ID 추출
     */
    public Long getAuthenticatedUserId(SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) {
            log.warn("WebSocket 인증 실패 - 사용자 ID가 세션에 없음");
        }
        return userId;
    }

    /**
     * 인증된 사용자 ID 필수 추출 (인증 실패 시 예외 발생)
     */
    public Long requireAuthenticatedUserId(SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getAuthenticatedUserId(headerAccessor);
        if (userId == null) {
            throw new CustomRuntimeException(ExceptionCode.UNAUTHORIZED_ACCESS);
        }
        return userId;
    }

    // ==================== 헬퍼 메서드 ====================

    /**
     * 데이터 타입 한글명 변환
     */
    private String getDataTypeKorean(String dataType) {
        return switch (dataType) {
            case "price" -> "체결가";
            case "orderbook" -> "호가";
            default -> dataType;
        };
    }

    /**
     * 표준 WebSocket 응답 생성
     */
    public WebSocketResponse createResponse(String type, Object data) {
        return WebSocketResponse.of(type, data);
    }

    // ==================== 내부 데이터 클래스 ====================

    /**
     * 구독 상태 데이터
     */
    @Builder
    @Getter
    public static class SubscriptionStatusData {
        private String stockCode;
        private String dataType;
        private String status;
        private String message;
        private Long timestamp;
    }

    /**
     * 일반 에러 응답 데이터
     */
    @Builder
    @Getter
    public static class ErrorResponseData {
        private String error;
        private String message;
        private Long timestamp;
    }

    /**
     * 체결가 업데이트 데이터
     */
    @Builder
    @Getter
    public static class PriceUpdateData {
        private String stockCode;
        private Object priceData;
        private Long timestamp;
    }

    /**
     * 호가 업데이트 데이터
     */
    @Builder
    @Getter
    public static class OrderbookUpdateData {
        private String stockCode;
        private Object orderbookData;
        private Long timestamp;
    }

    /**
     * 주식 채널 에러 응답 데이터
     */
    @Builder
    @Getter
    public static class StockErrorData {
        private String error;
        private String message;
        private String stockCode;
        private String dataType;
        private Long timestamp;
    }

    // ==================== 일괄 구독 관련 메서드 ====================

    /**
     * 일괄 구독 응답 전송
     */
    public void sendBulkSubscriptionResponse(Long userId, BulkSubscriptionResponse response) {
        try {
            if (userId == null) {
                log.warn("일괄 구독 응답 전송 실패 - UserId가 null입니다");
                return;
            }

            log.info("📤 일괄 구독 응답 전송 - UserId: {}, 성공: {}, 실패: {}, 총 구독: {}",
                userId, response.getTotalSuccessful(),
                response.getTotalFailed(),
                response.getTotalRequested());

            WebSocketResponse payload = WebSocketResponse.of(
                WebSocketResponse.MessageType.BULK_SUBSCRIPTION_RESPONSE,
                response
            );

            messagingTemplate.convertAndSendToUser(userId.toString(), BULK_SUBSCRIPTION_QUEUE, payload);

            // 개별 구독 성공/실패 로깅
            if (log.isDebugEnabled()) {
                response.getResults().forEach(result -> {
                    if (result.isSuccess()) {
                        log.debug("✅ 구독 성공 - UserId: {}, {}:{}", userId, result.getStockCode(), result.getDataType());
                    } else {
                        log.debug("❌ 구독 실패 - UserId: {}, {}:{}, 사유: {}",
                            userId, result.getStockCode(), result.getDataType(), result.getMessage());
                    }
                });
            }

        } catch (Exception e) {
            log.error("일괄 구독 응답 전송 실패 - UserId: {}", userId, e);
        }
    }

    /**
     * 일괄 구독 해제 응답 전송
     */
    public void sendBulkUnsubscriptionResponse(Long userId, List<SubscriptionResult> results,
                                             int successCount, int failureCount) {
        try {
            if (userId == null) {
                log.warn("일괄 구독 해제 응답 전송 실패 - UserId가 null입니다");
                return;
            }

            log.info("📤 일괄 구독 해제 응답 전송 - UserId: {}, 성공: {}, 실패: {}, 총 해제: {}",
                userId, successCount, failureCount, results.size());

            BulkUnsubscriptionResponseData responseData = BulkUnsubscriptionResponseData.builder()
                .success(failureCount == 0)
                .message(failureCount == 0 ?
                    String.format("모든 구독 해제가 성공했습니다. (총 %d개)", successCount) :
                    String.format("일부 구독 해제가 실패했습니다. (성공: %d개, 실패: %d개)", successCount, failureCount))
                .results(results)
                .summary(BulkUnsubscriptionSummary.builder()
                    .totalRequested(results.size())
                    .totalSuccessful(successCount)
                    .totalFailed(failureCount)
                    .successRate(results.size() > 0 ? (double) successCount / results.size() * 100.0 : 0.0)
                    .build())
                .timestamp(System.currentTimeMillis())
                .build();

            WebSocketResponse payload = WebSocketResponse.of(
                WebSocketResponse.MessageType.BULK_UNSUBSCRIPTION_RESPONSE,
                responseData
            );

            messagingTemplate.convertAndSendToUser(userId.toString(), BULK_UNSUBSCRIPTION_QUEUE, payload);

            // 개별 구독 해제 성공/실패 로깅
            if (log.isDebugEnabled()) {
                results.forEach(result -> {
                    if (result.isSuccess()) {
                        log.debug("✅ 구독 해제 성공 - UserId: {}, {}:{}", userId, result.getStockCode(), result.getDataType());
                    } else {
                        log.debug("❌ 구독 해제 실패 - UserId: {}, {}:{}, 사유: {}",
                            userId, result.getStockCode(), result.getDataType(), result.getMessage());
                    }
                });
            }

        } catch (Exception e) {
            log.error("일괄 구독 해제 응답 전송 실패 - UserId: {}", userId, e);
        }
    }

    /**
     * 일괄 구독 해제 에러 전송
     */
    public void sendBulkUnsubscriptionError(Long userId, String errorMessage) {
        try {
            if (userId == null) {
                log.warn("일괄 구독 해제 에러 전송 실패 - UserId가 null입니다");
                return;
            }

            log.error("📤 일괄 구독 해제 에러 전송 - UserId: {}, Error: {}", userId, errorMessage);

            BulkUnsubscriptionResponseData errorResponse = BulkUnsubscriptionResponseData.builder()
                .success(false)
                .message(errorMessage)
                .results(List.of())
                .summary(BulkUnsubscriptionSummary.builder()
                    .totalRequested(0)
                    .totalSuccessful(0)
                    .totalFailed(0)
                    .successRate(0.0)
                    .build())
                .timestamp(System.currentTimeMillis())
                .build();

            WebSocketResponse payload = WebSocketResponse.of(
                WebSocketResponse.MessageType.BULK_UNSUBSCRIPTION_ERROR,
                errorResponse
            );

            messagingTemplate.convertAndSendToUser(userId.toString(), BULK_UNSUBSCRIPTION_QUEUE, payload);

        } catch (Exception e) {
            log.error("일괄 구독 해제 에러 전송 실패 - UserId: {}", userId, e);
        }
    }

    /**
     * 일괄 구독 진행 상황 알림 전송 (선택적)
     */
    public void sendBulkSubscriptionProgress(Long userId, String message, int completed, int total) {
        try {
            if (userId == null) {
                return;
            }

            BulkSubscriptionProgressData progressData = BulkSubscriptionProgressData.builder()
                .message(message)
                .completed(completed)
                .total(total)
                .percentage(total > 0 ? (double) completed / total * 100.0 : 0.0)
                .timestamp(System.currentTimeMillis())
                .build();

            WebSocketResponse payload = WebSocketResponse.of(
                WebSocketResponse.MessageType.BULK_SUBSCRIPTION_PROGRESS,
                progressData
            );

            messagingTemplate.convertAndSendToUser(userId.toString(), BULK_SUBSCRIPTION_QUEUE, payload);

            log.debug("📤 일괄 구독 진행 상황 전송 - UserId: {}, 진행률: {}% ({}/{})",
                userId, progressData.getPercentage(), completed, total);

        } catch (Exception e) {
            log.error("일괄 구독 진행 상황 전송 실패 - UserId: {}", userId, e);
        }
    }

    // ==================== 일괄 구독 관련 데이터 클래스 ====================

    /**
     * 일괄 구독 해제 응답 데이터
     */
    @Builder
    @Getter
    public static class BulkUnsubscriptionResponseData {
        private boolean success;
        private String message;
        private List<SubscriptionResult> results;
        private BulkUnsubscriptionSummary summary;
        private Long timestamp;
    }

    /**
     * 일괄 구독 해제 요약 정보
     */
    @Builder
    @Getter
    public static class BulkUnsubscriptionSummary {
        private int totalRequested;
        private int totalSuccessful;
        private int totalFailed;
        private double successRate;
    }

    /**
     * 일괄 구독 진행 상황 데이터
     */
    @Builder
    @Getter
    public static class BulkSubscriptionProgressData {
        private String message;
        private int completed;
        private int total;
        private double percentage;
        private Long timestamp;
    }
}