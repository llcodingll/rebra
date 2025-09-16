package com.rebra.util;

import com.rebra.dto.portfoliodata.PortfolioReturnData;
import com.rebra.entity.PortfolioStock;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            return new PortfolioReturnData(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // 등록 주식의 종목코드 Set 생성 (O(1) 검색을 위해)
        Set<String> registeredStockCodes = portfolioStocks.stream()
            .map(ps -> ps.getStock().getStockCode())
            .collect(Collectors.toSet());

        log.debug("등록 주식 종목코드: {}", registeredStockCodes);

        // KIS API output1에서 등록 주식만 필터링하여 수익률 계산
        BigDecimal totalPurchaseAmount = BigDecimal.ZERO;
        BigDecimal totalEvaluationAmount = BigDecimal.ZERO;

        if (kisBalance.getOutput1() != null) {
            for (var stock : kisBalance.getOutput1()) {
                String stockCode = stock.getPdno(); // 종목코드

                if (registeredStockCodes.contains(stockCode)) {
                    // 매입금액과 평가금액을 BigDecimal로 변환하여 합산
                    BigDecimal purchaseAmount = new BigDecimal(stock.getPchsAmt() != null ? stock.getPchsAmt() : "0");
                    BigDecimal evaluationAmount = new BigDecimal(stock.getEvluAmt() != null ? stock.getEvluAmt() : "0");

                    totalPurchaseAmount = totalPurchaseAmount.add(purchaseAmount);
                    totalEvaluationAmount = totalEvaluationAmount.add(evaluationAmount);

                    log.debug("종목 매칭 - 종목코드: {}, 매입금액: {}, 평가금액: {}",
                        stockCode, purchaseAmount, evaluationAmount);
                }
            }
        }

        // 수익률 및 수익금액 계산
        BigDecimal returnAmount = totalEvaluationAmount.subtract(totalPurchaseAmount);
        BigDecimal returnRate = BigDecimal.ZERO;

        if (totalPurchaseAmount.compareTo(BigDecimal.ZERO) > 0) {
            returnRate = returnAmount
                .divide(totalPurchaseAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        log.debug("수익률 계산 완료 - 매입금액: {}, 평가금액: {}, 수익금액: {}, 수익률: {}%",
            totalPurchaseAmount, totalEvaluationAmount, returnAmount, returnRate);

        return new PortfolioReturnData(totalPurchaseAmount, totalEvaluationAmount, returnAmount, returnRate);
    }
}