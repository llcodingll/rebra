package com.rebra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.dto.backtest.BacktestRequest;
import com.rebra.dto.backtest.BacktestStockDto;
import com.rebra.dto.request.BacktestCreateRequest;
import com.rebra.entity.BacktestRecord;
import com.rebra.entity.BacktestStock;
import com.rebra.entity.Stock;
import com.rebra.entity.StockPrice;
import com.rebra.entity.User;
import com.rebra.exception.backtest.BacktestException;
import com.rebra.repository.BacktestRecordRepository;
import com.rebra.repository.BacktestStockRepository;
import com.rebra.repository.StockPriceRepository;
import com.rebra.repository.StockRepository;
import com.rebra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BacktestDataService {

    private final BacktestRecordRepository backtestRecordRepository;
    private final BacktestStockRepository backtestStockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final SmartStockDataService smartStockDataService;

    @Qualifier("backtestRequestKafkaTemplate")
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    /**
     * 백테스트 레코드를 생성하고 PENDING 상태로 저장한다 (별도 트랜잭션)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createInitialBacktestRecord(Long userId, BacktestCreateRequest request) {
        // 1. 요청 데이터 유효성 검사
        if (!request.isValid()) {
            throw BacktestException.invalidRequest();
        }

        // 2. BacktestRecord 생성 및 저장 (PENDING 상태)
        User userProxy = userRepository.getReferenceById(userId);
        BacktestRecord backtestRecord = BacktestRecord.builder()
                .user(userProxy)
                .testName(request.getTestName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rebalancingType(request.getRebalancingType())
                .rebalancingPeriod(request.getRebalancingPeriod())
                .status(BacktestRecord.BacktestStatus.PENDING)
                .build();

        backtestRecord = backtestRecordRepository.save(backtestRecord);

        // 3. 백테스트 종목 정보 저장
        saveBacktestStocks(backtestRecord, request);

        log.info("백테스트 레코드 생성 완료: backtestId={}, status=PENDING", backtestRecord.getId());
        return backtestRecord.getId();
    }

    /**
     * 백테스트 데이터를 처리하고 Kafka로 전송한다 (별도 트랜잭션)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processBacktestData(Long backtestId, BacktestCreateRequest request) {
        // 1. BacktestRecord 조회
        BacktestRecord backtestRecord = backtestRecordRepository.findById(backtestId)
                .orElseThrow(() -> BacktestException.notFound());

        try {
            // 2. 종목별 데이터 확보 (스마트 캐싱)
            List<String> tickers = request.getStocks().stream()
                    .map(BacktestCreateRequest.BacktestStockRequest::getTicker)
                    .toList();

            log.info("백테스트 데이터 확보 시작 - backtestId={}, 종목 수: {}, 기간: {} ~ {}", 
                backtestId, tickers.size(), request.getStartDate(), request.getEndDate());

            smartStockDataService.ensureBatchDataAvailable(
                tickers, 
                request.getStartDate(), 
                request.getEndDate()
            );
            log.info("백테스트 데이터 확보 완료 - backtestId={}", backtestId);

            // 3. 데이터 가용성 최종 검증
            validateDataAvailability(tickers, request.getStartDate(), request.getEndDate());

            // 4. Kafka로 백테스트 요청 전송
            BacktestRequest backtestRequest = createBacktestRequest(backtestRecord, request, tickers, null);
            kafkaTemplate.send("backtest-request", backtestRecord.getId().toString(), backtestRequest);
            log.info("백테스트 요청 전송 완료: backtestId={}", backtestRecord.getId());

            // 5. 상태를 PROCESSING으로 변경
            backtestRecord.updateStatus(BacktestRecord.BacktestStatus.PROCESSING);
            backtestRecordRepository.save(backtestRecord);
            log.info("백테스트 상태 변경: backtestId={}, status=PROCESSING", backtestId);

        } catch (Exception e) {
            log.error("백테스트 데이터 처리 실패: backtestId={}", backtestId, e);
            
            String errorMessage;
            if (e.getMessage() != null && e.getMessage().contains("데이터")) {
                errorMessage = "주식 데이터 수집 실패: " + e.getMessage();
            } else if (e.getMessage() != null && e.getMessage().contains("kafka") || 
                       e.getMessage() != null && e.getMessage().toLowerCase().contains("kafka")) {
                errorMessage = "백테스트 요청 전송 실패: " + e.getMessage();
            } else {
                errorMessage = "백테스트 처리 중 오류 발생: " + e.getMessage();
            }
            
            backtestRecord.updateStatus(BacktestRecord.BacktestStatus.FAILED, errorMessage);
            backtestRecordRepository.save(backtestRecord);
            
            // 예외를 다시 던지지 않고 로그만 남김 (사용자에게는 backtestId 반환)
            log.warn("백테스트 처리 실패했지만 ID는 반환됨: backtestId={}, error={}", backtestId, errorMessage);
        }
    }

    /**
     * 백테스트 종목 정보를 저장한다
     */
    private void saveBacktestStocks(BacktestRecord backtestRecord, BacktestCreateRequest request) {
        List<BacktestStock> backtestStocks = new ArrayList<>();
        
        for (BacktestCreateRequest.BacktestStockRequest stockRequest : request.getStocks()) {
            // Stock 엔티티 조회 (사용자가 미리 historical 조회를 통해 생성되어 있어야 함)
            Stock stock = stockRepository.findByStockCode(stockRequest.getTicker())
                .orElseThrow(() -> BacktestException.invalidRequest());
            
            // BacktestStock 엔티티 생성
            BacktestStock backtestStock = BacktestStock.builder()
                    .backtestRecord(backtestRecord)
                    .stock(stock)
                    .targetWeight(BigDecimal.valueOf(stockRequest.getWeight()))
                    .thresholdPercentage(stockRequest.getThresholdPercentage() != null ? 
                            BigDecimal.valueOf(stockRequest.getThresholdPercentage()) : null)
                    .build();
            
            backtestStocks.add(backtestStock);
        }
        
        // 일괄 저장
        backtestStockRepository.saveAll(backtestStocks);
        
        log.info("백테스트 종목 정보 저장 완료 - backtestId: {}, 종목수: {}", 
                backtestRecord.getId(), request.getStocks().size());
    }

    /**
     * 종목별 데이터 가용성을 검증한다
     * 거래정지 종목은 0원 처리하여 백테스트 진행 가능
     */
    private void validateDataAvailability(List<String> tickers, LocalDate startDate, LocalDate endDate) {
        List<Object[]> dataAvailability = stockPriceRepository.findDataAvailabilityByTickers(tickers);
        
        if (dataAvailability.isEmpty()) {
            throw BacktestException.invalidRequest();
        }

        boolean hasAnyValidData = false;
        
        for (Object[] data : dataAvailability) {
            String ticker = (String) data[0];
            LocalDate dataStartDate = (LocalDate) data[1];
            LocalDate dataEndDate = (LocalDate) data[2];
            Long dataCount = (Long) data[3];
            
            // 요청 기간에 데이터가 전혀 없는 경우 (거래정지 가능성)
            if (dataEndDate.isBefore(startDate) || dataStartDate.isAfter(endDate)) {
                log.info("종목 {}: 요청 기간에 데이터가 없음 - 거래정지로 간주하여 0원 처리 (데이터 보유 기간: {} ~ {})", 
                    ticker, dataStartDate, dataEndDate);
            } else {
                // 일부 기간에라도 데이터가 있으면 유효한 데이터로 간주
                hasAnyValidData = true;
            }
        }
        
        // 모든 종목이 전혀 데이터가 없는 경우에만 에러
        if (!hasAnyValidData && tickers.size() > 0) {
            log.error("모든 종목이 요청 기간에 데이터가 없습니다");
            throw BacktestException.invalidRequest();
        }
        
        log.info("백테스트 데이터 가용성 검증 완료 - 종목수: {} (거래정지 종목은 0원 처리)", tickers.size());
    }

    /**
     * Kafka로 전송할 백테스트 요청 객체를 생성한다
     */
    private BacktestRequest createBacktestRequest(BacktestRecord record, BacktestCreateRequest request, 
                                                     List<String> tickers, List<LocalDate> unusedTradingDates) {
        // 모든 리밸런싱 타입에 대해 전체 기간 데이터 조회
        List<StockPrice> stockPrices = stockPriceRepository.findByTickersAndDateRange(
                tickers, request.getStartDate(), request.getEndDate());
        
        // 실제 거래일 추출 (중복 제거 및 정렬)
        List<LocalDate> actualDates = stockPrices.stream()
                .map(StockPrice::getDate)
                .distinct()
                .sorted()
                .toList();
        
        log.info("{} 리밸런싱 - 전체 기간 데이터 조회: {}건 ({}일, {}종목)", 
                request.getRebalancingType(), stockPrices.size(), actualDates.size(), tickers.size());

        // 날짜별 가격 데이터 (맵 기반 구조) 먼저 생성
        Map<String, Map<String, Double>> dailyPrices = createDailyPricesMap(tickers, actualDates, stockPrices);

        // dailyPrices에서 리밸런싱 날짜 추출
        List<LocalDate> rebalancingDates = extractRebalancingDatesFromMap(
                dailyPrices, 
                request.getRebalancingType(), 
                request.getRebalancingPeriod()
        );

        // 백테스트 요청 객체 생성
        BacktestRequest backtestRequest = new BacktestRequest();
        backtestRequest.setBacktestId(record.getId());
        backtestRequest.setStartDate(request.getStartDate());
        backtestRequest.setEndDate(request.getEndDate());
        
        // enum 매핑: 메인 서버의 enum을 백테스트 서버의 enum으로 변환
        backtestRequest.setRebalancingType(convertToBacktestRebalancingType(request.getRebalancingType()));
        backtestRequest.setRebalancingPeriod(convertToBacktestRebalancingPeriod(request.getRebalancingPeriod()));

        // 리밸런싱 날짜 설정
        backtestRequest.setRebalancingDates(rebalancingDates);
        if (request.getRebalancingType() == BacktestRecord.RebalancingType.PERIODIC) {
            log.info("PERIODIC 리밸런싱 날짜 설정: {}개 ({})", 
                    rebalancingDates != null ? rebalancingDates.size() : 0, rebalancingDates);
        }

        // 종목 정보
        List<BacktestStockDto> stocks = request.getStocks().stream()
                .map(stock -> new BacktestStockDto(
                        stock.getTicker(),
                        stock.getWeight(),
                        stock.getThresholdPercentage(),
                        stock.getShares()
                ))
                .toList();
        backtestRequest.setStocks(stocks);

        // 날짜별 가격 데이터 설정
        backtestRequest.setDailyPrices(dailyPrices);

        return backtestRequest;
    }

    /**
     * 메인 서버의 RebalancingType을 백테스트 서버의 RebalancingType으로 변환
     */
    private com.rebra.enums.RebalancingType convertToBacktestRebalancingType(BacktestRecord.RebalancingType mainType) {
        if (mainType == null) {
            return null;
        }
        
        return switch (mainType) {
            case THRESHOLD -> com.rebra.enums.RebalancingType.THRESHOLD;
            case PERIODIC -> com.rebra.enums.RebalancingType.PERIODIC;
        };
    }

    /**
     * 메인 서버의 RebalancingPeriod를 백테스트 서버의 RebalancingPeriod로 변환
     */
    private com.rebra.enums.RebalancingPeriod convertToBacktestRebalancingPeriod(BacktestRecord.RebalancingPeriod mainPeriod) {
        if (mainPeriod == null) {
            return null;
        }
        
        return switch (mainPeriod) {
            case MONTHLY -> com.rebra.enums.RebalancingPeriod.MONTHLY;
            case QUARTERLY -> com.rebra.enums.RebalancingPeriod.QUARTERLY;
        };
    }

    /**
     * 리밸런싱 유형에 따라 리밸런싱 날짜 목록을 추출한다
     */
    private List<LocalDate> extractRebalancingDatesFromMap(
            Map<String, Map<String, Double>> dailyPrices,
            BacktestRecord.RebalancingType rebalancingType,
            BacktestRecord.RebalancingPeriod rebalancingPeriod) {
        
        if (rebalancingType == BacktestRecord.RebalancingType.THRESHOLD) {
            return Collections.emptyList();
        }
        
        if (dailyPrices == null || dailyPrices.isEmpty()) {
            return Collections.emptyList();
        }
        
        // dailyPrices의 키(날짜 문자열)에서 월말 날짜 찾기
        Map<YearMonth, LocalDate> monthEndDates = dailyPrices.keySet().stream()
                .map(LocalDate::parse)
                .collect(Collectors.toMap(
                        date -> YearMonth.from(date),
                        date -> date,
                        (existing, replacement) -> 
                                existing.isAfter(replacement) ? existing : replacement // 각 월의 가장 늦은 날짜
                ));
        
        if (rebalancingPeriod == BacktestRecord.RebalancingPeriod.MONTHLY) {
            // 모든 월말 거래일 반환
            return monthEndDates.values().stream()
                    .sorted()
                    .toList();
        }
        
        if (rebalancingPeriod == BacktestRecord.RebalancingPeriod.QUARTERLY) {
            // 분기말(3,6,9,12월)만 반환
            return monthEndDates.entrySet().stream()
                    .filter(entry -> {
                        int month = entry.getKey().getMonthValue();
                        return month == 3 || month == 6 || month == 9 || month == 12;
                    })
                    .map(Map.Entry::getValue)
                    .sorted()
                    .toList();
        }
        
        return Collections.emptyList();
    }

    /**
     * 날짜별 종목 가격 맵을 생성한다
     * 거래정지 종목은 0.0으로 처리
     */
    private Map<String, Map<String, Double>> createDailyPricesMap(
            List<String> tickers, 
            List<LocalDate> tradingDates,
            List<StockPrice> stockPrices) {
        
        Map<String, Map<String, Double>> dailyPricesMap = new LinkedHashMap<>();
        
        // 모든 거래일 초기화 (모든 종목을 0.0으로 초기화 - 거래정지 기본값)
        for (LocalDate date : tradingDates) {
            Map<String, Double> dayPrices = new HashMap<>();
            for (String ticker : tickers) {
                dayPrices.put(ticker, 0.0);  // null 대신 0.0으로 초기화
            }
            dailyPricesMap.put(date.toString(), dayPrices);
        }
        
        // 실제 가격 데이터로 업데이트
        int actualDataCount = 0;
        Map<String, Integer> tickerDataCount = new HashMap<>();
        
        for (StockPrice price : stockPrices) {
            String dateKey = price.getDate().toString();
            if (dailyPricesMap.containsKey(dateKey)) {
                dailyPricesMap.get(dateKey).put(
                        price.getTicker(), 
                        price.getClosePrice().doubleValue()
                );
                actualDataCount++;
                tickerDataCount.merge(price.getTicker(), 1, Integer::sum);
            }
        }
        
        // 거래정지 종목 로깅
        int totalDataPoints = tradingDates.size() * tickers.size();
        int suspendedDataPoints = totalDataPoints - actualDataCount;
        
        if (suspendedDataPoints > 0) {
            log.info("거래정지 처리된 데이터포인트: {}개 (0원으로 설정)", suspendedDataPoints);
            
            // 종목별 거래정지 비율 로깅
            for (String ticker : tickers) {
                int actualCount = tickerDataCount.getOrDefault(ticker, 0);
                int expectedCount = tradingDates.size();
                int suspendedCount = expectedCount - actualCount;
                
                if (suspendedCount > 0) {
                    double suspendedRatio = (double) suspendedCount / expectedCount * 100;
                    log.info("종목 {}: {}일 중 {}일 거래정지 ({}%)", 
                            ticker, expectedCount, suspendedCount, String.format("%.1f", suspendedRatio));
                }
            }
        }
        
        log.info("일별 가격 맵 생성 완료 - {}일 × {}종목 = {}개 데이터포인트 (실제 데이터: {}개, 거래정지: {}개)", 
                tradingDates.size(), tickers.size(), totalDataPoints, actualDataCount, suspendedDataPoints);
        
        return dailyPricesMap;
    }
}