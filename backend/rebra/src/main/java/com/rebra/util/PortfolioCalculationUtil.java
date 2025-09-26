package com.rebra.util;

import com.rebra.dto.portfoliodata.PortfolioReturnData;
import com.rebra.entity.PortfolioStock;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 포트폴리오 수익률 계산 유틸리티
 */
@Slf4j
public class PortfolioCalculationUtil {

    private PortfolioCalculationUtil() {
        // 유틸리티 클래스는 인스턴스 생성 방지
    }

    /**
     * 포트폴리오 수익률 계산
     *
     * @param portfolioStocks 포트폴리오에 등록된 주식 목록
     * @param kisBalance KIS API 잔고 조회 결과
     * @return 포트폴리오 수익률 데이터
     */
    public static PortfolioReturnData calculateReturn(List<PortfolioStock> portfolioStocks,
                                                     InquireBalanceResult kisBalance) {
        log.debug("포트폴리오 수익률 계산 시작 - 등록 주식 수: {}", portfolioStocks.size());

        if (portfolioStocks.isEmpty()) {
            log.info("등록된 주식이 없음");
            return new PortfolioReturnData(0L, 0L, 0L, 0.0);
        }

        // 등록 주식의 종목코드 Set 생성 (O(1) 검색을 위해)
        Set<String> registeredStockCodes = portfolioStocks.stream()
            .map(PortfolioStock::getStockCode)
            .collect(Collectors.toSet());

        log.debug("등록 주식 종목코드: {}", registeredStockCodes);

        // KIS API output1에서 등록 주식만 필터링하여 수익률 계산
        long totalPurchaseAmount = 0L;
        long totalEvaluationAmount = 0L;

        if (kisBalance.getOutput1() != null) {
            for (var stock : kisBalance.getOutput1()) {
                String stockCode = stock.getPdno(); // 종목코드

                if (registeredStockCodes.contains(stockCode)) {
                    // 매입금액과 평가금액을 long으로 변환하여 합산
                    long purchaseAmount = Long.parseLong(stock.getPchsAmt() != null ? stock.getPchsAmt() : "0");
                    long evaluationAmount = Long.parseLong(stock.getEvluAmt() != null ? stock.getEvluAmt() : "0");

                    totalPurchaseAmount += purchaseAmount;
                    totalEvaluationAmount += evaluationAmount;

                    log.debug("종목 매칭 - 종목코드: {}, 매입금액: {}, 평가금액: {}",
                        stockCode, purchaseAmount, evaluationAmount);
                }
            }
        }

        // 수익률 및 수익금액 계산
        long returnAmount = totalEvaluationAmount - totalPurchaseAmount;
        double returnRate = 0.0;

        if (totalPurchaseAmount > 0) {
            returnRate = ((double) returnAmount / totalPurchaseAmount) * 100.0;
        }

        log.debug("수익률 계산 완료 - 매입금액: {}, 평가금액: {}, 수익금액: {}, 수익률: {}%",
            totalPurchaseAmount, totalEvaluationAmount, returnAmount, returnRate);

        return new PortfolioReturnData(totalPurchaseAmount, totalEvaluationAmount, returnAmount, returnRate);
    }

    /**
     * 수익률 계산: (현재가 / 평균매입가 - 1) * 100
     *
     * @param currentPrice 현재가 (문자열)
     * @param averagePrice 평균매입가 (문자열)
     * @return 수익률 (백분율, 소수점 2자리)
     */
    public static Double calculateReturnRate(String currentPrice, String averagePrice) {
        try {
            double current = Double.parseDouble(currentPrice);
            double average = Double.parseDouble(averagePrice);

            if (average == 0) {
                return 0.0;
            }

            double returnRate = ((current / average) - 1.0) * 100.0;
            return Math.round(returnRate * 100.0) / 100.0; // 소수점 2자리 반올림

        } catch (Exception e) {
            log.warn("수익률 계산 실패 - CurrentPrice: {}, AveragePrice: {}, Error: {}",
                    currentPrice, averagePrice, e.getMessage());
            return 0.0;
        }
    }
}