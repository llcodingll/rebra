package com.rebra.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 백테스트 일별 상세 정보 응답 DTO
 * 프론트엔드와의 호환성을 위해 기존 BacktestDetail 엔티티 구조를 유지
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestDetailResponse {

    @JsonProperty("period_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodDate;

    @JsonProperty("portfolio_value")
    private BigDecimal portfolioValue;

    @JsonProperty("period_return")
    private BigDecimal periodReturn;

    @JsonProperty("is_rebalanced")
    private Boolean isRebalanced;

    @JsonProperty("cash_balance")
    private BigDecimal cashBalance;

    @JsonProperty("daily_borrowing_interest")
    private BigDecimal dailyBorrowingInterest;

    @JsonProperty("cumulative_return")
    private BigDecimal cumulativeReturn;

    @JsonProperty("buy_hold_return")
    private BigDecimal buyHoldReturn;

    @JsonProperty("total_buy_amount")
    private BigDecimal totalBuyAmount;

    @JsonProperty("total_sell_amount")
    private BigDecimal totalSellAmount;

    // 헬퍼 메서드들 (기존 BacktestDetail과 동일)
    public BigDecimal getPeriodReturnPercentage() {
        return periodReturn != null ? periodReturn.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    public boolean wasRebalanced() {
        return isRebalanced != null && isRebalanced;
    }

    public boolean isBorrowing() {
        return cashBalance != null && cashBalance.compareTo(BigDecimal.ZERO) < 0;
    }

    public BigDecimal getSafeCashBalance() {
        return cashBalance != null ? cashBalance : BigDecimal.ZERO;
    }

    public BigDecimal getBorrowingAmount() {
        return isBorrowing() ? cashBalance.abs() : BigDecimal.ZERO;
    }

    public BigDecimal getCumulativeReturnPercentage() {
        return cumulativeReturn != null ? cumulativeReturn.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    public BigDecimal getBuyHoldReturnPercentage() {
        return buyHoldReturn != null ? buyHoldReturn.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }

    public String getPeriodSummary() {
        String rebalanceInfo = wasRebalanced() ? " (리밸런싱 실행)" : "";
        String borrowingInfo = isBorrowing() ? String.format(" [차입: %,.0f원]", getBorrowingAmount()) : "";
        return String.format("%s: 포트폴리오 가치 %,.0f원, 기간 수익률 %.2f%%%s%s",
                periodDate,
                portfolioValue,
                getPeriodReturnPercentage(),
                rebalanceInfo,
                borrowingInfo);
    }
}