package com.rebra.service;

import com.rebra.dto.response.StockBasicInfoResponse;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockDetailResponse;
import com.rebra.dto.response.StockHoldingDetailResponse;
import com.rebra.dto.response.StockHoldingListResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface StockService {



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
     * 전체 보유 종목 조회 (페이지네이션)
     */
    com.rebra.common.PageResponse<StockHoldingListResponse> getHoldingStocks(Long accountId, Pageable pageable);

    /**
     * 특정 종목 보유 정보 조회
     */
    StockHoldingDetailResponse getStockHolding(String stockCode, Long accountId);
}