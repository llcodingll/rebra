package com.rebra.calculator.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static com.rebra.calculator.constant.BacktestConstants.TradingRules.WEIGHT_PRECISION;

/**
 * 백테스트에서 사용할 개별 주식 정보를 나타내는 도메인 클래스
 * 종목 코드, 이름, 목표 비중, 임계값 등의 정보를 포함한다.
 */
@Getter
@NoArgsConstructor
@ToString
public class Stock {
    
    /**
     * 종목 코드 (예: "005930")
     * 한국거래소에서 사용하는 6자리 종목코드
     */
    private String stockCode;
    
    
    /**
     * 목표 비중 (0.0 ~ 1.0)
     * 포트폴리오에서 이 종목이 차지해야 할 목표 비중
     * 예: 0.3 = 30%
     */
    private double targetWeight;
    
    /**
     * 임계값 비율 (0.0 ~ 1.0)
     * 목표 비중에서 이 값만큼 벗어나면 리밸런싱을 실행
     * 예: 0.05 = 5% (목표 30%에서 35% 또는 25%가 되면 리밸런싱)
     */
    private double thresholdPercentage;

    /**
     * Stock 생성자
     * 
     * @param stockCode 종목 코드
     * @param targetWeight 목표 비중 (0.0 ~ 1.0)
     * @param thresholdPercentage 임계값 비율 (0.0 ~ 1.0)
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     */
    public Stock(String stockCode, double targetWeight, double thresholdPercentage) {
        validateParameters(stockCode, targetWeight, thresholdPercentage);
        
        this.stockCode = stockCode.trim().toUpperCase();
        this.targetWeight = normalizeWeight(targetWeight);
        this.thresholdPercentage = normalizeWeight(thresholdPercentage);
    }

    /**
     * 매개변수 유효성 검증
     * 
     * @param stockCode 종목 코드
     * @param targetWeight 목표 비중
     * @param thresholdPercentage 임계값 비율
     * @throws IllegalArgumentException 유효하지 않은 매개변수
     */
    private void validateParameters(String stockCode, double targetWeight, double thresholdPercentage) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다.");
        }
        
        if (targetWeight < 0.0 || targetWeight > 1.0) {
            throw new IllegalArgumentException("목표 비중은 0.0과 1.0 사이여야 합니다: " + targetWeight);
        }
        
        if (thresholdPercentage < 0.0 || thresholdPercentage > 1.0) {
            throw new IllegalArgumentException("임계값은 0.0과 1.0 사이여야 합니다: " + thresholdPercentage);
        }
        
        // 종목 코드 형식 검증 (6자리 숫자)
        String cleanCode = stockCode.trim();
        if (!cleanCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("종목 코드는 6자리 숫자여야 합니다: " + stockCode);
        }
    }

    /**
     * 비중 값을 정규화하여 반환
     * 소수점 오차를 방지하기 위해 지정된 정밀도로 반올림
     * 
     * @param weight 정규화할 비중 값
     * @return 정규화된 비중 값
     */
    private double normalizeWeight(double weight) {
        return BigDecimal.valueOf(weight)
                .setScale(WEIGHT_PRECISION, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 현재 비중이 목표 비중에서 임계값을 초과했는지 확인
     * 리밸런싱 필요 여부를 판단하는 데 사용
     * 임계값은 목표 비중의 상대적 비율로 계산됨
     * 예: 목표 30%, 임계값 5% → 허용 범위 28.5%~31.5% (30% ± 30%×5%)
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @return 임계값을 초과했으면 true
     * @throws IllegalArgumentException 현재 비중이 유효하지 않은 경우
     */
    public boolean exceedsThreshold(double currentWeight) {
        if (currentWeight < 0.0 || currentWeight > 1.0) {
            throw new IllegalArgumentException("현재 비중이 유효하지 않습니다: " + currentWeight);
        }
        
        double normalizedCurrentWeight = normalizeWeight(currentWeight);
        double deviation = Math.abs(normalizedCurrentWeight - this.targetWeight);
        
        // 상대적 임계값 계산 (목표 비중 × 임계값 비율)
        double relativeThreshold = this.targetWeight * this.thresholdPercentage;
        
        return deviation > relativeThreshold;
    }

    /**
     * 목표 비중에서 현재 비중의 편차를 계산
     * 양수면 목표보다 높음, 음수면 목표보다 낮음
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @return 비중 편차 (-1.0 ~ 1.0)
     * @throws IllegalArgumentException 현재 비중이 유효하지 않은 경우
     */
    public double getWeightDeviation(double currentWeight) {
        if (currentWeight < 0.0 || currentWeight > 1.0) {
            throw new IllegalArgumentException("현재 비중이 유효하지 않습니다: " + currentWeight);
        }
        
        double normalizedCurrentWeight = normalizeWeight(currentWeight);
        return normalizedCurrentWeight - this.targetWeight;
    }

    /**
     * 비중 편차의 절댓값을 반환
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @return 비중 편차의 절댓값 (0.0 ~ 1.0)
     */
    public double getAbsoluteWeightDeviation(double currentWeight) {
        return Math.abs(getWeightDeviation(currentWeight));
    }

    /**
     * 현재 비중이 목표 비중보다 높은지 확인
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @return 현재 비중이 목표보다 높으면 true
     */
    public boolean isOverWeight(double currentWeight) {
        return getWeightDeviation(currentWeight) > 0;
    }

    /**
     * 현재 비중이 목표 비중보다 낮은지 확인
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @return 현재 비중이 목표보다 낮으면 true
     */
    public boolean isUnderWeight(double currentWeight) {
        return getWeightDeviation(currentWeight) < 0;
    }

    /**
     * 종목의 복사본을 생성
     * 불변 객체로 사용하기 위한 방어적 복사
     * 
     * @return Stock 객체의 복사본
     */
    public Stock copy() {
        return new Stock(this.stockCode, this.targetWeight, this.thresholdPercentage);
    }

    /**
     * 목표 비중을 백분율로 반환
     * 
     * @return 목표 비중 백분율
     */
    public double getTargetWeightPercentage() {
        return targetWeight * 100.0;
    }

    /**
     * 임계값을 백분율로 반환
     * 
     * @return 임계값 백분율
     */
    public double getThresholdPercentage() {
        return thresholdPercentage * 100.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Stock stock = (Stock) obj;
        return Objects.equals(stockCode, stock.stockCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockCode);
    }

    /**
     * 상세 정보를 포함한 문자열 표현
     * 
     * @return 상세 정보 문자열
     */
    public String toDetailedString() {
        return String.format("Stock{code='%s', targetWeight=%.2f%%, threshold=%.2f%%}", 
                stockCode, getTargetWeightPercentage(), getThresholdPercentage());
    }
}