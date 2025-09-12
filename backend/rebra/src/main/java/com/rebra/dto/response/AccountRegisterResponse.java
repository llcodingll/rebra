package com.rebra.dto.response;

import com.rebra.entity.AccountType;
import com.rebra.entity.ConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRegisterResponse {

    private Long accountId;
    private String accountNumber; // 마스킹된 계좌번호
    private AccountType accountType; // MOCK, REAL
    private String brokerName; // 한국투자증권
    private ConnectionStatus connectionStatus; // CONNECTED
    private LocalDateTime registeredAt;

    public static AccountRegisterResponse success(Long accountId, String maskedAccountNumber,
                                                  AccountType accountType, LocalDateTime registeredAt) {
        return new AccountRegisterResponse(
            accountId,
            maskedAccountNumber,
            accountType,
            "한국투자증권",
            ConnectionStatus.CONNECTED,
            registeredAt
        );
    }
}