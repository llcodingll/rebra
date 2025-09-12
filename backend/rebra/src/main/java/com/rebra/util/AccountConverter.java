package com.rebra.util;

import com.rebra.dto.request.AccountRegisterRequest;
import com.rebra.dto.response.AccountDetailResponse;
import com.rebra.dto.response.AccountListResponse;
import com.rebra.dto.response.AccountListResponse.AccountSummary;
import com.rebra.entity.Account;
import com.rebra.entity.AccountType;
import com.rebra.entity.ConnectionStatus;
import com.rebra.entity.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountConverter {

    private AccountConverter() {
        // 유틸리티 클래스이므로 인스턴스 생성 방지
    }

    /**
     * 계좌번호 복호화 및 마스킹 처리 (예외 처리 포함)
     */
    private static String decryptAndMaskAccountNumber(String encryptedAccountNumber, Long userId) {
        try {
            String decrypted = AccountEncryptionUtil.decryptAccountNumber(encryptedAccountNumber, userId);
            return AccountEncryptionUtil.maskAccountNumber(decrypted);
        } catch (Exception e) {
            log.error("계좌번호 복호화/마스킹 실패 - 사용자ID: {}", userId, e);
            return "****-****-****";
        }
    }

    /**
     * 앱키 복호화 및 마스킹 처리 (예외 처리 포함)
     */
    private static String decryptAndMaskAppKey(String encryptedAppKey, Long userId) {
        try {
            String decrypted = AccountEncryptionUtil.decryptAppKey(encryptedAppKey, userId);
            return AccountEncryptionUtil.maskAppKey(decrypted);
        } catch (Exception e) {
            log.error("앱키 복호화/마스킹 실패 - 사용자ID: {}", userId, e);
            return "****";
        }
    }

    public static AccountSummary toAccountSummary(Account account) {
        String maskedAccountNumber = decryptAndMaskAccountNumber(
            account.getAccountNumber(), account.getUser().getId());

        return new AccountListResponse.AccountSummary(
            account.getId(),
            maskedAccountNumber,
            account.getAccountType().name(),
            account.getBrokerName(),
            account.getConnectionStatus().name(),
            account.getCreatedAt()
        );
    }

    public static AccountDetailResponse toAccountDetail(Account account) {
        String maskedAccountNumber = decryptAndMaskAccountNumber(
            account.getAccountNumber(), account.getUser().getId());
        String maskedAppKey = decryptAndMaskAppKey(
            account.getAppKey(), account.getUser().getId());

        return new AccountDetailResponse(
            account.getId(),
            maskedAccountNumber,
            maskedAppKey,
            account.getAccountType().name(),
            account.getBrokerName(),
            account.getConnectionStatus().name(),
            account.getCreatedAt(),
            account.getIsDeleted(),
            "연결 상태 양호",
            !account.getIsDeleted()
        );
    }

    public static Account fromRegisterRequest(User user, AccountRegisterRequest request, AccountType accountType) {
        try {
            String encryptedAccountNumber = AccountEncryptionUtil.encryptAccountNumber(
                request.getAccountNumber(), user.getId());
            String accountNumberHash = AccountEncryptionUtil.generateAccountNumberHash(
                request.getAccountNumber());
            String encryptedAppKey = AccountEncryptionUtil.encryptAppKey(
                request.getAppKey(), user.getId());
            String encryptedAppSecret = AccountEncryptionUtil.encryptAppSecret(
                request.getAppSecret(), user.getId());

            return Account.builder()
                .user(user)
                .accountNumber(encryptedAccountNumber)
                .accountNumberHash(accountNumberHash)
                .appKey(encryptedAppKey)
                .appSecret(encryptedAppSecret)
                .brokerName("한국투자증권") // TODO: 확장 할까요 말까요..?
                .accountType(accountType)
                .isDeleted(false)
                .connectionStatus(ConnectionStatus.CONNECTED)
                .build();

        } catch (Exception e) {
            log.error("계좌 정보 암호화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("계좌 정보 암호화 실패", e);
        }
    }
}