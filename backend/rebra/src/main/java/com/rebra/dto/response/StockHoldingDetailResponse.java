package com.rebra.dto.response;

import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import com.youhogeon.finance.kis_api.api.rest.trading.InquirePsblOrderResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 특정 종목 보유 정보 응답 DTO (5개 필드만)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "특정 종목 보유 정보")
public class StockHoldingDetailResponse {

    @Schema(description = "평균매입가", example = "65000")
    private Integer averagePurchasePrice;

    @Schema(description = "매입금액", example = "6500000")
    private Integer purchaseAmount;

    @Schema(description = "보유수량", example = "100")
    private Integer holdingQuantity;

    @Schema(description = "주문가능수량", example = "100")
    private Integer orderableQuantity;

    @Schema(description = "주문가능금액", example = "10000000")
    private BigDecimal ordPsblCash;

    /**
     * KIS API 응답을 StockHoldingDetailResponse로 변환 (매수가능조회 포함)
     *
     * @param holding KIS API 잔고 조회 결과 (Output1)
     * @param psblOrderResult KIS API 매수가능조회 결과
     * @return StockHoldingDetailResponse 객체
     */
    public static StockHoldingDetailResponse from(InquireBalanceResult.Output1 holding, InquirePsblOrderResult psblOrderResult) {
        String possibleOrderAmount = psblOrderResult.getOutput() != null
                ? psblOrderResult.getOutput().getOrdPsblCash()
                : "0";

        return StockHoldingDetailResponse.builder()
                .averagePurchasePrice(Integer.parseInt(holding.getPchsAvgPric()))
                .purchaseAmount(Integer.parseInt(holding.getPchsAmt()))
                .holdingQuantity(Integer.parseInt(holding.getHldgQty()))
                .orderableQuantity(Integer.parseInt(holding.getOrdPsblQty()))
                .ordPsblCash(new BigDecimal(possibleOrderAmount))
                .build();
    }

    /**
     * 매수가능조회 결과만으로 StockHoldingDetailResponse 생성 (미보유 종목용)
     *
     * @param psblOrderResult KIS API 매수가능조회 결과
     * @return StockHoldingDetailResponse 객체
     */
    public static StockHoldingDetailResponse fromPossibleOrderOnly(InquirePsblOrderResult psblOrderResult) {
        String possibleOrderAmount = psblOrderResult.getOutput() != null
                ? psblOrderResult.getOutput().getOrdPsblCash()
                : "0";

        return StockHoldingDetailResponse.builder()
                .averagePurchasePrice(BigDecimal.ZERO)
                .purchaseAmount(BigDecimal.ZERO)
                .holdingQuantity(0)
                .orderableQuantity(0)
                .ordPsblCash(new BigDecimal(possibleOrderAmount))
                .build();
    }
}