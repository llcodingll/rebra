package com.rebra.dto.response;

import com.rebra.entity.TradeRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TradeDetailResponse {

    private Long tradeId;
    private String stockCode;
    private String stockName;
    private String tradeType; // BUY, SELL
    private Integer executedShares;
    private Long price;
    private String reason;
    private LocalDateTime tradeDate;

    public static TradeDetailResponse from(TradeRecord tradeRecord) {
        return TradeDetailResponse.builder()
                .tradeId(tradeRecord.getId())
                .stockCode(tradeRecord.getStockCode())
                .stockName(tradeRecord.getStockName())
                .tradeType(tradeRecord.getTradeType())
                .executedShares(tradeRecord.getExecutedShares())
                .price(tradeRecord.getExecutedPrice())
                .reason(tradeRecord.getReason())
                .tradeDate(tradeRecord.getTradeDate())
                .build();
    }
}