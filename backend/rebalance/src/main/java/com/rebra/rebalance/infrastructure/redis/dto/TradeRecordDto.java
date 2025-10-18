package com.rebra.rebalance.infrastructure.redis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradeRecordDto {
    private String stockCode;
    private String stockName;
    private String orderType;
    private Integer quantity;
    private Long price;
    private String status;
    private String kisOrderNumber;
}
