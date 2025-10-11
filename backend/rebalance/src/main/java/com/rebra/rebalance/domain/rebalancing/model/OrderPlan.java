package com.rebra.rebalance.domain.rebalancing.model;

public record OrderPlan(String stockCode, String stockName,
                        OrderType orderType, int quantity, long price) {}
