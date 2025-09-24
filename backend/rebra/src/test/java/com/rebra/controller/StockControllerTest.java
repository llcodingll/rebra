package com.rebra.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.config.resolver.LoginUserArgumentResolver;
import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.dto.response.StockHoldingListResponse;
import com.rebra.dto.response.StockHoldingResponse;
import com.rebra.dto.response.StockHoldingDetailResponse;
import com.rebra.common.PageResponse;
import com.rebra.common.PageInfo;
import com.rebra.enums.ExecutionType;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.exception.account.AccountException;
import com.rebra.exception.stock.StockException;
import com.rebra.service.StockService;
import com.rebra.service.StockTradingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StockService stockService;

    @MockitoBean
    private StockTradingService stockTradingService;

    @MockitoBean
    private LoginUserArgumentResolver loginUserArgumentResolver;

    @Test
    @DisplayName("주식 매수 성공")
    @WithMockUser
    void buyStock_Success() throws Exception {
        // Mock LoginUserArgumentResolver to return userId 1L
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", 10, 70000L, 1L);

        StockTradeResponse response = StockTradeResponse.success(
                "KRX20240101000001",
                "ORDER20240101000001",
                "09:00:00",
                stockCode,
                "00",
                10,
                70000L,
                "02"
        );

        given(stockTradingService.buyStock(eq(stockCode), any(StockTradeRequest.class), any(Long.class), eq(ExecutionType.BUY_PERSONAL)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/stocks/{stockCode}/buy", stockCode)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.exchangeOrderNumber").value("KRX20240101000001"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORDER20240101000001"))
                .andExpect(jsonPath("$.data.stockCode").value(stockCode))
                .andExpect(jsonPath("$.data.orderType").value("00"))
                .andExpect(jsonPath("$.data.quantity").value(10))
                .andExpect(jsonPath("$.data.price").value(70000))
                .andExpect(jsonPath("$.data.tradeType").value("02"));
    }

    @Test
    @DisplayName("주식 매도 성공")
    @WithMockUser
    void sellStock_Success() throws Exception {
        // Mock LoginUserArgumentResolver to return userId 1L
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", 5, 72000L, 1L);

        StockTradeResponse response = StockTradeResponse.success(
                "KRX20240101000002",
                "ORDER20240101000002",
                "09:01:00",
                stockCode,
                "00",
                5,
                72000L,
                "01"
        );

        given(stockTradingService.sellStock(eq(stockCode), any(StockTradeRequest.class), any(Long.class), eq(ExecutionType.SELL_PERSONAL)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/stocks/{stockCode}/sell", stockCode)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.exchangeOrderNumber").value("KRX20240101000002"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORDER20240101000002"))
                .andExpect(jsonPath("$.data.stockCode").value(stockCode))
                .andExpect(jsonPath("$.data.orderType").value("00"))
                .andExpect(jsonPath("$.data.quantity").value(5))
                .andExpect(jsonPath("$.data.price").value(72000))
                .andExpect(jsonPath("$.data.tradeType").value("01"));
    }

    @Test
    @DisplayName("주식 매수 실패 - 잘못된 요청 (주문수량 음수)")
    @WithMockUser
    void buyStock_InvalidQuantity_Fails() throws Exception {
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", -1, 70000L, 1L);

        // When & Then
        mockMvc.perform(post("/api/stocks/{stockCode}/buy", stockCode)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주식 매수 실패 - 주문구분 누락")
    @WithMockUser
    void buyStock_MissingOrderType_Fails() throws Exception {
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest(null, 10, 70000L, 1L);

        // When & Then
        mockMvc.perform(post("/api/stocks/{stockCode}/buy", stockCode)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주식 매수 실패 - 계좌 없음")
    @WithMockUser
    void buyStock_AccountNotFound_Fails() throws Exception {
        // Mock LoginUserArgumentResolver to return userId 1L
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", 10, 70000L, 999L);

        given(stockTradingService.buyStock(eq(stockCode), any(StockTradeRequest.class), any(Long.class), eq(ExecutionType.BUY_PERSONAL)))
                .willThrow(new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

        // When & Then
        mockMvc.perform(post("/api/stocks/{stockCode}/buy", stockCode)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorMessage").value("계좌를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("주식 매도 실패 - KIS API 오류")
    @WithMockUser
    void sellStock_KisApiError_Fails() throws Exception {
        // Mock LoginUserArgumentResolver to return userId 1L
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", 5, 72000L, 1L);

        given(stockTradingService.sellStock(eq(stockCode), any(StockTradeRequest.class), any(Long.class), eq(ExecutionType.SELL_PERSONAL)))
                .willThrow(new CustomRuntimeException(ExceptionCode.KIS_API_ERROR));

        // When & Then
        mockMvc.perform(post("/api/stocks/{stockCode}/sell", stockCode)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.errorMessage").value("KIS API 호출 중 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("주식 매수 실패 - 주문 수량 0")
    @WithMockUser
    void buyStock_ZeroQuantity_Fails() throws Exception {
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", 0, 70000L, 1L);

        // When & Then
        mockMvc.perform(post("/api/stocks/{stockCode}/buy", stockCode)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주식 매도 실패 - 계좌 ID 누락")
    @WithMockUser
    void sellStock_MissingAccountId_Fails() throws Exception {
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", 5, 72000L, null);

        // When & Then
        mockMvc.perform(post("/api/stocks/{stockCode}/sell", stockCode)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주식 매도 실패 - 주문 처리 실패")
    @WithMockUser
    void sellStock_OrderFailed_Fails() throws Exception {
        // Mock LoginUserArgumentResolver to return userId 1L
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", 5, 72000L, 1L);

        StockTradeResponse errorResponse = StockTradeResponse.error("주문 처리 중 알 수 없는 오류가 발생했습니다");

        given(stockTradingService.sellStock(eq(stockCode), any(StockTradeRequest.class), any(Long.class), eq(ExecutionType.SELL_PERSONAL)))
                .willReturn(errorResponse);

        // When & Then
        mockMvc.perform(post("/api/stocks/{stockCode}/sell", stockCode)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.data.errorMessage").value("주문 처리 중 알 수 없는 오류가 발생했습니다"));
    }

    @Nested
    @DisplayName("보유 종목 조회 API 테스트")
    class GetHoldingStocksTest {

        @Test
        @DisplayName("보유 종목 조회 성공")
        @WithMockUser
        void getHoldingStocks_Success() throws Exception {
            // Given
            Long accountId = 1L;

            StockHoldingResponse holding1 = StockHoldingResponse.builder()
                    .stockCode("005930")
                    .stockName("삼성전자")
                    .currentPrice(new BigDecimal("70000"))
                    .averagePurchasePrice(new BigDecimal("65000"))
                    .purchaseAmount(new BigDecimal("650000"))
                    .evaluationAmount(new BigDecimal("700000"))
                    .evaluationProfitLoss(new BigDecimal("50000"))
                    .returnRate(new BigDecimal("7.69"))
                    .holdingQuantity(10)
                    .orderableQuantity(10)
                    .priceChange(new BigDecimal("1000"))
                    .changeRate(new BigDecimal("1.45"))
                    .build();

            StockHoldingResponse holding2 = StockHoldingResponse.builder()
                    .stockCode("000660")
                    .stockName("SK하이닉스")
                    .currentPrice(new BigDecimal("120000"))
                    .averagePurchasePrice(new BigDecimal("110000"))
                    .purchaseAmount(new BigDecimal("550000"))
                    .evaluationAmount(new BigDecimal("600000"))
                    .evaluationProfitLoss(new BigDecimal("50000"))
                    .returnRate(new BigDecimal("9.09"))
                    .holdingQuantity(5)
                    .orderableQuantity(5)
                    .priceChange(new BigDecimal("-2000"))
                    .changeRate(new BigDecimal("-1.64"))
                    .build();

            StockHoldingListResponse listResponse = StockHoldingListResponse.of(Arrays.asList(holding1, holding2));
            PageInfo pageInfo = new PageInfo(0, 5, 2L, 1, true, true);
            PageResponse<StockHoldingListResponse> response = PageResponse.success("조회 성공", listResponse, pageInfo);

            given(stockService.getHoldingStocks(eq(accountId), any())).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/stocks/holdings")
                            .param("accountId", accountId.toString())
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.content.holdings").isArray())
                    .andExpect(jsonPath("$.data.content.holdings", hasSize(2)))
                    .andExpect(jsonPath("$.data.content.holdings[0].stockCode").value("005930"))
                    .andExpect(jsonPath("$.data.content.holdings[0].stockName").value("삼성전자"))
                    .andExpect(jsonPath("$.data.content.holdings[0].currentPrice").value(70000))
                    .andExpect(jsonPath("$.data.content.holdings[1].stockCode").value("000660"))
                    .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                    .andExpect(jsonPath("$.data.pageInfo.size").value(5))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2))
                    .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1));
        }

        @Test
        @DisplayName("보유 종목 조회 실패 - 계좌 없음")
        @WithMockUser
        void getHoldingStocks_AccountNotFound() throws Exception {
            // Given
            Long accountId = 999L;

            given(stockService.getHoldingStocks(eq(accountId), any()))
                    .willThrow(new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/stocks/holdings")
                            .param("accountId", accountId.toString())
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorMessage").value("계좌를 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("보유 종목 조회 실패 - 보유 종목 조회 실패")
        @WithMockUser
        void getHoldingStocks_FetchFailed() throws Exception {
            // Given
            Long accountId = 1L;

            given(stockService.getHoldingStocks(eq(accountId), any()))
                    .willThrow(StockException.stockHoldingFetchFailed());

            // When & Then
            mockMvc.perform(get("/api/stocks/holdings")
                            .param("accountId", accountId.toString())
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.errorMessage").value("보유 종목 조회에 실패했습니다."));
        }

        @Test
        @DisplayName("보유 종목 조회 성공 - 빈 결과")
        @WithMockUser
        void getHoldingStocks_EmptyResult() throws Exception {
            // Given
            Long accountId = 1L;

            StockHoldingListResponse listResponse = StockHoldingListResponse.of(Collections.emptyList());
            PageInfo pageInfo = new PageInfo(0, 5, 0L, 0, true, true);
            PageResponse<StockHoldingListResponse> response = PageResponse.success("조회 성공", listResponse, pageInfo);

            given(stockService.getHoldingStocks(eq(accountId), any())).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/stocks/holdings")
                            .param("accountId", accountId.toString())
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.content.holdings").isArray())
                    .andExpect(jsonPath("$.data.content.holdings", hasSize(0)))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("특정 종목 보유 정보 조회 API 테스트")
    class GetStockHoldingTest {

        @Test
        @DisplayName("특정 종목 보유 정보 조회 성공")
        @WithMockUser
        void getStockHolding_Success() throws Exception {
            // Given
            String stockCode = "005930";
            Long accountId = 1L;

            StockHoldingDetailResponse response = StockHoldingDetailResponse.builder()
                    .averagePurchasePrice(new BigDecimal("65000"))
                    .purchaseAmount(new BigDecimal("650000"))
                    .holdingQuantity(10)
                    .orderableQuantity(10)
                    .build();

            given(stockService.getStockHolding(eq(stockCode), eq(accountId))).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/stocks/{stockCode}/holding", stockCode)
                            .param("accountId", accountId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.averagePurchasePrice").value(65000))
                    .andExpect(jsonPath("$.data.purchaseAmount").value(650000))
                    .andExpect(jsonPath("$.data.holdingQuantity").value(10))
                    .andExpect(jsonPath("$.data.orderableQuantity").value(10));
        }

        @Test
        @DisplayName("특정 종목 보유 정보 조회 성공 - 보유하지 않는 종목")
        @WithMockUser
        void getStockHolding_NotHolding() throws Exception {
            // Given
            String stockCode = "035720";
            Long accountId = 1L;

            given(stockService.getStockHolding(eq(stockCode), eq(accountId))).willReturn(null);

            // When & Then
            mockMvc.perform(get("/api/stocks/{stockCode}/holding", stockCode)
                            .param("accountId", accountId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("특정 종목 보유 정보 조회 실패 - 계좌 없음")
        @WithMockUser
        void getStockHolding_AccountNotFound() throws Exception {
            // Given
            String stockCode = "005930";
            Long accountId = 999L;

            given(stockService.getStockHolding(eq(stockCode), eq(accountId)))
                    .willThrow(new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/stocks/{stockCode}/holding", stockCode)
                            .param("accountId", accountId.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorMessage").value("계좌를 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("특정 종목 보유 정보 조회 실패 - 종목 보유 정보 조회 실패")
        @WithMockUser
        void getStockHolding_FetchFailed() throws Exception {
            // Given
            String stockCode = "005930";
            Long accountId = 1L;

            given(stockService.getStockHolding(eq(stockCode), eq(accountId)))
                    .willThrow(StockException.stockHoldingDetailFetchFailed());

            // When & Then
            mockMvc.perform(get("/api/stocks/{stockCode}/holding", stockCode)
                            .param("accountId", accountId.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.errorMessage").value("종목 보유 정보 조회에 실패했습니다."));
        }
    }

    private StockTradeRequest createTradeRequest(String orderType, Integer quantity, Long price, Long accountId) {
        StockTradeRequest request = new StockTradeRequest();
        setField(request, "orderType", orderType);
        setField(request, "quantity", quantity);
        setField(request, "price", price);
        setField(request, "accountId", accountId);
        return request;
    }
}