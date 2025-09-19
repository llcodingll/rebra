package com.rebra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리밸런싱 필요 여부 체크 응답")
public class RebalancingCheckResponse {

    @Schema(description = "리밸런싱 필요 여부")
    private boolean needsRebalancing;

    @Schema(description = "리밸런싱 유형")
    private String rebalancingType;

    @Schema(description = "체크 기준 (THRESHOLD: 임계값, PERIODIC: 주기)")
    private String checkReason;

    @Schema(description = "다음 리밸런싱 예상 날짜")
    private LocalDate nextRebalanceDate;

    @Schema(description = "종목별 리밸런싱 상세 정보")
    private List<StockRebalancingInfo> stockInfos;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "종목별 리밸런싱 정보")
    public static class StockRebalancingInfo {
        
        @Schema(description = "종목 코드")
        private String stockCode;

        @Schema(description = "종목명")
        private String stockName;

        @Schema(description = "목표 비중")
        private Double targetWeight;

        @Schema(description = "현재 비중")
        private Double currentWeight;

        @Schema(description = "비중 차이")
        private Double weightDifference;

        @Schema(description = "임계값")
        private Double threshold;

        @Schema(description = "리밸런싱 필요 여부")
        private boolean needsRebalancing;

        @Schema(description = "현재 보유 수량")
        private Integer currentQuantity;

        @Schema(description = "현재 주가")
        private Long currentPrice;

        @Schema(description = "현재 평가액")
        private Long currentValue;
    }

    public static RebalancingCheckResponse notNeeded(String rebalancingType, LocalDate nextRebalanceDate) {
        return RebalancingCheckResponse.builder()
                .needsRebalancing(false)
                .rebalancingType(rebalancingType)
                .checkReason("임계값 미달 또는 주기 미도래")
                .nextRebalanceDate(nextRebalanceDate)
                .build();
    }

    public static RebalancingCheckResponse needed(String rebalancingType, String reason, 
                                                  List<StockRebalancingInfo> stockInfos) {
        return RebalancingCheckResponse.builder()
                .needsRebalancing(true)
                .rebalancingType(rebalancingType)
                .checkReason(reason)
                .stockInfos(stockInfos)
                .build();
    }
    
    /**
     * RebalancingCalculation.StockRebalancingDetail을 사용하여 StockRebalancingInfo 생성
     * 내부 비율을 퍼센트로 변환하여 반환
     */
    public static StockRebalancingInfo fromStockDetail(com.rebra.service.RebalancingCalculation.StockRebalancingDetail detail, 
                                                      String stockName) {
        return StockRebalancingInfo.builder()
                .stockCode(detail.getStockCode())
                .stockName(stockName)
                .targetWeight(detail.getNormalizedTargetWeightAsPercent())
                .currentWeight(detail.getCurrentWeightAsPercent())
                .weightDifference(detail.getWeightDifferenceAsPercent())
                .threshold(detail.getThresholdAsPercent())
                .needsRebalancing(detail.isNeedsRebalancing())
                .currentQuantity(detail.getCurrentQuantity())
                .currentPrice(detail.getCurrentPrice())
                .currentValue(detail.getCurrentValue())
                .build();
    }
}