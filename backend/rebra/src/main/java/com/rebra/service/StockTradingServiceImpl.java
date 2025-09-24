package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.entity.Account;
import com.rebra.entity.Portfolio;
import com.rebra.entity.RebalancingOrder;
import com.rebra.entity.TradeRecord;
import com.rebra.enums.ExecutionType;
import com.rebra.enums.TransactionStatus;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.RebalancingOrderRepository;
import com.rebra.repository.TradeRecordRepository;
import com.youhogeon.finance.kis_api.api.rest.trading.OrderCashResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTradingServiceImpl implements StockTradingService {

    private final KisApiComponent kisApiComponent;
    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;
    private final RebalancingOrderRepository rebalancingOrderRepository;
    private final TradeRecordRepository tradeRecordRepository;

    @Override
    @Transactional
    public StockTradeResponse buyStock(String stockCode, StockTradeRequest request, Long userId, ExecutionType executionType) {
        log.info("주식 매수 주문 시작 - UserId: {}, StockCode: {}, Quantity: {}, Price: {}",
                userId, request.getStockCode(), request.getQuantity(), request.getPrice());

        return executeOrder(stockCode, request, userId, "buy", executionType);
    }

    @Override
    @Transactional
    public StockTradeResponse sellStock(String stockCode, StockTradeRequest request, Long userId, ExecutionType executionType) {
        log.info("주식 매도 주문 시작 - UserId: {}, StockCode: {}, Quantity: {}, Price: {}",
                userId, request.getStockCode(), request.getQuantity(), request.getPrice());

        return executeOrder(stockCode, request, userId, "sell", executionType);
    }

    // StockTradeRequest만 받는 오버로드 메서드들
    @Override
    @Transactional
    public StockTradeResponse buyStock(StockTradeRequest request, Long userId, ExecutionType executionType) {
        log.info("주식 매수 주문 시작 - UserId: {}, StockCode: {}, Quantity: {}, Price: {}",
                userId, request.getStockCode(), request.getQuantity(), request.getPrice());

        return executeOrder(request.getStockCode(), request, userId, "buy", executionType);
    }

    @Override
    @Transactional
    public StockTradeResponse sellStock(StockTradeRequest request, Long userId, ExecutionType executionType) {
        log.info("주식 매도 주문 시작 - UserId: {}, StockCode: {}, Quantity: {}, Price: {}",
                userId, request.getStockCode(), request.getQuantity(), request.getPrice());

        return executeOrder(request.getStockCode(), request, userId, "sell", executionType);
    }

    private StockTradeResponse executeOrder(String stockCode, StockTradeRequest request, Long userId,
                                            String orderDirection, ExecutionType executionType) {
        try {
            // 1. 계좌 정보 조회 및 검증
            Account account = accountRepository.findByIdAndUserId(request.getAccountId(), userId)
                    .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

            // 2. 포트폴리오 조회 (계좌로 포트폴리오 찾기)
            Portfolio portfolio = portfolioRepository.findByAccountId(request.getAccountId())
                    .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND));

            // 3. 포트폴리오에 등록된 주식인지 확인
            boolean isRegisteredStock = portfolio.getPortfolioStocks().stream()
                    .anyMatch(portfolioStock -> portfolioStock.getStockCode().equals(stockCode));

            // 4. KisApiComponent를 통한 주문 API 호출
            OrderCashResult result;
            if (orderDirection.equals("buy")) {
                result = kisApiComponent.executeBuyOrder(
                        account, stockCode, request.getOrderType(), request.getQuantity(), request.getPrice()
                );

                // 5. 매수 거래 기록 생성 (포트폴리오에 등록된 주식이고, 개인 매수일 경우에만)
                if (isRegisteredStock && shouldCreateTradeRecord(executionType)) {
                    createTradeRecords(portfolio, stockCode, request, result, executionType, "BUY");
                } else {
                    if (!isRegisteredStock) {
                        log.info("미등록 주식 매수 - 거래 기록 생성 생략 - UserId: {}, StockCode: {}", userId, stockCode);
                    } else if (!shouldCreateTradeRecord(executionType)) {
                        log.info("ExecutionType 조건 불일치 - 거래 기록 생성 생략 - UserId: {}, StockCode: {}, ExecutionType: {}",
                                userId, stockCode, executionType);
                    }
                }

            } else {
                result = kisApiComponent.executeSellOrder(
                        account, stockCode, request.getOrderType(), request.getQuantity(), request.getPrice()
                );

                // 5. 매도 거래 기록 생성 (포트폴리오에 등록된 주식이고, 개인 매도일 경우에만)
                if (isRegisteredStock && shouldCreateTradeRecord(executionType)) {
                    createTradeRecords(portfolio, stockCode, request, result, executionType, "SELL");
                } else {
                    if (!isRegisteredStock) {
                        log.info("미등록 주식 매도 - 거래 기록 생성 생략 - UserId: {}, StockCode: {}", userId, stockCode);
                    } else if (!shouldCreateTradeRecord(executionType)) {
                        log.info("ExecutionType 조건 불일치 - 거래 기록 생성 생략 - UserId: {}, StockCode: {}, ExecutionType: {}",
                                userId, stockCode, executionType);
                    }
                }
            }

            // 6. 결과 처리
            if (result != null) {
                log.info("주식 {} 주문 완료 - UserId: {}, Result: {}",
                        orderDirection.equals("buy") ? "매수" : "매도", userId, "Success");

                // 기본적으로 성공으로 처리하고, 실제 주문번호 등은 KIS API Response 구조에 맞게 추후 수정
                return StockTradeResponse.success(
                        result.getOutput().getKrxFwdgOrdOrgno(), // 임시 거래소 주문번호
                        result.getOutput().getOdno(),     // 임시 주문번호
                        java.time.LocalTime.now().toString(),      // 현재 시간
                        stockCode,
                        request.getOrderType(),
                        request.getQuantity(),
                        request.getPrice(),
                        orderDirection.equals("buy") ? "02" : "01"
                );
            } else {
                String errorMessage = "주문 처리 중 알 수 없는 오류가 발생했습니다";
                log.error("주식 {} 주문 실패 - UserId: {}, Error: {}",
                        orderDirection.equals("buy") ? "매수" : "매도", userId, errorMessage);

                return StockTradeResponse.error(errorMessage);
            }

        } catch (CustomRuntimeException e) {
            log.error("주식 {} 주문 실패 - UserId: {}, BusinessError: {}",
                    orderDirection.equals("buy") ? "매수" : "매도", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("주식 {} 주문 중 시스템 오류 - UserId: {}",
                    orderDirection.equals("buy") ? "매수" : "매도", userId, e);
            throw new CustomRuntimeException(ExceptionCode.KIS_API_ERROR);
        }
    }

    /**
     * RebalancingOrder 및 TradeRecord 생성
     */
    private void createTradeRecords(Portfolio portfolio, String stockCode, StockTradeRequest request,
                                   OrderCashResult kisResult, ExecutionType executionType, String tradeType) {
        try {
            log.info("거래 기록 생성 시작 - Portfolio ID: {}, StockCode: {}, TradeType: {}",
                    portfolio.getId(), stockCode, tradeType);

            // 거래 금액 계산
            Long totalAmount = Long.valueOf(request.getQuantity()) * request.getPrice();

            System.out.println(request.getQuantity());
            System.out.println(request.getPrice());
            System.out.println(totalAmount);

            // 1. RebalancingOrder 생성
            RebalancingOrder rebalancingOrder = RebalancingOrder.builder()
                    .portfolio(portfolio)
                    .totalBuyAmount(tradeType.equals("BUY") ? totalAmount : 0L)
                    .totalSellAmount(tradeType.equals("SELL") ? totalAmount : 0L)
                    .rebalancingDate(java.time.LocalDateTime.now())
                    .status(TransactionStatus.COMPLETED)
                    .executionType(executionType)
                    .build();

            RebalancingOrder savedRebalancingOrder = rebalancingOrderRepository.save(rebalancingOrder);

            // 2. TradeRecord 생성
            TradeRecord tradeRecord = TradeRecord.builder()
                    .rebalancingOrder(savedRebalancingOrder)
                    .stockCode(stockCode)
                    .stockName(request.getStockName())
                    .tradeType(tradeType)
                    .tradeDate(java.time.LocalDateTime.now())
                    .executedShares(request.getQuantity())
                    .executedPrice(request.getPrice())
                    .totalAmount(totalAmount)
                    .status(TransactionStatus.COMPLETED)
                    .orderNumber(generateOrderNumber(kisResult))
                    .reason("개인 " + (tradeType.equals("BUY") ? "매수" : "매도"))
                    .build();

            tradeRecordRepository.save(tradeRecord);

            log.info("거래 기록 생성 완료 - Portfolio ID: {}, RebalancingOrder ID: {}, TradeRecord ID: {}",
                    portfolio.getId(), savedRebalancingOrder.getId(), tradeRecord.getId());

        } catch (Exception e) {
            log.error("거래 기록 생성 실패 - Portfolio ID: {}, StockCode: {}, TradeType: {}",
                    portfolio.getId(), stockCode, tradeType, e);
            throw new RuntimeException("거래 기록 생성 실패", e);
        }
    }

    /**
     * ExecutionType에 따라 거래 기록 생성 여부 결정
     */
    private boolean shouldCreateTradeRecord(ExecutionType executionType) {
        return executionType == ExecutionType.BUY_PERSONAL ||
               executionType == ExecutionType.SELL_PERSONAL;
    }

    /**
     * 종목명 조회 (임시 구현, 해야할까..?)
     */
    private String getStockName(String stockCode) {
        // TODO: StockService나 별도 API를 통해 종목명 조회
        return stockCode; // 임시로 종목코드 반환
    }

    /**
     * KIS API 응답에서 주문번호 추출
     */
    private String generateOrderNumber(OrderCashResult kisResult) {
        return kisResult.getOutput().getOdno(); // 주문번호
    }

}