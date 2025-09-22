package com.rebra.dto.response;

import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 특정 종목 보유 정보 응답 DTO (4개 필드만)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "특정 종목 보유 정보")
public class StockHoldingDetailResponse {

    @Schema(description = "평균매입가", example = "65000")
    private BigDecimal averagePurchasePrice;

    @Schema(description = "매입금액", example = "6500000")
    private BigDecimal purchaseAmount;

    @Schema(description = "보유수량", example = "100")
    private Integer holdingQuantity;

    @Schema(description = "주문가능수량", example = "100")
    private Integer orderableQuantity;

    /**
     * KIS API 응답을 StockHoldingDetailResponse로 변환
     *
     * @param holding KIS API 잔고 조회 결과
     * @return StockHoldingDetailResponse 객체
     */
    public static StockHoldingDetailResponse from(InquireBalanceResult.Output1 holding) {
        return StockHoldingDetailResponse.builder()
                .averagePurchasePrice(new BigDecimal(holding.getPchsAvgPric()))
                .purchaseAmount(new BigDecimal(holding.getPchsAmt()))
                .holdingQuantity(Integer.parseInt(holding.getHldgQty()))
                .orderableQuantity(Integer.parseInt(holding.getOrdPsblQty()))
                .build();
    }
}