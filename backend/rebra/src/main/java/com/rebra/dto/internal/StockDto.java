package com.rebra.dto.internal;

import com.rebra.entity.Stock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockDto {
    private Long id;
    private String stockCode;
    private String stockName;
    private String stockType;
    private Boolean isActive;
    private LocalDate dataStartDate;
    private LocalDate dataEndDate;
    
    public static StockDto from(Stock stock) {
        return StockDto.builder()
            .id(stock.getId())
            .stockCode(stock.getStockCode())
            .stockName(stock.getStockName())
            .stockType(stock.getStockType())
            .isActive(stock.getIsActive())
            .dataStartDate(stock.getDataStartDate())
            .dataEndDate(stock.getDataEndDate())
            .build();
    }
    
    public boolean hasDataInRange(LocalDate start, LocalDate end) {
        return dataStartDate != null && dataEndDate != null &&
               !start.isBefore(dataStartDate) && !end.isAfter(dataEndDate);
    }
}