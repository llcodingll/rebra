package com.rebra.dto.portfoliodata;

import com.rebra.entity.AccountType;
import com.rebra.entity.Portfolio;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 포트폴리오 요약 정보 DTO
 */
@Data
@Builder
public class PortfolioSummary {
    private Long portfolioId;              // 포트폴리오 ID
    private String name;                   // 포트폴리오 제목
    private String description;            // 설명
    private Integer registeredStockCount;  // 등록 주식 개수
    private BigDecimal totalReturnRate;    // 등록 주식 총 수익률 (%)
    private Boolean isAccountConnected;    // 계좌 연결 상태 (재연동 필요 여부)
    private LocalDateTime createdAt;       // 생성일
    private AccountType accountType;       // 계좌타입 (MOCK/REAL)

    /**
     * Portfolio 엔티티와 수익률 데이터로부터 PortfolioSummary 생성
     *
     * @param portfolio 포트폴리오 엔티티
     * @param returnData 수익률 계산 결과
     * @param stockCount 등록 주식 개수
     * @return PortfolioSummary 인스턴스
     */
    public static PortfolioSummary of(Portfolio portfolio, PortfolioReturnData returnData, int stockCount) {
        return PortfolioSummary.builder()
            .portfolioId(portfolio.getId())
            .name(portfolio.getName())
            .description(portfolio.getDescription())
            .registeredStockCount(stockCount)
            .totalReturnRate(returnData.getReturnRate())
            .isAccountConnected(portfolio.getAccount().isConnected())
            .createdAt(portfolio.getCreatedAt())
            .accountType(portfolio.getAccount().getAccountType())
            .build();
    }

    /**
     * 에러 발생 시 기본값으로 PortfolioSummary 생성
     *
     * @param portfolio 포트폴리오 엔티티
     * @return 기본값으로 설정된 PortfolioSummary 인스턴스
     */
    public static PortfolioSummary ofDefault(Portfolio portfolio) {
        return PortfolioSummary.builder()
            .portfolioId(portfolio.getId())
            .name(portfolio.getName())
            .description(portfolio.getDescription())
            .registeredStockCount(0)
            .totalReturnRate(BigDecimal.ZERO)
            .isAccountConnected(portfolio.getAccount().isConnected())
            .createdAt(portfolio.getCreatedAt())
            .accountType(portfolio.getAccount().getAccountType())
            .build();
    }
}