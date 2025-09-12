package com.rebra.calculator.constant;

import java.math.RoundingMode;

/**
 * 백테스트 계산에 사용되는 모든 상수를 정의하는 클래스
 * 거래비용, 차입비용, 최소 거래 단위 등을 포함한다.
 */
public final class BacktestConstants {

    /**
     * 거래비용 관련 상수
     */
    public static final class TradingFees {
        
        /**
         * 매수 수수료율 (0.015%)
         * 한국 증권시장 기준 일반적인 온라인 증권사 수수료율
         */
        public static final double BUY_FEE_RATE = 0.00015;
        
        /**
         * 매도 수수료율 (0.015%)
         * 매수와 동일한 수수료율 적용
         */
        public static final double SELL_FEE_RATE = 0.00015;
        
        /**
         * 증권거래세율 (0.3%)
         * 매도 시에만 부과되는 세금
         */
        public static final double SECURITIES_TRANSACTION_TAX_RATE = 0.003;
        
        /**
         * 최소 수수료 (원)
         * 실제로는 증권사마다 다르지만, 백테스트에서는 무시
         */
        public static final double MINIMUM_FEE = 0.0;
        
        private TradingFees() {
            // 인스턴스화 방지
        }
    }

    /**
     * 차입비용 관련 상수
     */
    public static final class BorrowingCosts {
        
        /**
         * 연간 차입 이자율 (4.5%)
         * 신용거래 이자율을 기준으로 설정
         * 현실적인 수준의 차입 비용을 반영
         */
        public static final double ANNUAL_BORROWING_RATE = 0.045;
        
        /**
         * 일간 차입 이자율
         * 연간 이자율을 365일로 나눈 값
         */
        public static final double DAILY_BORROWING_RATE = ANNUAL_BORROWING_RATE / 365.0;
        
        /**
         * 차입 이자 계산 시 소수점 처리 방식
         * 소수점 둘째 자리에서 반올림
         */
        public static final RoundingMode BORROWING_COST_ROUNDING_MODE = RoundingMode.HALF_UP;
        
        private BorrowingCosts() {
            // 인스턴스화 방지
        }
    }

    /**
     * 거래 규칙 관련 상수
     */
    public static final class TradingRules {
        
        /**
         * 최소 거래 단위 (주)
         * 한국 주식시장의 일반적인 최소 거래 단위
         */
        public static final int MINIMUM_TRADING_UNIT = 1;
        
        /**
         * 주식 수량 계산 시 소수점 처리 방식
         * 소수점 이하는 버림 (실제 매매 가능한 정수 주식 수만큼만 거래)
         */
        public static final RoundingMode SHARE_QUANTITY_ROUNDING_MODE = RoundingMode.DOWN;
        
        /**
         * 금액 계산 시 소수점 처리 방식
         * 원 단위로 반올림
         */
        public static final RoundingMode AMOUNT_ROUNDING_MODE = RoundingMode.HALF_UP;
        
        /**
         * 비중 계산 시 소수점 자리수
         * 포트폴리오 비중을 계산할 때 사용할 소수점 자리수
         */
        public static final int WEIGHT_PRECISION = 6;
        
        private TradingRules() {
            // 인스턴스화 방지
        }
    }

    /**
     * 리밸런싱 관련 상수
     */
    public static final class Rebalancing {
        
        /**
         * 기본 리밸런싱 임계값 (5%)
         * 목표 비중에서 이 값만큼 벗어나면 리밸런싱 실행
         */
        public static final double DEFAULT_THRESHOLD_PERCENTAGE = 0.05;
        
        /**
         * 최소 리밸런싱 금액 (원)
         * 이 금액보다 작은 거래는 수수료 대비 효과가 미미하므로 제외
         */
        public static final double MINIMUM_REBALANCING_AMOUNT = 10000.0;
        
        /**
         * 리밸런싱 비중 허용 오차
         * 계산 오차로 인한 미세한 차이는 무시
         */
        public static final double WEIGHT_TOLERANCE = 0.0001;
        
        private Rebalancing() {
            // 인스턴스화 방지
        }
    }

    /**
     * 백테스트 계산 관련 상수
     */
    public static final class Calculation {
        
        /**
         * 수익률 계산 시 소수점 자리수
         * 백분율로 표시할 때 4자리까지 표시 (0.0123 = 1.23%)
         */
        public static final int RETURN_PRECISION = 4;
        
        /**
         * 무한 루프 방지를 위한 최대 반복 횟수
         * 리밸런싱 계산 등에서 예외 상황 발생 시 사용
         */
        public static final int MAX_ITERATION_COUNT = 1000;
        
        /**
         * 영업일 기준 연간 거래일 수
         * 미국 증권시장 기준 평균 영업일 수
         */
        public static final int TRADING_DAYS_PER_YEAR = 252;
        
        /**
         * 무위험 수익률 (3%)
         * 샤프 비율 계산에 사용되는 기준 수익률
         * 일반적으로 국고채 수익률을 기준으로 설정
         */
        public static final double RISK_FREE_RATE = 0.03;
        
        private Calculation() {
            // 인스턴스화 방지
        }
    }

    /**
     * 로깅 관련 상수
     */
    public static final class Logging {
        
        /**
         * 백테스트 진행 상황 로그 출력 간격
         * 매 N일마다 진행 상황을 로그로 출력
         */
        public static final int PROGRESS_LOG_INTERVAL_DAYS = 30;
        
        /**
         * 상세 거래 내역 로그 레벨 임계값
         * 이 금액 이상의 거래만 상세 로그 출력
         */
        public static final double DETAILED_TRADE_LOG_THRESHOLD = 100000.0;
        
        private Logging() {
            // 인스턴스화 방지
        }
    }

    /**
     * 에러 메시지 관련 상수
     */
    public static final class ErrorMessages {
        
        public static final String INVALID_INITIAL_CAPITAL = "초기 자본은 0보다 커야 합니다.";
        public static final String INVALID_TARGET_WEIGHT = "목표 비중의 합이 1.0이 아닙니다.";
        public static final String INVALID_DATE_RANGE = "시작일이 종료일보다 늦습니다.";
        public static final String MISSING_STOCK_DATA = "필요한 주식 데이터가 없습니다.";
        public static final String INVALID_THRESHOLD = "임계값은 0과 1 사이여야 합니다.";
        public static final String CALCULATION_ERROR = "백테스트 계산 중 오류가 발생했습니다.";
        
        private ErrorMessages() {
            // 인스턴스화 방지
        }
    }

    // 전체 클래스 인스턴스화 방지
    private BacktestConstants() {
        throw new UnsupportedOperationException("Constants 클래스는 인스턴스화할 수 없습니다.");
    }
}