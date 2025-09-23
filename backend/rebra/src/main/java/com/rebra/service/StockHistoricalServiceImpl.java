package com.rebra.service;

import com.rebra.client.FssApiClient;
import com.rebra.dto.external.FssStockPriceResponse;
import com.rebra.dto.request.StockHistoricalSearchRequest;
import com.rebra.dto.response.StockHistoricalDataResponse;
import com.rebra.dto.response.StockHistoricalDataWithTradingInfo;
import com.rebra.entity.Stock;
import com.rebra.entity.StockPrice;
import com.rebra.exception.external.ExternalApiException;
import com.rebra.repository.StockPriceRepository;
import com.rebra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StockHistoricalServiceImpl implements StockHistoricalService {

    private final StockPriceRepository stockPriceRepository;
    private final StockRepository stockRepository;
    private final FssApiClient fssApiClient;
    private final HolidayService holidayService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public StockHistoricalDataWithTradingInfo getStockHistoricalData(StockHistoricalSearchRequest request) {
        String stockName = request.getStockName();
        LocalDate date = request.getDate();

        log.info("주식 과거 데이터 조회 시작 - 종목명: {}, 날짜: {}", stockName, date);

        // 1. 거래일 여부 체크
        boolean isTradingDay = holidayService.isTradingDay(date);
        log.info("거래일 체크 결과 - 날짜: {}, 거래일 여부: {}", date, isTradingDay);

        if (!isTradingDay) {
            log.info("거래일이 아님 - 날짜: {} (주말 또는 공휴일)", date);
            return StockHistoricalDataWithTradingInfo.builder()
                    .tradingDay(false)
                    .data(List.of())
                    .message(date + "은(는) 거래일이 아닙니다 (주말/공휴일)")
                    .build();
        }

        // 2. 거래일인 경우 주식 데이터 조회
        List<StockHistoricalDataResponse> stockData = new ArrayList<>();
        
        // FSS API에서 먼저 조회 (모든 관련 종목 검색)
        log.info("FSS API에서 조회 시작 - 모든 관련 종목 검색");
        List<FssStockPriceResponse.StockItem> apiResults = fssApiClient.getStockPriceByNameAndDate(stockName, date);

        if (apiResults.isEmpty()) {
            log.warn("FSS API에서 데이터를 찾을 수 없음 - 종목명: {}, 날짜: {}", stockName, date);
            // API에서 데이터가 없으면 DB에서 조회해보기
            List<StockPrice> dbResults = stockPriceRepository.findByNameContainingAndDate(stockName, date);
            if (!dbResults.isEmpty()) {
                log.info("DB에서 기존 데이터 조회 성공 - 조회된 건수: {}", dbResults.size());
                stockData = dbResults.stream()
                        .map(StockHistoricalDataResponse::from)
                        .collect(Collectors.toList());
            }
        } else {
            // API 결과 처리 - Stock 엔티티 배치 저장 후 StockPrice 생성
            
            // 유효한 종목들 필터링 및 종목코드 수집
            List<FssStockPriceResponse.StockItem> validItems = apiResults.stream()
                    .filter(item -> item.getItmsNm().contains(stockName))
                    .collect(Collectors.toList());
            
            if (!validItems.isEmpty()) {
                // 필요한 Stock 엔티티들을 배치로 준비
                Map<String, Stock> stockMap = prepareStocksInBatch(validItems);
                
                // StockPrice 생성 및 응답 구성
                for (FssStockPriceResponse.StockItem item : validItems) {
                    try {
                        Stock stock = stockMap.get(item.getSrtnCd());
                        if (stock == null) {
                            log.warn("Stock 엔티티를 찾을 수 없음 - 종목코드: {}", item.getSrtnCd());
                            continue;
                        }
                        
                        // StockPrice는 메모리에서만 생성 (DB 저장 없음)
                        StockPrice stockPrice = convertToStockPrice(item, stock);
                        
                        // 응답 리스트에 추가 (프론트엔드 반환용)
                        stockData.add(StockHistoricalDataResponse.from(stockPrice));
                        
                    } catch (Exception e) {
                        log.error("주식 데이터 변환/처리 실패 - 종목: {}, 오류: {}", item.getItmsNm(), e.getMessage());
                        // 개별 항목 실패는 전체 처리를 중단하지 않음
                    }
                }
                log.info("FSS API 조회 및 처리 완료 - 전체 응답 건수: {}", stockData.size());
            } else {
                log.warn("유효한 종목이 없습니다 - 요청 종목명: {}", stockName);
            }
        }
        
        String message = null;
        if (stockData.isEmpty()) {
            message = "해당 종목을 찾을 수 없습니다";
        }

        return StockHistoricalDataWithTradingInfo.builder()
                .tradingDay(true)
                .data(stockData)
                .message(message)
                .build();
    }


    /**
     * Stock 엔티티들을 배치로 준비한다 (기존 조회 + 신규 생성)
     */
    private Map<String, Stock> prepareStocksInBatch(List<FssStockPriceResponse.StockItem> validItems) {
        // 1. 종목코드 수집
        Set<String> stockCodes = validItems.stream()
                .map(FssStockPriceResponse.StockItem::getSrtnCd)
                .collect(Collectors.toSet());
        
        // 2. 기존 Stock 엔티티들 조회
        List<Stock> existingStocks = stockRepository.findByStockCodeIn(stockCodes);
        Map<String, Stock> stockMap = existingStocks.stream()
                .collect(Collectors.toMap(Stock::getStockCode, stock -> stock));
        
        // 3. 신규 Stock 엔티티들 준비
        List<Stock> newStocks = new ArrayList<>();
        Map<String, String> codeToNameMap = validItems.stream()
                .collect(Collectors.toMap(
                    FssStockPriceResponse.StockItem::getSrtnCd,
                    FssStockPriceResponse.StockItem::getItmsNm,
                    (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));
        
        for (String stockCode : stockCodes) {
            if (!stockMap.containsKey(stockCode)) {
                String stockName = codeToNameMap.get(stockCode);
                Stock newStock = Stock.builder()
                        .stockCode(stockCode)
                        .stockName(stockName)
                        .stockType("주식")
                        .isActive(true)
                        .build();
                newStocks.add(newStock);
                log.info("새로운 Stock 엔티티 준비 - 코드: {}, 이름: {}", stockCode, stockName);
            }
        }
        
        // 4. 신규 Stock 엔티티들 배치 저장
        if (!newStocks.isEmpty()) {
            log.info("신규 Stock 엔티티 배치 저장 시작 - 저장할 개수: {}", newStocks.size());
            List<Stock> savedStocks = stockRepository.saveAll(newStocks);
            log.info("신규 Stock 엔티티 배치 저장 완료 - 저장된 개수: {}", savedStocks.size());
            
            // 새로 저장된 Stock들을 맵에 추가
            for (Stock savedStock : savedStocks) {
                stockMap.put(savedStock.getStockCode(), savedStock);
            }
        }
        
        return stockMap;
    }

    /**
     * Stock 엔티티를 조회하거나 생성한다 (개별 처리용 - 호환성 유지)
     */
    private Stock getOrCreateStock(String stockCode, String stockName) {
        return stockRepository.findByStockCode(stockCode)
                .orElseGet(() -> {
                    log.info("새로운 Stock 엔티티 생성 - 코드: {}, 이름: {}", stockCode, stockName);
                    Stock newStock = Stock.builder()
                            .stockCode(stockCode)
                            .stockName(stockName)
                            .stockType("주식")
                            .isActive(true)
                            .build();
                    return stockRepository.save(newStock);
                });
    }

    private StockPrice convertToStockPrice(FssStockPriceResponse.StockItem item, Stock stock) {
        try {
            return StockPrice.builder()
                    .stock(stock) // Stock 연관관계 설정
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
            throw new ExternalApiException("주식 데이터 변환 중 오류가 발생했습니다");
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
}