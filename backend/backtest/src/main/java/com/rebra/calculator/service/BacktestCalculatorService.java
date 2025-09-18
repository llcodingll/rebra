package com.rebra.calculator.service;

import com.rebra.calculator.constant.BacktestConstants;
import com.rebra.calculator.context.BacktestContext;
import com.rebra.calculator.domain.Portfolio;
import com.rebra.calculator.domain.Stock;
import com.rebra.calculator.domain.Trade;
import com.rebra.calculator.dto.*;
import com.rebra.calculator.enums.RebalancingPeriod;
import com.rebra.calculator.enums.RebalancingType;
import com.rebra.calculator.strategy.PeriodicRebalancingStrategy;
import com.rebra.calculator.strategy.RebalancingStrategy;
import com.rebra.calculator.strategy.ThresholdRebalancingStrategy;
import com.rebra.calculator.util.PriceDataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rebra.calculator.constant.BacktestConstants.Calculation.*;

/**
 * 백테스트 계산의 메인 서비스 클래스
 * 모든 구성요소를 결합하여 완전한 백테스트를 수행한다.
 * 
 * 주요 기능:
 * - 백테스트 요청 처리 및 검증
 * - 적절한 리밸런싱 전략 선택 및 실행
 * - 일일 포트폴리오 가치 계산 및 차입비용 처리
 * - 성과 지표 계산 및 결과 생성
 * - 상세한 거래 내역 및 차입 기록 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BacktestCalculatorService {

    private final PortfolioManagerService portfolioManagerService;
    private final FeeCalculatorService feeCalculatorService;
    private final ThresholdRebalancingStrategy thresholdStrategy;
    private final PeriodicRebalancingStrategy periodicStrategy;
    
    // BacktestConstants에서 import된 상수 사용
    // TRADING_DAYS_PER_YEAR, RISK_FREE_RATE

    /**
     * 백테스트를 실행하고 결과를 반환한다
     * 
     * @param request 백테스트 요청
     * @return 백테스트 결과
     * @throws IllegalArgumentException 잘못된 요청 데이터
     * @throws RuntimeException 계산 중 오류 발생
     */
    public BacktestResponse executeBacktest(BacktestRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("백테스트 계산 시작 - ID: {}, 기간: {} ~ {}, 종목수: {}개",
                    request.getBacktestId(), request.getStartDate(), request.getEndDate(), 
                    request.getStocks().size());

            // 1. 요청 데이터 검증
            validateBacktestRequest(request);

            // 2. 백테스트 데이터 준비
            BacktestContext context = prepareBacktestContext(request);

            // 3. 초기 포트폴리오 구성
            Portfolio portfolio = createInitialPortfolio(context);

            // 4. 백테스트 실행
            BacktestResult result = runBacktest(portfolio, context);

            // 5. 결과 생성
            long calculationTime = System.currentTimeMillis() - startTime;
            BacktestResponse response = buildBacktestResponse(request.getBacktestId(), result, calculationTime);

            log.info(String.format("백테스트 계산 완료 - ID: %d, 소요시간: %dms, 총수익률: %.2f%%",
                    request.getBacktestId(), calculationTime, result.getSummary().getTotalReturn() * 100));

            return response;

        } catch (IllegalArgumentException e) {
            long calculationTime = System.currentTimeMillis() - startTime;
            log.error("백테스트 요청 데이터 오류 - ID: {}", request.getBacktestId(), e);
            return BacktestResponse.failure(request.getBacktestId(), e.getMessage(), calculationTime);

        } catch (Exception e) {
            long calculationTime = System.currentTimeMillis() - startTime;
            log.error("백테스트 계산 중 오류 발생 - ID: {}", request.getBacktestId(), e);
            return BacktestResponse.failure(request.getBacktestId(), "계산 중 오류 발생: " + e.getMessage(), calculationTime);
        }
    }

    /**
     * 백테스트 요청의 유효성을 검증한다
     */
    private void validateBacktestRequest(BacktestRequest request) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("백테스트 요청 데이터가 유효하지 않습니다");
        }

        // 추가 비즈니스 로직 검증
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("시작일이 종료일보다 늦습니다");
        }

        if (request.getBacktestPeriodDays() < BacktestConstants.Validation.MINIMUM_BACKTEST_PERIOD_DAYS) {
            throw new IllegalArgumentException("백테스트 기간이 너무 짧습니다 (최소 " + BacktestConstants.Validation.MINIMUM_BACKTEST_PERIOD_DAYS + "일)");
        }

        if (request.getStocks().size() < BacktestConstants.Validation.MINIMUM_STOCK_COUNT) {
            throw new IllegalArgumentException("최소 " + BacktestConstants.Validation.MINIMUM_STOCK_COUNT + "개 이상의 종목이 필요합니다");
        }

        log.debug("백테스트 요청 검증 완료 - 종목수: {}, 기간: {}일", 
                request.getStocks().size(), request.getBacktestPeriodDays());
    }

    /**
     * 백테스트 실행을 위한 컨텍스트를 준비한다
     * BacktestRequest에서 필요한 데이터만 추출하여 도메인 객체로 변환
     */
    private BacktestContext prepareBacktestContext(BacktestRequest request) {
        // 종목 정보 변환 (원본 가중치 기반)
        List<Stock> stocks = request.getStocks().stream()
                .map(BacktestStockDto::toDomain)
                .collect(Collectors.toList());
        
        // 일별 가격 데이터를 LocalDate 키로 변환
        LinkedHashMap<LocalDate, Map<String, Double>> dailyPrices = 
                convertDailyPricesToLocalDateMap(request.getDailyPrices());
        
        // 리밸런싱 전략 선택
        RebalancingStrategy rebalancingStrategy = selectRebalancingStrategy(request);
        
        // Context 생성 (request 참조 없이)
        BacktestContext context = new BacktestContext(
                request.getBacktestId(),
                request.getStartDate(),
                request.getEndDate(),
                stocks,
                dailyPrices,
                rebalancingStrategy,
                request.getRebalancingType(),
                request.getRebalancingPeriod(),
                request.getRebalancingDates()
        );
        
        log.debug("백테스트 컨텍스트 준비 완료 - 거래일: {}일, 전략: {}", 
                dailyPrices.size(), rebalancingStrategy.getStrategyName());
        
        return context;
    }

    /**
     * 요청의 일별 가격 데이터를 LocalDate 키를 가진 LinkedHashMap으로 변환한다
     * 
     * @param dailyPrices 요청에서 받은 일별 가격 데이터 (String 키)
     * @return LocalDate 키를 가진 일별 가격 데이터
     */
    private LinkedHashMap<LocalDate, Map<String, Double>> convertDailyPricesToLocalDateMap(
            Map<String, Map<String, Double>> dailyPrices) {
        return dailyPrices.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> LocalDate.parse(entry.getKey()),
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * 요청에 따른 적절한 리밸런싱 전략을 선택한다
     */
    private RebalancingStrategy selectRebalancingStrategy(BacktestRequest request) {
        RebalancingType type = request.getRebalancingType();
        
        if (type == RebalancingType.THRESHOLD) {
            return thresholdStrategy;
        } else if (type == RebalancingType.PERIODIC) {
            RebalancingPeriod period = request.getRebalancingPeriod();
            if (period != null) {
                periodicStrategy.setRebalancingPeriod(period);
            }
            return periodicStrategy;
        } else {
            throw new IllegalArgumentException("지원하지 않는 리밸런싱 유형: " + type);
        }
    }

    /**
     * 초기 포트폴리오를 구성한다
     */
    private Portfolio createInitialPortfolio(BacktestContext context) {
        Map<String, Double> initialPrices = context.getPricesForDate(context.getStartDate());
        
        if (initialPrices == null || initialPrices.isEmpty()) {
            throw new RuntimeException("시작일의 가격 정보가 없습니다: " + context.getStartDate());
        }

        Portfolio portfolio = portfolioManagerService.createInitialPortfolio(
            context.getStocks(), 
            initialPrices, 
            context.getStartDate()
        );

        log.info(String.format("초기 포트폴리오 구성 완료 - 현금잔액: %.0f원, 보유종목수: %d, 초기가치: %.0f원", 
                portfolio.getCash(), portfolio.getHoldingStockCodes().size(), portfolio.getInitialValue()));

        return portfolio;
    }

    /**
     * 실제 백테스트를 실행한다
     */
    private BacktestResult runBacktest(Portfolio portfolio, BacktestContext context) {
        BacktestResult result = new BacktestResult();
        result.setDetails(new ArrayList<>());
        
        LocalDate lastRebalancingDate = null; // 아직 리밸런싱이 발생하지 않음
        int rebalancingCount = 0; // 초기 구성은 리밸런싱이 아님
        LocalDate previousProcessedDate = context.getStartDate(); // 이전 처리 날짜 추적
        
        // Portfolio 객체에서 초기 가치 가져오기
        result.setInitialValue(portfolio.getInitialValue());
        
        log.info("백테스트 실행 시작 - 초기 구성: 보유주식 기반, 첫 리밸런싱: 목표 비중 달성을 위한 거래");
        
        // 각 거래일별로 처리
        for (LocalDate currentDate : context.getDailyPrices().keySet()) {
            Map<String, Double> currentPrices = context.getPricesForDate(currentDate);
            
            if (currentPrices == null || currentPrices.isEmpty()) {
                log.warn("날짜 {}의 가격 정보가 없습니다", currentDate);
                continue;
            }

            // 실제 경과 일수 계산
            int daysPassed = (int) ChronoUnit.DAYS.between(previousProcessedDate, currentDate);
            if (daysPassed <= 0) {
                daysPassed = 1; // 최소 1일
            }
            
            // 차입 이자 계산 (실제 경과 기간 적용)
            portfolio.calculateAndDeductBorrowingCost(currentDate, daysPassed);
            
            // 리밸런싱 필요 여부 확인 (Portfolio 자체에서 판단)
            boolean shouldRebalance = portfolio.shouldRebalance(currentPrices, context, currentDate, lastRebalancingDate);
            
            // 리밸런싱 실행
            List<Trade> rebalancingTrades = null;
            if (shouldRebalance) {
                rebalancingTrades = portfolioManagerService.executeRebalancing(
                    portfolio, currentPrices, context, currentDate);
                
                if (!rebalancingTrades.isEmpty()) {
                    rebalancingCount++;
                    lastRebalancingDate = currentDate;
                    
                    
                    if (rebalancingCount == 1) {
                        log.info("첫 번째 리밸런싱 실행 - 날짜: {}, 거래수: {}", 
                                currentDate, rebalancingTrades.size());
                    } else {
                        log.debug("리밸런싱 실행 - 날짜: {}, 거래수: {}", 
                                currentDate, rebalancingTrades.size());
                    }
                } else {
                    rebalancingTrades = null; // 빈 리스트면 null로 설정
                }
            }

            // 일일 상세 기록 추가
            addDailyDetail(result, portfolio, currentPrices, currentDate, shouldRebalance, 
                          rebalancingTrades);
            
            // 다음 반복을 위해 현재 날짜를 이전 처리 날짜로 업데이트
            previousProcessedDate = currentDate;
        }
        
        // 최종 결과 계산
        calculateFinalResults(result, portfolio, context, rebalancingCount);
        
        return result;
    }

    /**
     * 차입 기록을 결과에 추가한다
     */


    /**
     * 일일 상세 기록을 결과에 추가한다
     */
    private void addDailyDetail(BacktestResult result, Portfolio portfolio, Map<String, Double> currentPrices,
                               LocalDate currentDate, boolean wasRebalanced, List<Trade> rebalancingTrades) {
        double portfolioValue = portfolio.getTotalValue(currentPrices);
        double cumulativeReturn = portfolio.getCumulativeReturn(currentPrices);
        
        // 이전 기간 수익률 계산
        double periodReturn = 0.0;
        if (!result.getDetails().isEmpty()) {
            BacktestDetailDto lastDetail = result.getDetails().get(result.getDetails().size() - 1);
            double previousValue = lastDetail.getPortfolioValue();
            if (previousValue > 0) {
                periodReturn = (portfolioValue - previousValue) / previousValue;
            }
        }
        
        // 리밸런싱 총 매수/매도 금액 계산
        double totalBuyAmount = 0.0;
        double totalSellAmount = 0.0;
        
        if (rebalancingTrades != null && !rebalancingTrades.isEmpty()) {
            totalBuyAmount = rebalancingTrades.stream()
                .filter(Trade::isBuy)
                .mapToDouble(Trade::getAmount)
                .sum();
                
            totalSellAmount = rebalancingTrades.stream()
                .filter(Trade::isSell)
                .mapToDouble(Trade::getAmount)
                .sum();
        }
        
        BacktestDetailDto detail = new BacktestDetailDto();
        detail.setPeriodDate(currentDate);
        detail.setPortfolioValue(portfolioValue);
        detail.setPeriodReturn(periodReturn);
        detail.setIsRebalanced(wasRebalanced);
        detail.setCashBalance(portfolio.getCash());
        detail.setDailyBorrowingInterest(portfolio.isBorrowing() ? 
            feeCalculatorService.calculateDailyBorrowingInterest(portfolio.getCurrentBorrowingAmount()) : 0.0);
        detail.setCumulativeReturn(cumulativeReturn);
        detail.setTotalBuyAmount(totalBuyAmount);
        detail.setTotalSellAmount(totalSellAmount);
        
        // Buy-and-hold 수익률 및 가치 계산 (매일)
        // Portfolio의 targetStocks에서 초기 수량 가져오기
        Map<String, Stock> targetStocks = portfolio.getTargetStocks();
        double buyHoldValue = targetStocks.values().stream()
                .mapToDouble(stock -> {
                    String stockCode = stock.getStockCode();
                    int initialQty = stock.getInitialQuantity();
                    Double price = currentPrices.get(stockCode);
                    return (price != null && price > 0) 
                        ? initialQty * price : 0.0;
                })
                .sum();
        double buyHoldReturn = (buyHoldValue - portfolio.getInitialValue()) / portfolio.getInitialValue();
        detail.setBuyHoldValue(buyHoldValue);
        detail.setBuyHoldReturn(buyHoldReturn);
        
        result.getDetails().add(detail);
    }

    /**
     * 최종 결과를 계산한다
     */
    private void calculateFinalResults(BacktestResult result, Portfolio portfolio, 
                                     BacktestContext context, int rebalancingCount) {
        // 기본 정보
        double initialValue = portfolio.getInitialValue();
        
        // 마지막 거래일의 가격 데이터 가져오기
        LocalDate lastDate = context.getLastDate();
        Map<String, Double> finalPrices = context.getPricesForDate(lastDate);
        double finalValue = portfolio.getTotalValue(finalPrices);
        double totalReturn = (finalValue - initialValue) / initialValue;
        
        // 바이앤홀드 수익률 계산
        Map<String, Double> initialPrices = context.getPricesForDate(context.getStartDate());
        
        double buyHoldValue = portfolioManagerService.calculateBuyAndHoldValue(
            context.getInitialQuantities(), 
            PriceDataUtils.filterValidPrices(initialPrices), 
            PriceDataUtils.filterValidPrices(finalPrices));
        double buyHoldReturn = (buyHoldValue - initialValue) / initialValue;
        
        // 성과 지표 계산
        List<Double> periodReturns = result.getDetails().stream()
                .map(BacktestDetailDto::getPeriodReturn)
                .collect(Collectors.toList());
        
        List<Double> portfolioValues = result.getDetails().stream()
                .map(BacktestDetailDto::getPortfolioValue)
                .collect(Collectors.toList());
        double maxDrawdown = portfolioManagerService.calculateMaxDrawdown(portfolioValues);
        
        // 추가 수익률 지표 계산
        double periodGrowthRate = calculatePeriodGrowthRate(periodReturns);
        double volatility = calculateVolatility(periodReturns, periodGrowthRate);
        double annualizedReturn = calculateAnnualizedReturn(totalReturn, context.getStartDate(), context.getEndDate());
        double sharpeRatio = calculateSharpeRatio(annualizedReturn, volatility);
        double timeWeightedReturn = calculateTimeWeightedReturn(periodReturns);
        
        // 요약 결과 생성
        result.setSummary(createSummary(finalValue, totalReturn, buyHoldReturn, 
                                     maxDrawdown, rebalancingCount, portfolio, 
                                     periodGrowthRate, volatility, sharpeRatio, timeWeightedReturn));
        
        log.info(String.format("최종 결과 계산 완료 - 최종가치: %.0f원, 총수익률: %.2f%%, TWR: %.2f%%, 바이앤홀드: %.2f%%, " +
                        "최대낙폭: %.2f%%, 변동성: %.2f%%, 연환산수익률: %.2f%%, 샤프비율: %.2f, 주기성장률: %.4f",
                finalValue, totalReturn * 100, timeWeightedReturn * 100, buyHoldReturn * 100, 
                maxDrawdown * 100, volatility * 100, annualizedReturn * 100, sharpeRatio, periodGrowthRate));
    }

    /**
     * 백테스트 요약을 생성한다
     */
    private BacktestSummaryDto createSummary(double finalValue, double totalReturn, double buyHoldReturn,
                                           double maxDrawdown, int rebalancingCount, Portfolio portfolio,
                                           double periodGrowthRate, double volatility, double sharpeRatio, double timeWeightedReturn) {
        BacktestSummaryDto summary = new BacktestSummaryDto();
        summary.setFinalValue(finalValue);
        summary.setTotalReturn(totalReturn);
        summary.setBuyHoldReturn(buyHoldReturn);
        summary.setMaxDrawdown(maxDrawdown);
        summary.setRebalancingCount(rebalancingCount);
        summary.setTotalFee(portfolio.getTotalTradingCost());
        summary.setTotalBorrowingCost(portfolio.getTotalBorrowingCost());
        summary.setMaxBorrowingAmount(portfolio.getMaxBorrowingAmount());
        summary.setMinCashBalance(portfolio.getMinCashBalance());
        summary.setPeriodGrowthRate(periodGrowthRate);
        summary.setVolatility(volatility);
        summary.setSharpeRatio(sharpeRatio);
        summary.setTimeWeightedReturn(timeWeightedReturn);
        
        return summary;
    }

    /**
     * 백테스트 응답을 생성한다
     */
    private BacktestResponse buildBacktestResponse(Long backtestId, BacktestResult result, long calculationTime) {
        return BacktestResponse.success(
            backtestId,
            result.getSummary(),
            result.getDetails(),
            calculationTime
        );
    }

    // Helper 메서드들
    
    /**
     * 주기별 평균 성장률을 계산한다
     * 
     * @param periodReturns 각 기간별 수익률 리스트
     * @return 평균 성장률
     */
    private double calculatePeriodGrowthRate(List<Double> periodReturns) {
        if (periodReturns == null || periodReturns.isEmpty()) {
            return 0.0;
        }
        
        return periodReturns.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
    
    /**
     * 변동성을 계산한다 (일일 수익률의 연환산 표준편차)
     * 
     * @param periodReturns 각 기간별 수익률 리스트
     * @param meanReturn 평균 수익률
     * @return 연환산 변동성
     */
    private double calculateVolatility(List<Double> periodReturns, double meanReturn) {
        if (periodReturns == null || periodReturns.size() < 2) {
            return 0.0;
        }
        
        // 분산 계산
        double variance = periodReturns.stream()
                .mapToDouble(returns -> Math.pow(returns - meanReturn, 2))
                .average()
                .orElse(0.0);
        
        // 표준편차 계산 후 연환산
        double dailyStandardDeviation = Math.sqrt(variance);
        return dailyStandardDeviation * Math.sqrt(TRADING_DAYS_PER_YEAR);
    }
    
    /**
     * 연환산 수익률을 계산한다
     * 
     * @param totalReturn 총 수익률
     * @param startDate 백테스트 시작일
     * @param endDate 백테스트 종료일
     * @return 연환산 수익률
     */
    private double calculateAnnualizedReturn(double totalReturn, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
            return 0.0;
        }
        
        // 실제 경과 일수 계산
        long actualDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (actualDays <= 0) {
            return 0.0;
        }
        
        // 복리 계산: (1 + 총수익률)^(365/실제경과일수) - 1
        double compoundGrowthRate = Math.pow(1 + totalReturn, 365.0 / actualDays);
        return compoundGrowthRate - 1.0;
    }
    
    /**
     * 샤프 비율을 계산한다
     * 
     * @param annualizedReturn 연환산 수익률
     * @param volatility 연환산 변동성
     * @return 샤프 비율
     */
    private double calculateSharpeRatio(double annualizedReturn, double volatility) {
        if (volatility <= 0) {
            return 0.0;
        }
        
        return (annualizedReturn - RISK_FREE_RATE) / volatility;
    }
    
    /**
     * 시간 가중 수익률을 계산한다
     * 각 기간의 수익률을 복리로 계산하여 리밸런싱 영향을 제거한 순수 투자 성과를 측정
     * 
     * @param periodReturns 각 기간별 수익률 리스트
     * @return 시간 가중 수익률
     */
    private double calculateTimeWeightedReturn(List<Double> periodReturns) {
        if (periodReturns == null || periodReturns.isEmpty()) {
            return 0.0;
        }
        
        // 각 기간 수익률을 (1 + r)로 변환하여 곱한 후 1을 빼서 최종 수익률 계산
        double twrProduct = periodReturns.stream()
                .map(r -> 1 + r)  // (1 + 수익률)로 변환
                .reduce(1.0, (a, b) -> a * b);  // 곱셈 누적
        
        double timeWeightedReturn = twrProduct - 1.0;
        
        log.debug(String.format("시간 가중 수익률 계산 완료 - 기간수: %d, TWR: %.4f", 
                periodReturns.size(), timeWeightedReturn));
        
        return timeWeightedReturn;
    }
    

}