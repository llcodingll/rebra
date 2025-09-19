package com.rebra.service;

import com.rebra.dto.HoldingInfo;
import com.rebra.entity.PortfolioStock;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 리밸런싱 계산 결과를 담는 클래스
 * 중복 계산을 방지하고 계산된 데이터를 재사용하기 위한 용도
 */
@Getter
@Builder
public class RebalancingCalculation {
    
    // 기본 정보
    private final Map<String, HoldingInfo> currentHoldings;
    private final List<PortfolioStock> targetStocks;
    private final long totalPortfolioValue;
    private final double totalTargetWeight;
    
    // 종목별 상세 정보
    private final List<StockRebalancingDetail> stockDetails;
    
    /**
     * 종목별 리밸런싱 상세 정보
     */
    @Getter
    @Builder
    public static class StockRebalancingDetail {
        private final String stockCode;
        private final double normalizedTargetWeight;
        private final double currentWeight;
        private final double weightDifference;
        private final double threshold;
        private final int currentQuantity;
        private final long currentPrice;
        private final long currentValue;
        private final long targetValue;
        
        /**
         * 정규화된 목표 비중을 퍼센트로 반환
         * @return 퍼센트 단위 목표 비중 (0~100)
         */
        public double getNormalizedTargetWeightAsPercent() {
            return normalizedTargetWeight * 100.0;
        }
        
        /**
         * 현재 비중을 퍼센트로 반환
         * @return 퍼센트 단위 현재 비중 (0~100)
         */
        public double getCurrentWeightAsPercent() {
            return currentWeight * 100.0;
        }
        
        /**
         * 비중 차이를 퍼센트로 반환
         * @return 퍼센트 단위 비중 차이 (0~100)
         */
        public double getWeightDifferenceAsPercent() {
            return weightDifference * 100.0;
        }
        
        /**
         * 임계값을 퍼센트로 반환
         * @return 퍼센트 단위 임계값 (0~100)
         */
        public double getThresholdAsPercent() {
            return threshold * 100.0;
        }
        
        /**
         * 상대적 임계값을 계산하여 반환
         * @return 목표 비중 × 임계값 비율 (0~1)
         */
        public double getRelativeThreshold() {
            return normalizedTargetWeight * threshold;
        }
        
        /**
         * 상대적 임계값을 퍼센트로 반환
         * @return 퍼센트 단위 상대적 임계값 (0~100)
         */
        public double getRelativeThresholdAsPercent() {
            return getRelativeThreshold() * 100.0;
        }
        
        /**
         * 이 종목이 리밸런싱이 필요한지 판단
         * 상대적 임계값(목표 비중 × 임계값 비율)을 사용하여 판단
         * @return 비중 차이가 상대적 임계값을 초과하면 true
         */
        public boolean isNeedsRebalancing() {
            return weightDifference > getRelativeThreshold();
        }
    }
    
    /**
     * 특정 종목의 정규화된 목표 비중을 반환한다
     * 
     * @param stockCode 종목 코드
     * @return 정규화된 목표 비중 (없으면 0)
     */
    public double getNormalizedWeight(String stockCode) {
        return stockDetails.stream()
                .filter(detail -> detail.getStockCode().equals(stockCode))
                .mapToDouble(StockRebalancingDetail::getNormalizedTargetWeight)
                .findFirst()
                .orElse(0.0);
    }
    
    /**
     * 특정 종목의 현재 비중을 반환한다
     * 
     * @param stockCode 종목 코드
     * @return 현재 비중 (없으면 0)
     */
    public double getCurrentWeight(String stockCode) {
        return stockDetails.stream()
                .filter(detail -> detail.getStockCode().equals(stockCode))
                .mapToDouble(StockRebalancingDetail::getCurrentWeight)
                .findFirst()
                .orElse(0.0);
    }
    
    /**
     * 특정 종목의 상세 정보를 반환한다
     * 
     * @param stockCode 종목 코드
     * @return 종목별 상세 정보 (없으면 null)
     */
    public StockRebalancingDetail getStockDetail(String stockCode) {
        return stockDetails.stream()
                .filter(detail -> detail.getStockCode().equals(stockCode))
                .findFirst()
                .orElse(null);
    }
}