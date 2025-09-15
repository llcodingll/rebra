package com.rebra.util;

import com.rebra.dto.response.WebSocketResponse;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket 메시지 전송 및 관리를 위한 유틸리티 클래스
 * Backend → Frontend 통신을 위한 표준화된 메시지 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHelper {

    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket 채널 상수 - 모든 메시지를 개인 Queue로 전송
    private static final String STOCK_QUEUE_PREFIX = "/queue/stock/";
    private static final String USER_QUEUE = "/queue/user";

    // ==================== 주식 실시간 데이터 전송 ====================

    /**
     * 주식 실시간 체결가 데이터 브로드캐스트
     */
    public void broadcastPriceData(Long userId, String stockCode, Object priceData) {
        try {
            String queueDestination = STOCK_QUEUE_PREFIX + stockCode + "/price";
            WebSocketResponse response = WebSocketResponse.of(
                    WebSocketResponse.MessageType.PRICE_UPDATE,
                    priceData
            );

            messagingTemplate.convertAndSendToUser(userId.toString(), queueDestination, response);
            log.debug("체결가 데이터 개인 전송 - UserId: {}, StockCode: {}", userId, stockCode);

        } catch (Exception e) {
            log.error("체결가 데이터 개인 전송 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
        }
    }

    /**
     * 주식 실시간 호가 데이터 브로드캐스트
     */
    public void broadcastOrderbookData(Long userId, String stockCode, Object orderbookData) {
        try {
            String queueDestination = STOCK_QUEUE_PREFIX + stockCode + "/orderbook";
            WebSocketResponse response = WebSocketResponse.of(
                    WebSocketResponse.MessageType.ORDERBOOK_UPDATE,
                    orderbookData
            );

            messagingTemplate.convertAndSendToUser(userId.toString(), queueDestination, response);
            log.debug("호가 데이터 개인 전송 - UserId: {}, StockCode: {}", userId, stockCode);

        } catch (Exception e) {
            log.error("호가 데이터 개인 전송 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
        }
    }

    /**
     * 구독 시작 알림 전송
     */
    public void sendSubscriptionStarted(Long userId, String stockCode, String dataType) {
        try {
            String queueDestination = STOCK_QUEUE_PREFIX + stockCode + "/" + dataType;

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

            messagingTemplate.convertAndSendToUser(userId.toString(), queueDestination, response);
            log.info("구독 시작 알림 - UserId: {}, StockCode: {}, Type: {}", userId, stockCode, dataType);

        } catch (Exception e) {
            log.error("구독 시작 알림 실패 - UserId: {}, StockCode: {}, Type: {}", userId, stockCode, dataType, e);
        }
    }

    /**
     * 구독 중지 알림 전송
     */
    public void sendSubscriptionStopped(Long userId, String stockCode, String dataType) {
        try {
            String queueDestination = STOCK_QUEUE_PREFIX + stockCode + "/" + dataType;

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

            messagingTemplate.convertAndSendToUser(userId.toString(), queueDestination, response);
            log.info("구독 중지 알림 - UserId: {}, StockCode: {}, Type: {}", userId, stockCode, dataType);

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
            String queueDestination = STOCK_QUEUE_PREFIX + stockCode + "/" + dataType;

            StockErrorData errorResponse = StockErrorData.builder()
                    .error(error)
                    .message(message)
                    .stockCode(stockCode)
                    .dataType(dataType)
                    .timestamp(System.currentTimeMillis())
                    .build();

            WebSocketResponse payload = WebSocketResponse.of(WebSocketResponse.MessageType.ERROR, errorResponse);

            messagingTemplate.convertAndSendToUser(userId.toString(), queueDestination, payload);

            log.error("주식 채널 에러 전송 - UserId: {}, StockCode: {}, Type: {}, Error: {}",
                    userId, stockCode, dataType, error);
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
}