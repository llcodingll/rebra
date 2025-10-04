package com.rebra.rebalance.exception;

public class RebalancingRecoveryException extends RebalancingException {

    public RebalancingRecoveryException(Long jobId, String detail) {
        super(ErrorCode.RECOVERY_FAILED, "jobId=" + jobId + " - " + detail);
    }
}
