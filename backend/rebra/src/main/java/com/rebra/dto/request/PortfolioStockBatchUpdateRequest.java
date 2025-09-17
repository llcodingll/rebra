package com.rebra.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioStockBatchUpdateRequest {

    @NotEmpty(message = "업데이트할 주식 목록은 필수입니다")
    @Valid
    private List<PortfolioStockUpdateRequest> stocks;
}