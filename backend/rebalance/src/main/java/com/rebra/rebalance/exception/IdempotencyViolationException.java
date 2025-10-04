package com.rebra.rebalance.exception;

public class IdempotencyViolationException extends RebalancingException {

    public IdempotencyViolationException(Long jobId) {
        super(ErrorCode.IDEMPOTENCY_VIOLATION, "jobId=" + jobId);
    }
}
