package com.rebra.dto.request;

import com.rebra.entity.BacktestRecord;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BacktestCreateRequest {

    @NotBlank(message = "백테스트 이름은 필수입니다")
    @Size(max = 100, message = "백테스트 이름은 100자 이하여야 합니다")
    private String testName;

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    @NotNull(message = "리밸런싱 유형은 필수입니다")
    private BacktestRecord.RebalancingType rebalancingType;

    private BacktestRecord.RebalancingPeriod rebalancingPeriod;

    @NotEmpty(message = "종목 목록은 필수입니다")
    @Valid
    private List<BacktestStockRequest> stocks;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class BacktestStockRequest {

        @NotBlank(message = "종목 코드는 필수입니다")
        private String ticker;

        @NotBlank(message = "종목명은 필수입니다")
        private String name;

        @NotNull(message = "가중치는 필수입니다")
        private Integer weight;

        private Double thresholdPercentage;

        @NotNull(message = "초기 보유 주식 수는 필수입니다")
        private Integer shares;

    }

    // 유효성 검사 메서드
    public boolean isValid() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return false;
        }

        if (rebalancingType == BacktestRecord.RebalancingType.PERIODIC && rebalancingPeriod == null) {
            return false;
        }

        if (stocks != null) {
            int totalWeight = stocks.stream()
                    .mapToInt(stock -> stock.getWeight() != null ? stock.getWeight() : 0)
                    .sum();
            if (totalWeight <= 0) {
                return false;
            }
        }

        return true;
    }

}