package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.response.RebalancingExecutionResponse;
import com.rebra.service.PortfolioRebalancingService;
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
@RequestMapping("/api/v1/portfolios/{portfolioId}/rebalancing")
@Tag(name = "포트폴리오 리밸런싱", description = "포트폴리오 리밸런싱 실행 API")
public class RebalancingController {

    private final PortfolioRebalancingService portfolioRebalancingService;

    @Operation(
        summary = "수동 리밸런싱 실행", 
        description = "포트폴리오의 수동 리밸런싱을 즉시 실행합니다. 현재 보유 종목과 목표 비중을 기준으로 매수/매도 주문을 생성합니다."
    )
    @PostMapping("/execute")
    public ResponseEntity<CommonApiResponse<RebalancingExecutionResponse>> executeManualRebalancing(
            @Parameter(description = "포트폴리오 ID", required = true) 
            @PathVariable Long portfolioId,
            @Parameter(hidden = true) 
            @LoginUser Long userId) {

        log.info("수동 리밸런싱 실행 요청 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        RebalancingExecutionResponse response = portfolioRebalancingService
                .executeManualRebalancing(userId, portfolioId);

        if (response.isSuccess()) {
            log.info("수동 리밸런싱 실행 성공 - 사용자ID: {}, 포트폴리오ID: {}, " +
                    "매수금액: {}원, 매도금액: {}원, 거래건수: {}건",
                    userId, portfolioId, response.getTotalBuyAmount(), 
                    response.getTotalSellAmount(), response.getOrderResults().size());
        } else {
            log.warn("수동 리밸런싱 실행 실패 - 사용자ID: {}, 포트폴리오ID: {}, 실패사유: {}",
                    userId, portfolioId, response.getFailureReason());
        }

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }
}