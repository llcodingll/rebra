package com.rebra.service;

import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.entity.User;

public interface StockTradingService {

    /**
     * 주식 매수 주문
     * @param stockCode 종목코드
     * @param request 매수 요청 정보
     * @param user 주문을 실행하는 사용자
     * @return 주문 결과
     */
    StockTradeResponse buyStock(String stockCode, StockTradeRequest request, User user);

    /**
     * 주식 매도 주문
     * @param stockCode 종목코드
     * @param request 매도 요청 정보
     * @param user 주문을 실행하는 사용자
     * @return 주문 결과
     */
    StockTradeResponse sellStock(String stockCode, StockTradeRequest request, User user);
}