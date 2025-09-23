package com.rebra.controller;

import com.rebra.dto.response.MarketIndexResponse;
import com.rebra.service.MarketIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/market-index")
@RequiredArgsConstructor
@Tag(name = "Market Index", description = "시장 지수 관련 API")
public class MarketIndexController {

    private final MarketIndexService marketIndexService;

    @GetMapping("/latest")
    @Operation(
            summary = "최신 시장 지수 조회",
            description = "코스피, 코스피 200, KRX 300, 코스닥의 최신 지수 정보를 조회합니다. " +
                         "당일 데이터가 없는 경우 가장 최근 거래일의 데이터를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "시장 지수 조회 성공"),
            @ApiResponse(responseCode = "500", description = "시장 지수 조회 실패")
    })
    public ResponseEntity<MarketIndexResponse> getLatestMarketIndices() {
        log.info("시장 지수 조회 요청");
        
        MarketIndexResponse response = marketIndexService.getLatestMarketIndices();
        
        log.info("시장 지수 조회 응답 - 데이터일: {}, 지수 개수: {}", 
                response.getDataDate(), response.getIndices().size());
        
        return ResponseEntity.ok(response);
    }
}