package com.rebra.service;

import com.rebra.dto.request.PortfolioStockRegisterRequest;
import com.rebra.dto.request.PortfolioStockDeleteRequest;
import com.rebra.dto.request.PortfolioStockBatchUpdateRequest;
import com.rebra.dto.response.PortfolioStockResponse;

import java.util.List;

public interface PortfolioStockService {

    /**
     * 포트폴리오에 주식 등록
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     * @param request 주식 등록 요청
     * @return 주식 등록 응답
     */
    PortfolioStockResponse registerStock(Long userId, Long portfolioId, PortfolioStockRegisterRequest request);

    /**
     * 포트폴리오에서 주식 삭제
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     * @param request 주식 삭제 요청
     */
    void deleteStock(Long userId, Long portfolioId, PortfolioStockDeleteRequest request);

    /**
     * 포트폴리오 주식 설정 일괄 업데이트
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     * @param request 일괄 업데이트 요청
     * @return 업데이트된 주식 목록
     */
    List<PortfolioStockResponse> updateStocksBatch(Long userId, Long portfolioId, PortfolioStockBatchUpdateRequest request);
}