package com.rebra.service;

import com.rebra.dto.response.PerformanceMetricsChartResponse;
import com.rebra.dto.response.TradeHistoryResponse;
import com.rebra.entity.PerformanceMetrics;
import com.rebra.entity.Portfolio;

import java.time.LocalDate;
import java.util.List;

/**
 * 포트폴리오 성과 메트릭 서비스 인터페이스
 */
public interface PerformanceMetricsService {

    /**
     * 포트폴리오의 일일 성과 메트릭 수집 및 저장
     *
     * @param portfolio 포트폴리오 엔티티
     * @param targetDate 수집 대상 날짜
     */
    void collectDailyMetrics(Portfolio portfolio, LocalDate targetDate);

    /**
     * 포트폴리오별 성과 메트릭 조회 (최신순)
     *
     * @param portfolioId 포트폴리오 ID
     * @return 성과 메트릭 리스트
     */
    List<PerformanceMetrics> getPortfolioMetrics(Long portfolioId);

    /**
     * 포트폴리오의 특정 기간 성과 메트릭 조회
     *
     * @param portfolioId 포트폴리오 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 성과 메트릭 리스트
     */
    List<PerformanceMetrics> getPortfolioMetricsInPeriod(Long portfolioId, LocalDate startDate, LocalDate endDate);

    /**
     * 포트폴리오의 특정 날짜 성과 메트릭 존재 여부 확인
     *
     * @param portfolioId 포트폴리오 ID
     * @param targetDate 확인할 날짜
     * @return 존재 여부
     */
    boolean existsMetrics(Long portfolioId, LocalDate targetDate);

    /**
     * 포트폴리오 성과 차트 데이터 조회 (생성일부터 오늘까지)
     *
     * @param portfolioId 포트폴리오 ID
     * @param userId 사용자 ID (권한 검증용)
     * @return 차트용 성과 메트릭 응답
     */
    PerformanceMetricsChartResponse getPortfolioPerformanceChart(Long portfolioId, Long userId);

    /**
     * 특정 날짜의 거래 히스토리 조회
     *
     * @param portfolioId 포트폴리오 ID
     * @param userId 사용자 ID (권한 검증용)
     * @param targetDate 조회할 날짜
     * @return 거래 히스토리 응답 (날짜 + 거래 목록)
     */
    TradeHistoryResponse getTradeHistoryByDate(Long portfolioId, Long userId, LocalDate targetDate);
}