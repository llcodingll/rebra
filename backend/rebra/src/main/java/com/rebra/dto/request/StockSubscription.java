package com.rebra.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 개별 종목 구독 정보 DTO
 * 하나의 종목에 대해 구독할 데이터 타입들을 명시
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSubscription {

    /**
     * 종목코드 (6자리 숫자)
     * 예: "005930" (삼성전자), "000660" (SK하이닉스)
     */
    @JsonProperty("stockCode")
    @NotBlank(message = "종목코드는 필수입니다")
    @Pattern(regexp = "^[0-9]{6}$", message = "종목코드는 6자리 숫자여야 합니다")
    private String stockCode;

    /**
     * 구독할 데이터 타입들
     * 허용되는 값: "price" (체결가), "orderbook" (호가)
     * 향후 확장 가능: "news", "disclosure", "execution" 등
     */
    @JsonProperty("dataTypes")
    @NotEmpty(message = "구독할 데이터 타입은 최소 1개 이상이어야 합니다")
    @Size(min = 1, max = 10, message = "데이터 타입은 1개 이상 10개 이하여야 합니다")
    private List<String> dataTypes;

    /**
     * 지원되는 데이터 타입들
     */
    private static final Set<String> SUPPORTED_DATA_TYPES = Set.of(
        "price",        // 체결가 (H0STCNT0)
        "orderbook"    // 호가 (H0STASP0)
    );

    /**
     * 현재 구현된 데이터 타입들
     */
    private static final Set<String> IMPLEMENTED_DATA_TYPES = Set.of(
        "price",
        "orderbook"
    );

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

        // 지원되는 데이터 타입인지 확인
        if (!dataTypes.stream().allMatch(SUPPORTED_DATA_TYPES::contains)) {
            return false;
        }

        // 중복 데이터 타입 확인
        return dataTypes.size() == dataTypes.stream().distinct().count();
    }

    /**
     * 구현되지 않은 데이터 타입 확인
     */
    public List<String> getUnimplementedDataTypes() {
        return dataTypes != null ?
            dataTypes.stream()
                .filter(dataType -> !IMPLEMENTED_DATA_TYPES.contains(dataType))
                .toList() : List.of();
    }

    /**
     * 구현된 데이터 타입만 반환
     */
    public List<String> getImplementedDataTypes() {
        return dataTypes != null ?
            dataTypes.stream()
                .filter(IMPLEMENTED_DATA_TYPES::contains)
                .toList() : List.of();
    }

    /**
     * 특정 데이터 타입 포함 여부 확인
     */
    public boolean hasDataType(String dataType) {
        return dataTypes != null && dataTypes.contains(dataType);
    }

    /**
     * 체결가 구독 여부
     */
    public boolean includesPrice() {
        return hasDataType("price");
    }

    /**
     * 호가 구독 여부
     */
    public boolean includesOrderbook() {
        return hasDataType("orderbook");
    }

    /**
     * 지원되는 데이터 타입 목록 반환
     */
    public static Set<String> getSupportedDataTypes() {
        return SUPPORTED_DATA_TYPES;
    }

    /**
     * 구현된 데이터 타입 목록 반환 (정적 메서드)
     */
    public static Set<String> getAllImplementedDataTypes() {
        return IMPLEMENTED_DATA_TYPES;
    }
}