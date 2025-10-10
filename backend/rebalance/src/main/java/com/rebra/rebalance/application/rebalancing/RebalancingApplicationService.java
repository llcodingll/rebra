package com.rebra.rebalance.application.rebalancing;

import com.rebra.rebalance.application.rebalancing.dto.RebalancingOrderCommand;
import com.rebra.rebalance.application.rebalancing.dto.RebalancingResultEvent;
import com.rebra.rebalance.domain.rebalancing.model.OrderRecord;
import com.rebra.rebalance.domain.rebalancing.model.RebalancingExecution;
import com.rebra.rebalance.domain.rebalancing.service.RebalancingDomainService;
import com.rebra.rebalance.exception.IdempotencyViolationException;
import com.rebra.rebalance.exception.KisApiException;
import com.rebra.rebalance.exception.RebalancingCutoffException;
import com.rebra.rebalance.exception.RebalancingRecoveryException;
import com.rebra.rebalance.infrastructure.kafka.RebalancingResultProducer;
import com.rebra.rebalance.infrastructure.kis.KisApiAdapter;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RebalancingApplicationService {

    private final RebalancingTransactionService txService;
    private final RebalancingDomainService domainService;
    private final KisApiAdapter kisApiAdapter;
    private final RebalancingResultProducer resultProducer;

    private static final LocalTime CUTOFF_TIME = LocalTime.of(15, 30);

    public void execute(RebalancingOrderCommand command) {
        checkCutoff(command);
        RebalancingExecution execution = checkIdempotency(command);
        InquireBalanceResult balance = fetchBalance(command);
        recoverIfProcessing(command, execution);
        List<RebalancingDomainService.OrderPlan> plans = calculatePlans(command, execution, balance);
        if (plans.isEmpty()) {
            log.info("리밸런싱 불필요 jobId={}", command.getJobId());
            txService.completeExecution(execution, command, List.of());
            return;
        }
        txService.markProcessing(execution);
        List<OrderRecord> tradeResults = executeOrders(command, execution, plans);
        txService.completeExecution(execution, command, tradeResults);
    }

    private void checkCutoff(RebalancingOrderCommand command) {
        if (LocalTime.now().isAfter(CUTOFF_TIME)) {
            resultProducer.send(RebalancingResultEvent.failed(
                    command.getJobId(), command.getPortfolioId(), "15:30 cutoff 초과"));
            throw new RebalancingCutoffException(command.getJobId());
        }
    }

    private RebalancingExecution checkIdempotency(RebalancingOrderCommand command) {
        RebalancingExecution execution = txService.findOrCreate(
                command.getJobId(), command.getPortfolioId());
        if (execution.isCompleted() || execution.isFailed()) {
            throw new IdempotencyViolationException(command.getJobId());
        }
        return execution;
    }

    private InquireBalanceResult fetchBalance(RebalancingOrderCommand command) {
        return kisApiAdapter.getBalance(command);
    }

    private void recoverIfProcessing(RebalancingOrderCommand command,
                                      RebalancingExecution execution) {
        if (!execution.isProcessing()) return;
        for (OrderRecord pending : execution.getPendingOrders()) {
            try {
                boolean executed = kisApiAdapter.isOrderExecuted(
                        command, pending.getKisOrderNumber());
                txService.recoverOrder(pending, executed);
            } catch (Exception e) {
                log.error("복구 조회 실패 orderId={}", pending.getId(), e);
                throw new RebalancingRecoveryException(command.getJobId(), e.getMessage());
            }
        }
    }

    private List<RebalancingDomainService.OrderPlan> calculatePlans(
            RebalancingOrderCommand command,
            RebalancingExecution execution,
            InquireBalanceResult balance) {
        if (execution.isProcessing()) {
            return domainService.recalculateRemainingOrders(
                    balance, command.getTargets(),
                    execution.getCompletedOrders(), execution.getPendingOrders());
        }
        return domainService.calculateOrders(
                balance, command.getTargets(), command.getStrategy());
    }

    private List<OrderRecord> executeOrders(RebalancingOrderCommand command,
                                             RebalancingExecution execution,
                                             List<RebalancingDomainService.OrderPlan> plans) {
        List<OrderRecord> results = new ArrayList<>();
        for (RebalancingDomainService.OrderPlan plan : plans) {
            OrderRecord record = txService.saveOrderPending(execution, plan);
            try {
                String orderNum = kisApiAdapter.placeOrder(
                        command, plan.stockCode(), plan.stockName(),
                        plan.orderType(), plan.quantity());
                txService.completeOrder(record, orderNum);
            } catch (KisApiException e) {
                log.error("주문 실패 {} {} {}주",
                        plan.orderType(), plan.stockCode(), plan.quantity(), e);
                txService.failOrder(record, e.getMessage());
            }
            results.add(record);
        }
        return results;
    }
}
