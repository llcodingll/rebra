package com.rebra.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioStockUpdateRequest {

    @NotBlank(message = "주식 코드는 필수입니다")
    private String stockCode;

    @DecimalMin(value = "0.0", message = "목표 비중은 0 이상이어야 합니다")
    @DecimalMax(value = "100.0", message = "목표 비중은 100 이하여야 합니다")
    private Double targetWeight;

    @DecimalMin(value = "0.0", message = "임계값은 0 이상이어야 합니다")
    private Double thresholdPercentage;
}