package com.rebra.dto.response;

import com.rebra.entity.Portfolio;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PortfolioCreateResponse {

    private Long id;
    private String name;
    private String description;
    private AccountInfo account;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class AccountInfo {
        private Long id;
        private String accountNumber;
        private String brokerName;

        public static AccountInfo of(Long id, String accountNumber, String brokerName) {
            return AccountInfo.builder()
                .id(id)
                .accountNumber(accountNumber)
                .brokerName(brokerName)
                .build();
        }
    }

    public static PortfolioCreateResponse of(Portfolio portfolio, String accountNumber) {
        return PortfolioCreateResponse.builder()
            .id(portfolio.getId())
            .name(portfolio.getName())
            .description(portfolio.getDescription())
            .account(AccountInfo.of(
                portfolio.getAccount().getId(),
                accountNumber, // 복호화된 계좌번호
                portfolio.getAccount().getBrokerName()
            ))
            .createdAt(portfolio.getCreatedAt())
            .updatedAt(portfolio.getUpdatedAt())
            .build();
    }
}