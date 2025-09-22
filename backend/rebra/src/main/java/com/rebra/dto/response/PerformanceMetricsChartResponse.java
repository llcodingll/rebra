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
    }
}