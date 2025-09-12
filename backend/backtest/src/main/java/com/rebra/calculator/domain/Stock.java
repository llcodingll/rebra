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
     * 임계값 비율 (0.0 ~ 1.0)
     * 목표 비중에서 이 값만큼 벗어나면 리밸런싱을 실행
     * 예: 0.05 = 5% (목표 30%에서 35% 또는 25%가 되면 리밸런싱)
     */
    private double thresholdPercentage;
    
    /**
     * 원본 가중치 (정수, 필수)
     * 사용자가 설정한 원본 가중치로, 비중 재계산의 기준이 됨
     * 예: 3 (전체 가중치 합에서 3/10 = 30%의 비중을 가짐)
     */
    private int originalWeight;
    
    /**
     * 초기 보유 수량
     * 백테스트 시작 시점에서의 보유 주식 수량
     */
    private int initialQuantity;
    
    /**
     * 목표 비중 (0.0 ~ 1.0)
     * 리밸런싱 시 달성하고자 하는 목표 비중
     * 유효한 종목들로만 재계산된 정규화된 비중
     */
    private double targetWeight;

    
    /**
     * Stock 생성자
     * 
     * @param stockCode 종목 코드
     * @param originalWeight 원본 가중치 (정수, 양수)
     * @param thresholdPercentage 임계값 비율 (0.0 ~ 1.0)
     * @param initialQuantity 초기 보유 수량 (0 이상)
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     */
    public Stock(String stockCode, int originalWeight, double thresholdPercentage, int initialQuantity) {
        validateParameters(stockCode, thresholdPercentage);
        
        if (originalWeight <= 0) {
            throw new IllegalArgumentException("원본 가중치는 양수여야 합니다: " + originalWeight);
        }
        
        if (initialQuantity < 0) {
            throw new IllegalArgumentException("초기 보유 수량은 0 이상이어야 합니다: " + initialQuantity);
        }
        
        this.stockCode = stockCode.trim().toUpperCase();
        this.originalWeight = originalWeight;
        this.thresholdPercentage = normalizeWeight(thresholdPercentage);
        this.initialQuantity = initialQuantity;
        this.targetWeight = 0.0; // 초기값, 리밸런싱 시 재계산됨
    }
    
    /**
     * 기존 호환성을 위한 생성자 (initialQuantity = 0으로 기본값 설정)
     * @deprecated 새로운 생성자를 사용하세요
     */
    @Deprecated
    public Stock(String stockCode, int originalWeight, double thresholdPercentage) {
        this(stockCode, originalWeight, thresholdPercentage, 0);
    }

    /**
     * 매개변수 유효성 검증
     * 
     * @param stockCode 종목 코드
     * @param thresholdPercentage 임계값 비율
     * @throws IllegalArgumentException 유효하지 않은 매개변수
     */
    private void validateParameters(String stockCode, double thresholdPercentage) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다.");
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
     * 현재 비중이 목표 비중에서 임계값을 초과했는지 확인 (내부 targetWeight 사용)
     * 리밸런싱 필요 여부를 판단하는 데 사용
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @return 임계값을 초과했으면 true
     * @throws IllegalArgumentException 현재 비중이 유효하지 않은 경우
     */
    public boolean exceedsThreshold(double currentWeight) {
        return exceedsThreshold(currentWeight, this.targetWeight);
    }

    /**
     * 현재 비중이 목표 비중에서 임계값을 초과했는지 확인
     * 리밸런싱 필요 여부를 판단하는 데 사용
     * 임계값은 목표 비중의 상대적 비율로 계산됨
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @param targetWeight 목표 비중 (0.0 ~ 1.0)
     * @return 임계값을 초과했으면 true
     * @throws IllegalArgumentException 현재 비중이나 목표 비중이 유효하지 않은 경우
     */
    public boolean exceedsThreshold(double currentWeight, double targetWeight) {
        if (currentWeight < 0.0 || currentWeight > 1.0) {
            throw new IllegalArgumentException("현재 비중이 유효하지 않습니다: " + currentWeight);
        }
        
        if (targetWeight < 0.0 || targetWeight > 1.0) {
            throw new IllegalArgumentException("목표 비중이 유효하지 않습니다: " + targetWeight);
        }
        
        double normalizedCurrentWeight = normalizeWeight(currentWeight);
        double normalizedTargetWeight = normalizeWeight(targetWeight);
        double deviation = Math.abs(normalizedCurrentWeight - normalizedTargetWeight);
        
        // 상대적 임계값 계산 (목표 비중 × 임계값 비율)
        double relativeThreshold = normalizedTargetWeight * this.thresholdPercentage;
        
        return deviation > relativeThreshold;
    }

    /**
     * 목표 비중에서 현재 비중의 편차를 계산
     * 양수면 목표보다 높음, 음수면 목표보다 낮음
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @param targetWeight 목표 비중 (0.0 ~ 1.0)
     * @return 비중 편차 (-1.0 ~ 1.0)
     * @throws IllegalArgumentException 현재 비중이나 목표 비중이 유효하지 않은 경우
     */
    public double getWeightDeviation(double currentWeight, double targetWeight) {
        if (currentWeight < 0.0 || currentWeight > 1.0) {
            throw new IllegalArgumentException("현재 비중이 유효하지 않습니다: " + currentWeight);
        }
        
        if (targetWeight < 0.0 || targetWeight > 1.0) {
            throw new IllegalArgumentException("목표 비중이 유효하지 않습니다: " + targetWeight);
        }
        
        double normalizedCurrentWeight = normalizeWeight(currentWeight);
        double normalizedTargetWeight = normalizeWeight(targetWeight);
        return normalizedCurrentWeight - normalizedTargetWeight;
    }

    /**
     * 비중 편차의 절댓값을 반환
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @param targetWeight 목표 비중 (0.0 ~ 1.0)
     * @return 비중 편차의 절댓값 (0.0 ~ 1.0)
     */
    public double getAbsoluteWeightDeviation(double currentWeight, double targetWeight) {
        return Math.abs(getWeightDeviation(currentWeight, targetWeight));
    }

    /**
     * 현재 비중이 목표 비중보다 높은지 확인
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @param targetWeight 목표 비중 (0.0 ~ 1.0)
     * @return 현재 비중이 목표보다 높으면 true
     */
    public boolean isOverWeight(double currentWeight, double targetWeight) {
        return getWeightDeviation(currentWeight, targetWeight) > 0;
    }

    /**
     * 현재 비중이 목표 비중보다 낮은지 확인
     * 
     * @param currentWeight 현재 비중 (0.0 ~ 1.0)
     * @param targetWeight 목표 비중 (0.0 ~ 1.0)
     * @return 현재 비중이 목표보다 낮으면 true
     */
    public boolean isUnderWeight(double currentWeight, double targetWeight) {
        return getWeightDeviation(currentWeight, targetWeight) < 0;
    }

    /**
     * 종목의 복사본을 생성
     * 불변 객체로 사용하기 위한 방어적 복사
     * 
     * @return Stock 객체의 복사본
     */
    public Stock copy() {
        return new Stock(this.stockCode, this.originalWeight, this.thresholdPercentage);
    }

    /**
     * 원본 가중치를 대상 가중치로 계산하여 목표 비중을 백분율로 반환
     * 
     * @param totalWeight 전체 가중치 합계
     * @return 목표 비중 백분율
     * @throws IllegalArgumentException 전체 가중치가 0 이하인 경우
     */
    public double getTargetWeightPercentage(int totalWeight) {
        if (totalWeight <= 0) {
            throw new IllegalArgumentException("전체 가중치는 양수여야 합니다: " + totalWeight);
        }
        return ((double) originalWeight / totalWeight) * 100.0;
    }

    /**
     * 임계값을 백분율로 반환
     * 
     * @return 임계값 백분율
     */
    public double getThresholdPercentage() {
        return thresholdPercentage * 100.0;
    }

    /**
     * 목표 비중을 계산하여 반환한다
     * 원본 가중치와 전체 가중치 합을 기반으로 계산
     * 
     * @param totalWeight 전체 가중치 합계
     * @return 목표 비중 (0.0 ~ 1.0)
     * @throws IllegalArgumentException 전체 가중치가 0 이하인 경우
     */
    public double getTargetWeight(int totalWeight) {
        if (totalWeight <= 0) {
            throw new IllegalArgumentException("전체 가중치는 양수여야 합니다: " + totalWeight);
        }
        return normalizeWeight((double) originalWeight / totalWeight);
    }

    /**
     * 임계값을 설정한다
     * 
     * @param thresholdPercentage 새로운 임계값 (0.0 ~ 1.0)
     * @throws IllegalArgumentException 임계값이 유효하지 않은 경우
     */
    public void setThresholdPercentage(double thresholdPercentage) {
        if (thresholdPercentage < 0.0 || thresholdPercentage > 1.0) {
            throw new IllegalArgumentException("임계값은 0.0과 1.0 사이여야 합니다: " + thresholdPercentage);
        }
        
        this.thresholdPercentage = normalizeWeight(thresholdPercentage);
    }

    /**
     * 원본 가중치를 반환한다
     * 
     * @return 원본 가중치
     */
    public int getOriginalWeight() {
        return originalWeight;
    }

    /**
     * 원본 가중치를 설정한다
     * 
     * @param originalWeight 새로운 원본 가중치 (양수여야 함)
     * @throws IllegalArgumentException 원본 가중치가 유효하지 않은 경우
     */
    public void setOriginalWeight(int originalWeight) {
        if (originalWeight <= 0) {
            throw new IllegalArgumentException("원본 가중치는 양수여야 합니다: " + originalWeight);
        }
        
        this.originalWeight = originalWeight;
    }

    /**
     * 원본 가중치가 설정되었는지 확인한다
     * 
     * @return 원본 가중치가 설정되었으면 true
     */
    public boolean hasOriginalWeight() {
        return originalWeight > 0;
    }

    /**
     * 목표 비중을 반환한다
     * 
     * @return 목표 비중 (0.0 ~ 1.0)
     */
    public double getTargetWeight() {
        return targetWeight;
    }

    /**
     * 목표 비중을 설정한다
     * 
     * @param targetWeight 새로운 목표 비중 (0.0 ~ 1.0)
     * @throws IllegalArgumentException 목표 비중이 유효하지 않은 경우
     */
    public void setTargetWeight(double targetWeight) {
        if (targetWeight < 0.0 || targetWeight > 1.0) {
            throw new IllegalArgumentException("목표 비중은 0.0과 1.0 사이여야 합니다: " + targetWeight);
        }
        
        this.targetWeight = normalizeWeight(targetWeight);
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
     * @param totalWeight 전체 가중치 합계
     * @return 상세 정보 문자열
     */
    public String toDetailedString(int totalWeight) {
        return String.format("Stock{code='%s', originalWeight=%d, targetWeight=%.2f%%, threshold=%.2f%%}", 
                stockCode, originalWeight, getTargetWeightPercentage(totalWeight), getThresholdPercentage());
    }
}