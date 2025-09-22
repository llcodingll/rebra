package com.rebra.service;

import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockBasicInfoResponse;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockDetailResponse;
import com.rebra.dto.response.StockHistoricalDataResponse;
import com.rebra.dto.response.StockHoldingDetailResponse;
import com.rebra.dto.response.StockHoldingListResponse;
import com.rebra.dto.response.StockSearchResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface StockService {


    /**
     * 종목 코드로 주식 조회
     */
    StockSearchResponse findByStockCode(String stockCode);

    /**
     * 주식 검색 (종목명 부분 일치, 활성 상태만)
     */
    PageResponse<StockSearchResponse> searchStocks(String stockName, Pageable pageable);

    /**
     * FSS API를 통한 주식 검색 (종목명 부분 일치, 최근 영업일 기준)
     */
    List<StockHistoricalDataResponse> searchStocksFromApi(String stockName);

    /**
     * FSS API를 통한 종목 기본정보 검색 (종목명 부분 일치)
     */
    List<StockBasicInfoResponse> searchStockBasicInfoFromApi(String stockName);

    /**
     * 종목 차트 데이터 조회 (일/주/월/년봉)
     */
    StockChartResponse getStockChartData(String stockCode, String startDate, String endDate, String periodType,
                                         Long userId);

    /**
     * 종목 상세 정보 조회 (보유 정보 포함 옵션)
     */
    StockDetailResponse getStockDetail(String stockCode, boolean includeHolding, Long userId);

    /**
     * 전체 보유 종목 조회 (페이지네이션)
     */
    com.rebra.common.PageResponse<StockHoldingListResponse> getHoldingStocks(Long accountId, Pageable pageable);

    /**
     * 특정 종목 보유 정보 조회
     */
    StockHoldingDetailResponse getStockHolding(String stockCode, Long accountId);
}