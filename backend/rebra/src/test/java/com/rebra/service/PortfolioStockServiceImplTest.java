package com.rebra.service;

import com.rebra.config.SecurityTestConfig;
import com.rebra.dto.request.PortfolioStockRegisterRequest;
import com.rebra.dto.request.PortfolioStockDeleteRequest;
import com.rebra.dto.request.PortfolioStockBatchUpdateRequest;
import com.rebra.dto.request.PortfolioStockUpdateRequest;
import com.rebra.dto.response.PortfolioStockResponse;
import com.rebra.entity.Portfolio;
import com.rebra.entity.PortfolioStock;
import com.rebra.entity.User;
import com.rebra.exception.portfolio.PortfolioException;
import com.rebra.exception.portfoliostock.PortfolioStockException;
import com.rebra.repository.PerformanceMetricsRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.PortfolioStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioStockService 테스트")
class PortfolioStockServiceImplTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioStockRepository portfolioStockRepository;

    @Mock
    private PerformanceMetricsRepository performanceMetricsRepository;

    @InjectMocks
    private PortfolioStockServiceImpl portfolioStockService;

    @Nested
    @DisplayName("포트폴리오 주식 등록")
    class RegisterStock {

        @Test
        @DisplayName("성공 - 주식 등록 성공")
        void registerStock_성공() {
            // Given
            Long userId = 1L;
            Long portfolioId = 1L;
            String stockCode = "005930";
            PortfolioStockRegisterRequest request = new PortfolioStockRegisterRequest(stockCode);

            User user = User.builder().sub("test").nickname("test").build();
            ReflectionTestUtils.setField(user, "id", userId);

            Portfolio portfolio = Portfolio.builder().user(user).name("테스트 포트폴리오").build();
            ReflectionTestUtils.setField(portfolio, "id", portfolioId);

            PortfolioStock portfolioStock = PortfolioStock.builder()
                    .portfolio(portfolio)
                    .stockCode(stockCode)
                    .targetWeight(null)
                    .thresholdPercentage(null)
                    .status(null)
                    .build();

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId)).willReturn(Optional.of(portfolio));
            given(portfolioStockRepository.existsByPortfolioIdAndStockCode(portfolioId, stockCode)).willReturn(false);
            given(portfolioStockRepository.save(any(PortfolioStock.class))).willReturn(portfolioStock);

            // PerformanceMetrics 관련 Mock 설정
            given(performanceMetricsRepository.existsByPortfolioIdAndMetricDate(any(), any())).willReturn(false);
            given(performanceMetricsRepository.save(any())).willReturn(null);

            // When
            PortfolioStockResponse response = portfolioStockService.registerStock(userId, portfolioId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStockCode()).isEqualTo(stockCode);
            assertThat(response.getStockName()).isNull(); // stockName은 이제 null
            assertThat(response.getTargetWeight()).isNull();
            assertThat(response.getThresholdPercentage()).isNull();
            assertThat(response.getStatus()).isNull();

            verify(portfolioRepository).findByIdAndUserId(portfolioId, userId);
            verify(portfolioStockRepository).existsByPortfolioIdAndStockCode(portfolioId, stockCode);
            verify(portfolioStockRepository).save(any(PortfolioStock.class));
            verify(performanceMetricsRepository).existsByPortfolioIdAndMetricDate(any(), any());
            verify(performanceMetricsRepository).save(any());
        }

        @Test
        @DisplayName("실패 - 포트폴리오를 찾을 수 없음")
        void registerStock_실패_포트폴리오없음() {
            // Given
            Long userId = 1L;
            Long portfolioId = 999L;
            PortfolioStockRegisterRequest request = new PortfolioStockRegisterRequest("005930");

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> portfolioStockService.registerStock(userId, portfolioId, request))
                    .isInstanceOf(PortfolioException.class);

            verify(portfolioRepository).findByIdAndUserId(portfolioId, userId);
        }


        @Test
        @DisplayName("실패 - 이미 등록된 주식")
        void registerStock_실패_중복등록() {
            // Given
            Long userId = 1L;
            Long portfolioId = 1L;
            String stockCode = "005930";
            PortfolioStockRegisterRequest request = new PortfolioStockRegisterRequest(stockCode);

            User user = User.builder().sub("test").nickname("test").build();
            ReflectionTestUtils.setField(user, "id", userId);

            Portfolio portfolio = Portfolio.builder().user(user).name("테스트 포트폴리오").build();
            ReflectionTestUtils.setField(portfolio, "id", portfolioId);

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId)).willReturn(Optional.of(portfolio));
            given(portfolioStockRepository.existsByPortfolioIdAndStockCode(portfolioId, stockCode)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> portfolioStockService.registerStock(userId, portfolioId, request))
                    .isInstanceOf(PortfolioStockException.class);

            verify(portfolioRepository).findByIdAndUserId(portfolioId, userId);
            verify(portfolioStockRepository).existsByPortfolioIdAndStockCode(portfolioId, stockCode);
        }
    }

    @Nested
    @DisplayName("포트폴리오 주식 삭제")
    class DeleteStock {

        @Test
        @DisplayName("성공 - 주식 삭제 성공")
        void deleteStock_성공() {
            // Given
            Long userId = 1L;
            Long portfolioId = 1L;
            String stockCode = "005930";
            PortfolioStockDeleteRequest request = new PortfolioStockDeleteRequest(stockCode);

            User user = User.builder().sub("test").nickname("test").build();
            ReflectionTestUtils.setField(user, "id", userId);

            Portfolio portfolio = Portfolio.builder().user(user).name("테스트 포트폴리오").build();
            ReflectionTestUtils.setField(portfolio, "id", portfolioId);

            PortfolioStock portfolioStock = PortfolioStock.builder()
                    .portfolio(portfolio)
                    .stockCode(stockCode)
                    .build();

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId)).willReturn(Optional.of(portfolio));
            given(portfolioStockRepository.findByPortfolioIdAndStockCode(portfolioId, stockCode)).willReturn(Optional.of(portfolioStock));
            willDoNothing().given(portfolioStockRepository).delete(portfolioStock);

            // PerformanceMetrics 관련 Mock 설정
            given(performanceMetricsRepository.existsByPortfolioIdAndMetricDate(any(), any())).willReturn(false);
            given(performanceMetricsRepository.save(any())).willReturn(null);

            // When
            portfolioStockService.deleteStock(userId, portfolioId, request);

            // Then
            verify(portfolioRepository).findByIdAndUserId(portfolioId, userId);
            verify(portfolioStockRepository).findByPortfolioIdAndStockCode(portfolioId, stockCode);
            verify(portfolioStockRepository).delete(portfolioStock);
            verify(performanceMetricsRepository).existsByPortfolioIdAndMetricDate(any(), any());
            verify(performanceMetricsRepository).save(any());
        }

        @Test
        @DisplayName("실패 - 포트폴리오를 찾을 수 없음")
        void deleteStock_실패_포트폴리오없음() {
            // Given
            Long userId = 1L;
            Long portfolioId = 999L;
            String stockCode = "005930";
            PortfolioStockDeleteRequest request = new PortfolioStockDeleteRequest(stockCode);

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> portfolioStockService.deleteStock(userId, portfolioId, request))
                    .isInstanceOf(PortfolioException.class);

            verify(portfolioRepository).findByIdAndUserId(portfolioId, userId);
        }

        @Test
        @DisplayName("실패 - 포트폴리오 주식을 찾을 수 없음")
        void deleteStock_실패_포트폴리오주식없음() {
            // Given
            Long userId = 1L;
            Long portfolioId = 1L;
            String stockCode = "999999";
            PortfolioStockDeleteRequest request = new PortfolioStockDeleteRequest(stockCode);

            User user = User.builder().sub("test").nickname("test").build();
            Portfolio portfolio = Portfolio.builder().user(user).name("테스트 포트폴리오").build();

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId)).willReturn(Optional.of(portfolio));
            given(portfolioStockRepository.findByPortfolioIdAndStockCode(portfolioId, stockCode)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> portfolioStockService.deleteStock(userId, portfolioId, request))
                    .isInstanceOf(PortfolioStockException.class);

            verify(portfolioRepository).findByIdAndUserId(portfolioId, userId);
            verify(portfolioStockRepository).findByPortfolioIdAndStockCode(portfolioId, stockCode);
        }

    }

    @Nested
    @DisplayName("포트폴리오 주식 일괄 업데이트")
    class UpdateStocksBatch {

        @Test
        @DisplayName("성공 - 주식 설정 일괄 업데이트 성공")
        void updateStocksBatch_성공() {
            // Given
            Long userId = 1L;
            Long portfolioId = 1L;

            User user = User.builder().sub("test").nickname("test").build();
            ReflectionTestUtils.setField(user, "id", userId);

            Portfolio portfolio = Portfolio.builder().user(user).name("테스트 포트폴리오").build();
            ReflectionTestUtils.setField(portfolio, "id", portfolioId);

            // 업데이트 요청 생성
            List<PortfolioStockUpdateRequest> updateRequests = Arrays.asList(
                new PortfolioStockUpdateRequest("005930", 35.0, 5.0),
                new PortfolioStockUpdateRequest("000660", 25.0, 3.0)
            );
            PortfolioStockBatchUpdateRequest request = new PortfolioStockBatchUpdateRequest(updateRequests);

            // PortfolioStock 엔티티들 생성
            PortfolioStock portfolioStock1 = PortfolioStock.builder()
                    .portfolio(portfolio)
                    .stockCode("005930")
                    .targetWeight(null)
                    .thresholdPercentage(null)
                    .status(null)
                    .build();

            PortfolioStock portfolioStock2 = PortfolioStock.builder()
                    .portfolio(portfolio)
                    .stockCode("000660")
                    .targetWeight(null)
                    .thresholdPercentage(null)
                    .status(null)
                    .build();

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId)).willReturn(Optional.of(portfolio));
            given(portfolioStockRepository.findByPortfolioIdAndStockCode(portfolioId, "005930")).willReturn(Optional.of(portfolioStock1));
            given(portfolioStockRepository.findByPortfolioIdAndStockCode(portfolioId, "000660")).willReturn(Optional.of(portfolioStock2));

            // When
            List<PortfolioStockResponse> responses = portfolioStockService.updateStocksBatch(userId, portfolioId, request);

            // Then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getStockCode()).isEqualTo("005930");
            assertThat(responses.get(0).getTargetWeight()).isEqualTo(BigDecimal.valueOf(35.0));
            assertThat(responses.get(0).getThresholdPercentage()).isEqualTo(BigDecimal.valueOf(5.0));
            assertThat(responses.get(1).getStockCode()).isEqualTo("000660");
            assertThat(responses.get(1).getTargetWeight()).isEqualTo(BigDecimal.valueOf(25.0));
            assertThat(responses.get(1).getThresholdPercentage()).isEqualTo(BigDecimal.valueOf(3.0));

            verify(portfolioRepository).findByIdAndUserId(portfolioId, userId);
            verify(portfolioStockRepository).findByPortfolioIdAndStockCode(portfolioId, "005930");
            verify(portfolioStockRepository).findByPortfolioIdAndStockCode(portfolioId, "000660");
        }

        @Test
        @DisplayName("실패 - 포트폴리오를 찾을 수 없음")
        void updateStocksBatch_실패_포트폴리오없음() {
            // Given
            Long userId = 1L;
            Long portfolioId = 999L;
            List<PortfolioStockUpdateRequest> updateRequests = Arrays.asList(
                new PortfolioStockUpdateRequest("005930", 35.0, 5.0)
            );
            PortfolioStockBatchUpdateRequest request = new PortfolioStockBatchUpdateRequest(updateRequests);

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> portfolioStockService.updateStocksBatch(userId, portfolioId, request))
                    .isInstanceOf(PortfolioException.class);

            verify(portfolioRepository).findByIdAndUserId(portfolioId, userId);
        }

        @Test
        @DisplayName("실패 - 포트폴리오에 등록되지 않은 주식")
        void updateStocksBatch_실패_주식없음() {
            // Given
            Long userId = 1L;
            Long portfolioId = 1L;
            List<PortfolioStockUpdateRequest> updateRequests = Arrays.asList(
                new PortfolioStockUpdateRequest("999999", 35.0, 5.0)
            );
            PortfolioStockBatchUpdateRequest request = new PortfolioStockBatchUpdateRequest(updateRequests);

            User user = User.builder().sub("test").nickname("test").build();
            Portfolio portfolio = Portfolio.builder().user(user).name("테스트 포트폴리오").build();

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId)).willReturn(Optional.of(portfolio));
            given(portfolioStockRepository.findByPortfolioIdAndStockCode(portfolioId, "999999")).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> portfolioStockService.updateStocksBatch(userId, portfolioId, request))
                    .isInstanceOf(PortfolioStockException.class);

            verify(portfolioRepository).findByIdAndUserId(portfolioId, userId);
            verify(portfolioStockRepository).findByPortfolioIdAndStockCode(portfolioId, "999999");
        }

    }
}