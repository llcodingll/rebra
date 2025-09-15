package com.rebra.service;

import com.rebra.client.FssApiClient;
import com.rebra.dto.external.FssStockPriceResponse;
import com.rebra.dto.request.StockHistoricalSearchRequest;
import com.rebra.dto.response.StockHistoricalDataResponse;
import com.rebra.entity.StockPrice;
import com.rebra.exception.external.ExternalApiException;
import com.rebra.repository.StockPriceRepository;
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
    private final FssApiClient fssApiClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public List<StockHistoricalDataResponse> getStockHistoricalData(StockHistoricalSearchRequest request) {
        String stockName = request.getStockName();
        LocalDate date = request.getDate();

        log.info("주식 과거 데이터 조회 시작 - 종목명: {}, 날짜: {}", stockName, date);

        // 1. DB에서 먼저 조회 (정확한 이름으로)
        List<StockPrice> dbResults = stockPriceRepository.findByNameContainingAndDate(stockName, date);
        
        if (!dbResults.isEmpty()) {
            log.info("DB에서 데이터 조회 성공 - 조회된 건수: {}", dbResults.size());
            return dbResults.stream()
                    .map(StockHistoricalDataResponse::from)
                    .collect(Collectors.toList());
        }

        // 2. DB에 데이터가 없으면 FSS API에서 조회
        log.info("DB에 데이터가 없어 FSS API에서 조회 시작");
        List<FssStockPriceResponse.StockItem> apiResults = fssApiClient.getStockPriceByNameAndDate(stockName, date);

        if (apiResults.isEmpty()) {
            log.warn("FSS API에서도 데이터를 찾을 수 없음 - 종목명: {}, 날짜: {}", stockName, date);
            return List.of();
        }

        // 3. API 결과를 DB에 저장하고 응답 생성
        List<StockHistoricalDataResponse> responses = new ArrayList<>();
        
        for (FssStockPriceResponse.StockItem item : apiResults) {
            try {
                // 종목명이 요청한 종목명을 포함하는지 확인
                if (!item.getItmsNm().contains(stockName)) {
                    continue; // 관련 없는 종목은 건너뛰기
                }

                StockPrice stockPrice = convertToStockPrice(item);
                stockPriceRepository.save(stockPrice);
                
                responses.add(StockHistoricalDataResponse.from(stockPrice));
                
                log.debug("주식 데이터 저장 완료 - 종목: {}, 코드: {}", item.getItmsNm(), item.getSrtnCd());
                
            } catch (Exception e) {
                log.error("주식 데이터 변환/저장 실패 - 종목: {}, 오류: {}", item.getItmsNm(), e.getMessage());
                // 개별 항목 실패는 전체 처리를 중단하지 않음
            }
        }

        log.info("FSS API 조회 및 저장 완료 - 저장된 건수: {}", responses.size());
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