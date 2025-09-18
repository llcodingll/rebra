package com.rebra.service;

import com.rebra.dto.response.RebalancingHistoryDetailResponse;
import com.rebra.dto.response.RebalancingHistoryGraphResponse;
import com.rebra.dto.response.RebalancingHistoryListResponse;
import com.rebra.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface RebalancingHistoryService {

    /**
     * 포트폴리오 리밸런싱 히스토리 목록 조회 (페이지네이션)
     * @param portfolioId 포트폴리오 ID
     * @param pageable 페이지 정보
     * @return 페이지네이션된 리밸런싱 히스토리와 요약 정보
     */
    PageResponse<RebalancingHistoryListResponse> getRebalancingHistoryWithPagination(
            Long portfolioId, Pageable pageable);

    /**
     * 포트폴리오 리밸런싱 히스토리 전체 조회 (그래프용)
     * @param portfolioId 포트폴리오 ID
     * @return 전체 리밸런싱 히스토리 목록 (시작점 더미 데이터 포함, trades 제외)
     */
    List<RebalancingHistoryGraphResponse> getAllRebalancingHistory(Long portfolioId);

    /**
     * 리밸런싱 히스토리 상세 조회
     * @param portfolioId 포트폴리오 ID
     * @param orderId 리밸런싱 주문 ID
     * @return 리밸런싱 상세 정보 (거래 내역 포함)
     */
    RebalancingHistoryDetailResponse getRebalancingHistoryDetail(Long portfolioId, Long orderId);
}