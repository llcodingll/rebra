package com.rebra.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "주식 매수/매도 요청")
public class StockTradeRequest {


    @NotNull(message = "주문구분은 필수입니다")
    @Schema(description = "주문구분 (00: 지정가, 01: 시장가, 02: 조건부지정가 등)", example = "00")
    private String orderType;

    @NotNull(message = "주문수량은 필수입니다")
    @Positive(message = "주문수량은 양수여야 합니다")
    @Schema(description = "주문수량", example = "10")
    private Integer quantity;

    @Schema(description = "주문단가 (시장가의 경우 0 또는 null)", example = "70000")
    private Long price;

    @NotNull(message = "계좌ID는 필수입니다")
    @Schema(description = "사용할 계좌의 ID", example = "1")
    private Long accountId;
}