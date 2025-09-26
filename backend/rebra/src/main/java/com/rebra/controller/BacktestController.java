package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.request.BacktestCreateRequest;
import com.rebra.dto.request.StockHistoricalSearchRequest;
import com.rebra.dto.response.BacktestListResponse;
import com.rebra.dto.response.BacktestResultResponse;
import com.rebra.dto.response.BacktestValidationResponse;
import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockHistoricalDataResponse;
import com.rebra.dto.response.StockHistoricalDataWithTradingInfo;
import com.rebra.service.BacktestService;
import com.rebra.service.StockHistoricalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/backtests")
@RequiredArgsConstructor
@Tag(name = "백테스트", description = "포트폴리오 백테스트 API")
public class BacktestController {

    private final BacktestService backtestService;
    private final StockHistoricalService stockHistoricalService;

    @PostMapping("/validate")
    @Operation(summary = "백테스트 실행 가능성 검증", description = "백테스트 요청이 실행 가능한지 사전 검증합니다.")
    public ResponseEntity<CommonApiResponse<BacktestValidationResponse>> validateBacktest(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Valid @RequestBody BacktestCreateRequest request) {

        BacktestValidationResponse validation = backtestService.validateBacktestRequest(userId, request);
        return ResponseEntity.ok(CommonApiResponse.success(validation));
    }

    @PostMapping
    @Operation(summary = "백테스트 생성", description = "새로운 백테스트를 생성하고 계산을 요청합니다.")
    public ResponseEntity<CommonApiResponse<Long>> createBacktest(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Valid @RequestBody BacktestCreateRequest request) {

        Long backtestId = backtestService.createBacktest(userId, request);
        return ResponseEntity.ok(CommonApiResponse.success(backtestId));
    }

    @GetMapping
    @Operation(summary = "백테스트 목록 조회", description = "사용자의 백테스트 목록을 조회합니다.")
    public ResponseEntity<CommonApiResponse<PageResponse<BacktestListResponse>>> getBacktestList(
            @Parameter(hidden = true) @LoginUser Long userId,
            @PageableDefault(size = 10) Pageable pageable) {

        PageResponse<BacktestListResponse> backtests = backtestService.getBacktestList(userId, pageable);
        return ResponseEntity.ok(CommonApiResponse.success(backtests));
    }

    @GetMapping("/{backtestId}")
    @Operation(summary = "백테스트 결과 조회", description = "특정 백테스트의 상세 결과를 조회합니다.")
    public ResponseEntity<CommonApiResponse<BacktestResultResponse>> getBacktestResult(
            @PathVariable Long backtestId) {

        BacktestResultResponse result = backtestService.getBacktestResult(backtestId);
        return ResponseEntity.ok(CommonApiResponse.success(result));
    }

    @DeleteMapping("/{backtestId}")
    @Operation(summary = "백테스트 삭제", description = "특정 백테스트를 삭제합니다.")
    public ResponseEntity<CommonApiResponse<Void>> deleteBacktest(
            @Parameter(hidden = true) @LoginUser Long userId,
            @PathVariable Long backtestId) {

        backtestService.deleteBacktest(userId, backtestId);
        return ResponseEntity.ok(CommonApiResponse.success());
    }

    @GetMapping("/stocks/historical")
    @Operation(summary = "백테스트용 과거 주식 데이터 조회", 
               description = "종목명과 날짜로 과거 주식 데이터를 조회합니다. 거래일 여부도 함께 반환하여 클라이언트가 데이터 없음의 원인을 구분할 수 있습니다.")
    public ResponseEntity<CommonApiResponse<StockHistoricalDataWithTradingInfo>> getStockHistoricalData(
            @Parameter(description = "검색할 종목명", example = "삼성전자", required = true)
            @RequestParam String stockName,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2023-01-01", required = true)
            @RequestParam String date) {

        // 요청 객체 생성 (LocalDate로 변환)
        StockHistoricalSearchRequest request = new StockHistoricalSearchRequest(
                stockName, 
                java.time.LocalDate.parse(date)
        );

        StockHistoricalDataWithTradingInfo result = stockHistoricalService.getStockHistoricalData(request);
        return ResponseEntity.ok(CommonApiResponse.success(result));
    }

}