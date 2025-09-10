package com.rebra.service;

import com.rebra.dto.request.BacktestCreateRequest;
import com.rebra.dto.response.BacktestListResponse;
import com.rebra.dto.response.BacktestResultResponse;
import com.rebra.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BacktestService {

    /**
     * 백테스트 생성 및 요청
     */
    Long createBacktest(User user, BacktestCreateRequest request);

    /**
     * 사용자의 백테스트 목록 조회
     */
    Page<BacktestListResponse> getBacktestList(User user, Pageable pageable);

    /**
     * 백테스트 상세 결과 조회
     */
    BacktestResultResponse getBacktestResult(User user, Long backtestId);

    /**
     * 백테스트 삭제
     */
    void deleteBacktest(User user, Long backtestId);

    /**
     * 백테스트 결과 처리 (Kafka Consumer에서 호출)
     */
    void processBacktestResult(Object backtestResponse);

}