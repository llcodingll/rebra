package com.rebra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mockStatic;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.entity.Account;
import com.rebra.entity.AccountType;
import com.rebra.entity.Stock;
import com.rebra.entity.User;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.StockRepository;
import com.rebra.util.AccountEncryptionUtil;
import com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult;
import com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult.Output1;
import com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult.Output2;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockServiceImpl 차트 API 테스트")
class StockServiceImplChartTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private KisRealtimeService kisRealtimeService;

    @Mock
    private KisApiComponent kisApiComponent;

    @InjectMocks
    private StockServiceImpl stockService;

    private Stock testStock;
    private User testUser;
    private Account testAccount;
    private DecryptedAccountCredentials testCredentials;
    private InquireDailyItemchartpriceResult mockKisResult;

    @BeforeEach
    void setUp() {
        // 테스트용 User 객체 생성
        testUser = User.builder()
                .sub("test_sub")
                .nickname("테스트사용자")
                .build();
        setField(testUser, "id", 1L);

        // 테스트용 Stock 객체 생성
        testStock = Stock.builder()
                .stockCode("005930")
                .stockName("삼성전자")
                .stockType("STOCK")
                .isActive(true)
                .build();
        setField(testStock, "id", 1L);

        // 테스트용 Account 객체 생성
        testAccount = Account.builder()
                .user(testUser)
                .accountNumber("encrypted_account_number")
                .accountNumberHash("hash_value")
                .appKey("encrypted_app_key")
                .appSecret("encrypted_app_secret")
                .accountType(AccountType.MOCK)
                .brokerName("한국투자증권")
                .isConnected(true)
                .build();
        setField(testAccount, "id", 1L);

        // 테스트용 DecryptedAccountCredentials 객체 생성
        testCredentials = new DecryptedAccountCredentials(
                "1234567890",
                "test_app_key",
                "test_app_secret"
        );

        // Mock KIS API 응답 데이터는 각 테스트에서 개별 생성
    }

    private InquireDailyItemchartpriceResult createMockKisApiResponse() {
        InquireDailyItemchartpriceResult result = mock(InquireDailyItemchartpriceResult.class);

        // Output1 Mock
        Output1 output1 = mock(Output1.class);
        given(output1.getStckPrpr()).willReturn("71000");
        given(output1.getPrdyVrss()).willReturn("1000");
        given(output1.getPrdyCtrt()).willReturn("1.43");
        given(output1.getPrdyVrssSign()).willReturn("2");
        given(output1.getAcmlVol()).willReturn("15000000");
        given(output1.getHtsAvls()).willReturn("425000000000000");
        given(output1.getPer()).willReturn("12.5");
        given(output1.getPbr()).willReturn("0.8");

        // Output2 Mock
        Output2 output2Item = mock(Output2.class);
        given(output2Item.getStckBsopDate()).willReturn("20241215");
        given(output2Item.getStckOprc()).willReturn("70000");
        given(output2Item.getStckHgpr()).willReturn("72000");
        given(output2Item.getStckLwpr()).willReturn("69000");
        given(output2Item.getStckClpr()).willReturn("71000");
        given(output2Item.getAcmlVol()).willReturn("15000000");
        given(output2Item.getAcmlTrPbmn()).willReturn("1065000000000");
        given(output2Item.getPrdyVrss()).willReturn("1000");
        given(output2Item.getPrdyVrssSign()).willReturn("2");
        given(output2Item.getPrttRate()).willReturn("1.43");

        Output2[] output2Array = {output2Item};

        given(result.getOutput1()).willReturn(output1);
        given(result.getOutput2()).willReturn(output2Array);

        return result;
    }

    @ParameterizedTest
    @ValueSource(strings = {"D", "W", "M", "Y"})
    @DisplayName("차트 데이터 조회 성공 - 모든 기간 타입")
    void getStockChartData_Success_AllPeriodTypes(String periodType) {
        // Given
        String stockCode = "005930";
        String startDate = "20240101";
        String endDate = "20241231";
        Long userId = 1L;

        given(accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true))
                .willReturn(Optional.of(testAccount));

        try (MockedStatic<AccountEncryptionUtil> mockedUtil = mockStatic(AccountEncryptionUtil.class)) {
            mockedUtil.when(() -> AccountEncryptionUtil.decryptAccountCredentials(testAccount, userId))
                    .thenReturn(testCredentials);

            InquireDailyItemchartpriceResult mockKisResult = createMockKisApiResponse();
            willDoNothing().given(kisApiComponent)
                    .ensureUserCredentials(userId, testAccount.getId(), testAccount.getAccountType(), testCredentials);
            given(kisApiComponent.getStockChartData(userId, testAccount.getId(), testAccount.getAccountType(),
                    stockCode, startDate, endDate, periodType))
                    .willReturn(mockKisResult);

            // When
            StockChartResponse result = stockService.getStockChartData(stockCode, startDate, endDate, periodType,
                    userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPeriodType()).isEqualTo(periodType);
            assertThat(result.getStartDate()).isEqualTo(startDate);
            assertThat(result.getEndDate()).isEqualTo(endDate);

            // 차트 데이터 검증
            assertThat(result.getChartData()).hasSize(1);
            StockChartResponse.ChartDataPoint chartData = result.getChartData().get(0);
            assertThat(chartData.getTradingDate()).isEqualTo("20241215");
            assertThat(chartData.getOpenPrice()).isEqualTo("70000");
            assertThat(chartData.getHighPrice()).isEqualTo("72000");
            assertThat(chartData.getLowPrice()).isEqualTo("69000");
            assertThat(chartData.getClosePrice()).isEqualTo("71000");

            // 요약 정보 검증
            StockChartResponse.StockSummary summary = result.getSummary();
            assertThat(summary).isNotNull();
            assertThat(summary.getCurrentPrice()).isEqualTo("71000");
            assertThat(summary.getPriceChange()).isEqualTo("1000");
            assertThat(summary.getChangeRate()).isEqualTo("1.43");

            // Mock 호출 검증 - StockRepository 호출 제거됨
            then(accountRepository).should().findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true);
            then(kisApiComponent).should()
                    .ensureUserCredentials(userId, testAccount.getId(), testAccount.getAccountType(), testCredentials);
            then(kisApiComponent).should().getStockChartData(userId, testAccount.getId(), testAccount.getAccountType(),
                    stockCode, startDate, endDate, periodType);
        }
    }


    @Test
    @DisplayName("활성화된 계좌 없음으로 차트 조회 실패")
    void getStockChartData_Fail_NoActiveAccount() {
        // Given
        String stockCode = "005930";
        String startDate = "20240101";
        String endDate = "20241231";
        Long userId = 1L;

        given(accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stockService.getStockChartData(
                stockCode, startDate, endDate, "D", userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("활성화된 계좌를 찾을 수 없습니다");

        // Mock 호출 검증
        then(accountRepository).should().findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true);
        then(kisApiComponent).should(never())
                .getStockChartData(anyLong(), anyLong(), any(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("KIS API 호출 실패로 차트 조회 실패")
    void getStockChartData_Fail_KisApiError() {
        // Given
        String stockCode = "005930";
        String startDate = "20240101";
        String endDate = "20241231";
        String periodType = "D";
        Long userId = 1L;

        given(accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true))
                .willReturn(Optional.of(testAccount));

        try (MockedStatic<AccountEncryptionUtil> mockedUtil = mockStatic(AccountEncryptionUtil.class)) {
            mockedUtil.when(() -> AccountEncryptionUtil.decryptAccountCredentials(testAccount, userId))
                    .thenReturn(testCredentials);

            willDoNothing().given(kisApiComponent)
                    .ensureUserCredentials(userId, testAccount.getId(), testAccount.getAccountType(), testCredentials);
            given(kisApiComponent.getStockChartData(userId, testAccount.getId(), testAccount.getAccountType(),
                    stockCode, startDate, endDate, periodType))
                    .willThrow(new RuntimeException("KIS API 차트 데이터 조회 실패: Connection timeout"));

            // When & Then
            assertThatThrownBy(() -> stockService.getStockChartData(
                    stockCode, startDate, endDate, periodType, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("KIS API 연동에 실패했습니다");

            // Mock 호출 검증
            then(accountRepository).should().findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true);
            then(kisApiComponent).should()
                    .ensureUserCredentials(userId, testAccount.getId(), testAccount.getAccountType(), testCredentials);
            then(kisApiComponent).should().getStockChartData(userId, testAccount.getId(), testAccount.getAccountType(),
                    stockCode, startDate, endDate, periodType);
        }
    }

    @Test
    @DisplayName("계좌 복호화 실패로 차트 조회 실패")
    void getStockChartData_Fail_DecryptionError() {
        // Given
        String stockCode = "005930";
        String startDate = "20240101";
        String endDate = "20241231";
        String periodType = "D";
        Long userId = 1L;

        given(accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true))
                .willReturn(Optional.of(testAccount));

        try (MockedStatic<AccountEncryptionUtil> mockedUtil = mockStatic(AccountEncryptionUtil.class)) {
            mockedUtil.when(() -> AccountEncryptionUtil.decryptAccountCredentials(testAccount, userId))
                    .thenThrow(new RuntimeException("계좌 정보 복호화 실패"));

            // When & Then
            assertThatThrownBy(() -> stockService.getStockChartData(
                    stockCode, startDate, endDate, periodType, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("계좌 정보 복호화에 실패했습니다");

            // Mock 호출 검증
            then(accountRepository).should().findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true);
            then(kisApiComponent).should(never()).ensureUserCredentials(anyLong(), anyLong(), any(), any());
            then(kisApiComponent).should(never())
                    .getStockChartData(anyLong(), anyLong(), any(), anyString(), anyString(), anyString(), anyString());
        }
    }

    @Test
    @DisplayName("빈 KIS API 응답에 대한 차트 데이터 처리")
    void getStockChartData_Success_EmptyKisResponse() {
        // Given
        String stockCode = "005930";
        String startDate = "20240101";
        String endDate = "20241231";
        String periodType = "D";
        Long userId = 1L;

        InquireDailyItemchartpriceResult emptyKisResult = mock(InquireDailyItemchartpriceResult.class);

        given(accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true))
                .willReturn(Optional.of(testAccount));

        try (MockedStatic<AccountEncryptionUtil> mockedUtil = mockStatic(AccountEncryptionUtil.class)) {
            mockedUtil.when(() -> AccountEncryptionUtil.decryptAccountCredentials(testAccount, userId))
                    .thenReturn(testCredentials);

            willDoNothing().given(kisApiComponent)
                    .ensureUserCredentials(userId, testAccount.getId(), testAccount.getAccountType(), testCredentials);
            given(kisApiComponent.getStockChartData(userId, testAccount.getId(), testAccount.getAccountType(),
                    stockCode, startDate, endDate, periodType))
                    .willReturn(emptyKisResult);

            // When
            StockChartResponse result = stockService.getStockChartData(stockCode, startDate, endDate, periodType,
                    userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPeriodType()).isEqualTo(periodType);
            assertThat(result.getChartData()).isEmpty(); // 빈 차트 데이터
            assertThat(result.getSummary()).isNull();    // null 요약 정보
        }
    }

    @Test
    @DisplayName("기간 타입별 설명 검증")
    void getStockChartData_Success_PeriodDescriptions() {
        // Given
        String stockCode = "005930";
        String startDate = "20240101";
        String endDate = "20241231";
        Long userId = 1L;

        given(accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true))
                .willReturn(Optional.of(testAccount));

        try (MockedStatic<AccountEncryptionUtil> mockedUtil = mockStatic(AccountEncryptionUtil.class)) {
            mockedUtil.when(() -> AccountEncryptionUtil.decryptAccountCredentials(testAccount, userId))
                    .thenReturn(testCredentials);

            willDoNothing().given(kisApiComponent)
                    .ensureUserCredentials(userId, testAccount.getId(), testAccount.getAccountType(), testCredentials);
            given(kisApiComponent.getStockChartData(anyLong(), anyLong(), any(), anyString(), anyString(), anyString(),
                    anyString()))
                    .willReturn(mockKisResult);

            // When & Then for each period type
            Map<String, String> periodDescriptions = Map.of(
                    "D", "일봉",
                    "W", "주봉",
                    "M", "월봉",
                    "Y", "년봉"
            );

            periodDescriptions.forEach((periodType, expectedDescription) -> {
                StockChartResponse result = stockService.getStockChartData(stockCode, startDate, endDate, periodType,
                        userId);
                assertThat(result.getPeriodDescription()).isEqualTo(expectedDescription);
            });
        }
    }
}