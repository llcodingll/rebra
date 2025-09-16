package com.rebra.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioStockDeleteRequest {

    @NotBlank(message = "주식 코드는 필수입니다")
    private String stockCode;
}