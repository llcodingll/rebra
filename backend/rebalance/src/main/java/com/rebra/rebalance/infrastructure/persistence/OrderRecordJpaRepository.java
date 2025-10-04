package com.rebra.rebalance.infrastructure.persistence;

import com.rebra.rebalance.domain.rebalancing.model.OrderRecord;
import com.rebra.rebalance.domain.rebalancing.repository.OrderRecordRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRecordJpaRepository
        extends JpaRepository<OrderRecord, Long>, OrderRecordRepository {

    List<OrderRecord> findByExecutionId(Long executionId);
}
