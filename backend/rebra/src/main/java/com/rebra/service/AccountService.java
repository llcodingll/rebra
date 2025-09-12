package com.rebra.service;

import com.rebra.dto.request.AccountReconnectRequest;
import com.rebra.dto.request.AccountRegisterRequest;
import com.rebra.dto.request.AccountVerifyRequest;
import com.rebra.dto.response.AccountDetailResponse;
import com.rebra.dto.response.AccountListResponse;
import com.rebra.dto.response.AccountRegisterResponse;
import com.rebra.dto.response.AccountVerifyResponse;

public interface AccountService {

    /**
     * 계좌 인증 - KIS API 연결 테스트
     */
    AccountVerifyResponse verifyAccount(AccountVerifyRequest request);

    /**
     * 계좌 등록 - 인증 성공 후 계좌 정보 저장
     */
    AccountRegisterResponse registerAccount(Long userId, AccountRegisterRequest request);

    /**
     * 사용자별 계좌 목록 조회
     */
    AccountListResponse getAccountList(Long userId);

    /**
     * 계좌 상세 정보 조회
     */
    AccountDetailResponse getAccountDetail(Long userId, Long accountId);

    /**
     * 계좌 삭제 (비활성화)
     */
    void deleteAccount(Long userId, Long accountId);

    AccountVerifyResponse verifyAccountReconnection(Long userId, AccountReconnectRequest reconnectRequest);
}