package com.rebra.service;

import static com.rebra.entity.AccountType.REAL;
import static com.rebra.exception.ExceptionCode.ACCOUNT_NOT_FOUND;
import static com.rebra.exception.ExceptionCode.KIS_API_ERROR;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.dto.request.StockTradeRequest;
import com.rebra.entity.Account;
import com.rebra.entity.User;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.UserRepository;
import com.rebra.util.AccountEncryptionUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockTradingServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StockTradingServiceImpl stockTradingService;

    private User testUser;
    private Account testAccount;
    private DecryptedAccountCredentials testCredentials;
    private StockTradeRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .sub("test_sub")
                .nickname("테스트사용자")
                .build();
        setField(testUser, "id", 1L);

        testAccount = Account.builder()
                .accountNumber("12345678901234567890")
                .appKey("encrypted_app_key")
                .appSecret("encrypted_app_secret")
                .accountType(REAL)
                .user(testUser)
                .build();
        setField(testAccount, "id", 1L);

        testCredentials = new DecryptedAccountCredentials(
                "1234567890",
                "decrypted_app_key",
                "decrypted_app_secret"
        );

        testRequest = new StockTradeRequest();
        setField(testRequest, "orderType", "00");
        setField(testRequest, "quantity", 10);
        setField(testRequest, "price", 70000L);
        setField(testRequest, "accountId", 1L);
    }

    @Test
    @DisplayName("계좌 없음 - 예외 발생")
    void buyStock_AccountNotFound_ThrowsException() {
        // Given
        String stockCode = "005930";
//        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stockTradingService.buyStock(stockCode, testRequest, 1L))
                .isInstanceOf(CustomRuntimeException.class)
                .hasFieldOrPropertyWithValue("exceptionCode", ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("계좌 복호화 실패 - 예외 발생")
    void buyStock_DecryptionFailed_ThrowsException() {
        // Given
        String stockCode = "005930";
        given(accountRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.of(testAccount));

        try (MockedStatic<AccountEncryptionUtil> mockedUtil = Mockito.mockStatic(AccountEncryptionUtil.class)) {
//            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            mockedUtil.when(() -> AccountEncryptionUtil.decryptAccountCredentials(testAccount, 1L))
                    .thenThrow(new RuntimeException("복호화 실패"));

            // When & Then
            assertThatThrownBy(() -> stockTradingService.buyStock(stockCode, testRequest, 1L))
                    .isInstanceOf(CustomRuntimeException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", KIS_API_ERROR);
        }
    }

    @Test
    @DisplayName("시장가 주문 - null 가격 처리")
    void buyStock_MarketOrder_NullPrice() {
        // Given
        String stockCode = "005930";
        StockTradeRequest marketOrderRequest = new StockTradeRequest();
        setField(marketOrderRequest, "orderType", "01"); // 시장가
        setField(marketOrderRequest, "quantity", 10);
        setField(marketOrderRequest, "price", null); // 시장가는 가격 null
        setField(marketOrderRequest, "accountId", 1L);

//        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.empty()); // 계좌 없음으로 KIS API 호출 전에 예외 발생

        // When & Then
        assertThatThrownBy(() -> stockTradingService.buyStock(stockCode, marketOrderRequest, 1L))
                .isInstanceOf(CustomRuntimeException.class)
                .hasFieldOrPropertyWithValue("exceptionCode", ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 사용자 계좌 접근 시도 - 계좌 없음 예외")
    void buyStock_OtherUserAccount_ThrowsException() {
        // Given
        String stockCode = "005930";
        User otherUser = User.builder()
                .sub("other_sub")
                .nickname("다른사용자")
                .build();
        setField(otherUser, "id", 2L);

//        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));
        given(accountRepository.findByIdAndUserId(1L, 2L))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stockTradingService.buyStock(stockCode, testRequest, 2L))
                .isInstanceOf(CustomRuntimeException.class)
                .hasFieldOrPropertyWithValue("exceptionCode", ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("매도 주문 - 계좌 없음 예외")
    void sellStock_AccountNotFound_ThrowsException() {
        // Given
        String stockCode = "005930";
//        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stockTradingService.sellStock(stockCode, testRequest, 1L))
                .isInstanceOf(CustomRuntimeException.class)
                .hasFieldOrPropertyWithValue("exceptionCode", ACCOUNT_NOT_FOUND);
    }
}