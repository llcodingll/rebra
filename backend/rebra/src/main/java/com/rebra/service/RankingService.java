package com.rebra.service;

import com.rebra.dto.response.VolumeRankingResponse;
import com.rebra.dto.response.FluctuationRankingResponse;

/**
 * 주식 순위 조회 서비스 인터페이스
 */
public interface RankingService {

    /**
     * 거래량 순위 조회
     *
     * @param accountId 계좌 ID
     * @return 거래량 순위 응답
     */
    VolumeRankingResponse getVolumeRanking(Long accountId);

    /**
     * 급상승 종목 순위 조회
     *
     * @param accountId 계좌 ID
     * @return 급상승 종목 순위 응답
     */
    FluctuationRankingResponse getFluctuationRankingRising(Long accountId);

    /**
     * 급하락 종목 순위 조회
     *
     * @param accountId 계좌 ID
     * @return 급하락 종목 순위 응답
     */
    FluctuationRankingResponse getFluctuationRankingFalling(Long accountId);
}