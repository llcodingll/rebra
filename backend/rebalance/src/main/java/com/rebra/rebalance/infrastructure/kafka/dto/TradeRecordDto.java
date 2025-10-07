package com.rebra.rebalance.infrastructure.kafka.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradeRecordDto {
    private String stockCode;
    private String stockName;
    private String orderType;      // OrderType enum 이름
    private Integer quantity;
    private Long price;
    private String status;         // OrderStatus enum 이름
    private String kisOrderNumber;
}
