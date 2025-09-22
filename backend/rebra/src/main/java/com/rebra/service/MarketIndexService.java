package com.rebra.service;

import com.rebra.dto.response.MarketIndexResponse;

public interface MarketIndexService {
    
    /**
     * 최신 시장 지수 정보를 조회한다.
     * 코스피, 코스피 200, KRX 300, 코스닥 지수를 포함한다.
     * 
     * @return 시장 지수 응답 데이터
     */
    MarketIndexResponse getLatestMarketIndices();
}