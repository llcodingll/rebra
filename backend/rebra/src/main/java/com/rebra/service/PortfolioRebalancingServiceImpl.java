package com.rebra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.dto.rebalancing.RebalancingOrderMessage;
import com.rebra.dto.response.RebalancingExecutionResponse;
import com.rebra.entity.*;
import com.rebra.enums.ExecutionType;
import com.rebra.enums.TransactionStatus;
import com.rebra.repository.OutboxEventRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.RebalancingOrderRepository;
import com.rebra.util.AccountEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioRebalancingServiceImpl implements PortfolioRebalancingService {

    private final PortfolioRepository portfolioRepository;
    private final RebalancingOrderRepository rebalancingOrderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final HolidayService holidayService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RebalancingExecutionResponse executeManualRebalancing(Long userId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdWithPortfolioStocks(portfolioId, userId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다"));

        log.info("수동 리밸런싱 요청 (Outbox 큐잉): 포트폴리오 {}", portfolioId);
        enqueueRebalancing(portfolio, ExecutionType.MANUAL);

        return RebalancingExecutionResponse.queued(portfolioId);
    }

    @Override
    public void processAllActivePortfolios() {
        LocalDate today = LocalDate.now();

        if (!isTradingDay(today)) {
            log.info("오늘({})은 비거래일입니다. 리밸런싱 처리 건너뜀", today);
            return;
        }

        List<Portfolio> pendingPortfolios = portfolioRepository
                .findByAutoRebalancingTrueAndNextRebalanceDateLessThanEqual(today);

        log.info("리밸런싱 대상 포트폴리오: {}개", pendingPortfolios.size());

        for (Portfolio portfolio : pendingPortfolios) {
            try {
                enqueueRebalancingInTx(portfolio, today);
            } catch (Exception e) {
                log.error("포트폴리오 {} 리밸런싱 큐잉 실패", portfolio.getId(), e);
            }
        }
    }

    // 자동 리밸런싱: 별도 TX (포트폴리오별 독립)
    @Transactional
    public void enqueueRebalancingInTx(Portfolio portfolio, LocalDate today) {
        enqueueRebalancing(portfolio, ExecutionType.AUTO);
        portfolio.updateNextRebalanceDate(today.plusDays(1));
        portfolioRepository.save(portfolio);
    }

    /**
     * RebalancingOrder(PENDING) + OutboxEvent(PENDING)를 같은 트랜잭션에서 저장한다.
     * OutboxEventRelay가 5초 후 Kafka에 발행한다.
     */
    private void enqueueRebalancing(Portfolio portfolio, ExecutionType executionType) {
        Account account = portfolio.getAccount();
        Long userId = portfolio.getUser().getId();

        // 자격증명 복호화
        DecryptedAccountCredentials creds =
                AccountEncryptionUtil.decryptAccountCredentials(account, userId);

        // RebalancingOrder INSERT (PENDING)
        RebalancingOrder rebalancingOrder = RebalancingOrder.builder()
                .portfolio(portfolio)
                .totalBuyAmount(0L)
                .totalSellAmount(0L)
                .rebalancingDate(LocalDateTime.now())
                .status(TransactionStatus.EXECUTING)
                .executionType(executionType)
                .totalPortfolioValue(0L)
                .build();
        rebalancingOrder = rebalancingOrderRepository.save(rebalancingOrder);

        // DTO 빌드
        List<RebalancingOrderMessage.StockTargetDto> targets = portfolio.getPortfolioStocks().stream()
                .map(ps -> RebalancingOrderMessage.StockTargetDto.builder()
                        .stockCode(ps.getStockCode())
                        .stockName(ps.getStockCode()) // 종목명은 stockCode로 대체 (잔고 조회 후 업데이트됨)
                        .targetWeight(ps.getTargetWeight() != null ? ps.getTargetWeight().doubleValue() : 0.0)
                        .thresholdPercentage(ps.getThresholdPercentage() != null
                                ? ps.getThresholdPercentage().doubleValue() : 0.05)
                        .build())
                .collect(Collectors.toList());

        RebalancingOrderMessage message = RebalancingOrderMessage.builder()
                .jobId(rebalancingOrder.getId())
                .portfolioId(portfolio.getId())
                .userId(userId)
                .accountId(account.getId())
                .accountNumber(creds.getAccountNumber())
                .appKey(creds.getAppKey())
                .appSecret(creds.getAppSecret())
                .accountType(account.getAccountType().name())
                .executionType(executionType.name())
                .strategy(portfolio.getRebalancingStrategy() != null
                        ? portfolio.getRebalancingStrategy().name() : "THRESHOLD")
                .safeAssetRatio(portfolio.getSafeAssetRatio())
                .targets(targets)
                .triggeredAt(LocalDateTime.now())
                .build();

        // JSON 직렬화
        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("RebalancingOrderMessage 직렬화 실패", e);
        }

        // OutboxEvent INSERT (PENDING)
        OutboxEvent outboxEvent = OutboxEvent.create(
                rebalancingOrder.getId(), portfolio.getId(), payload);
        outboxEventRepository.save(outboxEvent);

        log.info("리밸런싱 큐잉 완료 jobId={} portfolioId={}",
                rebalancingOrder.getId(), portfolio.getId());
    }

    private boolean isTradingDay(LocalDate date) {
        return holidayService.isTradingDay(date);
    }
}
