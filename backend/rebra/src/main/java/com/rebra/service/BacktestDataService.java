package com.rebra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.dto.backtest.BacktestRequest;
import com.rebra.dto.backtest.BacktestStockDto;
import com.rebra.dto.internal.BacktestRecordDto;
import com.rebra.dto.request.BacktestCreateRequest;
import com.rebra.dto.response.BacktestListResponse;
import com.rebra.dto.response.BacktestResultResponse;
import com.rebra.dto.response.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * 백테스트 레코드를 생성하고 PENDING 상태로 저장한다
     */
    @Transactional
    public Long createInitialBacktestRecord(Long userId, BacktestCreateRequest request) {
        // 1. 요청 데이터 유효성 검사
        if (!request.isValid()) {
            throw BacktestException.invalidRequest();
        }

        // 2. BacktestRecord 생성 및 저장 (직접 인라인 처리)
        // BacktestRecord 생성 및 저장
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
        
        // BacktestStock 저장
        saveBacktestStocks(backtestRecord, request);
        
        log.info("백테스트 레코드 생성 완료: backtestId={}, status=PENDING", backtestRecord.getId());
        return backtestRecord.getId();
    }



    /**
     * 종목별 데이터 가용성을 검증한다
     * 거래정지 종목은 0원 처리하여 백테스트 진행 가능
     */
    public void validateDataAvailability(List<String> tickers, LocalDate startDate, LocalDate endDate) {
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
    public BacktestRequest createBacktestRequest(BacktestRecordDto recordDto, BacktestCreateRequest request, 
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
        backtestRequest.setBacktestId(recordDto.getId());
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

    /**
     * BacktestRecord DTO 조회
     */
    @Transactional(readOnly = true)
    public BacktestRecordDto findBacktestRecordDto(Long backtestId) {
        BacktestRecord record = backtestRecordRepository.findById(backtestId)
                .orElseThrow(() -> BacktestException.notFound());
        return BacktestRecordDto.from(record);
    }
    
    /**
     * BacktestRecord 엔티티 조회
     */
    @Transactional(readOnly = true)
    public BacktestRecord findBacktestRecord(Long backtestId) {
        return backtestRecordRepository.findById(backtestId)
                .orElseThrow(() -> BacktestException.notFound());
    }
    
    /**
     * BacktestRecord 생성 및 BacktestStock 저장
     */
    @Transactional
    public Long createBacktestRecordWithStocks(Long userId, BacktestCreateRequest request) {
        // 요청 데이터 유효성 검사
        if (!request.isValid()) {
            throw BacktestException.invalidRequest();
        }
        
        // BacktestRecord 생성 및 저장
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
        
        // BacktestStock 저장
        saveBacktestStocks(backtestRecord, request);
        
        return backtestRecord.getId();
    }
    
    /**
     * BacktestRecord 상태 업데이트
     */
    @Transactional
    public void updateBacktestStatus(Long backtestId, BacktestRecord.BacktestStatus status) {
        BacktestRecord record = backtestRecordRepository.findById(backtestId)
                .orElseThrow(() -> BacktestException.notFound());
        record.updateStatus(status);
        backtestRecordRepository.save(record);
    }
    
    /**
     * BacktestRecord 상태 및 에러 메시지 업데이트
     */
    @Transactional
    public void updateBacktestStatus(Long backtestId, BacktestRecord.BacktestStatus status, String errorMessage) {
        BacktestRecord record = backtestRecordRepository.findById(backtestId)
                .orElseThrow(() -> BacktestException.notFound());
        record.updateStatus(status, errorMessage);
        backtestRecordRepository.save(record);
    }
    
    /**
     * 백테스트 종목 정보를 저장한다
     */
    private void saveBacktestStocks(BacktestRecord backtestRecord, BacktestCreateRequest request) {
        List<BacktestStock> backtestStocks = new ArrayList<>();
        
        for (BacktestCreateRequest.BacktestStockRequest stockRequest : request.getStocks()) {
            // Stock 엔티티 조회
            Stock stock = stockRepository.findByStockCode(stockRequest.getTicker())
                .orElseThrow(() -> BacktestException.invalidRequest());
            
            // BacktestStock 엔티티 생성
            BacktestStock backtestStock = BacktestStock.builder()
                    .backtestRecord(backtestRecord)
                    .stock(stock)
                    .targetWeight(stockRequest.getWeight().doubleValue())
                    .thresholdPercentage(stockRequest.getThresholdPercentage())
                    .build();
            
            backtestStocks.add(backtestStock);
        }
        
        // 일괄 저장
        backtestStockRepository.saveAll(backtestStocks);
        
        log.info("백테스트 종목 정보 저장 완료 - backtestId: {}, 종목수: {}", 
                backtestRecord.getId(), request.getStocks().size());
    }

    /**
     * 사용자의 백테스트 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<BacktestListResponse> getBacktestList(Long userId, Pageable pageable) {
        Page<BacktestRecord> records = backtestRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        Page<BacktestListResponse> dtoPage = records.map(BacktestListResponse::from);
        return PageResponse.from(dtoPage);
    }

    /**
     * 백테스트 상세 결과 조회
     */
    @Transactional(readOnly = true)  
    public BacktestResultResponse getBacktestResult(Long backtestId) {
        BacktestRecord record = backtestRecordRepository.findById(backtestId)
                .orElseThrow(() -> BacktestException.notFound());

        List<BacktestStock> portfolioStocks = backtestStockRepository.findByBacktestRecordWithStock(record);

        return BacktestResultResponse.from(record, portfolioStocks);
    }

    /**
     * 백테스트 삭제
     */
    @Transactional
    public void deleteBacktest(Long userId, Long backtestId) {
        BacktestRecord record = backtestRecordRepository.findByUserIdAndId(userId, backtestId)
                .orElseThrow(() -> BacktestException.notFound());

        // 진행 중인 백테스트는 삭제 불가
        if (record.isInProgress()) {
            throw BacktestException.deleteNotAllowed();
        }

        // BacktestRecord 삭제 (cascade 설정으로 BacktestStock은 자동 삭제됨)
        // 상세 정보는 JSON으로 저장되므로 별도 삭제 불필요
        backtestRecordRepository.delete(record);
        log.info("백테스트 삭제 완료: backtestId={}", backtestId);
    }

    /**
     * 백테스트 결과 처리 (Kafka Consumer에서 호출)
     */
    @Transactional
    public void processBacktestResult(Map<String, Object> responseMap) {
        Long backtestId = null;
        try {
            // 이미 Map으로 받아왔으므로 변환 불필요
            backtestId = Long.valueOf(responseMap.get("backtest_id").toString());
            String status = responseMap.get("status").toString();

            BacktestRecord record = backtestRecordRepository.findById(backtestId)
                    .orElseThrow(() -> BacktestException.notFound());

            if ("COMPLETED".equals(status)) {
                // 성공적으로 완료된 경우
                try {
                    updateBacktestResults(record, responseMap);
                    record.updateStatus(BacktestRecord.BacktestStatus.COMPLETED);
                    log.info("백테스트 결과 처리 완료: backtestId={}", backtestId);
                } catch (Exception e) {
                    // 결과 처리 중 오류 발생 시 FAILED로 변경
                    // 상세 에러는 로그에만 기록, 저장은 간단한 메시지로
                    log.error("백테스트 결과 처리 실패: backtestId={}", backtestId, e);
                    record.updateStatus(BacktestRecord.BacktestStatus.FAILED, "메인 서버 처리 오류");
                }
            } else {
                // 실패한 경우
                String errorMessage = responseMap.getOrDefault("error_message", "알 수 없는 오류").toString();
                record.updateStatus(BacktestRecord.BacktestStatus.FAILED, errorMessage);
                log.warn("백테스트 실패: backtestId={}, error={}", backtestId, errorMessage);
            }

            backtestRecordRepository.save(record);

        } catch (Exception e) {
            log.error("백테스트 결과 처리 중 오류 발생: backtestId={}", backtestId, e);
            if (backtestId != null) {
                updateBacktestStatusToFailed(backtestId, "메인 서버 오류");
            }
        }
    }

    private void updateBacktestResults(BacktestRecord record, Map<String, Object> responseMap) {
        Map<String, Object> summaryMap = (Map<String, Object>) responseMap.get("summary");
        List<Map<String, Object>> detailsList = (List<Map<String, Object>>) responseMap.get("details");

        // BacktestRecord에 결과 업데이트
        Integer finalValue = Double.valueOf(summaryMap.get("final_value").toString()).intValue();
        Double totalReturn = Double.valueOf(summaryMap.get("total_return").toString());
        Double buyHoldReturn = Double.valueOf(summaryMap.get("buy_hold_return").toString());
        Double excessReturn = totalReturn - buyHoldReturn;
        Double periodGrowthRate = Double.valueOf(summaryMap.get("period_growth_rate").toString());
        Integer rebalancingCount = Double.valueOf(summaryMap.get("rebalancing_count").toString()).intValue();
        Integer totalFee = Double.valueOf(summaryMap.get("total_fee").toString()).intValue();
        Integer totalBorrowingCost = summaryMap.get("total_borrowing_cost") != null ?
                Double.valueOf(summaryMap.get("total_borrowing_cost").toString()).intValue() : 0;
        Integer maxBorrowingAmount = summaryMap.get("max_borrowing_amount") != null ?
                Double.valueOf(summaryMap.get("max_borrowing_amount").toString()).intValue() : null;
        Integer minCashBalance = summaryMap.get("min_cash_balance") != null ?
                Double.valueOf(summaryMap.get("min_cash_balance").toString()).intValue() : null;
        Double maxDrawdown = summaryMap.get("max_drawdown") != null ?
                Double.valueOf(summaryMap.get("max_drawdown").toString()) : null;
        Double volatility = summaryMap.get("volatility") != null ?
                Double.valueOf(summaryMap.get("volatility").toString()) : null;
        Double sharpeRatio = summaryMap.get("sharpe_ratio") != null ?
                Double.valueOf(summaryMap.get("sharpe_ratio").toString()) : null;
        Double timeWeightedReturn = summaryMap.get("time_weighted_return") != null ?
                Double.valueOf(summaryMap.get("time_weighted_return").toString()) : null;
        record.updateResults(finalValue, totalReturn, buyHoldReturn, excessReturn, periodGrowthRate,
                rebalancingCount, totalFee, totalBorrowingCost, maxBorrowingAmount, minCashBalance,
                maxDrawdown, volatility, sharpeRatio, timeWeightedReturn);

        // 백테스트 서버의 상세 정보를 그대로 JSON으로 저장 (파싱하지 않음)
        try {
            String detailsJson = objectMapper.writeValueAsString(detailsList);
            record.setDetailsJson(detailsJson);
            log.info("백테스트 상세 정보 JSON 저장 완료: backtestId={}, 상세 기록 수={}", 
                    record.getId(), detailsList.size());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("백테스트 상세 정보 JSON 변환 실패: backtestId={}", record.getId(), e);
            throw new RuntimeException("백테스트 상세 정보 JSON 변환 실패", e);
        }
        
        // 저장 검증 로그
        if (record.hasDetails()) {
            log.info("백테스트 상세 정보 검증 성공: backtestId={}, detailsJson 길이={}", 
                    record.getId(), record.getDetailsJson().length());
        } else {
            log.warn("백테스트 상세 정보 검증 실패: backtestId={}, detailsJson이 비어있음", 
                    record.getId());
        }
    }

    /**
     * 백테스트 상태를 FAILED로 업데이트 (별도 트랜잭션)
     * 다른 트랜잭션 실패와 무관하게 상태 업데이트를 보장한다
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateBacktestStatusToFailed(Long backtestId, String errorMessage) {
        try {
            BacktestRecord record = backtestRecordRepository.findById(backtestId).orElse(null);
            if (record != null) {
                // 항상 간단한 메시지만 저장
                record.updateStatus(BacktestRecord.BacktestStatus.FAILED, "메인 서버 오류");
                backtestRecordRepository.save(record);
                log.info("백테스트 상태를 FAILED로 변경: backtestId={}", backtestId);
            } else {
                log.warn("백테스트 레코드를 찾을 수 없어 상태 변경 실패: backtestId={}", backtestId);
            }
        } catch (Exception e) {
            log.error("백테스트 상태 변경 실패: backtestId={}", backtestId, e);
        }
    }
}