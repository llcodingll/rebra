package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.entity.Account;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.repository.AccountRepository;
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

    @Override
    @Transactional
    public StockTradeResponse buyStock(String stockCode, StockTradeRequest request, Long userId) {
        log.info("주식 매수 주문 시작 - UserId: {}, StockCode: {}, Quantity: {}, Price: {}",
                userId, stockCode, request.getQuantity(), request.getPrice());

        return executeOrder(stockCode, request, userId, "buy");
    }

    @Override
    @Transactional
    public StockTradeResponse sellStock(String stockCode, StockTradeRequest request, Long userId) {
        log.info("주식 매도 주문 시작 - UserId: {}, StockCode: {}, Quantity: {}, Price: {}",
                userId, stockCode, request.getQuantity(), request.getPrice());

        return executeOrder(stockCode, request, userId, "sell");
    }

    private StockTradeResponse executeOrder(String stockCode, StockTradeRequest request, Long userId,
                                            String orderDirection) {
        try {
            // 1. 계좌 정보 조회 및 검증
            Account account = accountRepository.findByIdAndUserId(request.getAccountId(), userId)
                    .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

            // 2. KisApiComponent를 통한 주문 API 호출
            OrderCashResult result;
            if (orderDirection.equals("buy")) {
                result = kisApiComponent.executeBuyOrder(
                        account, stockCode, request.getOrderType(), request.getQuantity(), request.getPrice()
                );
            } else {
                result = kisApiComponent.executeSellOrder(
                        account, stockCode, request.getOrderType(), request.getQuantity(), request.getPrice()
                );
            }

            // 5. 결과 처리
            if (result != null) {
                log.info("주식 {} 주문 완료 - UserId: {}, Result: {}",
                        orderDirection.equals("buy") ? "매수" : "매도", userId, "Success");

                // 기본적으로 성공으로 처리하고, 실제 주문번호 등은 KIS API Response 구조에 맞게 추후 수정
                return StockTradeResponse.success(
                        "KRX_ORDER_" + System.currentTimeMillis(), // 임시 거래소 주문번호
                        "ORDER_" + System.currentTimeMillis(),     // 임시 주문번호
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

}