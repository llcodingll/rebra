package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.request.AutoRebalancingUpdateRequest;
import com.rebra.dto.request.PortfolioBasicUpdateRequest;
import com.rebra.dto.request.PortfolioCreateRequest;
import com.rebra.dto.request.PortfolioRebalancingSettingsRequest;
import com.rebra.dto.response.PortfolioCreateResponse;
import com.rebra.dto.response.PortfolioDetailResponse;
import com.rebra.dto.response.PortfolioListResponse;
import com.rebra.dto.response.PortfolioUpdateResponse;
import com.rebra.service.PortfolioService;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/portfolios")
@Tag(name = "포트폴리오 관리", description = "포트폴리오 CRUD 및 설정 관리 API")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 목록 조회", description = "사용자의 포트폴리오 목록과 수익률 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<CommonApiResponse<PortfolioListResponse>> getPortfolioList(
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("포트폴리오 목록 조회 요청 - 사용자ID: {}", userId);

        PortfolioListResponse response = portfolioService.getPortfolioList(userId);

        log.info("포트폴리오 목록 조회 성공 - 사용자ID: {}, 포트폴리오 수: {}",
                userId, response.getPortfolios().size());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "포트폴리오 생성", description = "새로운 포트폴리오를 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<CommonApiResponse<PortfolioCreateResponse>> createPortfolio(
            @Valid @RequestBody PortfolioCreateRequest request,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("포트폴리오 생성 요청 - 사용자ID: {}, 포트폴리오명: {}, 계좌ID: {}",
                userId, request.getName(), request.getAccountId());

        PortfolioCreateResponse response = portfolioService.createPortfolio(userId, request);

        log.info("포트폴리오 생성 성공 - 사용자ID: {}, 포트폴리오ID: {}",
                userId, response.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.success(response));
    }

    @Operation(summary = "포트폴리오 삭제", description = "포트폴리오를 삭제합니다. 연관된 등록 주식도 함께 삭제됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<CommonApiResponse<Void>> deletePortfolio(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("포트폴리오 삭제 요청 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        portfolioService.deletePortfolio(userId, portfolioId);

        log.info("포트폴리오 삭제 성공 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        return ResponseEntity.ok(CommonApiResponse.success());
    }

    @Operation(summary = "포트폴리오 상세 조회", description = "포트폴리오 상세 정보와 등록/미등록 주식을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "조회 권한 없음"),
        @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{portfolioId}")
    public ResponseEntity<CommonApiResponse<PortfolioDetailResponse>> getPortfolioDetail(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("포트폴리오 상세 조회 요청 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        PortfolioDetailResponse response = portfolioService.getPortfolioDetail(userId, portfolioId);

        log.info("포트폴리오 상세 조회 성공 - 사용자ID: {}, 포트폴리오ID: {}, 등록주식: {}개, 미등록주식: {}개",
                userId, portfolioId, response.getRegisteredStocks().size(), response.getUnregisteredStocks().size());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "포트폴리오 기본 정보 수정", description = "포트폴리오의 이름과 설명을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{portfolioId}/info")
    public ResponseEntity<CommonApiResponse<PortfolioUpdateResponse>> updateBasicInfo(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioBasicUpdateRequest request,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("포트폴리오 기본 정보 수정 요청 - 사용자ID: {}, 포트폴리오ID: {}, 이름: {}",
                userId, portfolioId, request.getName());

        PortfolioUpdateResponse response = portfolioService.updateBasicInfo(userId, portfolioId, request);

        log.info("포트폴리오 기본 정보 수정 성공 - 사용자ID: {}, 포트폴리오ID: {}",
                userId, portfolioId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "자동 리밸런싱 설정", description = "포트폴리오의 자동 리밸런싱을 활성화하거나 비활성화합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "설정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "설정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{portfolioId}/auto-rebalancing")
    public ResponseEntity<CommonApiResponse<PortfolioUpdateResponse>> updateAutoRebalancing(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Valid @RequestBody AutoRebalancingUpdateRequest request,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("자동 리밸런싱 설정 요청 - 사용자ID: {}, 포트폴리오ID: {}, 설정: {}",
                userId, portfolioId, request.getAutoRebalancing());

        PortfolioUpdateResponse response = portfolioService.updateAutoRebalancing(
                userId, portfolioId, request.getAutoRebalancing());

        log.info("자동 리밸런싱 설정 성공 - 사용자ID: {}, 포트폴리오ID: {}, 설정: {}",
                userId, portfolioId, request.getAutoRebalancing());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(summary = "리밸런싱 주기 설정", description = "자동 리밸런싱의 주기와 관련 설정을 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "설정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "설정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{portfolioId}/rebalancing-schedule")
    public ResponseEntity<CommonApiResponse<PortfolioUpdateResponse>> updateRebalancingSettings(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioRebalancingSettingsRequest request,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("리밸런싱 설정 변경 요청 - 사용자ID: {}, 포트폴리오ID: {}, 주기: {}개월",
                userId, portfolioId, request.getRebalancingInterval());

        PortfolioUpdateResponse response = portfolioService.updateRebalancingSettings(
                userId, portfolioId, request);

        log.info("리밸런싱 설정 변경 성공 - 사용자ID: {}, 포트폴리오ID: {}",
                userId, portfolioId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }
}