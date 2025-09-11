package com.rebra.dto.response;

import com.rebra.entity.Stock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockSearchResponse {

    private Long id;
    private String stockCode;
    private String stockName;
    private String stockType;
    private Boolean isActive;

    public static StockSearchResponse from(Stock stock) {
        return new StockSearchResponse(
            stock.getId(),
            stock.getStockCode(),
            stock.getStockName(),
            stock.getStockType(),
            stock.getIsActive()
        );
    }
}