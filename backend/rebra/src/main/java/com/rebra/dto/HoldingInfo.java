package com.rebra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 현재 보유 종목 정보
 */
@Getter
@AllArgsConstructor
public class HoldingInfo {
    
    /**
     * 종목 코드
     */
    private final String stockCode;
    
    /**
     * 종목명
     */
    private final String stockName;
    
    /**
     * 보유 수량
     */
    private final int quantity;
    
    /**
     * 현재 가격 (원 단위)
     */
    private final long currentPrice;
}