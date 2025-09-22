package com.rebra.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 일괄 구독 해제 요청 DTO
 * 클라이언트에서 여러 종목의 구독을 한 번에 해제할 때 사용
 *
 * 사용 예시:
 * {
 *   "stocks": [
 *     {
 *       "stockCode": "005930",
 *       "dataTypes": ["price", "orderbook"]  // 특정 타입만 해제
 *     },
 *     {
 *       "stockCode": "000660",
 *       "dataTypes": ["all"]  // 모든 타입 해제
 *     }
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUnsubscriptionRequest {

    /**
     * 구독 해제할 종목들의 리스트
     */
    @NotEmpty(message = "구독 해제할 종목 리스트는 비어있을 수 없습니다")
    @Size(min = 1, max = 100, message = "구독 해제할 종목은 1개 이상 100개 이하여야 합니다")
    @Valid
    private List<StockUnsubscription> stocks;

    /**
     * 총 구독 해제 수 계산
     */
    public int getTotalUnsubscriptionCount() {
        return stocks != null ?
            stocks.stream()
                .mapToInt(stock -> {
                    if (stock.getDataTypes() != null && stock.getDataTypes().contains("all")) {
                        return 10; // "all"인 경우 최대 가능한 구독 수로 계산
                    }
                    return stock.getDataTypes() != null ? stock.getDataTypes().size() : 0;
                })
                .sum() : 0;
    }

    /**
     * 특정 데이터 타입을 해제하는 종목 리스트 반환
     */
    public List<String> getStockCodesForDataType(String dataType) {
        return stocks != null ?
            stocks.stream()
                .filter(stock -> stock.getDataTypes() != null &&
                    (stock.getDataTypes().contains(dataType) || stock.getDataTypes().contains("all")))
                .map(StockUnsubscription::getStockCode)
                .toList() : List.of();
    }

    /**
     * 모든 구독을 해제할 종목 리스트 반환
     */
    public List<String> getStockCodesForAllUnsubscription() {
        return stocks != null ?
            stocks.stream()
                .filter(stock -> stock.getDataTypes() != null && stock.getDataTypes().contains("all"))
                .map(StockUnsubscription::getStockCode)
                .toList() : List.of();
    }

    /**
     * 요청 검증
     */
    public boolean isValid() {
        if (stocks == null || stocks.isEmpty()) {
            return false;
        }

        // 각 종목별 유효성 검증
        return stocks.stream().allMatch(StockUnsubscription::isValid);
    }

    /**
     * 개별 종목 구독 해제 정보 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockUnsubscription {

    /**
     * 종목코드 (6자리 숫자)
     */
    @jakarta.validation.constraints.NotBlank(message = "종목코드는 필수입니다")
    @jakarta.validation.constraints.Pattern(regexp = "^[0-9]{6}$", message = "종목코드는 6자리 숫자여야 합니다")
    private String stockCode;

    /**
     * 해제할 데이터 타입들
     * 특별값 "all": 해당 종목의 모든 구독 해제
     */
    @NotEmpty(message = "해제할 데이터 타입은 최소 1개 이상이어야 합니다")
    private List<String> dataTypes;

    /**
     * 유효성 검증
     */
    public boolean isValid() {
        if (stockCode == null || !stockCode.matches("^[0-9]{6}$")) {
            return false;
        }

        if (dataTypes == null || dataTypes.isEmpty()) {
            return false;
        }

        // "all"이 포함된 경우 다른 타입과 함께 올 수 없음
        if (dataTypes.contains("all") && dataTypes.size() > 1) {
            return false;
        }

        // 지원되는 데이터 타입인지 확인 ("all" 포함)
        Set<String> allowedTypes = new java.util.HashSet<>(StockSubscription.getSupportedDataTypes());
        allowedTypes.add("all");

        if (!dataTypes.stream().allMatch(allowedTypes::contains)) {
            return false;
        }

        // 중복 데이터 타입 확인
        return dataTypes.size() == dataTypes.stream().distinct().count();
    }

    /**
     * 모든 구독 해제 여부
     */
    public boolean isUnsubscribeAll() {
        return dataTypes != null && dataTypes.contains("all");
    }

    /**
     * 특정 타입 해제 여부
     */
    public boolean hasDataType(String dataType) {
        return dataTypes != null && (dataTypes.contains(dataType) || dataTypes.contains("all"));
    }
}
}