package com.rebra.dto.response;

import com.rebra.dto.portfoliodata.RegisteredStockInfo;
import com.rebra.dto.portfoliodata.UnregisteredStockInfo;
import com.rebra.entity.Portfolio;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PortfolioDetailResponse {

    private PortfolioInfo portfolio;
    private List<RegisteredStockInfo> registeredStocks;
    private List<UnregisteredStockInfo> unregisteredStocks;

    @Data
    @Builder
    public static class PortfolioInfo {
        private Long id;
        private String name;
        private String description;
        private AccountInfo account;
        private Boolean autoRebalance;
        private String rebalancingType;
        private Integer rebalancingPeriod;
        private LocalDate startDate;
        private LocalDate nextRebalancingDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static PortfolioInfo from(Portfolio portfolio, String accountNumber) {
            return PortfolioInfo.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .description(portfolio.getDescription())
                .account(AccountInfo.from(portfolio.getAccount(), accountNumber))
                .autoRebalance(portfolio.getAutoRebalancing())
                .rebalancingType(portfolio.getRebalancingPeriod() != null ?
                    portfolio.getRebalancingPeriod().name() : null)
                .rebalancingPeriod(portfolio.getRebalancingInterval())
                .startDate(portfolio.getRebalancingStartDate())
                .nextRebalancingDate(portfolio.getNextRebalanceDate())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
        }
    }

    @Data
    @Builder
    public static class AccountInfo {
        private Long id;
        private String accountNumber;
        private String brokerName;

        public static AccountInfo from(com.rebra.entity.Account account, String accountNumber) {
            return AccountInfo.builder()
                .id(account.getId())
                .accountNumber(accountNumber)
                .brokerName(account.getBrokerName())
                .build();
        }
    }

    public static PortfolioDetailResponse of(Portfolio portfolio,
                                           String accountNumber,
                                           List<RegisteredStockInfo> registeredStocks,
                                           List<UnregisteredStockInfo> unregisteredStocks) {
        return PortfolioDetailResponse.builder()
            .portfolio(PortfolioInfo.from(portfolio, accountNumber))
            .registeredStocks(registeredStocks)
            .unregisteredStocks(unregisteredStocks)
            .build();
    }
}