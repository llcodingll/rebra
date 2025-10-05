package com.rebra.rebalance.infrastructure.kis;

import com.rebra.rebalance.application.rebalancing.dto.RebalancingOrderCommand;
import com.rebra.rebalance.domain.rebalancing.model.AccountType;
import com.rebra.rebalance.domain.rebalancing.model.OrderType;
import com.rebra.rebalance.exception.KisApiException;
import com.youhogeon.finance.kis_api.KisClient;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceApi;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import com.youhogeon.finance.kis_api.api.rest.trading.OrderCashApi;
import com.youhogeon.finance.kis_api.api.rest.trading.OrderCashResult;
import com.youhogeon.finance.kis_api.config.Configuration;
import com.youhogeon.finance.kis_api.config.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class KisApiAdapter {

    private static final String MOCK_BUY_TR_ID  = "VTTC0012U";
    private static final String MOCK_SELL_TR_ID = "VTTC0011U";
    private static final String REAL_BUY_TR_ID  = "TTTC0012U";
    private static final String REAL_SELL_TR_ID = "TTTC0011U";
    private static final String MOCK_BALANCE_TR_ID = "VTTC8434R";

    // 호출마다 신규 KisClient 생성 (메인 서버처럼 전역 Config 관리 불필요 — 리밸런싱은 1회성 실행)
    private KisClient buildClient(RebalancingOrderCommand cmd) {
        Configuration config = new Configuration();

        if (cmd.getAccountType() == AccountType.MOCK) {
            config.setHttpHost("https://openapivts.koreainvestment.com:29443");
        } else {
            config.setHttpHost("https://openapi.koreainvestment.com:9443");
        }
        config.setHttpTimeout(Duration.ofSeconds(30));
        config.setHttpTimeoutMaxRetries(3);

        String accountPre  = cmd.getAccountNumber().substring(0, 8);
        String accountPost = cmd.getAccountNumber().substring(8);
        Credentials credentials = new Credentials(
                cmd.getAppKey(), cmd.getAppSecret(), accountPre, accountPost);

        String credName = "rebalance-" + cmd.getPortfolioId();
        config.addCredentials(credName, credentials);

        return new KisClient(config);
    }

    private String credName(RebalancingOrderCommand cmd) {
        return "rebalance-" + cmd.getPortfolioId();
    }

    public InquireBalanceResult getBalance(RebalancingOrderCommand cmd) {
        try {
            KisClient client = buildClient(cmd);
            InquireBalanceApi api = new InquireBalanceApi();

            if (cmd.getAccountType() == AccountType.MOCK) {
                api.setTrId(MOCK_BALANCE_TR_ID);
            }

            InquireBalanceResult result = client.execute(api, credName(cmd));

            if (!"0".equals(result.getRtCd())) {
                throw new RuntimeException("KIS 잔고 조회 실패: rtCd=" + result.getRtCd());
            }
            return result;
        } catch (Exception e) {
            throw new KisApiException(null, null, "잔고 조회 실패: " + e.getMessage());
        }
    }

    public String placeOrder(RebalancingOrderCommand cmd,
                             String stockCode, String stockName,
                             OrderType orderType, int quantity) {
        try {
            KisClient client = buildClient(cmd);
            OrderCashApi api = new OrderCashApi();

            boolean isMock = cmd.getAccountType() == AccountType.MOCK;
            if (orderType == OrderType.BUY) {
                api.setTrId(isMock ? MOCK_BUY_TR_ID : REAL_BUY_TR_ID);
            } else {
                api.setTrId(isMock ? MOCK_SELL_TR_ID : REAL_SELL_TR_ID);
            }

            api.setPdno(stockCode);
            api.setOrdDvsn("01");               // 시장가
            api.setOrdQty(String.valueOf(quantity));
            api.setOrdUnpr("0");                // 시장가는 0
            api.setExcgIdDvsnCd("KRX");

            OrderCashResult result = client.execute(api, credName(cmd));

            if (result.getOutput() == null) {
                throw new RuntimeException("KIS 주문 응답 없음");
            }

            String orderNumber = result.getOutput().getOdno();
            log.info("KIS 주문 완료 {} {} {}주 orderNo={}", orderType, stockCode, quantity, orderNumber);
            return orderNumber;

        } catch (Exception e) {
            throw new KisApiException(stockCode, orderType, "주문 실패: " + e.getMessage());
        }
    }

    public boolean isOrderExecuted(RebalancingOrderCommand cmd, String kisOrderNumber) {
        if (kisOrderNumber == null || kisOrderNumber.isBlank()) {
            return false;
        }
        try {
            // 일별 체결 조회 API로 kisOrderNumber 확인
            // KIS InquireDailyccldApi 사용
            // 여기서는 보수적으로 false 반환 → 재주문 시도
            log.warn("체결 조회 미구현 - kisOrderNumber={} → 미체결로 처리", kisOrderNumber);
            return false;
        } catch (Exception e) {
            throw new KisApiException(null, null, "체결 조회 실패: " + e.getMessage());
        }
    }
}
