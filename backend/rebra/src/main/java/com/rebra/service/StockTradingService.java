package com.rebra.service;

import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.entity.User;
import com.rebra.enums.ExecutionType;

public interface StockTradingService {

    /**
     * 주식 매수 주문
     * @param stockCode 종목코드
     * @param request 매수 요청 정보
     * @param userId 주문을 실행하는 사용자 ID
     * @param executionType 실행 유형
     * @return 주문 결과
     */
    StockTradeResponse buyStock(String stockCode, StockTradeRequest request, Long userId, ExecutionType executionType);

    /**
     * 주식 매도 주문
     * @param stockCode 종목코드
     * @param request 매도 요청 정보
     * @param userId 주문을 실행하는 사용자 ID
     * @param executionType 실행 유형
     * @return 주문 결과
     */
    StockTradeResponse sellStock(String stockCode, StockTradeRequest request, Long userId, ExecutionType executionType);

    /**
     * 주식 매수 주문 (오버로드)
     * @param request 매수 요청 정보 (종목코드 포함)
     * @param userId 주문을 실행하는 사용자 ID
     * @param executionType 실행 유형
     * @return 주문 결과
     */
    StockTradeResponse buyStock(StockTradeRequest request, Long userId, ExecutionType executionType);

    /**
     * 주식 매도 주문 (오버로드)
     * @param request 매도 요청 정보 (종목코드 포함)
     * @param userId 주문을 실행하는 사용자 ID
     * @param executionType 실행 유형
     * @return 주문 결과
     */
    StockTradeResponse sellStock(StockTradeRequest request, Long userId, ExecutionType executionType);
}