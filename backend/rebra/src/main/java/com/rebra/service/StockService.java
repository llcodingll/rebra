package com.rebra.service;

import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockSearchResponse;
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
}