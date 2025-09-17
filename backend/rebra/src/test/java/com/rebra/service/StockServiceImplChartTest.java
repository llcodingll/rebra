package com.rebra.service;

import static com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult.Output1;
import static com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult.Output2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.mockStatic;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockChartResponse.ChartDataPoint;
import com.rebra.dto.response.StockChartResponse.StockSummary;
import com.rebra.entity.Account;
import com.rebra.entity.AccountType;
import com.rebra.entity.Stock;
import com.rebra.entity.User;
import com.rebra.repository.AccountRepository;
import com.rebra.util.AccountEncryptionUtil;
import java.util.HashMap;
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
    private AccountRepository accountRepository;


    @Mock
    private KisApiComponent kisApiComponent;

    @InjectMocks
    private StockServiceImpl stockService;

    private Stock testStock;
    private User testUser;
    private Account testAccount;
    private DecryptedAccountCredentials testCredentials;

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
    }

    private Map<String, Object> createMockKisApiResponse() {
        Map<String, Object> result = new HashMap<>();

        // output1 (종목 요약 정보) - 실제 KIS API 응답과 동일한 구조로 변경
        Output1 mockOutput1 = mock(Output1.class);
        given(mockOutput1.getStckPrpr()).willReturn("71000");         // 현재가
        given(mockOutput1.getPrdyVrss()).willReturn("1000");          // 전일대비
        given(mockOutput1.getPrdyCtrt()).willReturn("1.43");          // 전일대비율
        given(mockOutput1.getPrdyVrssSign()).willReturn("2");        // 전일대비부호
        given(mockOutput1.getAcmlVol()).willReturn("15000000");       // 누적거래량
        given(mockOutput1.getHtsAvls()).willReturn("425000000000000"); // HTS 시가총액
        given(mockOutput1.getPer()).willReturn("12.5");                // PER
        given(mockOutput1.getPbr()).willReturn("0.8");                 // PBR
        // 새로 추가된 필드들 Mock 설정
        given(mockOutput1.getStckPrdyClpr()).willReturn("70000");      // 전일 종가
        given(mockOutput1.getStckMxpr()).willReturn("91000");          // 상한가
        given(mockOutput1.getStckLlam()).willReturn("49000");          // 하한가
        given(mockOutput1.getAskp()).willReturn("71100");              // 매도호가
        given(mockOutput1.getBidp()).willReturn("70900");              // 매수호가
        given(mockOutput1.getEps()).willReturn("5680");                // EPS
        given(mockOutput1.getLstnStcn()).willReturn("5969782550");     // 상장주수
        given(mockOutput1.getCpfn()).willReturn("778047");             // 자본금
        result.put("output1", mockOutput1);

        // output2 (차트 데이터) - 실제 KIS API 응답과 동일한 구조로 변경
        // Mock InquireDailyItemchartpriceResult.Output2 객체 생성
        Output2 mockOutput2 = mock(Output2.class);
        given(mockOutput2.getStckBsopDate()).willReturn("20241215");     // 주식 영업일자
        given(mockOutput2.getStckOprc()).willReturn("70000");            // 주식 시가
        given(mockOutput2.getStckHgpr()).willReturn("72000");            // 주식 최고가
        given(mockOutput2.getStckLwpr()).willReturn("69000");            // 주식 최저가
        given(mockOutput2.getStckClpr()).willReturn("71000");            // 주식 종가
        given(mockOutput2.getAcmlVol()).willReturn("15000000");          // 누적 거래량
        given(mockOutput2.getAcmlTrPbmn()).willReturn("1065000000000"); // 누적 거래대금
        given(mockOutput2.getPrdyVrss()).willReturn("1000");             // 전일 대비
        given(mockOutput2.getPrdyVrssSign()).willReturn("2");           // 전일 대비 부호
        // Output2에는 changeRate(prdyCtrt)가 없음 - Output1에만 존재

        // KIS API에서 반환되는 것은 배열이므로 배열 형태로 모킹
        Output2[] output2 = new Output2[]{
                mockOutput2};
        result.put("output2", output2);

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

        // 성공 테스트에서만 Mock KIS API 응답 생성
        Map<String, Object> mockKisResult = createMockKisApiResponse();

        given(accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true))
                .willReturn(Optional.of(testAccount));

        try (MockedStatic<AccountEncryptionUtil> mockedUtil = mockStatic(AccountEncryptionUtil.class)) {
            mockedUtil.when(() -> AccountEncryptionUtil.decryptAccountCredentials(testAccount, userId))
                    .thenReturn(testCredentials);

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
            ChartDataPoint chartData = result.getChartData().get(0);
            assertThat(chartData.getTradingDate()).isEqualTo("20241215");
            assertThat(chartData.getOpenPrice()).isEqualTo("70000");
            assertThat(chartData.getHighPrice()).isEqualTo("72000");
            assertThat(chartData.getLowPrice()).isEqualTo("69000");
            assertThat(chartData.getClosePrice()).isEqualTo("71000");

            // 요약 정보 검증
            StockSummary summary = result.getSummary();
            assertThat(summary).isNotNull();
            assertThat(summary.getCurrentPrice()).isEqualTo("71000");
            assertThat(summary.getPriceChange()).isEqualTo("1000");
            assertThat(summary.getChangeRate()).isEqualTo("1.43");

            // 새로 추가된 필드들 검증
            assertThat(summary.getPreviousClosePrice()).isEqualTo("70000");
            assertThat(summary.getUpperLimit()).isEqualTo("91000");
            assertThat(summary.getLowerLimit()).isEqualTo("49000");
            assertThat(summary.getAskPrice()).isEqualTo("71100");
            assertThat(summary.getBidPrice()).isEqualTo("70900");
            assertThat(summary.getEps()).isEqualTo("5680");
            assertThat(summary.getListedShares()).isEqualTo("5969782550");
            assertThat(summary.getCapital()).isEqualTo("778047");

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
        // KIS API는 계좌를 찾지 못하면 호출되지 않음
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
            // KIS API는 복호화 실패 시 호출되지 않음
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

        Map<String, Object> emptyKisResult = new HashMap<>();

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

            // Mock 호출 검증
            then(accountRepository).should().findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true);
            then(kisApiComponent).should()
                    .ensureUserCredentials(userId, testAccount.getId(), testAccount.getAccountType(), testCredentials);
            then(kisApiComponent).should().getStockChartData(userId, testAccount.getId(), testAccount.getAccountType(),
                    stockCode, startDate, endDate, periodType);
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

        // 성공 테스트에서만 Mock KIS API 응답 생성
        Map<String, Object> mockKisResult = createMockKisApiResponse();

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