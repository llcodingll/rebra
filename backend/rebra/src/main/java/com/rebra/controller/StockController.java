package com.rebra.controller;

import com.rebra.common.CommonApiResponse;
import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockSearchResponse;
import com.rebra.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}