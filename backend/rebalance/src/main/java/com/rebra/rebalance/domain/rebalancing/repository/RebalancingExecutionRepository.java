package com.rebra.rebalance.domain.rebalancing.repository;

import com.rebra.rebalance.domain.rebalancing.model.RebalancingExecution;

import java.util.Optional;

public interface RebalancingExecutionRepository {
    Optional<RebalancingExecution> findByJobId(Long jobId);
    RebalancingExecution save(RebalancingExecution execution);
}
