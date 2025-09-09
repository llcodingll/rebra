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
    private Double finalValue;
    
    /**
     * 총 수익률 (리밸런싱 적용)
     * 리밸런싱을 적용한 실제 백테스트 수익률
     */
    @JsonProperty("total_return")
    private Double totalReturn;
    
    /**
     * 초과 수익률
     * 총 수익률 - 바이앤홀드 수익률
     * 리밸런싱 효과를 나타내는 지표
     */
    @JsonProperty("excess_return")
    private Double excessReturn;
    
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
    private Double totalFee;
    
    /**
     * 총 차입비용 (원)
     * 음수 현금 상태에서 발생한 이자 비용의 합계
     */
    @JsonProperty("total_borrowing_cost")
    private Double totalBorrowingCost;
    
    
    /**
     * 최대 차입 금액 (원)
     * 백테스트 기간 중 발생한 최대 차입 금액
     */
    @JsonProperty("max_borrowing_amount")
    private Double maxBorrowingAmount;
    
    /**
     * 최소 현금 잔액 (원)
     * 백테스트 기간 중 최소 현금 잔액 (음수 포함)
     */
    @JsonProperty("min_cash_balance")
    private Double minCashBalance;
    
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
     * 순수익을 계산한다 (거래비용 및 차입비용 차감 후)
     * 
     * @return 순수익 (원)
     */
    public double getNetProfit() {
        if (finalValue == null || totalFee == null || totalBorrowingCost == null) {
            return 0.0;
        }
        
        // finalValue는 이미 모든 비용이 차감된 값이라고 가정
        // 따라서 초기 자본 대비 순증가분을 계산하려면 별도 로직 필요
        return finalValue; // 임시로 최종 가치 반환
    }

    /**
     * 총 비용 (거래비용 + 차입비용)을 반환한다
     * 
     * @return 총 비용 (원)
     */
    public double getTotalCost() {
        double tradingCost = totalFee != null ? totalFee : 0.0;
        double borrowingCost = totalBorrowingCost != null ? totalBorrowingCost : 0.0;
        return tradingCost + borrowingCost;
    }

    /**
     * 비용 대비 초과 수익률을 계산한다
     * 
     * @return 비용 효율성 지표
     */
    public double getCostEfficiency() {
        double totalCost = getTotalCost();
        if (totalCost <= 0 || excessReturn == null) {
            return 0.0;
        }
        
        return excessReturn / totalCost;
    }

    /**
     * 평균 리밸런싱 비용을 계산한다
     * 
     * @return 리밸런싱 1회당 평균 비용 (원)
     */
    public double getAverageRebalancingCost() {
        if (rebalancingCount == null || rebalancingCount <= 0 || totalFee == null) {
            return 0.0;
        }
        
        return totalFee / rebalancingCount;
    }

    /**
     * 차입 발생률을 계산한다
     * 차입 금액이 최종 가치에서 차지하는 비율
     * 
     * @return 차입 발생률 (0.1 = 10%)
     */
    public double getBorrowingRatio() {
        if (maxBorrowingAmount == null || maxBorrowingAmount <= 0 || finalValue == null || finalValue <= 0) {
            return 0.0;
        }
        
        return maxBorrowingAmount / finalValue;
    }

    /**
     * 리밸런싱 효과를 나타내는 지표들이 모두 양수인지 확인
     * 
     * @return 리밸런싱이 효과적이었으면 true
     */
    public boolean isRebalancingEffective() {
        return excessReturn != null && excessReturn > 0;
    }

    /**
     * 차입이 발생했는지 확인
     * 
     * @return 차입이 발생했으면 true
     */
    public boolean hasBorrowing() {
        return maxBorrowingAmount != null && maxBorrowingAmount > 0;
    }

    /**
     * 높은 성과를 달성했는지 확인
     * 총 수익률이 양수이고 승률이 50% 이상인 경우
     * 
     * @return 높은 성과면 true
     */
    public boolean isHighPerformance() {
        return totalReturn != null && totalReturn > 0;
    }

    /**
     * 위험 대비 수익률 (샤프 비율 기준) 평가
     * 
     * @return 위험 대비 수익률 등급 (A, B, C, D, F)
     */
    public String getRiskAdjustedReturnGrade() {
        if (sharpeRatio == null) {
            return "N/A";
        }
        
        if (sharpeRatio >= 2.0) return "A";
        if (sharpeRatio >= 1.0) return "B"; 
        if (sharpeRatio >= 0.5) return "C";
        if (sharpeRatio >= 0.0) return "D";
        return "F";
    }

    /**
     * 백테스트 성과를 종합 평가
     * 
     * @return 종합 평가 점수 (0-100점)
     */
    public int getOverallScore() {
        int score = 0;
        
        // 총 수익률 평가 (30점)
        if (totalReturn != null) {
            if (totalReturn > 0.3) score += 30;
            else if (totalReturn > 0.2) score += 25;
            else if (totalReturn > 0.1) score += 20;
            else if (totalReturn > 0.05) score += 15;
            else if (totalReturn > 0) score += 10;
        }
        
        // 초과 수익률 평가 (25점)
        if (excessReturn != null) {
            if (excessReturn > 0.1) score += 25;
            else if (excessReturn > 0.05) score += 20;
            else if (excessReturn > 0.02) score += 15;
            else if (excessReturn > 0) score += 10;
        }
        
        // 승률 평가 제거됨
        
        // 샤프 비율 평가 (15점)
        if (sharpeRatio != null) {
            if (sharpeRatio >= 2.0) score += 15;
            else if (sharpeRatio >= 1.5) score += 12;
            else if (sharpeRatio >= 1.0) score += 9;
            else if (sharpeRatio >= 0.5) score += 6;
            else if (sharpeRatio >= 0) score += 3;
        }
        
        // 최대 낙폭 평가 (10점, 역점수)
        if (maxDrawdown != null) {
            double absDrawdown = Math.abs(maxDrawdown);
            if (absDrawdown <= 0.05) score += 10;
            else if (absDrawdown <= 0.1) score += 8;
            else if (absDrawdown <= 0.15) score += 6;
            else if (absDrawdown <= 0.2) score += 4;
            else if (absDrawdown <= 0.3) score += 2;
        }
        
        return Math.min(score, 100); // 최대 100점
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
            
            if (totalFee == null || totalFee < 0) {
                return false;
            }
            
            if (totalBorrowingCost == null || totalBorrowingCost < 0) {
                return false;
            }
            
            
            // 논리적 일관성 검증
            if (excessReturn != null && Math.abs((totalReturn - buyHoldReturn) - excessReturn) > 0.001) {
                return false; // 초과수익률 = 총수익률 - 바이앤홀드수익률
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 주요 지표들을 간단히 요약한 문자열 반환
     * 
     * @return 간단한 요약
     */
    public String getSimpleSummary() {
        return String.format("총수익률: %.2f%%, 초과수익률: %.2f%%, 리밸런싱: %d회",
                (totalReturn != null ? totalReturn * 100 : 0),
                (excessReturn != null ? excessReturn * 100 : 0),
                (rebalancingCount != null ? rebalancingCount : 0));
    }

    /**
     * 상세한 성과 분석 문자열 반환
     * 
     * @return 상세 분석
     */
    public String getDetailedAnalysis() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 백테스트 성과 분석 ===\n");
        sb.append(String.format("최종 가치: %,.0f원\n", finalValue != null ? finalValue : 0));
        sb.append(String.format("총 수익률: %.2f%%\n", (totalReturn != null ? totalReturn * 100 : 0)));
        sb.append(String.format("바이앤홀드: %.2f%%\n", (buyHoldReturn != null ? buyHoldReturn * 100 : 0)));
        sb.append(String.format("초과 수익률: %.2f%%\n", (excessReturn != null ? excessReturn * 100 : 0)));
        sb.append(String.format("시간 가중 수익률: %.2f%%\n", (timeWeightedReturn != null ? timeWeightedReturn * 100 : 0)));
        sb.append(String.format("총 비용: %,.0f원 (거래비용: %,.0f원, 차입비용: %,.0f원)\n", 
                getTotalCost(), 
                (totalFee != null ? totalFee : 0),
                (totalBorrowingCost != null ? totalBorrowingCost : 0)));
        sb.append(String.format("리밸런싱: %d회 (평균비용: %,.0f원/회)\n", 
                (rebalancingCount != null ? rebalancingCount : 0), 
                getAverageRebalancingCost()));
        
        if (hasBorrowing()) {
            sb.append(String.format("최대 차입금: %,.0f원 (차입비율: %.1f%%)\n", 
                    (maxBorrowingAmount != null ? maxBorrowingAmount : 0), 
                    getBorrowingRatio() * 100));
        }
        
        if (sharpeRatio != null) {
            sb.append(String.format("샤프 비율: %.2f (%s등급)\n", sharpeRatio, getRiskAdjustedReturnGrade()));
        }
        
        if (maxDrawdown != null) {
            sb.append(String.format("최대 낙폭: %.2f%%\n", maxDrawdown * 100));
        }
        
        sb.append(String.format("종합 평가: %d점 (%s)\n", getOverallScore(), 
                isHighPerformance() ? "우수" : "보통"));
        
        return sb.toString();
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