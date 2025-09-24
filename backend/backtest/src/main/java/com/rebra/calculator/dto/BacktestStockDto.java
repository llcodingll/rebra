package com.rebra.calculator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rebra.calculator.domain.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 백테스트 종목 정보를 나타내는 DTO 클래스
 * 메인 서버의 BACKTEST_STOCK 테이블 데이터에 대응된다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BacktestStockDto {
    
    /**
     * 종목 코드
     * 한국거래소 6자리 종목코드 (예: "005930")
     */
    @JsonProperty("stock_code")
    private String stockCode;
    
    
    /**
     * 가중치
     * 포트폴리오에서 이 종목의 상대적 가중치 (정수)
     * 예: 3 (전체 가중치 합에서 3/10 = 30%의 비중을 가짐)
     */
    @JsonProperty("weight")
    private Integer weight;
    
    /**
     * 임계값 비율
     * 목표 비중의 상대적 비율로, 이 값만큼 벗어나면 리밸런싱을 실행 (0.0 ~ 1.0)
     * 예: 0.05 = 5% (목표 30%에서 28.5%~31.5% 범위를 벗어나면 리밸런싱)
     *     허용 편차 = 30% × 5% = 1.5%
     */
    @JsonProperty("threshold_percentage")
    private Double thresholdPercentage;
    
    /**
     * 현재 보유 수량
     * 리밸런싱 전 현재 보유하고 있는 주식 수량
     * 초기 백테스트에서는 0으로 시작
     */
    @JsonProperty("shares")
    private Integer shares;

    /**
     * 간단 생성자
     * 기본적인 종목 정보만으로 DTO를 생성할 때 사용
     * 
     * @param stockCode 종목 코드
     * @param weight 가중치
     * @param thresholdPercentage 임계값 비율
     */
    public BacktestStockDto(String stockCode, Integer weight, Double thresholdPercentage) {
        this.stockCode = stockCode;
        this.weight = weight;
        this.thresholdPercentage = thresholdPercentage;
        this.shares = 0; // 기본값
    }

    /**
     * 종목 정보의 유효성을 검증한다
     * 
     * @return 유효한 종목 정보면 true
     */
    public boolean isValid() {
        try {
            // 종목 코드 검증
            if (stockCode == null || stockCode.trim().isEmpty()) {
                return false;
            }
            
            // 6자리 영숫자 형식 검증
            if (!stockCode.trim().toUpperCase().matches("[A-Z0-9]{6}")) {
                return false;
            }
            
            
            // 가중치 검증 (양수여야 함)
            if (weight == null || weight <= 0) {
                return false;
            }
            
            // 임계값 검증 (0 ~ 1 사이)
            if (thresholdPercentage == null || thresholdPercentage < 0.0 || thresholdPercentage > 1.0) {
                return false;
            }
            
            // 보유 수량 검증 (null이거나 음수가 아니어야 함)
            if (shares != null && shares < 0) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 현재 비중이 목표 비중에서 임계값을 초과했는지 확인
     * 임계값은 목표 비중의 상대적 비율로 계산됨
     * 예: 목표 30%, 임계값 5% → 허용 범위 28.5%~31.5% (30% ± 30%×5%)
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @param targetWeight 목표 비중 (0.0 ~ 1.0) - 정규화된 값
     * @return 임계값을 초과했으면 true
     */
    public boolean exceedsThreshold(double currentWeight, double targetWeight) {
        if (thresholdPercentage == null) {
            return false;
        }
        
        double deviation = Math.abs(currentWeight - targetWeight);
        // 상대적 임계값 계산 (목표 비중 × 임계값 비율)
        double relativeThreshold = targetWeight * thresholdPercentage;
        
        return deviation > relativeThreshold;
    }

    /**
     * 목표 비중과 현재 비중의 편차를 계산
     * 양수면 목표보다 높음, 음수면 목표보다 낮음
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @param targetWeight 목표 비중 (0.0 ~ 1.0) - 정규화된 값
     * @return 비중 편차
     */
    public double getWeightDeviation(double currentWeight, double targetWeight) {
        return currentWeight - targetWeight;
    }

    /**
     * 현재 비중이 목표 비중보다 높은지 확인
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @param targetWeight 목표 비중 (0.0 ~ 1.0) - 정규화된 값
     * @return 현재 비중이 목표보다 높으면 true
     */
    public boolean isOverWeight(double currentWeight, double targetWeight) {
        return getWeightDeviation(currentWeight, targetWeight) > 0;
    }

    /**
     * 현재 비중이 목표 비중보다 낮은지 확인
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @param targetWeight 목표 비중 (0.0 ~ 1.0) - 정규화된 값
     * @return 현재 비중이 목표보다 낮으면 true
     */
    public boolean isUnderWeight(double currentWeight, double targetWeight) {
        return getWeightDeviation(currentWeight, targetWeight) < 0;
    }

    /**
     * 가중치를 반환
     * 
     * @return 가중치
     */
    public int getWeight() {
        return weight != null ? weight : 0;
    }

    /**
     * 임계값을 백분율로 반환
     * 
     * @return 임계값 백분율
     */
    public double getThresholdPercentageValue() {
        return thresholdPercentage != null ? thresholdPercentage * 100.0 : 0.0;
    }

    /**
     * 종목 코드를 대문자로 정규화하여 반환
     * 
     * @return 정규화된 종목 코드
     */
    public String getNormalizedStockCode() {
        return stockCode != null ? stockCode.trim().toUpperCase() : null;
    }


    /**
     * 보유 수량을 안전하게 반환
     * null인 경우 0을 반환
     * 
     * @return 보유 수량
     */
    public int getSafeShares() {
        return shares != null ? shares : 0;
    }

    /**
     * 종목 정보를 Domain 객체로 변환 (원본 가중치 기반)
     * 
     * @return Stock 도메인 객체
     */
    public Stock toDomain() {
        return new Stock(
            getNormalizedStockCode(),
            weight,  // 원본 가중치
            thresholdPercentage != null ? thresholdPercentage : 0.0,
            getSafeShares()  // 초기 보유 수량
        );
    }

    /**
     * 간단한 종목 정보를 문자열로 반환
     * 
     * @return 간단한 종목 정보
     */
    public String getSimpleInfo() {
        return String.format("%s - 가중치: %d, 임계: %.1f%%",
                stockCode,
                getWeight(),
                getThresholdPercentageValue());
    }

    /**
     * 상세한 종목 정보를 문자열로 반환
     * 
     * @return 상세 종목 정보
     */
    public String getDetailedInfo() {
        return String.format("BacktestStock{code='%s', weight=%d, threshold=%.3f, shares=%d}",
                stockCode, getWeight(), thresholdPercentage, getSafeShares());
    }

    /**
     * 두 종목이 동일한지 비교 (종목 코드 기준)
     * 
     * @param other 비교할 다른 종목
     * @return 동일한 종목이면 true
     */
    public boolean isSameStock(BacktestStockDto other) {
        if (other == null) {
            return false;
        }
        
        String thisCode = getNormalizedStockCode();
        String otherCode = other.getNormalizedStockCode();
        
        return thisCode != null && thisCode.equals(otherCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BacktestStockDto that = (BacktestStockDto) obj;
        return java.util.Objects.equals(getNormalizedStockCode(), that.getNormalizedStockCode());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(getNormalizedStockCode());
    }
}