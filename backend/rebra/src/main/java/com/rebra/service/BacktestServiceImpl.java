package com.rebra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.dto.request.BacktestCreateRequest;
import com.rebra.dto.response.BacktestListResponse;
import com.rebra.dto.response.BacktestResultResponse;
import com.rebra.dto.response.BacktestValidationResponse;
import com.rebra.entity.BacktestDetail;
import com.rebra.entity.BacktestRecord;
import com.rebra.entity.BacktestStock;
import com.rebra.entity.Stock;
import com.rebra.entity.StockPrice;
import com.rebra.entity.User;
import com.rebra.exception.backtest.BacktestException;
import com.rebra.repository.BacktestDetailRepository;
import com.rebra.repository.BacktestRecordRepository;
import com.rebra.repository.BacktestStockRepository;
import com.rebra.repository.StockPriceRepository;
import com.rebra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BacktestServiceImpl implements BacktestService {

    private final BacktestRecordRepository backtestRecordRepository;
    private final BacktestDetailRepository backtestDetailRepository;
    private final BacktestStockRepository backtestStockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockRepository stockRepository;

    @Qualifier("backtestRequestKafkaTemplate")
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public BacktestValidationResponse validateBacktestRequest(User user, BacktestCreateRequest request) {
        try {
            // 1. 기본 유효성 검사
            if (!request.isValid()) {
                return BacktestValidationResponse.failure(
                        List.of("백테스트 요청 데이터가 유효하지 않습니다"), 
                        List.of());
            }

            // 2. 종목 정보 추출
            List<String> tickers = request.getStocks().stream()
                    .map(BacktestCreateRequest.BacktestStockRequest::getTicker)
                    .toList();

            // 3. 종목별 데이터 가용성 검증
            List<BacktestValidationResponse.StockValidation> stockValidations = 
                    validateStockDataAvailability(tickers, request.getStartDate(), request.getEndDate());

            // 4. 전체 검증 결과 판정
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            boolean hasInvalidStock = stockValidations.stream().anyMatch(stock -> !stock.isValid());
            if (hasInvalidStock) {
                errors.add("일부 종목의 데이터가 부족하여 백테스트를 실행할 수 없습니다");
                return BacktestValidationResponse.failure(errors, stockValidations);
            }

            // 5. 리밸런싱 거래일 확인
            List<LocalDate> tradingDates = getTradingDatesForRebalancing(
                    request.getRebalancingType(), request.getRebalancingPeriod(),
                    request.getStartDate(), request.getEndDate());

            if (tradingDates.isEmpty()) {
                errors.add("지정된 기간에 거래일이 없습니다");
                return BacktestValidationResponse.failure(errors, stockValidations);
            }

            // 6. 검증 요약 정보 생성
            BacktestValidationResponse.ValidationSummary summary = createValidationSummary(
                    request, tradingDates, stockValidations);

            // 7. 경고 메시지 생성
            addValidationWarnings(warnings, stockValidations, tradingDates.size());

            return BacktestValidationResponse.success(summary, stockValidations, warnings);

        } catch (Exception e) {
            log.error("백테스트 검증 중 오류 발생", e);
            return BacktestValidationResponse.failure(
                    List.of("검증 중 오류가 발생했습니다: " + e.getMessage()),
                    List.of());
        }
    }

    @Override
    public Long createBacktest(User user, BacktestCreateRequest request) {
        // 1. 요청 데이터 유효성 검사
        if (!request.isValid()) {
            throw BacktestException.invalidRequest();
        }

        // 2. 종목별 데이터 가용성 검증
        List<String> tickers = request.getStocks().stream()
                .map(BacktestCreateRequest.BacktestStockRequest::getTicker)
                .toList();

        validateDataAvailability(tickers, request.getStartDate(), request.getEndDate());

        // 3. 리밸런싱 주기에 따른 실제 거래일 확인
        List<LocalDate> tradingDates = getTradingDatesForRebalancing(
                request.getRebalancingType(), request.getRebalancingPeriod(),
                request.getStartDate(), request.getEndDate());

        if (tradingDates.isEmpty()) {
            throw BacktestException.invalidRequest();
        }

        // 4. BacktestRecord 생성 및 저장
        BacktestRecord backtestRecord = BacktestRecord.builder()
                .user(user)
                .testName(request.getTestName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rebalancingType(request.getRebalancingType())
                .rebalancingPeriod(request.getRebalancingPeriod())
                .status(BacktestRecord.BacktestStatus.PENDING)
                .build();

        backtestRecord = backtestRecordRepository.save(backtestRecord);

        // 5. 백테스트 종목 정보 저장
        saveBacktestStocks(backtestRecord, request);

        // 6. Kafka로 백테스트 요청 전송
        try {
            Map<String, Object> backtestRequest = createBacktestRequest(backtestRecord, request, tickers, tradingDates);
            kafkaTemplate.send("backtest-request", backtestRecord.getId().toString(), backtestRequest);
            log.info("백테스트 요청 전송 완료: backtestId={}", backtestRecord.getId());

            // 상태를 PROCESSING으로 변경
            backtestRecord.updateStatus(BacktestRecord.BacktestStatus.PROCESSING);
            backtestRecordRepository.save(backtestRecord);

        } catch (Exception e) {
            log.error("백테스트 요청 전송 실패: backtestId={}", backtestRecord.getId(), e);
            backtestRecord.updateStatus(BacktestRecord.BacktestStatus.FAILED, "요청 전송 실패: " + e.getMessage());
            backtestRecordRepository.save(backtestRecord);
            throw BacktestException.failed();
        }

        return backtestRecord.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BacktestListResponse> getBacktestList(User user, Pageable pageable) {
        Page<BacktestRecord> records = backtestRecordRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return records.map(BacktestListResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public BacktestResultResponse getBacktestResult(User user, Long backtestId) {
        BacktestRecord record = backtestRecordRepository.findByUserAndIdWithUser(user, backtestId)
                .orElseThrow(() -> BacktestException.notFound());

        List<BacktestDetail> details = backtestDetailRepository.findByBacktestRecordOrderByPeriodDateAsc(record);
        List<BacktestStock> portfolioStocks = backtestStockRepository.findByBacktestRecordWithStock(record);

        return BacktestResultResponse.from(record, details, portfolioStocks);
    }

    @Override
    public void deleteBacktest(User user, Long backtestId) {
        BacktestRecord record = backtestRecordRepository.findByUserAndIdWithUser(user, backtestId)
                .orElseThrow(() -> BacktestException.notFound());

        // 진행 중인 백테스트는 삭제 불가
        if (record.isInProgress()) {
            throw BacktestException.deleteNotAllowed();
        }

        // 연관된 데이터 모두 삭제 (cascade 설정으로 BacktestStock은 자동 삭제됨)
        backtestDetailRepository.findByBacktestRecordOrderByPeriodDateAsc(record)
                .forEach(backtestDetailRepository::delete);

        backtestRecordRepository.delete(record);
        log.info("백테스트 삭제 완료: backtestId={}", backtestId);
    }

    // @KafkaListener(topics = "backtest-result", groupId = "rebra-main-server")
    public void handleBacktestResult(Object message, Acknowledgment acknowledgment) {
        try {
            processBacktestResult(message);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("백테스트 결과 처리 실패", e);
            // 에러 발생 시에도 ack하여 메시지가 무한 재시도되지 않도록 함
            acknowledgment.acknowledge();
        }
    }

    @Override
    public void processBacktestResult(Object backtestResponse) {
        try {
            // JSON을 Map으로 변환
            Map<String, Object> responseMap = objectMapper.convertValue(backtestResponse, Map.class);
            Long backtestId = Long.valueOf(responseMap.get("backtest_id").toString());
            String status = responseMap.get("status").toString();

            BacktestRecord record = backtestRecordRepository.findById(backtestId)
                    .orElseThrow(() -> BacktestException.notFound());

            if ("COMPLETED".equals(status)) {
                // 성공적으로 완료된 경우
                updateBacktestResults(record, responseMap);
                record.updateStatus(BacktestRecord.BacktestStatus.COMPLETED);
                log.info("백테스트 결과 처리 완료: backtestId={}", backtestId);
            } else {
                // 실패한 경우
                String errorMessage = responseMap.getOrDefault("error_message", "알 수 없는 오류").toString();
                record.updateStatus(BacktestRecord.BacktestStatus.FAILED, errorMessage);
                log.warn("백테스트 실패: backtestId={}, error={}", backtestId, errorMessage);
            }

            backtestRecordRepository.save(record);

        } catch (Exception e) {
            log.error("백테스트 결과 처리 중 오류 발생", e);
            throw e;
        }
    }

    private Map<String, Object> createBacktestRequest(BacktestRecord record, BacktestCreateRequest request, 
                                                     List<String> tickers, List<LocalDate> tradingDates) {
        // 특정 거래일의 주식 데이터만 조회
        List<StockPrice> stockPrices = getStockPricesForDates(tickers, tradingDates);

        // 백테스트 요청 객체 생성
        Map<String, Object> backtestRequest = new HashMap<>();
        backtestRequest.put("backtest_id", record.getId());
        backtestRequest.put("start_date", request.getStartDate().toString());
        backtestRequest.put("end_date", request.getEndDate().toString());
        backtestRequest.put("rebalancing_type", request.getRebalancingType().toString());
        backtestRequest.put("rebalancing_period", request.getRebalancingPeriod() != null ? 
                request.getRebalancingPeriod().toString() : null);

        // 종목 정보
        List<Map<String, Object>> stocks = request.getStocks().stream()
                .map(stock -> {
                    Map<String, Object> stockMap = new HashMap<>();
                    stockMap.put("stock_code", stock.getTicker());
                    stockMap.put("weight", stock.getWeight());
                    stockMap.put("threshold_percentage", stock.getThresholdPercentage());
                    return stockMap;
                })
                .toList();
        backtestRequest.put("stocks", stocks);

        // OHLCV 데이터 (종가만 전송)
        List<Map<String, Object>> ohlcvData = stockPrices.stream()
                .map(price -> {
                    Map<String, Object> priceMap = new HashMap<>();
                    priceMap.put("stock_code", price.getTicker());
                    priceMap.put("trade_date", price.getDate().toString());
                    priceMap.put("close_price", price.getClosePrice().doubleValue());
                    return priceMap;
                })
                .toList();
        backtestRequest.put("ohlcv_data", ohlcvData);

        return backtestRequest;
    }

    private void updateBacktestResults(BacktestRecord record, Map<String, Object> responseMap) {
        Map<String, Object> summaryMap = (Map<String, Object>) responseMap.get("summary");
        List<Map<String, Object>> detailsList = (List<Map<String, Object>>) responseMap.get("details");

        // BacktestRecord에 결과 업데이트
        BigDecimal finalValue = new BigDecimal(summaryMap.get("final_value").toString());
        BigDecimal totalReturn = new BigDecimal(summaryMap.get("total_return").toString());
        BigDecimal buyHoldReturn = new BigDecimal(summaryMap.get("buy_hold_return").toString());
        BigDecimal excessReturn = totalReturn.subtract(buyHoldReturn);
        BigDecimal periodGrowthRate = new BigDecimal(summaryMap.get("period_growth_rate").toString());
        Integer rebalancingCount = Integer.valueOf(summaryMap.get("rebalancing_count").toString());
        BigDecimal totalFee = new BigDecimal(summaryMap.get("total_fee").toString());
        BigDecimal totalBorrowingCost = summaryMap.get("total_borrowing_cost") != null ?
                new BigDecimal(summaryMap.get("total_borrowing_cost").toString()) : BigDecimal.ZERO;
        BigDecimal maxBorrowingAmount = summaryMap.get("max_borrowing_amount") != null ?
                new BigDecimal(summaryMap.get("max_borrowing_amount").toString()) : null;
        BigDecimal minCashBalance = summaryMap.get("min_cash_balance") != null ?
                new BigDecimal(summaryMap.get("min_cash_balance").toString()) : null;
        BigDecimal maxDrawdown = summaryMap.get("max_drawdown") != null ?
                new BigDecimal(summaryMap.get("max_drawdown").toString()) : null;
        BigDecimal volatility = summaryMap.get("volatility") != null ?
                new BigDecimal(summaryMap.get("volatility").toString()) : null;
        BigDecimal sharpeRatio = summaryMap.get("sharpe_ratio") != null ?
                new BigDecimal(summaryMap.get("sharpe_ratio").toString()) : null;
        BigDecimal timeWeightedReturn = summaryMap.get("time_weighted_return") != null ?
                new BigDecimal(summaryMap.get("time_weighted_return").toString()) : null;
        record.updateResults(finalValue, totalReturn, buyHoldReturn, excessReturn, periodGrowthRate,
                rebalancingCount, totalFee, totalBorrowingCost, maxBorrowingAmount, minCashBalance,
                maxDrawdown, volatility, sharpeRatio, timeWeightedReturn);

        // BacktestDetail 저장
        List<BacktestDetail> details = detailsList.stream()
                .map(detailMap -> BacktestDetail.builder()
                        .backtestRecord(record)
                        .periodDate(LocalDate.parse(detailMap.get("period_date").toString()))
                        .portfolioValue(new BigDecimal(detailMap.get("portfolio_value").toString()))
                        .periodReturn(new BigDecimal(detailMap.get("period_return").toString()))
                        .isRebalanced(Boolean.valueOf(detailMap.get("is_rebalanced").toString()))
                        .cashBalance(detailMap.get("cash_balance") != null ?
                                new BigDecimal(detailMap.get("cash_balance").toString()) : null)
                        .dailyBorrowingInterest(detailMap.get("daily_borrowing_interest") != null ?
                                new BigDecimal(detailMap.get("daily_borrowing_interest").toString()) : null)
                        .cumulativeReturn(detailMap.get("cumulative_return") != null ?
                                new BigDecimal(detailMap.get("cumulative_return").toString()) : null)
                        .buyHoldReturn(detailMap.get("buy_hold_return") != null ?
                                new BigDecimal(detailMap.get("buy_hold_return").toString()) : null)
                        .totalBuyAmount(detailMap.get("total_buy_amount") != null ?
                                new BigDecimal(detailMap.get("total_buy_amount").toString()) : null)
                        .totalSellAmount(detailMap.get("total_sell_amount") != null ?
                                new BigDecimal(detailMap.get("total_sell_amount").toString()) : null)
                        .build())
                .toList();
        backtestDetailRepository.saveAll(details);
    }

    /**
     * 종목별 데이터 가용성을 검증한다
     */
    private void validateDataAvailability(List<String> tickers, LocalDate startDate, LocalDate endDate) {
        List<Object[]> dataAvailability = stockPriceRepository.findDataAvailabilityByTickers(tickers);
        
        if (dataAvailability.isEmpty()) {
            throw BacktestException.invalidRequest();
        }

        StringBuilder errorMessages = new StringBuilder();
        
        for (Object[] data : dataAvailability) {
            String ticker = (String) data[0];
            LocalDate dataStartDate = (LocalDate) data[1];
            LocalDate dataEndDate = (LocalDate) data[2];
            Long dataCount = (Long) data[3];
            
            // 요청 기간에 데이터가 없는 경우
            if (dataEndDate.isBefore(startDate) || dataStartDate.isAfter(endDate)) {
                errorMessages.append(String.format("종목 %s: 요청 기간에 데이터가 없습니다 (데이터 보유 기간: %s ~ %s)\n", 
                    ticker, dataStartDate, dataEndDate));
            }
            // 데이터 커버리지가 부족한 경우 (50% 미만)
            else {
                LocalDate actualStart = dataStartDate.isAfter(startDate) ? dataStartDate : startDate;
                LocalDate actualEnd = dataEndDate.isBefore(endDate) ? dataEndDate : endDate;
                long requestedDays = startDate.until(endDate).getDays() + 1;
                long coverageDays = actualStart.until(actualEnd).getDays() + 1;
                
                if (coverageDays < requestedDays * 0.5) {
                    errorMessages.append(String.format("종목 %s: 데이터 커버리지가 부족합니다 (%.1f%%)\n", 
                        ticker, (double) coverageDays / requestedDays * 100));
                }
            }
        }
        
        if (errorMessages.length() > 0) {
            log.warn("백테스트 데이터 가용성 검증 실패:\n{}", errorMessages.toString());
            throw BacktestException.invalidRequest();
        }
        
        log.info("백테스트 데이터 가용성 검증 완료 - 종목수: {}", tickers.size());
    }

    /**
     * 리밸런싱 유형에 따른 거래일 목록을 반환한다
     */
    private List<LocalDate> getTradingDatesForRebalancing(BacktestRecord.RebalancingType rebalancingType,
                                                        BacktestRecord.RebalancingPeriod rebalancingPeriod,
                                                        LocalDate startDate, LocalDate endDate) {
        switch (rebalancingType) {
            case THRESHOLD:
                // 임계값 기반은 모든 거래일 필요
                return stockPriceRepository.findTradingDatesBetween(startDate, endDate);
                
            case PERIODIC:
                // 주기적 리밸런싱은 주기에 맞는 날짜만
                if (rebalancingPeriod == BacktestRecord.RebalancingPeriod.MONTHLY) {
                    return stockPriceRepository.findMonthEndTradingDates(startDate, endDate);
                } else if (rebalancingPeriod == BacktestRecord.RebalancingPeriod.QUARTERLY) {
                    return stockPriceRepository.findQuarterEndTradingDates(startDate, endDate);
                }
                break;
        }
        
        throw new IllegalArgumentException("지원하지 않는 리밸런싱 설정입니다");
    }

    /**
     * 특정 날짜들의 주식 가격 데이터를 조회한다
     */
    private List<StockPrice> getStockPricesForDates(List<String> tickers, List<LocalDate> dates) {
        List<StockPrice> allPrices = new ArrayList<>();
        
        for (LocalDate date : dates) {
            List<StockPrice> dailyPrices = stockPriceRepository.findByDateOrderByTickerAsc(date);
            // 요청된 종목만 필터링
            List<StockPrice> filteredPrices = dailyPrices.stream()
                    .filter(price -> tickers.contains(price.getTicker()))
                    .toList();
            allPrices.addAll(filteredPrices);
        }
        
        log.info("주식 가격 데이터 조회 완료 - 총 {}건 ({}일, {}종목)", 
                allPrices.size(), dates.size(), tickers.size());
        
        return allPrices;
    }

    /**
     * 종목별 데이터 가용성을 검증하고 상세 정보를 반환한다
     */
    private List<BacktestValidationResponse.StockValidation> validateStockDataAvailability(
            List<String> tickers, LocalDate startDate, LocalDate endDate) {
        
        List<Object[]> dataAvailability = stockPriceRepository.findDataAvailabilityByTickers(tickers);
        Map<String, Object[]> dataMap = dataAvailability.stream()
                .collect(Collectors.toMap(data -> (String) data[0], data -> data));

        List<BacktestValidationResponse.StockValidation> validations = new ArrayList<>();
        
        for (String ticker : tickers) {
            Object[] data = dataMap.get(ticker);
            
            if (data == null) {
                // 데이터가 전혀 없는 경우
                BacktestValidationResponse.StockValidation validation = 
                        BacktestValidationResponse.StockValidation.builder()
                                .ticker(ticker)
                                .name(getStockName(ticker))
                                .isValid(false)
                                .dataCoverage(0.0)
                                .dataCount(0L)
                                .issue("해당 종목의 데이터가 존재하지 않습니다")
                                .suggestion("다른 종목으로 변경하거나 데이터를 확인해주세요")
                                .build();
                validations.add(validation);
                continue;
            }

            LocalDate dataStartDate = (LocalDate) data[1];
            LocalDate dataEndDate = (LocalDate) data[2];
            Long dataCount = (Long) data[3];
            
            boolean isValid = true;
            String issue = null;
            String suggestion = null;
            double coverage = 0.0;
            
            // 요청 기간에 데이터가 없는 경우
            if (dataEndDate.isBefore(startDate) || dataStartDate.isAfter(endDate)) {
                isValid = false;
                issue = String.format("요청 기간에 데이터가 없습니다 (보유 기간: %s ~ %s)", 
                        dataStartDate, dataEndDate);
                suggestion = String.format("%s 이후 기간으로 변경해주세요", dataEndDate);
            } else {
                // 데이터 커버리지 계산
                LocalDate actualStart = dataStartDate.isAfter(startDate) ? dataStartDate : startDate;
                LocalDate actualEnd = dataEndDate.isBefore(endDate) ? dataEndDate : endDate;
                long requestedDays = startDate.until(endDate).getDays() + 1;
                long coverageDays = actualStart.until(actualEnd).getDays() + 1;
                coverage = (double) coverageDays / requestedDays * 100;
                
                // 커버리지가 50% 미만인 경우
                if (coverage < 50.0) {
                    isValid = false;
                    issue = String.format("데이터 커버리지가 부족합니다 (%.1f%%)", coverage);
                    suggestion = String.format("%s ~ %s 기간으로 조정해주세요", 
                            dataStartDate, dataEndDate);
                } else if (coverage < 90.0) {
                    issue = String.format("일부 기간의 데이터가 누락될 수 있습니다 (%.1f%%)", coverage);
                    suggestion = "전체 데이터 보유 기간으로 조정하는 것을 권장합니다";
                }
            }
            
            BacktestValidationResponse.StockValidation validation = 
                    BacktestValidationResponse.StockValidation.builder()
                            .ticker(ticker)
                            .name(getStockName(ticker))
                            .isValid(isValid)
                            .dataStartDate(dataStartDate)
                            .dataEndDate(dataEndDate)
                            .dataCoverage(coverage)
                            .dataCount(dataCount)
                            .issue(issue)
                            .suggestion(suggestion)
                            .build();
            validations.add(validation);
        }
        
        return validations;
    }

    /**
     * 검증 요약 정보를 생성한다
     */
    private BacktestValidationResponse.ValidationSummary createValidationSummary(
            BacktestCreateRequest request, List<LocalDate> tradingDates,
            List<BacktestValidationResponse.StockValidation> stockValidations) {
        
        LocalDate actualStartDate = tradingDates.isEmpty() ? request.getStartDate() : tradingDates.get(0);
        LocalDate actualEndDate = tradingDates.isEmpty() ? request.getEndDate() : 
                tradingDates.get(tradingDates.size() - 1);
        
        double overallCoverage = stockValidations.stream()
                .mapToDouble(BacktestValidationResponse.StockValidation::getDataCoverage)
                .average()
                .orElse(0.0);
        
        int validStockCount = (int) stockValidations.stream()
                .mapToLong(stock -> stock.isValid() ? 1 : 0)
                .sum();
        
        // 예상 리밸런싱 횟수 계산
        int expectedRebalancingCount = calculateExpectedRebalancingCount(
                request.getRebalancingType(), tradingDates.size());
        
        return BacktestValidationResponse.ValidationSummary.builder()
                .actualStartDate(actualStartDate)
                .actualEndDate(actualEndDate)
                .totalTradingDays(tradingDates.size())
                .expectedRebalancingCount(expectedRebalancingCount)
                .overallDataCoverage(overallCoverage)
                .validStockCount(validStockCount)
                .totalStockCount(stockValidations.size())
                .rebalancingDates(tradingDates.size() <= 20 ? tradingDates : 
                        tradingDates.subList(0, 20)) // 처음 20개만 표시
                .build();
    }

    /**
     * 검증 경고 메시지를 추가한다
     */
    private void addValidationWarnings(List<String> warnings, 
                                     List<BacktestValidationResponse.StockValidation> stockValidations,
                                     int tradingDaysCount) {
        
        // 데이터 커버리지 경고
        long partialCoverageCount = stockValidations.stream()
                .filter(stock -> stock.isValid() && stock.getDataCoverage() < 90.0)
                .count();
        
        if (partialCoverageCount > 0) {
            warnings.add(String.format("%d개 종목에서 일부 데이터가 누락될 수 있습니다", partialCoverageCount));
        }
        
        // 거래일 수 경고
        if (tradingDaysCount < 30) {
            warnings.add("백테스트 기간이 짧아 결과의 신뢰성이 낮을 수 있습니다");
        }
        
        // 종목 수 경고
        if (stockValidations.size() < 3) {
            warnings.add("분산투자 효과를 위해 3개 이상의 종목을 권장합니다");
        }
    }

    /**
     * 예상 리밸런싱 횟수를 계산한다
     */
    private int calculateExpectedRebalancingCount(BacktestRecord.RebalancingType rebalancingType, 
                                                int tradingDaysCount) {
        switch (rebalancingType) {
            case THRESHOLD:
                // 임계값 기반은 예측 어려우므로 대략적 추정 (월 1회 정도)
                return Math.max(1, tradingDaysCount / 20);
            case PERIODIC:
                // 주기적 리밸런싱은 거래일 수와 동일
                return tradingDaysCount;
            default:
                return 0;
        }
    }

    /**
     * 종목명을 조회한다 (임시 구현)
     */
    private String getStockName(String ticker) {
        // 실제로는 StockPrice에서 name을 가져와야 함
        Optional<StockPrice> stockPrice = stockPriceRepository.findTopByTickerOrderByDateDesc(ticker);
        return stockPrice.map(StockPrice::getName).orElse("알 수 없음");
    }

    /**
     * 백테스트 종목 정보를 저장한다
     */
    private void saveBacktestStocks(BacktestRecord backtestRecord, BacktestCreateRequest request) {
        for (BacktestCreateRequest.BacktestStockRequest stockRequest : request.getStocks()) {
            // Stock 엔티티 조회 또는 생성
            Stock stock = getOrCreateStock(stockRequest.getTicker(), stockRequest.getName());
            
            // BacktestStock 엔티티 생성 및 저장
            BacktestStock backtestStock = BacktestStock.builder()
                    .backtestRecord(backtestRecord)
                    .stock(stock)
                    .targetWeight(BigDecimal.valueOf(stockRequest.getWeight()))
                    .thresholdPercentage(stockRequest.getThresholdPercentage() != null ? 
                            BigDecimal.valueOf(stockRequest.getThresholdPercentage()) : null)
                    .build();
            
            backtestStockRepository.save(backtestStock);
        }
        
        log.info("백테스트 종목 정보 저장 완료 - backtestId: {}, 종목수: {}", 
                backtestRecord.getId(), request.getStocks().size());
    }

    /**
     * Stock 엔티티를 조회하거나 생성한다
     */
    private Stock getOrCreateStock(String stockCode, String stockName) {
        Optional<Stock> existingStock = stockRepository.findByStockCode(stockCode);
        
        if (existingStock.isPresent()) {
            return existingStock.get();
        }
        
        // 새로운 Stock 엔티티 생성
        Stock newStock = Stock.builder()
                .stockCode(stockCode)
                .stockName(stockName)
                .stockType("주식") // 기본값
                .isActive(true)
                .build();
        
        return stockRepository.save(newStock);
    }

}