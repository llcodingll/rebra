package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockDetailResponse;
import com.rebra.dto.response.StockSearchResponse;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.entity.User;
import com.rebra.service.StockService;
import com.rebra.service.StockTradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
@Tag(name = "Stock API", description = "주식 검색 관리 API")
public class StockController {

    private final StockService stockService;
    private final StockTradingService stockTradingService;

    @Operation(summary = "종목명으로 주식 검색", description = "종목명에 포함된 문자열로 주식을 검색합니다. (활성 상태인 주식만)")
    @GetMapping("/search")
    public ResponseEntity<CommonApiResponse<PageResponse<StockSearchResponse>>> searchStocks(
            @Parameter(description = "검색할 종목명", example = "삼성")
            @RequestParam String stockName,
            @PageableDefault(size = 20, sort = "stockName") Pageable pageable) {

        PageResponse<StockSearchResponse> responses = stockService.searchStocks(stockName, pageable);

        return ResponseEntity.ok(CommonApiResponse.success(responses));
    }

    @Operation(summary = "종목 코드로 주식 조회", description = "정확한 종목 코드로 주식 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "주식을 찾을 수 없음")
    })
    @GetMapping("/code")
    public ResponseEntity<CommonApiResponse<StockSearchResponse>> getStockByCode(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @RequestParam String stockCode) {

        StockSearchResponse stock = stockService.findByStockCode(stockCode);

        return ResponseEntity.ok(CommonApiResponse.success(stock));
    }

    @Operation(summary = "종목명으로 주식 조회", description = "정확한 종목명으로 주식 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "주식을 찾을 수 없음")
    })
    @GetMapping("/name")
    public ResponseEntity<CommonApiResponse<StockSearchResponse>> getStockByName(
            @Parameter(description = "조회할 종목명", example = "삼성전자")
            @RequestParam String stockName) {

        StockSearchResponse stock = stockService.findByStockName(stockName);

        return ResponseEntity.ok(CommonApiResponse.success(stock));
    }

    @Operation(summary = "종목 상세 정보 조회", description = "기본 종목 정보와 WebSocket 구독 정보를 조회합니다. 실시간 데이터는 WebSocket을 통해 제공됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "종목을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/{stockCode}")
    public ResponseEntity<CommonApiResponse<StockDetailResponse>> getStockDetail(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Parameter(hidden = true) @LoginUser Long userId) {

        StockDetailResponse stockDetail = stockService.getStockDetailWithWebSocketInfo(stockCode, userId);

        return ResponseEntity.ok(CommonApiResponse.success(stockDetail));
    }

    @Operation(summary = "주식 매수 주문", description = "지정된 종목을 매수합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 주문 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음")
    })
    @PostMapping("/{stockCode}/buy")
    public ResponseEntity<CommonApiResponse<StockTradeResponse>> buyStock(
            @Parameter(description = "매수할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Valid @RequestBody StockTradeRequest request,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("주식 매수 요청 - UserId: {}, StockCode: {}, Quantity: {}, Price: {}",
                userId, stockCode, request.getQuantity(), request.getPrice());

        StockTradeResponse response = stockTradingService.buyStock(stockCode, request, userId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "주식 매도 주문", description = "지정된 종목을 매도합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 주문 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음")
    })
    @PostMapping("/{stockCode}/sell")
    public ResponseEntity<CommonApiResponse<StockTradeResponse>> sellStock(
            @Parameter(description = "매도할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Valid @RequestBody StockTradeRequest request,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("주식 매도 요청 - UserId: {}, StockCode: {}, Quantity: {}, Price: {}",
                userId, stockCode, request.getQuantity(), request.getPrice());

        StockTradeResponse response = stockTradingService.sellStock(stockCode, request, userId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "종목 차트 데이터 조회 (일봉)", description = "종목의 일봉 차트 데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "종목을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/{stockCode}/chart/daily")
    public ResponseEntity<CommonApiResponse<StockChartResponse>> getDailyChart(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Parameter(description = "조회 시작일 (YYYYMMDD)", example = "20240101")
            @RequestParam(defaultValue = "20240101") String startDate,
            @Parameter(description = "조회 종료일 (YYYYMMDD)", example = "20241231")
            @RequestParam(defaultValue = "20241231") String endDate,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("일봉 차트 데이터 조회 요청 - UserId: {}, StockCode: {}", userId, stockCode);

        StockChartResponse response = stockService.getStockChartData(stockCode, startDate, endDate, "D", userId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "종목 차트 데이터 조회 (주봉)", description = "종목의 주봉 차트 데이터를 조회합니다.")
    @GetMapping("/{stockCode}/chart/weekly")
    public ResponseEntity<CommonApiResponse<StockChartResponse>> getWeeklyChart(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Parameter(description = "조회 시작일 (YYYYMMDD)", example = "20240101")
            @RequestParam(defaultValue = "20240101") String startDate,
            @Parameter(description = "조회 종료일 (YYYYMMDD)", example = "20241231")
            @RequestParam(defaultValue = "20241231") String endDate,
            @Parameter(hidden = true) @LoginUser Long userId) {

        StockChartResponse response = stockService.getStockChartData(stockCode, startDate, endDate, "W", userId);
        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "종목 차트 데이터 조회 (월봉)", description = "종목의 월봉 차트 데이터를 조회합니다.")
    @GetMapping("/{stockCode}/chart/monthly")
    public ResponseEntity<CommonApiResponse<StockChartResponse>> getMonthlyChart(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Parameter(description = "조회 시작일 (YYYYMMDD)", example = "20240101")
            @RequestParam(defaultValue = "20240101") String startDate,
            @Parameter(description = "조회 종료일 (YYYYMMDD)", example = "20241231")
            @RequestParam(defaultValue = "20241231") String endDate,
            @Parameter(hidden = true) @LoginUser Long userId) {

        StockChartResponse response = stockService.getStockChartData(stockCode, startDate, endDate, "M", userId);
        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "종목 차트 데이터 조회 (년봉)", description = "종목의 년봉 차트 데이터를 조회합니다.")
    @GetMapping("/{stockCode}/chart/yearly")
    public ResponseEntity<CommonApiResponse<StockChartResponse>> getYearlyChart(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Parameter(description = "조회 시작일 (YYYYMMDD)", example = "20240101")
            @RequestParam(defaultValue = "20240101") String startDate,
            @Parameter(description = "조회 종료일 (YYYYMMDD)", example = "20241231")
            @RequestParam(defaultValue = "20241231") String endDate,
            @Parameter(hidden = true) @LoginUser Long userId) {

        StockChartResponse response = stockService.getStockChartData(stockCode, startDate, endDate, "Y", userId);
        return ResponseEntity.ok(CommonApiResponse.success(response));
    }
}