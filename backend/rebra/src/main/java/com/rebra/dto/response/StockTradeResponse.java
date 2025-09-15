package com.rebra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "주식 매수/매도 응답")
public class StockTradeResponse {

    @Schema(description = "거래소 주문번호", example = "KRX20240101000001")
    private String exchangeOrderNumber;

    @Schema(description = "주문번호", example = "0000001")
    private String orderNumber;

    @Schema(description = "주문시간", example = "123000")
    private String orderTime;

    @Schema(description = "종목코드", example = "005930")
    private String stockCode;

    @Schema(description = "주문구분", example = "00")
    private String orderType;

    @Schema(description = "주문수량", example = "10")
    private Integer quantity;

    @Schema(description = "주문단가", example = "70000")
    private Long price;

    @Schema(description = "매매구분 (01: 매도, 02: 매수)", example = "02")
    private String tradeType;

    @Schema(description = "주문 성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "오류 메시지 (실패시)", example = "")
    private String errorMessage;

    public static StockTradeResponse success(String exchangeOrderNumber, String orderNumber,
                                           String orderTime, String stockCode, String orderType,
                                           Integer quantity, Long price, String tradeType) {
        return StockTradeResponse.builder()
                .exchangeOrderNumber(exchangeOrderNumber)
                .orderNumber(orderNumber)
                .orderTime(orderTime)
                .stockCode(stockCode)
                .orderType(orderType)
                .quantity(quantity)
                .price(price)
                .tradeType(tradeType)
                .success(true)
                .build();
    }

    public static StockTradeResponse error(String errorMessage) {
        return StockTradeResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}