package com.rebra.rebalance.domain.rebalancing.model;

public enum RebalancingStrategy {
    THRESHOLD,  // 비중 차이가 임계값 초과 시 실행
    PERIODIC    // 주기적 실행
}
