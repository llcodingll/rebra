package com.rebra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.dto.backtest.BacktestRequest;
import com.rebra.dto.backtest.BacktestStockDto;
import com.rebra.dto.internal.BacktestRecordDto;
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

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
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

    private final StockPriceRepository stockPriceRepository;
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
            
            // 2단계: BacktestRecord 조회 (트랜잭션 있음)
            BacktestRecordDto backtestRecordDto = backtestDataService.findBacktestRecordDto(backtestId);
            
            try {
                // 3단계: 종목별 데이터 확보 (스마트 캐싱)
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

                // 4단계: 데이터 가용성 최종 검증
                backtestDataService.validateDataAvailability(tickers, request.getStartDate(), request.getEndDate());

                // 5단계: Kafka로 백테스트 요청 전송
                BacktestRequest backtestRequest = backtestDataService.createBacktestRequest(backtestRecordDto, request, tickers, null);
                kafkaTemplate.send("backtest-request", backtestRecordDto.getId().toString(), backtestRequest)
                    .get(10, TimeUnit.SECONDS);
                log.info("백테스트 요청 전송 완료: backtestId={}", backtestRecordDto.getId());

                // 6단계: 상태를 PROCESSING으로 변경 (트랜잭션 있음)
                backtestDataService.updateBacktestStatus(backtestId, BacktestRecord.BacktestStatus.PROCESSING);
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
                
                // 에러 상태 업데이트 (트랜잭션 있음)
                backtestDataService.updateBacktestStatus(backtestId, BacktestRecord.BacktestStatus.FAILED, errorMessage);
                
                // 예외를 다시 던지지 않고 로그만 남김 (사용자에게는 backtestId 반환)
                log.warn("백테스트 처리 실패했지만 ID는 반환됨: backtestId={}, error={}", backtestId, errorMessage);
            }
            
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
        return backtestDataService.getBacktestList(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BacktestResultResponse getBacktestResult(Long backtestId) {
        return backtestDataService.getBacktestResult(backtestId);
    }


    @Override
    @Transactional
    public void deleteBacktest(Long userId, Long backtestId) {
        backtestDataService.deleteBacktest(userId, backtestId);
    }

    @KafkaListener(topics = "backtest-result", groupId = "rebra-main-server")
    public void handleBacktestResult(@Payload Map<String, Object> message, Acknowledgment acknowledgment) {
        Long backtestId = null;
        try {
            // backtestId 먼저 추출
            backtestId = message.get("backtest_id") != null ? 
                Long.valueOf(message.get("backtest_id").toString()) : null;
                
            processBacktestResult(message);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("백테스트 결과 처리 실패: backtestId={}", backtestId, e);
            if (backtestId != null) {
                try {
                    backtestDataService.updateBacktestStatusToFailed(backtestId, "메인 서버 오류");
                } catch (Exception dbEx) {
                    log.error("DB 상태 업데이트 실패, 재처리 대기: backtestId={}", backtestId, dbEx);
                }
                acknowledgment.acknowledge();
            } else {
                acknowledgment.acknowledge();  // backtestId 없음 → 재처리해도 소용없음
            }
        }
    }

    @Override
    @Transactional
    public void processBacktestResult(Map<String, Object> responseMap) {
        backtestDataService.processBacktestResult(responseMap);
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