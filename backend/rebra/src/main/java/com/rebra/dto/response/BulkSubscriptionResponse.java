package com.rebra.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 일괄 구독 요청 응답 DTO
 * 클라이언트에게 구독 요청 처리 결과를 상세히 전달
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkSubscriptionResponse {

    /**
     * 전체 요청 성공 여부
     * 모든 구독이 성공한 경우에만 true
     */
    private boolean success;

    /**
     * 전체 요청 메시지
     */
    private String message;

    /**
     * 처리 완료 시간
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 개별 구독 결과 리스트
     */
    private List<SubscriptionResult> results;

    /**
     * 요약 정보
     */
    private SubscriptionSummary summary;

    /**
     * 세션 ID (재연결 시 참조용)
     */
    private String sessionId;

    /**
     * 성공한 구독 결과만 반환
     */
    public List<SubscriptionResult> getSuccessfulResults() {
        return results != null ?
            results.stream()
                .filter(SubscriptionResult::isSuccess)
                .toList() : List.of();
    }

    /**
     * 실패한 구독 결과만 반환
     */
    public List<SubscriptionResult> getFailedResults() {
        return results != null ?
            results.stream()
                .filter(result -> !result.isSuccess())
                .toList() : List.of();
    }

    /**
     * 종목별 구독 결과 맵 반환
     */
    public Map<String, List<SubscriptionResult>> getResultsByStockCode() {
        return results != null ?
            results.stream()
                .collect(Collectors.groupingBy(SubscriptionResult::getStockCode)) :
            Map.of();
    }

    /**
     * 데이터 타입별 구독 결과 맵 반환
     */
    public Map<String, List<SubscriptionResult>> getResultsByDataType() {
        return results != null ?
            results.stream()
                .collect(Collectors.groupingBy(SubscriptionResult::getDataType)) :
            Map.of();
    }

    /**
     * 총 성공 수 반환
     */
    public int getTotalSuccessful() {
        return summary != null ? summary.getTotalSuccessful() : 0;
    }

    /**
     * 총 실패 수 반환
     */
    public int getTotalFailed() {
        return summary != null ? summary.getTotalFailed() : 0;
    }

    /**
     * 총 요청 수 반환
     */
    public int getTotalRequested() {
        return summary != null ? summary.getTotalRequested() : 0;
    }

    /**
     * 요약 정보 생성
     */
    public static SubscriptionSummary createSummary(List<SubscriptionResult> results) {
        if (results == null || results.isEmpty()) {
            return SubscriptionSummary.builder()
                .totalRequested(0)
                .totalSuccessful(0)
                .totalFailed(0)
                .build();
        }

        int totalRequested = results.size();
        int totalSuccessful = (int) results.stream().filter(SubscriptionResult::isSuccess).count();
        int totalFailed = totalRequested - totalSuccessful;

        Map<String, Integer> successByDataType = results.stream()
            .filter(SubscriptionResult::isSuccess)
            .collect(Collectors.groupingBy(
                SubscriptionResult::getDataType,
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));

        Map<String, Integer> failureByDataType = results.stream()
            .filter(result -> !result.isSuccess())
            .collect(Collectors.groupingBy(
                SubscriptionResult::getDataType,
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));

        return SubscriptionSummary.builder()
            .totalRequested(totalRequested)
            .totalSuccessful(totalSuccessful)
            .totalFailed(totalFailed)
            .successByDataType(successByDataType)
            .failureByDataType(failureByDataType)
            .build();
    }

    /**
     * 성공 응답 생성 헬퍼
     */
    public static BulkSubscriptionResponse success(List<SubscriptionResult> results, String sessionId) {
        SubscriptionSummary summary = createSummary(results);
        boolean allSuccess = summary.getTotalFailed() == 0;

        return BulkSubscriptionResponse.builder()
            .success(allSuccess)
            .message(allSuccess ?
                String.format("모든 구독이 성공했습니다. (총 %d개)", summary.getTotalSuccessful()) :
                String.format("일부 구독이 실패했습니다. (성공: %d개, 실패: %d개)",
                    summary.getTotalSuccessful(), summary.getTotalFailed()))
            .results(results)
            .summary(summary)
            .sessionId(sessionId)
            .build();
    }

    /**
     * 실패 응답 생성 헬퍼
     */
    public static BulkSubscriptionResponse failure(String message, String sessionId) {
        return BulkSubscriptionResponse.builder()
            .success(false)
            .message(message)
            .results(List.of())
            .summary(SubscriptionSummary.builder()
                .totalRequested(0)
                .totalSuccessful(0)
                .totalFailed(0)
                .build())
            .sessionId(sessionId)
            .build();
    }
}

/**
 * 구독 요약 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SubscriptionSummary {

    /**
     * 총 요청된 구독 수
     */
    private int totalRequested;

    /**
     * 성공한 구독 수
     */
    private int totalSuccessful;

    /**
     * 실패한 구독 수
     */
    private int totalFailed;

    /**
     * 데이터 타입별 성공 수
     */
    private Map<String, Integer> successByDataType;

    /**
     * 데이터 타입별 실패 수
     */
    private Map<String, Integer> failureByDataType;

    /**
     * 성공률 계산 (백분율)
     */
    public double getSuccessRate() {
        return totalRequested > 0 ?
            (double) totalSuccessful / totalRequested * 100.0 : 0.0;
    }

    /**
     * 실패율 계산 (백분율)
     */
    public double getFailureRate() {
        return 100.0 - getSuccessRate();
    }
}