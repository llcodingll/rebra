package com.rebra.service;

import com.rebra.dto.response.StockSearchResponse;

import java.util.List;

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
     * 종목명으로 주식 검색 (부분 일치, 대소문자 무시)
     */
    List<StockSearchResponse> searchByStockName(String stockName);

    /**
     * 활성 상태인 주식만 종목명으로 검색 (부분 일치, 대소문자 무시)
     */
    List<StockSearchResponse> searchActiveStocksByName(String stockName);
}