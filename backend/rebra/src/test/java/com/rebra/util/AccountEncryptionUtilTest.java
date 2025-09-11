package com.rebra.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountEncryptionUtilTest {

    @Test
    @DisplayName("계좌번호 암호화 후 복호화가 원본과 일치해야 한다")
    void encryptAndDecryptAccountNumber_shouldReturnOriginal() {
        Long userId = 1L;
        String plain = "1234567890";

        String encrypted = AccountEncryptionUtil.encryptAccountNumber(plain, userId);
        String decrypted = AccountEncryptionUtil.decryptAccountNumber(encrypted, userId);

        assertThat(decrypted).isEqualTo(plain);
    }

    @Test
    @DisplayName("앱키 암호화 후 복호화가 원본과 일치해야 한다")
    void encryptAndDecryptAppKey_shouldReturnOriginal() {
        Long userId = 2L;
        String plain = "myAppKey123";

        String encrypted = AccountEncryptionUtil.encryptAppKey(plain, userId);
        String decrypted = AccountEncryptionUtil.decryptAppKey(encrypted, userId);

        assertThat(decrypted).isEqualTo(plain);
    }

    @Test
    @DisplayName("앱시크릿 암호화 후 복호화가 원본과 일치해야 한다")
    void encryptAndDecryptAppSecret_shouldReturnOriginal() {
        Long userId = 3L;
        String plain = "mySuperSecret";

        String encrypted = AccountEncryptionUtil.encryptAppSecret(plain, userId);
        String decrypted = AccountEncryptionUtil.decryptAppSecret(encrypted, userId);

        assertThat(decrypted).isEqualTo(plain);
    }

    @Test
    @DisplayName("계좌번호 해시는 동일한 입력이면 항상 동일해야 한다")
    void generateAccountNumberHash_shouldBeConsistent() {
        String account = "1234567890";
        String hash1 = AccountEncryptionUtil.generateAccountNumberHash(account);
        String hash2 = AccountEncryptionUtil.generateAccountNumberHash(account);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("계좌번호 마스킹이 올바르게 처리되어야 한다")
    void maskAccountNumber_shouldMaskCorrectly() {
        String masked = AccountEncryptionUtil.maskAccountNumber("1234567890");
        assertThat(masked).isEqualTo("1234****");
    }

    @Test
    @DisplayName("앱키 마스킹이 올바르게 처리되어야 한다")
    void maskAppKey_shouldMaskCorrectly() {
        String masked = AccountEncryptionUtil.maskAppKey("abcd1234");
        assertThat(masked).isEqualTo("abcd****");
    }

    @Test
    @DisplayName("앱시크릿 마스킹이 올바르게 처리되어야 한다")
    void maskAppSecret_shouldMaskCorrectly() {
        String masked = AccountEncryptionUtil.maskAppSecret("secret123");
        assertThat(masked).isEqualTo("****");
    }
}
