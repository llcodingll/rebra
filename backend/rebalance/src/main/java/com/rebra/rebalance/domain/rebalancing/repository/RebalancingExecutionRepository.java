package com.rebra.rebalance.domain.rebalancing.repository;

import com.rebra.rebalance.domain.rebalancing.model.RebalancingExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RebalancingExecutionRepository extends JpaRepository<RebalancingExecution, Long> {
    Optional<RebalancingExecution> findByJobId(Long jobId);
}
