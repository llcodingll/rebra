package com.rebra.dto.portfoliodata;

import com.rebra.entity.PortfolioStock;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class RegisteredStockInfo {

    private String stockCode;
    private String stockName;
    private Integer purchasePrice;        // 매입평균가격 (KIS API)
    private Long quantity;                   // 보유수량 (KIS API)
    private Integer currentPrice;         // 현재가 (KIS API)
    private Double targetWeight;         // 목표 비중 (PortfolioStock)
    private Double thresholdPercentage;  // 임계치 (PortfolioStock)
    private String status;                   // 상태 (PortfolioStock)

    public static RegisteredStockInfo from(InquireBalanceResult.Output1 balance,
                                          Double targetWeight,
                                          Double thresholdPercentage,
                                          String status) {
        return RegisteredStockInfo.builder()
            .stockCode(balance.getPdno())
            .stockName(balance.getPrdtName())
            .purchasePrice(Integer.parseInt(balance.getPchsAvgPric()))
            .quantity(Long.parseLong(balance.getHldgQty()))
            .currentPrice(Integer.parseInt(balance.getPrpr()))
            .targetWeight(targetWeight)
            .thresholdPercentage(thresholdPercentage)
            .status(status)
            .build();
    }
}