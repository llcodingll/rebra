package com.rebra.service;

import com.rebra.client.FssApiClient;
import com.rebra.dto.external.FssStockPriceResponse;
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
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SmartStockDataService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final FssApiClient fssApiClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 종목의 데이터를 효율적으로 확보 (스마트 캐싱)
     */
    public void ensureDataAvailable(String stockCode, LocalDate requestStart, LocalDate requestEnd) {
        log.info("데이터 확보 요청 - 종목: {}, 범위: {} ~ {}", stockCode, requestStart, requestEnd);

        // 1. Stock 엔티티 조회 또는 생성
        Stock stock = stockRepository.findByStockCode(stockCode)
            .orElseGet(() -> createNewStock(stockCode));

        // 2. 이미 데이터가 충분한지 확인
        if (stock.hasDataInRange(requestStart, requestEnd)) {
            log.info("캐싱된 데이터로 충분 - 종목: {}", stockCode);
            return;
        }

        // 3. 필요한 구간 직접 계산 및 조회
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

        // 4. Stock 엔티티의 데이터 범위 업데이트
        if (dataFetched) {
            stock.updateDataRange(requestStart, requestEnd);
            stockRepository.save(stock);
        }

        log.info("데이터 확보 완료 - 종목: {}", stockCode);
    }

    /**
     * 여러 종목을 배치로 처리
     */
    public void ensureBatchDataAvailable(List<String> stockCodes, LocalDate requestStart, LocalDate requestEnd) {
        log.info("배치 데이터 확보 시작 - 종목 수: {}, 범위: {} ~ {}", 
            stockCodes.size(), requestStart, requestEnd);

        for (String stockCode : stockCodes) {
            try {
                ensureDataAvailable(stockCode, requestStart, requestEnd);
            } catch (Exception e) {
                log.error("종목 데이터 확보 실패 - 종목: {}, 오류: {}", stockCode, e.getMessage());
                // 개별 종목 실패는 전체를 중단하지 않음
            }
        }

        log.info("배치 데이터 확보 완료 - 종목 수: {}", stockCodes.size());
    }

    /**
     * 새로운 Stock 엔티티 생성
     */
    private Stock createNewStock(String stockCode) {
        log.info("새로운 Stock 엔티티 생성 - 종목코드: {}", stockCode);

        Stock newStock = Stock.builder()
            .stockCode(stockCode)
            .stockName(stockCode) // 임시로 코드와 동일하게 설정
            .stockType("STOCK")
            .isActive(true)
            .build();

        return stockRepository.save(newStock);
    }

    /**
     * API를 통해 데이터 조회 및 저장
     */
    private void fetchDataForPeriod(Stock stock, LocalDate start, LocalDate end) {
        log.info("API 데이터 조회 - 종목: {}, 범위: {} ~ {}", stock.getStockCode(), start, end);

        LocalDate current = start;
        int savedCount = 0;

        while (!current.isAfter(end)) {
            try {
                // FSS API 호출 (종목명으로 검색)
                List<FssStockPriceResponse.StockItem> items = 
                    fssApiClient.getStockPriceByNameAndDate(stock.getStockName(), current);

                // 매칭되는 종목 데이터 저장
                for (FssStockPriceResponse.StockItem item : items) {
                    if (isMatchingStock(item, stock)) {
                        StockPrice stockPrice = convertToStockPrice(item, stock);
                        stockPriceRepository.save(stockPrice);
                        
                        // Stock 정보 업데이트 (실제 데이터에서 얻은 정보)
                        updateStockInfoFromApi(stock, item);
                        
                        savedCount++;
                        break; // 해당 종목 찾으면 중단
                    }
                }
            } catch (Exception e) {
                log.debug("데이터 조회 실패 (주말/공휴일 가능) - 날짜: {}, 오류: {}", 
                    current, e.getMessage());
            }

            current = current.plusDays(1);
        }

        log.info("API 데이터 저장 완료 - 종목: {}, 저장된 건수: {}", 
            stock.getStockCode(), savedCount);
    }

    /**
     * API 응답 종목이 요청 종목과 일치하는지 확인
     */
    private boolean isMatchingStock(FssStockPriceResponse.StockItem item, Stock stock) {
        // 종목 코드로 매칭 (6자리)
        if (item.getSrtnCd() != null && item.getSrtnCd().equals(stock.getStockCode())) {
            return true;
        }
        
        // 종목명으로 매칭 (부분 일치)
        if (item.getItmsNm() != null && stock.getStockName() != null) {
            return item.getItmsNm().contains(stock.getStockName()) ||
                   stock.getStockName().contains(item.getItmsNm());
        }
        
        return false;
    }

    /**
     * API 응답에서 Stock 정보 업데이트
     */
    private void updateStockInfoFromApi(Stock stock, FssStockPriceResponse.StockItem item) {
        // 종목명이 임시값이면 실제 종목명으로 업데이트
        if (stock.getStockName().equals(stock.getStockCode()) && 
            StringUtils.hasText(item.getItmsNm())) {
            
            // 리플렉션을 사용하지 않고 새로운 Stock 객체 생성 후 저장
            // 실제로는 Setter를 추가하거나 다른 방법 사용
            log.info("종목명 업데이트 필요 - 코드: {}, 기존: {}, 신규: {}", 
                stock.getStockCode(), stock.getStockName(), item.getItmsNm());
        }
    }

    /**
     * StockPrice 엔티티 변환 (Stock 연관관계 포함)
     */
    private StockPrice convertToStockPrice(FssStockPriceResponse.StockItem item, Stock stock) {
        try {
            return StockPrice.builder()
                .stock(stock) // 연관관계 설정
                .ticker(stock.getStockCode()) // 호환성 유지
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

    private BigDecimal parsePrice(String priceStr) {
        if (!StringUtils.hasText(priceStr)) {
            return BigDecimal.ZERO;
        }
        try {
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

    /**
     * 종목의 현재 데이터 범위를 가진 Stock 조회
     */
    @Transactional(readOnly = true)
    public Optional<Stock> getStockWithDataRange(String stockCode) {
        return stockRepository.findByStockCode(stockCode)
            .filter(stock -> stock.getDataStartDate() != null && stock.getDataEndDate() != null);
    }

    /**
     * 종목의 데이터 통계 조회
     */
    @Transactional(readOnly = true)
    public DataStats getDataStats(String stockCode) {
        Optional<Stock> stockOpt = stockRepository.findByStockCode(stockCode);
        if (stockOpt.isEmpty()) {
            return new DataStats(stockCode, 0L, null, null);
        }

        Stock stock = stockOpt.get();
        Long dataCount = stockPriceRepository.countByStock(stock);
        
        return new DataStats(
            stockCode, 
            dataCount, 
            stock.getDataStartDate(), 
            stock.getDataEndDate()
        );
    }

    public record DataStats(String stockCode, Long dataCount, LocalDate startDate, LocalDate endDate) {}
}