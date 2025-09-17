package com.rebra.dto.response;

import com.rebra.entity.StockPrice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "주식 과거 데이터 응답")
public class StockHistoricalDataResponse {

    @Schema(description = "종목 코드 (6자리)", example = "005930")
    private String ticker;

    @Schema(description = "종목명", example = "삼성전자")
    private String name;

    @Schema(description = "조회 날짜", example = "2023-01-01")
    private LocalDate date;

    @Schema(description = "종가", example = "65000.00")
    private BigDecimal closePrice;

    @Schema(description = "시가", example = "64500.00")
    private BigDecimal openPrice;

    @Schema(description = "고가", example = "65500.00")
    private BigDecimal highPrice;

    @Schema(description = "저가", example = "64000.00")
    private BigDecimal lowPrice;

    @Schema(description = "거래량", example = "1000000")
    private Long volume;

    @Schema(description = "등락률 (%)", example = "1.25")
    private BigDecimal changeRate;

    public static StockHistoricalDataResponse from(StockPrice stockPrice) {
        return StockHistoricalDataResponse.builder()
                .ticker(stockPrice.getTicker())
                .name(stockPrice.getName())
                .date(stockPrice.getDate())
                .closePrice(stockPrice.getClosePrice())
                .openPrice(stockPrice.getOpenPrice())
                .highPrice(stockPrice.getHighPrice())
                .lowPrice(stockPrice.getLowPrice())
                .volume(stockPrice.getVolume())
                .changeRate(stockPrice.getChangeRate())
                .build();
    }
}