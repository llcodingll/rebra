package com.rebra.dto.portfoliodata;

import com.rebra.entity.PortfolioStock;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RegisteredStockInfo {

    private String stockCode;
    private String stockName;
    private BigDecimal purchasePrice;        // 매입평균가격 (KIS API)
    private Long quantity;                   // 보유수량 (KIS API)
    private BigDecimal currentPrice;         // 현재가 (KIS API)
    private BigDecimal targetWeight;         // 목표 비중 (PortfolioStock)
    private BigDecimal thresholdPercentage;  // 임계치 (PortfolioStock)
    private String status;                   // 상태 (PortfolioStock)

    public static RegisteredStockInfo from(InquireBalanceResult.Output1 balance, PortfolioStock portfolioStock) {
        return RegisteredStockInfo.builder()
            .stockCode(balance.getPdno())
            .stockName(balance.getPrdtName())
            .purchasePrice(new BigDecimal(balance.getPchsAvgPric()))
            .quantity(Long.parseLong(balance.getHldgQty()))
            .currentPrice(new BigDecimal(balance.getPrpr()))
            .targetWeight(portfolioStock.getTargetWeight())
            .thresholdPercentage(portfolioStock.getThresholdPercentage())
            .status(portfolioStock.getStatus())
            .build();
    }
}