package com.rebra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailResponse {

    private Long accountId;
    private String accountNumber; // 마스킹된 계좌번호
    private String appKey; // 마스킹된 앱키
    private String accountType; // MOCK, REAL
    private String brokerName; // 한국투자증권
    private boolean isConnected; // 연결 상태
    private LocalDateTime registeredAt;

    // 계좌 상태 정보
    private String statusMessage;
    private boolean canTrade; // 거래 가능 여부

    public static AccountDetailResponse of(Long accountId, String maskedAccountNumber,
            String maskedAppKey, String accountType, String brokerName,
            boolean isConnected, LocalDateTime registeredAt,
            String statusMessage, boolean canTrade) {
        return new AccountDetailResponse(
            accountId, maskedAccountNumber, maskedAppKey, accountType,
            brokerName, isConnected, registeredAt,
            statusMessage, canTrade
        );
    }
}