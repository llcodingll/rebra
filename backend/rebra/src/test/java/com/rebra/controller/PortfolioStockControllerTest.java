package com.rebra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.config.SecurityTestConfig;
import com.rebra.dto.request.PortfolioStockRegisterRequest;
import com.rebra.dto.request.PortfolioStockDeleteRequest;
import com.rebra.dto.request.PortfolioStockBatchUpdateRequest;
import com.rebra.dto.request.PortfolioStockUpdateRequest;
import com.rebra.dto.response.PortfolioStockResponse;
import com.rebra.exception.portfolio.PortfolioException;
import com.rebra.exception.portfoliostock.PortfolioStockException;
import com.rebra.service.PortfolioStockService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioStockController.class)
@Import(SecurityTestConfig.class)
@DisplayName("PortfolioStockController 테스트")
class PortfolioStockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioStockService portfolioStockService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("포트폴리오 주식 등록 API 테스트")
    class RegisterStockTest {

        @Test
        @DisplayName("성공 - 주식 등록 성공")
        void registerStock_성공() throws Exception {
            // Given
            Long portfolioId = 1L;
            PortfolioStockRegisterRequest request = new PortfolioStockRegisterRequest("005930");
            PortfolioStockResponse response = PortfolioStockResponse.builder()
                    .stockCode("005930")
                    .stockName(null)
                    .targetWeight(null)
                    .thresholdPercentage(null)
                    .status(null)
                    .build();

            given(portfolioStockService.registerStock(eq(1L), eq(portfolioId), any(PortfolioStockRegisterRequest.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/portfolios/{portfolioId}/stocks", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.stockCode").value("005930"))
                    .andExpect(jsonPath("$.data.stockName").doesNotExist())
                    .andExpect(jsonPath("$.data.targetWeight").doesNotExist())
                    .andExpect(jsonPath("$.data.thresholdPercentage").doesNotExist())
                    .andExpect(jsonPath("$.data.status").doesNotExist());

            verify(portfolioStockService).registerStock(eq(1L), eq(portfolioId), any(PortfolioStockRegisterRequest.class));
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 데이터 (주식코드 누락)")
        void registerStock_실패_주식코드누락() throws Exception {
            // Given
            Long portfolioId = 1L;
            PortfolioStockRegisterRequest invalidRequest = new PortfolioStockRegisterRequest("");

            // When & Then
            mockMvc.perform(post("/api/v1/portfolios/{portfolioId}/stocks", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(portfolioStockService);
        }

        @Test
        @DisplayName("실패 - 포트폴리오를 찾을 수 없음")
        void registerStock_실패_포트폴리오없음() throws Exception {
            // Given
            Long portfolioId = 999L;
            PortfolioStockRegisterRequest request = new PortfolioStockRegisterRequest("005930");

            given(portfolioStockService.registerStock(eq(1L), eq(portfolioId), any(PortfolioStockRegisterRequest.class)))
                    .willThrow(PortfolioException.portfolioNotFound());

            // When & Then
            mockMvc.perform(post("/api/v1/portfolios/{portfolioId}/stocks", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(portfolioStockService).registerStock(eq(1L), eq(portfolioId), any(PortfolioStockRegisterRequest.class));
        }

        @Test
        @DisplayName("실패 - 이미 등록된 주식")
        void registerStock_실패_중복등록() throws Exception {
            // Given
            Long portfolioId = 1L;
            PortfolioStockRegisterRequest request = new PortfolioStockRegisterRequest("005930");

            given(portfolioStockService.registerStock(eq(1L), eq(portfolioId), any(PortfolioStockRegisterRequest.class)))
                    .willThrow(PortfolioStockException.portfolioStockAlreadyExists());

            // When & Then
            mockMvc.perform(post("/api/v1/portfolios/{portfolioId}/stocks", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            verify(portfolioStockService).registerStock(eq(1L), eq(portfolioId), any(PortfolioStockRegisterRequest.class));
        }
    }

    @Nested
    @DisplayName("포트폴리오 주식 삭제 API 테스트")
    class DeleteStockTest {

        @Test
        @DisplayName("성공 - 주식 삭제 성공")
        void deleteStock_성공() throws Exception {
            // Given
            Long portfolioId = 1L;
            PortfolioStockDeleteRequest request = new PortfolioStockDeleteRequest("005930");

            willDoNothing().given(portfolioStockService).deleteStock(eq(1L), eq(portfolioId), any(PortfolioStockDeleteRequest.class));

            // When & Then
            mockMvc.perform(delete("/api/v1/portfolios/{portfolioId}/stocks", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").doesNotExist());

            verify(portfolioStockService).deleteStock(eq(1L), eq(portfolioId), any(PortfolioStockDeleteRequest.class));
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 데이터 (주식코드 누락)")
        void deleteStock_실패_주식코드누락() throws Exception {
            // Given
            Long portfolioId = 1L;
            PortfolioStockDeleteRequest invalidRequest = new PortfolioStockDeleteRequest("");

            // When & Then
            mockMvc.perform(delete("/api/v1/portfolios/{portfolioId}/stocks", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(portfolioStockService);
        }

        @Test
        @DisplayName("실패 - 포트폴리오를 찾을 수 없음")
        void deleteStock_실패_포트폴리오없음() throws Exception {
            // Given
            Long portfolioId = 999L;
            PortfolioStockDeleteRequest request = new PortfolioStockDeleteRequest("005930");

            willThrow(PortfolioException.portfolioNotFound())
                    .given(portfolioStockService).deleteStock(eq(1L), eq(portfolioId), any(PortfolioStockDeleteRequest.class));

            // When & Then
            mockMvc.perform(delete("/api/v1/portfolios/{portfolioId}/stocks", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(portfolioStockService).deleteStock(eq(1L), eq(portfolioId), any(PortfolioStockDeleteRequest.class));
        }

        @Test
        @DisplayName("실패 - 포트폴리오에 등록되지 않은 주식")
        void deleteStock_실패_주식없음() throws Exception {
            // Given
            Long portfolioId = 1L;
            PortfolioStockDeleteRequest request = new PortfolioStockDeleteRequest("999999");

            willThrow(PortfolioStockException.portfolioStockNotFound())
                    .given(portfolioStockService).deleteStock(eq(1L), eq(portfolioId), any(PortfolioStockDeleteRequest.class));

            // When & Then
            mockMvc.perform(delete("/api/v1/portfolios/{portfolioId}/stocks", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(portfolioStockService).deleteStock(eq(1L), eq(portfolioId), any(PortfolioStockDeleteRequest.class));
        }
    }

    @Nested
    @DisplayName("포트폴리오 주식 일괄 업데이트 API 테스트")
    class UpdateStocksBatchTest {

        @Test
        @DisplayName("성공 - 주식 설정 일괄 업데이트 성공")
        void updateStocksBatch_성공() throws Exception {
            // Given
            Long portfolioId = 1L;
            List<PortfolioStockUpdateRequest> updateRequests = Arrays.asList(
                new PortfolioStockUpdateRequest("005930", 35.0, 5.0),
                new PortfolioStockUpdateRequest("000660", 25.0, 3.0)
            );
            PortfolioStockBatchUpdateRequest request = new PortfolioStockBatchUpdateRequest(updateRequests);

            List<PortfolioStockResponse> responses = Arrays.asList(
                PortfolioStockResponse.builder()
                        .stockCode("005930")
                        .stockName(null)
                        .targetWeight(35.0)
                        .thresholdPercentage(5.0)
                        .status(null)
                        .build(),
                PortfolioStockResponse.builder()
                        .stockCode("000660")
                        .stockName(null)
                        .targetWeight(25.0)
                        .thresholdPercentage(3.0)
                        .status(null)
                        .build()
            );

            given(portfolioStockService.updateStocksBatch(eq(1L), eq(portfolioId), any(PortfolioStockBatchUpdateRequest.class)))
                    .willReturn(responses);

            // When & Then
            mockMvc.perform(put("/api/v1/portfolios/{portfolioId}/stocks/batch", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].stockCode").value("005930"))
                    .andExpect(jsonPath("$.data[0].targetWeight").value(35.0))
                    .andExpect(jsonPath("$.data[0].thresholdPercentage").value(5.0))
                    .andExpect(jsonPath("$.data[1].stockCode").value("000660"))
                    .andExpect(jsonPath("$.data[1].targetWeight").value(25.0))
                    .andExpect(jsonPath("$.data[1].thresholdPercentage").value(3.0));

            verify(portfolioStockService).updateStocksBatch(eq(1L), eq(portfolioId), any(PortfolioStockBatchUpdateRequest.class));
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 데이터 (빈 주식 목록)")
        void updateStocksBatch_실패_빈목록() throws Exception {
            // Given
            Long portfolioId = 1L;
            PortfolioStockBatchUpdateRequest invalidRequest = new PortfolioStockBatchUpdateRequest(Arrays.asList());

            // When & Then
            mockMvc.perform(put("/api/v1/portfolios/{portfolioId}/stocks/batch", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(portfolioStockService);
        }

        @Test
        @DisplayName("실패 - 포트폴리오를 찾을 수 없음")
        void updateStocksBatch_실패_포트폴리오없음() throws Exception {
            // Given
            Long portfolioId = 999L;
            List<PortfolioStockUpdateRequest> updateRequests = Arrays.asList(
                new PortfolioStockUpdateRequest("005930", 35.0, 5.0)
            );
            PortfolioStockBatchUpdateRequest request = new PortfolioStockBatchUpdateRequest(updateRequests);

            given(portfolioStockService.updateStocksBatch(eq(1L), eq(portfolioId), any(PortfolioStockBatchUpdateRequest.class)))
                    .willThrow(PortfolioException.portfolioNotFound());

            // When & Then
            mockMvc.perform(put("/api/v1/portfolios/{portfolioId}/stocks/batch", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(portfolioStockService).updateStocksBatch(eq(1L), eq(portfolioId), any(PortfolioStockBatchUpdateRequest.class));
        }

        @Test
        @DisplayName("실패 - 포트폴리오에 등록되지 않은 주식")
        void updateStocksBatch_실패_주식없음() throws Exception {
            // Given
            Long portfolioId = 1L;
            List<PortfolioStockUpdateRequest> updateRequests = Arrays.asList(
                new PortfolioStockUpdateRequest("999999", 35.0, 5.0)
            );
            PortfolioStockBatchUpdateRequest request = new PortfolioStockBatchUpdateRequest(updateRequests);

            given(portfolioStockService.updateStocksBatch(eq(1L), eq(portfolioId), any(PortfolioStockBatchUpdateRequest.class)))
                    .willThrow(PortfolioStockException.portfolioStockNotFound());

            // When & Then
            mockMvc.perform(put("/api/v1/portfolios/{portfolioId}/stocks/batch", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(portfolioStockService).updateStocksBatch(eq(1L), eq(portfolioId), any(PortfolioStockBatchUpdateRequest.class));
        }
    }
}