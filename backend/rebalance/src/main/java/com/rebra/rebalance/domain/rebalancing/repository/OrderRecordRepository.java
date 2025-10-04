package com.rebra.rebalance.domain.rebalancing.repository;

import com.rebra.rebalance.domain.rebalancing.model.OrderRecord;

import java.util.List;

public interface OrderRecordRepository {
    OrderRecord save(OrderRecord record);
    List<OrderRecord> findByExecutionId(Long executionId);
}
