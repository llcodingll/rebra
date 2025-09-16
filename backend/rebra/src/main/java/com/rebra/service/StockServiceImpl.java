package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockSearchResponse;
import com.rebra.entity.Account;
import com.rebra.entity.Stock;
import com.rebra.exception.stock.StockException;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.StockRepository;
import com.rebra.util.AccountEncryptionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final KisApiComponent kisApiComponent;
    // Redis 캐싱 제거 - 프론트엔드에서 실시간 데이터 관리
    // 실시간 데이터는 WebSocket을 통해 직접 클라이언트로 전달

    @Override
    public StockSearchResponse findByStockCode(String stockCode) {
        return stockRepository.findByStockCodeAndIsActiveTrue(stockCode)
                .map(StockSearchResponse::from)
                .orElseThrow(StockException::stockCodeNotFound);
    }

    @Override
    public StockSearchResponse findByStockName(String stockName) {
        return stockRepository.findByStockNameAndIsActiveTrue(stockName)
                .map(StockSearchResponse::from)
                .orElseThrow(StockException::stockNameNotFound);
    }

    @Override
    public PageResponse<StockSearchResponse> searchStocks(String stockName, Pageable pageable) {
        Page<Stock> stockPage = stockRepository.findByStockNameContainingIgnoreCaseAndIsActiveTrue(stockName, pageable);
        Page<StockSearchResponse> dtoPage = stockPage.map(StockSearchResponse::from);
        return PageResponse.from(dtoPage);
    }

    @Override
    public StockChartResponse getStockChartData(String stockCode, String startDate, String endDate, String periodType,
                                                Long userId) {
        try {
            log.info("차트 데이터 조회 시작 - UserId: {}, StockCode: {}, Period: {}", userId, stockCode, periodType);

            Account account = accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true)
                    .orElseThrow(() -> new RuntimeException("활성화된 계좌를 찾을 수 없습니다."));

            DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
            kisApiComponent.ensureUserCredentials(userId, account.getId(), account.getAccountType(), credentials);

            Map<String, Object> kisResult = kisApiComponent.getStockChartData(
                    userId, account.getId(), account.getAccountType(),
                    stockCode, startDate, endDate, periodType
            );

            return buildStockChartResponse(stockCode, kisResult, startDate, endDate, periodType);

        } catch (Exception e) {
            log.error("차트 데이터 조회 실패 - UserId: {}, StockCode: {}, Period: {}, ErrorType: {}, Message: {}",
                    userId, stockCode, periodType, e.getClass().getSimpleName(), e.getMessage(), e);

            // 구체적인 에러 메시지 제공
            String detailedMessage = "차트 데이터 조회에 실패했습니다";
            if (e.getMessage() != null) {
                if (e.getMessage().contains("활성화된 계좌")) {
                    detailedMessage = "활성화된 계좌를 찾을 수 없습니다. 계좌를 연결해주세요.";
                } else if (e.getMessage().contains("복호화")) {
                    detailedMessage = "계좌 정보 복호화에 실패했습니다.";
                } else if (e.getMessage().contains("KIS")) {
                    detailedMessage = "KIS API 연동에 실패했습니다. 잠시 후 다시 시도해주세요.";
                } else {
                    // KIS API에서 발생한 구체적인 에러 메시지를 그대로 전달
                    detailedMessage = "차트 데이터 조회 실패: " + e.getMessage();
                }
            }

            throw new RuntimeException(detailedMessage, e);
        }
    }

    private StockChartResponse buildStockChartResponse(String stockCode, Map<String, Object> kisResult,
                                                       String startDate, String endDate, String periodType) {
        List<StockChartResponse.ChartDataPoint> chartData = new ArrayList<>();
        StockChartResponse.StockSummary summary = null;

        if (kisResult != null && kisResult.containsKey("output2")) {
            List<Map<String, Object>> output2 = (List<Map<String, Object>>) kisResult.get("output2");

            for (Map<String, Object> item : output2) {
                chartData.add(StockChartResponse.ChartDataPoint.builder()
                        .tradingDate((String) item.get("stck_bsop_date"))
                        .openPrice((String) item.get("stck_oprc"))
                        .highPrice((String) item.get("stck_hgpr"))
                        .lowPrice((String) item.get("stck_lwpr"))
                        .closePrice((String) item.get("stck_clpr"))
                        .volume((String) item.get("acml_vol"))
                        .tradingValue((String) item.get("acml_tr_pbmn"))
                        .priceChange((String) item.get("prdy_vrss"))
                        .changeSign((String) item.get("prdy_vrss_sign"))
                        .changeRate((String) item.get("prdy_ctrt"))
                        .build());
            }

            if (kisResult.containsKey("output1")) {
                Map<String, Object> output1 = (Map<String, Object>) kisResult.get("output1");
                summary = StockChartResponse.StockSummary.builder()
                        .currentPrice((String) output1.get("stck_prpr"))
                        .priceChange((String) output1.get("prdy_vrss"))
                        .changeRate((String) output1.get("prdy_ctrt"))
                        .changeSign((String) output1.get("prdy_vrss_sign"))
                        .volume((String) output1.get("acml_vol"))
                        .marketCap((String) output1.get("hts_avls"))
                        .per((String) output1.get("per"))
                        .pbr((String) output1.get("pbr"))
                        .build();
            }
        }

        return StockChartResponse.builder()
                .stockCode(stockCode)
                .stockName(null) // 프론트엔드에서 관리하므로 null로 설정
                .periodType(periodType)
                .periodDescription(StockChartResponse.PeriodType.fromCode(periodType).getDescription())
                .startDate(startDate)
                .endDate(endDate)
                .chartData(chartData)
                .summary(summary)
                .build();
    }
}