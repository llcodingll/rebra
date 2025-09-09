package com.rebra.calculator.service;

import com.rebra.calculator.domain.Portfolio;
import com.rebra.calculator.domain.Stock;
import com.rebra.calculator.domain.Trade;
import com.rebra.calculator.dto.BacktestStockDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rebra.calculator.constant.BacktestConstants.TradingRules.MINIMUM_TRADING_UNIT;

/**
 * 포트폴리오 관리를 담당하는 서비스 클래스
 * 포트폴리오 초기 구성, 리밸런싱 실행, 성과 계산 등을 수행한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioManagerService {

    private final FeeCalculatorService feeCalculatorService;

    /**
     * 초기 포트폴리오를 구성한다
     * 초기 보유 주식 수량을 바탕으로 포트폴리오를 생성
     * 
     * @param initialHoldings 초기 보유 종목 정보 (수량 포함)
     * @param stocks 종목 목록 (목표 비중 포함)
     * @param initialPrices 초기 주가 정보
     * @param startDate 백테스트 시작일
     * @return 초기 구성된 포트폴리오
     * @throws IllegalArgumentException 잘못된 매개변수
     */
    public Portfolio createInitialPortfolio(List<BacktestStockDto> initialHoldings, List<Stock> stocks, 
                                          Map<String, Double> initialPrices, LocalDate startDate) {
        validateInitialPortfolioParameters(initialHoldings, stocks, initialPrices, startDate);
        
        // 초기 포트폴리오 가치 계산
        double initialValue = calculateInitialPortfolioValue(initialHoldings, initialPrices);
        
        log.info("초기 포트폴리오 구성 시작 - 초기가치: {:.0f}원, 종목수: {}", initialValue, initialHoldings.size());
        
        // 초기 현금 0원으로 포트폴리오 생성
        Portfolio portfolio = new Portfolio();

        // 각 종목별로 초기 보유 수량 설정
        for (BacktestStockDto holding : initialHoldings) {
            String stockCode = holding.getStockCode();
            int quantity = holding.getSafeShares();

            if (quantity <= 0) {
                log.warn("종목 {}의 초기 보유 수량이 0 이하입니다: {}주", stockCode, quantity);
                continue;
            }

            Double currentPrice = initialPrices.get(stockCode);
            if (currentPrice == null || currentPrice <= 0) {
                log.warn("종목 {}의 초기 가격 정보가 없거나 유효하지 않습니다: {}", stockCode, currentPrice);
                continue;
            }

            try {
                // 초기 보유 주식을 포트폴리오에 직접 설정 (거래 없이)
                portfolio.setInitialHolding(stockCode, quantity);

                log.debug("초기 보유 설정 완료 - 종목: {}, 수량: {}주, 가격: {:.0f}원",
                        stockCode, quantity, currentPrice);

            } catch (Exception e) {
                log.error("종목 {} 초기 보유 설정 실패", stockCode, e);
                throw new RuntimeException("초기 포트폴리오 구성 실패: " + e.getMessage(), e);
            }
        }
        
        // 초기 포트폴리오 가치 설정
        portfolio.setInitialValue(initialValue);
        
        log.info("초기 포트폴리오 구성 완료 - 총 {}개 종목, 초기가치: {:.0f}원",
                portfolio.getHoldingStockCodes().size(), portfolio.getInitialValue());
        
        return portfolio;
    }

    /**
     * 포트폴리오 리밸런싱을 실행한다
     * 현재 비중과 목표 비중의 차이에 따라 매매를 실행
     * 
     * @param portfolio 현재 포트폴리오
     * @param stocks 종목 목록 (목표 비중 포함)
     * @param currentPrices 현재 주가 정보
     * @param rebalancingDate 리밸런싱 실행일
     * @return 리밸런싱으로 발생한 거래 목록
     * @throws IllegalArgumentException 잘못된 매개변수
     */
    public List<Trade> executeRebalancing(Portfolio portfolio, List<Stock> stocks, 
                                        Map<String, Double> currentPrices, LocalDate rebalancingDate) {
        validateRebalancingParameters(portfolio, stocks, currentPrices, rebalancingDate);
        
        log.info("리밸런싱 실행 시작 - 날짜: {}, 종목수: {}", rebalancingDate, stocks.size());
        
        List<Trade> rebalancingTrades = new ArrayList<>();
        double totalValue = portfolio.getTotalValue(currentPrices);
        
        if (totalValue <= 0) {
            log.warn("포트폴리오 총 가치가 0 이하입니다: {:.0f}원", totalValue);
            return rebalancingTrades;
        }
        
        // 1단계: 현재 비중과 목표 비중 계산
        Map<String, Double> currentWeights = portfolio.getCurrentWeights(currentPrices);
        Map<String, RebalancingAction> rebalancingPlan = createRebalancingPlan(stocks, currentWeights, totalValue, currentPrices);
        
        // 2단계: 매도 거래 먼저 실행 (현금 확보)
        List<Trade> sellTrades = executeSellTrades(portfolio, rebalancingPlan, currentPrices, rebalancingDate);
        rebalancingTrades.addAll(sellTrades);
        
        // 3단계: 매수 거래 실행 (목표 비중 달성)
        List<Trade> buyTrades = executeBuyTrades(portfolio, rebalancingPlan, currentPrices, rebalancingDate);
        rebalancingTrades.addAll(buyTrades);
        
        log.info("리밸런싱 실행 완료 - 총 {}건 거래 (매도: {}건, 매수: {}건)", 
                rebalancingTrades.size(), sellTrades.size(), buyTrades.size());
        
        return rebalancingTrades;
    }

    /**
     * 바이앤홀드 전략의 최종 가치를 계산한다
     * 초기 보유 주식을 그대로 보유했을 때의 최종 가치
     * 
     * @param initialHoldings 초기 보유 종목 정보
     * @param initialPrices 초기 주가
     * @param finalPrices 최종 주가
     * @return 바이앤홀드 최종 가치
     */
    public double calculateBuyAndHoldValue(List<BacktestStockDto> initialHoldings,
                                         Map<String, Double> initialPrices, Map<String, Double> finalPrices) {
        validateBuyAndHoldParameters(initialHoldings, initialPrices, finalPrices);
        
        log.debug("바이앤홀드 가치 계산 시작 - 초기보유 종목수: {}개", initialHoldings.size());
        
        double totalValue = 0.0;
        
        for (BacktestStockDto holding : initialHoldings) {
            String stockCode = holding.getStockCode();
            int quantity = holding.getSafeShares();
            
            if (quantity <= 0) {
                continue;
            }
            
            Double initialPrice = initialPrices.get(stockCode);
            Double finalPrice = finalPrices.get(stockCode);
            
            if (initialPrice == null || finalPrice == null || initialPrice <= 0 || finalPrice <= 0) {
                log.warn("종목 {}의 가격 정보가 불완전합니다. 초기: {}, 최종: {}", 
                        stockCode, initialPrice, finalPrice);
                continue;
            }
            
            // 최종 가치 계산
            double stockValue = quantity * finalPrice;
            totalValue += stockValue;
            
            log.debug("바이앤홀드 - {}: {}주, 초기가치: {:.0f}원, 최종가치: {:.0f}원", 
                    stockCode, quantity, quantity * initialPrice, stockValue);
        }
        
        log.debug("바이앤홀드 가치 계산 완료 - 총가치: {:.0f}원", totalValue);
        
        return totalValue;
    }

    /**
     * 포트폴리오의 승률을 계산한다
     * 수익이 발생한 기간의 비율
     * 
     * @param periodReturns 주기별 수익률 목록
     * @return 승률 (0.0 ~ 1.0)
     */
    public double calculateWinRate(List<Double> periodReturns) {
        if (periodReturns == null || periodReturns.isEmpty()) {
            return 0.0;
        }
        
        long winCount = periodReturns.stream()
                .mapToLong(ret -> ret > 0 ? 1 : 0)
                .sum();
        
        double winRate = (double) winCount / periodReturns.size();
        
        log.debug("승률 계산 - 총 {}기간 중 {}기간 수익, 승률: {:.2f}%", 
                periodReturns.size(), winCount, winRate * 100);
        
        return winRate;
    }

    /**
     * 최대 낙폭(Maximum Drawdown)을 계산한다
     * 
     * @param portfolioValues 포트폴리오 가치 변화 목록
     * @return 최대 낙폭 (음수로 표현)
     */
    public double calculateMaxDrawdown(List<Double> portfolioValues) {
        if (portfolioValues == null || portfolioValues.size() < 2) {
            return 0.0;
        }
        
        double maxDrawdown = 0.0;
        double peak = portfolioValues.get(0);
        
        for (double currentValue : portfolioValues) {
            if (currentValue > peak) {
                peak = currentValue;
            }
            
            double drawdown = (currentValue - peak) / peak;
            if (drawdown < maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        
        log.debug("최대 낙폭 계산 완료: {:.2f}%", maxDrawdown * 100);
        
        return maxDrawdown;
    }

    /**
     * 포트폴리오 변동성을 계산한다 (연환산 표준편차)
     * 
     * @param periodReturns 주기별 수익률 목록
     * @param periodsPerYear 연간 주기 수 (일별: 252, 월별: 12 등)
     * @return 연환산 변동성
     */
    public double calculateVolatility(List<Double> periodReturns, int periodsPerYear) {
        if (periodReturns == null || periodReturns.size() < 2) {
            return 0.0;
        }
        
        // 평균 수익률 계산
        double meanReturn = periodReturns.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // 분산 계산
        double variance = periodReturns.stream()
                .mapToDouble(ret -> Math.pow(ret - meanReturn, 2))
                .average()
                .orElse(0.0);
        
        // 표준편차 계산 후 연환산
        double volatility = Math.sqrt(variance) * Math.sqrt(periodsPerYear);
        
        log.debug("변동성 계산 완료 - 평균수익률: {:.4f}, 표준편차: {:.4f}, 연환산변동성: {:.4f}", 
                meanReturn, Math.sqrt(variance), volatility);
        
        return volatility;
    }

    /**
     * 샤프 비율을 계산한다
     * 
     * @param portfolioReturn 포트폴리오 연환산 수익률
     * @param volatility 연환산 변동성
     * @param riskFreeRate 무위험 수익률 (기본값: 0.02 = 2%)
     * @return 샤프 비율
     */
    public double calculateSharpeRatio(double portfolioReturn, double volatility, double riskFreeRate) {
        if (volatility <= 0) {
            return 0.0;
        }
        
        double sharpeRatio = (portfolioReturn - riskFreeRate) / volatility;
        
        log.debug("샤프 비율 계산 완료 - 포트폴리오수익률: {:.4f}, 변동성: {:.4f}, 무위험수익률: {:.4f}, 샤프비율: {:.4f}", 
                portfolioReturn, volatility, riskFreeRate, sharpeRatio);
        
        return sharpeRatio;
    }

    /**
     * 샤프 비율을 계산한다 (기본 무위험 수익률 2% 사용)
     */
    public double calculateSharpeRatio(double portfolioReturn, double volatility) {
        return calculateSharpeRatio(portfolioReturn, volatility, 0.02);
    }

    // ===== Private Helper Methods =====

    /**
     * 리밸런싱 계획을 수립한다
     */
    private Map<String, RebalancingAction> createRebalancingPlan(List<Stock> stocks, 
                                                               Map<String, Double> currentWeights, 
                                                               double totalValue,
                                                               Map<String, Double> currentPrices) {
        Map<String, RebalancingAction> plan = new HashMap<>();
        
        for (Stock stock : stocks) {
            String stockCode = stock.getStockCode();
            double currentWeight = currentWeights.getOrDefault(stockCode, 0.0);
            double targetWeight = stock.getTargetWeight();
            double targetAmount = totalValue * targetWeight;
            double currentAmount = totalValue * currentWeight;
            double difference = targetAmount - currentAmount;
            
            Double price = currentPrices.get(stockCode);
            if (price == null || price <= 0) {
                log.warn("종목 {}의 가격 정보가 없습니다", stockCode);
                continue;
            }
            
            if (Math.abs(difference) < 10000) { // 1만원 미만 차이는 무시
                continue;
            }
            
            RebalancingAction action = new RebalancingAction();
            action.stockCode = stockCode;
            action.currentWeight = currentWeight;
            action.targetWeight = targetWeight;
            action.price = price;
            
            if (difference > 0) {
                // 매수 필요
                action.actionType = "BUY";
                action.quantity = (int) Math.floor(difference / price);
                action.amount = action.quantity * price;
            } else {
                // 매도 필요
                action.actionType = "SELL";
                action.quantity = (int) Math.floor(Math.abs(difference) / price);
                action.amount = action.quantity * price;
            }
            
            if (action.quantity >= MINIMUM_TRADING_UNIT) {
                plan.put(stockCode, action);
            }
        }
        
        return plan;
    }

    /**
     * 매도 거래를 실행한다
     */
    private List<Trade> executeSellTrades(Portfolio portfolio, Map<String, RebalancingAction> plan,
                                        Map<String, Double> currentPrices, LocalDate date) {
        List<Trade> sellTrades = new ArrayList<>();
        
        for (RebalancingAction action : plan.values()) {
            if (!"SELL".equals(action.actionType)) {
                continue;
            }
            
            int currentHoldings = portfolio.getHoldings(action.stockCode);
            if (currentHoldings < action.quantity) {
                log.warn("매도 수량이 보유 수량을 초과합니다. 종목: {}, 보유: {}, 매도요청: {}", 
                        action.stockCode, currentHoldings, action.quantity);
                action.quantity = currentHoldings; // 보유 수량만큼만 매도
            }
            
            if (action.quantity < MINIMUM_TRADING_UNIT) {
                continue;
            }
            
            try {
                double fee = feeCalculatorService.calculateSellFee(action.amount);
                double tax = feeCalculatorService.calculateSecuritiesTransactionTax(action.amount);
                
                Trade trade = portfolio.sellStock(
                    action.stockCode,
                    action.quantity,
                    action.price,
                    fee,
                    tax,
                    date
                );
                
                sellTrades.add(trade);
                
                log.debug("매도 실행 - {}: {}주 @{:.0f}원", action.stockCode, action.quantity, action.price);
                
            } catch (Exception e) {
                log.error("매도 실행 중 오류 발생: {}", action.stockCode, e);
            }
        }
        
        return sellTrades;
    }

    /**
     * 매수 거래를 실행한다
     */
    private List<Trade> executeBuyTrades(Portfolio portfolio, Map<String, RebalancingAction> plan,
                                       Map<String, Double> currentPrices, LocalDate date) {
        List<Trade> buyTrades = new ArrayList<>();
        
        for (RebalancingAction action : plan.values()) {
            if (!"BUY".equals(action.actionType)) {
                continue;
            }
            
            if (action.quantity < MINIMUM_TRADING_UNIT) {
                continue;
            }
            
            try {
                double fee = feeCalculatorService.calculateBuyFee(action.amount);
                
                Trade trade = portfolio.buyStock(
                    action.stockCode,
                    action.quantity,
                    action.price,
                    fee,
                    date
                );
                
                buyTrades.add(trade);
                
                log.debug("매수 실행 - {}: {}주 @{:.0f}원", action.stockCode, action.quantity, action.price);
                
            } catch (Exception e) {
                log.error("매수 실행 중 오류 발생: {}", action.stockCode, e);
            }
        }
        
        return buyTrades;
    }

    // ===== Validation Methods =====

    private void validateInitialPortfolioParameters(List<BacktestStockDto> initialHoldings, List<Stock> stocks,
                                                   Map<String, Double> initialPrices, LocalDate startDate) {
        if (initialHoldings == null || initialHoldings.isEmpty()) {
            throw new IllegalArgumentException("초기 보유 종목 정보가 없습니다");
        }
        
        if (stocks == null || stocks.isEmpty()) {
            throw new IllegalArgumentException("종목 목록이 비어있습니다");
        }
        
        if (initialPrices == null || initialPrices.isEmpty()) {
            throw new IllegalArgumentException("초기 가격 정보가 없습니다");
        }
        
        if (startDate == null) {
            throw new IllegalArgumentException("시작 날짜가 없습니다");
        }
        
        // 목표 비중 합계 검증
        double totalWeight = stocks.stream().mapToDouble(Stock::getTargetWeight).sum();
        if (Math.abs(totalWeight - 1.0) > 0.01) {
            throw new IllegalArgumentException("목표 비중의 합이 100%가 아닙니다: " + totalWeight * 100 + "%");
        }
    }

    /**
     * 초기 포트폴리오 가치를 계산한다
     * 
     * @param initialHoldings 초기 보유 종목 정보
     * @param initialPrices 초기 가격 정보
     * @return 초기 포트폴리오 가치
     */
    public double calculateInitialPortfolioValue(List<BacktestStockDto> initialHoldings, 
                                                 Map<String, Double> initialPrices) {
        return initialHoldings.stream()
                .mapToDouble(holding -> {
                    String stockCode = holding.getStockCode();
                    int quantity = holding.getSafeShares();
                    Double price = initialPrices.get(stockCode);
                    
                    if (quantity <= 0 || price == null || price <= 0) {
                        return 0.0;
                    }
                    
                    return quantity * price;
                })
                .sum();
    }

    private void validateRebalancingParameters(Portfolio portfolio, List<Stock> stocks,
                                             Map<String, Double> currentPrices, LocalDate rebalancingDate) {
        if (portfolio == null) {
            throw new IllegalArgumentException("포트폴리오가 null입니다");
        }
        
        if (stocks == null || stocks.isEmpty()) {
            throw new IllegalArgumentException("종목 목록이 비어있습니다");
        }
        
        if (currentPrices == null || currentPrices.isEmpty()) {
            throw new IllegalArgumentException("현재 가격 정보가 없습니다");
        }
        
        if (rebalancingDate == null) {
            throw new IllegalArgumentException("리밸런싱 날짜가 없습니다");
        }
    }

    private void validateBuyAndHoldParameters(List<BacktestStockDto> initialHoldings,
                                            Map<String, Double> initialPrices, Map<String, Double> finalPrices) {
        if (initialHoldings == null || initialHoldings.isEmpty()) {
            throw new IllegalArgumentException("초기 보유 종목 정보가 없습니다");
        }
        
        if (initialPrices == null || initialPrices.isEmpty()) {
            throw new IllegalArgumentException("초기 가격 정보가 없습니다");
        }
        
        if (finalPrices == null || finalPrices.isEmpty()) {
            throw new IllegalArgumentException("최종 가격 정보가 없습니다");
        }
    }

    /**
     * 리밸런싱 액션을 나타내는 내부 클래스
     */
    private static class RebalancingAction {
        String stockCode;
        String actionType; // "BUY" or "SELL"
        int quantity;
        double price;
        double amount;
        double currentWeight;
        double targetWeight;
    }
}