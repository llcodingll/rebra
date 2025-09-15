package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.dto.request.StockTradeRequest;
import com.rebra.dto.response.StockTradeResponse;
import com.rebra.entity.Account;
import com.rebra.entity.AccountType;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.repository.AccountRepository;
import com.rebra.util.AccountEncryptionUtil;
import com.youhogeon.finance.kis_api.KisClient;
import com.youhogeon.finance.kis_api.api.rest.trading.OrderCashApi;
import com.youhogeon.finance.kis_api.api.rest.trading.OrderCashResult;
import com.youhogeon.finance.kis_api.config.Configuration;
import com.youhogeon.finance.kis_api.config.Credentials;
import java.time.Duration;
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
            Account account = accountRepository.findByIdAndUserIdAndIsDeletedFalse(request.getAccountId(), userId)
                    .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.ACCOUNT_NOT_FOUND));

            // 2. 계좌 인증 정보 복호화
            DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);

            // 3. KIS 클라이언트 초기화
            String credentialsName = generateCredentialsName(userId, account.getId());
            kisApiComponent.ensureUserCredentials(userId, account.getId(), account.getAccountType(), credentials);

            // 4. 주문 API 호출
            OrderCashResult result = callKisOrderApi(stockCode, request, account, credentials, orderDirection,
                    credentialsName);

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

    private OrderCashResult callKisOrderApi(String stockCode, StockTradeRequest request, Account account,
                                            DecryptedAccountCredentials credentials, String orderDirection,
                                            String credentialsName) {
        try {
            // Configuration 설정
            Configuration config = new Configuration();
            if (account.getAccountType() == AccountType.MOCK) {
                config.setHttpHost("https://openapivts.koreainvestment.com:29443");
            } else {
                config.setHttpHost("https://openapi.koreainvestment.com:9443");
            }
            config.setHttpTimeout(Duration.ofSeconds(30));
            config.setHttpTimeoutMaxRetries(3);

            // 계좌번호를 앞 8자리, 뒤 2자리로 분리
            String accountPre = credentials.getAccountNumber().length() >= 8
                    ? credentials.getAccountNumber().substring(0, 8)
                    : credentials.getAccountNumber();
            String accountPost = credentials.getAccountNumber().length() > 8
                    ? credentials.getAccountNumber().substring(8)
                    : "01";

            Credentials kisCredentials = new Credentials(
                    credentials.getAppKey(),
                    credentials.getAppSecret(),
                    accountPre,
                    accountPost
            );

            config.addCredentials(credentialsName, kisCredentials);
            KisClient client = new KisClient(config);

            // 주문 API 실행
            OrderCashApi orderApi = new OrderCashApi();

            // TR ID 설정
            if (account.getAccountType() == AccountType.MOCK) {
                orderApi.setTrId(orderDirection.equals("buy") ? "VTTC0012U" : "VTTC0011U");
            } else {
                orderApi.setTrId(orderDirection.equals("buy") ? "TTTC0012U" : "TTTC0011U");
            }

            // 주문 파라미터 설정
            orderApi.setPdno(stockCode);  // 종목코드
            orderApi.setOrdDvsn(request.getOrderType());  // 주문구분
            orderApi.setOrdQty(String.valueOf(request.getQuantity()));  // 주문수량
            orderApi.setOrdUnpr(request.getPrice() != null ? String.valueOf(request.getPrice()) : "0");  // 주문단가
            orderApi.setExcgIdDvsnCd("KRX");  // 거래소ID구분코드

            // API 실행
            OrderCashResult result = client.execute(orderApi, credentialsName);

            log.debug("KIS 주문 API 호출 완료 - StockCode: {}, OrderDirection: {}, Result: {}",
                    stockCode, orderDirection, result != null ? "Success" : "Failed");

            return result;

        } catch (Exception e) {
            log.error("KIS 주문 API 호출 실패 - StockCode: {}, OrderDirection: {}, Error: {}",
                    stockCode, orderDirection, e.getMessage(), e);
            throw new RuntimeException("KIS API 주문 실행 실패", e);
        }
    }

    private String generateCredentialsName(Long userId, Long accountId) {
        return userId + "_" + accountId;
    }
}