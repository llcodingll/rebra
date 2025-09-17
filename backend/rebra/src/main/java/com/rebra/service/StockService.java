package com.rebra.service;

import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockDetailResponse;
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

    /**
     * 종목 차트 데이터 조회 (일/주/월/년봉)
     */
    StockChartResponse getStockChartData(String stockCode, String startDate, String endDate, String periodType,
                                         Long userId);

    /**
     * 종목 상세 정보 조회 (보유 정보 포함 옵션)
     */
    StockDetailResponse getStockDetail(String stockCode, boolean includeHolding, Long userId);
}