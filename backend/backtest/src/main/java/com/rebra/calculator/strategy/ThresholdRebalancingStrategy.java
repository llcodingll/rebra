package com.rebra.calculator.strategy;

import com.rebra.calculator.context.BacktestContext;
import com.rebra.calculator.domain.Portfolio;
import com.rebra.calculator.domain.Stock;
import com.rebra.calculator.util.PriceDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rebra.calculator.constant.BacktestConstants.Rebalancing.DEFAULT_THRESHOLD_PERCENTAGE;
import static com.rebra.calculator.constant.BacktestConstants.Rebalancing.WEIGHT_TOLERANCE;

/**
 * 임계값 기반 리밸런싱 전략
 * 목표 비중에서 설정된 임계값만큼 벗어날 때 리밸런싱을 실행하는 전략
 * 
 * 특징:
 * - 불필요한 거래를 줄여 거래비용 절약
 * - 시장 변동성에 따른 자연스러운 비중 조정 허용
 * - 각 종목별로 개별 임계값 설정 가능
 * - 시장 상황에 따라 적응적으로 작동
 */
@Slf4j
@Component
public class ThresholdRebalancingStrategy implements RebalancingStrategy {

    @Override
    public boolean shouldRebalance(Map<String, Double> currentPrices, BacktestContext context, LocalDate currentDate, Portfolio portfolio,
                                 LocalDate lastRebalancingDate) {
        if (portfolio == null || context == null || currentPrices == null) {
            return false;
        }

        try {
            // 포트폴리오에서 종목 정보 가져오기 (업데이트된 targetWeight 포함)
            Map<String, Stock> targetStocksMap = portfolio.getTargetStocks();
            List<Stock> stocks = new ArrayList<>(targetStocksMap.values());
            
            if (stocks == null || stocks.isEmpty()) {
                return false;
            }
            
            // 유효한 가격 데이터만 필터링
            Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(currentPrices);
            if (validPrices.isEmpty()) {
                log.debug("유효한 가격 정보가 없어 리밸런싱을 건너뜁니다 - 날짜: {}", currentDate);
                return false;
            }
            
            // 현재 포트폴리오 가치 계산 (유효한 가격으로만)
            double totalValue = portfolio.getTotalValue(validPrices);
            if (totalValue <= 0) {
                log.warn("포트폴리오 총 가치가 0 이하입니다: {}", totalValue);
                return false;
            }

            // 현재 비중 계산 (유효한 가격으로만)
            Map<String, Double> currentWeights = portfolio.getCurrentWeights(validPrices);

            // 각 종목별로 임계값 초과 여부 확인 (유효한 가격을 가진 종목만)
            for (Stock stock : stocks) {
                String stockCode = stock.getStockCode();
                
                // 유효한 가격이 없는 종목은 건너뛰기
                if (!validPrices.containsKey(stockCode)) {
                    log.trace("종목 {} 가격 정보 없음 - 임계값 검사 제외", stockCode);
                    continue;
                }
                
                double currentWeight = currentWeights.getOrDefault(stockCode, 0.0);
                
                // Stock의 targetWeight 필드 사용 (Portfolio에서 이미 업데이트됨)
                if (stock.exceedsThreshold(currentWeight)) {
                    double relativeThreshold = stock.getTargetWeight() * stock.getThresholdPercentage();
                    double deviation = Math.abs(currentWeight - stock.getTargetWeight());
                    log.debug("종목 {} 임계값 초과 - 현재비중: {}%, 목표비중: {}%, 임계값: {}%, 상대임계값: {}%, 편차: {}%", 
                            stockCode, 
                            String.format("%.2f", currentWeight * 100), 
                            String.format("%.2f", stock.getTargetWeight() * 100), 
                            String.format("%.2f", stock.getThresholdPercentage() * 100),
                            String.format("%.2f", relativeThreshold * 100),
                            String.format("%.2f", deviation * 100));
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("리밸런싱 필요 여부 판단 중 오류 발생", e);
            return false;
        }
    }

    @Override
    public String getRebalancingReason(BacktestContext context, LocalDate currentDate, Portfolio portfolio,
                                     LocalDate lastRebalancingDate) {
        // 컨텍스트에서 현재 가격과 종목 정보 가져오기
        Map<String, Double> currentPrices = context.getPricesForDate(currentDate);
        
        if (!shouldRebalance(currentPrices, context, currentDate, portfolio, lastRebalancingDate)) {
            return "NO_REBALANCING_NEEDED";
        }

        try {
            // 포트폴리오에서 종목 정보 가져오기 (업데이트된 targetWeight 포함)
            Map<String, Stock> targetStocksMap = portfolio.getTargetStocks();
            List<Stock> stocks = new ArrayList<>(targetStocksMap.values());
            
            if (stocks == null || currentPrices == null) {
                return "DATA_NOT_AVAILABLE";
            }
            
            // 유효한 가격 데이터만 사용
            Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(currentPrices);
            Map<String, Double> currentWeights = portfolio.getCurrentWeights(validPrices);
            List<String> exceededStocks = new ArrayList<>();

            for (Stock stock : stocks) {
                String stockCode = stock.getStockCode();
                
                // 유효한 가격이 없는 종목은 건너뛰기
                if (!validPrices.containsKey(stockCode)) {
                    continue;
                }
                
                double currentWeight = currentWeights.getOrDefault(stockCode, 0.0);
                
                // Stock의 targetWeight 필드 사용 (Portfolio에서 이미 업데이트됨)
                if (stock.exceedsThreshold(currentWeight)) {
                    double deviation = Math.abs(currentWeight - stock.getTargetWeight());
                    exceededStocks.add(String.format("%s(%.1f%%편차)", stockCode, deviation * 100));
                }
            }

            if (exceededStocks.isEmpty()) {
                return "THRESHOLD_CHECK_ERROR";
            }

            return String.format("THRESHOLD_EXCEEDED: %s", String.join(", ", exceededStocks));

        } catch (Exception e) {
            log.error("리밸런싱 사유 계산 중 오류 발생", e);
            return "THRESHOLD_CALCULATION_ERROR";
        }
    }

    @Override
    public String getRebalancingReasonKorean(BacktestContext context, LocalDate currentDate, Portfolio portfolio,
                                           LocalDate lastRebalancingDate) {
        String englishReason = getRebalancingReason(context, currentDate, portfolio, lastRebalancingDate);
        
        if (englishReason.equals("NO_REBALANCING_NEEDED")) {
            return "리밸런싱 불필요";
        }
        
        if (englishReason.equals("THRESHOLD_CHECK_ERROR")) {
            return "임계값 확인 오류";
        }
        
        if (englishReason.equals("THRESHOLD_CALCULATION_ERROR")) {
            return "임계값 계산 오류";
        }
        
        if (englishReason.startsWith("THRESHOLD_EXCEEDED:")) {
            String details = englishReason.substring("THRESHOLD_EXCEEDED:".length()).trim();
            return String.format("임계값 초과: %s", details);
        }
        
        return "임계값 기반 리밸런싱";
    }

    @Override
    public String getStrategyName() {
        return "Threshold-Based Rebalancing";
    }

    @Override
    public String getStrategyDescription() {
        return "목표 비중에서 설정된 임계값만큼 벗어날 때만 리밸런싱을 실행하는 전략. " +
               "불필요한 거래를 줄여 거래비용을 절약하고, 시장 변동성에 따른 자연스러운 비중 조정을 허용합니다.";
    }

    @Override
    public List<Stock> getStocksNeedingRebalancing(BacktestContext context, Portfolio portfolio) {
        if (portfolio == null || context == null) {
            return new ArrayList<>();
        }

        try {
            // 포트폴리오에서 종목 정보 가져오기 (업데이트된 targetWeight 포함)
            Map<String, Stock> targetStocksMap = portfolio.getTargetStocks();
            List<Stock> stocks = new ArrayList<>(targetStocksMap.values());
            LocalDate currentDate = context.getLastDate();
            Map<String, Double> currentPrices = context.getPricesForDate(currentDate);
            
            if (stocks == null || currentPrices == null) {
                return new ArrayList<>();
            }
            
            // 유효한 가격 데이터만 사용
            Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(currentPrices);
            Map<String, Double> currentWeights = portfolio.getCurrentWeights(validPrices);

            return stocks.stream()
                    .filter(stock -> {
                        String stockCode = stock.getStockCode();
                        
                        // 유효한 가격이 없는 종목은 제외
                        if (!validPrices.containsKey(stockCode)) {
                            return false;
                        }
                        
                        double currentWeight = currentWeights.getOrDefault(stockCode, 0.0);
                        
                        // Stock의 targetWeight 필드 사용 (Portfolio에서 이미 업데이트됨)
                        return stock.exceedsThreshold(currentWeight);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("리밸런싱 필요 종목 식별 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    @Override
    public LocalDate getNextRebalancingDate(LocalDate currentDate, LocalDate lastRebalancingDate) {
        // 임계값 전략은 예정일이 없음 (조건 만족 시에만 실행)
        return null;
    }

    @Override
    public int getMinimumCheckInterval() {
        // 임계값 전략은 매일 체크 (1일 간격)
        return 1;
    }

    @Override
    public boolean validateConfiguration(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            log.error("종목 목록이 비어있습니다");
            return false;
        }

        try {
            // 원본 가중치 합계 검증
            int totalOriginalWeight = stocks.stream()
                    .mapToInt(Stock::getOriginalWeight)
                    .sum();

            if (totalOriginalWeight <= 0) {
                log.error("원본 가중치 합계가 0 이하입니다: {}", totalOriginalWeight);
                return false;
            }

            // 각 종목의 설정 검증
            for (Stock stock : stocks) {
                // 원본 가중치 검증
                if (stock.getOriginalWeight() <= 0) {
                    log.error("종목 {}의 원본 가중치가 잘못되었습니다: {}", 
                            stock.getStockCode(), stock.getOriginalWeight());
                    return false;
                }

                // 임계값 검증
                if (stock.getThresholdPercentage() <= 0 || stock.getThresholdPercentage() > 1.0) {
                    log.error("종목 {}의 임계값이 잘못되었습니다: {}%", 
                            stock.getStockCode(), String.format("%.2f", stock.getThresholdPercentage() * 100));
                    return false;
                }

                // 임계값이 목표 비중(계산된)보다 크면 경고
                double targetWeight = (double) stock.getOriginalWeight() / totalOriginalWeight;
                if (stock.getThresholdPercentage() > targetWeight) {
                    log.warn("종목 {}의 임계값({}%)이 목표비중({}%)보다 큽니다", 
                            stock.getStockCode(), 
                            String.format("%.2f", stock.getThresholdPercentage() * 100), 
                            String.format("%.2f", targetWeight * 100));
                }
            }

            return true;

        } catch (Exception e) {
            log.error("설정 검증 중 오류 발생", e);
            return false;
        }
    }

    @Override
    public Map<String, Double> predictPerformance(Object historicalData) {
        // 임계값 전략의 예상 성과 지표
        return Map.of(
            "expected_rebalancing_frequency", 8.0, // 연간 약 8회 예상
            "expected_transaction_cost", 0.012, // 연간 약 1.2% 거래비용 예상
            "volatility_impact", -0.05, // 변동성 5% 감소 효과
            "risk_adjusted_return", 0.02, // 위험조정수익률 2% 개선
            "max_drawdown_reduction", -0.03 // 최대낙폭 3% 감소
        );
    }

    @Override
    public double calculateStrategyScore(Map<String, Object> currentConditions) {
        double baseScore = 70.0;

        try {
            // 시장 변동성이 높을수록 임계값 전략이 유리
            if (currentConditions.containsKey("market_volatility")) {
                double volatility = (Double) currentConditions.get("market_volatility");
                if (volatility > 0.20) { // 20% 이상 변동성
                    baseScore += 15.0;
                } else if (volatility > 0.15) { // 15% 이상 변동성
                    baseScore += 10.0;
                } else if (volatility < 0.10) { // 10% 미만 변동성
                    baseScore -= 10.0;
                }
            }

            // 거래비용이 높을수록 임계값 전략이 유리
            if (currentConditions.containsKey("transaction_cost")) {
                double transactionCost = (Double) currentConditions.get("transaction_cost");
                if (transactionCost > 0.005) { // 0.5% 이상
                    baseScore += 10.0;
                } else if (transactionCost > 0.002) { // 0.2% 이상
                    baseScore += 5.0;
                }
            }

            // 포트폴리오 크기가 클수록 임계값 전략이 유리
            if (currentConditions.containsKey("portfolio_size")) {
                int portfolioSize = (Integer) currentConditions.get("portfolio_size");
                if (portfolioSize >= 10) {
                    baseScore += 10.0;
                } else if (portfolioSize >= 5) {
                    baseScore += 5.0;
                }
            }

        } catch (Exception e) {
            log.warn("전략 점수 계산 중 오류 발생", e);
        }

        return Math.min(Math.max(baseScore, 0.0), 100.0);
    }

    @Override
    public boolean isEffectiveInMarketCondition(String marketCondition) {
        switch (marketCondition.toUpperCase()) {
            case "BULL":
                return true;  // 상승장에서 효과적 (과도한 거래 방지)
            case "BEAR":
                return true;  // 하락장에서 효과적 (손실 제한)
            case "SIDEWAYS":
                return false; // 횡보장에서는 주기적 리밸런싱이 더 효과적
            case "VOLATILE":
                return true;  // 변동성 큰 시장에서 매우 효과적
            default:
                return true;
        }
    }

    @Override
    public List<String> getRecommendations() {
        return List.of(
            "각 종목별로 적절한 임계값을 설정하세요 (일반적으로 5-10%)",
            "시장 변동성이 높을 때 임계값을 낮춰서 더 자주 리밸런싱하는 것을 고려하세요",
            "거래비용이 높은 시장에서는 임계값을 높여 거래 빈도를 줄이세요",
            "목표 비중이 낮은 종목일수록 상대적으로 낮은 임계값을 설정하세요",
            "정기적으로 임계값 설정의 효과를 점검하고 조정하세요"
        );
    }

    @Override
    public String getRiskLevel() {
        return "MEDIUM";
    }

    @Override
    public double getMinimumCapitalRequirement() {
        return 5000000.0; // 500만원 (임계값 전략은 어느 정도 큰 자본에서 효과적)
    }

    /**
     * 임계값 초과 정도를 계산한다 (Stock의 targetWeight 필드 사용)
     * 
     * @param stock 대상 종목
     * @param currentWeight 현재 비중
     * @return 초과 정도 (0 이상, 클수록 초과 정도가 큼)
     */
    public double calculateExcessAmount(Stock stock, double currentWeight) {
        double deviation = Math.abs(currentWeight - stock.getTargetWeight());
        double threshold = stock.getThresholdPercentage();
        
        return Math.max(0, deviation - threshold);
    }
    

    /**
     * 종목별 리밸런싱 우선순위를 계산한다
     * 
     * @param stocks 종목 목록
     * @param currentWeights 현재 비중
     * @return 우선순위별로 정렬된 종목 목록
     */
    public List<Stock> getRebalancingPriority(List<Stock> stocks, Map<String, Double> currentWeights) {
        // 유효한 가격을 가진 종목들만 필터링
        Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(currentWeights);
        
        return stocks.stream()
                .filter(stock -> {
                    String stockCode = stock.getStockCode();
                    
                    // 유효한 가격이 없는 종목은 제외
                    if (!validPrices.containsKey(stockCode)) {
                        return false;
                    }
                    
                    double currentWeight = currentWeights.getOrDefault(stockCode, 0.0);
                    
                    // Stock의 targetWeight 필드 사용 (Portfolio에서 이미 업데이트됨)
                    return stock.exceedsThreshold(currentWeight);
                })
                .sorted((a, b) -> {
                    double weightA = currentWeights.getOrDefault(a.getStockCode(), 0.0);
                    double weightB = currentWeights.getOrDefault(b.getStockCode(), 0.0);
                    
                    // Stock의 targetWeight 필드를 사용하여 초과량 계산
                    double excessA = calculateExcessAmount(a, weightA);
                    double excessB = calculateExcessAmount(b, weightB);
                    
                    return Double.compare(excessB, excessA); // 내림차순 정렬
                })
                .collect(Collectors.toList());
    }

    /**
     * 전략의 효율성 지표를 계산한다
     * 
     * @param rebalancingCount 리밸런싱 횟수
     * @param totalReturn 총 수익률
     * @param transactionCost 총 거래비용
     * @return 효율성 지표
     */
    public double calculateEfficiency(int rebalancingCount, double totalReturn, double transactionCost) {
        if (rebalancingCount == 0 || transactionCost <= 0) {
            return 0.0;
        }
        
        // 리밸런싱 1회당 순수익률
        double netReturnPerRebalancing = (totalReturn - transactionCost) / rebalancingCount;
        
        // 거래비용 대비 수익률
        double costEfficiency = totalReturn / transactionCost;
        
        // 종합 효율성 지표
        return netReturnPerRebalancing * Math.log(1 + costEfficiency);
    }
}