package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.response.PerformanceMetricsChartResponse;
import com.rebra.entity.*;
import com.rebra.enums.ExecutionType;
import com.rebra.exception.portfolio.PortfolioException;
import com.rebra.repository.PerformanceMetricsRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.RebalancingOrderRepository;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceMetricsService 테스트")
class PerformanceMetricsServiceImplTest {

    @Mock
    private PerformanceMetricsRepository performanceMetricsRepository;

    @Mock
    private RebalancingOrderRepository rebalancingOrderRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private KisApiComponent kisApiComponent;

    @InjectMocks
    private PerformanceMetricsServiceImpl performanceMetricsService;

    @Nested
    @DisplayName("일일 성과 메트릭 수집")
    class CollectDailyMetrics {

        @Test
        @DisplayName("성공 - 정상적인 메트릭 수집")
        void collectDailyMetrics_성공() {
            // Given
            User user = createUser(1L);
            Account account = createAccount(1L, user);
            Portfolio portfolio = createPortfolio(1L, user, account);
            LocalDate targetDate = LocalDate.of(2024, 1, 1);

            given(performanceMetricsRepository.existsByPortfolioIdAndMetricDate(portfolio.getId(), targetDate))
                    .willReturn(false);

            InquireBalanceResult balanceResult = createMockBalanceResult();
            given(kisApiComponent.getUserBalance(account)).willReturn(balanceResult);

            List<RebalancingOrder> rebalancingOrders = Arrays.asList(
                    createRebalancingOrder(1L, portfolio, ExecutionType.BUY_PERSONAL)
            );
            given(rebalancingOrderRepository.findAllByPortfolioIdWithDateRange(
                    eq(portfolio.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(rebalancingOrders);

            // When
            performanceMetricsService.collectDailyMetrics(portfolio, targetDate);

            // Then
            verify(performanceMetricsRepository).save(any(PerformanceMetrics.class));
        }

        @Test
        @DisplayName("성공 - 이미 존재하는 데이터는 스킵")
        void collectDailyMetrics_성공_중복스킵() {
            // Given
            User user = createUser(1L);
            Account account = createAccount(1L, user);
            Portfolio portfolio = createPortfolio(1L, user, account);
            LocalDate targetDate = LocalDate.of(2024, 1, 1);

            given(performanceMetricsRepository.existsByPortfolioIdAndMetricDate(portfolio.getId(), targetDate))
                    .willReturn(true);

            // When
            performanceMetricsService.collectDailyMetrics(portfolio, targetDate);

            // Then
            verify(performanceMetricsRepository, never()).save(any(PerformanceMetrics.class));
            verify(kisApiComponent, never()).getUserBalance(any());
        }

        @Test
        @DisplayName("성공 - KIS API 응답이 null인 경우")
        void collectDailyMetrics_성공_KIS응답null() {
            // Given
            User user = createUser(1L);
            Account account = createAccount(1L, user);
            Portfolio portfolio = createPortfolio(1L, user, account);
            LocalDate targetDate = LocalDate.of(2024, 1, 1);

            given(performanceMetricsRepository.existsByPortfolioIdAndMetricDate(portfolio.getId(), targetDate))
                    .willReturn(false);
            given(kisApiComponent.getUserBalance(account)).willReturn(null);
            given(rebalancingOrderRepository.findAllByPortfolioIdWithDateRange(
                    any(), any(), any())).willReturn(Collections.emptyList());

            // When
            performanceMetricsService.collectDailyMetrics(portfolio, targetDate);

            // Then
            verify(performanceMetricsRepository).save(argThat(metrics ->
                    metrics.getTotalValue() == 0.0 &&
                    !metrics.isRebalanced() &&
                    !metrics.isSold() &&
                    !metrics.isBought()
            ));
        }

        @Test
        @DisplayName("성공 - 거래 활동 분석 테스트")
        void collectDailyMetrics_성공_거래활동분석() {
            // Given
            User user = createUser(1L);
            Account account = createAccount(1L, user);
            Portfolio portfolio = createPortfolio(1L, user, account);
            LocalDate targetDate = LocalDate.of(2024, 1, 1);

            given(performanceMetricsRepository.existsByPortfolioIdAndMetricDate(portfolio.getId(), targetDate))
                    .willReturn(false);
            given(kisApiComponent.getUserBalance(account)).willReturn(null);

            List<RebalancingOrder> rebalancingOrders = Arrays.asList(
                    createRebalancingOrder(1L, portfolio, ExecutionType.AUTO),
                    createRebalancingOrder(2L, portfolio, ExecutionType.BUY_PERSONAL),
                    createRebalancingOrder(3L, portfolio, ExecutionType.SELL_PERSONAL)
            );
            given(rebalancingOrderRepository.findAllByPortfolioIdWithDateRange(
                    any(), any(), any())).willReturn(rebalancingOrders);

            // When
            performanceMetricsService.collectDailyMetrics(portfolio, targetDate);

            // Then
            verify(performanceMetricsRepository).save(argThat(metrics ->
                    metrics.isRebalanced() &&
                    metrics.isBought() &&
                    metrics.isSold()
            ));
        }
    }

    @Nested
    @DisplayName("포트폴리오 성과 차트 조회")
    class GetPortfolioPerformanceChart {

        @Test
        @DisplayName("성공 - 정상적인 차트 데이터 조회")
        void getPortfolioPerformanceChart_성공() {
            // Given
            Long portfolioId = 1L;
            Long userId = 1L;
            User user = createUser(userId);
            Account account = createAccount(1L, user);
            Portfolio portfolio = createPortfolio(portfolioId, user, account);

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId))
                    .willReturn(Optional.of(portfolio));

            List<PerformanceMetrics> metricsData = Arrays.asList(
                    createPerformanceMetrics(1L, portfolio, LocalDate.of(2024, 1, 1), 1000000.0, false, false, true),
                    createPerformanceMetrics(2L, portfolio, LocalDate.of(2024, 1, 2), 1050000.0, true, false, false)
            );

            given(performanceMetricsRepository.findByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(metricsData);

            // When
            PerformanceMetricsChartResponse result = performanceMetricsService
                    .getPortfolioPerformanceChart(portfolioId, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPortfolioId()).isEqualTo(portfolioId);
            assertThat(result.getPortfolioName()).isEqualTo(portfolio.getName());
            assertThat(result.getPerformanceData()).hasSize(2);
            assertThat(result.getStatistics().getTotalDataPoints()).isEqualTo(2);
            assertThat(result.getStatistics().getRebalancingCount()).isEqualTo(1);
            assertThat(result.getStatistics().getBuyCount()).isEqualTo(1);
            assertThat(result.getStatistics().getSellCount()).isEqualTo(0);
            assertThat(result.getStatistics().getInitialValue()).isEqualTo(1000000.0);
            assertThat(result.getStatistics().getFinalValue()).isEqualTo(1050000.0);
            assertThat(result.getStatistics().getTotalReturnRate()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("성공 - 빈 데이터 처리")
        void getPortfolioPerformanceChart_성공_빈데이터() {
            // Given
            Long portfolioId = 1L;
            Long userId = 1L;
            User user = createUser(userId);
            Account account = createAccount(1L, user);
            Portfolio portfolio = createPortfolio(portfolioId, user, account);

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId))
                    .willReturn(Optional.of(portfolio));
            given(performanceMetricsRepository.findByPortfolioIdWithDateRange(
                    any(), any(), any())).willReturn(Collections.emptyList());

            // When
            PerformanceMetricsChartResponse result = performanceMetricsService
                    .getPortfolioPerformanceChart(portfolioId, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPerformanceData()).isEmpty();
            assertThat(result.getStatistics().getTotalDataPoints()).isEqualTo(0);
            assertThat(result.getStatistics().getRebalancingCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 포트폴리오")
        void getPortfolioPerformanceChart_실패_포트폴리오없음() {
            // Given
            Long portfolioId = 999L;
            Long userId = 1L;

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> performanceMetricsService.getPortfolioPerformanceChart(portfolioId, userId))
                    .isInstanceOf(PortfolioException.class);
        }

        @Test
        @DisplayName("실패 - 권한 없는 포트폴리오 접근")
        void getPortfolioPerformanceChart_실패_권한없음() {
            // Given
            Long portfolioId = 1L;
            Long userId = 999L; // 다른 사용자

            given(portfolioRepository.findByIdAndUserId(portfolioId, userId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> performanceMetricsService.getPortfolioPerformanceChart(portfolioId, userId))
                    .isInstanceOf(PortfolioException.class);
        }
    }

    @Nested
    @DisplayName("기본 조회 메서드들")
    class BasicQueryMethods {

        @Test
        @DisplayName("포트폴리오별 성과 메트릭 조회")
        void getPortfolioMetrics_성공() {
            // Given
            Long portfolioId = 1L;
            List<PerformanceMetrics> expectedMetrics = Arrays.asList(
                    createPerformanceMetrics(1L, null, LocalDate.now(), 1000000.0, false, false, false),
                    createPerformanceMetrics(2L, null, LocalDate.now().minusDays(1), 950000.0, true, false, false)
            );

            given(performanceMetricsRepository.findByPortfolioIdOrderByMetricDateDesc(portfolioId))
                    .willReturn(expectedMetrics);

            // When
            List<PerformanceMetrics> result = performanceMetricsService.getPortfolioMetrics(portfolioId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(expectedMetrics);
        }

        @Test
        @DisplayName("특정 기간 성과 메트릭 조회")
        void getPortfolioMetricsInPeriod_성공() {
            // Given
            Long portfolioId = 1L;
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<PerformanceMetrics> expectedMetrics = Collections.singletonList(
                    createPerformanceMetrics(1L, null, LocalDate.of(2024, 1, 15), 1000000.0, false, false, false)
            );

            given(performanceMetricsRepository.findByPortfolioIdWithDateRange(portfolioId, startDate, endDate))
                    .willReturn(expectedMetrics);

            // When
            List<PerformanceMetrics> result = performanceMetricsService
                    .getPortfolioMetricsInPeriod(portfolioId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(expectedMetrics);
        }

        @Test
        @DisplayName("메트릭 존재 여부 확인")
        void existsMetrics_성공() {
            // Given
            Long portfolioId = 1L;
            LocalDate targetDate = LocalDate.now();

            given(performanceMetricsRepository.existsByPortfolioIdAndMetricDate(portfolioId, targetDate))
                    .willReturn(true);

            // When
            boolean result = performanceMetricsService.existsMetrics(portfolioId, targetDate);

            // Then
            assertThat(result).isTrue();
        }
    }

    // 헬퍼 메서드들
    private User createUser(Long id) {
        User user = User.builder()
                .sub("test-sub-" + id)
                .nickname("testuser" + id)
                .phoneNumber("010-1234-5678")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Account createAccount(Long id, User user) {
        Account account = Account.builder()
                .user(user)
                .accountNumber("12345678")
                .accountType(AccountType.MOCK)
                .build();
        ReflectionTestUtils.setField(account, "id", id);
        return account;
    }

    private Portfolio createPortfolio(Long id, User user, Account account) {
        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .account(account)
                .name("테스트 포트폴리오")
                .description("테스트용 포트폴리오")
                .build();
        ReflectionTestUtils.setField(portfolio, "id", id);
        ReflectionTestUtils.setField(portfolio, "createdAt", java.time.LocalDateTime.of(2024, 1, 1, 0, 0));
        ReflectionTestUtils.setField(portfolio, "portfolioStocks", java.util.Collections.emptyList());
        return portfolio;
    }

    private PerformanceMetrics createPerformanceMetrics(Long id, Portfolio portfolio, LocalDate metricDate,
                                                       double totalValue, boolean isRebalanced,
                                                       boolean isSold, boolean isBought) {
        return PerformanceMetrics.builder()
                .portfolio(portfolio)
                .metricDate(metricDate)
                .totalValue(totalValue)
                .isRebalanced(isRebalanced)
                .isSold(isSold)
                .isBought(isBought)
                .build();
    }

    private RebalancingOrder createRebalancingOrder(Long id, Portfolio portfolio, ExecutionType executionType) {
        return RebalancingOrder.builder()
                .portfolio(portfolio)
                .totalBuyAmount(0L)
                .totalSellAmount(0L)
                .rebalancingDate(LocalDateTime.now())
                .status(com.rebra.enums.TransactionStatus.COMPLETED)
                .executionType(executionType)
                .build();
    }

    private InquireBalanceResult createMockBalanceResult() {
        // KIS API 결과는 실제 사용 시 Mock으로 처리
        InquireBalanceResult result = new InquireBalanceResult();
        // 필요한 경우 Mock 설정 추가
        return result;
    }
}