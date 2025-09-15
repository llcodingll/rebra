package com.rebra.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RebalancingPeriod {
    MONTHLY("월간"),
    YEARLY("연간");

    private final String description;
}