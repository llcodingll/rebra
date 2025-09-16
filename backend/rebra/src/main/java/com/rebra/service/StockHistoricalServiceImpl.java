package com.rebra.service;

import com.rebra.client.FssApiClient;
import com.rebra.dto.external.FssStockPriceResponse;
import com.rebra.dto.request.StockHistoricalSearchRequest;
import com.rebra.dto.response.StockHistoricalDataResponse;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StockHistoricalServiceImpl implements StockHistoricalService {

    private final StockPriceRepository stockPriceRepository;
    private final StockRepository stockRepository;
    private final FssApiClient fssApiClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public List<StockHistoricalDataResponse> getStockHistoricalData(StockHistoricalSearchRequest request) {
        String stockName = request.getStockName();
        LocalDate date = request.getDate();

        log.info("주식 과거 데이터 조회 시작 - 종목명: {}, 날짜: {}", stockName, date);

        // 1. FSS API에서 먼저 조회 (모든 관련 종목 검색)
        log.info("FSS API에서 조회 시작 - 모든 관련 종목 검색");
        List<FssStockPriceResponse.StockItem> apiResults = fssApiClient.getStockPriceByNameAndDate(stockName, date);

        if (apiResults.isEmpty()) {
            log.warn("FSS API에서 데이터를 찾을 수 없음 - 종목명: {}, 날짜: {}", stockName, date);
            // API에서 데이터가 없으면 DB에서 조회해보기
            List<StockPrice> dbResults = stockPriceRepository.findByNameContainingAndDate(stockName, date);
            if (!dbResults.isEmpty()) {
                log.info("DB에서 기존 데이터 조회 성공 - 조회된 건수: {}", dbResults.size());
                return dbResults.stream()
                        .map(StockHistoricalDataResponse::from)
                        .collect(Collectors.toList());
            }
            return List.of();
        }

        // 2. API 결과 처리 및 새로운 데이터만 DB에 저장
        List<StockHistoricalDataResponse> responses = new ArrayList<>();
        List<StockPrice> newStockPrices = new ArrayList<>();
        
        for (FssStockPriceResponse.StockItem item : apiResults) {
            try {
                // 종목명이 요청한 종목명을 포함하는지 확인
                if (!item.getItmsNm().contains(stockName)) {
                    continue; // 관련 없는 종목은 건너뛰기
                }

                // Stock 엔티티 생성 또는 조회
                Stock stock = getOrCreateStock(item.getSrtnCd(), item.getItmsNm());
                
                // StockPrice가 이미 DB에 존재하는지 확인
                LocalDate itemDate = LocalDate.parse(item.getBasDt(), DATE_FORMATTER);
                boolean exists = stockPriceRepository.existsByTickerAndDate(item.getSrtnCd(), itemDate);
                
                StockPrice stockPrice = convertToStockPrice(item, stock);
                
                // DB에 없는 경우에만 저장 리스트에 추가
                if (!exists) {
                    newStockPrices.add(stockPrice);
                    log.debug("새로운 주식 데이터 추가 - 종목: {}, 코드: {}", item.getItmsNm(), item.getSrtnCd());
                } else {
                    log.debug("기존 주식 데이터 발견 - 종목: {}, 코드: {}", item.getItmsNm(), item.getSrtnCd());
                }
                
                responses.add(StockHistoricalDataResponse.from(stockPrice));
                
            } catch (Exception e) {
                log.error("주식 데이터 변환/처리 실패 - 종목: {}, 오류: {}", item.getItmsNm(), e.getMessage());
                // 개별 항목 실패는 전체 처리를 중단하지 않음
            }
        }
        
        // 3. 새로운 StockPrice만 일괄 저장
        if (!newStockPrices.isEmpty()) {
            stockPriceRepository.saveAll(newStockPrices);
            log.info("새로운 주식 데이터 저장 완료 - 저장된 건수: {}", newStockPrices.size());
        }

        log.info("FSS API 조회 및 처리 완료 - 전체 응답 건수: {}, 새로 저장된 건수: {}", 
                responses.size(), newStockPrices.size());
        return responses;
    }

    /**
     * Stock 엔티티를 조회하거나 생성한다
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