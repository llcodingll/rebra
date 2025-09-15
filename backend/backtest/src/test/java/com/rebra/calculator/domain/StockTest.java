package com.rebra.calculator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

/**
 * Stock 도메인 클래스 단위 테스트
 * 주식 종목 정보의 생성, 검증, 계산 로직을 테스트한다.
 */
class StockTest {

    @Nested
    @DisplayName("Stock 객체 생성 테스트")
    class StockCreationTest {

        @Test
        @DisplayName("정상적인 Stock 객체 생성")
        void createValidStock() {
            // given
            String stockCode = "005930";
            int originalWeight = 40;
            double thresholdPercentage = 0.05;
            int initialQuantity = 100;

            // when
            Stock stock = new Stock(stockCode, originalWeight, thresholdPercentage, initialQuantity);

            // then
            assertThat(stock.getStockCode()).isEqualTo(stockCode);
            assertThat(stock.getOriginalWeight()).isEqualTo(originalWeight);
            assertThat(stock.getInitialQuantity()).isEqualTo(initialQuantity);
            assertThat(stock.getThresholdPercentage()).isEqualTo(thresholdPercentage * 100.0); // getThresholdPercentage returns percentage
            assertThat(stock.getTargetWeight()).isZero(); // 초기값 0
        }

        @Test
        @DisplayName("null 종목코드로 생성 시 예외")
        void createStockWithNullCode() {
            // when & then
            assertThatThrownBy(() -> 
                new Stock(null, 40, 0.05, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종목 코드는 필수입니다");
        }

        @Test
        @DisplayName("빈 종목코드로 생성 시 예외")
        void createStockWithEmptyCode() {
            // when & then
            assertThatThrownBy(() -> 
                new Stock("", 40, 0.05, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종목 코드는 필수입니다");
        }

        @Test
        @DisplayName("공백 종목코드로 생성 시 예외")
        void createStockWithBlankCode() {
            // when & then
            assertThatThrownBy(() -> 
                new Stock("   ", 40, 0.05, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종목 코드는 필수입니다");
        }

        @Test
        @DisplayName("잘못된 종목코드 형식으로 생성 시 예외")
        void createStockWithInvalidCodeFormat() {
            // when & then
            assertThatThrownBy(() -> 
                new Stock("12345", 40, 0.05, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종목 코드는 6자리 숫자여야 합니다");
        }

        @Test
        @DisplayName("음수 원본 가중치로 생성 시 예외")
        void createStockWithNegativeWeight() {
            // when & then
            assertThatThrownBy(() -> 
                new Stock("005930", -10, 0.05, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원본 가중치는 양수여야 합니다");
        }

        @Test
        @DisplayName("음수 초기 수량으로 생성 시 예외")
        void createStockWithNegativeQuantity() {
            // when & then
            assertThatThrownBy(() -> 
                new Stock("005930", 40, 0.05, -10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("초기 보유 수량은 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("음수 임계값으로 생성 시 예외")
        void createStockWithNegativeThreshold() {
            // when & then
            assertThatThrownBy(() -> 
                new Stock("005930", 40, -0.01, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("임계값은 0.0과 1.0 사이여야 합니다");
        }

        @Test
        @DisplayName("0 값들로 생성 가능")
        void createStockWithZeroValues() {
            // when
            Stock stock = new Stock("005930", 1, 0.0, 0);

            // then
            assertThat(stock.getOriginalWeight()).isEqualTo(1);
            assertThat(stock.getInitialQuantity()).isZero();
            assertThat(stock.getThresholdPercentage()).isZero();
        }
    }

    @Nested
    @DisplayName("목표 비중 계산 및 설정 테스트")
    class TargetWeightTest {

        @Test
        @DisplayName("목표 비중 설정")
        void setTargetWeight() {
            // given
            Stock stock = new Stock("005930", 40, 0.05, 100);
            double targetWeight = 0.4;

            // when
            stock.setTargetWeight(targetWeight);

            // then
            assertThat(stock.getTargetWeight()).isEqualTo(targetWeight);
        }

        @Test
        @DisplayName("음수 목표 비중 설정 시 예외")
        void setNegativeTargetWeight() {
            // given
            Stock stock = new Stock("005930", 40, 0.05, 100);

            // when & then
            assertThatThrownBy(() -> stock.setTargetWeight(-0.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("목표 비중은 0.0과 1.0 사이여야 합니다");
        }

        @Test
        @DisplayName("1 초과 목표 비중 설정 시 예외")
        void setTargetWeightOver100Percent() {
            // given
            Stock stock = new Stock("005930", 40, 0.05, 100);

            // when & then
            assertThatThrownBy(() -> stock.setTargetWeight(1.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("목표 비중은 0.0과 1.0 사이여야 합니다");
        }

        @Test
        @DisplayName("경계값 목표 비중 설정")
        void setBoundaryTargetWeights() {
            // given
            Stock stock = new Stock("005930", 40, 0.05, 100);

            // when & then
            assertThatCode(() -> stock.setTargetWeight(0.0)).doesNotThrowAnyException();
            assertThatCode(() -> stock.setTargetWeight(1.0)).doesNotThrowAnyException();
            
            assertThat(stock.getTargetWeight()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("비중 계산 로직 테스트")
    class WeightCalculationTest {

        @Test
        @DisplayName("정규화된 비중 계산")
        void calculateNormalizedWeight() {
            // given
            Stock stock1 = new Stock("005930", 40, 0.05, 100);
            Stock stock2 = new Stock("000660", 30, 0.05, 50);
            Stock stock3 = new Stock("035420", 30, 0.05, 25);
            
            int totalWeight = 40 + 30 + 30; // 100

            // when
            double normalizedWeight1 = (double) stock1.getOriginalWeight() / totalWeight;
            double normalizedWeight2 = (double) stock2.getOriginalWeight() / totalWeight;
            double normalizedWeight3 = (double) stock3.getOriginalWeight() / totalWeight;

            // then
            assertThat(normalizedWeight1).isEqualTo(0.4);
            assertThat(normalizedWeight2).isEqualTo(0.3);
            assertThat(normalizedWeight3).isEqualTo(0.3);
            assertThat(normalizedWeight1 + normalizedWeight2 + normalizedWeight3).isEqualTo(1.0);
        }

        @Test
        @DisplayName("불균등 비중 정규화")
        void calculateUnequalNormalizedWeight() {
            // given
            Stock stock1 = new Stock("005930", 50, 0.05, 100);
            Stock stock2 = new Stock("000660", 30, 0.05, 50);
            Stock stock3 = new Stock("035420", 20, 0.05, 25);
            
            int totalWeight = 50 + 30 + 20; // 100

            // when
            double normalizedWeight1 = (double) stock1.getOriginalWeight() / totalWeight;
            double normalizedWeight2 = (double) stock2.getOriginalWeight() / totalWeight;
            double normalizedWeight3 = (double) stock3.getOriginalWeight() / totalWeight;

            // then
            assertThat(normalizedWeight1).isEqualTo(0.5);
            assertThat(normalizedWeight2).isEqualTo(0.3);
            assertThat(normalizedWeight3).isEqualTo(0.2);
        }
    }

    @Nested
    @DisplayName("임계값 관련 테스트")
    class ThresholdTest {

        @Test
        @DisplayName("임계값 퍼센트 계산")
        void calculateThresholdPercentage() {
            // given
            Stock stock = new Stock("005930", 40, 0.05, 100);

            // when
            double thresholdPercentage = stock.getThresholdPercentage();

            // then
            assertThat(thresholdPercentage).isEqualTo(0.05); // 5%
        }

        @Test
        @DisplayName("임계값 절대값 계산")
        void calculateAbsoluteThreshold() {
            // given
            Stock stock = new Stock("005930", 40, 0.05, 100);
            stock.setTargetWeight(0.4); // 40% 목표 비중
            double portfolioValue = 10000000.0; // 1천만원

            // when
            double targetAmount = portfolioValue * stock.getTargetWeight(); // 400만원
            double thresholdAmount = targetAmount * stock.getThresholdPercentage(); // 20만원

            // then
            assertThat(targetAmount).isEqualTo(4000000.0);
            assertThat(thresholdAmount).isEqualTo(200000.0);
        }

        @Test
        @DisplayName("다양한 임계값 크기 테스트")
        void variousThresholdSizes() {
            // given
            Stock conservativeStock = new Stock("005930", 40, 0.01, 100); // 1%
            Stock moderateStock = new Stock("000660", 30, 0.05, 50);    // 5%
            Stock aggressiveStock = new Stock("035420", 30, 0.10, 25);       // 10%

            // when & then
            assertThat(conservativeStock.getThresholdPercentage()).isEqualTo(0.01);
            assertThat(moderateStock.getThresholdPercentage()).isEqualTo(0.05);
            assertThat(aggressiveStock.getThresholdPercentage()).isEqualTo(0.10);
        }
    }

    @Nested
    @DisplayName("Stock 객체 비교 및 식별 테스트")
    class StockComparisonTest {

        @Test
        @DisplayName("같은 종목코드 Stock 객체 equals")
        void equalStocksWithSameCode() {
            // given
            Stock stock1 = new Stock("005930", 40, 0.05, 100);
            Stock stock2 = new Stock("005930", 40, 0.05, 100);

            // when & then
            assertThat(stock1).isEqualTo(stock2);
            assertThat(stock1.hashCode()).isEqualTo(stock2.hashCode());
        }

        @Test
        @DisplayName("다른 종목코드 Stock 객체 not equals")
        void differentStocksWithDifferentCode() {
            // given
            Stock stock1 = new Stock("005930", 40, 0.05, 100);
            Stock stock2 = new Stock("000660", 30, 0.05, 50);

            // when & then
            assertThat(stock1).isNotEqualTo(stock2);
        }

        @Test
        @DisplayName("같은 종목코드, 다른 속성값 equals")
        void sameCodeDifferentAttributes() {
            // given
            Stock stock1 = new Stock("005930", 40, 0.05, 100);
            Stock stock2 = new Stock("005930", 50, 0.10, 200);

            // when & then
            assertThat(stock1).isEqualTo(stock2); // 종목코드만으로 비교
        }

        @Test
        @DisplayName("toString 메서드 테스트")
        void toStringMethod() {
            // given
            Stock stock = new Stock("005930", 40, 0.05, 100);
            stock.setTargetWeight(0.4);

            // when
            String stockString = stock.toString();

            // then
            assertThat(stockString).contains("005930");
            // Note: Stock doesn't have stockName field anymore
            assertThat(stockString).contains("40");
            assertThat(stockString).contains("100");
        }
    }

    @Nested
    @DisplayName("복사 및 변경 테스트")
    class CopyAndModificationTest {

        @Test
        @DisplayName("Stock 객체 복사")
        void copyStock() {
            // given
            Stock originalStock = new Stock("005930", 40, 0.05, 100);
            originalStock.setTargetWeight(0.4);

            // when
            Stock copiedStock = originalStock.copy();

            // then
            assertThat(copiedStock).isEqualTo(originalStock);
            assertThat(copiedStock.getStockCode()).isEqualTo(originalStock.getStockCode());
            // Note: Stock doesn't have stockName field anymore
            assertThat(copiedStock.getOriginalWeight()).isEqualTo(originalStock.getOriginalWeight());
            assertThat(copiedStock.getInitialQuantity()).isEqualTo(originalStock.getInitialQuantity());
            assertThat(copiedStock.getThresholdPercentage()).isEqualTo(originalStock.getThresholdPercentage());
            assertThat(copiedStock.getTargetWeight()).isEqualTo(originalStock.getTargetWeight());
            
            // 다른 객체 인스턴스인지 확인
            assertThat(copiedStock).isNotSameAs(originalStock);
        }

        @Test
        @DisplayName("복사 후 원본 수정이 복사본에 영향 없음")
        void copyIndependence() {
            // given
            Stock originalStock = new Stock("005930", 40, 0.05, 100);
            originalStock.setTargetWeight(0.4);
            
            Stock copiedStock = originalStock.copy();

            // when
            originalStock.setTargetWeight(0.6);

            // then
            assertThat(copiedStock.getTargetWeight()).isEqualTo(0.4); // 변경되지 않음
            assertThat(originalStock.getTargetWeight()).isEqualTo(0.6);
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class RealUsageScenarioTest {

        @Test
        @DisplayName("포트폴리오 구성 시나리오")
        void portfolioCompositionScenario() {
            // given - 3개 종목으로 포트폴리오 구성
            Stock samsung = new Stock("005930", 40, 0.05, 167);
            Stock skhynix = new Stock("000660", 30, 0.05, 83);
            Stock naver = new Stock("035420", 30, 0.05, 50);
            
            int totalWeight = samsung.getOriginalWeight() + skhynix.getOriginalWeight() + naver.getOriginalWeight();

            // when - 정규화된 목표 비중 설정
            samsung.setTargetWeight((double) samsung.getOriginalWeight() / totalWeight);
            skhynix.setTargetWeight((double) skhynix.getOriginalWeight() / totalWeight);
            naver.setTargetWeight((double) naver.getOriginalWeight() / totalWeight);

            // then
            assertThat(samsung.getTargetWeight()).isEqualTo(0.4);
            assertThat(skhynix.getTargetWeight()).isEqualTo(0.3);
            assertThat(naver.getTargetWeight()).isEqualTo(0.3);
            
            double totalTargetWeight = samsung.getTargetWeight() + skhynix.getTargetWeight() + naver.getTargetWeight();
            assertThat(totalTargetWeight).isCloseTo(1.0, within(0.0001));
        }

        @Test
        @DisplayName("리밸런싱 필요 여부 판단 시나리오")
        void rebalancingDecisionScenario() {
            // given
            Stock stock = new Stock("005930", 40, 0.05, 100);
            stock.setTargetWeight(0.4);
            
            double portfolioValue = 10000000.0; // 1천만원
            double targetAmount = portfolioValue * stock.getTargetWeight(); // 400만원
            double thresholdAmount = targetAmount * stock.getThresholdPercentage(); // 20만원

            // when - 다양한 현재 보유 금액에 대한 임계값 초과 여부 확인
            double currentAmount1 = 3900000.0; // 390만원 (10만원 차이, 임계값 이내)
            double currentAmount2 = 3700000.0; // 370만원 (30만원 차이, 임계값 초과)
            double currentAmount3 = 4300000.0; // 430만원 (30만원 차이, 임계값 초과)

            // then
            assertThat(Math.abs(targetAmount - currentAmount1)).isLessThan(thresholdAmount);
            assertThat(Math.abs(targetAmount - currentAmount2)).isGreaterThan(thresholdAmount);
            assertThat(Math.abs(targetAmount - currentAmount3)).isGreaterThan(thresholdAmount);
        }

        @Test
        @DisplayName("종목 가중치 재조정 시나리오")
        void stockWeightRebalancingScenario() {
            // given - 초기 비중
            Stock stock1 = new Stock("005930", 40, 0.05, 100);
            Stock stock2 = new Stock("000660", 30, 0.05, 50);
            Stock stock3 = new Stock("035420", 30, 0.05, 25);
            
            // when - 가격 정보가 없어진 종목이 있는 경우 비중 재조정
            // 예: NAVER 상장폐지로 삼성전자와 SK하이닉스만 남은 경우
            int remainingWeight = stock1.getOriginalWeight() + stock2.getOriginalWeight(); // 70
            
            stock1.setTargetWeight((double) stock1.getOriginalWeight() / remainingWeight);
            stock2.setTargetWeight((double) stock2.getOriginalWeight() / remainingWeight);
            stock3.setTargetWeight(0.0); // 거래 불가

            // then
            assertThat(stock1.getTargetWeight()).isCloseTo(0.571, within(0.001)); // 40/70
            assertThat(stock2.getTargetWeight()).isCloseTo(0.429, within(0.001)); // 30/70
            assertThat(stock3.getTargetWeight()).isZero();
            
            double totalActiveWeight = stock1.getTargetWeight() + stock2.getTargetWeight();
            assertThat(totalActiveWeight).isCloseTo(1.0, within(0.001));
        }
    }
}