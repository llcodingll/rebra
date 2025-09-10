package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.request.BacktestCreateRequest;
import com.rebra.dto.response.BacktestListResponse;
import com.rebra.dto.response.BacktestResultResponse;
import com.rebra.dto.response.BacktestValidationResponse;
import com.rebra.entity.User;
import com.rebra.service.BacktestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/backtests")
@RequiredArgsConstructor
@Tag(name = "백테스트", description = "포트폴리오 백테스트 API")
public class BacktestController {

    private final BacktestService backtestService;

    @PostMapping("/validate")
    @Operation(summary = "백테스트 실행 가능성 검증", description = "백테스트 요청이 실행 가능한지 사전 검증합니다.")
    public ResponseEntity<CommonApiResponse<BacktestValidationResponse>> validateBacktest(
            @Parameter(hidden = true) @LoginUser User user,
            @Valid @RequestBody BacktestCreateRequest request) {

        BacktestValidationResponse validation = backtestService.validateBacktestRequest(user, request);
        return ResponseEntity.ok(CommonApiResponse.success(validation));
    }

    @PostMapping
    @Operation(summary = "백테스트 생성", description = "새로운 백테스트를 생성하고 계산을 요청합니다.")
    public ResponseEntity<CommonApiResponse<Long>> createBacktest(
            @Parameter(hidden = true) @LoginUser User user,
            @Valid @RequestBody BacktestCreateRequest request) {

        Long backtestId = backtestService.createBacktest(user, request);
        return ResponseEntity.ok(CommonApiResponse.success(backtestId));
    }

    @GetMapping
    @Operation(summary = "백테스트 목록 조회", description = "사용자의 백테스트 목록을 조회합니다.")
    public ResponseEntity<CommonApiResponse<Page<BacktestListResponse>>> getBacktestList(
            @Parameter(hidden = true) @LoginUser User user,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<BacktestListResponse> backtests = backtestService.getBacktestList(user, pageable);
        return ResponseEntity.ok(CommonApiResponse.success(backtests));
    }

    @GetMapping("/{backtestId}")
    @Operation(summary = "백테스트 결과 조회", description = "특정 백테스트의 상세 결과를 조회합니다.")
    public ResponseEntity<CommonApiResponse<BacktestResultResponse>> getBacktestResult(
            @Parameter(hidden = true) @LoginUser User user,
            @PathVariable Long backtestId) {

        BacktestResultResponse result = backtestService.getBacktestResult(user, backtestId);
        return ResponseEntity.ok(CommonApiResponse.success(result));
    }

    @DeleteMapping("/{backtestId}")
    @Operation(summary = "백테스트 삭제", description = "특정 백테스트를 삭제합니다.")
    public ResponseEntity<CommonApiResponse<Void>> deleteBacktest(
            @Parameter(hidden = true) @LoginUser User user,
            @PathVariable Long backtestId) {

        backtestService.deleteBacktest(user, backtestId);
        return ResponseEntity.ok(CommonApiResponse.success());
    }

}