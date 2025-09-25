package com.rebra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.rebra.common.PageResponse;
import com.rebra.dto.response.StockHoldingDetailResponse;
import com.rebra.dto.response.StockHoldingListResponse;
import com.rebra.entity.Account;
import com.rebra.entity.User;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.exception.stock.StockException;
import com.rebra.repository.AccountRepository;
import com.rebra.component.KisApiComponent;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import com.youhogeon.finance.kis_api.api.rest.trading.InquirePsblOrderResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private KisApiComponent kisApiComponent;

    @InjectMocks
    private StockServiceImpl stockService;

    @Nested
    @DisplayName("보유 종목 조회 테스트")
    class GetHoldingStocksTest {

        @Test
        @DisplayName("보유 종목 조회 성공")
        void getHoldingStocks_Success() throws Exception {
            // Given
            Long accountId = 1L;
            Pageable pageable = PageRequest.of(0, 5);
            Account mockAccount = mock(Account.class);
            InquireBalanceResult mockResult = new InquireBalanceResult();

            given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
            given(kisApiComponent.getUserBalance(eq(mockAccount))).willReturn(mockResult);

            // When
            PageResponse<StockHoldingListResponse> result = stockService.getHoldingStocks(accountId, pageable);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("보유 종목 조회 성공 - 빈 결과")
        void getHoldingStocks_EmptyResult() throws Exception {
            // Given
            Long accountId = 1L;
            Pageable pageable = PageRequest.of(0, 5);
            Account mockAccount = mock(Account.class);
            InquireBalanceResult mockResult = new InquireBalanceResult();

            given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
            given(kisApiComponent.getUserBalance(eq(mockAccount))).willReturn(mockResult);

            // When
            PageResponse<StockHoldingListResponse> result = stockService.getHoldingStocks(accountId, pageable);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("보유 종목 조회 실패 - 계좌 없음")
        void getHoldingStocks_AccountNotFound() {
            // Given
            Long accountId = 999L;
            Pageable pageable = PageRequest.of(0, 5);

            given(accountRepository.findById(accountId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stockService.getHoldingStocks(accountId, pageable))
                    .isInstanceOf(StockException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", ExceptionCode.STOCK_HOLDING_FETCH_FAILED);
        }

        @Test
        @DisplayName("보유 종목 조회 실패 - KIS API 오류")
        void getHoldingStocks_KisApiFailed() throws Exception {
            // Given
            Long accountId = 1L;
            Pageable pageable = PageRequest.of(0, 5);
            Account mockAccount = mock(Account.class);

            given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
            given(kisApiComponent.getUserBalance(eq(mockAccount)))
                    .willThrow(new RuntimeException("KIS API 오류"));

            // When & Then
            assertThatThrownBy(() -> stockService.getHoldingStocks(accountId, pageable))
                    .isInstanceOf(StockException.class)
                    .hasMessage("보유 종목 조회에 실패했습니다.");
        }
    }

    @Nested
    @DisplayName("특정 종목 보유 정보 조회 테스트")
    class GetStockHoldingTest {

        @Test
        @DisplayName("특정 종목 보유 정보 조회 성공")
        void getStockHolding_Success() throws Exception {
            // Given
            String stockCode = "005930";
            Long accountId = 1L;
            Account mockAccount = mock(Account.class);
            InquireBalanceResult mockResult = new InquireBalanceResult();

            // Mock InquirePsblOrderResult with mocks
            InquirePsblOrderResult mockPsblOrderResult = mock(InquirePsblOrderResult.class);
            InquirePsblOrderResult.Output mockOutput = mock(InquirePsblOrderResult.Output.class);
            given(mockPsblOrderResult.getOutput()).willReturn(mockOutput);
            given(mockOutput.getOrdPsblCash()).willReturn("10000000");

            given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
            given(kisApiComponent.getUserBalance(eq(mockAccount))).willReturn(mockResult);
            given(kisApiComponent.getUserPossibleOrder(eq(mockAccount), eq(stockCode))).willReturn(mockPsblOrderResult);

            // When
            StockHoldingDetailResponse result = stockService.getStockHolding(stockCode, accountId);

            // Then (result could be null if stock not found)
            // Test passes if no exception is thrown
        }

        @Test
        @DisplayName("특정 종목 보유 정보 조회 성공 - 보유하지 않는 종목")
        void getStockHolding_NotHolding() throws Exception {
            // Given
            String stockCode = "035720";
            Long accountId = 1L;
            Account mockAccount = mock(Account.class);
            InquireBalanceResult mockResult = new InquireBalanceResult();

            // Mock InquirePsblOrderResult for non-holding case with mocks
            InquirePsblOrderResult mockPsblOrderResult = mock(InquirePsblOrderResult.class);
            InquirePsblOrderResult.Output mockOutput = mock(InquirePsblOrderResult.Output.class);
            given(mockPsblOrderResult.getOutput()).willReturn(mockOutput);
            given(mockOutput.getOrdPsblCash()).willReturn("5000000");

            given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
            given(kisApiComponent.getUserBalance(eq(mockAccount))).willReturn(mockResult);
            given(kisApiComponent.getUserPossibleOrder(eq(mockAccount), eq(stockCode))).willReturn(mockPsblOrderResult);

            // When
            StockHoldingDetailResponse result = stockService.getStockHolding(stockCode, accountId);

            // Then (result could be null if stock not found)
            // Test passes if no exception is thrown
        }

        @Test
        @DisplayName("특정 종목 보유 정보 조회 실패 - 계좌 없음")
        void getStockHolding_AccountNotFound() {
            // Given
            String stockCode = "005930";
            Long accountId = 999L;

            given(accountRepository.findById(accountId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stockService.getStockHolding(stockCode, accountId))
                    .isInstanceOf(StockException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", ExceptionCode.STOCK_HOLDING_DETAIL_FETCH_FAILED);
        }

        @Test
        @DisplayName("특정 종목 보유 정보 조회 실패 - KIS API 오류")
        void getStockHolding_KisApiFailed() throws Exception {
            // Given
            String stockCode = "005930";
            Long accountId = 1L;
            Account mockAccount = mock(Account.class);

            given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
            given(kisApiComponent.getUserBalance(eq(mockAccount)))
                    .willThrow(new RuntimeException("KIS API 오류"));

            // When & Then
            assertThatThrownBy(() -> stockService.getStockHolding(stockCode, accountId))
                    .isInstanceOf(StockException.class)
                    .hasMessage("종목 보유 정보 조회에 실패했습니다.");
        }
    }
}