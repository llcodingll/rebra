package com.rebra.dummy.controller;

import com.rebra.common.CommonApiResponse;
import com.rebra.annotation.LoginUser;
import com.rebra.dummy.dto.DummyDataResponse;
import com.rebra.dummy.dto.UpdateCreatedAtRequest;
import com.rebra.dummy.dto.UpdateCreatedAtResponse;
import com.rebra.dummy.service.DummyDataService;
import com.rebra.dto.response.TokenRefreshResponse;
import com.rebra.entity.User;
import com.rebra.entity.Account;
import com.rebra.entity.AccountType;
import com.rebra.entity.Portfolio;
import com.rebra.entity.PortfolioStock;
import com.rebra.entity.RebalancingOrder;
import com.rebra.entity.TradeRecord;
import com.rebra.enums.TransactionStatus;
import com.rebra.enums.ExecutionType;
import com.rebra.repository.UserRepository;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.PortfolioStockRepository;
import com.rebra.repository.StockRepository;
import com.rebra.repository.RebalancingOrderRepository;
import com.rebra.repository.TradeRecordRepository;
import com.rebra.service.TokenService;
import com.rebra.util.CookieUtil;
import org.springframework.http.ResponseCookie;
import java.time.Duration;
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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Tag(name = "Developer", description = "개발자용 더미 데이터 생성 API")
public class DummyController {

    private final DummyDataService dummyDataService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockRepository portfolioStockRepository;
    private final RebalancingOrderRepository rebalancingOrderRepository;
    private final TradeRecordRepository tradeRecordRepository;
    private final TokenService tokenService;

    @Operation(
            summary = "성과 메트릭 더미 데이터 생성",
            description = "포트폴리오에 대한 더미 성과 메트릭 데이터를 생성합니다. " +
                         "포트폴리오 생성일부터 오늘까지 매일의 성과 데이터를 생성하며, " +
                         "포트폴리오 가치 변화, 수익률, 리밸런싱 및 거래 이력을 포함합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "더미 데이터 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 - 본인의 포트폴리오가 아님"),
            @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음")
    })
    @PostMapping("/portfolios/{portfolioId}/dummy-performance-metrics")
    public ResponseEntity<CommonApiResponse<DummyDataResponse>> createDummyPerformanceMetrics(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId) {

        log.info("더미 성과 메트릭 생성 요청 - userId: {}, portfolioId: {}", userId, portfolioId);

        DummyDataResponse response = dummyDataService.createDummyPerformanceMetrics(userId, portfolioId);

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