package com.rebra.service;

import com.rebra.dto.response.RebalancingHistoryDetailResponse;
import com.rebra.dto.response.RebalancingHistoryGraphResponse;
import com.rebra.dto.response.RebalancingHistoryListResponse;
import com.rebra.dto.response.RebalancingHistoryResponse;
import com.rebra.dto.response.RebalancingHistorySummaryResponse;
import com.rebra.dto.response.TradeDetailResponse;
import com.rebra.common.PageInfo;
import com.rebra.common.PageResponse;
import com.rebra.entity.RebalancingOrder;
import com.rebra.entity.TradeRecord;
import com.rebra.entity.Portfolio;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.repository.RebalancingOrderRepository;
import com.rebra.repository.TradeRecordRepository;
import com.rebra.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 최적화 대상
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RebalancingHistoryServiceImpl implements RebalancingHistoryService {

    private final RebalancingOrderRepository rebalancingOrderRepository;
    private final TradeRecordRepository tradeRecordRepository;
    private final PortfolioRepository portfolioRepository;

    @Override
    public PageResponse<RebalancingHistoryListResponse> getRebalancingHistoryWithPagination(
            Long portfolioId, Pageable pageable) {

        log.info("리밸런싱 히스토리 페이지네이션 조회 - 포트폴리오 ID: {}, 페이지: {}",
                portfolioId, pageable.getPageNumber());

        // 포트폴리오 조회 및 날짜 자동 계산
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND));

        LocalDateTime startDateTime = portfolio.getCreatedAt(); // 포트폴리오 생성일
        LocalDateTime endDateTime = portfolio.getLastRebalanceDate() != null ?
                portfolio.getLastRebalanceDate().atTime(23, 59, 59) : LocalDateTime.now();

        // 페이지네이션된 리밸런싱 주문 조회
        Page<RebalancingOrder> orderPage = rebalancingOrderRepository
                .findByPortfolioIdWithDateRange(portfolioId, startDateTime, endDateTime, pageable);

        // 리밸런싱 히스토리 응답 생성
        List<RebalancingHistoryResponse> historyResponses = orderPage.getContent().stream()
                .map(order -> {
                    Integer totalStocks = tradeRecordRepository.countByRebalancingOrderId(order.getId());
                    return RebalancingHistoryResponse.from(order, totalStocks);
                })
                .collect(Collectors.toList());

        // 요약 정보 생성
        RebalancingHistorySummaryResponse summary = createSummary(portfolioId, startDateTime, endDateTime);

        log.info("리밸런싱 히스토리 페이지네이션 조회 성공 - 포트폴리오ID: {}, 기간: {} ~ {}, 총 요소 수: {}",
                portfolioId, startDateTime.toLocalDate(), endDateTime.toLocalDate(), orderPage.getTotalElements());

        // 응답 데이터 구성
        RebalancingHistoryListResponse content = RebalancingHistoryListResponse.of(historyResponses, summary);
        PageInfo pageInfo = PageInfo.from(orderPage);

        return PageResponse.success("리밸런싱 히스토리 조회 성공", content, pageInfo);
    }

    @Override
    public List<RebalancingHistoryGraphResponse> getAllRebalancingHistory(Long portfolioId) {

        log.info("리밸런싱 히스토리 전체 조회 (그래프용) - 포트폴리오 ID: {}", portfolioId);

        // 포트폴리오 조회 및 날짜 자동 계산
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND));

        LocalDateTime startDateTime = portfolio.getCreatedAt(); // 포트폴리오 생성일
        LocalDateTime endDateTime = portfolio.getLastRebalanceDate() != null ?
                portfolio.getLastRebalanceDate().atTime(23, 59, 59) : LocalDateTime.now();

        // 전체 리밸런싱 주문 조회 (날짜 오름차순)
        List<RebalancingOrder> orders = rebalancingOrderRepository
                .findAllByPortfolioIdWithDateRange(portfolioId, startDateTime, endDateTime);

        // 시작점 더미 데이터 생성 (포트폴리오 생성일, 100% 수익률)
        List<RebalancingHistoryGraphResponse> result = new ArrayList<>();
        result.add(RebalancingHistoryGraphResponse.createStartingPoint(portfolio.getCreatedAt()));

        // 히스토리 정보 생성 후 추가
        List<RebalancingHistoryGraphResponse> historyGraphs = orders.stream()
                .map(RebalancingHistoryGraphResponse::from)
                .collect(Collectors.toList());

        result.addAll(historyGraphs);

        log.info("리밸런싱 히스토리 전체 조회 (그래프용) 성공 - 포트폴리오ID: {}, 기간: {} ~ {}, 히스토리 수: {} (더미 포함)",
                portfolioId, startDateTime.toLocalDate(), endDateTime.toLocalDate(), result.size());

        return result;
    }

    @Override
    public RebalancingHistoryDetailResponse getRebalancingHistoryDetail(Long portfolioId, Long orderId) {
        log.info("리밸런싱 히스토리 상세 조회 - 포트폴리오 ID: {}, 주문 ID: {}", portfolioId, orderId);

        // 리밸런싱 주문 조회 (소유자 검증 포함)
        RebalancingOrder order = rebalancingOrderRepository.findByIdAndPortfolioId(orderId, portfolioId)
                .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.REBALANCING_ORDER_NOT_FOUND));

        // 거래 내역 조회
        List<TradeDetailResponse> trades = getTradeDetails(orderId);

        return RebalancingHistoryDetailResponse.from(order, trades);
    }

    /**
     * 리밸런싱 주문의 거래 내역 조회
     */
    private List<TradeDetailResponse> getTradeDetails(Long rebalancingOrderId) {
        List<TradeRecord> tradeRecords = tradeRecordRepository
                .findByRebalancingOrderIdOrderByTradeDate(rebalancingOrderId);

        return tradeRecords.stream()
                .map(TradeDetailResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 리밸런싱 요약 정보 생성
     * 추후에 필요할 거 같아서.. 추가
     */
    private RebalancingHistorySummaryResponse createSummary(
            Long portfolioId, LocalDateTime startDateTime, LocalDateTime endDateTime) {

        // 통계 정보 조회
        Integer totalRebalances = rebalancingOrderRepository
                .countByPortfolioIdWithDateRange(portfolioId, startDateTime, endDateTime);

        Long totalBuyAmount = rebalancingOrderRepository
                .sumTotalBuyAmountByPortfolioIdWithDateRange(portfolioId, startDateTime, endDateTime);

        Long totalSellAmount = rebalancingOrderRepository
                .sumTotalSellAmountByPortfolioIdWithDateRange(portfolioId, startDateTime, endDateTime);

        Integer autoRebalances = rebalancingOrderRepository
                .countAutoRebalancingByPortfolioIdWithDateRange(portfolioId, startDateTime, endDateTime);

        Integer manualRebalances = rebalancingOrderRepository
                .countManualRebalancingByPortfolioIdWithDateRange(portfolioId, startDateTime, endDateTime);

        // 날짜 변환
        LocalDate startDate = startDateTime != null ? startDateTime.toLocalDate() : null;
        LocalDate endDate = endDateTime != null ? endDateTime.toLocalDate() : null;

        return RebalancingHistorySummaryResponse.of(
                startDate, endDate, totalRebalances, totalBuyAmount, totalSellAmount,
                autoRebalances, manualRebalances);
    }
}