package com.rebra.dto.response;

import com.rebra.dto.portfoliodata.PortfolioSummary;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PortfolioListResponse {

    private Integer totalCount;
    private List<PortfolioSummary> portfolios;

    public static PortfolioListResponse of(List<PortfolioSummary> portfolios) {
        return PortfolioListResponse.builder()
            .portfolios(portfolios)
            .totalCount(portfolios.size())
            .build();
    }
}