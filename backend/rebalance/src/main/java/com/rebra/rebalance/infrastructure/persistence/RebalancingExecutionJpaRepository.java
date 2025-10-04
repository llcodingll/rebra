package com.rebra.rebalance.infrastructure.persistence;

import com.rebra.rebalance.domain.rebalancing.model.RebalancingExecution;
import com.rebra.rebalance.domain.rebalancing.repository.RebalancingExecutionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RebalancingExecutionJpaRepository
        extends JpaRepository<RebalancingExecution, Long>, RebalancingExecutionRepository {

    Optional<RebalancingExecution> findByJobId(Long jobId);
}
