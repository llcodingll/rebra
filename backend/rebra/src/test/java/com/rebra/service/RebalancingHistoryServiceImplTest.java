package com.rebra.service;

import com.rebra.common.PageInfo;
import com.rebra.common.PageResponse;
import com.rebra.dto.response.RebalancingHistoryDetailResponse;
import com.rebra.dto.response.RebalancingHistoryGraphResponse;
import com.rebra.dto.response.RebalancingHistoryListResponse;
import com.rebra.dto.response.RebalancingHistoryResponse;
import com.rebra.dto.response.RebalancingHistorySummaryResponse;
import com.rebra.dto.response.TradeDetailResponse;
import com.rebra.entity.Portfolio;
import com.rebra.entity.RebalancingOrder;
import com.rebra.entity.TradeRecord;
import com.rebra.enums.ExecutionType;
import com.rebra.enums.TransactionStatus;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.RebalancingOrderRepository;
import com.rebra.repository.TradeRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("RebalancingHistoryService 테스트")
class RebalancingHistoryServiceImplTest {


    @Mock
    private RebalancingOrderRepository rebalancingOrderRepository;

    @Mock
    private TradeRecordRepository tradeRecordRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private RebalancingHistoryServiceImpl rebalancingHistoryService;

    private Portfolio createTestPortfolio() {
        Portfolio portfolio = mock(Portfolio.class);
        lenient().when(portfolio.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 1, 1, 9, 0));
        lenient().when(portfolio.getLastRebalanceDate()).thenReturn(LocalDate.of(2025, 9, 17));
        return portfolio;
    }

    private RebalancingOrder createTestRebalancingOrder(Long id, ExecutionType executionType, LocalDateTime rebalancingDate) {
        RebalancingOrder order = mock(RebalancingOrder.class);
        lenient().when(order.getId()).thenReturn(id);
        lenient().when(order.getTotalBuyAmount()).thenReturn(new BigDecimal("1500000"));
        lenient().when(order.getTotalSellAmount()).thenReturn(new BigDecimal("800000"));
        lenient().when(order.getRebalancingDate()).thenReturn(rebalancingDate);
        lenient().when(order.getStatus()).thenReturn(TransactionStatus.COMPLETED);
        lenient().when(order.getExecutionType()).thenReturn(executionType);
        lenient().when(order.getCumulativeReturn()).thenReturn(new BigDecimal("105.25"));
        lenient().when(order.getTotalPortfolioValue()).thenReturn(new BigDecimal("10525000")); // 수익률 기반 계산
        return order;
    }

    private TradeRecord createTestTradeRecord(Long id, String stockCode, String stockName, String tradeType) {
        TradeRecord tradeRecord = mock(TradeRecord.class);
        lenient().when(tradeRecord.getId()).thenReturn(id);
        lenient().when(tradeRecord.getStockCode()).thenReturn(stockCode);
        lenient().when(tradeRecord.getStockName()).thenReturn(stockName);
        lenient().when(tradeRecord.getTradeType()).thenReturn(tradeType);
        lenient().when(tradeRecord.getExecutedShares()).thenReturn(10);
        lenient().when(tradeRecord.getExecutedPrice()).thenReturn(new BigDecimal("50000"));
        lenient().when(tradeRecord.getFee()).thenReturn(new BigDecimal("750"));
        lenient().when(tradeRecord.getProfitAmount()).thenReturn(new BigDecimal("5000"));
        lenient().when(tradeRecord.getProfitRate()).thenReturn(new BigDecimal("10.0"));
        lenient().when(tradeRecord.getReason()).thenReturn("리밸런싱");
        lenient().when(tradeRecord.getTradeDate()).thenReturn(LocalDateTime.now());
        return tradeRecord;
    }

    @Nested
    @DisplayName("getRebalancingHistoryWithPagination() - 페이지네이션 조회")
    class GetRebalancingHistoryWithPagination {

        @Test
        @DisplayName("성공 - 정상적인 페이지 응답과 요약 정보 반환")
        void getRebalancingHistoryWithPagination_성공() {
            // Given
            Long portfolioId = 1L;
            Pageable pageable = PageRequest.of(0, 20);
            Portfolio portfolio = createTestPortfolio();

            RebalancingOrder order1 = createTestRebalancingOrder(1L, ExecutionType.AUTO,
                    LocalDateTime.of(2025, 2, 15, 14, 30));
            RebalancingOrder order2 = createTestRebalancingOrder(2L, ExecutionType.MANUAL,
                    LocalDateTime.of(2025, 3, 15, 10, 15));

            Page<RebalancingOrder> orderPage = new PageImpl<>(
                    Arrays.asList(order1, order2),
                    pageable,
                    2L);

            // Mock 설정
            given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));
            given(rebalancingOrderRepository.findByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                    .willReturn(orderPage);

            given(tradeRecordRepository.countByRebalancingOrderId(1L)).willReturn(3);
            given(tradeRecordRepository.countByRebalancingOrderId(2L)).willReturn(2);

            // 요약 정보 Mock
            given(rebalancingOrderRepository.countByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(5);
            given(rebalancingOrderRepository.sumTotalBuyAmountByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(new BigDecimal("5000000"));
            given(rebalancingOrderRepository.sumTotalSellAmountByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(new BigDecimal("3000000"));
            given(rebalancingOrderRepository.countAutoRebalancingByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(3);
            given(rebalancingOrderRepository.countManualRebalancingByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(2);

            // When
            PageResponse<RebalancingHistoryListResponse> result =
                    rebalancingHistoryService.getRebalancingHistoryWithPagination(portfolioId, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.success()).isTrue();
            assertThat(result.message()).isEqualTo("리밸런싱 히스토리 조회 성공");

            RebalancingHistoryListResponse content = result.content();
            assertThat(content.getHistories()).hasSize(2);

            // 첫 번째 히스토리 검증
            RebalancingHistoryResponse history1 = content.getHistories().get(0);
            assertThat(history1.getOrderId()).isEqualTo(1L);
            assertThat(history1.getExecutionType()).isEqualTo(ExecutionType.AUTO);
            assertThat(history1.getTotalStocks()).isEqualTo(3);
            assertThat(history1.getTotalBuyAmount()).isEqualTo(new BigDecimal("1500000"));
            assertThat(history1.getTotalPortfolioValue()).isEqualTo(new BigDecimal("10525000"));
            assertThat(history1.getStatus()).isEqualTo(TransactionStatus.COMPLETED);

            // 두 번째 히스토리 검증
            RebalancingHistoryResponse history2 = content.getHistories().get(1);
            assertThat(history2.getOrderId()).isEqualTo(2L);
            assertThat(history2.getExecutionType()).isEqualTo(ExecutionType.MANUAL);
            assertThat(history2.getTotalStocks()).isEqualTo(2);
            assertThat(history2.getTotalPortfolioValue()).isEqualTo(new BigDecimal("10525000"));

            // 요약 정보 검증
            RebalancingHistorySummaryResponse summary = content.getSummary();
            assertThat(summary.getPeriod().getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
            assertThat(summary.getPeriod().getEndDate()).isEqualTo(LocalDate.of(2025, 9, 17));
            assertThat(summary.getTotalRebalances()).isEqualTo(5);
            assertThat(summary.getTotalBuyAmount()).isEqualTo(new BigDecimal("5000000"));
            assertThat(summary.getTotalSellAmount()).isEqualTo(new BigDecimal("3000000"));
            assertThat(summary.getAutoRebalances()).isEqualTo(3);
            assertThat(summary.getManualRebalances()).isEqualTo(2);

            // 페이지 정보 검증
            PageInfo pageInfo = result.pageInfo();
            assertThat(pageInfo.page()).isEqualTo(0);
            assertThat(pageInfo.totalElements()).isEqualTo(2L);
            assertThat(pageInfo.totalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 빈 페이지 응답")
        void getRebalancingHistoryWithPagination_성공_빈페이지() {
            // Given
            Long portfolioId = 1L;
            Pageable pageable = PageRequest.of(0, 20);
            Portfolio portfolio = createTestPortfolio();

            Page<RebalancingOrder> emptyPage = new PageImpl<>(
                    Collections.emptyList(),
                    pageable,
                    0L);

            given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));
            given(rebalancingOrderRepository.findByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                    .willReturn(emptyPage);

            // 요약 정보 Mock (모두 0)
            given(rebalancingOrderRepository.countByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0);
            given(rebalancingOrderRepository.sumTotalBuyAmountByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(BigDecimal.ZERO);
            given(rebalancingOrderRepository.sumTotalSellAmountByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(BigDecimal.ZERO);
            given(rebalancingOrderRepository.countAutoRebalancingByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0);
            given(rebalancingOrderRepository.countManualRebalancingByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0);

            // When
            PageResponse<RebalancingHistoryListResponse> result =
                    rebalancingHistoryService.getRebalancingHistoryWithPagination(portfolioId, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.success()).isTrue();
            assertThat(result.content().getHistories()).isEmpty();
            assertThat(result.content().getSummary().getTotalRebalances()).isEqualTo(0);
            assertThat(result.pageInfo().totalElements()).isEqualTo(0L);
        }

        @Test
        @DisplayName("실패 - 포트폴리오 없음")
        void getRebalancingHistoryWithPagination_실패_포트폴리오없음() {
            // Given
            Long portfolioId = 999L;
            Pageable pageable = PageRequest.of(0, 20);

            given(portfolioRepository.findById(portfolioId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() ->
                    rebalancingHistoryService.getRebalancingHistoryWithPagination(portfolioId, pageable))
                    .isInstanceOf(CustomRuntimeException.class)
                    .hasMessage(ExceptionCode.PORTFOLIO_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("성공 - lastRebalanceDate가 null인 경우 현재 시간 사용")
        void getRebalancingHistoryWithPagination_성공_lastRebalanceDateNull() {
            // Given
            Long portfolioId = 1L;
            Pageable pageable = PageRequest.of(0, 20);

            Portfolio portfolio = mock(Portfolio.class);
            lenient().when(portfolio.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 1, 1, 9, 0));
            lenient().when(portfolio.getLastRebalanceDate()).thenReturn(null); // null로 설정

            Page<RebalancingOrder> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0L);

            given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));
            given(rebalancingOrderRepository.findByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                    .willReturn(emptyPage);

            // 요약 정보 Mock
            given(rebalancingOrderRepository.countByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0);
            given(rebalancingOrderRepository.sumTotalBuyAmountByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(BigDecimal.ZERO);
            given(rebalancingOrderRepository.sumTotalSellAmountByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(BigDecimal.ZERO);
            given(rebalancingOrderRepository.countAutoRebalancingByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0);
            given(rebalancingOrderRepository.countManualRebalancingByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0);

            // When
            PageResponse<RebalancingHistoryListResponse> result =
                    rebalancingHistoryService.getRebalancingHistoryWithPagination(portfolioId, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.success()).isTrue();

            // endDate가 현재 날짜로 설정되었는지 확인 (정확한 시간은 검증하지 않고 오늘 날짜인지만 확인)
            LocalDate today = LocalDate.now();
            assertThat(result.content().getSummary().getPeriod().getEndDate()).isEqualTo(today);
        }
    }

    @Nested
    @DisplayName("getAllRebalancingHistory() - 그래프용 전체 조회")
    class GetAllRebalancingHistory {

        @Test
        @DisplayName("성공 - 더미 데이터와 실제 히스토리 리스트 반환")
        void getAllRebalancingHistory_성공() {
            // Given
            Long portfolioId = 1L;
            Portfolio portfolio = createTestPortfolio();

            RebalancingOrder order1 = createTestRebalancingOrder(1L, ExecutionType.AUTO,
                    LocalDateTime.of(2025, 2, 15, 14, 30));
            RebalancingOrder order2 = createTestRebalancingOrder(2L, ExecutionType.MANUAL,
                    LocalDateTime.of(2025, 3, 15, 10, 15));

            List<RebalancingOrder> orders = Arrays.asList(order1, order2);

            given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));
            given(rebalancingOrderRepository.findAllByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(orders);

            // When
            List<RebalancingHistoryGraphResponse> result =
                    rebalancingHistoryService.getAllRebalancingHistory(portfolioId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3); // 더미 데이터 1개 + 실제 데이터 2개

            // 더미 데이터 검증 (첫 번째 요소)
            RebalancingHistoryGraphResponse dummyData = result.get(0);
            assertThat(dummyData.getOrderId()).isNull();
            assertThat(dummyData.getExecutedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 9, 0));
            assertThat(dummyData.getCumulativeReturn()).isEqualTo(new BigDecimal("100.00"));

            // 실제 데이터 검증 (두 번째, 세 번째 요소)
            RebalancingHistoryGraphResponse actual1 = result.get(1);
            assertThat(actual1.getOrderId()).isEqualTo(1L);
            assertThat(actual1.getExecutedAt()).isEqualTo(LocalDateTime.of(2025, 2, 15, 14, 30));
            assertThat(actual1.getCumulativeReturn()).isEqualTo(new BigDecimal("105.25"));

            RebalancingHistoryGraphResponse actual2 = result.get(2);
            assertThat(actual2.getOrderId()).isEqualTo(2L);
            assertThat(actual2.getExecutedAt()).isEqualTo(LocalDateTime.of(2025, 3, 15, 10, 15));
            assertThat(actual2.getCumulativeReturn()).isEqualTo(new BigDecimal("105.25"));
        }

        @Test
        @DisplayName("성공 - 더미 데이터만 있는 경우 (히스토리 없음)")
        void getAllRebalancingHistory_성공_더미데이터만() {
            // Given
            Long portfolioId = 1L;
            Portfolio portfolio = createTestPortfolio();

            given(portfolioRepository.findById(portfolioId)).willReturn(Optional.of(portfolio));
            given(rebalancingOrderRepository.findAllByPortfolioIdWithDateRange(
                    eq(portfolioId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(Collections.emptyList());

            // When
            List<RebalancingHistoryGraphResponse> result =
                    rebalancingHistoryService.getAllRebalancingHistory(portfolioId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1); // 더미 데이터만

            RebalancingHistoryGraphResponse dummyData = result.get(0);
            assertThat(dummyData.getOrderId()).isNull();
            assertThat(dummyData.getExecutedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 9, 0));
            assertThat(dummyData.getCumulativeReturn()).isEqualTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("실패 - 포트폴리오 없음")
        void getAllRebalancingHistory_실패_포트폴리오없음() {
            // Given
            Long portfolioId = 999L;

            given(portfolioRepository.findById(portfolioId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() ->
                    rebalancingHistoryService.getAllRebalancingHistory(portfolioId))
                    .isInstanceOf(CustomRuntimeException.class)
                    .hasMessage(ExceptionCode.PORTFOLIO_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("getRebalancingHistoryDetail() - 상세 조회")
    class GetRebalancingHistoryDetail {

        @Test
        @DisplayName("성공 - 상세 정보와 거래 내역 반환")
        void getRebalancingHistoryDetail_성공() {
            // Given
            Long portfolioId = 1L;
            Long orderId = 123L;

            RebalancingOrder order = createTestRebalancingOrder(orderId, ExecutionType.AUTO,
                    LocalDateTime.of(2025, 2, 15, 14, 30));

            TradeRecord trade1 = createTestTradeRecord(789L, "005930", "삼성전자", "SELL");
            TradeRecord trade2 = createTestTradeRecord(790L, "000660", "SK하이닉스", "BUY");
            List<TradeRecord> tradeRecords = Arrays.asList(trade1, trade2);

            given(rebalancingOrderRepository.findByIdAndPortfolioId(orderId, portfolioId))
                    .willReturn(Optional.of(order));
            given(tradeRecordRepository.findByRebalancingOrderIdOrderByTradeDate(orderId))
                    .willReturn(tradeRecords);

            // When
            RebalancingHistoryDetailResponse result =
                    rebalancingHistoryService.getRebalancingHistoryDetail(portfolioId, orderId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
            assertThat(result.getExecutionType()).isEqualTo(ExecutionType.AUTO);
            assertThat(result.getExecutedAt()).isEqualTo(LocalDateTime.of(2025, 2, 15, 14, 30));
            assertThat(result.getCumulativeReturn()).isEqualTo(new BigDecimal("105.25"));
            assertThat(result.getTotalPortfolioValue()).isEqualTo(new BigDecimal("10525000"));

            // 거래 내역 검증
            List<TradeDetailResponse> trades = result.getTrades();
            assertThat(trades).hasSize(2);

            TradeDetailResponse tradeDetail1 = trades.get(0);
            assertThat(tradeDetail1.getTradeId()).isEqualTo(789L);
            assertThat(tradeDetail1.getStockCode()).isEqualTo("005930");
            assertThat(tradeDetail1.getStockName()).isEqualTo("삼성전자");
            assertThat(tradeDetail1.getTradeType()).isEqualTo("SELL");
            assertThat(tradeDetail1.getExecutedShares()).isEqualTo(10);
            assertThat(tradeDetail1.getPrice()).isEqualTo(new BigDecimal("50000"));

            TradeDetailResponse tradeDetail2 = trades.get(1);
            assertThat(tradeDetail2.getTradeId()).isEqualTo(790L);
            assertThat(tradeDetail2.getStockCode()).isEqualTo("000660");
            assertThat(tradeDetail2.getStockName()).isEqualTo("SK하이닉스");
            assertThat(tradeDetail2.getTradeType()).isEqualTo("BUY");
        }

        @Test
        @DisplayName("성공 - 거래 내역이 없는 경우")
        void getRebalancingHistoryDetail_성공_거래내역없음() {
            // Given
            Long portfolioId = 1L;
            Long orderId = 123L;

            RebalancingOrder order = createTestRebalancingOrder(orderId, ExecutionType.MANUAL,
                    LocalDateTime.of(2025, 2, 15, 14, 30));

            given(rebalancingOrderRepository.findByIdAndPortfolioId(orderId, portfolioId))
                    .willReturn(Optional.of(order));
            given(tradeRecordRepository.findByRebalancingOrderIdOrderByTradeDate(orderId))
                    .willReturn(Collections.emptyList());

            // When
            RebalancingHistoryDetailResponse result =
                    rebalancingHistoryService.getRebalancingHistoryDetail(portfolioId, orderId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
            assertThat(result.getExecutionType()).isEqualTo(ExecutionType.MANUAL);
            assertThat(result.getTrades()).isEmpty();
        }

        @Test
        @DisplayName("실패 - 리밸런싱 주문 없음")
        void getRebalancingHistoryDetail_실패_주문없음() {
            // Given
            Long portfolioId = 1L;
            Long orderId = 999L;

            given(rebalancingOrderRepository.findByIdAndPortfolioId(orderId, portfolioId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() ->
                    rebalancingHistoryService.getRebalancingHistoryDetail(portfolioId, orderId))
                    .isInstanceOf(CustomRuntimeException.class)
                    .hasMessage(ExceptionCode.REBALANCING_ORDER_NOT_FOUND.getMessage());
        }
    }
}