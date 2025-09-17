package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.RebalancingCheckResponse;
import com.rebra.dto.response.RebalancingExecutionResponse;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.entity.*;
import com.rebra.enums.ExecutionType;
import com.rebra.enums.TransactionStatus;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.PortfolioStockRepository;
import com.rebra.repository.RebalancingOrderRepository;
import com.rebra.util.AccountEncryptionUtil;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioRebalancingServiceImpl implements PortfolioRebalancingService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockRepository portfolioStockRepository;
    private final RebalancingOrderRepository rebalancingOrderRepository;
    private final AccountRepository accountRepository;
    private final KisApiComponent kisApiComponent;
    private final StockTradingService stockTradingService;

    @Override
    @Transactional(readOnly = true)
    public RebalancingCheckResponse checkRebalancingNeeded(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다"));

        if (!portfolio.getAutoRebalancing()) {
            return RebalancingCheckResponse.notNeeded(
                    portfolio.getRebalancingStrategy().name(),
                    portfolio.getNextRebalanceDate()
            );
        }

        try {
            // 현재 보유 종목 및 가격 정보 조회
            Map<String, HoldingInfo> currentHoldings = getCurrentHoldings(portfolio);
            
            // 포트폴리오 목표 종목 정보 조회
            List<PortfolioStock> targetStocks = portfolioStockRepository.findByPortfolioId(portfolioId);
            
            // 리밸런싱 필요 여부 체크
            boolean needsRebalancing = false;
            List<RebalancingCheckResponse.StockRebalancingInfo> stockInfos = new ArrayList<>();
            
            if (portfolio.getRebalancingStrategy() == RebalancingStrategy.THRESHOLD) {
                needsRebalancing = checkThresholdRebalancing(currentHoldings, targetStocks, stockInfos);
            } else if (portfolio.getRebalancingStrategy() == RebalancingStrategy.PERIODIC) {
                needsRebalancing = checkPeriodicRebalancing(portfolio);
                if (needsRebalancing) {
                    calculateStockInfos(currentHoldings, targetStocks, stockInfos);
                }
            }

            if (needsRebalancing) {
                return RebalancingCheckResponse.needed(
                        portfolio.getRebalancingStrategy().name(),
                        getCheckReason(portfolio.getRebalancingStrategy()),
                        stockInfos
                );
            } else {
                return RebalancingCheckResponse.notNeeded(
                        portfolio.getRebalancingStrategy().name(),
                        portfolio.getNextRebalanceDate()
                );
            }

        } catch (Exception e) {
            log.error("리밸런싱 체크 중 오류 발생: portfolioId={}", portfolioId, e);
            return RebalancingCheckResponse.notNeeded(
                    portfolio.getRebalancingStrategy().name(),
                    portfolio.getNextRebalanceDate()
            );
        }
    }

    @Override
    @Transactional
    public RebalancingExecutionResponse executeAutoRebalancing(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다"));

        if (!portfolio.getAutoRebalancing()) {
            return RebalancingExecutionResponse.failure("자동 리밸런싱이 비활성화되어 있습니다");
        }

        return executeRebalancing(portfolio, ExecutionType.AUTO);
    }

    @Override
    @Transactional
    public RebalancingExecutionResponse executeManualRebalancing(Long userId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다"));

        return executeRebalancing(portfolio, ExecutionType.MANUAL);
    }

    @Override
    @Transactional
    public void processAllActivePortfolios() {
        List<Portfolio> activePortfolios = portfolioRepository.findByAutoRebalancingTrueOrderByNextRebalanceDateAsc();
        
        log.info("자동 리밸런싱 대상 포트폴리오 {}개 처리 시작", activePortfolios.size());

        for (Portfolio portfolio : activePortfolios) {
            try {
                if (isRebalancingNeeded(portfolio.getId())) {
                    log.info("포트폴리오 {} 자동 리밸런싱 실행", portfolio.getId());
                    executeAutoRebalancing(portfolio.getId());
                }
            } catch (Exception e) {
                log.error("포트폴리오 {} 자동 리밸런싱 실패", portfolio.getId(), e);
            }
        }
    }

    @Override
    public boolean isRebalancingNeeded(Long portfolioId) {
        RebalancingCheckResponse response = checkRebalancingNeeded(portfolioId);
        return response.isNeedsRebalancing();
    }

    private RebalancingExecutionResponse executeRebalancing(Portfolio portfolio, ExecutionType executionType) {
        try {
            log.info("포트폴리오 {} 리밸런싱 실행 시작", portfolio.getId());

            // 현재 보유 종목 정보 조회
            Map<String, HoldingInfo> currentHoldings = getCurrentHoldings(portfolio);
            
            // 목표 종목 정보 조회
            List<PortfolioStock> targetStocks = portfolioStockRepository.findByPortfolioId(portfolio.getId());

            // 리밸런싱 주문 계산
            List<RebalancingOrderData> orders = calculateRebalancingOrders(currentHoldings, targetStocks, portfolio);

            if (orders.isEmpty()) {
                return RebalancingExecutionResponse.failure("리밸런싱할 주문이 없습니다");
            }

            // 주문 실행
            List<RebalancingExecutionResponse.OrderResult> orderResults = new ArrayList<>();
            BigDecimal totalBuyAmount = BigDecimal.ZERO;
            BigDecimal totalSellAmount = BigDecimal.ZERO;
            BigDecimal totalPortfolioValue = calculateTotalPortfolioValue(currentHoldings);

            for (RebalancingOrderData order : orders) {
                try {
                    RebalancingExecutionResponse.OrderResult result = executeOrder(order, portfolio);
                    orderResults.add(result);

                    if (result.isSuccess()) {
                        if ("BUY".equals(result.getOrderType())) {
                            totalBuyAmount = totalBuyAmount.add(result.getPrice().multiply(BigDecimal.valueOf(result.getQuantity())));
                        } else {
                            totalSellAmount = totalSellAmount.add(result.getPrice().multiply(BigDecimal.valueOf(result.getQuantity())));
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
            com.rebra.entity.RebalancingOrder rebalancingOrder = com.rebra.entity.RebalancingOrder.builder()
                    .portfolio(portfolio)
                    .totalBuyAmount(totalBuyAmount)
                    .totalSellAmount(totalSellAmount)
                    .rebalancingDate(LocalDateTime.now())
                    .status(TransactionStatus.COMPLETED)
                    .executionType(executionType)
                    .totalPortfolioValue(totalPortfolioValue)
                    .build();

            rebalancingOrder = rebalancingOrderRepository.save(rebalancingOrder);

            // 다음 리밸런싱 날짜 업데이트
            updateNextRebalanceDate(portfolio);

            log.info("포트폴리오 {} 리밸런싱 완료", portfolio.getId());

            return RebalancingExecutionResponse.success(
                    rebalancingOrder.getId(),
                    totalBuyAmount,
                    totalSellAmount,
                    totalPortfolioValue,
                    orderResults
            );

        } catch (Exception e) {
            log.error("포트폴리오 {} 리밸런싱 실행 실패", portfolio.getId(), e);
            return RebalancingExecutionResponse.failure("리밸런싱 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
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
                    int quantity = Integer.parseInt(holding.getHldgQty()); // 보유수량
                    BigDecimal currentPrice = new BigDecimal(holding.getPrpr()); // 현재가
                    
                    if (quantity > 0) {
                        holdings.put(stockCode, new HoldingInfo(stockCode, quantity, currentPrice));
                    }
                }
            }

            return holdings;
        } catch (Exception e) {
            log.error("현재 보유 종목 조회 실패: portfolioId={}", portfolio.getId(), e);
            throw new RuntimeException("현재 보유 종목 조회 실패", e);
        }
    }

    private boolean checkThresholdRebalancing(Map<String, HoldingInfo> currentHoldings, 
                                            List<PortfolioStock> targetStocks,
                                            List<RebalancingCheckResponse.StockRebalancingInfo> stockInfos) {
        
        BigDecimal totalValue = calculateTotalValue(currentHoldings);
        boolean needsRebalancing = false;

        for (PortfolioStock targetStock : targetStocks) {
            String stockCode = targetStock.getStockCode();
            BigDecimal targetWeight = targetStock.getTargetWeight();
            BigDecimal threshold = targetStock.getThresholdPercentage();
            
            HoldingInfo holding = currentHoldings.get(stockCode);
            BigDecimal currentWeight = BigDecimal.ZERO;
            BigDecimal currentValue = BigDecimal.ZERO;
            int currentQuantity = 0;
            BigDecimal currentPrice = BigDecimal.ZERO;

            if (holding != null) {
                currentValue = holding.getCurrentPrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
                currentWeight = currentValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                currentQuantity = holding.getQuantity();
                currentPrice = holding.getCurrentPrice();
            }

            BigDecimal weightDifference = currentWeight.subtract(targetWeight).abs();
            boolean stockNeedsRebalancing = weightDifference.compareTo(threshold) > 0;
            
            if (stockNeedsRebalancing) {
                needsRebalancing = true;
            }

            stockInfos.add(RebalancingCheckResponse.StockRebalancingInfo.builder()
                    .stockCode(stockCode)
                    .targetWeight(targetWeight)
                    .currentWeight(currentWeight)
                    .weightDifference(weightDifference)
                    .threshold(threshold)
                    .needsRebalancing(stockNeedsRebalancing)
                    .currentQuantity(currentQuantity)
                    .currentPrice(currentPrice)
                    .currentValue(currentValue)
                    .build());
        }

        return needsRebalancing;
    }

    private boolean checkPeriodicRebalancing(Portfolio portfolio) {
        LocalDate today = LocalDate.now();
        LocalDate nextRebalanceDate = portfolio.getNextRebalanceDate();
        
        if (nextRebalanceDate == null) {
            return true; // 첫 리밸런싱
        }
        
        return !today.isBefore(nextRebalanceDate);
    }

    private void calculateStockInfos(Map<String, HoldingInfo> currentHoldings,
                                   List<PortfolioStock> targetStocks,
                                   List<RebalancingCheckResponse.StockRebalancingInfo> stockInfos) {
        
        BigDecimal totalValue = calculateTotalValue(currentHoldings);

        for (PortfolioStock targetStock : targetStocks) {
            String stockCode = targetStock.getStockCode();
            BigDecimal targetWeight = targetStock.getTargetWeight();
            
            HoldingInfo holding = currentHoldings.get(stockCode);
            BigDecimal currentWeight = BigDecimal.ZERO;
            BigDecimal currentValue = BigDecimal.ZERO;
            int currentQuantity = 0;
            BigDecimal currentPrice = BigDecimal.ZERO;

            if (holding != null) {
                currentValue = holding.getCurrentPrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
                currentWeight = currentValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                currentQuantity = holding.getQuantity();
                currentPrice = holding.getCurrentPrice();
            }

            stockInfos.add(RebalancingCheckResponse.StockRebalancingInfo.builder()
                    .stockCode(stockCode)
                    .targetWeight(targetWeight)
                    .currentWeight(currentWeight)
                    .weightDifference(currentWeight.subtract(targetWeight).abs())
                    .threshold(targetStock.getThresholdPercentage())
                    .needsRebalancing(true)
                    .currentQuantity(currentQuantity)
                    .currentPrice(currentPrice)
                    .currentValue(currentValue)
                    .build());
        }
    }

    private BigDecimal calculateTotalValue(Map<String, HoldingInfo> holdings) {
        return holdings.values().stream()
                .map(holding -> holding.getCurrentPrice().multiply(BigDecimal.valueOf(holding.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalPortfolioValue(Map<String, HoldingInfo> holdings) {
        return calculateTotalValue(holdings);
    }

    private List<RebalancingOrderData> calculateRebalancingOrders(Map<String, HoldingInfo> currentHoldings,
                                                           List<PortfolioStock> targetStocks,
                                                           Portfolio portfolio) {
        List<RebalancingOrderData> orders = new ArrayList<>();
        BigDecimal totalValue = calculateTotalValue(currentHoldings);

        for (PortfolioStock targetStock : targetStocks) {
            String stockCode = targetStock.getStockCode();
            BigDecimal targetWeight = targetStock.getTargetWeight();
            BigDecimal targetValue = totalValue.multiply(targetWeight).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            HoldingInfo holding = currentHoldings.get(stockCode);
            BigDecimal currentValue = BigDecimal.ZERO;
            int currentQuantity = 0;
            BigDecimal currentPrice = BigDecimal.ZERO;

            if (holding != null) {
                currentValue = holding.getCurrentPrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
                currentQuantity = holding.getQuantity();
                currentPrice = holding.getCurrentPrice();
            }

            BigDecimal valueDifference = targetValue.subtract(currentValue);
            
            if (valueDifference.abs().compareTo(BigDecimal.valueOf(1000)) > 0) { // 1000원 이상 차이날 때만 주문
                if (valueDifference.compareTo(BigDecimal.ZERO) > 0) {
                    // 매수 필요
                    int buyQuantity = valueDifference.divide(currentPrice, 0, RoundingMode.DOWN).intValue();
                    if (buyQuantity > 0) {
                        orders.add(new RebalancingOrderData(stockCode, "BUY", buyQuantity, currentPrice));
                    }
                } else {
                    // 매도 필요
                    int sellQuantity = valueDifference.abs().divide(currentPrice, 0, RoundingMode.DOWN).intValue();
                    sellQuantity = Math.min(sellQuantity, currentQuantity); // 보유 수량 초과 불가
                    if (sellQuantity > 0) {
                        orders.add(new RebalancingOrderData(stockCode, "SELL", sellQuantity, currentPrice));
                    }
                }
            }
        }

        return orders;
    }

    private RebalancingExecutionResponse.OrderResult executeOrder(RebalancingOrderData order, Portfolio portfolio) {
        try {
            StockTradeRequest tradeRequest = new StockTradeRequest();
            tradeRequest.setOrderType("01"); // 시장가
            tradeRequest.setQuantity(order.getQuantity());
            tradeRequest.setPrice(0L); // 시장가
            tradeRequest.setAccountId(portfolio.getAccount().getId());

            StockTradeResponse response;
            if ("BUY".equals(order.getOrderType())) {
                response = stockTradingService.buyStock(order.getStockCode(), tradeRequest, portfolio.getUser().getId());
            } else {
                response = stockTradingService.sellStock(order.getStockCode(), tradeRequest, portfolio.getUser().getId());
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

    private void updateNextRebalanceDate(Portfolio portfolio) {
        if (portfolio.getRebalancingStrategy() == RebalancingStrategy.PERIODIC) {
            LocalDate nextDate = calculateNextRebalanceDate(portfolio);
            portfolio.updateNextRebalanceDate(nextDate);
        }
        portfolio.updateLastRebalanceDate(LocalDate.now());
        portfolioRepository.save(portfolio);
    }

    private LocalDate calculateNextRebalanceDate(Portfolio portfolio) {
        LocalDate today = LocalDate.now();
        
        if (portfolio.getRebalancingPeriod() == RebalancingPeriod.MONTHLY) {
            return today.plusMonths(1).withDayOfMonth(today.getDayOfMonth());
        } else if (portfolio.getRebalancingPeriod() == RebalancingPeriod.YEARLY) {
            return today.plusYears(1);
        }
        
        return today.plusDays(1);
    }

    private String getCheckReason(RebalancingStrategy strategy) {
        return switch (strategy) {
            case THRESHOLD -> "임계값 초과";
            case PERIODIC -> "주기 도래";
        };
    }

    // 내부 클래스들
    private static class HoldingInfo {
        private final String stockCode;
        private final int quantity;
        private final BigDecimal currentPrice;

        public HoldingInfo(String stockCode, int quantity, BigDecimal currentPrice) {
            this.stockCode = stockCode;
            this.quantity = quantity;
            this.currentPrice = currentPrice;
        }

        public String getStockCode() { return stockCode; }
        public int getQuantity() { return quantity; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
    }

    private static class RebalancingOrderData {
        private final String stockCode;
        private final String orderType;
        private final int quantity;
        private final BigDecimal price;

        public RebalancingOrderData(String stockCode, String orderType, int quantity, BigDecimal price) {
            this.stockCode = stockCode;
            this.orderType = orderType;
            this.quantity = quantity;
            this.price = price;
        }

        public String getStockCode() { return stockCode; }
        public String getOrderType() { return orderType; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
    }
}