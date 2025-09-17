package com.rebra.service;

import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockHistoricalDataResponse;
import com.rebra.dto.response.StockSearchResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockService {


    /**
     * 주식 검색 (종목명 부분 일치, 활성 상태만)
     */
    PageResponse<StockSearchResponse> searchStocks(String stockName, Pageable pageable);

    /**
     * FSS API를 통한 주식 검색 (종목명 부분 일치, 최근 영업일 기준)
     */
    List<StockHistoricalDataResponse> searchStocksFromApi(String stockName);

    /**
     * 종목 차트 데이터 조회 (일/주/월/년봉)
     */
    StockChartResponse getStockChartData(String stockCode, String startDate, String endDate, String periodType,
                                         Long userId);
}