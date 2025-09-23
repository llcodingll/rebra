package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.response.PerformanceMetricsChartResponse;
import com.rebra.dto.response.TradeHistoryResponse;
import com.rebra.scheduler.PerformanceMetricsScheduler;
import com.rebra.service.PerformanceMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/portfolios")
@Tag(name = "포트폴리오 성과 메트릭", description = "포트폴리오 성과 데이터 조회 API")
public class PerformanceMetricsController {

    private final PerformanceMetricsService performanceMetricsService;
//    private final PerformanceMetricsScheduler performanceMetricsScheduler;

    @Operation(
        summary = "포트폴리오 성과 차트 데이터 조회",
        description = "포트폴리오 생성일부터 오늘까지의 성과 메트릭 데이터를 차트용으로 조회합니다. " +
                     "일별 포트폴리오 총 가치와 거래 활동 내역을 포함합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 포트폴리오가 아님)"),
        @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{portfolioId}/performance-metrics")
    public ResponseEntity<CommonApiResponse<PerformanceMetricsChartResponse>> getPortfolioPerformanceChart(
            @Parameter(description = "포트폴리오 ID", required = true, example = "1")
            @PathVariable Long portfolioId,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("포트폴리오 성과 차트 데이터 조회 요청 - Portfolio ID: {}, User ID: {}", portfolioId, userId);

        /**
         * 스케줄러 메서드 강제 호출 / 테스트 시에 주석 풀고 사용하세요.
         */
//        performanceMetricsScheduler.collectDailyPerformanceMetrics();

        PerformanceMetricsChartResponse response = performanceMetricsService
                .getPortfolioPerformanceChart(portfolioId, userId);

        log.info("포트폴리오 성과 차트 데이터 조회 성공 - Portfolio ID: {}, User ID: {}, Data Points: {}",
                portfolioId, userId, response.getPerformanceData().size());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(
        summary = "특정 날짜 거래 히스토리 조회",
        description = "포트폴리오의 특정 날짜에 발생한 모든 거래 기록을 조회합니다. " +
                     "해당 날짜에 리밸런싱 주문이 있었다면 관련된 모든 TradeRecord를 반환합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (날짜 형식 오류 등)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 포트폴리오가 아님)"),
        @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{portfolioId}/trade-history")
    public ResponseEntity<CommonApiResponse<TradeHistoryResponse>> getTradeHistoryByDate(
            @Parameter(description = "포트폴리오 ID", required = true, example = "1")
            @PathVariable Long portfolioId,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식)", required = true, example = "2024-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("특정 날짜 거래 히스토리 조회 요청 - Portfolio ID: {}, User ID: {}, Date: {}",
                portfolioId, userId, date);

        TradeHistoryResponse response = performanceMetricsService
                .getTradeHistoryByDate(portfolioId, userId, date);

        log.info("특정 날짜 거래 히스토리 조회 성공 - Portfolio ID: {}, User ID: {}, Date: {}, Trade Count: {}",
                portfolioId, userId, date, response.getTrades().size());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }
}