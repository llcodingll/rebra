package com.rebra.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Long portfolioValue;

    @JsonProperty("period_return")
    private Double periodReturn;

    @JsonProperty("is_rebalanced")
    private Boolean isRebalanced;

    @JsonProperty("cash_balance")
    private Long cashBalance;

    @JsonProperty("daily_borrowing_interest")
    private Long dailyBorrowingInterest;

    @JsonProperty("cumulative_return")
    private Double cumulativeReturn;

    @JsonProperty("buy_hold_return")
    private Double buyHoldReturn;

    @JsonProperty("total_buy_amount")
    private Long totalBuyAmount;

    @JsonProperty("total_sell_amount")
    private Long totalSellAmount;

    // 추가된 percentage 필드들 (백테스트 계산 서버에서 계산된 퍼센트 값)
    @JsonProperty("buy_hold_return_percentage")
    private Double buyHoldReturnPercentage;

    @JsonProperty("cumulative_return_percentage")
    private Double cumulativeReturnPercentage;

    @JsonProperty("period_return_percentage")
    private Double periodReturnPercentage;

    // 헬퍼 메서드들 (기존 BacktestDetail과 동일)
    public Double getPeriodReturnPercentage() {
        return periodReturn != null ? periodReturn * 100.0 : 0.0;
    }

    public boolean wasRebalanced() {
        return isRebalanced != null && isRebalanced;
    }

    public boolean isBorrowing() {
        return cashBalance != null && cashBalance < 0;
    }

    public Long getSafeCashBalance() {
        return cashBalance != null ? cashBalance : 0L;
    }

    public Long getBorrowingAmount() {
        return isBorrowing() ? Math.abs(cashBalance) : 0L;
    }

    public Double getCumulativeReturnPercentage() {
        return cumulativeReturn != null ? cumulativeReturn * 100.0 : 0.0;
    }

    public Double getBuyHoldReturnPercentage() {
        return buyHoldReturn != null ? buyHoldReturn * 100.0 : 0.0;
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