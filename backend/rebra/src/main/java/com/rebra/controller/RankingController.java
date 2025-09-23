package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.response.FluctuationRankingResponse;
import com.rebra.dto.response.VolumeRankingResponse;
import com.rebra.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
@Tag(name = "주식 순위", description = "주식 거래량 및 등락률 순위 조회 API")
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "거래량 순위 조회", description = "주식 거래량 순위를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (모의계좌 등)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/volume")
    public ResponseEntity<CommonApiResponse<VolumeRankingResponse>> getVolumeRanking(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "계좌 ID") @RequestParam Long accountId) {

        log.info("거래량 순위 조회 요청 - 사용자ID: {}, 계좌ID: {}", userId, accountId);

        VolumeRankingResponse response = rankingService.getVolumeRanking(accountId);

        log.info("거래량 순위 조회 성공 - 사용자ID: {}, 계좌ID: {}, 결과 수: {}",
                userId, accountId, response.getRankings().size());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "급상승 종목 순위 조회", description = "급상승 종목 등락률 순위를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (모의계좌 등)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/fluctuation/rising")
    public ResponseEntity<CommonApiResponse<FluctuationRankingResponse>> getFluctuationRankingRising(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "계좌 ID") @RequestParam Long accountId) {

        log.info("급상승 종목 순위 조회 요청 - 사용자ID: {}, 계좌ID: {}", userId, accountId);

        FluctuationRankingResponse response = rankingService.getFluctuationRankingRising(accountId);

        log.info("급상승 종목 순위 조회 성공 - 사용자ID: {}, 계좌ID: {}, 결과 수: {}",
                userId, accountId, response.getRankings().size());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "급하락 종목 순위 조회", description = "급하락 종목 등락률 순위를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (모의계좌 등)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/fluctuation/falling")
    public ResponseEntity<CommonApiResponse<FluctuationRankingResponse>> getFluctuationRankingFalling(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "계좌 ID") @RequestParam Long accountId) {

        log.info("급하락 종목 순위 조회 요청 - 사용자ID: {}, 계좌ID: {}", userId, accountId);

        FluctuationRankingResponse response = rankingService.getFluctuationRankingFalling(accountId);

        log.info("급하락 종목 순위 조회 성공 - 사용자ID: {}, 계좌ID: {}, 결과 수: {}",
                userId, accountId, response.getRankings().size());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }
}