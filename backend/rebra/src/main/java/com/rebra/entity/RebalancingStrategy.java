package com.rebra.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RebalancingStrategy {
    THRESHOLD("임계값 기반"),
    PERIODIC("주기적 리밸런싱"),
    CASHFLOW("캐시플로우 기반");

    private final String description;
}