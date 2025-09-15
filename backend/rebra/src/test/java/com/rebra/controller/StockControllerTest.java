package com.rebra.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.entity.User;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.service.StockService;
import com.rebra.service.StockTradingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    @DisplayName("주식 매수 성공")
    @WithMockUser
    void buyStock_Success() throws Exception {
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

        given(stockTradingService.buyStock(eq(stockCode), any(StockTradeRequest.class), any(User.class)))
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

        given(stockTradingService.sellStock(eq(stockCode), any(StockTradeRequest.class), any(User.class)))
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
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", 10, 70000L, 999L);

        given(stockTradingService.buyStock(eq(stockCode), any(StockTradeRequest.class), any(User.class)))
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
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", 5, 72000L, 1L);

        given(stockTradingService.sellStock(eq(stockCode), any(StockTradeRequest.class), any(User.class)))
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
        // Given
        String stockCode = "005930";
        StockTradeRequest request = createTradeRequest("00", 5, 72000L, 1L);

        StockTradeResponse errorResponse = StockTradeResponse.error("주문 처리 중 알 수 없는 오류가 발생했습니다");

        given(stockTradingService.sellStock(eq(stockCode), any(StockTradeRequest.class), any(User.class)))
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

    private StockTradeRequest createTradeRequest(String orderType, Integer quantity, Long price, Long accountId) {
        StockTradeRequest request = new StockTradeRequest();
        setField(request, "orderType", orderType);
        setField(request, "quantity", quantity);
        setField(request, "price", price);
        setField(request, "accountId", accountId);
        return request;
    }
}