package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.response.PerformanceMetricsChartResponse;
import com.rebra.dto.response.TradeHistoryResponse;
import com.rebra.dto.response.TradeDetailResponse;
import com.rebra.dto.internal.TradingActivityInfo;
import com.rebra.entity.PerformanceMetrics;
import com.rebra.entity.Portfolio;
import com.rebra.entity.PortfolioStock;
import com.rebra.entity.RebalancingOrder;
import com.rebra.entity.TradeRecord;
import com.rebra.enums.ExecutionType;
import com.rebra.exception.ExceptionCode;
import com.rebra.exception.portfolio.PortfolioException;
import com.rebra.repository.PerformanceMetricsRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.RebalancingOrderRepository;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceMetricsServiceImpl implements PerformanceMetricsService {

    private final PerformanceMetricsRepository performanceMetricsRepository;
    private final RebalancingOrderRepository rebalancingOrderRepository;
    private final PortfolioRepository portfolioRepository;
    private final KisApiComponent kisApiComponent;

    /**
     * 포트폴리오의 일일 성과 메트릭 수집 및 저장
     */
    @Override
    @Transactional
    public void collectDailyMetrics(Portfolio portfolio, LocalDate targetDate) {
        try {
            log.info("포트폴리오 성과 메트릭 수집 시작 - Portfolio ID: {}, Date: {}",
                    portfolio.getId(), targetDate);

            // 1. 포트폴리오 총 가치 계산
            double totalValue = calculatePortfolioTotalValue(portfolio);

            // 2. 거래 여부 판단
            TradingActivityInfo tradingInfo = analyzeTradingActivity(portfolio, targetDate);

            // 3. 기존 성과 메트릭 조회 (isCompositionChanged 관계없이)
            Optional<PerformanceMetrics> existingMetrics = performanceMetricsRepository
                    .findByPortfolioIdAndMetricDate(portfolio.getId(), targetDate);

            PerformanceMetrics metrics;
            if (existingMetrics.isPresent()) {
                // 기존 데이터 업데이트
                metrics = existingMetrics.get();
                metrics.updateDailyMetrics(totalValue,
                                         tradingInfo.isRebalanced(),
                                         tradingInfo.isSold(),
                                         tradingInfo.isBought());

                log.info("기존 성과 메트릭 업데이트 - Portfolio ID: {}, Date: {}",
                        portfolio.getId(), targetDate);
            } else {
                // 새로운 데이터 생성
                metrics = PerformanceMetrics.builder()
                        .portfolio(portfolio)
                        .metricDate(targetDate)
                        .totalValue(totalValue)
                        .isRebalanced(tradingInfo.isRebalanced())
                        .isSold(tradingInfo.isSold())
                        .isBought(tradingInfo.isBought())
                        .isCompositionChanged(false) // 일일 수집은 구성 변경이 아님
                        .build();

                log.info("새로운 성과 메트릭 생성 - Portfolio ID: {}, Date: {}",
                        portfolio.getId(), targetDate);
            }

            performanceMetricsRepository.save(metrics);

            log.info("포트폴리오 성과 메트릭 저장 완료 - Portfolio ID: {}, Date: {}, TotalValue: {}, " +
                    "IsRebalanced: {}, IsSold: {}, IsBought: {}",
                    portfolio.getId(), targetDate, totalValue,
                    tradingInfo.isRebalanced(), tradingInfo.isSold(), tradingInfo.isBought());

        } catch (Exception e) {
            log.error("포트폴리오 성과 메트릭 수집 실패 - Portfolio ID: {}, Date: {}",
                    portfolio.getId(), targetDate, e);
            throw e;
        }
    }

    /**
     * KIS API를 활용한 포트폴리오 총 가치 계산
     */
    private double calculatePortfolioTotalValue(Portfolio portfolio) {
        try {
            log.debug("포트폴리오 총 가치 계산 시작 - Portfolio ID: {}", portfolio.getId());

            // KIS API를 통한 잔고 조회
            InquireBalanceResult balanceResult = kisApiComponent.getUserBalance(portfolio.getAccount());

            if (balanceResult == null || balanceResult.getOutput1() == null) {
                log.warn("KIS API 잔고 조회 결과가 null - Portfolio ID: {}", portfolio.getId());
                return 0.0;
            }

            // 등록된 포트폴리오 주식 코드 집합 생성
            Set<String> portfolioStockCodes = new HashSet<>();
            for (PortfolioStock portfolioStock : portfolio.getPortfolioStocks()) {
                portfolioStockCodes.add(portfolioStock.getStockCode());
            }

            double totalValue = 0.0;

            // KIS API 결과에서 등록된 주식만 필터링하여 총 가치 계산
            if (balanceResult.getOutput1() != null) {
                for (var balanceItem : balanceResult.getOutput1()) {
                    String stockCode = balanceItem.getPdno(); // 종목코드

                    if (portfolioStockCodes.contains(stockCode)) {
                        try {
                            // 보유수량
                            int quantity = Integer.parseInt(balanceItem.getHldgQty());
                            // 현재가
                            double currentPrice = Double.parseDouble(balanceItem.getPrpr());

                            double stockValue = quantity * currentPrice;
                            totalValue += stockValue;

                            log.debug("주식 가치 계산 - StockCode: {}, Quantity: {}, Price: {}, Value: {}",
                                    stockCode, quantity, currentPrice, stockValue);
                        } catch (NumberFormatException e) {
                            log.warn("주식 정보 파싱 실패 - StockCode: {}, Error: {}", stockCode, e.getMessage());
                        }
                    }
                }
            }

            log.debug("포트폴리오 총 가치 계산 완료 - Portfolio ID: {}, TotalValue: {}",
                    portfolio.getId(), totalValue);

            return totalValue;

        } catch (Exception e) {
            log.error("포트폴리오 총 가치 계산 실패 - Portfolio ID: {}", portfolio.getId(), e);
            return 0.0;
        }
    }

    /**
     * 오늘 자의 RebalancingOrder를 통한 거래 여부 판단
     */
    private TradingActivityInfo analyzeTradingActivity(Portfolio portfolio, LocalDate targetDate) {
        try {
            log.debug("거래 활동 분석 시작 - Portfolio ID: {}, Date: {}", portfolio.getId(), targetDate);

            // 해당 날짜의 RebalancingOrder 조회
            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();

            List<RebalancingOrder> todayOrders = rebalancingOrderRepository
                    .findAllByPortfolioIdWithDateRange(portfolio.getId(), startOfDay, endOfDay);

            boolean isRebalanced = false;
            boolean isSold = false;
            boolean isBought = false;

            // ExecutionType별 거래 활동 분석
            for (RebalancingOrder order : todayOrders) {
                ExecutionType executionType = order.getExecutionType();

                switch (executionType) {
                    case AUTO:
                    case MANUAL:
                        isRebalanced = true;
                        log.debug("리밸런싱 실행 감지 - Portfolio ID: {}, ExecutionType: {}",
                                portfolio.getId(), executionType);
                        break;
                    case BUY_PERSONAL:
                        isBought = true;
                        log.debug("개인 매수 실행 감지 - Portfolio ID: {}", portfolio.getId());
                        break;
                    case SELL_PERSONAL:
                        isSold = true;
                        log.debug("개인 매도 실행 감지 - Portfolio ID: {}", portfolio.getId());
                        break;
                }
            }

            TradingActivityInfo tradingInfo = new TradingActivityInfo(isRebalanced, isSold, isBought);

            log.debug("거래 활동 분석 완료 - Portfolio ID: {}, Date: {}, IsRebalanced: {}, IsSold: {}, IsBought: {}",
                    portfolio.getId(), targetDate, isRebalanced, isSold, isBought);

            return tradingInfo;

        } catch (Exception e) {
            log.error("거래 활동 분석 실패 - Portfolio ID: {}, Date: {}", portfolio.getId(), targetDate, e);
            return new TradingActivityInfo(false, false, false);
        }
    }

    /**
     * 포트폴리오별 성과 메트릭 조회
     */
    @Override
    public List<PerformanceMetrics> getPortfolioMetrics(Long portfolioId) {
        return performanceMetricsRepository.findByPortfolioIdOrderByMetricDateDesc(portfolioId);
    }

    /**
     * 포트폴리오의 특정 기간 성과 메트릭 조회
     */
    @Override
    public List<PerformanceMetrics> getPortfolioMetricsInPeriod(Long portfolioId, LocalDate startDate, LocalDate endDate) {
        return performanceMetricsRepository.findByPortfolioIdWithDateRange(portfolioId, startDate, endDate);
    }

    /**
     * 포트폴리오의 특정 날짜 성과 메트릭 존재 여부 확인
     */
    @Override
    public boolean existsMetrics(Long portfolioId, LocalDate targetDate) {
        return performanceMetricsRepository.existsByPortfolioIdAndMetricDate(portfolioId, targetDate);
    }

    /**
     * 포트폴리오 성과 차트 데이터 조회 (생성일부터 오늘까지)
     */
    @Override
    public PerformanceMetricsChartResponse getPortfolioPerformanceChart(Long portfolioId, Long userId) {
        try {
            log.info("포트폴리오 성과 차트 데이터 조회 시작 - Portfolio ID: {}, User ID: {}", portfolioId, userId);

            // 포트폴리오 조회 및 권한 검증
            Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                    .orElseThrow(() -> new PortfolioException(ExceptionCode.PORTFOLIO_NOT_FOUND));

            // 조회 기간 설정 (포트폴리오 생성일부터 오늘까지)
            LocalDate startDate = portfolio.getCreatedAt().toLocalDate();
            LocalDate endDate = LocalDate.now();

            log.debug("차트 데이터 조회 기간 - Start: {}, End: {}", startDate, endDate);

            // 성과 메트릭 데이터 조회
            List<PerformanceMetrics> metricsData = performanceMetricsRepository
                    .findByPortfolioIdWithDateRange(portfolioId, startDate, endDate);

            // 데이터 포인트 변환
            List<PerformanceMetricsChartResponse.PerformanceDataPoint> dataPoints = metricsData.stream()
                    .map(PerformanceMetricsChartResponse.PerformanceDataPoint::from)
                    .toList();

            // 통계 정보 계산
            PerformanceMetricsChartResponse.PerformanceStatistics statistics =
                    PerformanceMetricsChartResponse.PerformanceStatistics.from(metricsData);

            // 응답 객체 생성
            PerformanceMetricsChartResponse response = PerformanceMetricsChartResponse.builder()
                    .portfolioId(portfolioId)
                    .portfolioName(portfolio.getName())
                    .portfolioCreatedDate(startDate)
                    .startDate(startDate)
                    .endDate(endDate)
                    .performanceData(dataPoints)
                    .statistics(statistics)
                    .build();

            log.info("포트폴리오 성과 차트 데이터 조회 완료 - Portfolio ID: {}, Data Points: {}",
                    portfolioId, dataPoints.size());

            return response;

        } catch (PortfolioException e) {
            log.error("포트폴리오 성과 차트 조회 실패 - Portfolio ID: {}, User ID: {}, Error: {}",
                    portfolioId, userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("포트폴리오 성과 차트 조회 중 예상치 못한 오류 - Portfolio ID: {}, User ID: {}",
                    portfolioId, userId, e);
            throw new RuntimeException("성과 차트 조회 실패", e);
        }
    }

    /**
     * 특정 날짜의 거래 히스토리 조회
     */
    @Override
    public TradeHistoryResponse getTradeHistoryByDate(Long portfolioId, Long userId, LocalDate targetDate) {
        try {
            log.info("거래 히스토리 조회 시작 - Portfolio ID: {}, User ID: {}, Date: {}",
                    portfolioId, userId, targetDate);

            // 포트폴리오 조회 및 권한 검증
            Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                    .orElseThrow(() -> new PortfolioException(ExceptionCode.PORTFOLIO_NOT_FOUND));

            // 날짜 범위 설정 (해당 날짜 00:00:00 ~ 다음날 00:00:00)
            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();

            log.debug("거래 히스토리 조회 범위 - Start: {}, End: {}", startOfDay, endOfDay);

            // 해당 날짜의 리밸런싱 주문과 거래 기록 조회
            Optional<RebalancingOrder> rebalancingOrderOpt = rebalancingOrderRepository
                    .findByPortfolioIdAndDateWithTrades(portfolioId, startOfDay, endOfDay);

            List<TradeDetailResponse> tradeDetails = new ArrayList<>();

            if (rebalancingOrderOpt.isPresent()) {
                RebalancingOrder rebalancingOrder = rebalancingOrderOpt.get();

                log.debug("리밸런싱 주문 발견 - Order ID: {}, Trade Records: {}",
                        rebalancingOrder.getId(), rebalancingOrder.getTradeRecords().size());

                // TradeRecord를 TradeDetailResponse로 변환
                tradeDetails = rebalancingOrder.getTradeRecords().stream()
                        .map(TradeDetailResponse::from)
                        .toList();
            } else {
                log.debug("해당 날짜에 거래 기록 없음 - Portfolio ID: {}, Date: {}", portfolioId, targetDate);
            }

            // TradeHistoryResponse 생성
            TradeHistoryResponse response = TradeHistoryResponse.of(targetDate, tradeDetails);

            log.info("거래 히스토리 조회 완료 - Portfolio ID: {}, Date: {}, Trade Count: {}",
                    portfolioId, targetDate, tradeDetails.size());

            return response;

        } catch (PortfolioException e) {
            log.error("거래 히스토리 조회 실패 - Portfolio ID: {}, User ID: {}, Date: {}, Error: {}",
                    portfolioId, userId, targetDate, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("거래 히스토리 조회 중 예상치 못한 오류 - Portfolio ID: {}, User ID: {}, Date: {}",
                    portfolioId, userId, targetDate, e);
            throw new RuntimeException("거래 히스토리 조회 실패", e);
        }
    }
}