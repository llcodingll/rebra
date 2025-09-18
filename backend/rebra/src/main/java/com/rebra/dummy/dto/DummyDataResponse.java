package com.rebra.dummy.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DummyDataResponse {

    private String message;
    private int createdRebalancingOrders;
    private int createdTradeRecords;
    private LocalDateTime createdAt;

    public static DummyDataResponse success(int rebalancingOrders, int tradeRecords) {
        return DummyDataResponse.builder()
                .message("더미 리밸런싱 히스토리 데이터 생성 완료")
                .createdRebalancingOrders(rebalancingOrders)
                .createdTradeRecords(tradeRecords)
                .createdAt(LocalDateTime.now())
                .build();
    }
}