package com.rebra.rebalance.infrastructure.kis;

import com.rebra.rebalance.application.rebalancing.dto.RebalancingOrderCommand;
import com.rebra.rebalance.domain.rebalancing.model.AccountType;
import com.rebra.rebalance.domain.rebalancing.model.OrderType;
import com.rebra.rebalance.exception.KisApiException;
import com.youhogeon.finance.kis_api.KisClient;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceApi;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireDailyCcldApi;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireDailyCcldResult;
import com.youhogeon.finance.kis_api.api.rest.trading.OrderCashApi;
import com.youhogeon.finance.kis_api.api.rest.trading.OrderCashResult;
import com.youhogeon.finance.kis_api.config.Configuration;
import com.youhogeon.finance.kis_api.config.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Slf4j
@Component
public class KisApiAdapter {

    public record PlaceOrderResult(String orderNumber, String orderDate) {}

    private static final String MOCK_BUY_TR_ID      = "VTTC0012U";
    private static final String MOCK_SELL_TR_ID     = "VTTC0011U";
    private static final String REAL_BUY_TR_ID      = "TTTC0012U";
    private static final String REAL_SELL_TR_ID     = "TTTC0011U";
    private static final String MOCK_BALANCE_TR_ID  = "VTTC8434R";
    private static final String REAL_CCLD_TR_ID     = "TTTC0081R";
    private static final String MOCK_CCLD_TR_ID     = "VTTT0081R";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public KisClient buildClient(RebalancingOrderCommand cmd) {
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

        config.addCredentials(credName(cmd), credentials);
        return new KisClient(config);
    }

    private String credName(RebalancingOrderCommand cmd) {
        return "rebalance-" + cmd.getPortfolioId();
    }

    public InquireBalanceResult getBalance(RebalancingOrderCommand cmd, KisClient client) {
        try {
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

    public PlaceOrderResult placeOrder(RebalancingOrderCommand cmd, KisClient client,
                                       String stockCode, String stockName,
                                       OrderType orderType, int quantity) {
        try {
            OrderCashApi api = new OrderCashApi();
            boolean isMock = cmd.getAccountType() == AccountType.MOCK;
            api.setTrId(orderType == OrderType.BUY
                    ? (isMock ? MOCK_BUY_TR_ID : REAL_BUY_TR_ID)
                    : (isMock ? MOCK_SELL_TR_ID : REAL_SELL_TR_ID));
            api.setPdno(stockCode);
            api.setOrdDvsn("01");               // 시장가
            api.setOrdQty(String.valueOf(quantity));
            api.setOrdUnpr("0");
            api.setExcgIdDvsnCd("KRX");

            OrderCashResult result = client.execute(api, credName(cmd));
            if (result.getOutput() == null) {
                throw new RuntimeException("KIS 주문 응답 없음");
            }

            String orderNumber = result.getOutput().getOdno();
            String orderDate   = LocalDate.now().format(DATE_FMT);
            log.info("KIS 주문 완료 {} {} {}주 orderNo={}", orderType, stockCode, quantity, orderNumber);
            return new PlaceOrderResult(orderNumber, orderDate);
        } catch (Exception e) {
            throw new KisApiException(stockCode, orderType, "주문 실패: " + e.getMessage());
        }
    }

    public boolean isOrderExecuted(RebalancingOrderCommand cmd, KisClient client,
                                   String kisOrderNumber, String kisOrderDate) {
        if (kisOrderNumber == null || kisOrderNumber.isBlank()) {
            return false;
        }
        try {
            String today     = LocalDate.now().format(DATE_FMT);
            String orderDate = (kisOrderDate != null) ? kisOrderDate : today;

            InquireDailyCcldApi api = new InquireDailyCcldApi(orderDate, today, "01");
            api.setTrId(cmd.getAccountType() == AccountType.MOCK ? MOCK_CCLD_TR_ID : REAL_CCLD_TR_ID);
            api.setOdno(kisOrderNumber);

            InquireDailyCcldResult result = client.execute(api, credName(cmd));
            if (result.getOutput1() == null || result.getOutput1().length == 0) {
                return false;
            }
            return Arrays.stream(result.getOutput1())
                    .anyMatch(o -> Integer.parseInt(o.getTotCcldQty()) > 0);
        } catch (Exception e) {
            throw new KisApiException(null, null, "체결 조회 실패: " + e.getMessage());
        }
    }
}
