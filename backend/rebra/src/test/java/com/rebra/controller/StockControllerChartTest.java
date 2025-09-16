package com.rebra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.common.CommonApiResponse;
import com.rebra.config.resolver.LoginUserArgumentResolver;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.exception.stock.StockException;
import com.rebra.service.StockService;
import com.rebra.service.StockTradingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@DisplayName("StockController 차트 API 테스트")
class StockControllerChartTest {

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

    private StockChartResponse createMockChartResponse(String periodType) {
        List<StockChartResponse.ChartDataPoint> chartData = new ArrayList<>();
        chartData.add(StockChartResponse.ChartDataPoint.builder()
                .tradingDate("20241215")
                .openPrice("70000")
                .highPrice("72000")
                .lowPrice("69000")
                .closePrice("71000")
                .volume("15000000")
                .tradingValue("1065000000000")
                .priceChange("1000")
                .changeSign("2")
                .changeRate("1.43")
                .build());

        StockChartResponse.StockSummary summary = StockChartResponse.StockSummary.builder()
                .currentPrice("71000")
                .priceChange("1000")
                .changeRate("1.43")
                .changeSign("2")
                .volume("15000000")
                .marketCap("425000000000000")
                .per("12.5")
                .pbr("0.8")
                .build();

        return StockChartResponse.builder()
                .stockCode("005930")
                .stockName("삼성전자")
                .periodType(periodType)
                .periodDescription(StockChartResponse.PeriodType.fromCode(periodType).getDescription())
                .startDate("20240101")
                .endDate("20241231")
                .chartData(chartData)
                .summary(summary)
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("일봉 차트 데이터 조회 성공")
    void getDailyChart_Success() throws Exception {
        // Mock LoginUserArgumentResolver to return userId 1L
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);

        // Given
        String stockCode = "005930";
        StockChartResponse mockResponse = createMockChartResponse("D");
        when(stockService.getStockChartData(eq(stockCode), eq("20240101"), eq("20241231"), eq("D"), eq(1L)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stocks/{stockCode}/chart/daily", stockCode)
                        .param("startDate", "20240101")
                        .param("endDate", "20241231")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.stockCode").value("005930"))
                .andExpect(jsonPath("$.data.stockName").value("삼성전자"))
                .andExpect(jsonPath("$.data.periodType").value("D"))
                .andExpect(jsonPath("$.data.periodDescription").value("일봉"))
                .andExpect(jsonPath("$.data.chartData").isArray())
                .andExpect(jsonPath("$.data.chartData[0].tradingDate").value("20241215"))
                .andExpect(jsonPath("$.data.chartData[0].openPrice").value("70000"))
                .andExpect(jsonPath("$.data.chartData[0].closePrice").value("71000"))
                .andExpect(jsonPath("$.data.summary.currentPrice").value("71000"));

        verify(stockService, times(1)).getStockChartData(eq(stockCode), eq("20240101"), eq("20241231"), eq("D"), eq(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("존재하지 않는 종목코드로 차트 조회 - StockException")
    void getChart_StockNotFound() throws Exception {
        // Mock LoginUserArgumentResolver to return userId 1L
        when(loginUserArgumentResolver.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(loginUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);

        // Given
        String nonExistentStockCode = "999999";
        when(stockService.getStockChartData(eq(nonExistentStockCode), anyString(), anyString(), anyString(), eq(1L)))
                .thenThrow(StockException.stockCodeNotFound());

        // When & Then
        mockMvc.perform(get("/api/stocks/{stockCode}/chart/daily", nonExistentStockCode)
                        .param("startDate", "20240101")
                        .param("endDate", "20241231")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(stockService, times(1)).getStockChartData(eq(nonExistentStockCode), eq("20240101"), eq("20241231"), eq("D"), eq(1L));
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 차트 조회 요청")
    void getChart_Unauthorized() throws Exception {
        // Given
        String stockCode = "005930";

        // When & Then (인증 없이 요청)
        mockMvc.perform(get("/api/stocks/{stockCode}/chart/daily", stockCode)
                        .param("startDate", "20240101")
                        .param("endDate", "20241231")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // 서비스가 호출되지 않아야 함
        verify(stockService, never()).getStockChartData(anyString(), anyString(), anyString(), anyString(), anyLong());
    }
}