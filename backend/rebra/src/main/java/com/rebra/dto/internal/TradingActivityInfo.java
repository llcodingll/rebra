package com.rebra.dto.internal;

/**
 * 거래 활동 정보를 담는 내부 데이터 구조
 *
 * @param isRebalanced 리밸런싱 실행 여부
 * @param isSold 매도 실행 여부
 * @param isBought 매수 실행 여부
 */
public record TradingActivityInfo(boolean isRebalanced, boolean isSold, boolean isBought) {
}