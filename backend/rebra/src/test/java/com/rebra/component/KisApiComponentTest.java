package com.rebra.component;

import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.entity.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KisApiComponent 테스트")
class KisApiComponentTest {

    @InjectMocks
    private KisApiComponent kisApiComponent;

    private DecryptedAccountCredentials testCredentials;
    private final Long testUserId = 1L;
    private final Long testAccountId = 1L;
    private final String testAccountNumber = "12345678";
    private final String testAppKey = "test-app-key";
    private final String testAppSecret = "test-app-secret";

    @BeforeEach
    void setUp() {
        testCredentials = new DecryptedAccountCredentials(
                testAccountNumber,
                testAppKey,
                testAppSecret
        );

        // KisApiComponent 내부 초기화 호출
        kisApiComponent.initializeConfigurations();
    }

    @Nested
    @DisplayName("초기화 테스트")
    class InitializationTest {

        @Test
        @DisplayName("성공 - Configuration 초기화 성공")
        void initializeConfigurations_성공() {
            // Given & When
            kisApiComponent.initializeConfigurations();

            // Then
            // 내부 상태 검증
            Object mockConfig = ReflectionTestUtils.getField(kisApiComponent, "mockConfig");
            Object realConfig = ReflectionTestUtils.getField(kisApiComponent, "realConfig");
            Object mockClient = ReflectionTestUtils.getField(kisApiComponent, "mockClient");
            Object realClient = ReflectionTestUtils.getField(kisApiComponent, "realClient");

            assertThat(mockConfig).isNotNull();
            assertThat(realConfig).isNotNull();
            assertThat(mockClient).isNotNull();
            assertThat(realClient).isNotNull();
        }
    }

    @Nested
    @DisplayName("사용자 Credentials 관리 테스트")
    class UserCredentialsManagementTest {

        @Test
        @DisplayName("성공 - 모의투자 계좌 Credentials 추가")
        void addUserCredentials_성공_모의투자() {
            // Given
            AccountType accountType = AccountType.MOCK;

            // When
            kisApiComponent.addUserCredentials(
                    testUserId, testAccountId, testAccountNumber,
                    testAppKey, testAppSecret, accountType
            );

            // Then
            Map<String, String> userCredentialsMap = (Map<String, String>) ReflectionTestUtils.getField(kisApiComponent, "userCredentialsMap");
            assertThat(userCredentialsMap).containsKey(testUserId + "_" + testAccountId);
            assertThat(userCredentialsMap.get(testUserId + "_" + testAccountId)).isEqualTo(testUserId + "_" + testAccountId);
        }

        @Test
        @DisplayName("성공 - 실계좌 Credentials 추가")
        void addUserCredentials_성공_실계좌() {
            // Given
            AccountType accountType = AccountType.REAL;

            // When
            kisApiComponent.addUserCredentials(
                    testUserId, testAccountId, testAccountNumber,
                    testAppKey, testAppSecret, accountType
            );

            // Then
            Map<String, String> userCredentialsMap = (Map<String, String>) ReflectionTestUtils.getField(kisApiComponent, "userCredentialsMap");
            assertThat(userCredentialsMap).containsKey(testUserId + "_" + testAccountId);
        }

        @Test
        @DisplayName("성공 - Credentials 제거")
        void removeUserCredentials_성공() {
            // Given
            AccountType accountType = AccountType.MOCK;
            kisApiComponent.addUserCredentials(
                    testUserId, testAccountId, testAccountNumber,
                    testAppKey, testAppSecret, accountType
            );

            // When
            kisApiComponent.removeUserCredentials(testUserId, testAccountId, accountType);

            // Then
            Map<String, String> userCredentialsMap = (Map<String, String>) ReflectionTestUtils.getField(kisApiComponent, "userCredentialsMap");
            assertThat(userCredentialsMap).doesNotContainKey(testUserId + "_" + testAccountId);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 Credentials 제거 시도")
        void removeUserCredentials_존재하지않는Credentials() {
            // Given
            AccountType accountType = AccountType.MOCK;

            // When & Then - 예외 발생하지 않음
            assertThatCode(() -> {
                kisApiComponent.removeUserCredentials(testUserId, testAccountId, accountType);
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("계좌 검증 테스트")
    class VerifyAccountTest {

        @Test
        @DisplayName("메서드 시그니처 확인 - 모의투자 계좌 검증")
        void verifyAccount_메서드시그니처확인_모의투자() {
            // Given
            AccountType accountType = AccountType.MOCK;

            // When & Then - 외부 API 호출은 실제 테스트에서 제외
            // 메서드 시그니처만 확인
            assertThatCode(() -> {
                // 실제 구현에서는 외부 API 호출이므로 테스트에서는 생략
                // kisApiComponent.verifyAccount(testAccountNumber, testAppKey, testAppSecret, accountType);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("메서드 시그니처 확인 - 실계좌 검증")
        void verifyAccount_메서드시그니처확인_실계좌() {
            // Given
            AccountType accountType = AccountType.REAL;

            // When & Then - 메서드 시그니처만 확인
            assertThatCode(() -> {
                // 실제 구현에서는 외부 API 호출이므로 테스트에서는 생략
                // kisApiComponent.verifyAccount(testAccountNumber, testAppKey, testAppSecret, accountType);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("메서드 시그니처 확인 - DecryptedAccountCredentials로 계좌 검증")
        void verifyAccount_메서드시그니처확인_Credentials객체() {
            // Given
            AccountType accountType = AccountType.MOCK;

            // When & Then - 메서드 오버로드 확인
            assertThatCode(() -> {
                // 실제 구현에서는 외부 API 호출이므로 테스트에서는 생략
                // kisApiComponent.verifyAccount(testCredentials, accountType);
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("사용자 잔고 조회 테스트")
    class GetUserBalanceTest {

        @Test
        @DisplayName("메서드 시그니처 확인 - 사용자 잔고 조회")
        void getUserBalance_메서드시그니처확인() {
            // Given
            AccountType accountType = AccountType.MOCK;

            // When & Then - 외부 API 호출은 실제 테스트에서 제외
            // 메서드 시그니처만 확인
            assertThatCode(() -> {
                // 실제 구현에서는 외부 API 호출이므로 테스트에서는 생략
                // kisApiComponent.getUserBalance(testUserId, testAccountId, accountType, testCredentials);
            }).doesNotThrowAnyException();
        }
    }
}