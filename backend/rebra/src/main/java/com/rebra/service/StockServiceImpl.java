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
import com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult;
import java.util.ArrayList;
import java.util.List;
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

            InquireDailyItemchartpriceResult kisResult = kisApiComponent.getStockChartData(
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

    private StockChartResponse buildStockChartResponse(String stockCode, InquireDailyItemchartpriceResult kisResult,
                                                       String startDate, String endDate, String periodType) {
        List<StockChartResponse.ChartDataPoint> chartData = new ArrayList<>();
        StockChartResponse.StockSummary summary = null;

        if (kisResult != null && kisResult.getOutput2() != null) {
            InquireDailyItemchartpriceResult.Output2[] output2Array = kisResult.getOutput2();

            for (InquireDailyItemchartpriceResult.Output2 item : output2Array) {
                chartData.add(StockChartResponse.ChartDataPoint.builder()
                        .tradingDate(item.getStckBsopDate())
                        .openPrice(item.getStckOprc())
                        .highPrice(item.getStckHgpr())
                        .lowPrice(item.getStckLwpr())
                        .closePrice(item.getStckClpr())
                        .volume(item.getAcmlVol())
                        .tradingValue(item.getAcmlTrPbmn())
                        .priceChange(item.getPrdyVrss())
                        .changeSign(item.getPrdyVrssSign())
                        .changeRate(item.getPrttRate())
                        .build());
            }

            if (kisResult.getOutput1() != null) {
                InquireDailyItemchartpriceResult.Output1 output1 = kisResult.getOutput1();
                summary = StockChartResponse.StockSummary.builder()
                        .currentPrice(output1.getStckPrpr())
                        .priceChange(output1.getPrdyVrss())
                        .changeRate(output1.getPrdyCtrt())
                        .changeSign(output1.getPrdyVrssSign())
                        .volume(output1.getAcmlVol())
                        .marketCap(output1.getHtsAvls())
                        .per(output1.getPer())
                        .pbr(output1.getPbr())
                        .build();
            }
        }

        return StockChartResponse.builder()
                .periodType(periodType)
                .periodDescription(StockChartResponse.PeriodType.fromCode(periodType).getDescription())
                .startDate(startDate)
                .endDate(endDate)
                .chartData(chartData)
                .summary(summary)
                .build();
    }
}