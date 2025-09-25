package com.rebra.dto.portfoliodata;

import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class UnregisteredStockInfo {

    private String stockCode;
    private String stockName;
    private Integer purchasePrice;  // 매입평균가격 (KIS API)
    private Long quantity;             // 보유수량 (KIS API)
    private Integer currentPrice;   // 현재가 (KIS API)

    public static UnregisteredStockInfo from(InquireBalanceResult.Output1 balance) {
        return UnregisteredStockInfo.builder()
            .stockCode(balance.getPdno())
            .stockName(balance.getPrdtName())
            .purchasePrice(Integer.parseInt(balance.getPchsAvgPric()))
            .quantity(Long.parseLong(balance.getHldgQty()))
            .currentPrice(Integer.parseInt(balance.getPrpr()))
            .build();
    }
}