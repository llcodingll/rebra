package com.rebra.exception.account;

import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;

public class AccountException extends CustomRuntimeException {

    public AccountException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    // 계좌 조회 관련 예외
    public static AccountException accountNotFound() {
        return new AccountException(ExceptionCode.ACCOUNT_NOT_FOUND);
    }

    public static AccountException accountAccessDenied() {
        return new AccountException(ExceptionCode.ACCOUNT_ACCESS_DENIED);
    }

    // 계좌 등록 관련 예외
    public static AccountException duplicateAccount() {
        return new AccountException(ExceptionCode.DUPLICATE_ACCOUNT);
    }

    public static AccountException accountRegistrationFailed() {
        return new AccountException(ExceptionCode.ACCOUNT_REGISTRATION_FAILED);
    }

    public static AccountException accountLimitExceeded() {
        return new AccountException(ExceptionCode.ACCOUNT_LIMIT_EXCEEDED);
    }

    // 계좌 인증 관련 예외
    public static AccountException accountVerificationFailed() {
        return new AccountException(ExceptionCode.ACCOUNT_VERIFICATION_FAILED);
    }

    public static AccountException invalidAccountCredentials() {
        return new AccountException(ExceptionCode.INVALID_ACCOUNT_CREDENTIALS);
    }

    public static AccountException kisConnectionFailed() {
        return new AccountException(ExceptionCode.KIS_CONNECTION_FAILED);
    }

    // 계좌 암복호화 관련 예외
    public static AccountException encryptionFailed() {
        return new AccountException(ExceptionCode.ACCOUNT_ENCRYPTION_FAILED);
    }

    public static AccountException decryptionFailed() {
        return new AccountException(ExceptionCode.ACCOUNT_DECRYPTION_FAILED);
    }

    // 계좌 삭제 관련 예외
    public static AccountException accountDeletionFailed() {
        return new AccountException(ExceptionCode.ACCOUNT_DELETION_FAILED);
    }

    public static AccountException accountAlreadyInactive() {
        return new AccountException(ExceptionCode.ACCOUNT_ALREADY_INACTIVE);
    }
}