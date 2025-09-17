package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockDetailResponse;
import com.rebra.dto.response.StockSearchResponse;
import com.rebra.entity.Account;
import com.rebra.entity.Stock;
import com.rebra.exception.stock.StockException;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.StockRepository;
import com.rebra.util.AccountEncryptionUtil;
import com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
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
            InquireDailyItemchartpriceResult.Output2[] output2 = (InquireDailyItemchartpriceResult.Output2[]) kisResult.get("output2");

            for (InquireDailyItemchartpriceResult.Output2 item : output2) {
                chartData.add(StockChartResponse.ChartDataPoint.builder()
                        .tradingDate(item.getStckBsopDate())      // 주식 영업일자
                        .openPrice(item.getStckOprc())             // 주식 시가
                        .highPrice(item.getStckHgpr())             // 주식 최고가
                        .lowPrice(item.getStckLwpr())              // 주식 최저가
                        .closePrice(item.getStckClpr())            // 주식 종가
                        .volume(item.getAcmlVol())                 // 누적 거래량
                        .tradingValue(item.getAcmlTrPbmn())        // 누적 거래대금
                        .priceChange(item.getPrdyVrss())           // 전일 대비
                        .changeSign(item.getPrdyVrssSign())        // 전일 대비 부호
                        // changeRate는 각 포인트마다 계산하거나 Output1에서 가져와야 함
                        .build());
            }

            if (kisResult.containsKey("output1")) {
                InquireDailyItemchartpriceResult.Output1 output1 = (InquireDailyItemchartpriceResult.Output1) kisResult.get("output1");
                summary = StockChartResponse.StockSummary.builder()
                        .currentPrice(output1.getStckPrpr())         // 주식 현재가
                        .priceChange(output1.getPrdyVrss())          // 전일 대비
                        .changeRate(output1.getPrdyCtrt())           // 전일 대비율
                        .changeSign(output1.getPrdyVrssSign())       // 전일 대비 부호
                        .volume(output1.getAcmlVol())                // 누적 거래량
                        .marketCap(output1.getHtsAvls())             // HTS 시가총액
                        .per(output1.getPer())                       // PER
                        .pbr(output1.getPbr())                       // PBR
                        // 새로 추가된 필드들 활용
                        .previousClosePrice(output1.getStckPrdyClpr()) // 전일 종가
                        .upperLimit(output1.getStckMxpr())             // 상한가
                        .lowerLimit(output1.getStckLlam())             // 하한가
                        .askPrice(output1.getAskp())                   // 매도호가
                        .bidPrice(output1.getBidp())                   // 매수호가
                        .eps(output1.getEps())                         // EPS
                        .listedShares(output1.getLstnStcn())           // 상장주수
                        .capital(output1.getCpfn())                    // 자본금
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

    @Override
    public StockDetailResponse getStockDetail(String stockCode, boolean includeHolding, Long userId) {
        try {
            log.info("종목 상세 정보 조회 시작 - UserId: {}, StockCode: {}, IncludeHolding: {}",
                    userId, stockCode, includeHolding);

            // 1. 종목 기본 정보 조회
            Stock stock = stockRepository.findByStockCodeAndIsActiveTrue(stockCode)
                    .orElseThrow(StockException::stockCodeNotFound);

            // 2. 보유 정보 조회 (옵션)
            StockDetailResponse.HoldingInfo holdingInfo = null;
            if (includeHolding) {
                holdingInfo = getHoldingInfo(stockCode, userId);
            }

            // 3. 응답 생성
            return StockDetailResponse.ofWithHoldingInfo(stock, userId, stockCode, holdingInfo);

        } catch (Exception e) {
            log.error("종목 상세 정보 조회 실패 - UserId: {}, StockCode: {}, ErrorType: {}, Message: {}",
                    userId, stockCode, e.getClass().getSimpleName(), e.getMessage(), e);

            String detailedMessage = "종목 상세 정보 조회에 실패했습니다";
            if (e.getMessage() != null) {
                if (e.getMessage().contains("활성화된 계좌")) {
                    detailedMessage = "활성화된 계좌를 찾을 수 없습니다. 계좌를 연결해주세요.";
                } else if (e.getMessage().contains("복호화")) {
                    detailedMessage = "계좌 정보 복호화에 실패했습니다.";
                } else if (e.getMessage().contains("KIS")) {
                    detailedMessage = "KIS API 연동에 실패했습니다. 잠시 후 다시 시도해주세요.";
                } else {
                    detailedMessage = "종목 상세 정보 조회 실패: " + e.getMessage();
                }
            }

            throw new RuntimeException(detailedMessage, e);
        }
    }

    private StockDetailResponse.HoldingInfo getHoldingInfo(String stockCode, Long userId) {
        try {
            // 1. 활성화된 계좌 조회
            Account account = accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true)
                    .orElseThrow(() -> new RuntimeException("활성화된 계좌를 찾을 수 없습니다."));

            // 2. 계좌 복호화
            DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
            kisApiComponent.ensureUserCredentials(userId, account.getId(), account.getAccountType(), credentials);

            // 3. KIS API로 잔고 조회
            InquireBalanceResult balanceResult = kisApiComponent.getUserBalance(
                    userId, account.getId(), account.getAccountType(), credentials);

            // 4. 해당 종목의 보유 정보 찾기
            if (balanceResult.getOutput1() != null && balanceResult.getOutput1().length > 0) {
                for (InquireBalanceResult.Output1 holding : balanceResult.getOutput1()) {
                    if (stockCode.equals(holding.getPdno())) {
                        // 보유 수량이 0이 아닌 경우만 반환
                        if (!"0".equals(holding.getHldgQty())) {
                            return StockDetailResponse.HoldingInfo.builder()
                                    .holdingQuantity(holding.getHldgQty())
                                    .purchaseAmount(holding.getPchsAmt())
                                    .averagePrice(holding.getPchsAvgPric())
                                    .currentValue(holding.getEvluAmt())
                                    .profitLoss(holding.getEvluPflsAmt())
                                    .profitLossRate(holding.getEvluPflsRt())
                                    .build();
                        }
                        break;
                    }
                }
            }

            // 보유하지 않은 경우 null 반환
            return null;

        } catch (Exception e) {
            log.warn("보유 정보 조회 실패 (종목은 정상 조회됨) - UserId: {}, StockCode: {}, Error: {}",
                    userId, stockCode, e.getMessage());
            // 보유 정보 조회 실패시에도 종목 기본 정보는 제공
            return null;
        }
    }
}