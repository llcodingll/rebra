package com.rebra.service;

import com.rebra.client.FssApiClient;
import com.rebra.common.PageInfo;
import com.rebra.component.KisApiComponent;
import com.rebra.dto.external.FssStockBasicInfoResponse;
import com.rebra.dto.response.StockBasicInfoResponse;
import com.rebra.dto.response.StockChartResponse;
import com.rebra.dto.response.StockDetailResponse;
import com.rebra.dto.response.StockHoldingDetailResponse;
import com.rebra.dto.response.StockHoldingListResponse;
import com.rebra.dto.response.StockHoldingResponse;
import com.rebra.dto.internal.StockDto;
import com.rebra.dto.internal.StockPriceDto;
import com.rebra.entity.Account;
import com.rebra.entity.Stock;
import com.rebra.entity.StockPrice;
import com.rebra.dto.external.FssStockPriceResponse;
import com.rebra.exception.account.AccountException;
import com.rebra.exception.external.ExternalApiException;
import com.rebra.exception.stock.StockException;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.StockPriceRepository;
import com.rebra.repository.StockRepository;
import com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult;
import com.youhogeon.finance.kis_api.api.rest.quotations.InquireAskingPriceExpCcnResult;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.youhogeon.finance.kis_api.api.rest.trading.InquirePsblOrderResult;
import com.rebra.exception.kis.KisException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
@Transactional
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final AccountRepository accountRepository;
    private final KisApiComponent kisApiComponent;
    private final FssApiClient fssApiClient;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Stock DTO 조회 (SmartStockDataService에서 사용)
     */
    public StockDto findStockDtoByCode(String stockCode) {
        Stock stock = stockRepository.findByStockCode(stockCode)
            .orElseThrow(() -> StockException.stockCodeNotFound());
        return StockDto.from(stock);
    }
    
    /**
     * Stock 엔티티 조회 (기존 사용자 호환성)
     */
    public Stock findStockByCode(String stockCode) {
        return stockRepository.findByStockCode(stockCode)
            .orElseThrow(() -> StockException.stockCodeNotFound());
    }
    
    /**
     * Stock 엔티티 조회 (Optional 반환)
     */
    public Optional<Stock> findStockByCodeOptional(String stockCode) {
        return stockRepository.findByStockCode(stockCode);
    }
    
    
    /**
     * 종목 데이터 확보 - 트랜잭션 내에서 실행 (비관적 잠금 적용)
     */
    @Transactional
    public void ensureStockDataWithTransaction(String stockCode, LocalDate requestStart, LocalDate requestEnd) {
        log.info("데이터 확보 요청 (트랜잭션 내) - 종목: {}, 범위: {} ~ {}", 
            stockCode, requestStart, requestEnd);
        
        // 1. Stock 조회 (비관적 잠금 적용)
        Stock stock = stockRepository.findByStockCodeWithLock(stockCode)
            .orElseThrow(() -> StockException.stockCodeNotFound());
        log.info("Stock 조회 완료 (비관적 잠금) - 종목: {}, 현재 dataRange: {} ~ {}", 
            stockCode, stock.getDataStartDate(), stock.getDataEndDate());
        
        // 2. 캐싱 체크
        log.info("캐싱 체크 - 종목: {}, 요청범위: {} ~ {}, 현재범위: {} ~ {}",
            stockCode, requestStart, requestEnd, 
            stock.getDataStartDate(), stock.getDataEndDate());
        
        if (stock.hasDataInRange(requestStart, requestEnd)) {
            log.info("캐싱된 데이터로 충분 - 종목: {}", stockCode);
            return;
        }
        
        log.info("추가 데이터 필요 - 종목: {}, 이유: 요청시작({}) < 현재시작({}) = {}, 요청종료({}) > 현재종료({}) = {}",
            stockCode, 
            requestStart, stock.getDataStartDate(), 
            stock.getDataStartDate() == null || requestStart.isBefore(stock.getDataStartDate()),
            requestEnd, stock.getDataEndDate(),
            stock.getDataEndDate() == null || requestEnd.isAfter(stock.getDataEndDate()));
        
        // 3. 필요한 구간 계산 및 API 호출
        boolean dataFetched = false;
        
        if (stock.getDataStartDate() != null && stock.getDataEndDate() != null) {
            // 앞쪽 구간만 필요한 경우
            if (requestStart.isBefore(stock.getDataStartDate())) {
                LocalDate fetchEnd = stock.getDataStartDate().minusDays(1);
                fetchDataForPeriod(stock, requestStart, fetchEnd);
                dataFetched = true;
                log.info("앞쪽 구간 데이터 조회 - {} ~ {}", requestStart, fetchEnd);
            }
            
            // 뒤쪽 구간만 필요한 경우
            if (requestEnd.isAfter(stock.getDataEndDate())) {
                LocalDate fetchStart = stock.getDataEndDate().plusDays(1);
                fetchDataForPeriod(stock, fetchStart, requestEnd);
                dataFetched = true;
                log.info("뒤쪽 구간 데이터 조회 - {} ~ {}", fetchStart, requestEnd);
            }
        } else {
            // 데이터가 없으면 전체 구간 조회
            fetchDataForPeriod(stock, requestStart, requestEnd);
            dataFetched = true;
            log.info("전체 구간 데이터 조회 - {} ~ {}", requestStart, requestEnd);
        }
        
        // 4. 데이터 범위 업데이트 (같은 트랜잭션 내)
        if (dataFetched) {
            stock.updateDataRange(requestStart, requestEnd);
            stockRepository.save(stock);
            log.info("Stock 데이터 범위 업데이트 완료 - 종목: {}, 새 범위: {} ~ {}", 
                stockCode, stock.getDataStartDate(), stock.getDataEndDate());
        }
        
        log.info("데이터 확보 완료 (트랜잭션 내) - 종목: {}", stockCode);
    }
    
    /**
     * API를 통해 데이터 조회 및 저장
     */
    private void fetchDataForPeriod(Stock stock, LocalDate start, LocalDate end) {
        log.info("API 데이터 조회 - 종목: {}, 범위: {} ~ {}", stock.getStockCode(), start, end);

        try {
            // FSS API 호출 (종목코드로 구간 검색)
            List<FssStockPriceResponse.StockItem> items = 
                fssApiClient.getStockPriceByCodeAndDateRange(stock.getStockCode(), start, end);

            // 조회된 모든 데이터를 리스트로 변환
            List<StockPrice> stockPrices = new ArrayList<>();
            
            for (FssStockPriceResponse.StockItem item : items) {
                try {
                    StockPrice stockPrice = convertToStockPrice(item, stock);
                    stockPrices.add(stockPrice);
                    
                    // Stock 정보 업데이트 (첫 번째 데이터에서만)
                    if (stockPrices.size() == 1) {
                        updateStockInfoFromApi(stock, item);
                    }
                } catch (Exception e) {
                    log.warn("개별 데이터 변환 실패 - 종목: {}, 날짜: {}, 오류: {}", 
                        stock.getStockCode(), item.getBasDt(), e.getMessage());
                }
            }
            
            // 중복 체크 후 저장
            List<StockPriceDto> stockPriceDtos = stockPrices.stream()
                .map(StockPriceDto::from)
                .collect(Collectors.toList());
            List<StockPriceDto> savedPriceDtos = saveStockPricesWithDuplicateCheck(
                stock.getStockCode(), stockPriceDtos);
            int savedCount = savedPriceDtos.size();

            log.info("API 데이터 저장 완료 - 종목: {}, 조회된 건수: {}, 저장된 건수: {}", 
                stock.getStockCode(), items.size(), savedCount);
                
        } catch (Exception e) {
            log.error("API 데이터 조회 실패 - 종목: {}, 범위: {} ~ {}, 오류: {}", 
                stock.getStockCode(), start, end, e.getMessage());
            throw e;
        }
    }
    
    /**
     * StockPrice 엔티티 변환
     */
    private StockPrice convertToStockPrice(FssStockPriceResponse.StockItem item, Stock stock) {
        try {
            return StockPrice.builder()
                .stock(stock)
                .ticker(stock.getStockCode())
                .name(StringUtils.hasText(item.getItmsNm()) ? item.getItmsNm() : stock.getStockName())
                .date(LocalDate.parse(item.getBasDt(), DATE_FORMATTER))
                .openPrice(parsePrice(item.getMkp()))
                .highPrice(parsePrice(item.getHipr()))
                .lowPrice(parsePrice(item.getLopr()))
                .closePrice(parsePrice(item.getClpr()))
                .volume(parseLong(item.getTrqu()))
                .changeRate(parseChangeRate(item.getFltRt()))
                .build();
        } catch (Exception e) {
            log.error("StockPrice 변환 실패 - 종목: {}, 오류: {}", item.getItmsNm(), e.getMessage());
            throw new ExternalApiException("주식 데이터 변환 중 오류가 발생했습니다");
        }
    }
    
    /**
     * API 응답에서 Stock 정보 업데이트
     */
    private void updateStockInfoFromApi(Stock stock, FssStockPriceResponse.StockItem item) {
        if (stock.getStockName().equals(stock.getStockCode()) && 
            StringUtils.hasText(item.getItmsNm())) {
            log.info("종목명 업데이트 필요 - 코드: {}, 기존: {}, 신규: {}", 
                stock.getStockCode(), stock.getStockName(), item.getItmsNm());
        }
    }
    
    private Integer parsePrice(String priceStr) {
        if (!StringUtils.hasText(priceStr)) {
            return 0;
        }
        try {
            String cleanPrice = priceStr.replaceAll(",", "");
            return Integer.parseInt(cleanPrice);
        } catch (NumberFormatException e) {
            log.warn("가격 파싱 실패: {}", priceStr);
            return 0;
        }
    }

    private Long parseLong(String longStr) {
        if (!StringUtils.hasText(longStr)) {
            return 0L;
        }
        try {
            String cleanLong = longStr.replaceAll(",", "");
            return Long.parseLong(cleanLong);
        } catch (NumberFormatException e) {
            log.warn("Long 파싱 실패: {}", longStr);
            return 0L;
        }
    }

    private Double parseChangeRate(String rateStr) {
        if (!StringUtils.hasText(rateStr)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(rateStr);
        } catch (NumberFormatException e) {
            log.warn("등락률 파싱 실패: {}", rateStr);
            return 0.0;
        }
    }
    
    /**
     * StockPrice 저장 (DTO 사용)
     */
    @Transactional
    public List<StockPriceDto> saveStockPricesWithDuplicateCheck(
            String stockCode, List<StockPriceDto> stockPriceDtos) {
        if (stockPriceDtos.isEmpty()) {
            return Collections.emptyList();
        }
        
        Stock stock = stockRepository.findByStockCode(stockCode)
            .orElseThrow(() -> StockException.stockCodeNotFound());
        
        List<StockPrice> stockPrices = stockPriceDtos.stream()
            .map(dto -> dto.toEntity(stock))
            .collect(Collectors.toList());
        
        List<StockPrice> savedPrices = stockPriceRepository.saveAllWithBatchCheck(stockPrices);
        
        return savedPrices.stream()
            .map(StockPriceDto::from)
            .collect(Collectors.toList());
    }
    
    /**
     * Stock 데이터 통계 조회
     */
    public SmartStockDataService.DataStats getStockDataStats(String stockCode) {
        Optional<Stock> stockOpt = stockRepository.findByStockCode(stockCode);
        if (stockOpt.isEmpty()) {
            return new SmartStockDataService.DataStats(stockCode, 0L, null, null);
        }
        
        Stock stock = stockOpt.get();
        Long dataCount = stockPriceRepository.countByStock(stock);
        
        return new SmartStockDataService.DataStats(
            stockCode,
            dataCount,
            stock.getDataStartDate(),
            stock.getDataEndDate()
        );
    }

    // Redis 캐싱 제거 - 프론트엔드에서 실시간 데이터 관리
    // 실시간 데이터는 WebSocket을 통해 직접 클라이언트로 전달

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

            // 유효한 종목들 필터링 및 Stock 저장
            List<FssStockBasicInfoResponse.StockBasicItem> validItems = apiResults.stream()
                    .filter(item -> item.getStckIssuCmpyNm() != null 
                            && item.getStckIssuCmpyNm().contains(stockName)
                            && StringUtils.hasText(item.getLstgDt()) 
                            && !StringUtils.hasText(item.getLstgAbolDt()))
                    .collect(Collectors.toList());

            if (!validItems.isEmpty()) {
                // Stock 엔티티 배치 저장
                prepareStocksInBatch(validItems);
            }

            // API 결과를 StockBasicInfoResponse로 변환
            List<StockBasicInfoResponse> responses = new ArrayList<>();

            for (FssStockBasicInfoResponse.StockBasicItem item : validItems) {
                try {
                    responses.add(StockBasicInfoResponse.from(item));
                    log.debug("상장 중인 종목 추가: {} ({}) - 상장일: {}", 
                             item.getStckIssuCmpyNm(), 
                             item.getItmsShrtnCd(), 
                             item.getLstgDt());

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

    private void prepareStocksInBatch(List<FssStockBasicInfoResponse.StockBasicItem> validItems) {
        // 1. 종목코드 수집
        Set<String> stockCodes = validItems.stream()
                .map(FssStockBasicInfoResponse.StockBasicItem::getItmsShrtnCd)
                .collect(Collectors.toSet());
        
        // 2. 기존 Stock 조회
        List<Stock> existingStocks = stockRepository.findByStockCodeIn(stockCodes);
        Set<String> existingCodes = existingStocks.stream()
                .map(Stock::getStockCode)
                .collect(Collectors.toSet());
        
        // 3. 새로운 Stock만 저장
        List<Stock> newStocks = validItems.stream()
                .filter(item -> !existingCodes.contains(item.getItmsShrtnCd()))
                .map(item -> Stock.builder()
                        .stockCode(item.getItmsShrtnCd())
                        .stockName(item.getStckIssuCmpyNm())
                        .stockType("주식")
                        .isActive(true)
                        .build())
                .collect(Collectors.toList());
        
        if (!newStocks.isEmpty()) {
            log.info("신규 Stock 엔티티 배치 저장 시작 - 저장할 개수: {}", newStocks.size());
            List<Stock> savedStocks = stockRepository.saveAll(newStocks);
            log.info("신규 Stock 엔티티 배치 저장 완료 - 저장된 개수: {}", savedStocks.size());
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

            // 2. KIS API로 잔고 조회 및 매수가능조회
            InquireBalanceResult balanceResult = kisApiComponent.getUserBalance(account);
            InquirePsblOrderResult psblOrderResult = kisApiComponent.getUserPossibleOrder(account, stockCode);

            // 3. 해당 종목의 보유 정보 찾기
            if (balanceResult.getOutput1() != null && balanceResult.getOutput1().length > 0) {
                for (InquireBalanceResult.Output1 holding : balanceResult.getOutput1()) {
                    if (stockCode.equals(holding.getPdno())) {
                        // 보유 수량이 0이 아닌 경우만 반환
                        if (!"0".equals(holding.getHldgQty())) {
                            log.info("특정 종목 보유 정보 조회 완료 - StockCode: {}, AccountId: {}", stockCode, accountId);
                            return StockHoldingDetailResponse.from(holding, psblOrderResult);
                        }
                        break;
                    }
                }
            }

            // 보유하지 않은 경우 - 매수가능금액만 반환
            log.info("특정 종목 미보유 - StockCode: {}, AccountId: {}", stockCode, accountId);
            return StockHoldingDetailResponse.fromPossibleOrderOnly(psblOrderResult);

        } catch (Exception e) {
            log.error("특정 종목 보유 정보 조회 실패 - StockCode: {}, AccountId: {}, Error: {}",
                    stockCode, accountId, e.getMessage(), e);
            throw StockException.stockHoldingDetailFetchFailed();
        }
    }

    @Override
    public InquireAskingPriceExpCcnResult getCurrentAskingPrice(String stockCode, Long accountId) {
        try {
            log.info("주식현재가 호가/예상체결 조회 시작 - 종목코드: {}, 계좌ID: {}", stockCode, accountId);

            // 1. 계좌 조회
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(AccountException::accountNotFound);

            // 2. KIS API로 현재가 호가/예상체결 조회 및 직접 반환
            InquireAskingPriceExpCcnResult result = kisApiComponent.getCurrentAskingPrice(account, stockCode);

            log.info("주식현재가 호가/예상체결 조회 완료 - 종목코드: {}, 계좌ID: {}", stockCode, accountId);
            return result;

        } catch (KisException e) {
            // KisException은 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("주식현재가 호가/예상체결 조회 중 오류 발생 - 종목코드: {}, 계좌ID: {}, 오류: {}", stockCode, accountId, e.getMessage(), e);
            throw new RuntimeException("주식현재가 호가/예상체결 조회 실패", e);
        }
    }
}