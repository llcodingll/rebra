package com.rebra.rebalance.domain.rebalancing.service;

import com.rebra.rebalance.domain.rebalancing.model.OrderRecord;
import com.rebra.rebalance.domain.rebalancing.model.OrderStatus;
import com.rebra.rebalance.domain.rebalancing.model.OrderType;
import com.rebra.rebalance.domain.rebalancing.model.RebalancingStrategy;
import com.rebra.rebalance.domain.rebalancing.model.StockTargetDto;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RebalancingDomainService {

    public record OrderPlan(String stockCode, String stockName,
                            OrderType orderType, int quantity, long price) {}

    /**
     * 현재 잔고와 목표 비중으로 주문 목록을 산출한다. (매도 먼저, 매수 나중 정렬)
     */
    public List<OrderPlan> calculateOrders(InquireBalanceResult balance,
                                           List<StockTargetDto> targets,
                                           RebalancingStrategy strategy) {
        if (targets == null || targets.isEmpty()) {
            return List.of();
        }

        // 현재 보유 맵 (종목코드 → 보유정보)
        Map<String, HoldingInfo> holdings = buildHoldingsMap(balance);

        // 관리 종목 코드 집합
        Set<String> managedCodes = targets.stream()
                .map(StockTargetDto::getStockCode)
                .collect(Collectors.toSet());

        // 관리 종목만의 총 평가액 계산
        double totalValue = holdings.entrySet().stream()
                .filter(e -> managedCodes.contains(e.getKey()))
                .mapToDouble(e -> (double) e.getValue().price * e.getValue().quantity)
                .sum();

        if (totalValue <= 0) {
            log.warn("관리 종목 총 평가액이 0 이하 → 주문 산출 불가");
            return List.of();
        }

        // 목표 비중 정규화 (safeAsset 제외된 targets의 합으로 나누기)
        double totalTargetWeight = targets.stream()
                .mapToDouble(StockTargetDto::getTargetWeight)
                .sum();

        if (totalTargetWeight <= 0) {
            return List.of();
        }

        List<OrderPlan> sellPlans = new ArrayList<>();
        List<OrderPlan> buyPlans  = new ArrayList<>();

        for (StockTargetDto target : targets) {
            String stockCode = target.getStockCode();
            double normalizedWeight = target.getTargetWeight() / totalTargetWeight;
            long targetValue = Math.round(totalValue * normalizedWeight);

            HoldingInfo holding = holdings.get(stockCode);
            long currentValue    = holding != null ? (long) holding.price * holding.quantity : 0L;
            long currentPrice    = holding != null ? holding.price : 0L;
            int  currentQuantity = holding != null ? holding.quantity : 0;

            if (currentPrice <= 0) {
                log.warn("종목 {} 현재가 0 → 스킵", stockCode);
                continue;
            }

            // THRESHOLD 전략: 상대 임계값 초과 시에만 주문 생성
            if (strategy == RebalancingStrategy.THRESHOLD) {
                double currentWeight   = currentValue / totalValue;
                double weightDiff      = Math.abs(currentWeight - normalizedWeight);
                double relThreshold    = normalizedWeight * (target.getThresholdPercentage() != null
                                            ? target.getThresholdPercentage() : 0.05);
                if (weightDiff <= relThreshold) {
                    log.debug("종목 {} 임계값 이내 weightDiff={} threshold={}", stockCode, weightDiff, relThreshold);
                    continue;
                }
            }

            long valueDiff = targetValue - currentValue;

            if (valueDiff > 0) {
                int qty = (int) (valueDiff / currentPrice);
                if (qty > 0) {
                    buyPlans.add(new OrderPlan(stockCode,
                            holding != null ? holding.name : stockCode,
                            OrderType.BUY, qty, currentPrice));
                }
            } else if (valueDiff < 0) {
                int qty = (int) (Math.abs(valueDiff) / currentPrice);
                qty = Math.min(qty, currentQuantity);
                if (qty > 0) {
                    sellPlans.add(new OrderPlan(stockCode,
                            holding != null ? holding.name : stockCode,
                            OrderType.SELL, qty, currentPrice));
                }
            }
        }

        // 매도 먼저, 매수 나중
        List<OrderPlan> result = new ArrayList<>(sellPlans);
        result.addAll(buyPlans);
        return result;
    }

    /**
     * PROCESSING 복구 시 — 이미 완료된 주문을 제외하고 잔여 주문을 재계산한다.
     */
    public List<OrderPlan> recalculateRemainingOrders(InquireBalanceResult balance,
                                                      List<StockTargetDto> targets,
                                                      List<OrderRecord> completedOrders,
                                                      List<OrderRecord> pendingOrders) {
        // 이미 체결된 종목 코드 집합 (COMPLETED 주문)
        Set<String> completedSellCodes = completedOrders.stream()
                .filter(o -> o.getOrderType() == OrderType.SELL
                          && o.getStatus() == OrderStatus.COMPLETED)
                .map(OrderRecord::getStockCode)
                .collect(Collectors.toSet());

        Set<String> completedBuyCodes = completedOrders.stream()
                .filter(o -> o.getOrderType() == OrderType.BUY
                          && o.getStatus() == OrderStatus.COMPLETED)
                .map(OrderRecord::getStockCode)
                .collect(Collectors.toSet());

        // 전체 계산 후 이미 완료된 종목 제외
        List<OrderPlan> allPlans = calculateOrders(balance, targets, RebalancingStrategy.PERIODIC);

        return allPlans.stream()
                .filter(plan -> {
                    if (plan.orderType() == OrderType.SELL) {
                        return !completedSellCodes.contains(plan.stockCode());
                    } else {
                        return !completedBuyCodes.contains(plan.stockCode());
                    }
                })
                .toList();
    }

    private Map<String, HoldingInfo> buildHoldingsMap(InquireBalanceResult balance) {
        Map<String, HoldingInfo> map = new HashMap<>();
        if (balance == null || balance.getOutput1() == null) {
            return map;
        }
        for (InquireBalanceResult.Output1 item : balance.getOutput1()) {
            int qty = Integer.parseInt(item.getHldgQty());
            if (qty <= 0) continue;
            long price = Long.parseLong(item.getPrpr());
            map.put(item.getPdno(), new HoldingInfo(item.getPdno(), item.getPrdtName(), qty, price));
        }
        return map;
    }

    private record HoldingInfo(String code, String name, int quantity, long price) {}
}
