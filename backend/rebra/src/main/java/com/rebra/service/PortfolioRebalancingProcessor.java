package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.dto.HoldingInfo;
import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.RebalancingExecutionResponse;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.entity.*;
import com.rebra.enums.ExecutionType;
import com.rebra.enums.TransactionStatus;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.PortfolioStockRepository;
import com.rebra.repository.RebalancingOrderRepository;
import com.rebra.repository.TradeRecordRepository;
import com.rebra.util.AccountEncryptionUtil;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 개별 포트폴리오 리밸런싱 처리를 담당하는 서비스
 * 트랜잭션 경계를 명확히 하기 위해 분리됨
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioRebalancingProcessor {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockRepository portfolioStockRepository;
    private final RebalancingOrderRepository rebalancingOrderRepository;
    private final TradeRecordRepository tradeRecordRepository;
    private final AccountRepository accountRepository;
    private final KisApiComponent kisApiComponent;
    private final StockTradingService stockTradingService;

    /**
     * 개별 포트폴리오 리밸런싱을 트랜잭션 내에서 처리
     * 
     * @param portfolioId 포트폴리오 ID
     * @param today 오늘 날짜
     */
    @Transactional
    public void processPortfolio(Long portfolioId, LocalDate today) {
        Portfolio portfolio = portfolioRepository.findByIdWithPortfolioStocks(portfolioId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다: " + portfolioId));

        log.debug("포트폴리오 {} 리밸런싱 처리 시작 - 전략: {}", 
            portfolio.getId(), portfolio.getRebalancingStrategy());
        
        if (portfolio.getRebalancingStrategy() == RebalancingStrategy.THRESHOLD) {
            processThresholdRebalancing(portfolio, today);
        } else if (portfolio.getRebalancingStrategy() == RebalancingStrategy.PERIODIC) {
            processPeriodicRebalancing(portfolio, today);
        } else {
            log.warn("지원하지 않는 리밸런싱 전략: {} (포트폴리오 {})", 
                portfolio.getRebalancingStrategy(), portfolio.getId());
        }
    }

    /**
     * 임계값 기반 리밸런싱을 처리한다
     * RebalancingCalculation 생성 → 임계값 판단 → 필요시 리밸런싱 실행
     * 
     * @param portfolio 포트폴리오
     * @param today 오늘 날짜
     */
    private void processThresholdRebalancing(Portfolio portfolio, LocalDate today) {
        log.debug("포트폴리오 {} 임계값 기반 리밸런싱 체크 시작", portfolio.getId());
        
        // 1. RebalancingCalculation 생성
        RebalancingCalculation calc = calculateRebalancing(portfolio);
        
        // 2. 임계값 판단
        if (checkThresholdRebalancing(calc)) {
            log.info("포트폴리오 {} 임계값 초과, 리밸런싱 실행", portfolio.getId());
            
            // 3. 리밸런싱 실행
            RebalancingExecutionResponse result = doRebalancing(calc, portfolio, ExecutionType.AUTO);
            
            if (result.isSuccess()) {
                portfolio.updateLastRebalanceDate(today);
                log.info("포트폴리오 {} 임계값 리밸런싱 완료", portfolio.getId());
            } else {
                log.error("포트폴리오 {} 임계값 리밸런싱 실패: {}", portfolio.getId(), result.getFailureReason());
            }
        } else {
            log.debug("포트폴리오 {} 임계값 미달, 리밸런싱 건너뜀", portfolio.getId());
        }
        
        // 다음날로 설정 (매일 체크)
        portfolio.updateNextRebalanceDate(today.plusDays(1));
        portfolioRepository.save(portfolio);
    }

    /**
     * 주기 기반 리밸런싱을 처리한다
     * RebalancingCalculation 생성 → 즉시 리밸런싱 실행 (주기 도래했으므로 판단 불필요)
     * 
     * @param portfolio 포트폴리오
     * @param today 오늘 날짜
     */
    private void processPeriodicRebalancing(Portfolio portfolio, LocalDate today) {
        log.info("포트폴리오 {} 주기 기반 리밸런싱 실행", portfolio.getId());
        
        // 1. RebalancingCalculation 생성
        RebalancingCalculation calc = calculateRebalancing(portfolio);
        
        // 2. 즉시 리밸런싱 실행 (주기 도래했으므로 판단 불필요)
        RebalancingExecutionResponse result = doRebalancing(calc, portfolio, ExecutionType.AUTO);
        
        if (result.isSuccess()) {
            portfolio.updateLastRebalanceDate(today);
            
            // rebalancingStartDate 기준으로 다음 주기 계산
            LocalDate nextDate = calculateNextPeriodicDate(portfolio, today);
            portfolio.updateNextRebalanceDate(nextDate);
            
            log.info("포트폴리오 {} 주기 리밸런싱 완료, 다음 예정일: {}", 
                portfolio.getId(), nextDate);
        } else {
            log.error("포트폴리오 {} 주기 리밸런싱 실패: {}", portfolio.getId(), result.getFailureReason());
            
            // 실패해도 다음 날짜는 업데이트 (무한 재시도 방지)
            LocalDate nextDate = calculateNextPeriodicDate(portfolio, today);
            portfolio.updateNextRebalanceDate(nextDate);
        }
        
        portfolioRepository.save(portfolio);
    }

    // ==================== 비즈니스 로직 메서드들 ====================

    /**
     * 임계값 기반 리밸런싱 필요 여부를 판단한다
     * 
     * @param calculation 계산된 리밸런싱 정보
     * @return 임계값을 초과하는 종목이 있으면 true
     */
    private boolean checkThresholdRebalancing(RebalancingCalculation calculation) {
        if (calculation.getStockDetails().isEmpty()) {
            log.debug("목표 종목이 설정되지 않아 리밸런싱 불필요");
            return false;
        }
        
        boolean needsRebalancing = false;
        
        for (RebalancingCalculation.StockRebalancingDetail detail : calculation.getStockDetails()) {
            if (detail.isNeedsRebalancing()) {
                log.debug("종목 {} 리밸런싱 필요 - 현재비중: {}%, 목표비중: {}%, " +
                        "상대임계값: {}%, 편차: {}%",
                        detail.getStockCode(),
                        detail.getCurrentWeightAsPercent(),
                        detail.getNormalizedTargetWeightAsPercent(),
                        detail.getRelativeThresholdAsPercent(),
                        detail.getWeightDifferenceAsPercent());
                needsRebalancing = true;
            }
        }
        
        if (!needsRebalancing) {
            log.debug("모든 종목이 임계값 이내");
        }
        
        return needsRebalancing;
    }

    /**
     * 모든 리밸런싱 정보를 계산한다
     * 순수하게 계산만 수행하며, 리밸런싱 필요 여부 판단은 별도 메서드에서 처리
     */
    public RebalancingCalculation calculateRebalancing(Portfolio portfolio) {
        // 1. 기본 정보 조회
        Map<String, HoldingInfo> currentHoldings = getCurrentHoldings(portfolio);
        List<PortfolioStock> targetStocks = portfolio.getPortfolioStocks();
        
        if (targetStocks.isEmpty()) {
            return RebalancingCalculation.builder()
                    .currentHoldings(currentHoldings)
                    .targetStocks(targetStocks)
                    .totalPortfolioValue(calculateManagedTotalValue(currentHoldings, targetStocks))
                    .totalTargetWeight(0.0)
                    .stockDetails(List.of())
                    .build();
        }
        
        // 2. 관리 종목의 전체 값 계산
        long totalValue = calculateManagedTotalValue(currentHoldings, targetStocks);
        double totalTargetWeight = calculateTotalTargetWeight(targetStocks);
        
        // 3. 종목별 상세 계산
        List<RebalancingCalculation.StockRebalancingDetail> stockDetails = new ArrayList<>();
        
        for (PortfolioStock targetStock : targetStocks) {
            String stockCode = targetStock.getStockCode();
            
            // 정규화된 목표 비중 계산
            double normalizedTargetWeight = calculateNormalizedTargetWeight(targetStock, totalTargetWeight);
            
            // 현재 보유 정보
            HoldingInfo holding = currentHoldings.get(stockCode);
            double currentWeight = 0.0;
            long currentValue = 0;
            int currentQuantity = 0;
            long currentPrice = 0;
            
            if (holding != null) {
                currentValue = holding.getCurrentPrice() * holding.getQuantity();
                currentWeight = totalValue > 0 ? 
                        (double) currentValue / totalValue : 
                        0.0;
                currentQuantity = holding.getQuantity();
                currentPrice = holding.getCurrentPrice();
            }
            
            // 목표 가치 계산
            long targetValue = Math.round(totalValue * normalizedTargetWeight);
            
            // 비중 차이 계산
            double weightDifference = Math.abs(currentWeight - normalizedTargetWeight);
            double threshold = targetStock.getThresholdPercentage().doubleValue();
            
            // 상세 정보 저장
            stockDetails.add(RebalancingCalculation.StockRebalancingDetail.builder()
                    .stockCode(stockCode)
                    .normalizedTargetWeight(normalizedTargetWeight)
                    .currentWeight(currentWeight)
                    .weightDifference(weightDifference)
                    .threshold(threshold)
                    .currentQuantity(currentQuantity)
                    .currentPrice(currentPrice)
                    .currentValue(currentValue)
                    .targetValue(targetValue)
                    .build());
        }
        
        return RebalancingCalculation.builder()
                .currentHoldings(currentHoldings)
                .targetStocks(targetStocks)
                .totalPortfolioValue(totalValue)
                .totalTargetWeight(totalTargetWeight)
                .stockDetails(stockDetails)
                .build();
    }

    private Map<String, HoldingInfo> getCurrentHoldings(Portfolio portfolio) {
        try {
            Account account = portfolio.getAccount();
            DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, portfolio.getUser().getId());
            
            InquireBalanceResult balanceResult = kisApiComponent.getUserBalance(
                    portfolio.getUser().getId(),
                    account.getId(),
                    account.getAccountType(),
                    credentials
            );

            Map<String, HoldingInfo> holdings = new HashMap<>();
            
            if (balanceResult.getOutput1() != null) {
                for (InquireBalanceResult.Output1 holding : balanceResult.getOutput1()) {
                    String stockCode = holding.getPdno(); // 종목코드
                    String stockName = holding.getPrdtName(); // 종목명
                    int quantity = Integer.parseInt(holding.getHldgQty()); // 보유수량
                    long currentPrice = Long.parseLong(holding.getPrpr()); // 현재가
                    
                    if (quantity > 0) {
                        holdings.put(stockCode, new HoldingInfo(stockCode, stockName, quantity, currentPrice));
                    }
                }
            }

            return holdings;
        } catch (Exception e) {
            log.error("현재 보유 종목 조회 실패: portfolioId={}", portfolio.getId(), e);
            throw new RuntimeException("현재 보유 종목 조회 실패", e);
        }
    }

    private long calculateTotalValue(Map<String, HoldingInfo> holdings) {
        return holdings.values().stream()
                .mapToLong(holding -> holding.getCurrentPrice() * holding.getQuantity())
                .sum();
    }

    /**
     * 포트폴리오가 관리하는 종목만의 총 가치를 계산한다
     * 
     * @param holdings 현재 보유 종목 정보
     * @param targetStocks 포트폴리오가 관리하는 목표 종목 리스트
     * @return 관리 종목만의 총 가치
     */
    private long calculateManagedTotalValue(Map<String, HoldingInfo> holdings, List<PortfolioStock> targetStocks) {
        Set<String> managedStockCodes = targetStocks.stream()
                .map(PortfolioStock::getStockCode)
                .collect(Collectors.toSet());
        
        return holdings.entrySet().stream()
                .filter(entry -> managedStockCodes.contains(entry.getKey()))
                .mapToLong(entry -> {
                    HoldingInfo holding = entry.getValue();
                    return holding.getCurrentPrice() * holding.getQuantity();
                })
                .sum();
    }

    private double calculateTotalTargetWeight(List<PortfolioStock> targetStocks) {
        return targetStocks.stream()
                .mapToDouble(stock -> stock.getTargetWeight().doubleValue())
                .sum();
    }

    private double calculateNormalizedTargetWeight(PortfolioStock targetStock, double totalTargetWeight) {
        if (totalTargetWeight <= 0) {
            return 0.0;
        }
        return targetStock.getTargetWeight().doubleValue() / totalTargetWeight;
    }

    /**
     * 실제 리밸런싱을 실행하는 공통 메서드
     */
    public RebalancingExecutionResponse doRebalancing(RebalancingCalculation calculation, 
                                                      Portfolio portfolio, 
                                                      ExecutionType executionType) {
        
        List<RebalancingOrderData> orders = createRebalancingOrders(calculation);
        
        if (orders.isEmpty()) {
            return RebalancingExecutionResponse.failure("리밸런싱할 주문이 없습니다");
        }
        
        // 일관된 시간 사용을 위해 한 번만 호출
        LocalDateTime executionTime = LocalDateTime.now();
        
        // 주문 실행 및 TradeRecord 생성
        List<RebalancingExecutionResponse.OrderResult> orderResults = new ArrayList<>();
        List<TradeRecord> tradeRecords = new ArrayList<>();
        long totalBuyAmount = 0L;
        long totalSellAmount = 0L;
        long totalPortfolioValue = calculation.getTotalPortfolioValue();
        
        String reason = executionType == ExecutionType.AUTO ? "자동 리밸런싱" : "수동 리밸런싱";

        for (RebalancingOrderData order : orders) {
            try {
                RebalancingExecutionResponse.OrderResult result = executeOrder(order, portfolio, executionType);
                orderResults.add(result);

                if (result.isSuccess()) {
                    // 매수/매도 금액 계산
                    if ("BUY".equals(result.getOrderType())) {
                        totalBuyAmount += result.getPrice() * result.getQuantity();
                    } else {
                        totalSellAmount += result.getPrice() * result.getQuantity();
                    }
                }
            } catch (Exception e) {
                log.error("종목 {} 주문 실행 실패", order.getStockCode(), e);
                orderResults.add(RebalancingExecutionResponse.OrderResult.builder()
                        .stockCode(order.getStockCode())
                        .orderType(order.getOrderType())
                        .quantity(order.getQuantity())
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        // RebalancingOrder 엔티티 생성 및 저장
        RebalancingOrder rebalancingOrder = RebalancingOrder.builder()
                .portfolio(portfolio)
                .totalBuyAmount(totalBuyAmount)
                .totalSellAmount(totalSellAmount)
                .rebalancingDate(executionTime)
                .status(TransactionStatus.COMPLETED)
                .executionType(executionType)
                .totalPortfolioValue(totalPortfolioValue)
                .build();

        rebalancingOrder = rebalancingOrderRepository.save(rebalancingOrder);

        // 성공한 주문에 대해 TradeRecord 생성
        for (int i = 0; i < orders.size(); i++) {
            RebalancingOrderData order = orders.get(i);
            RebalancingExecutionResponse.OrderResult result = orderResults.get(i);
            
            if (result.isSuccess()) {
                TradeRecord tradeRecord = TradeRecord.builder()
                        .rebalancingOrder(rebalancingOrder)
                        .stockCode(order.getStockCode())
                        .stockName(order.getStockName())
                        .tradeType(order.getOrderType())
                        .tradeDate(executionTime)
                        .executedShares(result.getQuantity())
                        .executedPrice(result.getPrice())
                        .totalAmount(result.getPrice() * result.getQuantity())
                        .status(TransactionStatus.COMPLETED)
                        .orderNumber(result.getOrderId())
                        .reason(reason)
                        .build();
                
                tradeRecords.add(tradeRecord);
            }
        }
        
        // TradeRecord 일괄 저장
        if (!tradeRecords.isEmpty()) {
            tradeRecordRepository.saveAll(tradeRecords);
        }

        boolean allSuccess = orderResults.stream().allMatch(RebalancingExecutionResponse.OrderResult::isSuccess);
        
        return RebalancingExecutionResponse.builder()
                .success(allSuccess)
                .failureReason(allSuccess ? null : "일부 주문이 실패했습니다")
                .orderResults(orderResults)
                .totalBuyAmount(totalBuyAmount)
                .totalSellAmount(totalSellAmount)
                .executionTime(executionTime)
                .build();
    }

    private List<RebalancingOrderData> createRebalancingOrders(RebalancingCalculation calculation) {
        List<RebalancingOrderData> sellOrders = new ArrayList<>();
        List<RebalancingOrderData> buyOrders = new ArrayList<>();
        
        for (RebalancingCalculation.StockRebalancingDetail detail : calculation.getStockDetails()) {
            String stockCode = detail.getStockCode();
            long currentValue = detail.getCurrentValue();
            long targetValue = detail.getTargetValue();
            long currentPrice = detail.getCurrentPrice();
            int currentQuantity = detail.getCurrentQuantity();
            
            // 종목명 찾기 (보유 종목에서 먼저, 없으면 기본값)
            String stockName = calculation.getCurrentHoldings().get(stockCode) != null ? 
                calculation.getCurrentHoldings().get(stockCode).getStockName() : 
                stockCode; // 보유하지 않은 종목은 종목코드를 종목명으로 사용
            
            if (currentPrice <= 0) {
                log.warn("종목 {}의 현재 가격이 0 이하입니다: {}", stockCode, currentPrice);
                continue;
            }
            
            long valueDifference = targetValue - currentValue;
            
            if (valueDifference > 0) {
                // 매수 필요
                int buyQuantity = (int)(valueDifference / currentPrice);
                if (buyQuantity > 0) {
                    buyOrders.add(new RebalancingOrderData(stockCode, stockName, "BUY", buyQuantity, currentPrice));
                }
            } else if (valueDifference < 0) {
                // 매도 필요
                int sellQuantity = (int)(Math.abs(valueDifference) / currentPrice);
                sellQuantity = Math.min(sellQuantity, currentQuantity); // 보유 수량 초과 불가
                if (sellQuantity > 0) {
                    sellOrders.add(new RebalancingOrderData(stockCode, stockName, "SELL", sellQuantity, currentPrice));
                }
            }
        }
        
        // 매도 주문을 먼저, 매수 주문을 나중에 합침
        List<RebalancingOrderData> allOrders = new ArrayList<>();
        allOrders.addAll(sellOrders);
        allOrders.addAll(buyOrders);
        
        return allOrders;
    }

    private RebalancingExecutionResponse.OrderResult executeOrder(RebalancingOrderData order, Portfolio portfolio, ExecutionType executionType) {
        try {
            StockTradeRequest tradeRequest = new StockTradeRequest();
            tradeRequest.setOrderType("01"); // 시장가
            tradeRequest.setQuantity(order.getQuantity());
            tradeRequest.setPrice(0L); // 시장가
            tradeRequest.setAccountId(portfolio.getAccount().getId());

            StockTradeResponse response;
            if ("BUY".equals(order.getOrderType())) {
                response = stockTradingService.buyStock(order.getStockCode(), tradeRequest, portfolio.getUser().getId(), executionType);
            } else {
                response = stockTradingService.sellStock(order.getStockCode(), tradeRequest, portfolio.getUser().getId(), executionType);
            }

            return RebalancingExecutionResponse.OrderResult.builder()
                    .stockCode(order.getStockCode())
                    .orderType(order.getOrderType())
                    .quantity(order.getQuantity())
                    .price(order.getPrice())
                    .success(response.isSuccess())
                    .orderId(response.getOrderId())
                    .errorMessage(response.isSuccess() ? null : response.getMessage())
                    .build();

        } catch (Exception e) {
            log.error("주문 실행 실패: stockCode={}, orderType={}", order.getStockCode(), order.getOrderType(), e);
            throw e;
        }
    }

    /**
     * 주기 기반 리밸런싱의 다음 날짜를 계산한다
     * 이전 nextRebalanceDate를 기준으로 다음 주기를 계산하여
     * 공휴일로 인한 실행 지연과 무관하게 일정한 패턴을 유지한다
     */
    public LocalDate calculateNextPeriodicDate(Portfolio portfolio, LocalDate baseDate) {
        RebalancingPeriod period = portfolio.getRebalancingPeriod();
        LocalDate startDate = portfolio.getRebalancingStartDate();
        
        if (startDate == null) {
            log.warn("포트폴리오 {}의 rebalancingStartDate가 null입니다. 기본값 사용", portfolio.getId());
            return baseDate.plusDays(1);
        }
        
        if (period == RebalancingPeriod.MONTHLY) {
            // rebalancingStartDate에서 목표 일자 추출
            int targetDayOfMonth = startDate.getDayOfMonth();
            
            // 갱신 전 nextRebalanceDate 기준으로 계산 (실행 지연과 무관하게 패턴 유지)
            LocalDate previousNextDate = portfolio.getNextRebalanceDate();
            if (previousNextDate == null) {
                // 최초 설정 시 startDate 기준
                previousNextDate = startDate;
            }
            
            // 이전 예정일의 다음 달로 이동
            LocalDate nextMonth = previousNextDate.plusMonths(1);
            
            // 해당 월의 마지막 날보다 큰 경우 월말로 조정
            // 예: 31일 → 2월(28일)이면 28일로 조정
            int lastDayOfNextMonth = nextMonth.lengthOfMonth();
            int actualDay = Math.min(targetDayOfMonth, lastDayOfNextMonth);
            
            return nextMonth.withDayOfMonth(actualDay);
            
        } else if (period == RebalancingPeriod.YEARLY) {
            // rebalancingStartDate에서 월일 추출
            int targetMonth = startDate.getMonthValue();
            int targetDay = startDate.getDayOfMonth();
            
            // 갱신 전 nextRebalanceDate 기준으로 계산
            LocalDate previousNextDate = portfolio.getNextRebalanceDate();
            if (previousNextDate == null) {
                // 최초 설정 시 startDate 기준
                previousNextDate = startDate;
            }
            
            // 이전 예정일의 다음 년도로 이동
            LocalDate nextYear = previousNextDate.plusYears(1)
                .withMonth(targetMonth);
                
            // 윤년 처리 (예: 2월 29일 → 평년 2월 28일)
            int lastDayOfTargetMonth = nextYear.lengthOfMonth();
            int actualDay = Math.min(targetDay, lastDayOfTargetMonth);
            
            return nextYear.withDayOfMonth(actualDay);
        }
        
        // 기본값 (DAILY나 기타)
        log.warn("지원하지 않는 리밸런싱 주기: {}. 다음날로 설정", period);
        return baseDate.plusDays(1);
    }

    // ==================== 내부 클래스들 ====================
    
    private static class RebalancingOrderData {
        private final String stockCode;
        private final String stockName;
        private final String orderType;
        private final int quantity;
        private final long price;

        public RebalancingOrderData(String stockCode, String stockName, String orderType, int quantity, long price) {
            this.stockCode = stockCode;
            this.stockName = stockName;
            this.orderType = orderType;
            this.quantity = quantity;
            this.price = price;
        }

        public String getStockCode() { return stockCode; }
        public String getStockName() { return stockName; }
        public String getOrderType() { return orderType; }
        public int getQuantity() { return quantity; }
        public long getPrice() { return price; }
    }
}