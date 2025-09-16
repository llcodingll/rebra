package com.rebra.dto.response;

import com.rebra.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountListResponse {

    private List<AccountSummary> accounts;
    private int totalCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountSummary {
        private Long accountId;
        private String accountNumber; // 마스킹된 계좌번호
        private AccountType accountType; // 모의투자, 실계좌
        private String brokerName; // 한국투자증권
        private boolean isConnected; // 연결 상태
        private LocalDateTime registeredAt;
    }

    public static AccountListResponse of(List<AccountSummary> accounts) {
        return new AccountListResponse(accounts, accounts.size());
    }
}