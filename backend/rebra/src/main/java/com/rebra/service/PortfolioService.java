package com.rebra.service;

import com.rebra.dto.request.PortfolioBasicUpdateRequest;
import com.rebra.dto.request.PortfolioCreateRequest;
import com.rebra.dto.request.PortfolioRebalancingSettingsRequest;
import com.rebra.dto.response.PortfolioCreateResponse;
import com.rebra.dto.response.PortfolioDetailResponse;
import com.rebra.dto.response.PortfolioListResponse;
import com.rebra.dto.response.PortfolioUpdateResponse;

public interface PortfolioService {

    /**
     * 포트폴리오 목록 조회
     * @param userId 사용자 ID
     * @return 포트폴리오 목록
     */
    PortfolioListResponse getPortfolioList(Long userId);

    /**
     * 포트폴리오 생성
     * @param userId 사용자 ID
     * @param request 포트폴리오 생성 요청
     * @return 포트폴리오 생성 응답
     */
    PortfolioCreateResponse createPortfolio(Long userId, PortfolioCreateRequest request);

    /**
     * 포트폴리오 삭제
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     */
    void deletePortfolio(Long userId, Long portfolioId);

    /**
     * 포트폴리오 개수 조회
     * @param userId 사용자 ID
     * @return 포트폴리오 개수
     */
    long getPortfolioCount(Long userId);

    /**
     * 자동 리밸런싱 활성화/비활성화
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     * @param autoRebalancing 자동 리밸런싱 여부
     * @return 포트폴리오 수정 응답
     */
    PortfolioUpdateResponse updateAutoRebalancing(Long userId, Long portfolioId, Boolean autoRebalancing);

    /**
     * 리밸런싱 설정 변경
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     * @param request 리밸런싱 설정 요청
     * @return 포트폴리오 수정 응답
     */
    PortfolioUpdateResponse updateRebalancingSettings(Long userId, Long portfolioId, PortfolioRebalancingSettingsRequest request);

    /**
     * 포트폴리오 기본 정보 수정
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     * @param request 기본 정보 수정 요청
     * @return 포트폴리오 수정 응답
     */
    PortfolioUpdateResponse updateBasicInfo(Long userId, Long portfolioId, PortfolioBasicUpdateRequest request);

    /**
     * 포트폴리오 상세 조회 (등록/미등록 주식 분류 포함)
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     * @return 포트폴리오 상세 정보
     */
    PortfolioDetailResponse getPortfolioDetail(Long userId, Long portfolioId);
}