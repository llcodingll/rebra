package com.rebra.calculator.strategy;

import com.rebra.calculator.context.BacktestContext;
import com.rebra.calculator.domain.Portfolio;
import com.rebra.calculator.domain.Stock;
import com.rebra.calculator.enums.RebalancingPeriod;
import com.rebra.calculator.util.PriceDataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rebra.calculator.constant.BacktestConstants.Rebalancing.WEIGHT_TOLERANCE;

/**
 * 주기적 리밸런싱 전략
 * 설정된 주기(월말, 분기말 등)마다 무조건 리밸런싱을 실행하는 전략
 * 
 * 특징:
 * - 규칙적이고 예측 가능한 리밸런싱
 * - 장기적으로 일관된 포트폴리오 유지
 * - 감정적 판단 배제
 * - 시장 상황과 무관하게 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PeriodicRebalancingStrategy implements RebalancingStrategy {
    
    private RebalancingPeriod rebalancingPeriod = RebalancingPeriod.MONTHLY; // 기본값

    /**
     * 리밸런싱 주기를 설정한다
     * 
     * @param period 리밸런싱 주기
     */
    public void setRebalancingPeriod(RebalancingPeriod period) {
        this.rebalancingPeriod = period != null ? period : RebalancingPeriod.MONTHLY;
        log.debug("리밸런싱 주기 설정: {}", this.rebalancingPeriod.getDisplayName());
    }

    /**
     * 현재 설정된 리밸런싱 주기를 반환한다
     * 
     * @return 리밸런싱 주기
     */
    public RebalancingPeriod getRebalancingPeriod() {
        return this.rebalancingPeriod;
    }

    @Override
    public boolean shouldRebalance(Map<String, Double> currentPrices, BacktestContext context, LocalDate currentDate, Portfolio portfolio,
                                 LocalDate lastRebalancingDate) {
        // 유효한 가격 정보 확인
        if (currentPrices == null) {
            log.warn("날짜 {}의 가격 정보를 찾을 수 없습니다", currentDate);
            return false;
        }
        
        Map<String, Double> validPrices = PriceDataUtils.filterValidPrices(currentPrices);
        if (validPrices.isEmpty()) {
            log.warn("유효한 가격 정보가 없어 주기적 리밸런싱을 건너뜁니다 - 날짜: {}", currentDate);
            return false;
        }
        
        // 메인 서버에서 전달받은 리밸런싱 날짜인지 확인
        boolean isRebalancingDate = context.isRebalancingDate(currentDate);
        
        if (isRebalancingDate) {
            log.debug("주기적 리밸런싱 실행: {} (주기: {}, 유효 가격: {}개)", 
                    currentDate, rebalancingPeriod.getDisplayName(), validPrices.size());
        }
        
        return isRebalancingDate;
    }

    @Override
    public String getRebalancingReason(BacktestContext context, LocalDate currentDate, Portfolio portfolio,
                                     LocalDate lastRebalancingDate) {
        try {
            String periodName = rebalancingPeriod.name().toLowerCase();
            return String.format("PERIODIC_REBALANCING_%s", periodName.toUpperCase());

        } catch (Exception e) {
            log.error("리밸런싱 사유 계산 중 오류 발생", e);
            return "PERIODIC_REBALANCING_ERROR";
        }
    }

    @Override
    public String getRebalancingReasonKorean(BacktestContext context, LocalDate currentDate, Portfolio portfolio,
                                           LocalDate lastRebalancingDate) {
        String englishReason = getRebalancingReason(context, currentDate, portfolio, lastRebalancingDate);
        
        if (englishReason.equals("PERIODIC_REBALANCING_ERROR")) {
            return "주기적 리밸런싱 오류";
        }
        
        if (englishReason.startsWith("PERIODIC_REBALANCING_")) {
            return String.format("주기적 리밸런싱 (%s)", rebalancingPeriod.getDisplayName());
        }
        
        return "주기적 리밸런싱";
    }

    @Override
    public String getStrategyName() {
        return "Periodic Rebalancing";
    }

    @Override
    public String getStrategyDescription() {
        return String.format("설정된 주기(%s)마다 무조건 리밸런싱을 실행하는 전략. " +
               "규칙적이고 예측 가능하며, 장기적으로 일관된 포트폴리오 유지가 가능합니다. " +
               "시장 상황과 무관하게 실행되어 감정적 판단을 배제할 수 있습니다.",
               rebalancingPeriod.getDisplayName());
    }

    @Override
    public List<Stock> getStocksNeedingRebalancing(BacktestContext context, Portfolio portfolio) {
        if (portfolio == null || context == null) {
            return new ArrayList<>();
        }

        try {
            // 컨텍스트에서 종목 정보와 현재 가격 가져오기 (최신 날짜 기준)
            List<Stock> stocks = context.getStocks();
            LocalDate currentDate = context.getLastDate();
            Map<String, Double> currentPrices = context.getPricesForDate(currentDate);
            
            if (stocks == null || currentPrices == null) {
                return new ArrayList<>();
            }
            
            // 주기적 전략에서는 모든 종목이 리밸런싱 대상
            // 단, 현재 비중과 목표 비중이 크게 다르지 않은 종목은 제외
            // 유효한 가격을 가진 종목들만 고려
            
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
                        double targetWeight = stock.getTargetWeight();
                        double deviation = Math.abs(currentWeight - targetWeight);
                        
                        // 0.1% 이상 차이가 나는 종목만 리밸런싱 대상으로 포함
                        return deviation > 0.001;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("리밸런싱 필요 종목 식별 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    @Override
    public LocalDate getNextRebalancingDate(LocalDate currentDate, LocalDate lastRebalancingDate) {
        // 주기적 전략에서는 메인 서버에서 이미 적절한 날짜를 필터링하여 전송하므로
        // 이 메서드는 주로 예측/분석 목적으로만 사용
        if (currentDate == null) {
            return null;
        }

        try {
            return rebalancingPeriod.getNextRebalancingDate(currentDate);

        } catch (Exception e) {
            log.error("다음 리밸런싱 날짜 계산 중 오류 발생", e);
            return null;
        }
    }

    @Override
    public int getMinimumCheckInterval() {
        // 주기에 따라 체크 간격 설정
        switch (rebalancingPeriod) {
            case MONTHLY:
                return 7;   // 주별 체크
            case QUARTERLY:
                return 30;  // 월별 체크
            case SEMI_ANNUALLY:
                return 60;  // 2개월별 체크
            case ANNUALLY:
                return 90;  // 분기별 체크
            default:
                return 7;
        }
    }

    @Override
    public boolean validateConfiguration(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            log.error("종목 목록이 비어있습니다");
            return false;
        }

        if (rebalancingPeriod == null) {
            log.error("리밸런싱 주기가 설정되지 않았습니다");
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
                if (stock.getOriginalWeight() <= 0) {
                    log.error("종목 {}의 원본 가중치가 잘못되었습니다: {}", 
                            stock.getStockCode(), stock.getOriginalWeight());
                    return false;
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
        double rebalancingFrequency = getAnnualRebalancingFrequency();
        
        return Map.of(
            "expected_rebalancing_frequency", rebalancingFrequency,
            "expected_transaction_cost", rebalancingFrequency * 0.003, // 주기별 0.3% 거래비용 가정
            "volatility_impact", -0.08, // 변동성 8% 감소 효과
            "risk_adjusted_return", 0.01, // 위험조정수익률 1% 개선
            "max_drawdown_reduction", -0.05, // 최대낙폭 5% 감소
            "consistency_score", 0.85 // 일관성 점수 85%
        );
    }

    @Override
    public double calculateStrategyScore(Map<String, Object> currentConditions) {
        double baseScore = 75.0;

        try {
            // 시장이 횡보할 때 주기적 전략이 유리
            if (currentConditions.containsKey("market_trend")) {
                String trend = (String) currentConditions.get("market_trend");
                if ("SIDEWAYS".equalsIgnoreCase(trend)) {
                    baseScore += 15.0;
                } else if ("VOLATILE".equalsIgnoreCase(trend)) {
                    baseScore += 10.0;
                }
            }

            // 거래비용이 낮을수록 주기적 전략이 유리
            if (currentConditions.containsKey("transaction_cost")) {
                double transactionCost = (Double) currentConditions.get("transaction_cost");
                if (transactionCost < 0.001) { // 0.1% 미만
                    baseScore += 10.0;
                } else if (transactionCost > 0.005) { // 0.5% 이상
                    baseScore -= 15.0;
                }
            }

            // 투자 기간이 길수록 주기적 전략이 유리
            if (currentConditions.containsKey("investment_horizon")) {
                int months = (Integer) currentConditions.get("investment_horizon");
                if (months >= 36) { // 3년 이상
                    baseScore += 10.0;
                } else if (months >= 12) { // 1년 이상
                    baseScore += 5.0;
                } else if (months < 6) { // 6개월 미만
                    baseScore -= 10.0;
                }
            }

            // 리밸런싱 주기에 따른 점수 조정
            switch (rebalancingPeriod) {
                case MONTHLY:
                    baseScore += 5.0; // 적절한 주기
                    break;
                case QUARTERLY:
                    baseScore += 8.0; // 가장 효과적인 주기
                    break;
                case SEMI_ANNUALLY:
                    baseScore += 3.0; // 약간 긴 주기
                    break;
                case ANNUALLY:
                    baseScore -= 5.0; // 너무 긴 주기
                    break;
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
                return false; // 상승장에서는 임계값 전략이 더 효과적
            case "BEAR":
                return false; // 하락장에서는 임계값 전략이 더 효과적  
            case "SIDEWAYS":
                return true;  // 횡보장에서 매우 효과적
            case "VOLATILE":
                return false; // 변동성 큰 시장에서는 임계값 전략이 더 효과적
            default:
                return true;
        }
    }

    @Override
    public List<String> getRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        recommendations.add("장기 투자(1년 이상)에 적합한 전략입니다");
        recommendations.add("거래비용이 낮은 시장에서 더 효과적입니다");
        recommendations.add("감정적 투자를 피하고 규율적인 투자를 원하는 경우에 적합합니다");
        
        // 주기별 특화 권장사항
        switch (rebalancingPeriod) {
            case MONTHLY:
                recommendations.add("월말 리밸런싱은 적당한 빈도로 균형잡힌 선택입니다");
                break;
            case QUARTERLY:
                recommendations.add("분기별 리밸런싱은 가장 널리 사용되는 효과적인 주기입니다");
                recommendations.add("기업의 실적 발표 주기와 맞아 논리적입니다");
                break;
            case SEMI_ANNUALLY:
                recommendations.add("반기별 리밸런싱은 거래비용을 최소화하면서도 효과를 얻을 수 있습니다");
                break;
            case ANNUALLY:
                recommendations.add("연간 리밸런싱은 매우 보수적인 접근법입니다");
                recommendations.add("세금 효율성을 고려할 때 장점이 있습니다");
                break;
        }
        
        return recommendations;
    }

    @Override
    public String getRiskLevel() {
        // 리밸런싱 주기에 따라 위험 수준 결정
        switch (rebalancingPeriod) {
            case MONTHLY:
                return "MEDIUM";
            case QUARTERLY:
                return "MEDIUM";
            case SEMI_ANNUALLY:
                return "LOW";
            case ANNUALLY:
                return "LOW";
            default:
                return "MEDIUM";
        }
    }

    @Override
    public double getMinimumCapitalRequirement() {
        // 주기별로 다른 최소 자본 요구
        switch (rebalancingPeriod) {
            case MONTHLY:
                return 3000000.0; // 300만원
            case QUARTERLY:
                return 2000000.0; // 200만원
            case SEMI_ANNUALLY:
                return 1500000.0; // 150만원
            case ANNUALLY:
                return 1000000.0; // 100만원
            default:
                return 2000000.0;
        }
    }

    /**
     * 연간 리밸런싱 빈도를 계산한다
     * 
     * @return 연간 리밸런싱 횟수
     */
    public double getAnnualRebalancingFrequency() {
        switch (rebalancingPeriod) {
            case MONTHLY:
                return 12.0;
            case QUARTERLY:
                return 4.0;
            case SEMI_ANNUALLY:
                return 2.0;
            case ANNUALLY:
                return 1.0;
            default:
                return 12.0;
        }
    }

    /**
     * 해당 연도의 리밸런싱 예정일 목록을 반환한다
     * 
     * @param year 연도
     * @return 리밸런싱 예정일 목록
     */
    public List<LocalDate> getRebalancingDatesInYear(int year) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (rebalancingPeriod.isRebalancingDate(currentDate)) {
                dates.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return dates;
    }

    /**
     * 주기적 전략의 규칙성 점수를 계산한다
     * 
     * @param executedDates 실제 실행된 리밸런싱 날짜들
     * @param expectedDates 예상 리밸런싱 날짜들
     * @return 규칙성 점수 (0.0 ~ 1.0)
     */
    public double calculateConsistencyScore(List<LocalDate> executedDates, List<LocalDate> expectedDates) {
        if (expectedDates == null || expectedDates.isEmpty()) {
            return 0.0;
        }
        
        if (executedDates == null || executedDates.isEmpty()) {
            return 0.0;
        }
        
        long matchCount = expectedDates.stream()
                .mapToLong(expected -> executedDates.contains(expected) ? 1 : 0)
                .sum();
        
        return (double) matchCount / expectedDates.size();
    }

    /**
     * 전략 실행 후 다음 리밸런싱까지의 예상 수익률 편차를 계산한다
     * 
     * @return 예상 편차 (표준편차)
     */
    public double getExpectedReturnDeviation() {
        // 주기가 길수록 편차가 커짐
        switch (rebalancingPeriod) {
            case MONTHLY:
                return 0.05; // 5%
            case QUARTERLY:
                return 0.08; // 8%
            case SEMI_ANNUALLY:
                return 0.12; // 12%
            case ANNUALLY:
                return 0.18; // 18%
            default:
                return 0.08;
        }
    }
}