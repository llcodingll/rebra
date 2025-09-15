package com.rebra.service;

import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockDetailResponse;
import com.rebra.dto.response.StockSearchResponse;
import com.rebra.entity.User;
import org.springframework.data.domain.Pageable;

public interface StockService {

    /**
     * 종목 코드로 주식 조회
     */
    StockSearchResponse findByStockCode(String stockCode);

    /**
     * 종목명으로 주식 조회
     */
    StockSearchResponse findByStockName(String stockName);

    /**
     * 주식 검색 (종목명 부분 일치, 활성 상태만)
     */
    PageResponse<StockSearchResponse> searchStocks(String stockName, Pageable pageable);

    /**
     * 종목 상세 정보 조회 (실시간 데이터 포함) - 기존 방식
     */
    StockDetailResponse getStockDetailWithRealtime(String stockCode, User user);
    
    /**
     * 종목 상세 정보 조회 (WebSocket 채널 정보 포함) - 하이브리드 방식
     */
    StockDetailResponse getStockDetailWithWebSocketInfo(String stockCode, User user);
}