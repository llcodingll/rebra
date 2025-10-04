package com.rebra.rebalance.exception;

import lombok.Getter;

@Getter
public class RebalancingException extends RuntimeException {

    private final ErrorCode errorCode;

    public RebalancingException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public RebalancingException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + " - " + detail);
        this.errorCode = errorCode;
    }
}
