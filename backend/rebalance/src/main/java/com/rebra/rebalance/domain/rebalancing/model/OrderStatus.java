package com.rebra.rebalance.domain.rebalancing.model;

public enum OrderStatus {
    PENDING,    // KIS API 호출 전 선기록
    COMPLETED,  // KIS 체결 확인
    FAILED      // KIS 실패 또는 체결 미확인
}
