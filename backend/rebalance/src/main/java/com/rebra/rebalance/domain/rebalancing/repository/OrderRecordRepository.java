package com.rebra.rebalance.domain.rebalancing.repository;

import com.rebra.rebalance.domain.rebalancing.model.OrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRecordRepository extends JpaRepository<OrderRecord, Long> {
    List<OrderRecord> findByExecutionId(Long executionId);
}
