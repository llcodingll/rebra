package com.rebra.calculator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 백테스트 요약 결과를 나타내는 DTO 클래스
 * 메인 서버의 BACKTEST_RECORD 테이블 업데이트에 사용될 주요 지표들을 담는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BacktestSummaryDto {
    
    /**
     * 바이앤홀드 수익률
     * 리밸런싱 없이 초기 비중대로 보유했을 때의 수익률
     */
    @JsonProperty("buy_hold_return")
    private Double buyHoldReturn;
    
    /**
     * 최종 포트폴리오 가치 (원)
     * 백테스트 종료 시점의 총 포트폴리오 가치
     */
    @JsonProperty("final_value")
    private Long finalValue;
    
    /**
     * 총 수익률 (리밸런싱 적용)
     * 리밸런싱을 적용한 실제 백테스트 수익률
     */
    @JsonProperty("total_return")
    private Double totalReturn;
    
    
    /**
     * 주기별 평균 성장률
     * 각 주기(월/분기)별 평균 수익률
     */
    @JsonProperty("period_growth_rate")
    private Double periodGrowthRate;
    
    /**
     * 리밸런싱 실행 횟수
     * 백테스트 기간 중 총 리밸런싱 실행 횟수
     */
    @JsonProperty("rebalancing_count")
    private Integer rebalancingCount;
    
    /**
     * 총 거래비용 (원)
     * 수수료 + 증권거래세의 합계
     */
    @JsonProperty("total_fee")
    private Long totalFee;
    
    /**
     * 총 차입비용 (원)
     * 음수 현금 상태에서 발생한 이자 비용의 합계
     */
    @JsonProperty("total_borrowing_cost")
    private Long totalBorrowingCost;
    
    
    /**
     * 최대 차입 금액 (원)
     * 백테스트 기간 중 발생한 최대 차입 금액
     */
    @JsonProperty("max_borrowing_amount")
    private Long maxBorrowingAmount;
    
    /**
     * 최소 현금 잔액 (원)
     * 백테스트 기간 중 최소 현금 잔액 (음수 포함)
     */
    @JsonProperty("min_cash_balance")
    private Long minCashBalance;
    
    /**
     * 최대 낙폭 (Maximum Drawdown)
     * 고점 대비 최대 하락률
     */
    @JsonProperty("max_drawdown")
    private Double maxDrawdown;
    
    /**
     * 변동성 (연환산 표준편차)
     * 일일 수익률의 연환산 표준편차
     */
    @JsonProperty("volatility")
    private Double volatility;
    
    /**
     * 샤프 비율
     * (수익률 - 무위험 수익률) / 변동성
     */
    @JsonProperty("sharpe_ratio")
    private Double sharpeRatio;

    /**
     * 시간 가중 수익률 (Time-Weighted Return)
     * 리밸런싱 시점의 현금 흐름을 제거한 순수 투자 성과
     */
    @JsonProperty("time_weighted_return")
    private Double timeWeightedReturn;


    /**
     * 초과 수익률을 계산한다
     * 총 수익률 - 바이앤홀드 수익률
     * 리밸런싱 효과를 나타내는 지표
     *
     * @return 초과 수익률
     */
    @JsonProperty("excess_return")
    public double getExcessReturn() {
        if (totalReturn == null || buyHoldReturn == null) {
            return 0.0;
        }
        return totalReturn - buyHoldReturn;
    }

    /**
     * 총 비용 (거래비용 + 차입비용)을 반환한다
     * 
     * @return 총 비용 (원)
     */
    public long getTotalCost() {
        long tradingCost = totalFee != null ? totalFee : 0L;
        long borrowingCost = totalBorrowingCost != null ? totalBorrowingCost : 0L;
        return tradingCost + borrowingCost;
    }
    /**
     * 요약 정보의 유효성을 검증
     * 
     * @return 유효한 데이터면 true
     */
    public boolean isValid() {
        try {
            // 필수 필드 검증
            if (finalValue == null || finalValue < 0) {
                return false;
            }
            
            if (totalReturn == null) {
                return false;
            }
            
            if (buyHoldReturn == null) {
                return false;
            }
            
            if (rebalancingCount == null || rebalancingCount < 0) {
                return false;
            }
            
            if (totalFee == null || totalFee < 0L) {
                return false;
            }
            
            if (totalBorrowingCost == null || totalBorrowingCost < 0L) {
                return false;
            }
            
            
            // 논리적 일관성 검증
            if (Math.abs((totalReturn - buyHoldReturn) - getExcessReturn()) > 0.001) {
                return false; // 초과수익률 = 총수익률 - 바이앤홀드수익률
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BacktestSummaryDto that = (BacktestSummaryDto) obj;
        return java.util.Objects.equals(totalReturn, that.totalReturn) &&
               java.util.Objects.equals(finalValue, that.finalValue);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(totalReturn, finalValue);
    }
}