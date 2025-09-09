package com.rebra.calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 리밸런싱 거래 내역을 나타내는 DTO 클래스
 * 백테스트 중 발생한 개별 거래 정보를 메인 서버로 전달하기 위한 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RebalancingTradeDto {
    
    /**
     * 백테스트 기록 ID
     * 해당 거래가 속한 백테스트의 식별자
     */
    @JsonProperty("backtest_record_id")
    private Long backtestRecordId;
    
    /**
     * 거래 날짜
     */
    @JsonProperty("trade_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tradeDate;
    
    /**
     * 종목 코드
     */
    @JsonProperty("stock_code")
    private String stockCode;
    
    
    /**
     * 거래 유형
     * "BUY" 또는 "SELL"
     */
    @JsonProperty("trade_type")
    private String tradeType;
    
    /**
     * 리밸런싱 전 보유 수량
     */
    @JsonProperty("before_shares")
    private Integer beforeShares;
    
    /**
     * 리밸런싱 후 보유 수량
     */
    @JsonProperty("after_shares")
    private Integer afterShares;
    
    /**
     * 실제 거래 수량
     * 매수면 양수, 매도면 양수 (절댓값)
     */
    @JsonProperty("trade_quantity")
    private Integer tradeQuantity;
    
    /**
     * 거래 가격 (원)
     */
    @JsonProperty("trade_price")
    private Double tradePrice;
    
    /**
     * 거래 금액 (원)
     * tradeQuantity * tradePrice
     */
    @JsonProperty("trade_amount")
    private Double tradeAmount;
    
    /**
     * 거래 수수료 (원)
     */
    @JsonProperty("trade_fee")
    private Double tradeFee;
    
    /**
     * 증권거래세 (원)
     * 매도 시에만 발생
     */
    @JsonProperty("tax")
    private Double tax;
    
    /**
     * 총 거래 비용 (원)
     * tradeFee + tax
     */
    @JsonProperty("total_cost")
    private Double totalCost;
    
    /**
     * 실제 거래 금액 (원)
     * 매수: tradeAmount + totalCost (현금 차감액)
     * 매도: tradeAmount - totalCost (현금 증가액)
     */
    @JsonProperty("net_amount")
    private Double netAmount;
    
    /**
     * 리밸런싱 전 목표 비중
     */
    @JsonProperty("target_weight_before")
    private Double targetWeightBefore;
    
    /**
     * 리밸런싱 전 실제 비중
     */
    @JsonProperty("actual_weight_before")
    private Double actualWeightBefore;
    
    /**
     * 리밸런싱 후 실제 비중
     */
    @JsonProperty("actual_weight_after")
    private Double actualWeightAfter;
    
    /**
     * 리밸런싱 사유
     * "THRESHOLD_EXCEEDED", "PERIODIC_REBALANCING" 등
     */
    @JsonProperty("rebalancing_reason")
    private String rebalancingReason;

    /**
     * 거래가 매수인지 확인
     * 
     * @return 매수 거래면 true
     */
    public boolean isBuy() {
        return "BUY".equalsIgnoreCase(tradeType);
    }

    /**
     * 거래가 매도인지 확인
     * 
     * @return 매도 거래면 true
     */
    public boolean isSell() {
        return "SELL".equalsIgnoreCase(tradeType);
    }

    /**
     * 거래 수량 변화량을 반환
     * 매수: +quantity, 매도: -quantity
     * 
     * @return 수량 변화량
     */
    public int getSharesChange() {
        if (beforeShares == null || afterShares == null) {
            return 0;
        }
        return afterShares - beforeShares;
    }

    /**
     * 현금 변화량을 반환
     * 매수: -netAmount (현금 차감), 매도: +netAmount (현금 증가)
     * 
     * @return 현금 변화량
     */
    public double getCashChange() {
        if (netAmount == null) {
            return 0.0;
        }
        
        return isBuy() ? -netAmount : netAmount;
    }

    /**
     * 비중 변화량을 계산
     * 
     * @return 비중 변화량 (리밸런싱 후 - 리밸런싱 전)
     */
    public double getWeightChange() {
        if (actualWeightBefore == null || actualWeightAfter == null) {
            return 0.0;
        }
        return actualWeightAfter - actualWeightBefore;
    }

    /**
     * 목표 비중과의 편차를 계산 (리밸런싱 전)
     * 
     * @return 리밸런싱 전 편차
     */
    public double getWeightDeviationBefore() {
        if (targetWeightBefore == null || actualWeightBefore == null) {
            return 0.0;
        }
        return Math.abs(actualWeightBefore - targetWeightBefore);
    }

    /**
     * 목표 비중과의 편차를 계산 (리밸런싱 후)
     * 
     * @return 리밸런싱 후 편차
     */
    public double getWeightDeviationAfter() {
        if (targetWeightBefore == null || actualWeightAfter == null) {
            return 0.0;
        }
        return Math.abs(actualWeightAfter - targetWeightBefore);
    }

    /**
     * 리밸런싱 효과를 측정
     * 편차가 줄어들었으면 양수, 늘어났으면 음수
     * 
     * @return 리밸런싱 효과
     */
    public double getRebalancingEffect() {
        return getWeightDeviationBefore() - getWeightDeviationAfter();
    }

    /**
     * 거래 비용율을 계산
     * 
     * @return 거래 비용율 (총비용 / 거래금액)
     */
    public double getCostRatio() {
        if (tradeAmount == null || tradeAmount <= 0 || totalCost == null) {
            return 0.0;
        }
        return totalCost / tradeAmount;
    }

    /**
     * 안전하게 거래 수량을 반환
     * 
     * @return 거래 수량 (null이면 0)
     */
    public int getSafeTradeQuantity() {
        return tradeQuantity != null ? tradeQuantity : 0;
    }

    /**
     * 안전하게 거래 금액을 반환
     * 
     * @return 거래 금액 (null이면 0.0)
     */
    public double getSafeTradeAmount() {
        return tradeAmount != null ? tradeAmount : 0.0;
    }

    /**
     * 안전하게 총 비용을 반환
     * 
     * @return 총 비용 (null이면 0.0)
     */
    public double getSafeTotalCost() {
        return totalCost != null ? totalCost : 0.0;
    }

    /**
     * 안전하게 실거래액을 반환
     * 
     * @return 실거래액 (null이면 0.0)
     */
    public double getSafeNetAmount() {
        return netAmount != null ? netAmount : 0.0;
    }

    /**
     * 리밸런싱 사유를 한국어로 반환
     * 
     * @return 한국어 리밸런싱 사유
     */
    public String getRebalancingReasonKorean() {
        if (rebalancingReason == null) {
            return "알 수 없음";
        }
        
        switch (rebalancingReason.toUpperCase()) {
            case "THRESHOLD_EXCEEDED":
                return "임계값 초과";
            case "PERIODIC_REBALANCING":
                return "주기적 리밸런싱";
            case "INITIAL_INVESTMENT":
                return "초기 투자";
            default:
                return rebalancingReason;
        }
    }

    /**
     * 거래 내역의 유효성을 검증
     * 
     * @return 유효한 거래면 true
     */
    public boolean isValid() {
        try {
            // 필수 필드 검증
            if (backtestRecordId == null || backtestRecordId <= 0) {
                return false;
            }
            
            if (tradeDate == null) {
                return false;
            }
            
            if (stockCode == null || stockCode.trim().isEmpty()) {
                return false;
            }
            
            if (tradeType == null || (!isBuy() && !isSell())) {
                return false;
            }
            
            if (tradeQuantity == null || tradeQuantity <= 0) {
                return false;
            }
            
            if (tradePrice == null || tradePrice <= 0) {
                return false;
            }
            
            if (tradeAmount == null || tradeAmount <= 0) {
                return false;
            }
            
            // 논리적 일관성 검증
            if (beforeShares != null && afterShares != null) {
                int expectedChange = afterShares - beforeShares;
                int actualChange = isBuy() ? tradeQuantity : -tradeQuantity;
                
                if (expectedChange != actualChange) {
                    return false; // 수량 변화와 거래 유형이 맞지 않음
                }
            }
            
            // 매수 시 세금이 없어야 함
            if (isBuy() && tax != null && tax > 0) {
                return false;
            }
            
            // 비용 계산 검증
            if (tradeFee != null && tax != null && totalCost != null) {
                double expectedTotalCost = tradeFee + tax;
                if (Math.abs(totalCost - expectedTotalCost) > 0.01) {
                    return false; // 비용 계산 오류
                }
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 간단한 거래 요약을 반환
     * 
     * @return 간단한 거래 요약
     */
    public String getTradeSummary() {
        return String.format("%s %s %s %d주 @%,.0f원 (총 %,.0f원)", 
                tradeDate,
                getRebalancingReasonKorean(),
                tradeType, 
                getSafeTradeQuantity(), 
                tradePrice != null ? tradePrice : 0,
                getSafeNetAmount());
    }

    /**
     * 상세한 거래 리포트를 반환
     * 
     * @return 상세 거래 리포트
     */
    public String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== %s 거래 내역 ===\n", tradeDate));
        sb.append(String.format("종목: %s\n", stockCode));
        sb.append(String.format("거래: %s %d주 @%,.0f원\n", tradeType, getSafeTradeQuantity(), tradePrice != null ? tradePrice : 0));
        sb.append(String.format("거래금액: %,.0f원\n", getSafeTradeAmount()));
        sb.append(String.format("거래비용: %,.0f원 (수수료: %,.0f원, 세금: %,.0f원)\n", 
                getSafeTotalCost(), 
                tradeFee != null ? tradeFee : 0,
                tax != null ? tax : 0));
        sb.append(String.format("실거래액: %,.0f원\n", getSafeNetAmount()));
        sb.append(String.format("보유수량: %d주 → %d주 (%+d주)\n", 
                beforeShares != null ? beforeShares : 0, 
                afterShares != null ? afterShares : 0, 
                getSharesChange()));
        sb.append(String.format("비중 변화: %.2f%% → %.2f%% (목표: %.2f%%)\n", 
                (actualWeightBefore != null ? actualWeightBefore : 0) * 100,
                (actualWeightAfter != null ? actualWeightAfter : 0) * 100,
                (targetWeightBefore != null ? targetWeightBefore : 0) * 100));
        sb.append(String.format("리밸런싱 효과: %.2f%%p\n", getRebalancingEffect() * 100));
        sb.append(String.format("사유: %s\n", getRebalancingReasonKorean()));
        
        return sb.toString();
    }

    /**
     * CSV 형태로 데이터를 반환
     * 
     * @return CSV 형태 문자열
     */
    public String toCsv() {
        return String.format("%s,%s,%s,%s,%d,%.0f,%.0f,%.0f,%.0f,%.0f,%.6f,%.6f,%.6f,%s", 
                tradeDate,
                stockCode,
                tradeType,
                getSafeTradeQuantity(),
                tradePrice != null ? tradePrice : 0,
                getSafeTradeAmount(),
                getSafeTotalCost(),
                getSafeNetAmount(),
                getCashChange(),
                actualWeightBefore != null ? actualWeightBefore : 0,
                actualWeightAfter != null ? actualWeightAfter : 0,
                targetWeightBefore != null ? targetWeightBefore : 0,
                getRebalancingReasonKorean());
    }

    /**
     * CSV 헤더를 반환
     * 
     * @return CSV 헤더 문자열
     */
    public static String getCsvHeader() {
        return "Date,StockCode,StockName,TradeType,Quantity,Price,TradeAmount,TotalCost,NetAmount,CashChange,WeightBefore,WeightAfter,TargetWeight,Reason";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RebalancingTradeDto that = (RebalancingTradeDto) obj;
        return java.util.Objects.equals(backtestRecordId, that.backtestRecordId) &&
               java.util.Objects.equals(tradeDate, that.tradeDate) &&
               java.util.Objects.equals(stockCode, that.stockCode) &&
               java.util.Objects.equals(tradeType, that.tradeType);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(backtestRecordId, tradeDate, stockCode, tradeType);
    }
}