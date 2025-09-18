package com.rebra.service;

import com.rebra.dto.request.StockHistoricalSearchRequest;
import com.rebra.dto.response.StockHistoricalDataResponse;

import java.util.List;

public interface StockHistoricalService {

    /**
     * 종목명과 날짜로 과거 주식 데이터 조회
     * 1. DB에서 먼저 조회
     * 2. 없으면 FSS API에서 조회 후 DB에 저장
     * 3. 결과 반환
     */
    List<StockHistoricalDataResponse> getStockHistoricalData(StockHistoricalSearchRequest request);
}