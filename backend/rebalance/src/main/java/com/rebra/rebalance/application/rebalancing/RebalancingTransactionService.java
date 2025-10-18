package com.rebra.rebalance.application.rebalancing;

import com.rebra.rebalance.application.rebalancing.dto.RebalancingOrderCommand;
import com.rebra.rebalance.application.rebalancing.dto.RebalancingResultEvent;
import com.rebra.rebalance.domain.rebalancing.model.OrderRecord;
import com.rebra.rebalance.domain.rebalancing.model.RebalancingExecution;
import com.rebra.rebalance.domain.rebalancing.repository.OrderRecordRepository;
import com.rebra.rebalance.domain.rebalancing.repository.RebalancingExecutionRepository;
import com.rebra.rebalance.domain.rebalancing.model.OrderPlan;
import com.rebra.rebalance.infrastructure.redis.RebalancingResultProducer;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RebalancingTransactionService {

    private final RebalancingExecutionRepository executionRepository;
    private final OrderRecordRepository orderRecordRepository;
    private final RebalancingResultProducer resultProducer;

    @Transactional
    public RebalancingExecution findOrCreate(Long jobId, Long portfolioId) {
        RebalancingExecution execution = executionRepository.findByJobId(jobId)
                .orElseGet(() -> executionRepository.save(
                        RebalancingExecution.create(jobId, portfolioId)));
        Hibernate.initialize(execution.getOrderRecords());
        return execution;
    }

    @Transactional
    public void markProcessing(RebalancingExecution execution) {
        execution.startProcessing();
        executionRepository.save(execution);
    }

    @Transactional
    public void recoverOrder(OrderRecord pending, boolean executed) {
        if (executed) {
            pending.complete(pending.getKisOrderNumber(), pending.getKisOrderDate());
        } else {
            pending.fail("재처리 시 미체결 확인");
        }
        orderRecordRepository.save(pending);
    }

    @Transactional
    public OrderRecord saveOrderPending(RebalancingExecution execution,
                                        OrderPlan plan) {
        return orderRecordRepository.save(OrderRecord.pending(
                execution, plan.stockCode(), plan.stockName(),
                plan.orderType(), plan.quantity(), plan.price()));
    }

    @Transactional
    public void completeOrder(OrderRecord record, String kisOrderNumber, String kisOrderDate) {
        record.complete(kisOrderNumber, kisOrderDate);
        orderRecordRepository.save(record);
    }

    @Transactional
    public void failOrder(OrderRecord record, String reason) {
        record.fail(reason);
        orderRecordRepository.save(record);
    }

    @Transactional
    public void completeExecution(RebalancingExecution execution,
                                   RebalancingOrderCommand command,
                                   List<OrderRecord> tradeResults) {
        execution.complete();
        executionRepository.save(execution);
        resultProducer.sendSync(RebalancingResultEvent.completed(
                command.getJobId(), command.getPortfolioId(), tradeResults));
    }
}
