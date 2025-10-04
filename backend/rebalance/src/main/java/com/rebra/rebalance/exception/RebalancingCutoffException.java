package com.rebra.rebalance.exception;

public class RebalancingCutoffException extends RebalancingException {

    public RebalancingCutoffException(Long jobId) {
        super(ErrorCode.CUTOFF_TIME_EXCEEDED, "jobId=" + jobId);
    }
}
