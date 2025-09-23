package com.rebra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "포트폴리오 성과 메트릭 차트 응답")
public class PerformanceMetricsChartResponse {

    @Schema(description = "포트폴리오 ID", example = "1")
    private Long portfolioId;

    @Schema(description = "포트폴리오 이름", example = "내 포트폴리오")
    private String portfolioName;

    @Schema(description = "포트폴리오 생성일", example = "2024-01-01")
    private LocalDate portfolioCreatedDate;

    @Schema(description = "차트 데이터 조회 시작일", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "차트 데이터 조회 종료일", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "성과 데이터 포인트 목록")
    private List<PerformanceDataPoint> performanceData;

    @Schema(description = "통계 정보")
    private PerformanceStatistics statistics;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "성과 데이터 포인트")
    public static class PerformanceDataPoint {

        @Schema(description = "측정 날짜", example = "2024-01-01")
        private LocalDate metricDate;

        @Schema(description = "포트폴리오 총 가치", example = "1000000.0")
        private double totalValue;

        @Schema(description = "리밸런싱 실행 여부", example = "true")
        private boolean isRebalanced;

        @Schema(description = "매도 실행 여부", example = "false")
        private boolean isSold;

        @Schema(description = "매수 실행 여부", example = "false")
        private boolean isBought;

        @Schema(description = "포트폴리오 구성 변경 여부", example = "false")
        private boolean isCompositionChanged;

        /**
         * PerformanceMetrics 엔티티로부터 PerformanceDataPoint 생성
         */
        public static PerformanceDataPoint from(com.rebra.entity.PerformanceMetrics metrics) {
            return PerformanceDataPoint.builder()
                    .metricDate(metrics.getMetricDate())
                    .totalValue(metrics.getTotalValue())
                    .isRebalanced(metrics.isRebalanced())
                    .isSold(metrics.isSold())
                    .isBought(metrics.isBought())
                    .isCompositionChanged(metrics.isCompositionChanged())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "성과 통계 정보")
    public static class PerformanceStatistics {

        @Schema(description = "총 데이터 포인트 수", example = "30")
        private int totalDataPoints;

        @Schema(description = "리밸런싱 실행 횟수", example = "5")
        private int rebalancingCount;

        @Schema(description = "매수 실행 횟수", example = "3")
        private int buyCount;

        @Schema(description = "매도 실행 횟수", example = "2")
        private int sellCount;

        @Schema(description = "초기 포트폴리오 가치", example = "1000000.0")
        private Double initialValue;

        @Schema(description = "최종 포트폴리오 가치", example = "1200000.0")
        private Double finalValue;

        @Schema(description = "총 수익률 (%)", example = "20.0")
        private Double totalReturnRate;

        @Schema(description = "최고 포트폴리오 가치", example = "1250000.0")
        private Double maxValue;

        @Schema(description = "최저 포트폴리오 가치", example = "950000.0")
        private Double minValue;

        /**
         * PerformanceMetrics 리스트로부터 PerformanceStatistics 생성
         */
        public static PerformanceStatistics from(java.util.List<com.rebra.entity.PerformanceMetrics> metricsData) {
            if (metricsData.isEmpty()) {
                return PerformanceStatistics.builder()
                        .totalDataPoints(0)
                        .rebalancingCount(0)
                        .buyCount(0)
                        .sellCount(0)
                        .build();
            }

            int rebalancingCount = 0;
            int buyCount = 0;
            int sellCount = 0;

            double maxValue = Double.MIN_VALUE;
            double minValue = Double.MAX_VALUE;

            for (com.rebra.entity.PerformanceMetrics metrics : metricsData) {
                if (metrics.isRebalanced()) rebalancingCount++;
                if (metrics.isBought()) buyCount++;
                if (metrics.isSold()) sellCount++;

                double value = metrics.getTotalValue();
                if (value > maxValue) maxValue = value;
                if (value < minValue) minValue = value;
            }

            // 초기값과 최종값
            Double initialValue = metricsData.get(0).getTotalValue();
            Double finalValue = metricsData.get(metricsData.size() - 1).getTotalValue();

            // 총 수익률 계산
            Double totalReturnRate = null;
            if (initialValue != null && initialValue > 0 && finalValue != null) {
                totalReturnRate = ((finalValue - initialValue) / initialValue) * 100.0;
            }

            return PerformanceStatistics.builder()
                    .totalDataPoints(metricsData.size())
                    .rebalancingCount(rebalancingCount)
                    .buyCount(buyCount)
                    .sellCount(sellCount)
                    .initialValue(initialValue)
                    .finalValue(finalValue)
                    .totalReturnRate(totalReturnRate)
                    .maxValue(maxValue != Double.MIN_VALUE ? maxValue : null)
                    .minValue(minValue != Double.MAX_VALUE ? minValue : null)
                    .build();
        }
    }
}