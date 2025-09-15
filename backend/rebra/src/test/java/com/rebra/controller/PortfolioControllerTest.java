package com.rebra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.config.SecurityTestConfig;
import com.rebra.dto.request.AutoRebalancingUpdateRequest;
import com.rebra.dto.request.PortfolioBasicUpdateRequest;
import com.rebra.dto.request.PortfolioCreateRequest;
import com.rebra.dto.request.PortfolioRebalancingSettingsRequest;
import com.rebra.dto.response.PortfolioCreateResponse;
import com.rebra.dto.response.PortfolioDetailResponse;
import com.rebra.dto.response.PortfolioListResponse;
import com.rebra.dto.response.PortfolioUpdateResponse;
import com.rebra.entity.RebalancingPeriod;
import com.rebra.exception.portfolio.PortfolioException;
import com.rebra.exception.user.UserException;
import com.rebra.service.PortfolioService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(PortfolioController.class)
@Import(SecurityTestConfig.class)
@DisplayName("PortfolioController 테스트")
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/v1/portfolios - 포트폴리오 목록 조회")
    class GetPortfolioList {

        @Test
        @DisplayName("성공 - 포트폴리오 목록 반환")
        void getPortfolioList_성공() throws Exception {
            // Given
            PortfolioListResponse response = PortfolioListResponse.of(Collections.emptyList());

            given(portfolioService.getPortfolioList(anyLong())).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.totalCount").value(0));
        }

        @Test
        @DisplayName("실패 - 사용자를 찾을 수 없음")
        void getPortfolioList_실패_사용자없음() throws Exception {
            // Given
            given(portfolioService.getPortfolioList(anyLong()))
                    .willThrow(UserException.userNotFound());

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/portfolios - 포트폴리오 생성")
    class CreatePortfolio {

        @Test
        @DisplayName("성공 - 포트폴리오 생성")
        void createPortfolio_성공() throws Exception {
            // Given
            PortfolioCreateRequest request = new PortfolioCreateRequest();
            request.setName("테스트 포트폴리오");
            request.setDescription("테스트 설명");
            request.setAccountId(1L);

            // Mock portfolio entity for response creation
            PortfolioCreateResponse response = PortfolioCreateResponse.builder()
                    .id(1L)
                    .name("테스트 포트폴리오")
                    .description("테스트 설명")
                    .account(PortfolioCreateResponse.AccountInfo.of(1L, "123-45-678901", "한국투자증권"))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            given(portfolioService.createPortfolio(anyLong(), any(PortfolioCreateRequest.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/portfolios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.name").value("테스트 포트폴리오"));
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 데이터")
        void createPortfolio_실패_잘못된요청() throws Exception {
            // Given - 빈 이름과 null 계좌 ID
            PortfolioCreateRequest invalidRequest = new PortfolioCreateRequest();
            invalidRequest.setName("");
            invalidRequest.setDescription("설명");
            invalidRequest.setAccountId(null);

            given(portfolioService.createPortfolio(anyLong(), any(PortfolioCreateRequest.class)))
                    .willThrow(new IllegalArgumentException("잘못된 요청 데이터"));

            // When & Then
            mockMvc.perform(post("/api/v1/portfolios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 계좌를 찾을 수 없음")
        void createPortfolio_실패_계좌없음() throws Exception {
            // Given
            PortfolioCreateRequest request = new PortfolioCreateRequest();
            request.setName("테스트 포트폴리오");
            request.setDescription("테스트 설명");
            request.setAccountId(999L);

            given(portfolioService.createPortfolio(anyLong(), any(PortfolioCreateRequest.class)))
                    .willThrow(PortfolioException.portfolioCreationFailed());

            // When & Then
            mockMvc.perform(post("/api/v1/portfolios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/portfolios/{portfolioId} - 포트폴리오 상세 조회")
    class GetPortfolioDetail {

        @Test
        @DisplayName("성공 - 포트폴리오 상세 정보 반환")
        void getPortfolioDetail_성공() throws Exception {
            // Given
            Long portfolioId = 1L;
            // Create mock portfolio info with nested structure
            PortfolioDetailResponse.PortfolioInfo portfolioInfo = PortfolioDetailResponse.PortfolioInfo.builder()
                    .id(portfolioId)
                    .name("테스트 포트폴리오")
                    .description("테스트 설명")
                    .account(PortfolioDetailResponse.AccountInfo.builder()
                            .id(1L)
                            .accountNumber("123-45-678901")
                            .brokerName("한국투자증권")
                            .build())
                    .autoRebalance(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            PortfolioDetailResponse response = PortfolioDetailResponse.builder()
                    .portfolio(portfolioInfo)
                    .registeredStocks(Collections.emptyList())
                    .unregisteredStocks(Collections.emptyList())
                    .build();

            given(portfolioService.getPortfolioDetail(anyLong(), eq(portfolioId)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.portfolio.id").value(portfolioId))
                    .andExpect(jsonPath("$.data.portfolio.name").value("테스트 포트폴리오"));
        }

        @Test
        @DisplayName("실패 - 포트폴리오를 찾을 수 없음")
        void getPortfolioDetail_실패_포트폴리오없음() throws Exception {
            // Given
            Long portfolioId = 999L;

            given(portfolioService.getPortfolioDetail(anyLong(), eq(portfolioId)))
                    .willThrow(PortfolioException.portfolioNotFound());

            // When & Then
            mockMvc.perform(get("/api/v1/portfolios/{portfolioId}", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/portfolios/{portfolioId}/info - 포트폴리오 기본 정보 수정")
    class UpdateBasicInfo {

        @Test
        @DisplayName("성공 - 기본 정보 수정")
        void updateBasicInfo_성공() throws Exception {
            // Given
            Long portfolioId = 1L;
            PortfolioBasicUpdateRequest request = new PortfolioBasicUpdateRequest();
            request.setName("수정된 포트폴리오");
            request.setDescription("수정된 설명");

            PortfolioUpdateResponse response = PortfolioUpdateResponse.of(
                    portfolioId, "수정된 포트폴리오", "수정된 설명", LocalDateTime.now());

            given(portfolioService.updateBasicInfo(anyLong(), eq(portfolioId), any(PortfolioBasicUpdateRequest.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/portfolios/{portfolioId}/info", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.portfolioId").value(portfolioId))
                    .andExpect(jsonPath("$.data.name").value("수정된 포트폴리오"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/portfolios/{portfolioId}/auto-rebalancing - 자동 리밸런싱 설정")
    class UpdateAutoRebalancing {

        @Test
        @DisplayName("성공 - 자동 리밸런싱 활성화")
        void updateAutoRebalancing_성공_활성화() throws Exception {
            // Given
            Long portfolioId = 1L;
            AutoRebalancingUpdateRequest request = new AutoRebalancingUpdateRequest();
            request.setAutoRebalancing(true);

            PortfolioUpdateResponse response = PortfolioUpdateResponse.success(
                    portfolioId, "자동 리밸런싱이 활성화되었습니다.");

            given(portfolioService.updateAutoRebalancing(anyLong(), eq(portfolioId), eq(true)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/portfolios/{portfolioId}/auto-rebalancing", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.portfolioId").value(portfolioId))
                    .andExpect(jsonPath("$.data.message").value("자동 리밸런싱이 활성화되었습니다."));
        }

        @Test
        @DisplayName("성공 - 자동 리밸런싱 비활성화")
        void updateAutoRebalancing_성공_비활성화() throws Exception {
            // Given
            Long portfolioId = 1L;
            AutoRebalancingUpdateRequest request = new AutoRebalancingUpdateRequest();
            request.setAutoRebalancing(false);

            PortfolioUpdateResponse response = PortfolioUpdateResponse.success(
                    portfolioId, "자동 리밸런싱이 비활성화되었습니다.");

            given(portfolioService.updateAutoRebalancing(anyLong(), eq(portfolioId), eq(false)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/portfolios/{portfolioId}/auto-rebalancing", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.portfolioId").value(portfolioId))
                    .andExpect(jsonPath("$.data.message").value("자동 리밸런싱이 비활성화되었습니다."));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/portfolios/{portfolioId}/rebalancing-schedule - 리밸런싱 주기 설정")
    class UpdateRebalancingSettings {

        @Test
        @DisplayName("성공 - 리밸런싱 설정 변경")
        void updateRebalancingSettings_성공() throws Exception {
            // Given
            Long portfolioId = 1L;
            PortfolioRebalancingSettingsRequest request = new PortfolioRebalancingSettingsRequest();
            request.setRebalancingStartDate(LocalDate.now().plusDays(1));
            request.setRebalancingPeriod(RebalancingPeriod.MONTHLY);
            request.setRebalancingInterval(3);

            PortfolioUpdateResponse response = PortfolioUpdateResponse.success(
                    portfolioId, "리밸런싱 설정이 성공적으로 변경되었습니다.");

            given(portfolioService.updateRebalancingSettings(anyLong(), eq(portfolioId), any(PortfolioRebalancingSettingsRequest.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/portfolios/{portfolioId}/rebalancing-schedule", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.portfolioId").value(portfolioId));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/portfolios/{portfolioId} - 포트폴리오 삭제")
    class DeletePortfolio {

        @Test
        @DisplayName("성공 - 포트폴리오 삭제")
        void deletePortfolio_성공() throws Exception {
            // Given
            Long portfolioId = 1L;

            willDoNothing().given(portfolioService).deletePortfolio(anyLong(), eq(portfolioId));

            // When & Then
            mockMvc.perform(delete("/api/v1/portfolios/{portfolioId}", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200));
        }

        @Test
        @DisplayName("실패 - 포트폴리오를 찾을 수 없음")
        void deletePortfolio_실패_포트폴리오없음() throws Exception {
            // Given
            Long portfolioId = 999L;

            willThrow(PortfolioException.portfolioNotFound())
                    .given(portfolioService).deletePortfolio(anyLong(), eq(portfolioId));

            // When & Then
            mockMvc.perform(delete("/api/v1/portfolios/{portfolioId}", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 삭제 권한 없음 (다른 사용자의 포트폴리오)")
        void deletePortfolio_실패_권한없음() throws Exception {
            // Given
            Long portfolioId = 1L;

            willThrow(PortfolioException.portfolioNotFound())
                    .given(portfolioService).deletePortfolio(anyLong(), eq(portfolioId));

            // When & Then
            mockMvc.perform(delete("/api/v1/portfolios/{portfolioId}", portfolioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}