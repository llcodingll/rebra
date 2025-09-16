package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.request.PortfolioStockRegisterRequest;
import com.rebra.dto.request.PortfolioStockDeleteRequest;
import com.rebra.dto.request.PortfolioStockBatchUpdateRequest;
import com.rebra.dto.response.PortfolioStockResponse;
import com.rebra.service.PortfolioStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/portfolios/{portfolioId}/stocks")
@Tag(name = "포트폴리오 주식 관리", description = "포트폴리오 주식 등록/삭제/설정 API")
public class PortfolioStockController {

    private final PortfolioStockService portfolioStockService;

    @Operation(summary = "포트폴리오 주식 등록", description = "포트폴리오에 새로운 주식을 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "등록 권한 없음"),
        @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 등록된 주식"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<CommonApiResponse<PortfolioStockResponse>> registerStock(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioStockRegisterRequest request,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("포트폴리오 주식 등록 요청 - 사용자ID: {}, 포트폴리오ID: {}, 주식코드: {}",
                userId, portfolioId, request.getStockCode());

        PortfolioStockResponse response = portfolioStockService.registerStock(userId, portfolioId, request);

        log.info("포트폴리오 주식 등록 성공 - 사용자ID: {}, 포트폴리오ID: {}, 주식코드: {}",
                userId, portfolioId, request.getStockCode());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.success(response));
    }

    @Operation(summary = "포트폴리오 주식 삭제", description = "포트폴리오에서 주식을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "포트폴리오 또는 주식을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping
    public ResponseEntity<CommonApiResponse<Void>> deleteStock(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioStockDeleteRequest request,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("포트폴리오 주식 삭제 요청 - 사용자ID: {}, 포트폴리오ID: {}, 주식코드: {}",
                userId, portfolioId, request.getStockCode());

        portfolioStockService.deleteStock(userId, portfolioId, request);

        log.info("포트폴리오 주식 삭제 성공 - 사용자ID: {}, 포트폴리오ID: {}, 주식코드: {}",
                userId, portfolioId, request.getStockCode());

        return ResponseEntity.ok(CommonApiResponse.success());
    }

    @Operation(summary = "포트폴리오 주식 설정 일괄 업데이트",
               description = "여러 주식의 목표 비중과 임계값을 한 번에 업데이트합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업데이트 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "업데이트 권한 없음"),
        @ApiResponse(responseCode = "404", description = "포트폴리오 또는 주식을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/batch")
    public ResponseEntity<CommonApiResponse<List<PortfolioStockResponse>>> updateStocksBatch(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioStockBatchUpdateRequest request,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("포트폴리오 주식 일괄 업데이트 요청 - 사용자ID: {}, 포트폴리오ID: {}, 업데이트 주식 수: {}",
                userId, portfolioId, request.getStocks().size());

        List<PortfolioStockResponse> responses = portfolioStockService.updateStocksBatch(userId, portfolioId, request);

        log.info("포트폴리오 주식 일괄 업데이트 성공 - 사용자ID: {}, 포트폴리오ID: {}, 업데이트된 주식 수: {}",
                userId, portfolioId, responses.size());

        return ResponseEntity.ok(CommonApiResponse.success(responses));
    }
}