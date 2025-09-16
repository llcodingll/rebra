package com.rebra.dto.portfoliodata;

import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UnregisteredStockInfo {

    private String stockCode;
    private String stockName;
    private BigDecimal purchasePrice;  // 매입평균가격 (KIS API)
    private Long quantity;             // 보유수량 (KIS API)
    private BigDecimal currentPrice;   // 현재가 (KIS API)

    public static UnregisteredStockInfo from(InquireBalanceResult.Output1 balance) {
        return UnregisteredStockInfo.builder()
            .stockCode(balance.getPdno())
            .stockName(balance.getPrdtName())
            .purchasePrice(new BigDecimal(balance.getPchsAvgPric()))
            .quantity(Long.parseLong(balance.getHldgQty()))
            .currentPrice(new BigDecimal(balance.getPrpr()))
            .build();
    }
}