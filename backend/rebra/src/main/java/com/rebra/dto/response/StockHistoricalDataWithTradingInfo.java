package com.rebra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "주식 과거 데이터 응답 (거래일 정보 포함)")
public class StockHistoricalDataWithTradingInfo {

    @Schema(description = "거래일 여부", example = "true")
    private boolean tradingDay;

    @Schema(description = "주식 과거 데이터 목록")
    private List<StockHistoricalDataResponse> data;

    @Schema(description = "안내 메시지", example = "2024-01-01은(는) 거래일이 아닙니다")
    private String message;
}