package com.rebra.util;

import com.rebra.config.TestAccountEncryptionConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestAccountEncryptionConfig.class)
@TestPropertySource(properties = {
    "security.encryption.account.hash-pepper=test-pepper-value-for-unit-test"
})
@DisplayName("AccountEncryptionUtil 테스트")
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

    @Test
    @DisplayName("null 값 처리 테스트")
    void handleNullValues() {
        Long userId = 1L;
        
        // null 입력시 그대로 반환되어야 함
        assertThat(AccountEncryptionUtil.encryptAccountNumber(null, userId)).isNull();
        assertThat(AccountEncryptionUtil.decryptAccountNumber(null, userId)).isNull();
        
        // 빈 문자열 처리
        assertThat(AccountEncryptionUtil.encryptAccountNumber("", userId)).isEmpty();
        assertThat(AccountEncryptionUtil.maskAccountNumber(null)).isEqualTo("****");
        assertThat(AccountEncryptionUtil.maskAccountNumber("")).isEqualTo("****");
    }

    @Test
    @DisplayName("잘못된 userId로 복호화 시도시 예외 발생")
    void decryptWithWrongUserId_shouldThrowException() {
        Long correctUserId = 1L;
        Long wrongUserId = 999L;
        String plainText = "test123";
        
        String encrypted = AccountEncryptionUtil.encryptAccountNumber(plainText, correctUserId);
        
        // 잘못된 userId로 복호화 시도시 런타임 예외 발생
        assertThatThrownBy(() -> 
            AccountEncryptionUtil.decryptAccountNumber(encrypted, wrongUserId))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("해시 생성시 null/빈값 처리")
    void generateHash_withNullOrEmpty_shouldThrowException() {
        assertThatThrownBy(() -> 
            AccountEncryptionUtil.generateAccountNumberHash(null))
            .isInstanceOf(Exception.class); // IllegalArgumentException 또는 RuntimeException
            
        assertThatThrownBy(() -> 
            AccountEncryptionUtil.generateAccountNumberHash(""))
            .isInstanceOf(Exception.class); // IllegalArgumentException 또는 RuntimeException
            
        assertThatThrownBy(() -> 
            AccountEncryptionUtil.generateAccountNumberHash("   "))
            .isInstanceOf(Exception.class); // IllegalArgumentException 또는 RuntimeException
    }

    @Test
    @DisplayName("서로 다른 사용자ID로 암호화된 데이터는 달라야 함")
    void differentUserIds_shouldProduceDifferentEncryption() {
        String plainText = "sameAccountNumber";
        Long userId1 = 1L;
        Long userId2 = 2L;
        
        String encrypted1 = AccountEncryptionUtil.encryptAccountNumber(plainText, userId1);
        String encrypted2 = AccountEncryptionUtil.encryptAccountNumber(plainText, userId2);
        
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("같은 데이터를 여러 번 암호화하면 매번 다른 결과 (IV 때문)")
    void sameData_multipleEncryptions_shouldBeDifferent() {
        Long userId = 1L;
        String plainText = "testData";
        
        String encrypted1 = AccountEncryptionUtil.encryptAccountNumber(plainText, userId);
        String encrypted2 = AccountEncryptionUtil.encryptAccountNumber(plainText, userId);
        
        // IV가 매번 달라서 암호화 결과도 달라야 함
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        
        // 하지만 복호화하면 동일한 결과
        assertThat(AccountEncryptionUtil.decryptAccountNumber(encrypted1, userId)).isEqualTo(plainText);
        assertThat(AccountEncryptionUtil.decryptAccountNumber(encrypted2, userId)).isEqualTo(plainText);
    }

    @Test
    @DisplayName("짧은 계좌번호 마스킹 테스트")
    void maskShortAccountNumber() {
        assertThat(AccountEncryptionUtil.maskAccountNumber("123")).isEqualTo("****");
        assertThat(AccountEncryptionUtil.maskAccountNumber("1234")).isEqualTo("****");
        assertThat(AccountEncryptionUtil.maskAccountNumber("12345")).isEqualTo("1234****");
    }
}
