package com.rebra.service;

import com.rebra.dto.request.StockHistoricalSearchRequest;
import com.rebra.dto.response.StockHistoricalDataResponse;
import com.rebra.dto.response.StockHistoricalDataWithTradingInfo;

import java.util.List;

public interface StockHistoricalService {

    /**
     * 종목명과 날짜로 과거 주식 데이터 조회 (거래일 정보 포함)
     * 1. 거래일 여부 체크
     * 2. DB에서 먼저 조회
     * 3. 없으면 FSS API에서 조회 후 DB에 저장
     * 4. 거래일 정보와 함께 결과 반환
     */
    StockHistoricalDataWithTradingInfo getStockHistoricalData(StockHistoricalSearchRequest request);

    /**
     * 종목명과 날짜로 과거 주식 데이터 조회 (기존 메서드 - 하위 호환성)
     * @deprecated 거래일 정보가 포함된 getStockHistoricalData 메서드 사용 권장
     */
    @Deprecated
    List<StockHistoricalDataResponse> getStockHistoricalDataLegacy(StockHistoricalSearchRequest request);
}