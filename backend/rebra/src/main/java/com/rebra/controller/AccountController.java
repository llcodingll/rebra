package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.request.AccountRegisterRequest;
import com.rebra.dto.request.AccountVerifyRequest;
import com.rebra.dto.response.AccountDetailResponse;
import com.rebra.dto.response.AccountListResponse;
import com.rebra.dto.response.AccountRegisterResponse;
import com.rebra.dto.response.AccountVerifyResponse;
import com.rebra.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "계좌 관리", description = "사용자 계좌 CRUD 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "계좌 인증",
            description = "KIS API 연결 테스트를 통해 계좌 인증을 수행합니다. " +
                    "계좌번호, 앱키, 앱시크릿을 입력받아 KIS API 연결을 확인합니다."
    )
    @PostMapping("/verify")
    public ResponseEntity<CommonApiResponse<AccountVerifyResponse>> verifyAccount(
            @Valid @RequestBody AccountVerifyRequest request) {

        log.info("계좌 인증 요청 - 계좌번호: {}", request.getAccountNumber());

        AccountVerifyResponse response = accountService.verifyAccount(request);

        log.info("계좌 인증 성공 - 계좌번호: {}, 타입: {}",
                request.getAccountNumber(), response.getAccountType());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(
        summary = "계좌 등록",
        description = "KIS API 인증 후 계좌를 등록합니다. " +
                     "사전에 /api/accounts/verify로 인증을 완료한 계좌 정보를 사용해야 합니다."
    )
    @PostMapping("/register")
    public ResponseEntity<CommonApiResponse<AccountRegisterResponse>> registerAccount(
            @Valid @RequestBody AccountRegisterRequest request,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("계좌 등록 요청 - 사용자ID: {}, 계좌번호: {}", userId, request.getAccountNumber());

        AccountRegisterResponse response = accountService.registerAccount(userId, request);

        log.info("계좌 등록 성공 - 사용자ID: {}, 계좌ID: {}", userId, response.getAccountId());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(
        summary = "계좌 목록 조회",
        description = "사용자가 등록한 모든 활성 계좌 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<CommonApiResponse<AccountListResponse>> getAccountList(
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("계좌 목록 조회 요청 - 사용자ID: {}", userId);

        AccountListResponse response = accountService.getAccountList(userId);

        log.info("계좌 목록 조회 성공 - 사용자ID: {}, 계좌 수: {}", userId, response.getTotalCount());

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(
        summary = "계좌 상세 조회",
        description = "특정 계좌의 상세 정보를 조회합니다. " +
                     "계좌번호, 앱키 등의 민감한 정보는 마스킹되어 반환됩니다."
    )
    @GetMapping("/{accountId}")
    public ResponseEntity<CommonApiResponse<AccountDetailResponse>> getAccountDetail(
            @Parameter(description = "계좌 ID", required = true)
            @PathVariable Long accountId,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("계좌 상세 조회 요청 - 사용자ID: {}, 계좌ID: {}", userId, accountId);

        AccountDetailResponse response = accountService.getAccountDetail(userId, accountId);

        log.info("계좌 상세 조회 성공 - 사용자ID: {}, 계좌ID: {}", userId, accountId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    @Operation(
        summary = "계좌 삭제",
        description = "계좌를 비활성화합니다. 실제로는 soft delete가 수행되어 데이터는 보존됩니다."
    )
    @DeleteMapping("/{accountId}")
    public ResponseEntity<CommonApiResponse<Void>> deleteAccount(
            @Parameter(description = "계좌 ID", required = true)
            @PathVariable Long accountId,
            @Parameter(hidden = true) @LoginUser Long userId) {

        log.info("계좌 삭제 요청 - 사용자ID: {}, 계좌ID: {}", userId, accountId);

        accountService.deleteAccount(userId, accountId);

        log.info("계좌 삭제 성공 - 사용자ID: {}, 계좌ID: {}", userId, accountId);

        return ResponseEntity.ok(CommonApiResponse.success());
    }
}