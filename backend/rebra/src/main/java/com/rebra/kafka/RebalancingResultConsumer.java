package com.rebra.kafka;

import com.rebra.entity.RebalancingOrder;
import com.rebra.entity.TradeRecord;
import com.rebra.enums.TransactionStatus;
import com.rebra.kafka.dto.RebalancingResultMessage;
import com.rebra.repository.RebalancingOrderRepository;
import com.rebra.repository.TradeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RebalancingResultConsumer {

    private final RebalancingOrderRepository rebalancingOrderRepository;
    private final TradeRecordRepository tradeRecordRepository;

    @KafkaListener(
            topics = "rebalancing-results",
            groupId = "rebra-main-server",
            containerFactory = "rebalancingResultsListenerContainerFactory"
    )
    @Transactional
    public void handleResult(@Payload Map<String, Object> rawMessage, Acknowledgment ack) {
        Long jobId = null;
        try {
            jobId = toLong(rawMessage.get("jobId"));
            Long portfolioId = toLong(rawMessage.get("portfolioId"));
            String status = (String) rawMessage.get("status");

            log.info("리밸런싱 결과 수신 jobId={} status={}", jobId, status);

            RebalancingOrder order = rebalancingOrderRepository
                    .findById(jobId)
                    .orElseThrow(() -> new IllegalStateException(
                            "RebalancingOrder 없음 jobId=" + jobId));

            if ("COMPLETED".equals(status)) {
                Long totalBuyAmount  = toLong(rawMessage.get("totalBuyAmount"));
                Long totalSellAmount = toLong(rawMessage.get("totalSellAmount"));
                order.updateStatus(TransactionStatus.COMPLETED);
                order.updateAmounts(totalBuyAmount, totalSellAmount);

                List<TradeRecord> records = buildTradeRecords(order, rawMessage);
                if (!records.isEmpty()) {
                    tradeRecordRepository.saveAll(records);
                }
            } else {
                order.updateStatus(TransactionStatus.FAILED);
            }

            rebalancingOrderRepository.save(order);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("결과 처리 실패 jobId={}", jobId, e);
            ack.acknowledge(); // 재처리 방지
        }
    }

    @SuppressWarnings("unchecked")
    private List<TradeRecord> buildTradeRecords(RebalancingOrder order, Map<String, Object> raw) {
        List<TradeRecord> result = new ArrayList<>();
        Object tradesObj = raw.get("trades");
        if (!(tradesObj instanceof List)) return result;

        for (Object tradeObj : (List<?>) tradesObj) {
            if (!(tradeObj instanceof Map)) continue;
            Map<String, Object> t = (Map<String, Object>) tradeObj;

            if (!"COMPLETED".equals(t.get("status"))) continue;

            result.add(TradeRecord.builder()
                    .rebalancingOrder(order)
                    .stockCode((String) t.get("stockCode"))
                    .stockName(t.getOrDefault("stockName", t.get("stockCode")).toString())
                    .tradeType((String) t.get("orderType"))
                    .tradeDate(LocalDateTime.now())
                    .executedShares(toInt(t.get("quantity")))
                    .executedPrice(toLong(t.get("price")))
                    .totalAmount(toLong(t.get("price")) * toInt(t.get("quantity")))
                    .status(TransactionStatus.COMPLETED)
                    .orderNumber(t.getOrDefault("kisOrderNumber", "").toString())
                    .reason("리밸런싱")
                    .build());
        }
        return result;
    }

    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }
}
