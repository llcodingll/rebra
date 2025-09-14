package com.rebra.calculator.strategy;

import com.rebra.calculator.context.BacktestContext;
import com.rebra.calculator.domain.Portfolio;
import com.rebra.calculator.domain.Stock;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 리밸런싱 전략을 정의하는 인터페이스
 * 다양한 리밸런싱 조건과 방법을 구현하기 위한 전략 패턴을 제공한다.
 */
public interface RebalancingStrategy {

    /**
     * 리밸런싱이 필요한지 판단한다
     * 각 전략의 고유한 조건에 따라 리밸런싱 필요 여부를 결정
     * 
     * @param context 백테스트 컨텍스트 (전략, 종목, 가격 정보 포함)
     * @param currentDate 현재 날짜
     * @param portfolio 현재 포트폴리오 상태
     * @param lastRebalancingDate 마지막 리밸런싱 실행 날짜 (null이면 최초)
     * @return 리밸런싱이 필요하면 true
     */
    boolean shouldRebalance(Map<String, Double> currentPrices, BacktestContext context, LocalDate currentDate, Portfolio portfolio,
                          LocalDate lastRebalancingDate);

    /**
     * 리밸런싱 필요 사유를 반환한다
     * 왜 리밸런싱이 필요한지에 대한 설명을 제공
     * 
     * @param context 백테스트 컨텍스트 (전략, 종목, 가격 정보 포함)
     * @param currentDate 현재 날짜
     * @param portfolio 현재 포트폴리오 상태
     * @param lastRebalancingDate 마지막 리밸런싱 실행 날짜
     * @return 리밸런싱 사유 (영문)
     */
    String getRebalancingReason(BacktestContext context, LocalDate currentDate, Portfolio portfolio,
                               LocalDate lastRebalancingDate);

    /**
     * 리밸런싱 사유를 한국어로 반환한다
     * 
     * @param context 백테스트 컨텍스트 (전략, 종목, 가격 정보 포함)
     * @param currentDate 현재 날짜
     * @param portfolio 현재 포트폴리오 상태
     * @param lastRebalancingDate 마지막 리밸런싱 실행 날짜
     * @return 리밸런싱 사유 (한국어)
     */
    String getRebalancingReasonKorean(BacktestContext context, LocalDate currentDate, Portfolio portfolio,
                                     LocalDate lastRebalancingDate);

    /**
     * 전략의 이름을 반환한다
     * 
     * @return 전략 이름
     */
    String getStrategyName();

    /**
     * 전략에 대한 설명을 반환한다
     * 
     * @return 전략 설명
     */
    String getStrategyDescription();

    /**
     * 리밸런싱 필요 종목을 식별한다
     * 전체 종목 중에서 실제로 리밸런싱이 필요한 종목들만 필터링
     * 
     * @param context 백테스트 컨텍스트 (전략, 종목, 가격 정보 포함)
     * @param portfolio 현재 포트폴리오 상태
     * @return 리밸런싱이 필요한 종목 목록
     */
    List<Stock> getStocksNeedingRebalancing(BacktestContext context, Portfolio portfolio);

    /**
     * 다음 리밸런싱 예정일을 계산한다
     * 주기적 전략에서 사용되며, 임계값 전략에서는 null을 반환할 수 있음
     * 
     * @param currentDate 현재 날짜
     * @param lastRebalancingDate 마지막 리밸런싱 날짜
     * @return 다음 리밸런싱 예정일 (없으면 null)
     */
    LocalDate getNextRebalancingDate(LocalDate currentDate, LocalDate lastRebalancingDate);

    /**
     * 리밸런싱 실행 후 다음번 체크까지의 최소 간격을 반환한다
     * 성능 최적화를 위해 불필요한 체크를 줄이는 용도
     * 
     * @return 최소 체크 간격 (일 단위)
     */
    int getMinimumCheckInterval();

    /**
     * 전략 설정이 유효한지 검증한다
     * 잘못된 설정으로 인한 오류를 사전에 방지
     * 
     * @param stocks 종목 목록
     * @return 설정이 유효하면 true
     */
    boolean validateConfiguration(List<Stock> stocks);

    /**
     * 전략의 백테스트 성과를 예측한다
     * 과거 데이터를 기반으로 해당 전략의 예상 성과 지표 제공
     * 
     * @param historicalData 과거 데이터 (구현체에 따라 다름)
     * @return 예상 성과 지표 맵
     */
    default Map<String, Double> predictPerformance(Object historicalData) {
        return Map.of(
            "expected_rebalancing_frequency", 0.0,
            "expected_transaction_cost", 0.0,
            "volatility_impact", 0.0
        );
    }

    /**
     * 전략 간 비교를 위한 점수를 계산한다
     * 높을수록 더 나은 전략임을 의미
     * 
     * @param currentConditions 현재 시장 조건
     * @return 전략 점수 (0-100)
     */
    default double calculateStrategyScore(Map<String, Object> currentConditions) {
        return 50.0; // 기본 점수
    }

    /**
     * 전략이 특정 시장 조건에서 효과적인지 판단한다
     * 
     * @param marketCondition 시장 조건 ("BULL", "BEAR", "SIDEWAYS" 등)
     * @return 해당 조건에서 효과적이면 true
     */
    default boolean isEffectiveInMarketCondition(String marketCondition) {
        return true; // 기본적으로 모든 조건에서 효과적
    }

    /**
     * 전략 사용 시 권장 사항을 반환한다
     * 
     * @return 권장 사항 목록
     */
    default List<String> getRecommendations() {
        return List.of("정기적으로 전략 성과를 점검하세요");
    }

    /**
     * 전략의 위험 수준을 반환한다
     * 
     * @return 위험 수준 ("LOW", "MEDIUM", "HIGH")
     */
    default String getRiskLevel() {
        return "MEDIUM";
    }

    /**
     * 전략 실행에 필요한 최소 자본을 반환한다
     * 
     * @return 최소 필요 자본 (원)
     */
    default double getMinimumCapitalRequirement() {
        return 1000000.0; // 기본 100만원
    }
}