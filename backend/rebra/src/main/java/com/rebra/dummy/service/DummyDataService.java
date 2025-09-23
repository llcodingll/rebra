package com.rebra.dummy.service;

import com.rebra.dummy.dto.DummyDataResponse;
import com.rebra.dummy.dto.UpdateCreatedAtResponse;
import com.rebra.entity.PerformanceMetrics;
import com.rebra.entity.Portfolio;
import com.rebra.entity.RebalancingOrder;
import com.rebra.entity.TradeRecord;
import com.rebra.enums.ExecutionType;
import com.rebra.enums.TransactionStatus;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.repository.PerformanceMetricsRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.RebalancingOrderRepository;
import com.rebra.repository.TradeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DummyDataService {

    private final PerformanceMetricsRepository performanceMetricsRepository;
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

    public DummyDataResponse createDummyPerformanceMetrics(Long userId, Long portfolioId) {
        log.info("더미 성과 메트릭 생성 시작 - userId: {}, portfolioId: {}", userId, portfolioId);

        // 포트폴리오 검증 및 소유권 확인
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new CustomRuntimeException(ExceptionCode.ACCESS_DENIED);
        }

        // 포트폴리오 생성일부터 오늘까지 날짜 범위 계산
        LocalDate startDate = portfolio.getCreatedAt().toLocalDate();
        LocalDate endDate = LocalDate.now();

        log.info("성과 메트릭 생성 기간 - 시작일: {}, 종료일: {}", startDate, endDate);

        // 매일 PerformanceMetrics 생성
        List<PerformanceMetrics> metricsData = new ArrayList<>();
        LocalDate currentDate = startDate;
        double baseValue = 10_000_000.0; // 초기 1000만원

        while (!currentDate.isAfter(endDate)) {
            PerformanceMetrics metrics = createDummyMetricsForDate(portfolio, currentDate, baseValue);
            metricsData.add(metrics);

            // 다음 날로 이동, 총 가치 점진적 변화
            currentDate = currentDate.plusDays(1);
            baseValue = calculateNextDayValue(baseValue, currentDate);
        }

        // 데이터베이스 저장
        List<PerformanceMetrics> savedMetrics = performanceMetricsRepository.saveAll(metricsData);

        log.info("더미 성과 메트릭 생성 완료 - PerformanceMetrics: {}개, 기간: {}일",
                savedMetrics.size(), savedMetrics.size());

        return DummyDataResponse.success(savedMetrics.size(), 0);
    }

    private PerformanceMetrics createDummyMetricsForDate(Portfolio portfolio, LocalDate date, double totalValue) {
        // 1. ExecutionType 결정
        ExecutionType executionType = determineExecutionType(date);

        RebalancingOrder rebalancingOrder = null;
        List<TradeRecord> tradeRecords = new ArrayList<>();

        if (executionType != null) {
            // 2. RebalancingOrder를 먼저 생성 (TradeRecord 없이)
            rebalancingOrder = createDummyRebalancingOrder(
                    portfolio, new ArrayList<>(), date, totalValue, executionType);

            // 3. DB에 저장하여 ID 생성
            rebalancingOrder = rebalancingOrderRepository.save(rebalancingOrder);

            // 4. ExecutionType에 맞는 TradeRecord 생성 (RebalancingOrder와 연결)
            tradeRecords = createTradeRecordsByTypeWithOrder(executionType, rebalancingOrder);

            // 5. TradeRecord 저장
            tradeRecordRepository.saveAll(tradeRecords);
        }

        // 6. PerformanceMetrics 생성 (실제 거래 내용 반영)
        return PerformanceMetrics.builder()
                .portfolio(portfolio)
                .metricDate(date)
                .totalValue(totalValue)
                .isRebalanced(isRebalancingType(executionType))
                .isSold(hasSellTrades(tradeRecords))
                .isBought(hasBuyTrades(tradeRecords))
                .isCompositionChanged(isRebalancingType(executionType))
                .build();
    }

    private double calculateNextDayValue(double currentValue, LocalDate date) {
        // 주말이면 값 변동 없음
        if (date.getDayOfWeek().getValue() >= 6) { // 토요일(6), 일요일(7)
            return currentValue;
        }

        // 일반적인 시장 변동성 시뮬레이션
        // 연간 변동성 15% 기준으로 일일 변동성 계산 (15% / sqrt(252))
        double annualVolatility = 0.15;
        double dailyVolatility = annualVolatility / Math.sqrt(252);

        // 정규분포를 근사한 랜덤 변동 (-2sigma ~ +2sigma)
        double randomChange = (random.nextGaussian() * dailyVolatility);

        // 장기적으로 연간 7% 성장하도록 약간의 상승 편향 추가
        double annualGrowth = 0.07;
        double dailyGrowth = Math.pow(1 + annualGrowth, 1.0/252) - 1;

        // 최종 변동률 계산
        double totalChange = dailyGrowth + randomChange;

        // 새로운 포트폴리오 가치 계산
        double newValue = currentValue * (1 + totalChange);

        // 최소값 제한 (원금의 50% 이하로는 떨어지지 않도록)
        double minValue = 10_000_000.0 * 0.5;
        return Math.max(newValue, minValue);
    }

    private boolean shouldRebalance(LocalDate date) {
        // 월초 리밸런싱 확률 높임 (매월 1일~5일)
        if (date.getDayOfMonth() <= 5) {
            return random.nextDouble() < 0.15; // 15% 확률
        }

        // 분기 말 리밸런싱 확률 높임 (3,6,9,12월 마지막 주)
        int month = date.getMonthValue();
        if ((month == 3 || month == 6 || month == 9 || month == 12) && date.getDayOfMonth() > 25) {
            return random.nextDouble() < 0.25; // 25% 확률
        }

        // 일반적인 날에는 낮은 확률
        return random.nextDouble() < 0.03; // 3% 확률
    }

    private boolean shouldTrade(LocalDate date) {
        // 주말이면 거래 없음
        if (date.getDayOfWeek().getValue() >= 6) {
            return false;
        }

        // 월요일과 금요일에 거래 확률 높임
        int dayOfWeek = date.getDayOfWeek().getValue();
        if (dayOfWeek == 1 || dayOfWeek == 5) { // 월요일(1), 금요일(5)
            return random.nextDouble() < 0.20; // 20% 확률
        }

        // 일반적인 평일
        return random.nextDouble() < 0.10; // 10% 확률
    }

    private ExecutionType determineExecutionType(LocalDate date) {
        // 월초/분기말: 리밸런싱 확률 높음
        if (shouldRebalance(date)) {
            return random.nextBoolean() ? ExecutionType.AUTO : ExecutionType.MANUAL;
        }

        // 평상시: 개인 거래 확률
        if (shouldTrade(date)) {
            double randomValue = random.nextDouble();
            if (randomValue < 0.4) return ExecutionType.BUY_PERSONAL;   // 40%
            else if (randomValue < 0.7) return ExecutionType.SELL_PERSONAL; // 30%
            else return ExecutionType.MANUAL; // 30%
        }

        return null; // 거래 없음
    }

    private List<TradeRecord> createTradeRecordsByType(ExecutionType executionType) {
        switch (executionType) {
            case AUTO:
            case MANUAL:
                return createRebalancingTrades(); // 3~6개 (매수/매도 혼합)

            case BUY_PERSONAL:
                return createSingleBuyTrade();    // 1개 (BUY만)

            case SELL_PERSONAL:
                return createSingleSellTrade();   // 1개 (SELL만)

            default:
                return new ArrayList<>();
        }
    }

    private List<TradeRecord> createRebalancingTrades() {
        List<TradeRecord> trades = new ArrayList<>();
        int tradeCount = 3 + random.nextInt(4); // 3~6개

        for (int i = 0; i < tradeCount; i++) {
            String[] stockInfo = STOCK_DATA[random.nextInt(STOCK_DATA.length)];
            String tradeType = TRADE_TYPES[random.nextInt(TRADE_TYPES.length)]; // BUY/SELL 랜덤
            int executedShares = 5 + random.nextInt(45); // 5~50주
            Long executedPrice = Long.parseLong(stockInfo[2]) + random.nextInt(10000);
            Long totalAmount = executedPrice * executedShares;

            TradeRecord trade = TradeRecord.builder()
                    .stockCode(stockInfo[0])
                    .stockName(stockInfo[1])
                    .tradeType(tradeType)
                    .tradeDate(generateRandomTradeTime())
                    .executedShares(executedShares)
                    .executedPrice(executedPrice)
                    .totalAmount(totalAmount)
                    .status(TransactionStatus.COMPLETED)
                    .orderNumber("REB" + System.currentTimeMillis() + "_" + i)
                    .reason("리밸런싱에 의한 " + (tradeType.equals("BUY") ? "매수" : "매도"))
                    .build();

            trades.add(trade);
        }
        return trades;
    }

    private List<TradeRecord> createSingleBuyTrade() {
        String[] stockInfo = STOCK_DATA[random.nextInt(STOCK_DATA.length)];
        int executedShares = 1 + random.nextInt(30); // 1~30주
        Long executedPrice = Long.parseLong(stockInfo[2]) + random.nextInt(5000);
        Long totalAmount = executedPrice * executedShares;

        TradeRecord trade = TradeRecord.builder()
                .stockCode(stockInfo[0])
                .stockName(stockInfo[1])
                .tradeType("BUY")
                .tradeDate(generateRandomTradeTime())
                .executedShares(executedShares)
                .executedPrice(executedPrice)
                .totalAmount(totalAmount)
                .status(TransactionStatus.COMPLETED)
                .orderNumber("BUY" + System.currentTimeMillis())
                .reason("개인 추가 매수")
                .build();

        return List.of(trade);
    }

    private List<TradeRecord> createSingleSellTrade() {
        String[] stockInfo = STOCK_DATA[random.nextInt(STOCK_DATA.length)];
        int executedShares = 1 + random.nextInt(20); // 1~20주
        Long executedPrice = Long.parseLong(stockInfo[2]) + random.nextInt(5000);
        Long totalAmount = executedPrice * executedShares;

        TradeRecord trade = TradeRecord.builder()
                .stockCode(stockInfo[0])
                .stockName(stockInfo[1])
                .tradeType("SELL")
                .tradeDate(generateRandomTradeTime())
                .executedShares(executedShares)
                .executedPrice(executedPrice)
                .totalAmount(totalAmount)
                .status(TransactionStatus.COMPLETED)
                .orderNumber("SELL" + System.currentTimeMillis())
                .reason("개인 부분 매도")
                .build();

        return List.of(trade);
    }

    private LocalDateTime generateRandomTradeTime() {
        LocalDateTime now = LocalDateTime.now();
        // 평일 장중 시간대 (9:00~15:30)
        return now.minusDays(random.nextInt(90))
                .withHour(9 + random.nextInt(7))
                .withMinute(random.nextInt(60))
                .withSecond(random.nextInt(60));
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
            List<TradeRecord> tradeRecords, LocalDate date, double totalValue, ExecutionType executionType) {

        Long totalBuyAmount = tradeRecords.stream()
                .filter(trade -> "BUY".equals(trade.getTradeType()))
                .mapToLong(TradeRecord::getTotalAmount)
                .sum();

        Long totalSellAmount = tradeRecords.stream()
                .filter(trade -> "SELL".equals(trade.getTradeType()))
                .mapToLong(TradeRecord::getTotalAmount)
                .sum();

        TransactionStatus status = STATUSES[random.nextInt(STATUSES.length)];

        // 포트폴리오 총 평가액과 수익률 계산
        double cumulativeReturn = totalValue / 10_000_000.0;
        Long totalPortfolioValue = (long) totalValue;

        return RebalancingOrder.builder()
                .portfolio(portfolio)
                .totalBuyAmount(totalBuyAmount)
                .totalSellAmount(totalSellAmount)
                .rebalancingDate(date.atTime(9 + random.nextInt(7), random.nextInt(60)))
                .status(status)
                .executionType(executionType)
                .cumulativeReturn(cumulativeReturn)
                .totalPortfolioValue(totalPortfolioValue)
                .build();
    }

    private boolean isRebalancingType(ExecutionType executionType) {
        return executionType == ExecutionType.AUTO || executionType == ExecutionType.MANUAL;
    }

    private boolean hasSellTrades(List<TradeRecord> tradeRecords) {
        return tradeRecords.stream().anyMatch(tr -> "SELL".equals(tr.getTradeType()));
    }

    private boolean hasBuyTrades(List<TradeRecord> tradeRecords) {
        return tradeRecords.stream().anyMatch(tr -> "BUY".equals(tr.getTradeType()));
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

    /**
     * ExecutionType에 맞는 TradeRecord를 생성하되, RebalancingOrder와 연결
     */
    private List<TradeRecord> createTradeRecordsByTypeWithOrder(ExecutionType executionType, RebalancingOrder rebalancingOrder) {
        List<TradeRecord> tradeRecords = new ArrayList<>();

        switch (executionType) {
            case AUTO:
            case MANUAL:
                // 리밸런싱: 3~6개의 매수/매도 혼합 거래
                tradeRecords = createRebalancingTradesWithOrder(rebalancingOrder);
                break;
            case BUY_PERSONAL:
                // 개인 매수: 1개의 매수 거래
                tradeRecords = createSingleBuyTradeWithOrder(rebalancingOrder);
                break;
            case SELL_PERSONAL:
                // 개인 매도: 1개의 매도 거래
                tradeRecords = createSingleSellTradeWithOrder(rebalancingOrder);
                break;
        }

        return tradeRecords;
    }

    /**
     * 리밸런싱용 다중 TradeRecord 생성 (RebalancingOrder 연결)
     */
    private List<TradeRecord> createRebalancingTradesWithOrder(RebalancingOrder rebalancingOrder) {
        List<TradeRecord> trades = new ArrayList<>();
        int tradeCount = 3 + random.nextInt(4); // 3~6개

        for (int i = 0; i < tradeCount; i++) {
            String[] stockInfo = STOCK_DATA[random.nextInt(STOCK_DATA.length)];
            String tradeType = (i % 2 == 0) ? "BUY" : "SELL"; // 매수/매도 번갈아
            int executedShares = 5 + random.nextInt(25); // 5~30주
            Long executedPrice = Long.parseLong(stockInfo[2]) + random.nextInt(10000);
            Long totalAmount = executedPrice * executedShares;

            TradeRecord trade = TradeRecord.builder()
                    .rebalancingOrder(rebalancingOrder)
                    .stockCode(stockInfo[0])
                    .stockName(stockInfo[1])
                    .tradeType(tradeType)
                    .tradeDate(generateRandomTradeTime())
                    .executedShares(executedShares)
                    .executedPrice(executedPrice)
                    .totalAmount(totalAmount)
                    .status(TransactionStatus.COMPLETED)
                    .orderNumber(tradeType + System.currentTimeMillis() + "_" + i)
                    .reason("자동 리밸런싱")
                    .build();

            trades.add(trade);
        }

        return trades;
    }

    /**
     * 개인 매수용 단일 TradeRecord 생성 (RebalancingOrder 연결)
     */
    private List<TradeRecord> createSingleBuyTradeWithOrder(RebalancingOrder rebalancingOrder) {
        String[] stockInfo = STOCK_DATA[random.nextInt(STOCK_DATA.length)];
        int executedShares = 1 + random.nextInt(30); // 1~30주
        Long executedPrice = Long.parseLong(stockInfo[2]) + random.nextInt(5000);
        Long totalAmount = executedPrice * executedShares;

        TradeRecord trade = TradeRecord.builder()
                .rebalancingOrder(rebalancingOrder)
                .stockCode(stockInfo[0])
                .stockName(stockInfo[1])
                .tradeType("BUY")
                .tradeDate(generateRandomTradeTime())
                .executedShares(executedShares)
                .executedPrice(executedPrice)
                .totalAmount(totalAmount)
                .status(TransactionStatus.COMPLETED)
                .orderNumber("BUY" + System.currentTimeMillis())
                .reason("개인 추가 매수")
                .build();

        return List.of(trade);
    }

    /**
     * 개인 매도용 단일 TradeRecord 생성 (RebalancingOrder 연결)
     */
    private List<TradeRecord> createSingleSellTradeWithOrder(RebalancingOrder rebalancingOrder) {
        String[] stockInfo = STOCK_DATA[random.nextInt(STOCK_DATA.length)];
        int executedShares = 1 + random.nextInt(20); // 1~20주
        Long executedPrice = Long.parseLong(stockInfo[2]) - random.nextInt(3000); // 매도는 시세보다 낮게
        Long totalAmount = executedPrice * executedShares;

        TradeRecord trade = TradeRecord.builder()
                .rebalancingOrder(rebalancingOrder)
                .stockCode(stockInfo[0])
                .stockName(stockInfo[1])
                .tradeType("SELL")
                .tradeDate(generateRandomTradeTime())
                .executedShares(executedShares)
                .executedPrice(executedPrice)
                .totalAmount(totalAmount)
                .status(TransactionStatus.COMPLETED)
                .orderNumber("SELL" + System.currentTimeMillis())
                .reason("개인 부분 매도")
                .build();

        return List.of(trade);
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