package com.rebra.rebalance.application.rebalancing;

import com.rebra.rebalance.application.rebalancing.dto.RebalancingOrderCommand;
import com.rebra.rebalance.application.rebalancing.dto.RebalancingResultEvent;
import com.rebra.rebalance.domain.rebalancing.model.OrderRecord;
import com.rebra.rebalance.domain.rebalancing.model.RebalancingExecution;
import com.rebra.rebalance.domain.rebalancing.repository.OrderRecordRepository;
import com.rebra.rebalance.domain.rebalancing.repository.RebalancingExecutionRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RebalancingApplicationService {

    private final RebalancingExecutionRepository executionRepository;
    private final OrderRecordRepository orderRecordRepository;
    private final RebalancingDomainService domainService;
    private final KisApiAdapter kisApiAdapter;
    private final RebalancingResultProducer resultProducer;

    private static final LocalTime CUTOFF_TIME = LocalTime.of(15, 30);

    @Transactional
    public void execute(RebalancingOrderCommand command) {

        // STEP 1: 15:30 cutoff 체크
        if (LocalTime.now().isAfter(CUTOFF_TIME)) {
            log.warn("15:30 cutoff 초과 jobId={}", command.getJobId());
            resultProducer.send(RebalancingResultEvent.failed(
                    command.getJobId(), command.getPortfolioId(), "15:30 cutoff 초과"));
            throw new RebalancingCutoffException(command.getJobId());
        }

        // STEP 2: 멱등성 체크
        RebalancingExecution execution = executionRepository
                .findByJobId(command.getJobId())
                .orElseGet(() -> executionRepository.save(
                        RebalancingExecution.create(command.getJobId(), command.getPortfolioId())));

        if (execution.isCompleted()) {
            log.info("중복 consume 스킵 jobId={}", command.getJobId());
            throw new IdempotencyViolationException(command.getJobId());
        }
        if (execution.isFailed()) {
            log.warn("이미 FAILED 처리된 job jobId={}", command.getJobId());
            throw new IdempotencyViolationException(command.getJobId());
        }

        // STEP 3: KIS API 잔고 조회 (트랜잭션과 무관한 외부 호출)
        InquireBalanceResult balance = kisApiAdapter.getBalance(command);

        // STEP 4: PROCESSING 상태면 미체결 order_record 복구
        List<OrderRecord> pendingOrders = execution.getPendingOrders();
        if (execution.isProcessing() && !pendingOrders.isEmpty()) {
            recoverPendingOrders(command, pendingOrders);
        }

        // STEP 5: 주문 계산
        List<OrderRecord> completedOrders = execution.getCompletedOrders();
        List<RebalancingDomainService.OrderPlan> plans;

        if (execution.isProcessing()) {
            plans = domainService.recalculateRemainingOrders(
                    balance, command.getTargets(), completedOrders, pendingOrders);
        } else {
            plans = domainService.calculateOrders(
                    balance, command.getTargets(), command.getStrategy());
        }

        if (plans.isEmpty()) {
            log.info("리밸런싱 불필요 jobId={}", command.getJobId());
            complete(execution, command, List.of());
            return;
        }

        // STEP 6: PENDING → PROCESSING (매도 실행 직전에 상태 전환)
        execution.startProcessing();
        executionRepository.save(execution);

        // STEP 7: 개별 주문 실행
        List<OrderRecord> tradeResults = executeOrders(command, execution, plans);

        // STEP 8: 완료 처리
        complete(execution, command, tradeResults);
    }

    @Transactional
    protected void recoverPendingOrders(RebalancingOrderCommand command,
                                        List<OrderRecord> pendingOrders) {
        for (OrderRecord pending : pendingOrders) {
            try {
                boolean executed = kisApiAdapter.isOrderExecuted(
                        command, pending.getKisOrderNumber());
                if (executed) {
                    pending.complete(pending.getKisOrderNumber());
                } else {
                    pending.fail("재처리 시 미체결 확인");
                }
                orderRecordRepository.save(pending);
            } catch (Exception e) {
                log.error("복구 조회 실패 orderId={}", pending.getId(), e);
                throw new RebalancingRecoveryException(command.getJobId(), e.getMessage());
            }
        }
    }

    protected List<OrderRecord> executeOrders(RebalancingOrderCommand command,
                                              RebalancingExecution execution,
                                              List<RebalancingDomainService.OrderPlan> plans) {
        List<OrderRecord> results = new ArrayList<>();

        for (RebalancingDomainService.OrderPlan plan : plans) {
            OrderRecord record = saveOrderPending(execution, plan);

            try {
                String kisOrderNumber = kisApiAdapter.placeOrder(
                        command,
                        plan.stockCode(), plan.stockName(),
                        plan.orderType(), plan.quantity());
                completeOrder(record, kisOrderNumber);
                results.add(record);
            } catch (KisApiException e) {
                log.error("주문 실패 {} {} {}주", plan.orderType(), plan.stockCode(), plan.quantity(), e);
                failOrder(record, e.getMessage());
                results.add(record);
            }
        }
        return results;
    }

    @Transactional
    protected OrderRecord saveOrderPending(RebalancingExecution execution,
                                           RebalancingDomainService.OrderPlan plan) {
        OrderRecord record = OrderRecord.pending(
                execution,
                plan.stockCode(), plan.stockName(),
                plan.orderType(), plan.quantity(), plan.price());
        return orderRecordRepository.save(record);
    }

    @Transactional
    protected void completeOrder(OrderRecord record, String kisOrderNumber) {
        record.complete(kisOrderNumber);
        orderRecordRepository.save(record);
    }

    @Transactional
    protected void failOrder(OrderRecord record, String reason) {
        record.fail(reason);
        orderRecordRepository.save(record);
    }

    @Transactional
    protected void complete(RebalancingExecution execution,
                            RebalancingOrderCommand command,
                            List<OrderRecord> tradeResults) {
        execution.complete();
        executionRepository.save(execution);

        RebalancingResultEvent result = RebalancingResultEvent.completed(
                command.getJobId(), command.getPortfolioId(), tradeResults);
        resultProducer.sendSync(result);
    }
}
