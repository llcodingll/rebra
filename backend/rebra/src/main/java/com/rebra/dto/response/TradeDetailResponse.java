package com.rebra.dto.response;

import com.rebra.entity.TradeRecord;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TradeDetailResponse {

    private Long tradeId;
    private String stockCode;
    private String stockName;
    private String tradeType; // BUY, SELL
    private Integer executedShares;
    private BigDecimal price;
    private BigDecimal fee;
    private BigDecimal profitAmount;
    private BigDecimal profitRate;
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
                .fee(tradeRecord.getFee())
                .profitAmount(tradeRecord.getProfitAmount())
                .profitRate(tradeRecord.getProfitRate())
                .reason(tradeRecord.getReason())
                .tradeDate(tradeRecord.getTradeDate())
                .build();
    }
}