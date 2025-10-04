package com.rebra.rebalance.exception;

import com.rebra.rebalance.domain.rebalancing.model.OrderType;
import lombok.Getter;

@Getter
public class KisApiException extends RebalancingException {

    private final String stockCode;
    private final OrderType orderType;

    public KisApiException(String stockCode, OrderType orderType, String detail) {
        super(ErrorCode.KIS_API_ERROR, detail);
        this.stockCode = stockCode;
        this.orderType = orderType;
    }
}
