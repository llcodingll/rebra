package com.rebra.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.config.resolver.LoginUserArgumentResolver;
import com.rebra.dto.response.StockDetailResponse;
import com.rebra.dto.response.StockSearchResponse;
import com.rebra.entity.Stock;
import com.rebra.exception.stock.StockException;
import com.rebra.service.StockService;
import com.rebra.service.StockTradingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StockController.class)
@DisplayName("StockController 상세 조회 API 테스트")
class StockControllerDetailTest {

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

    private StockDetailResponse createMockDetailResponse(boolean withHolding) {
        // Mock Stock 정보
        StockDetailResponse.StockInfo stockInfo = StockDetailResponse.StockInfo.builder()
                .id(1L)
                .stockCode("005930")
                .stockName("삼성전자")
                .stockType("STOCK")
                .isActive(true)
                .build();

        // Mock WebSocket 정보
        StockDetailResponse.WebSocketInfo webSocketInfo = StockDetailResponse.WebSocketInfo.builder()
                .priceChannel("/user/queue/stock/price")
                .orderbookChannel("/user/queue/stock/orderbook")
                .endpoint("/ws")
                .build();

        // Mock 보유 정보 (옵션)
        StockDetailResponse.HoldingInfo holdingInfo = null;
        if (withHolding) {
            holdingInfo = StockDetailResponse.HoldingInfo.builder()
                    .holdingQuantity("100")
                    .purchaseAmount("7000000")
                    .averagePrice("70000")
                    .currentValue("7100000")
                    .profitLoss("100000")
                    .profitLossRate("1.43")
                    .build();
        }

        return StockDetailResponse.builder()
                .stock(stockInfo)
                .webSocketInfo(webSocketInfo)
                .holdingInfo(holdingInfo)
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("종목 상세 조회 성공 - 보유 정보 없음")
    void getStockDetail_Success_WithoutHolding() throws Exception {
        // Mock LoginUserArgumentResolver
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);

        // Given
        String stockCode = "005930";
        StockDetailResponse mockResponse = createMockDetailResponse(false);
        when(stockService.getStockDetail(eq(stockCode), eq(false), eq(1L)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stocks/{stockCode}", stockCode)
                        .param("includeHolding", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.stock.stockCode").value("005930"))
                .andExpect(jsonPath("$.data.stock.stockName").value("삼성전자"))
                .andExpect(jsonPath("$.data.webSocketInfo.priceChannel").value("/user/queue/stock/price"))
                .andExpect(jsonPath("$.data.holdingInfo").doesNotExist());
    }

    @Test
    @WithMockUser
    @DisplayName("종목 상세 조회 성공 - 보유 정보 포함")
    void getStockDetail_Success_WithHolding() throws Exception {
        // Mock LoginUserArgumentResolver
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);

        // Given
        String stockCode = "005930";
        StockDetailResponse mockResponse = createMockDetailResponse(true);
        when(stockService.getStockDetail(eq(stockCode), eq(true), eq(1L)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stocks/{stockCode}", stockCode)
                        .param("includeHolding", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.stock.stockCode").value("005930"))
                .andExpect(jsonPath("$.data.holdingInfo.holdingQuantity").value("100"))
                .andExpect(jsonPath("$.data.holdingInfo.purchaseAmount").value("7000000"))
                .andExpect(jsonPath("$.data.holdingInfo.averagePrice").value("70000"))
                .andExpect(jsonPath("$.data.holdingInfo.profitLoss").value("100000"));
    }

    @Test
    @WithMockUser
    @DisplayName("종목 상세 조회 - 기본값 (보유 정보 미포함)")
    void getStockDetail_DefaultParameter() throws Exception {
        // Mock LoginUserArgumentResolver
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);

        // Given
        String stockCode = "005930";
        StockDetailResponse mockResponse = createMockDetailResponse(false);
        when(stockService.getStockDetail(eq(stockCode), eq(false), eq(1L)))
                .thenReturn(mockResponse);

        // When & Then (includeHolding 파라미터 없이 요청)
        mockMvc.perform(get("/api/stocks/{stockCode}", stockCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stock.stockCode").value("005930"))
                .andExpect(jsonPath("$.data.holdingInfo").doesNotExist());
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 종목 상세 조회 요청")
    void getStockDetail_Unauthorized() throws Exception {
        // Given
        String stockCode = "005930";

        // When & Then (인증 없이 요청)
        mockMvc.perform(get("/api/stocks/{stockCode}", stockCode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("존재하지 않는 종목 조회 - 404 에러")
    void getStockDetail_StockNotFound() throws Exception {
        // Mock LoginUserArgumentResolver
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);

        // Given
        String stockCode = "999999";
        when(stockService.getStockDetail(eq(stockCode), eq(false), eq(1L)))
                .thenThrow(new RuntimeException("종목을 찾을 수 없습니다"));

        // When & Then
        mockMvc.perform(get("/api/stocks/{stockCode}", stockCode)
                        .param("includeHolding", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    @DisplayName("KIS API 연동 실패 - 500 에러")
    void getStockDetail_KisApiError() throws Exception {
        // Mock LoginUserArgumentResolver
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);

        // Given
        String stockCode = "005930";
        when(stockService.getStockDetail(eq(stockCode), eq(true), eq(1L)))
                .thenThrow(new RuntimeException("KIS API 연동에 실패했습니다. 잠시 후 다시 시도해주세요."));

        // When & Then
        mockMvc.perform(get("/api/stocks/{stockCode}", stockCode)
                        .param("includeHolding", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    @DisplayName("활성화된 계좌 없음 - 500 에러")
    void getStockDetail_NoActiveAccount() throws Exception {
        // Mock LoginUserArgumentResolver
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);

        // Given
        String stockCode = "005930";
        when(stockService.getStockDetail(eq(stockCode), eq(true), eq(1L)))
                .thenThrow(new RuntimeException("활성화된 계좌를 찾을 수 없습니다. 계좌를 연결해주세요."));

        // When & Then
        mockMvc.perform(get("/api/stocks/{stockCode}", stockCode)
                        .param("includeHolding", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    @DisplayName("보유 정보 조회 실패해도 종목 정보는 성공 - holdingInfo null")
    void getStockDetail_HoldingInfoFailButStockInfoSuccess() throws Exception {
        // Mock LoginUserArgumentResolver
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);

        // Given
        String stockCode = "005930";
        StockDetailResponse mockResponse = createMockDetailResponse(false); // holdingInfo는 null
        when(stockService.getStockDetail(eq(stockCode), eq(true), eq(1L)))
                .thenReturn(mockResponse); // 보유 정보 실패했지만 종목 정보는 성공

        // When & Then
        mockMvc.perform(get("/api/stocks/{stockCode}", stockCode)
                        .param("includeHolding", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stock.stockCode").value("005930"))
                .andExpect(jsonPath("$.data.holdingInfo").doesNotExist()); // 보유 정보는 없음
    }
}