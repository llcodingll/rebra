package com.rebra.service;

import com.rebra.client.FssApiClient;
import com.rebra.common.PageInfo;
import com.rebra.component.KisApiComponent;
import com.rebra.dto.external.FssStockBasicInfoResponse;
import com.rebra.dto.external.FssStockPriceResponse;
import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.StockBasicInfoResponse;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockDetailResponse;
import com.rebra.dto.response.StockHistoricalDataResponse;
import com.rebra.dto.response.StockHoldingDetailResponse;
import com.rebra.dto.response.StockHoldingListResponse;
import com.rebra.dto.response.StockHoldingResponse;
import com.rebra.dto.response.StockSearchResponse;
import com.rebra.entity.Account;
import com.rebra.entity.Stock;
import com.rebra.entity.StockPrice;
import com.rebra.exception.account.AccountException;
import com.rebra.exception.stock.StockException;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.StockRepository;
import com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final KisApiComponent kisApiComponent;
    private final FssApiClient fssApiClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    // Redis 캐싱 제거 - 프론트엔드에서 실시간 데이터 관리
    // 실시간 데이터는 WebSocket을 통해 직접 클라이언트로 전달

    @Override
    public StockSearchResponse findByStockCode(String stockCode) {
        return stockRepository.findByStockCodeAndIsActiveTrue(stockCode)
                .map(StockSearchResponse::from)
                .orElseThrow(StockException::stockCodeNotFound);
    }

    @Override
    public PageResponse<StockSearchResponse> searchStocks(String stockName, Pageable pageable) {
        Page<Stock> stockPage = stockRepository.findByStockNameContainingIgnoreCaseAndIsActiveTrue(stockName, pageable);
        Page<StockSearchResponse> dtoPage = stockPage.map(StockSearchResponse::from);
        return PageResponse.from(dtoPage);
    }

    @Override
    public List<StockHistoricalDataResponse> searchStocksFromApi(String stockName) {
        log.info("FSS API를 통한 주식 검색 시작 - 종목명: {}", stockName);

        // 최근 영업일을 찾기 위해 최대 10일 전까지 시도
        LocalDate searchDate = LocalDate.now().minusDays(1);
        List<FssStockPriceResponse.StockItem> apiResults = null;

        for (int i = 0; i < 10; i++) {
            log.info("FSS API 조회 시도 - 날짜: {}, 시도 횟수: {}", searchDate, i + 1);

            try {
                apiResults = fssApiClient.getStockPriceByNameAndDate(stockName, searchDate);
                if (!apiResults.isEmpty()) {
                    log.info("FSS API에서 데이터 발견 - 날짜: {}, 조회된 종목 수: {}", searchDate, apiResults.size());
                    break;
                }
            } catch (Exception e) {
                log.warn("FSS API 호출 실패 - 날짜: {}, 오류: {}", searchDate, e.getMessage());
            }

            searchDate = searchDate.minusDays(1);
        }

        if (apiResults == null || apiResults.isEmpty()) {
            log.warn("FSS API에서 최근 10일 내 데이터를 찾을 수 없음 - 종목명: {}", stockName);
            return List.of();
        }

        // API 결과를 StockHistoricalDataResponse로 변환
        List<StockHistoricalDataResponse> responses = new ArrayList<>();

        for (FssStockPriceResponse.StockItem item : apiResults) {
            try {
                // 종목명이 요청한 종목명을 포함하는지 확인
                if (!item.getItmsNm().contains(stockName)) {
                    continue; // 관련 없는 종목은 건너뛰기
                }

                // StockPrice 객체 생성 (메모리에서만 사용, DB 저장 안 함)
                StockPrice stockPrice = convertToStockPrice(item);

                // 응답 리스트에 추가
                responses.add(StockHistoricalDataResponse.from(stockPrice));

            } catch (Exception e) {
                log.error("주식 데이터 변환 실패 - 종목: {}, 오류: {}", item.getItmsNm(), e.getMessage());
                // 개별 항목 실패는 전체 처리를 중단하지 않음
            }
        }

        log.info("FSS API 주식 검색 완료 - 요청 종목명: {}, 응답 건수: {}", stockName, responses.size());

        return responses;
    }

    private StockPrice convertToStockPrice(FssStockPriceResponse.StockItem item) {
        try {
            return StockPrice.builder()
                    .ticker(item.getSrtnCd()) // 6자리 단축코드
                    .name(item.getItmsNm()) // 종목명
                    .date(LocalDate.parse(item.getBasDt(), DATE_FORMATTER)) // 기준일자
                    .openPrice(parsePrice(item.getMkp())) // 시가
                    .highPrice(parsePrice(item.getHipr())) // 고가
                    .lowPrice(parsePrice(item.getLopr())) // 저가
                    .closePrice(parsePrice(item.getClpr())) // 종가
                    .volume(parseLong(item.getTrqu())) // 거래량
                    .changeRate(parseChangeRate(item.getFltRt())) // 등락률
                    .build();

        } catch (Exception e) {
            log.error("StockPrice 변환 실패 - 종목: {}, 오류: {}", item.getItmsNm(), e.getMessage());
            throw new RuntimeException("주식 데이터 변환 중 오류가 발생했습니다");
        }
    }

    private BigDecimal parsePrice(String priceStr) {
        if (!StringUtils.hasText(priceStr)) {
            return BigDecimal.ZERO;
        }
        try {
            // 콤마 제거 후 변환
            String cleanPrice = priceStr.replaceAll(",", "");
            return new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            log.warn("가격 파싱 실패: {}", priceStr);
            return BigDecimal.ZERO;
        }
    }

    private Long parseLong(String longStr) {
        if (!StringUtils.hasText(longStr)) {
            return 0L;
        }
        try {
            // 콤마 제거 후 변환
            String cleanLong = longStr.replaceAll(",", "");
            return Long.parseLong(cleanLong);
        } catch (NumberFormatException e) {
            log.warn("Long 파싱 실패: {}", longStr);
            return 0L;
        }
    }

    private BigDecimal parseChangeRate(String rateStr) {
        if (!StringUtils.hasText(rateStr)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(rateStr);
        } catch (NumberFormatException e) {
            log.warn("등락률 파싱 실패: {}", rateStr);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public StockChartResponse getStockChartData(String stockCode, String startDate, String endDate, String periodType,
                                                Long userId) {
        try {
            log.info("차트 데이터 조회 시작 - UserId: {}, StockCode: {}, Period: {}", userId, stockCode, periodType);

            Account account = accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true)
                    .orElseThrow(() -> new RuntimeException("활성화된 계좌를 찾을 수 없습니다."));

            Map<String, Object> kisResult = kisApiComponent.getStockChartData(
                    account, stockCode, startDate, endDate, periodType
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
            InquireDailyItemchartpriceResult.Output2[] output2 = (InquireDailyItemchartpriceResult.Output2[]) kisResult.get(
                    "output2");

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
                InquireDailyItemchartpriceResult.Output1 output1 = (InquireDailyItemchartpriceResult.Output1) kisResult.get(
                        "output1");
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

    private StockDetailResponse.HoldingInfo getHoldingInfo(String stockCode, Long userId) {
        try {
            // 1. 활성화된 계좌 조회
            Account account = accountRepository.findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(userId, true)
                    .orElseThrow(() -> new RuntimeException("활성화된 계좌를 찾을 수 없습니다."));

            // 2. KIS API로 잔고 조회
            InquireBalanceResult balanceResult = kisApiComponent.getUserBalance(account);

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

    @Override
    public List<StockBasicInfoResponse> searchStockBasicInfoFromApi(String stockName) {
        log.info("FSS API를 통한 종목기본정보 검색 시작 - 종목명: {}", stockName);

        try {
            // FSS 종목기본정보조회 API 호출
            List<FssStockBasicInfoResponse.StockBasicItem> apiResults = fssApiClient.getStockBasicInfoByName(stockName);

            if (apiResults == null || apiResults.isEmpty()) {
                log.warn("FSS API에서 데이터를 찾을 수 없음 - 종목명: {}", stockName);
                return List.of();
            }

            // API 결과를 StockBasicInfoResponse로 변환
            List<StockBasicInfoResponse> responses = new ArrayList<>();

            for (FssStockBasicInfoResponse.StockBasicItem item : apiResults) {
                try {
                    // 종목명이 요청한 종목명을 포함하는지 확인
                    if (item.getStckIssuCmpyNm() != null && 
                        item.getStckIssuCmpyNm().contains(stockName)) {
                        
                        // 상장 중인 종목만 필터링
                        // 1. 상장일자가 존재해야 함 (텍스트가 있음) - 비상장 주식 제외
                        // 2. 상장폐지일자가 없어야 함 (텍스트가 없음) - 상장폐지 종목 제외
                        if (StringUtils.hasText(item.getLstgDt()) && !StringUtils.hasText(item.getLstgAbolDt())) {
                            responses.add(StockBasicInfoResponse.from(item));
                            log.debug("상장 중인 종목 추가: {} ({}) - 상장일: {}", 
                                     item.getStckIssuCmpyNm(), 
                                     item.getItmsShrtnCd(), 
                                     item.getLstgDt());
                        } else {
                            log.debug("필터링된 종목: {} ({}) - 상장일: {}, 폐지일: {}", 
                                     item.getStckIssuCmpyNm(), 
                                     item.getItmsShrtnCd(),
                                     item.getLstgDt(), 
                                     item.getLstgAbolDt());
                        }
                    }

                } catch (Exception e) {
                    log.error("종목 기본정보 데이터 변환 실패 - 종목: {}, 오류: {}", 
                            item.getStckIssuCmpyNm(), e.getMessage());
                    // 개별 항목 실패는 전체 처리를 중단하지 않음
                }
            }

            log.info("FSS API 종목기본정보 검색 완료 - 요청 종목명: {}, 응답 건수: {}", stockName, responses.size());

            return responses;

        } catch (Exception e) {
            log.error("FSS API 종목기본정보 검색 실패 - 종목명: {}, 오류: {}", stockName, e.getMessage());
            return List.of();
        }
    }


    @Override
    public com.rebra.common.PageResponse<StockHoldingListResponse> getHoldingStocks(Long accountId, Pageable pageable) {
        try {
            log.info("보유 종목 조회 시작 - AccountId: {}, Page: {}, Size: {}",
                    accountId, pageable.getPageNumber(), pageable.getPageSize());

            // 1. 계좌 조회
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(AccountException::accountNotFound);

            // 2. KIS API로 잔고 조회
            InquireBalanceResult balanceResult = kisApiComponent.getUserBalance(account);

            // 3. 보유 종목 데이터 변환
            List<StockHoldingResponse> holdings = new ArrayList<>();

            if (balanceResult.getOutput1() != null && balanceResult.getOutput1().length > 0) {
                for (InquireBalanceResult.Output1 holding : balanceResult.getOutput1()) {
                    // 보유 수량이 0이 아닌 경우만 처리
                    if (!"0".equals(holding.getHldgQty())) {
                        holdings.add(StockHoldingResponse.from(holding));
                    }
                }
            }

            // 4. 페이지네이션 처리
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), holdings.size());
            List<StockHoldingResponse> pagedHoldings = holdings.subList(start, end);

            Page<StockHoldingResponse> page = new PageImpl<>(pagedHoldings, pageable, holdings.size());

            log.info("보유 종목 조회 완료 - AccountId: {}, 총 종목 수: {}, 페이지 크기: {}",
                    accountId, holdings.size(), pagedHoldings.size());

            StockHoldingListResponse content = StockHoldingListResponse.of(pagedHoldings);
            PageInfo pageInfo = PageInfo.from(page);

            return com.rebra.common.PageResponse.success("조회 성공", content, pageInfo);

        } catch (Exception e) {
            log.error("보유 종목 조회 실패 - AccountId: {}, Error: {}", accountId, e.getMessage(), e);
            throw StockException.stockHoldingFetchFailed();
        }
    }

    @Override
    public StockHoldingDetailResponse getStockHolding(String stockCode, Long accountId) {
        try {
            log.info("특정 종목 보유 정보 조회 시작 - StockCode: {}, AccountId: {}", stockCode, accountId);

            // 1. 계좌 조회
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(AccountException::accountNotFound);

            // 2. KIS API로 잔고 조회
            InquireBalanceResult balanceResult = kisApiComponent.getUserBalance(account);

            // 3. 해당 종목의 보유 정보 찾기
            if (balanceResult.getOutput1() != null && balanceResult.getOutput1().length > 0) {
                for (InquireBalanceResult.Output1 holding : balanceResult.getOutput1()) {
                    if (stockCode.equals(holding.getPdno())) {
                        // 보유 수량이 0이 아닌 경우만 반환
                        if (!"0".equals(holding.getHldgQty())) {
                            log.info("특정 종목 보유 정보 조회 완료 - StockCode: {}, AccountId: {}", stockCode, accountId);
                            return StockHoldingDetailResponse.from(holding);
                        }
                        break;
                    }
                }
            }

            // 보유하지 않은 경우 null 반환
            log.info("특정 종목 미보유 - StockCode: {}, AccountId: {}", stockCode, accountId);
            return null;

        } catch (Exception e) {
            log.error("특정 종목 보유 정보 조회 실패 - StockCode: {}, AccountId: {}, Error: {}",
                    stockCode, accountId, e.getMessage(), e);
            throw StockException.stockHoldingDetailFetchFailed();
        }
    }
}