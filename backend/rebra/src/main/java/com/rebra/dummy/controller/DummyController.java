package com.rebra.dummy.controller;

import com.rebra.common.CommonApiResponse;
import com.rebra.annotation.LoginUser;
import com.rebra.dummy.dto.DummyDataResponse;
import com.rebra.dummy.dto.UpdateCreatedAtRequest;
import com.rebra.dummy.dto.UpdateCreatedAtResponse;
import com.rebra.dummy.service.DummyDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Tag(name = "Developer", description = "개발자용 더미 데이터 생성 API")
public class DummyController {

    private final DummyDataService dummyDataService;

    @Operation(
            summary = "리밸런싱 히스토리 더미 데이터 생성",
            description = "포트폴리오에 대한 더미 리밸런싱 히스토리 데이터를 생성합니다. " +
                         "최근 3개월간 4~6개의 리밸런싱 주문과 각각 2~5개의 거래 기록을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "더미 데이터 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 - 본인의 포트폴리오가 아님"),
            @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음")
    })
    @PostMapping("/portfolios/{portfolioId}/dummy-rebalancing-history")
    public ResponseEntity<CommonApiResponse<DummyDataResponse>> createDummyRebalancingHistory(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId) {

        log.info("더미 리밸런싱 히스토리 생성 요청 - userId: {}, portfolioId: {}", userId, portfolioId);

        DummyDataResponse response = dummyDataService.createDummyRebalancingHistory(userId, portfolioId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(
            summary = "포트폴리오 생성일시 업데이트",
            description = "특정 포트폴리오의 생성일시(createdAt)를 지정된 날짜/시간으로 업데이트합니다. " +
                         "개발 및 테스트 목적으로 사용됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성일시 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 유효하지 않은 날짜/시간"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 - 본인의 포트폴리오가 아님"),
            @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음")
    })
    @PutMapping("/portfolios/{portfolioId}/created-at")
    public ResponseEntity<CommonApiResponse<UpdateCreatedAtResponse>> updatePortfolioCreatedAt(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Parameter(description = "업데이트할 생성일시", required = true) @Valid @RequestBody UpdateCreatedAtRequest request) {

        log.info("포트폴리오 생성일시 업데이트 요청 - userId: {}, portfolioId: {}, createdAt: {}",
                userId, portfolioId, request.getCreatedAt());

        UpdateCreatedAtResponse response = dummyDataService.updatePortfolioCreatedAt(userId, portfolioId, request.getCreatedAt());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }
}