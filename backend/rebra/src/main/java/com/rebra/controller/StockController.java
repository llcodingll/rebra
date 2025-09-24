package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.common.PageResponse;
import com.rebra.dto.WatchlistDto;
import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.StockBasicInfoResponse;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockDetailResponse;
import com.rebra.dto.response.StockHistoricalDataResponse;
import com.rebra.dto.response.StockHoldingDetailResponse;
import com.rebra.dto.response.StockHoldingListResponse;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.enums.ExecutionType;
import com.rebra.dto.response.WatchlistToggleResponse;
import com.rebra.service.StockService;
import com.rebra.service.StockTradingService;
import com.rebra.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final WatchlistService watchlistService;

    @Operation(summary = "종목명으로 주식 검색", description = "FSS API를 통해 종목명에 포함된 문자열로 주식 기본정보를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<CommonApiResponse<List<StockBasicInfoResponse>>> searchStocks(
            @Parameter(description = "검색할 종목명", example = "삼성")
            @RequestParam String stockName) {

        List<StockBasicInfoResponse> responses = stockService.searchStockBasicInfoFromApi(stockName);

        return ResponseEntity.ok(CommonApiResponse.success(responses));
    }

    @Operation(summary = "관심종목 토글", description = "관심종목 추가/제거를 토글 방식으로 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 종목을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/watchlist/toggle")
    public ResponseEntity<CommonApiResponse<WatchlistToggleResponse>> toggleWatchlist(
            @Parameter(description = "종목 코드", example = "005930", required = true)
            @RequestParam String stockCode,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("관심종목 토글 요청 - UserId: {}, StockCode: {}", userId, stockCode);

        WatchlistToggleResponse response = watchlistService.toggleWatchlist(userId, stockCode);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "관심종목 전체 조회", description = "사용자의 관심종목 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/watchlist")
    public ResponseEntity<CommonApiResponse<List<WatchlistDto>>> getAllWatchlist(
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("관심종목 전체 조회 요청 - UserId: {}", userId);

        List<WatchlistDto> response = watchlistService.getAllWatchlist(userId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "관심종목 상태 확인", description = "특정 종목의 관심등록 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "확인 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 종목을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/watchlist/status")
    public ResponseEntity<CommonApiResponse<Boolean>> checkWatchlistStatus(
            @Parameter(description = "종목 코드", example = "005930", required = true)
            @RequestParam String stockCode,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("관심종목 상태 확인 요청 - UserId: {}, StockCode: {}", userId, stockCode);

        boolean isInWatchlist = watchlistService.isInWatchlist(userId, stockCode);

        return ResponseEntity.ok(CommonApiResponse.success(isInWatchlist));
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

        StockTradeResponse response = stockTradingService.buyStock(stockCode, request, userId, ExecutionType.BUY_PERSONAL);

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

        StockTradeResponse response = stockTradingService.sellStock(stockCode, request, userId, ExecutionType.SELL_PERSONAL);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "종목 차트 데이터 조회 (일봉)", description = "종목의 일봉 차트 데이터를 조회합니다. (최대 100개 데이터)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "종목을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/{stockCode}/chart/daily")
    public ResponseEntity<CommonApiResponse<StockChartResponse>> getDailyChart(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Parameter(description = "조회 시작일 (YYYYMMDD) - 최근 3개월 권장", example = "20241001")
            @RequestParam(defaultValue = "20241001") String startDate,
            @Parameter(description = "조회 종료일 (YYYYMMDD)", example = "20241231")
            @RequestParam(defaultValue = "20241231") String endDate,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("일봉 차트 데이터 조회 요청 - UserId: {}, StockCode: {}", userId, stockCode);

        StockChartResponse response = stockService.getStockChartData(stockCode, startDate, endDate, "D", userId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "종목 차트 데이터 조회 (주봉)", description = "종목의 주봉 차트 데이터를 조회합니다. (최대 100개 데이터)")
    @GetMapping("/{stockCode}/chart/weekly")
    public ResponseEntity<CommonApiResponse<StockChartResponse>> getWeeklyChart(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Parameter(description = "조회 시작일 (YYYYMMDD) - 최근 2년 권장", example = "20230101")
            @RequestParam(defaultValue = "20230101") String startDate,
            @Parameter(description = "조회 종료일 (YYYYMMDD)", example = "20241231")
            @RequestParam(defaultValue = "20241231") String endDate,
            @Parameter(hidden = true) @LoginUser Long userId) {

        StockChartResponse response = stockService.getStockChartData(stockCode, startDate, endDate, "W", userId);
        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "종목 차트 데이터 조회 (월봉)", description = "종목의 월봉 차트 데이터를 조회합니다. (최대 100개 데이터)")
    @GetMapping("/{stockCode}/chart/monthly")
    public ResponseEntity<CommonApiResponse<StockChartResponse>> getMonthlyChart(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Parameter(description = "조회 시작일 (YYYYMMDD) - 최근 8년 권장", example = "20170101")
            @RequestParam(defaultValue = "20170101") String startDate,
            @Parameter(description = "조회 종료일 (YYYYMMDD)", example = "20241231")
            @RequestParam(defaultValue = "20241231") String endDate,
            @Parameter(hidden = true) @LoginUser Long userId) {

        StockChartResponse response = stockService.getStockChartData(stockCode, startDate, endDate, "M", userId);
        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "종목 차트 데이터 조회 (년봉)", description = "종목의 년봉 차트 데이터를 조회합니다. (최대 100개 데이터)")
    @GetMapping("/{stockCode}/chart/yearly")
    public ResponseEntity<CommonApiResponse<StockChartResponse>> getYearlyChart(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Parameter(description = "조회 시작일 (YYYYMMDD) - 최근 100년 권장", example = "19250101")
            @RequestParam(defaultValue = "19250101") String startDate,
            @Parameter(description = "조회 종료일 (YYYYMMDD)", example = "20241231")
            @RequestParam(defaultValue = "20241231") String endDate,
            @Parameter(hidden = true) @LoginUser Long userId) {

        StockChartResponse response = stockService.getStockChartData(stockCode, startDate, endDate, "Y", userId);
        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "보유 종목 전체 조회 (페이지네이션)", description = "계좌의 보유 종목을 페이지 단위로 조회합니다. 기본 페이지 크기는 5개입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "KIS API 연동 실패")
    })
    @GetMapping("/holdings")
    public ResponseEntity<CommonApiResponse<PageResponse<StockHoldingListResponse>>> getHoldingStocks(
            @Parameter(description = "계좌 ID", required = true, example = "1")
            @RequestParam Long accountId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "5")
            @RequestParam(defaultValue = "5") int size,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("보유 종목 전체 조회 요청 - UserId: {}, AccountId: {}, Page: {}, Size: {}",
                userId, accountId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<StockHoldingListResponse> response = stockService.getHoldingStocks(accountId, pageable);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "특정 종목 보유 정보 조회", description = "특정 종목의 보유 정보를 조회합니다. 보유하지 않는 경우 null을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공 (보유/미보유 모두 성공)"),
            @ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "KIS API 연동 실패")
    })
    @GetMapping("/{stockCode}/holding")
    public ResponseEntity<CommonApiResponse<StockHoldingDetailResponse>> getStockHolding(
            @Parameter(description = "조회할 종목 코드", example = "005930")
            @PathVariable String stockCode,
            @Parameter(description = "계좌 ID", required = true, example = "1")
            @RequestParam Long accountId,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("특정 종목 보유 정보 조회 요청 - UserId: {}, StockCode: {}, AccountId: {}",
                userId, stockCode, accountId);

        StockHoldingDetailResponse response = stockService.getStockHolding(stockCode, accountId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }
}