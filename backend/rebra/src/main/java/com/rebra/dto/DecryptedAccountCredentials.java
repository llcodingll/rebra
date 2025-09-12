package com.rebra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 복호화된 계좌 인증 정보 DTO
 */
@Getter
@AllArgsConstructor
public class DecryptedAccountCredentials {
    private final String accountNumber;
    private final String appKey;
    private final String appSecret;
}