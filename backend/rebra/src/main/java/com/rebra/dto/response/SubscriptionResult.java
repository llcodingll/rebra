package com.rebra.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 개별 구독 결과 DTO
 * 하나의 종목 × 데이터타입 조합에 대한 구독 처리 결과
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResult {

    /**
     * 종목코드
     */
    private String stockCode;

    /**
     * 데이터 타입 ("price", "orderbook" 등)
     */
    private String dataType;

    /**
     * 구독 성공 여부
     */
    private boolean success;

    /**
     * 결과 메시지 (성공/실패 사유)
     */
    private String message;

    /**
     * 에러 코드 (실패 시)
     */
    private String errorCode;

    /**
     * 처리 시간
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 추가 정보 (디버깅용)
     */
    private String additionalInfo;

    /**
     * 성공 결과 생성 헬퍼
     */
    public static SubscriptionResult success(String stockCode, String dataType) {
        return SubscriptionResult.builder()
            .stockCode(stockCode)
            .dataType(dataType)
            .success(true)
            .message("구독이 성공했습니다")
            .build();
    }

    /**
     * 성공 결과 생성 헬퍼 (메시지 포함)
     */
    public static SubscriptionResult success(String stockCode, String dataType, String message) {
        return SubscriptionResult.builder()
            .stockCode(stockCode)
            .dataType(dataType)
            .success(true)
            .message(message)
            .build();
    }

    /**
     * 실패 결과 생성 헬퍼
     */
    public static SubscriptionResult failure(String stockCode, String dataType, String errorMessage) {
        return SubscriptionResult.builder()
            .stockCode(stockCode)
            .dataType(dataType)
            .success(false)
            .message(errorMessage)
            .build();
    }

    /**
     * 실패 결과 생성 헬퍼 (에러코드 포함)
     */
    public static SubscriptionResult failure(String stockCode, String dataType,
                                           String errorMessage, String errorCode) {
        return SubscriptionResult.builder()
            .stockCode(stockCode)
            .dataType(dataType)
            .success(false)
            .message(errorMessage)
            .errorCode(errorCode)
            .build();
    }

    /**
     * 실패 결과 생성 헬퍼 (예외 포함)
     */
    public static SubscriptionResult failure(String stockCode, String dataType, Exception exception) {
        return SubscriptionResult.builder()
            .stockCode(stockCode)
            .dataType(dataType)
            .success(false)
            .message(exception.getMessage() != null ? exception.getMessage() : "알 수 없는 오류")
            .errorCode(exception.getClass().getSimpleName())
            .additionalInfo(exception.toString())
            .build();
    }

    /**
     * 구독 식별자 반환 (종목코드:데이터타입)
     */
    public String getSubscriptionKey() {
        return stockCode + ":" + dataType;
    }

    /**
     * 표시용 메시지 반환
     */
    public String getDisplayMessage() {
        if (success) {
            return String.format("[%s-%s] %s", stockCode, dataType, message);
        } else {
            return String.format("[%s-%s] 실패: %s%s",
                stockCode, dataType, message,
                errorCode != null ? " (" + errorCode + ")" : "");
        }
    }

    /**
     * 에러 여부 확인
     */
    public boolean isError() {
        return !success;
    }

    /**
     * 특정 에러 코드 여부 확인
     */
    public boolean hasErrorCode(String code) {
        return errorCode != null && errorCode.equals(code);
    }

    /**
     * 재시도 가능한 에러인지 확인
     */
    public boolean isRetryableError() {
        if (success || errorCode == null) {
            return false;
        }

        // 재시도 가능한 에러 코드들
        return errorCode.contains("Timeout") ||
               errorCode.contains("Connection") ||
               errorCode.contains("Network") ||
               errorCode.equals("KisClientException");
    }

    /**
     * 클라이언트 에러인지 확인 (사용자 입력 오류 등)
     */
    public boolean isClientError() {
        if (success || errorCode == null) {
            return false;
        }

        return errorCode.contains("ValidationException") ||
               errorCode.contains("IllegalArgument") ||
               errorCode.contains("BadRequest");
    }
}