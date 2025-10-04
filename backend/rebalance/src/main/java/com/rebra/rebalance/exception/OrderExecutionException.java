package com.rebra.rebalance.exception;

import com.rebra.rebalance.domain.rebalancing.model.OrderType;
import lombok.Getter;

@Getter
public class OrderExecutionException extends RebalancingException {

    private final String stockCode;
    private final OrderType orderType;
    private final int quantity;

    public OrderExecutionException(String stockCode, OrderType orderType, int quantity, String reason) {
        super(ErrorCode.ORDER_EXECUTION_FAILED, reason);
        this.stockCode = stockCode;
        this.orderType = orderType;
        this.quantity = quantity;
    }
}
