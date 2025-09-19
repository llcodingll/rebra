package com.rebra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.dto.backtest.BacktestRequest;
import com.rebra.dto.backtest.BacktestStockDto;
import com.rebra.dto.request.BacktestCreateRequest;
import com.rebra.dto.response.BacktestDetailResponse;
import com.rebra.dto.response.BacktestListResponse;
import com.rebra.dto.response.BacktestResultResponse;
import com.rebra.dto.response.BacktestValidationResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BacktestServiceImpl implements BacktestService {

    private final BacktestRecordRepository backtestRecordRepository;
    private final BacktestStockRepository backtestStockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final SmartStockDataService smartStockDataService;
    private final BacktestDataService backtestDataService;

    @Qualifier("backtestRequestKafkaTemplate")
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public BacktestValidationResponse validateBacktestRequest(Long userId, BacktestCreateRequest request) {
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

            // THRESHOLD가 아닌 경우에만 빈 리스트 검증
            if (tradingDates.isEmpty() && request.getRebalancingType() != BacktestRecord.RebalancingType.THRESHOLD) {
                errors.add("지정된 기간에 거래일이 없습니다");
                return BacktestValidationResponse.failure(errors, stockValidations);
            }

            // THRESHOLD 타입의 경우 실제 거래일 조회하여 검증 요약 생성
            if (request.getRebalancingType() == BacktestRecord.RebalancingType.THRESHOLD) {
                tradingDates = stockPriceRepository.findTradingDatesBetween(
                        request.getStartDate(), request.getEndDate());
                if (tradingDates.isEmpty()) {
                    errors.add("지정된 기간에 거래일이 없습니다");
                    return BacktestValidationResponse.failure(errors, stockValidations);
                }
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
    public Long createBacktest(Long userId, BacktestCreateRequest request) {
        try {
            // 1단계: BacktestRecord 생성 및 즉시 커밋 (PENDING 상태)
            Long backtestId = backtestDataService.createInitialBacktestRecord(userId, request);
            
            // 2단계: 별도 트랜잭션에서 데이터 처리 및 Kafka 전송 (PROCESSING 상태로 변경)
            // 이 단계에서 실패해도 backtestId는 반환되어 사용자가 상태를 확인할 수 있음
            backtestDataService.processBacktestData(backtestId, request);
            
            return backtestId;
            
        } catch (Exception e) {
            log.error("백테스트 생성 실패: userId={}", userId, e);
            // 초기 레코드 생성 실패 시에만 예외 던짐
            throw e;
        }
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<BacktestListResponse> getBacktestList(Long userId, Pageable pageable) {
        Page<BacktestRecord> records = backtestRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        Page<BacktestListResponse> dtoPage = records.map(BacktestListResponse::from);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public BacktestResultResponse getBacktestResult(Long userId, Long backtestId) {
        BacktestRecord record = backtestRecordRepository.findByUserIdAndId(userId, backtestId)
                .orElseThrow(() -> BacktestException.notFound());

        // 디버그 로그 추가
        log.info("백테스트 조회: id={}, status={}, hasDetails={}, detailsJson 길이={}", 
                backtestId, record.getStatus(), record.hasDetails(), 
                record.getDetailsJson() != null ? record.getDetailsJson().length() : 0);

        String detailsJson = record.getDetailsJson(); // JSON 문자열 직접 사용
        log.info("백테스트 상세 정보 JSON 직접 반환: 길이={}", detailsJson != null ? detailsJson.length() : 0);
        
        List<BacktestStock> portfolioStocks = backtestStockRepository.findByBacktestRecordWithStock(record);

        return BacktestResultResponse.from(record, detailsJson, portfolioStocks);
    }

    @Override
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

    @KafkaListener(topics = "backtest-result", groupId = "rebra-main-server")
    public void handleBacktestResult(@Payload Map<String, Object> message, Acknowledgment acknowledgment) {
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
    @Transactional
    public void processBacktestResult(Map<String, Object> responseMap) {
        try {
            // 이미 Map으로 받아왔으므로 변환 불필요
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
     * 리밸런싱 유형에 따른 거래일 목록을 반환한다 (검증용)
     */
    private List<LocalDate> getTradingDatesForRebalancing(BacktestRecord.RebalancingType rebalancingType,
                                                        BacktestRecord.RebalancingPeriod rebalancingPeriod,
                                                        LocalDate startDate, LocalDate endDate) {
        switch (rebalancingType) {
            case THRESHOLD:
                // 임계값 기반은 createBacktestRequest에서 직접 처리하므로 빈 리스트 반환
                return Collections.emptyList();

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



}