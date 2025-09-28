package com.rebra.service;

import com.google.common.util.concurrent.Striped;
import com.rebra.client.FssApiClient;
import com.rebra.dto.external.FssStockPriceResponse;
import com.rebra.dto.internal.StockPriceDto;
import com.rebra.entity.Stock;
import com.rebra.entity.StockPrice;
import com.rebra.exception.external.ExternalApiException;
import com.rebra.exception.stock.StockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartStockDataService {

    private final StockServiceImpl stockService;
    private final FssApiClient fssApiClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    // Striped Lock for stock-level synchronization
    private final Striped<Lock> stockLocks = Striped.lazyWeakLock(64);

    /**
     * 종목의 데이터를 효율적으로 확보 (스마트 캐싱)
     * Striped Lock을 사용한 종목별 동시성 제어
     */
    public void ensureDataAvailable(String stockCode, LocalDate requestStart, LocalDate requestEnd) {
        Lock lock = stockLocks.get(stockCode);
        log.info("Lock 획득 시도 - 종목: {}, 스레드: {}", stockCode, Thread.currentThread().getName());
        lock.lock();
        log.info("Lock 획득 성공 - 종목: {}, 스레드: {}", stockCode, Thread.currentThread().getName());
        
        try {
            // 트랜잭션과 비즈니스 로직은 StockServiceImpl에 위임
            stockService.ensureStockDataWithTransaction(stockCode, requestStart, requestEnd);
        } finally {
            log.info("Lock 해제 - 종목: {}, 스레드: {}", stockCode, Thread.currentThread().getName());
            
            // DB 반영 대기
            try {
                Thread.sleep(5000); // 5초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            lock.unlock();
        }
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
     * API를 통해 데이터 조회 및 저장 (구간 검색 사용)
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
            
            // 중복 체크 후 저장 (트랜잭션 있음)
            List<StockPriceDto> stockPriceDtos = stockPrices.stream()
                .map(StockPriceDto::from)
                .collect(Collectors.toList());
            List<StockPriceDto> savedPriceDtos = stockService.saveStockPricesWithDuplicateCheck(
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

    // isMatchingStock 메서드 제거: 종목코드로 정확하게 검색하므로 매칭 검증 불필요

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
     * 종목의 현재 데이터 범위를 가진 Stock 조회
     */
    public Optional<Stock> getStockWithDataRange(String stockCode) {
        return stockService.findStockByCodeOptional(stockCode)
            .filter(stock -> stock.getDataStartDate() != null && stock.getDataEndDate() != null);
    }

    /**
     * 종목의 데이터 통계 조회
     */
    public DataStats getDataStats(String stockCode) {
        return stockService.getStockDataStats(stockCode);
    }

    public record DataStats(String stockCode, Long dataCount, LocalDate startDate, LocalDate endDate) {}
}