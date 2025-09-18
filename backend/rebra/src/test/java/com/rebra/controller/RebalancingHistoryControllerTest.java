package com.rebra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.common.PageInfo;
import com.rebra.common.PageResponse;
import com.rebra.config.SecurityTestConfig;
import com.rebra.dto.response.RebalancingHistoryDetailResponse;
import com.rebra.dto.response.RebalancingHistoryGraphResponse;
import com.rebra.dto.response.RebalancingHistoryListResponse;
import com.rebra.dto.response.RebalancingHistoryResponse;
import com.rebra.dto.response.RebalancingHistorySummaryResponse;
import com.rebra.dto.response.TradeDetailResponse;
import com.rebra.enums.ExecutionType;
import com.rebra.enums.TransactionStatus;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.service.RebalancingHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RebalancingHistoryController.class)
@Import(SecurityTestConfig.class)
@DisplayName("RebalancingHistoryController 테스트")
class RebalancingHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RebalancingHistoryService rebalancingHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/v1/portfolios/{portfolioId}/rebalancing-history - 페이지네이션 조회")
    class GetRebalancingHistoryWithPagination {

        @Test
        @DisplayName("성공 - 정상적인 페이지네이션 응답 반환")
        void getRebalancingHistoryWithPagination_성공() throws Exception {
            // Given
            Long portfolioId = 1L;

            RebalancingHistoryResponse historyResponse = RebalancingHistoryResponse.builder()
                    .orderId(1L)
                    .executionType(ExecutionType.AUTO)
                    .totalStocks(3)
                    .totalBuyAmount(new BigDecimal("1500000"))
                    .totalSellAmount(new BigDecimal("800000"))
                    .status(TransactionStatus.COMPLETED)
                    .executedAt(LocalDateTime.now())
                    .build();

            RebalancingHistorySummaryResponse summaryResponse = RebalancingHistorySummaryResponse.of(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 9, 17),
                    5,
                    new BigDecimal("5000000"),
                    new BigDecimal("3000000"),
                    3,
                    2);

            RebalancingHistoryListResponse content = RebalancingHistoryListResponse.of(
                    Collections.singletonList(historyResponse),
                    summaryResponse);

            PageInfo pageInfo = new PageInfo(0, 20, 1L, 1, true, true);

            PageResponse<RebalancingHistoryListResponse> response = PageResponse.success(
                    "리밸런싱 히스토리 조회 성공", content, pageInfo);

            given(rebalancingHistoryService.getRebalancingHistoryWithPagination(eq(portfolioId), any()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/rebalancing-history", portfolioId)
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.success").value(true))
                    .andExpect(jsonPath("$.data.content.histories").isArray())
                    .andExpect(jsonPath("$.data.content.histories[0].orderId").value(1L))
                    .andExpect(jsonPath("$.data.content.histories[0].executionType").value("AUTO"))
                    .andExpect(jsonPath("$.data.content.histories[0].totalStocks").value(3))
                    .andExpect(jsonPath("$.data.content.summary.totalRebalances").value(5))
                    .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));
        }

        @Test
        @DisplayName("성공 - 빈 목록 반환 (히스토리 없음)")
        void getRebalancingHistoryWithPagination_성공_빈목록() throws Exception {
            // Given
            Long portfolioId = 1L;

            RebalancingHistorySummaryResponse summaryResponse = RebalancingHistorySummaryResponse.of(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 9, 17),
                    0,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    0);

            RebalancingHistoryListResponse content = RebalancingHistoryListResponse.of(
                    Collections.emptyList(),
                    summaryResponse);

            PageInfo pageInfo = new PageInfo(0, 20, 0L, 0, true, true);

            PageResponse<RebalancingHistoryListResponse> response = PageResponse.success(
                    "리밸런싱 히스토리 조회 성공", content, pageInfo);

            given(rebalancingHistoryService.getRebalancingHistoryWithPagination(eq(portfolioId), any()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/rebalancing-history", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.histories").isEmpty())
                    .andExpect(jsonPath("$.data.content.summary.totalRebalances").value(0))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value(0));
        }

        @Test
        @DisplayName("실패 - 포트폴리오 없음 (404)")
        void getRebalancingHistoryWithPagination_실패_포트폴리오없음() throws Exception {
            // Given
            Long portfolioId = 999L;

            given(rebalancingHistoryService.getRebalancingHistoryWithPagination(eq(portfolioId), any()))
                    .willThrow(new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/rebalancing-history", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("성공 - 쿼리 파라미터 기본값 처리")
        void getRebalancingHistoryWithPagination_성공_기본값() throws Exception {
            // Given
            Long portfolioId = 1L;

            RebalancingHistoryListResponse content = RebalancingHistoryListResponse.of(
                    Collections.emptyList(),
                    RebalancingHistorySummaryResponse.of(
                            LocalDate.now(), LocalDate.now(), 0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0));

            PageInfo pageInfo = new PageInfo(0, 20, 0L, 0, true, true);

            PageResponse<RebalancingHistoryListResponse> response = PageResponse.success(
                    "리밸런싱 히스토리 조회 성공", content, pageInfo);

            given(rebalancingHistoryService.getRebalancingHistoryWithPagination(eq(portfolioId), any()))
                    .willReturn(response);

            // When & Then - 파라미터 없이 요청하면 기본값 적용
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/rebalancing-history", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/portfolios/{portfolioId}/rebalancing-history/all - 그래프용 전체 조회")
    class GetAllRebalancingHistory {

        @Test
        @DisplayName("성공 - 더미 데이터 포함 리스트 반환")
        void getAllRebalancingHistory_성공() throws Exception {
            // Given
            Long portfolioId = 1L;

            List<RebalancingHistoryGraphResponse> response = Arrays.asList(
                    // 더미 데이터 (시작점)
                    RebalancingHistoryGraphResponse.builder()
                            .orderId(null)
                            .executedAt(LocalDateTime.of(2025, 1, 1, 9, 0))
                            .cumulativeReturn(new BigDecimal("100.00"))
                            .build(),
                    // 실제 데이터
                    RebalancingHistoryGraphResponse.builder()
                            .orderId(1L)
                            .executedAt(LocalDateTime.of(2025, 2, 15, 14, 30))
                            .cumulativeReturn(new BigDecimal("105.25"))
                            .build(),
                    RebalancingHistoryGraphResponse.builder()
                            .orderId(2L)
                            .executedAt(LocalDateTime.of(2025, 3, 15, 10, 15))
                            .cumulativeReturn(new BigDecimal("108.50"))
                            .build()
            );

            given(rebalancingHistoryService.getAllRebalancingHistory(eq(portfolioId)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/rebalancing-history/all", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    // 더미 데이터 검증
                    .andExpect(jsonPath("$.data[0].orderId").isEmpty())
                    .andExpect(jsonPath("$.data[0].cumulativeReturn").value(100.00))
                    // 실제 데이터 검증
                    .andExpect(jsonPath("$.data[1].orderId").value(1L))
                    .andExpect(jsonPath("$.data[1].cumulativeReturn").value(105.25))
                    .andExpect(jsonPath("$.data[2].orderId").value(2L))
                    .andExpect(jsonPath("$.data[2].cumulativeReturn").value(108.50))
                    // executionType 필드가 없는지 확인
                    .andExpect(jsonPath("$.data[0].executionType").doesNotExist())
                    .andExpect(jsonPath("$.data[1].executionType").doesNotExist());
        }

        @Test
        @DisplayName("성공 - 더미 데이터만 있는 경우 (히스토리 없음)")
        void getAllRebalancingHistory_성공_더미데이터만() throws Exception {
            // Given
            Long portfolioId = 1L;

            List<RebalancingHistoryGraphResponse> response = Collections.singletonList(
                    RebalancingHistoryGraphResponse.builder()
                            .orderId(null)
                            .executedAt(LocalDateTime.of(2025, 1, 1, 9, 0))
                            .cumulativeReturn(new BigDecimal("100.00"))
                            .build()
            );

            given(rebalancingHistoryService.getAllRebalancingHistory(eq(portfolioId)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/rebalancing-history/all", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].orderId").isEmpty())
                    .andExpect(jsonPath("$.data[0].cumulativeReturn").value(100.00));
        }

        @Test
        @DisplayName("실패 - 포트폴리오 없음 (404)")
        void getAllRebalancingHistory_실패_포트폴리오없음() throws Exception {
            // Given
            Long portfolioId = 999L;

            given(rebalancingHistoryService.getAllRebalancingHistory(eq(portfolioId)))
                    .willThrow(new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/rebalancing-history/all", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/portfolios/{portfolioId}/rebalancing-history/{orderId} - 상세 조회")
    class GetRebalancingHistoryDetail {

        @Test
        @DisplayName("성공 - 거래 내역 포함 상세 정보 반환")
        void getRebalancingHistoryDetail_성공() throws Exception {
            // Given
            Long portfolioId = 1L;
            Long orderId = 123L;

            List<TradeDetailResponse> trades = Arrays.asList(
                    TradeDetailResponse.builder()
                            .tradeId(789L)
                            .stockCode("005930")
                            .stockName("삼성전자")
                            .tradeType("SELL")
                            .executedShares(5)
                            .price(new BigDecimal("78000.00"))
                            .fee(new BigDecimal("1170.00"))
                            .profitAmount(new BigDecimal("15000.00"))
                            .profitRate(new BigDecimal("4.17"))
                            .reason("목표 비중 초과로 인한 매도")
                            .tradeDate(LocalDateTime.of(2025, 2, 15, 14, 30, 15))
                            .build(),
                    TradeDetailResponse.builder()
                            .tradeId(790L)
                            .stockCode("000660")
                            .stockName("SK하이닉스")
                            .tradeType("BUY")
                            .executedShares(8)
                            .price(new BigDecimal("95000.00"))
                            .fee(new BigDecimal("2280.00"))
                            .profitAmount(BigDecimal.ZERO)
                            .profitRate(BigDecimal.ZERO)
                            .reason("목표 비중 미달로 인한 매수")
                            .tradeDate(LocalDateTime.of(2025, 2, 15, 14, 30, 30))
                            .build()
            );

            RebalancingHistoryDetailResponse response = RebalancingHistoryDetailResponse.builder()
                    .orderId(orderId)
                    .executionType(ExecutionType.AUTO)
                    .executedAt(LocalDateTime.of(2025, 2, 15, 14, 30))
                    .cumulativeReturn(new BigDecimal("105.25"))
                    .trades(trades)
                    .build();

            given(rebalancingHistoryService.getRebalancingHistoryDetail(eq(portfolioId), eq(orderId)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/rebalancing-history/{orderId}",
                            portfolioId, orderId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.orderId").value(orderId))
                    .andExpect(jsonPath("$.data.executionType").value("AUTO"))
                    .andExpect(jsonPath("$.data.cumulativeReturn").value(105.25))
                    .andExpect(jsonPath("$.data.trades").isArray())
                    .andExpect(jsonPath("$.data.trades.length()").value(2))
                    .andExpect(jsonPath("$.data.trades[0].tradeId").value(789L))
                    .andExpect(jsonPath("$.data.trades[0].stockCode").value("005930"))
                    .andExpect(jsonPath("$.data.trades[0].stockName").value("삼성전자"))
                    .andExpect(jsonPath("$.data.trades[0].tradeType").value("SELL"))
                    .andExpect(jsonPath("$.data.trades[1].tradeId").value(790L))
                    .andExpect(jsonPath("$.data.trades[1].stockCode").value("000660"))
                    .andExpect(jsonPath("$.data.trades[1].stockName").value("SK하이닉스"))
                    .andExpect(jsonPath("$.data.trades[1].tradeType").value("BUY"));
        }

        @Test
        @DisplayName("실패 - 리밸런싱 주문 없음 (404)")
        void getRebalancingHistoryDetail_실패_주문없음() throws Exception {
            // Given
            Long portfolioId = 1L;
            Long orderId = 999L;

            given(rebalancingHistoryService.getRebalancingHistoryDetail(eq(portfolioId), eq(orderId)))
                    .willThrow(new CustomRuntimeException(ExceptionCode.REBALANCING_ORDER_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/rebalancing-history/{orderId}",
                            portfolioId, orderId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 포트폴리오 없음 (404)")
        void getRebalancingHistoryDetail_실패_포트폴리오없음() throws Exception {
            // Given
            Long portfolioId = 999L;
            Long orderId = 1L;

            given(rebalancingHistoryService.getRebalancingHistoryDetail(eq(portfolioId), eq(orderId)))
                    .willThrow(new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}/rebalancing-history/{orderId}",
                            portfolioId, orderId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}