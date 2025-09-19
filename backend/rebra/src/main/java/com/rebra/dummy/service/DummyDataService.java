package com.rebra.dummy.service;

import com.rebra.dummy.dto.DummyDataResponse;
import com.rebra.dummy.dto.UpdateCreatedAtResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DummyDataService {

    private final PortfolioRepository portfolioRepository;
    private final RebalancingOrderRepository rebalancingOrderRepository;
    private final TradeRecordRepository tradeRecordRepository;
    private final Random random = new Random();

    // 더미 데이터용 주식 정보
    private static final String[][] STOCK_DATA = {
            {"005930", "삼성전자", "75000", "85000"},
            {"000660", "SK하이닉스", "90000", "110000"},
            {"035420", "NAVER", "180000", "220000"},
            {"207940", "삼성바이오로직스", "750000", "900000"},
            {"005380", "현대차", "180000", "220000"},
            {"006400", "삼성SDI", "400000", "500000"},
            {"051910", "LG화학", "350000", "450000"},
            {"035720", "카카오", "45000", "65000"}
    };

    private static final String[] TRADE_TYPES = {"BUY", "SELL"};
    private static final ExecutionType[] EXECUTION_TYPES = {ExecutionType.AUTO, ExecutionType.MANUAL};
    private static final TransactionStatus[] STATUSES = {
            TransactionStatus.COMPLETED, TransactionStatus.COMPLETED, TransactionStatus.COMPLETED,
            TransactionStatus.EXECUTING, TransactionStatus.FAILED
    }; // COMPLETED 높은 비율

    public DummyDataResponse createDummyRebalancingHistory(Long userId, Long portfolioId) {
        log.info("더미 리밸런싱 히스토리 생성 시작 - userId: {}, portfolioId: {}", userId, portfolioId);

        // 포트폴리오 검증 및 소유권 확인
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new CustomRuntimeException(ExceptionCode.ACCESS_DENIED);
        }

        // 더미 데이터 생성
        List<RebalancingOrder> rebalancingOrders = new ArrayList<>();
        List<TradeRecord> allTradeRecords = new ArrayList<>();

        // 최근 3개월간 4~6개의 리밸런싱 생성
        int rebalancingCount = 4 + random.nextInt(3); // 4~6개
        Double currentReturn = 1.0; // 시작 수익률 100%

        for (int i = 0; i < rebalancingCount; i++) {
            // 리밸런싱 날짜: 최근 3개월 내 랜덤
            LocalDateTime rebalancingDate = generateRandomRebalancingDate(i, rebalancingCount);

            // TradeRecord 먼저 생성
            List<TradeRecord> tradeRecords = createDummyTradeRecords();

            // RebalancingOrder 생성
            RebalancingOrder rebalancingOrder = createDummyRebalancingOrder(
                    portfolio, tradeRecords, rebalancingDate, currentReturn);

            // TradeRecord에 RebalancingOrder 연결
            for (TradeRecord trade : tradeRecords) {
                TradeRecord updatedTrade = TradeRecord.builder()
                        .rebalancingOrder(rebalancingOrder)
                        .stockCode(trade.getStockCode())
                        .stockName(trade.getStockName())
                        .tradeType(trade.getTradeType())
                        .tradeDate(trade.getTradeDate())
                        .executedShares(trade.getExecutedShares())
                        .executedPrice(trade.getExecutedPrice())
                        .totalAmount(trade.getTotalAmount())
                        .status(trade.getStatus())
                        .orderNumber(trade.getOrderNumber())
                        .reason(trade.getReason())
                        .build();
                allTradeRecords.add(updatedTrade);
            }

            rebalancingOrders.add(rebalancingOrder);

            // 다음 수익률 계산 (점진적 증가)
            currentReturn = currentReturn + (random.nextDouble() * 0.05 + 0.01); // 1~6% 증가
        }

        // 데이터베이스 저장
        List<RebalancingOrder> savedOrders = rebalancingOrderRepository.saveAll(rebalancingOrders);
        List<TradeRecord> savedTrades = tradeRecordRepository.saveAll(allTradeRecords);

        log.info("더미 리밸런싱 히스토리 생성 완료 - RebalancingOrders: {}, TradeRecords: {}",
                savedOrders.size(), savedTrades.size());

        return DummyDataResponse.success(savedOrders.size(), savedTrades.size());
    }

    private List<TradeRecord> createDummyTradeRecords() {
        List<TradeRecord> tradeRecords = new ArrayList<>();
        int tradeCount = 2 + random.nextInt(4); // 2~5개

        for (int i = 0; i < tradeCount; i++) {
            String[] stockInfo = STOCK_DATA[random.nextInt(STOCK_DATA.length)];
            String stockCode = stockInfo[0];
            String stockName = stockInfo[1];
            int minPrice = Integer.parseInt(stockInfo[2]);
            int maxPrice = Integer.parseInt(stockInfo[3]);

            String tradeType = TRADE_TYPES[random.nextInt(TRADE_TYPES.length)];
            int executedShares = 1 + random.nextInt(50); // 1~50주
            Long executedPrice = (long)(minPrice + random.nextInt(maxPrice - minPrice));
            Long totalAmount = executedPrice * executedShares;

            TradeRecord tradeRecord = TradeRecord.builder()
                    .stockCode(stockCode)
                    .stockName(stockName)
                    .tradeType(tradeType)
                    .tradeDate(generateRandomTradeDate())
                    .executedShares(executedShares)
                    .executedPrice(executedPrice)
                    .totalAmount(totalAmount)
                    .status(TransactionStatus.COMPLETED)
                    .orderNumber("ORD" + System.currentTimeMillis() + i)
                    .reason(tradeType.equals("BUY") ? "목표 비중 미달로 인한 매수" : "목표 비중 초과로 인한 매도")
                    .build();

            tradeRecords.add(tradeRecord);
        }

        return tradeRecords;
    }

    private RebalancingOrder createDummyRebalancingOrder(Portfolio portfolio,
            List<TradeRecord> tradeRecords, LocalDateTime rebalancingDate, Double cumulativeReturn) {

        Long totalBuyAmount = tradeRecords.stream()
                .filter(trade -> "BUY".equals(trade.getTradeType()))
                .mapToLong(TradeRecord::getTotalAmount)
                .sum();

        Long totalSellAmount = tradeRecords.stream()
                .filter(trade -> "SELL".equals(trade.getTradeType()))
                .mapToLong(TradeRecord::getTotalAmount)
                .sum();

        ExecutionType executionType = EXECUTION_TYPES[random.nextInt(EXECUTION_TYPES.length)];
        TransactionStatus status = STATUSES[random.nextInt(STATUSES.length)];

        // 포트폴리오 총 평가액 계산 (수익률 기반으로 계산)
        Long baseAmount = 10000000L; // 초기 1000만원 기준
        Long totalPortfolioValue = (long)(baseAmount * cumulativeReturn);

        return RebalancingOrder.builder()
                .portfolio(portfolio)
                .totalBuyAmount(totalBuyAmount)
                .totalSellAmount(totalSellAmount)
                .rebalancingDate(rebalancingDate)
                .status(status)
                .executionType(executionType)
                .cumulativeReturn(cumulativeReturn)
                .totalPortfolioValue(totalPortfolioValue)
                .build();
    }

    private LocalDateTime generateRandomRebalancingDate(int index, int total) {
        // 최근 3개월 내에서 순서대로 분배
        LocalDateTime now = LocalDateTime.now();
        long daysBack = (long) (90.0 * (total - index) / total); // 90일 전부터 현재까지 분배
        return now.minusDays(daysBack).minusHours(random.nextInt(12) + 9); // 9~21시 사이
    }

    private LocalDateTime generateRandomTradeDate() {
        LocalDateTime now = LocalDateTime.now();
        // 평일 장중 시간대 (9:00~15:30)
        return now.minusDays(random.nextInt(90))
                .withHour(9 + random.nextInt(7))
                .withMinute(random.nextInt(60))
                .withSecond(random.nextInt(60));
    }

    public UpdateCreatedAtResponse updatePortfolioCreatedAt(Long userId, Long portfolioId, LocalDateTime createdAt) {
        log.info("포트폴리오 생성일시 업데이트 요청 - userId: {}, portfolioId: {}, createdAt: {}", userId, portfolioId, createdAt);

        // 포트폴리오 존재 및 소유권 확인
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new CustomRuntimeException(ExceptionCode.ACCESS_DENIED);
        }

        // 포트폴리오 생성일시 업데이트
        int updatedCount = portfolioRepository.updateCreatedAtById(portfolioId, createdAt);

        if (updatedCount == 0) {
            throw new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND);
        }

        log.info("포트폴리오 생성일시 업데이트 완료 - portfolioId: {}, updatedCount: {}", portfolioId, updatedCount);

        return UpdateCreatedAtResponse.success(portfolioId, createdAt);
    }
}