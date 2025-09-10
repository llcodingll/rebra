package com.rebra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.dto.request.BacktestCreateRequest;
import com.rebra.dto.response.BacktestListResponse;
import com.rebra.dto.response.BacktestResultResponse;
import com.rebra.entity.BacktestDetail;
import com.rebra.entity.BacktestRecord;
import com.rebra.entity.BacktestResult;
import com.rebra.entity.StockPrice;
import com.rebra.entity.User;
import com.rebra.exception.backtest.BacktestException;
import com.rebra.repository.BacktestDetailRepository;
import com.rebra.repository.BacktestRecordRepository;
import com.rebra.repository.BacktestResultRepository;
import com.rebra.repository.StockPriceRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BacktestServiceImpl implements BacktestService {

    private final BacktestRecordRepository backtestRecordRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final BacktestDetailRepository backtestDetailRepository;
    private final StockPriceRepository stockPriceRepository;

    @Qualifier("backtestRequestKafkaTemplate")
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public Long createBacktest(User user, BacktestCreateRequest request) {
        // 1. 요청 데이터 유효성 검사
        if (!request.isValid()) {
            throw BacktestException.invalidRequest();
        }

        // 2. 주식 데이터 존재 여부 확인
        List<String> tickers = request.getStocks().stream()
                .map(BacktestCreateRequest.BacktestStockRequest::getTicker)
                .toList();

        List<LocalDate> availableDates = stockPriceRepository.findTradingDatesBetween(
                request.getStartDate(), request.getEndDate());

        if (availableDates.isEmpty()) {
            throw BacktestException.invalidRequest();
        }

        // 3. BacktestRecord 생성 및 저장
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

        // 4. Kafka로 백테스트 요청 전송
        try {
            Map<String, Object> backtestRequest = createBacktestRequest(backtestRecord, request, tickers);
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

        BacktestResult result = backtestResultRepository.findByBacktestRecord(record).orElse(null);
        List<BacktestDetail> details = backtestDetailRepository.findByBacktestRecordOrderByPeriodDateAsc(record);

        return BacktestResultResponse.from(record, result, details);
    }

    @Override
    public void deleteBacktest(User user, Long backtestId) {
        BacktestRecord record = backtestRecordRepository.findByUserAndIdWithUser(user, backtestId)
                .orElseThrow(() -> BacktestException.notFound());

        // 진행 중인 백테스트는 삭제 불가
        if (record.isInProgress()) {
            throw BacktestException.deleteNotAllowed();
        }

        // 연관된 데이터 모두 삭제
        backtestDetailRepository.findByBacktestRecordOrderByPeriodDateAsc(record)
                .forEach(backtestDetailRepository::delete);
        
        backtestResultRepository.findByBacktestRecord(record)
                .ifPresent(backtestResultRepository::delete);

        backtestRecordRepository.delete(record);
        log.info("백테스트 삭제 완료: backtestId={}", backtestId);
    }

    @KafkaListener(topics = "backtest-result", groupId = "rebra-main-server")
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
                saveBacktestResult(record, responseMap);
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

    private Map<String, Object> createBacktestRequest(BacktestRecord record, BacktestCreateRequest request, List<String> tickers) {
        // 주식 데이터 조회
        List<StockPrice> stockPrices = stockPriceRepository.findByTickersAndDateRange(
                tickers, request.getStartDate(), request.getEndDate());

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

    private void saveBacktestResult(BacktestRecord record, Map<String, Object> responseMap) {
        Map<String, Object> summaryMap = (Map<String, Object>) responseMap.get("summary");
        List<Map<String, Object>> detailsList = (List<Map<String, Object>>) responseMap.get("details");

        // BacktestResult 저장
        BacktestResult result = BacktestResult.builder()
                .backtestRecord(record)
                .finalValue(new BigDecimal(summaryMap.get("final_value").toString()))
                .totalReturn(new BigDecimal(summaryMap.get("total_return").toString()))
                .buyHoldReturn(new BigDecimal(summaryMap.get("buy_hold_return").toString()))
                .excessReturn(new BigDecimal(summaryMap.get("total_return").toString())
                    .subtract(new BigDecimal(summaryMap.get("buy_hold_return").toString())))
                .periodGrowthRate(new BigDecimal(summaryMap.get("period_growth_rate").toString()))
                .rebalancingCount(Integer.valueOf(summaryMap.get("rebalancing_count").toString()))
                .totalFee(new BigDecimal(summaryMap.get("total_fee").toString()))
                .totalBorrowingCost(summaryMap.get("total_borrowing_cost") != null ?
                    new BigDecimal(summaryMap.get("total_borrowing_cost").toString()) : BigDecimal.ZERO)
                .maxBorrowingAmount(summaryMap.get("max_borrowing_amount") != null ?
                    new BigDecimal(summaryMap.get("max_borrowing_amount").toString()) : null)
                .minCashBalance(summaryMap.get("min_cash_balance") != null ?
                    new BigDecimal(summaryMap.get("min_cash_balance").toString()) : null)
                .maxDrawdown(summaryMap.get("max_drawdown") != null ?
                    new BigDecimal(summaryMap.get("max_drawdown").toString()) : null)
                .volatility(summaryMap.get("volatility") != null ?
                    new BigDecimal(summaryMap.get("volatility").toString()) : null)
                .sharpeRatio(summaryMap.get("sharpe_ratio") != null ?
                    new BigDecimal(summaryMap.get("sharpe_ratio").toString()) : null)
                .timeWeightedReturn(summaryMap.get("time_weighted_return") != null ?
                    new BigDecimal(summaryMap.get("time_weighted_return").toString()) : null)
                .build();
        backtestResultRepository.save(result);

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

}