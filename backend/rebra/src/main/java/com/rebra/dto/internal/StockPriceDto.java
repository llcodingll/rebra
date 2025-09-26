package com.rebra.dto.internal;

import com.rebra.entity.Stock;
import com.rebra.entity.StockPrice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceDto {
    private Long id;
    private Long stockId;
    private String ticker;
    private String name;
    private LocalDate date;
    private Integer openPrice;
    private Integer highPrice;
    private Integer lowPrice;
    private Integer closePrice;
    private Long volume;
    private Double changeRate;
    
    public static StockPriceDto from(StockPrice stockPrice) {
        return StockPriceDto.builder()
            .id(stockPrice.getId())
            .stockId(stockPrice.getStock() != null ? stockPrice.getStock().getId() : null)
            .ticker(stockPrice.getTicker())
            .name(stockPrice.getName())
            .date(stockPrice.getDate())
            .openPrice(stockPrice.getOpenPrice())
            .highPrice(stockPrice.getHighPrice())
            .lowPrice(stockPrice.getLowPrice())
            .closePrice(stockPrice.getClosePrice())
            .volume(stockPrice.getVolume())
            .changeRate(stockPrice.getChangeRate())
            .build();
    }
    
    public StockPrice toEntity(Stock stock) {
        return StockPrice.builder()
            .stock(stock)
            .ticker(ticker)
            .name(name)
            .date(date)
            .openPrice(openPrice)
            .highPrice(highPrice)
            .lowPrice(lowPrice)
            .closePrice(closePrice)
            .volume(volume)
            .changeRate(changeRate)
            .build();
    }
}