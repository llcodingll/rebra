package com.rebra.dto.response;

import com.rebra.entity.PortfolioStock;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioStockResponse {

    private Long portfolioStockId;
    private Long portfolioId;
    private String stockCode;
    private String stockName;
    private Double targetWeight;
    private Double thresholdPercentage;
    private String status;
    private LocalDateTime createdAt;

    @Builder
    public PortfolioStockResponse(Long portfolioStockId, Long portfolioId,
                                  String stockCode, String stockName, Double targetWeight,
                                  Double thresholdPercentage, String status, LocalDateTime createdAt) {
        this.portfolioStockId = portfolioStockId;
        this.portfolioId = portfolioId;
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.targetWeight = targetWeight;
        this.thresholdPercentage = thresholdPercentage;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static PortfolioStockResponse from(PortfolioStock portfolioStock, String stockName) {
        return PortfolioStockResponse.builder()
                .portfolioStockId(portfolioStock.getId())
                .portfolioId(portfolioStock.getPortfolio().getId())
                .stockCode(portfolioStock.getStockCode())
                .stockName(stockName)
                .targetWeight(portfolioStock.getTargetWeight())
                .thresholdPercentage(portfolioStock.getThresholdPercentage())
                .status(portfolioStock.getStatus())
                .createdAt(portfolioStock.getCreatedAt())
                .build();
    }
}