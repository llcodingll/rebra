package com.rebra.dto.response;

import com.rebra.util.PortfolioCalculationUtil;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 보유 종목 상세 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "보유 종목 상세 정보")
public class StockHoldingResponse {

    @Schema(description = "종목코드", example = "005930")
    private String stockCode;

    @Schema(description = "종목명", example = "삼성전자")
    private String stockName;

    @Schema(description = "평균매입가", example = "65000")
    private BigDecimal averagePurchasePrice;

    @Schema(description = "현재가", example = "70000")
    private BigDecimal currentPrice;

    @Schema(description = "평가손익금액", example = "500000")
    private BigDecimal evaluationProfitLoss;

    @Schema(description = "수익률 (%)", example = "7.69")
    private BigDecimal returnRate;

    @Schema(description = "전일대비증감", example = "1000")
    private BigDecimal priceChange;

    @Schema(description = "등락률 (%)", example = "1.45")
    private BigDecimal changeRate;

    @Schema(description = "매입금액", example = "6500000")
    private BigDecimal purchaseAmount;

    @Schema(description = "평가금액", example = "7000000")
    private BigDecimal evaluationAmount;

    @Schema(description = "보유수량", example = "100")
    private Integer holdingQuantity;

    @Schema(description = "주문가능수량", example = "100")
    private Integer orderableQuantity;

    /**
     * KIS API 응답을 StockHoldingResponse로 변환
     *
     * @param holding KIS API 잔고 조회 결과
     * @return StockHoldingResponse 객체
     */
    public static StockHoldingResponse from(InquireBalanceResult.Output1 holding) {
        // 수익률 계산: (현재가 / 평균매입가 - 1) * 100
        BigDecimal returnRate = PortfolioCalculationUtil.calculateReturnRate(holding.getPrpr(), holding.getPchsAvgPric());

        return StockHoldingResponse.builder()
                .stockCode(holding.getPdno())
                .stockName(holding.getPrdtName())
                .averagePurchasePrice(new BigDecimal(holding.getPchsAvgPric()))
                .currentPrice(new BigDecimal(holding.getPrpr()))
                .evaluationProfitLoss(new BigDecimal(holding.getEvluPflsAmt()))
                .returnRate(returnRate)
                .priceChange(new BigDecimal(holding.getBfdyCprsIcdc()))
                .changeRate(new BigDecimal(holding.getFlttRt()))
                .purchaseAmount(new BigDecimal(holding.getPchsAmt()))
                .evaluationAmount(new BigDecimal(holding.getEvluAmt()))
                .holdingQuantity(Integer.parseInt(holding.getHldgQty()))
                .orderableQuantity(Integer.parseInt(holding.getOrdPsblQty()))
                .build();
    }
}