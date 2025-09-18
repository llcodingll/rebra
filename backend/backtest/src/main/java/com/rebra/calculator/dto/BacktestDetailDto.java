package com.rebra.calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 백테스트 상세 기록을 나타내는 DTO 클래스
 * 메인 서버의 BACKTEST_DETAIL 테이블에 저장될 주기별 포트폴리오 상태를 담는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BacktestDetailDto {
    
    
    /**
     * 백테스트 주기 기준일자
     * 해당 기록의 기준이 되는 날짜 (거래일)
     */
    @JsonProperty("period_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodDate;
    
    /**
     * 해당 주기 포트폴리오 가치 (원)
     * 현금 + 주식 평가액의 합계
     */
    @JsonProperty("portfolio_value")
    private Double portfolioValue;
    
    /**
     * 해당 주기 수익률
     * 이전 주기 대비 수익률
     */
    @JsonProperty("period_return")
    private Double periodReturn;
    
    /**
     * 해당 주기 리밸런싱 실행 여부
     * true면 해당 날짜에 리밸런싱이 실행됨
     */
    @JsonProperty("is_rebalanced")
    private Boolean isRebalanced;
    
    /**
     * 현금 잔액 (원)
     * 해당 시점의 현금 보유액 (음수 가능)
     */
    @JsonProperty("cash_balance")
    private Double cashBalance;
    
    
    /**
     * 일일 차입 이자 (원)
     * 해당 날짜에 발생한 차입 이자
     */
    @JsonProperty("daily_borrowing_interest")
    private Double dailyBorrowingInterest;
    
    /**
     * 누적 수익률
     * 백테스트 시작일부터 해당 날짜까지의 누적 수익률
     */
    @JsonProperty("cumulative_return")
    private Double cumulativeReturn;
    
    /**
     * 바이앤홀드 수익률
     * 동일 기간 바이앤홀드 전략의 수익률 (비교용)
     */
    @JsonProperty("buy_hold_return")
    private Double buyHoldReturn;
    
    
    /**
     * 총 매수 금액 (원)
     * 해당 주기 리밸런싱에서 발생한 총 매수 금액
     */
    @JsonProperty("total_buy_amount")
    private Double totalBuyAmount;
    
    /**
     * 총 매도 금액 (원)
     * 해당 주기 리밸런싱에서 발생한 총 매도 금액
     */
    @JsonProperty("total_sell_amount")
    private Double totalSellAmount;

    /**
     * 간단한 생성자 (필수 필드만)
     * 
     * @param periodDate 기준 날짜
     * @param portfolioValue 포트폴리오 가치
     * @param periodReturn 주기 수익률
     * @param isRebalanced 리밸런싱 여부
     */
    public BacktestDetailDto(LocalDate periodDate, 
                           Double portfolioValue, Double periodReturn, Boolean isRebalanced) {
        this.periodDate = periodDate;
        this.portfolioValue = portfolioValue;
        this.periodReturn = periodReturn;
        this.isRebalanced = isRebalanced;
    }

    /**
     * 차입 상태인지 확인
     * 
     * @return 차입 상태면 true
     */
    @JsonIgnore
    public boolean isBorrowing() {
        return cashBalance != null && cashBalance < 0;
    }

    /**
     * 해당 주기에 수익이 발생했는지 확인
     * 
     * @return 수익이 발생했으면 true
     */
    public boolean isProfitable() {
        return periodReturn != null && periodReturn > 0;
    }

    /**
     * 해당 주기에 손실이 발생했는지 확인
     * 
     * @return 손실이 발생했으면 true
     */
    public boolean isLoss() {
        return periodReturn != null && periodReturn < 0;
    }

    /**
     * 보합인지 확인
     * 
     * @return 보합이면 true
     */
    public boolean isFlat() {
        return periodReturn != null && Math.abs(periodReturn) < 0.0001;
    }

    /**
     * 리밸런싱이 실행되었는지 안전하게 확인
     * 
     * @return 리밸런싱되었으면 true
     */
    @JsonIgnore
    public boolean wasRebalanced() {
        return isRebalanced != null && isRebalanced;
    }

    /**
     * 실제 거래가 발생했는지 확인
     * 리밸런싱 조건을 충족했더라도 실제로 거래가 없었다면 false 반환
     * 
     * @return 실제 거래가 발생했으면 true
     */
    @JsonProperty("has_actual_trades")
    public boolean hasActualTrades() {
        // isRebalanced가 true이고, 매수 또는 매도 금액이 0보다 크면 실제 거래 발생
        return wasRebalanced() && 
               (getSafeTotalBuyAmount() > 0 || getSafeTotalSellAmount() > 0);
    }

    /**
     * 현금 잔액을 안전하게 반환
     * 
     * @return 현금 잔액 (null이면 0.0 반환)
     */
    @JsonIgnore
    public double getSafeCashBalance() {
        return cashBalance != null ? cashBalance : 0.0;
    }

    /**
     * 차입 금액을 안전하게 반환
     * 
     * @return 차입 금액 (cashBalance가 음수일 때 절댓값, 아니면 0.0)
     */
    @JsonIgnore
    public double getSafeBorrowingAmount() {
        return (cashBalance != null && cashBalance < 0) ? Math.abs(cashBalance) : 0.0;
    }

    /**
     * 일일 차입 이자를 안전하게 반환
     * 
     * @return 일일 차입 이자 (null이면 0.0 반환)
     */
    @JsonIgnore
    public double getSafeDailyBorrowingInterest() {
        return dailyBorrowingInterest != null ? dailyBorrowingInterest : 0.0;
    }

    /**
     * 포트폴리오 가치를 안전하게 반환
     * 
     * @return 포트폴리오 가치 (null이면 0.0 반환)
     */
    @JsonIgnore
    public double getSafePortfolioValue() {
        return portfolioValue != null ? portfolioValue : 0.0;
    }

    /**
     * 주기 수익률을 안전하게 반환
     * 
     * @return 주기 수익률 (null이면 0.0 반환)
     */
    @JsonIgnore
    public double getSafePeriodReturn() {
        return periodReturn != null ? periodReturn : 0.0;
    }

    /**
     * 누적 수익률을 안전하게 반환
     * 
     * @return 누적 수익률 (null이면 0.0 반환)
     */
    @JsonIgnore
    public double getSafeCumulativeReturn() {
        return cumulativeReturn != null ? cumulativeReturn : 0.0;
    }

    
    /**
     * 총 매수 금액을 안전하게 반환
     * 
     * @return 총 매수 금액 (null이면 0.0 반환)
     */
    @JsonIgnore
    public double getSafeTotalBuyAmount() {
        return totalBuyAmount != null ? totalBuyAmount : 0.0;
    }
    
    /**
     * 총 매도 금액을 안전하게 반환
     * 
     * @return 총 매도 금액 (null이면 0.0 반환)
     */
    @JsonIgnore
    public double getSafeTotalSellAmount() {
        return totalSellAmount != null ? totalSellAmount : 0.0;
    }

    /**
     * 현재 상태에 따른 상태 아이콘을 반환
     * 
     * @return 상태 표시 문자열
     */
    @JsonIgnore
    public String getStatusIcon() {
        StringBuilder status = new StringBuilder();
        
        if (isProfitable()) {
            status.append("📈"); // 수익
        } else if (isLoss()) {
            status.append("📉"); // 손실
        } else {
            status.append("➡️"); // 보합
        }
        
        if (wasRebalanced()) {
            status.append("⚖️"); // 리밸런싱
        }
        
        if (isBorrowing()) {
            status.append("💳"); // 차입
        }
        
        return status.toString();
    }

    /**
     * 주기별 성과를 등급으로 평가
     * 
     * @return 성과 등급 (A, B, C, D, F)
     */
    @JsonIgnore
    public String getPerformanceGrade() {
        if (periodReturn == null) {
            return "N/A";
        }
        
        double returnPercent = periodReturn * 100;
        
        if (returnPercent >= 5.0) return "A";
        if (returnPercent >= 2.0) return "B";
        if (returnPercent >= 0.0) return "C";
        if (returnPercent >= -2.0) return "D";
        return "F";
    }

    /**
     * 주기별 상세 정보의 유효성을 검증
     * 
     * @return 유효한 데이터면 true
     */
    public boolean isValid() {
        try {
            // 필수 필드 검증
            
            if (periodDate == null) {
                return false;
            }
            
            if (portfolioValue == null || portfolioValue < 0) {
                return false;
            }
            
            if (periodReturn == null) {
                return false;
            }
            
            if (isRebalanced == null) {
                return false;
            }
            
            // 논리적 일관성 검증
            
            if (dailyBorrowingInterest != null && dailyBorrowingInterest < 0) {
                return false; // 차입 이자는 음수일 수 없음
            }
            
            // 차입 상태와 현금 잔액의 일관성은 isBorrowing() 메서드에서 자동 처리됨
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 간단한 일일 요약을 반환
     * 
     * @return 간단한 요약
     */
    @JsonIgnore
    public String getDailySummary() {
        return String.format("%s: %,.0f원 (%.2f%%) %s", 
                periodDate, 
                getSafePortfolioValue(), 
                getSafePeriodReturn() * 100,
                getStatusIcon());
    }

    /**
     * 상세한 일일 리포트를 반환
     * 
     * @return 상세 리포트
     */
    @JsonIgnore
    public String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== %s 일일 리포트 ===\n", periodDate));
        sb.append(String.format("포트폴리오 가치: %,.0f원\n", getSafePortfolioValue()));
        sb.append(String.format("일일 수익률: %.2f%% (%s등급)\n", 
                getSafePeriodReturn() * 100, getPerformanceGrade()));
        sb.append(String.format("누적 수익률: %.2f%%\n", getSafeCumulativeReturn() * 100));
        
        
        sb.append(String.format("현금 잔액: %,.0f원\n", getSafeCashBalance()));
        
        if (isBorrowing()) {
            sb.append(String.format("차입 금액: %,.0f원\n", getSafeBorrowingAmount()));
            sb.append(String.format("일일 이자: %,.0f원\n", getSafeDailyBorrowingInterest()));
        }
        
        sb.append(String.format("리밸런싱: %s\n", wasRebalanced() ? "실행됨" : "없음"));
        sb.append(String.format("상태: %s\n", getStatusIcon()));
        
        return sb.toString();
    }

    /**
     * CSV 형태로 데이터를 반환 (데이터 분석용)
     * 
     * @return CSV 형태 문자열
     */
    @JsonIgnore
    public String toCsv() {
        return String.format("%s,%.0f,%.6f,%.6f,%.6f,%s,%s,%.0f,%.0f,%.6f,%.0f,%.0f", 
                periodDate,
                getSafePortfolioValue(),
                getSafePeriodReturn(),
                getSafeCumulativeReturn(),
                0.0, // excessReturn 제거됨
                wasRebalanced() ? "Y" : "N",
                isBorrowing() ? "Y" : "N",
                getSafeCashBalance(),
                getSafeBorrowingAmount(),
                getSafeDailyBorrowingInterest(),
                getSafeTotalBuyAmount(),
                getSafeTotalSellAmount());
    }

    /**
     * CSV 헤더를 반환
     * 
     * @return CSV 헤더 문자열
     */
    public static String getCsvHeader() {
        return "Date,PortfolioValue,PeriodReturn,CumulativeReturn,ExcessReturn,Rebalanced,Borrowing,CashBalance,BorrowingAmount,DailyInterest,TotalBuyAmount,TotalSellAmount";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BacktestDetailDto that = (BacktestDetailDto) obj;
        return java.util.Objects.equals(periodDate, that.periodDate);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(periodDate);
    }
}