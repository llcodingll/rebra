package com.rebra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 보유 종목 목록 응답 DTO (페이지네이션용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "보유 종목 목록 응답")
public class StockHoldingListResponse {

    @Schema(description = "보유 종목 목록")
    private List<StockHoldingResponse> holdings;

    public static StockHoldingListResponse of(List<StockHoldingResponse> holdings) {
        return StockHoldingListResponse.builder()
                .holdings(holdings)
                .build();
    }
}