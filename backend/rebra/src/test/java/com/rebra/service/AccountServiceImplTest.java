package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.request.AccountRegisterRequest;
import com.rebra.dto.request.AccountVerifyRequest;
import com.rebra.dto.response.AccountDetailResponse;
import com.rebra.dto.response.AccountListResponse;
import com.rebra.dto.response.AccountRegisterResponse;
import com.rebra.dto.response.AccountVerifyResponse;
import com.rebra.entity.Account;
import com.rebra.entity.AccountType;
import com.rebra.entity.User;
import com.rebra.exception.account.AccountException;
import com.rebra.exception.user.UserException;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.UserRepository;
import com.rebra.util.AccountConverter;
import com.rebra.util.AccountEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AccountService 테스트")
class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KisApiComponent kisApiComponent;

    private User testUser;
    private Account testAccount;
    private AccountVerifyRequest testVerifyRequest;
    private AccountRegisterRequest testRegisterRequest;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testAccount = createTestAccount();
        testVerifyRequest = createTestVerifyRequest();
        testRegisterRequest = createTestRegisterRequest();
    }

    private User createTestUser() {
        User user = User.builder()
                .sub("test-sub")
                .nickname("테스트사용자")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Account createTestAccount() {
        Account account = Account.builder()
                .user(testUser)
                .accountNumber("encrypted-account-number")
                .accountNumberHash("hashed-account-number")
                .appKey("encrypted-app-key")
                .appSecret("encrypted-app-secret")
                .brokerName("한국투자증권")
                .accountType(AccountType.MOCK)
                .isConnected(true)
                .build();
        ReflectionTestUtils.setField(account, "id", 1L);
        return account;
    }

    private AccountVerifyRequest createTestVerifyRequest() {
        return new AccountVerifyRequest(
                "12345678",
                "test-app-key-12345678901234567890",
                "test-app-secret-12345678901234567890123456789012345678901234567890",
                AccountType.MOCK
        );
    }

    private AccountRegisterRequest createTestRegisterRequest() {
        return new AccountRegisterRequest(
                "12345678",
                "test-app-key-12345678901234567890",
                "test-app-secret-12345678901234567890123456789012345678901234567890",
                AccountType.MOCK
        );
    }

    @Nested
    @DisplayName("계좌 인증 테스트")
    class VerifyAccountTest {

        @Test
        @DisplayName("성공 - KIS API 연결 성공")
        void verifyAccount_성공() {
            // Given
            doNothing().when(kisApiComponent).verifyAccount(
                    testVerifyRequest.getAccountNumber(),
                    testVerifyRequest.getAppKey(),
                    testVerifyRequest.getAppSecret(),
                    testVerifyRequest.getAccountType()
            );

            // When
            AccountVerifyResponse response = accountService.verifyAccount(testVerifyRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.isConnected()).isTrue();
            assertThat(response.getAccountType()).isEqualTo(AccountType.MOCK);
            assertThat(response.getBrokerName()).isEqualTo("한국투자증권");

            verify(kisApiComponent).verifyAccount(
                    testVerifyRequest.getAccountNumber(),
                    testVerifyRequest.getAppKey(),
                    testVerifyRequest.getAppSecret(),
                    testVerifyRequest.getAccountType()
            );
        }

        @Test
        @DisplayName("실패 - KIS API 연결 실패")
        void verifyAccount_실패_연결오류() {
            // Given
            doThrow(new RuntimeException("KIS API 연결 실패"))
                    .when(kisApiComponent).verifyAccount(
                            testVerifyRequest.getAccountNumber(),
                            testVerifyRequest.getAppKey(),
                            testVerifyRequest.getAppSecret(),
                            testVerifyRequest.getAccountType()
                    );

            // When & Then
            assertThatThrownBy(() -> accountService.verifyAccount(testVerifyRequest))
                    .isInstanceOf(AccountException.class)
                    .hasMessageContaining("계좌 인증에 실패했습니다");

            verify(kisApiComponent).verifyAccount(
                    testVerifyRequest.getAccountNumber(),
                    testVerifyRequest.getAppKey(),
                    testVerifyRequest.getAppSecret(),
                    testVerifyRequest.getAccountType()
            );
        }
    }

    @Nested
    @DisplayName("계좌 등록 테스트")
    class RegisterAccountTest {

        @Test
        @DisplayName("성공 - 계좌 등록 성공")
        void registerAccount_성공() {
            // Given
            Long userId = 1L;
            String hashedAccountNumber = "hashed-account-number";

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(accountRepository.existsByAccountNumberHash(hashedAccountNumber)).willReturn(false);
            doNothing().when(kisApiComponent).verifyAccount(
                    testRegisterRequest.getAccountNumber(),
                    testRegisterRequest.getAppKey(),
                    testRegisterRequest.getAppSecret(),
                    testRegisterRequest.getAccountType()
            );
            given(accountRepository.save(any(Account.class))).willReturn(testAccount);
            doNothing().when(kisApiComponent).addUserCredentials(
                    eq(userId), any(Long.class), eq(testRegisterRequest.getAccountNumber()),
                    eq(testRegisterRequest.getAppKey()), eq(testRegisterRequest.getAppSecret()),
                    eq(testRegisterRequest.getAccountType())
            );

            try (MockedStatic<AccountEncryptionUtil> mockedUtil = mockStatic(AccountEncryptionUtil.class);
                 MockedStatic<AccountConverter> mockedConverter = mockStatic(AccountConverter.class)) {

                mockedUtil.when(() -> AccountEncryptionUtil.generateAccountNumberHash(testRegisterRequest.getAccountNumber()))
                         .thenReturn(hashedAccountNumber);
                mockedUtil.when(() -> AccountEncryptionUtil.maskAccountNumber(testRegisterRequest.getAccountNumber()))
                         .thenReturn("123-45-***01");
                mockedConverter.when(() -> AccountConverter.fromRegisterRequest(testUser, testRegisterRequest, testRegisterRequest.getAccountType()))
                               .thenReturn(testAccount);

                // When
                AccountRegisterResponse response = accountService.registerAccount(userId, testRegisterRequest);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getAccountId()).isEqualTo(testAccount.getId());
                assertThat(response.getAccountType()).isEqualTo(AccountType.MOCK);
                assertThat(response.getAccountNumber()).isEqualTo("123-45-***01");

                verify(userRepository).findById(userId);
                verify(accountRepository).existsByAccountNumberHash(hashedAccountNumber);
                verify(kisApiComponent).verifyAccount(
                        testRegisterRequest.getAccountNumber(),
                        testRegisterRequest.getAppKey(),
                        testRegisterRequest.getAppSecret(),
                        testRegisterRequest.getAccountType()
                );
                verify(accountRepository).save(any(Account.class));
                verify(kisApiComponent).addUserCredentials(
                        eq(userId), any(Long.class), eq(testRegisterRequest.getAccountNumber()),
                        eq(testRegisterRequest.getAppKey()), eq(testRegisterRequest.getAppSecret()),
                        eq(testRegisterRequest.getAccountType())
                );
            }
        }

        @Test
        @DisplayName("실패 - 사용자를 찾을 수 없음")
        void registerAccount_실패_사용자없음() {
            // Given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> accountService.registerAccount(userId, testRegisterRequest))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining("사용자를 찾을수 없습니다");

            verify(userRepository).findById(userId);
            verifyNoInteractions(accountRepository);
            verifyNoInteractions(kisApiComponent);
        }

        @Test
        @DisplayName("실패 - 중복 계좌 등록")
        void registerAccount_실패_중복계좌() {
            // Given
            Long userId = 1L;
            String hashedAccountNumber = "hashed-account-number";

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(accountRepository.existsByAccountNumberHash(hashedAccountNumber)).willReturn(true);

            try (MockedStatic<AccountEncryptionUtil> mockedUtil = mockStatic(AccountEncryptionUtil.class)) {
                mockedUtil.when(() -> AccountEncryptionUtil.generateAccountNumberHash(testRegisterRequest.getAccountNumber()))
                         .thenReturn(hashedAccountNumber);

                // When & Then
                assertThatThrownBy(() -> accountService.registerAccount(userId, testRegisterRequest))
                        .isInstanceOf(AccountException.class)
                        .hasMessageContaining("이미 등록된 계좌입니다");

                verify(userRepository).findById(userId);
                verify(accountRepository).existsByAccountNumberHash(hashedAccountNumber);
                verifyNoMoreInteractions(kisApiComponent);
            }
        }

        @Test
        @DisplayName("실패 - KIS API 재검증 실패")
        void registerAccount_실패_KIS연결실패() {
            // Given
            Long userId = 1L;
            String hashedAccountNumber = "hashed-account-number";

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(accountRepository.existsByAccountNumberHash(hashedAccountNumber)).willReturn(false);
            doThrow(new RuntimeException("KIS API 연결 실패"))
                    .when(kisApiComponent).verifyAccount(
                            testRegisterRequest.getAccountNumber(),
                            testRegisterRequest.getAppKey(),
                            testRegisterRequest.getAppSecret(),
                            testRegisterRequest.getAccountType()
                    );

            try (MockedStatic<AccountEncryptionUtil> mockedUtil = mockStatic(AccountEncryptionUtil.class)) {
                mockedUtil.when(() -> AccountEncryptionUtil.generateAccountNumberHash(testRegisterRequest.getAccountNumber()))
                         .thenReturn(hashedAccountNumber);

                // When & Then
                assertThatThrownBy(() -> accountService.registerAccount(userId, testRegisterRequest))
                        .isInstanceOf(AccountException.class)
                        .hasMessageContaining("계좌 등록에 실패했습니다");

                verify(userRepository).findById(userId);
                verify(accountRepository).existsByAccountNumberHash(hashedAccountNumber);
                verify(kisApiComponent).verifyAccount(
                        testRegisterRequest.getAccountNumber(),
                        testRegisterRequest.getAppKey(),
                        testRegisterRequest.getAppSecret(),
                        testRegisterRequest.getAccountType()
                );
                verifyNoMoreInteractions(accountRepository);
            }
        }
    }

    @Nested
    @DisplayName("계좌 목록 조회 테스트")
    class GetAccountListTest {

        @Test
        @DisplayName("성공 - 계좌 목록 조회 성공")
        void getAccountList_성공() {
            // Given
            Long userId = 1L;
            List<Account> accounts = Arrays.asList(testAccount);
            AccountListResponse.AccountSummary accountSummary = new AccountListResponse.AccountSummary(
                    1L, "123-45-***01", AccountType.MOCK, "한국투자증권", true, testAccount.getCreatedAt()
            );

            given(accountRepository.findAvailableAccountsByUserId(userId)).willReturn(accounts);

            try (MockedStatic<AccountConverter> mockedConverter = mockStatic(AccountConverter.class)) {
                mockedConverter.when(() -> AccountConverter.toAccountSummary(testAccount))
                              .thenReturn(accountSummary);

                // When
                AccountListResponse response = accountService.getAccountList(userId);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getTotalCount()).isEqualTo(1);
                assertThat(response.getAccounts()).hasSize(1);
                assertThat(response.getAccounts().get(0).getAccountId()).isEqualTo(1L);
                assertThat(response.getAccounts().get(0).getAccountType()).isEqualTo(AccountType.MOCK);

                verify(accountRepository).findAvailableAccountsByUserId(userId);
            }
        }

        @Test
        @DisplayName("성공 - 빈 계좌 목록")
        void getAccountList_빈목록() {
            // Given
            Long userId = 1L;
            given(accountRepository.findAvailableAccountsByUserId(userId)).willReturn(Collections.emptyList());

            // When
            AccountListResponse response = accountService.getAccountList(userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotalCount()).isEqualTo(0);
            assertThat(response.getAccounts()).isEmpty();

            verify(accountRepository).findAvailableAccountsByUserId(userId);
        }
    }

    @Nested
    @DisplayName("계좌 상세 조회 테스트")
    class GetAccountDetailTest {

        @Test
        @DisplayName("성공 - 계좌 상세 조회 성공")
        void getAccountDetail_성공() {
            // Given
            Long userId = 1L;
            Long accountId = 1L;
            AccountDetailResponse accountDetail = AccountDetailResponse.of(
                    accountId, "123-45-***01", "***", AccountType.MOCK, "한국투자증권",
                    true, testAccount.getCreatedAt(), "정상", true
            );

            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.of(testAccount));

            try (MockedStatic<AccountConverter> mockedConverter = mockStatic(AccountConverter.class)) {
                mockedConverter.when(() -> AccountConverter.toAccountDetail(testAccount))
                              .thenReturn(accountDetail);

                // When
                AccountDetailResponse response = accountService.getAccountDetail(userId, accountId);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getAccountId()).isEqualTo(accountId);
                assertThat(response.getAccountType()).isEqualTo(AccountType.MOCK);
                assertThat(response.getBrokerName()).isEqualTo("한국투자증권");
                assertThat(response.isConnected()).isTrue();

                verify(accountRepository).findByIdAndUserId(accountId, userId);
            }
        }

        @Test
        @DisplayName("실패 - 계좌를 찾을 수 없음")
        void getAccountDetail_실패_계좌없음() {
            // Given
            Long userId = 1L;
            Long accountId = 999L;
            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> accountService.getAccountDetail(userId, accountId))
                    .isInstanceOf(AccountException.class)
                    .hasMessageContaining("계좌를 찾을 수 없습니다");

            verify(accountRepository).findByIdAndUserId(accountId, userId);
        }
    }

    @Nested
    @DisplayName("계좌 삭제 테스트")
    class DeleteAccountTest {

        @Test
        @DisplayName("성공 - 계좌 삭제 성공")
        void deleteAccount_성공() {
            // Given
            Long userId = 1L;
            Long accountId = 1L;

            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.of(testAccount));
            doNothing().when(portfolioRepository).deleteByAccountId(accountId);
            doNothing().when(kisApiComponent).removeUserCredentials(userId, accountId, testAccount.getAccountType());
            doNothing().when(accountRepository).delete(testAccount);

            // When
            accountService.deleteAccount(userId, accountId);

            // Then
            verify(accountRepository).findByIdAndUserId(accountId, userId);
            verify(portfolioRepository).deleteByAccountId(accountId);
            verify(kisApiComponent).removeUserCredentials(userId, accountId, testAccount.getAccountType());
            verify(accountRepository).delete(testAccount);
        }

        @Test
        @DisplayName("실패 - 계좌를 찾을 수 없음")
        void deleteAccount_실패_계좌없음() {
            // Given
            Long userId = 1L;
            Long accountId = 999L;
            given(accountRepository.findByIdAndUserId(accountId, userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> accountService.deleteAccount(userId, accountId))
                    .isInstanceOf(AccountException.class)
                    .hasMessageContaining("계좌를 찾을 수 없습니다");

            verify(accountRepository).findByIdAndUserId(accountId, userId);
            verifyNoInteractions(portfolioRepository);
            verifyNoInteractions(kisApiComponent);
            verifyNoMoreInteractions(accountRepository);
        }
    }
}