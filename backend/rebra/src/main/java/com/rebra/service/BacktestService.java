package com.rebra.service;

import com.rebra.dto.request.BacktestCreateRequest;
import com.rebra.dto.response.BacktestListResponse;
import com.rebra.dto.response.BacktestResultResponse;
import com.rebra.dto.response.BacktestValidationResponse;
import com.rebra.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface BacktestService {

    /**
     * 백테스트 실행 가능성 검증
     */
    BacktestValidationResponse validateBacktestRequest(Long userId, BacktestCreateRequest request);

    /**
     * 백테스트 생성 및 요청
     */
    Long createBacktest(Long userId, BacktestCreateRequest request);

    /**
     * 사용자의 백테스트 목록 조회
     */
    PageResponse<BacktestListResponse> getBacktestList(Long userId, Pageable pageable);

    /**
     * 백테스트 상세 결과 조회
     */
    BacktestResultResponse getBacktestResult(Long backtestId);

    /**
     * 백테스트 삭제
     */
    void deleteBacktest(Long userId, Long backtestId);

    /**
     * 백테스트 결과 처리 (Kafka Consumer에서 호출)
     */
    void processBacktestResult(Map<String, Object> backtestResponse);

}