package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.response.RebalancingHistoryDetailResponse;
import com.rebra.dto.response.RebalancingHistoryGraphResponse;
import com.rebra.dto.response.RebalancingHistoryListResponse;
import com.rebra.common.PageResponse;
import com.rebra.service.RebalancingHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/portfolios/{portfolioId}/rebalancing-history")
@Tag(name = "리밸런싱 히스토리", description = "포트폴리오 리밸런싱 히스토리 조회 API")
public class RebalancingHistoryController {

    private final RebalancingHistoryService rebalancingHistoryService;

    @Operation(
            summary = "리밸런싱 히스토리 목록 조회 (페이지네이션)",
            description = "포트폴리오의 리밸런싱 히스토리를 페이지 단위로 조회합니다. 요약 정보도 함께 제공됩니다. " +
                         "조회 기간은 포트폴리오 생성일부터 마지막 리밸런싱 날짜까지 자동으로 계산됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<CommonApiResponse<PageResponse<RebalancingHistoryListResponse>>> getRebalancingHistoryWithPagination(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "페이지 번호 (0부터 시작)", schema = @Schema(defaultValue = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", schema = @Schema(defaultValue = "20"))
            @RequestParam(defaultValue = "20") int size) {

        log.info("리밸런싱 히스토리 페이지네이션 조회 요청 - 사용자ID: {}, 포트폴리오ID: {}, 페이지: {}, 크기: {}",
                userId, portfolioId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<RebalancingHistoryListResponse> response = rebalancingHistoryService
                .getRebalancingHistoryWithPagination(portfolioId, pageable);

        log.info("리밸런싱 히스토리 페이지네이션 조회 성공 - 포트폴리오ID: {}, 총 요소 수: {}",
                portfolioId, response.pageInfo().totalElements());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(
            summary = "리밸런싱 히스토리 전체 조회 (그래프용)",
            description = "포트폴리오의 모든 리밸런싱 히스토리를 조회합니다. 그래프 그리기 용도로 사용됩니다. " +
                         "조회 기간은 포트폴리오 생성일부터 마지막 리밸런싱 날짜까지 자동으로 계산되며, " +
                         "시작점으로 100% 수익률의 더미 데이터가 포함됩니다. " +
                         "성능 최적화를 위해 거래 내역(trades)은 제외되며, orderId 클릭 시 상세 조회 API를 사용하세요."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/all")
    public ResponseEntity<CommonApiResponse<List<RebalancingHistoryGraphResponse>>> getAllRebalancingHistory(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId) {

        log.info("리밸런싱 히스토리 전체 조회 (그래프용) 요청 - 사용자ID: {}, 포트폴리오ID: {}",
                userId, portfolioId);

        List<RebalancingHistoryGraphResponse> response = rebalancingHistoryService
                .getAllRebalancingHistory(portfolioId);

        log.info("리밸런싱 히스토리 전체 조회 (그래프용) 성공 - 포트폴리오ID: {}, 히스토리 수: {}",
                portfolioId, response.size());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(
            summary = "리밸런싱 히스토리 상세 조회",
            description = "특정 리밸런싱 주문의 상세 정보와 거래 내역을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "리밸런싱 주문을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<CommonApiResponse<RebalancingHistoryDetailResponse>> getRebalancingHistoryDetail(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "리밸런싱 주문 ID", required = true)
            @PathVariable Long orderId) {

        log.info("리밸런싱 히스토리 상세 조회 요청 - 사용자ID: {}, 포트폴리오ID: {}, 주문ID: {}",
                userId, portfolioId, orderId);

        RebalancingHistoryDetailResponse response = rebalancingHistoryService
                .getRebalancingHistoryDetail(portfolioId, orderId);

        log.info("리밸런싱 히스토리 상세 조회 성공 - 포트폴리오ID: {}, 주문ID: {}, 거래 수: {}",
                portfolioId, orderId, response.getTrades().size());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }
}