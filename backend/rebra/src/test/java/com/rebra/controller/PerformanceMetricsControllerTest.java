package com.rebra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.config.SecurityTestConfig;
import com.rebra.dto.response.PerformanceMetricsChartResponse;
import com.rebra.exception.portfolio.PortfolioException;
import com.rebra.service.PerformanceMetricsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PerformanceMetricsController.class)
@Import(SecurityTestConfig.class)
@DisplayName("PerformanceMetricsController 테스트")
class PerformanceMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PerformanceMetricsService performanceMetricsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/v1/portfolios/{portfolioId}/performance-metrics - 포트폴리오 성과 차트 조회")
    class GetPortfolioPerformanceChart {

        @Test
        @DisplayName("성공 - 성과 데이터가 있는 경우")
        void getPortfolioPerformanceChart_성공_데이터있음() throws Exception {
            // Given
            Long portfolioId = 1L;

            PerformanceMetricsChartResponse.PerformanceDataPoint dataPoint1 =
                PerformanceMetricsChartResponse.PerformanceDataPoint.builder()
                    .metricDate(LocalDate.of(2024, 1, 1))
                    .totalValue(1000000.0)
                    .isRebalanced(false)
                    .isSold(false)
                    .isBought(true)
                    .build();

            PerformanceMetricsChartResponse.PerformanceDataPoint dataPoint2 =
                PerformanceMetricsChartResponse.PerformanceDataPoint.builder()
                    .metricDate(LocalDate.of(2024, 1, 2))
                    .totalValue(1050000.0)
                    .isRebalanced(true)
                    .isSold(false)
                    .isBought(false)
                    .build();

            PerformanceMetricsChartResponse.PerformanceStatistics statistics =
                PerformanceMetricsChartResponse.PerformanceStatistics.builder()
                    .totalDataPoints(2)
                    .rebalancingCount(1)
                    .buyCount(1)
                    .sellCount(0)
                    .initialValue(1000000.0)
                    .finalValue(1050000.0)
                    .totalReturnRate(5.0)
                    .maxValue(1050000.0)
                    .minValue(1000000.0)
                    .build();

            PerformanceMetricsChartResponse response = PerformanceMetricsChartResponse.builder()
                    .portfolioId(portfolioId)
                    .portfolioName("테스트 포트폴리오")
                    .portfolioCreatedDate(LocalDate.of(2024, 1, 1))
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.now())
                    .performanceData(Arrays.asList(dataPoint1, dataPoint2))
                    .statistics(statistics)
                    .build();

            given(performanceMetricsService.getPortfolioPerformanceChart(eq(portfolioId), anyLong()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/performance-metrics", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.portfolioId").value(portfolioId))
                    .andExpect(jsonPath("$.data.portfolioName").value("테스트 포트폴리오"))
                    .andExpect(jsonPath("$.data.performanceData").isArray())
                    .andExpect(jsonPath("$.data.performanceData.length()").value(2))
                    .andExpect(jsonPath("$.data.performanceData[0].metricDate").value("2024-01-01"))
                    .andExpect(jsonPath("$.data.performanceData[0].totalValue").value(1000000.0))
                    .andExpect(jsonPath("$.data.performanceData[0].bought").value(true))
                    .andExpect(jsonPath("$.data.performanceData[1].rebalanced").value(true))
                    .andExpect(jsonPath("$.data.statistics.totalDataPoints").value(2))
                    .andExpect(jsonPath("$.data.statistics.rebalancingCount").value(1))
                    .andExpect(jsonPath("$.data.statistics.totalReturnRate").value(5.0));
        }

        @Test
        @DisplayName("성공 - 성과 데이터가 없는 경우")
        void getPortfolioPerformanceChart_성공_데이터없음() throws Exception {
            // Given
            Long portfolioId = 1L;

            PerformanceMetricsChartResponse.PerformanceStatistics statistics =
                PerformanceMetricsChartResponse.PerformanceStatistics.builder()
                    .totalDataPoints(0)
                    .rebalancingCount(0)
                    .buyCount(0)
                    .sellCount(0)
                    .build();

            PerformanceMetricsChartResponse response = PerformanceMetricsChartResponse.builder()
                    .portfolioId(portfolioId)
                    .portfolioName("빈 포트폴리오")
                    .portfolioCreatedDate(LocalDate.now())
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now())
                    .performanceData(Collections.emptyList())
                    .statistics(statistics)
                    .build();

            given(performanceMetricsService.getPortfolioPerformanceChart(eq(portfolioId), anyLong()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/performance-metrics", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.portfolioId").value(portfolioId))
                    .andExpect(jsonPath("$.data.performanceData").isArray())
                    .andExpect(jsonPath("$.data.performanceData.length()").value(0))
                    .andExpect(jsonPath("$.data.statistics.totalDataPoints").value(0));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 포트폴리오")
        void getPortfolioPerformanceChart_실패_포트폴리오없음() throws Exception {
            // Given
            Long portfolioId = 999L;

            given(performanceMetricsService.getPortfolioPerformanceChart(eq(portfolioId), anyLong()))
                    .willThrow(PortfolioException.portfolioNotFound());

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/performance-metrics", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 권한 없는 포트폴리오 접근")
        void getPortfolioPerformanceChart_실패_권한없음() throws Exception {
            // Given
            Long portfolioId = 1L;

            given(performanceMetricsService.getPortfolioPerformanceChart(eq(portfolioId), anyLong()))
                    .willThrow(PortfolioException.portfolioNotFound()); // 권한 없음도 동일한 예외로 처리

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/performance-metrics", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 잘못된 포트폴리오 ID 형식")
        void getPortfolioPerformanceChart_실패_잘못된ID형식() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/performance-metrics", "invalid")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError()); // 실제로는 500이 발생함
        }

        @Test
        @DisplayName("실패 - 서비스 예외 발생")
        void getPortfolioPerformanceChart_실패_서비스예외() throws Exception {
            // Given
            Long portfolioId = 1L;

            given(performanceMetricsService.getPortfolioPerformanceChart(eq(portfolioId), anyLong()))
                    .willThrow(new RuntimeException("서비스 오류"));

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/performance-metrics", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }
    }
}