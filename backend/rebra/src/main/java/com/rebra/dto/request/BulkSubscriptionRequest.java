package com.rebra.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 일괄 구독 요청 DTO
 * 클라이언트에서 여러 종목의 다양한 데이터 타입을 한 번에 구독 요청할 때 사용
 *
 * 사용 예시:
 * {
 *   "stocks": [
 *     {
 *       "stockCode": "005930",
 *       "dataTypes": ["price", "orderbook"]
 *     },
 *     {
 *       "stockCode": "000660",
 *       "dataTypes": ["price"]
 *     }
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkSubscriptionRequest {

    /**
     * 구독할 종목들의 리스트
     * 최소 1개, 최대 50개까지 허용 (성능 고려)
     */
    @JsonProperty("stocks")
    @NotEmpty(message = "구독할 종목 리스트는 비어있을 수 없습니다")
    @Size(min = 1, max = 50, message = "구독할 종목은 1개 이상 50개 이하여야 합니다")
    @Valid
    private List<StockSubscription> stocks;

    /**
     * 총 구독 수 계산 (모든 종목 × 데이터타입 조합)
     */
    public int getTotalSubscriptionCount() {
        return stocks != null ?
            stocks.stream()
                .mapToInt(stock -> stock.getDataTypes() != null ? stock.getDataTypes().size() : 0)
                .sum() : 0;
    }

    /**
     * 특정 데이터 타입을 구독하는 종목 리스트 반환
     */
    public List<String> getStockCodesForDataType(String dataType) {
        return stocks != null ?
            stocks.stream()
                .filter(stock -> stock.getDataTypes() != null && stock.getDataTypes().contains(dataType))
                .map(StockSubscription::getStockCode)
                .toList() : List.of();
    }

    /**
     * 요청 검증
     */
    public boolean isValid() {
        if (stocks == null || stocks.isEmpty()) {
            return false;
        }

        // 총 구독 수 제한 (100개)
        if (getTotalSubscriptionCount() > 100) {
            return false;
        }

        // 각 종목별 유효성 검증
        return stocks.stream().allMatch(StockSubscription::isValid);
    }

    /**
     * 중복 종목코드 × 데이터타입 조합 검증
     */
    public boolean hasDuplicateSubscriptions() {
        return stocks != null &&
            stocks.stream()
                .flatMap(stock -> stock.getDataTypes() != null ?
                    stock.getDataTypes().stream()
                        .map(dataType -> stock.getStockCode() + ":" + dataType) :
                    java.util.stream.Stream.empty())
                .distinct()
                .count() != getTotalSubscriptionCount();
    }
}