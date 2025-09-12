package com.rebra.dto.response;

import com.rebra.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountVerifyResponse {

    private String connectionStatus;
    private AccountType accountType; // 모의투자 or 실계좌
    private String brokerName; // 증권사명 (한국투자증권)
    
    public static AccountVerifyResponse success(AccountType accountType) {
        return new AccountVerifyResponse(
            "CONNECTED", 
            accountType,
            "한국투자증권"
        );
    }
}