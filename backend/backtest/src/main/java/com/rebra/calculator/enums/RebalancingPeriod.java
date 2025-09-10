package com.rebra.calculator.enums;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * 리밸런싱 주기를 정의하는 열거형
 * PERIODIC 리밸런싱 유형에서 사용되며, 언제 리밸런싱을 실행할지 주기를 결정한다.
 */
public enum RebalancingPeriod {

    /**
     * 월말 리밸런싱
     * 매월 마지막 영업일에 리밸런싱을 실행한다.
     * 가장 일반적인 리밸런싱 주기로, 적절한 빈도를 제공한다.
     * 
     * 특징:
     * - 연간 약 12회 리밸런싱
     * - 단기 변동성의 영향을 적절히 흡수
     * - 거래비용과 효과의 균형점
     */
    MONTHLY("월말", "매월 마지막 날 리밸런싱", 1),

    /**
     * 분기말 리밸런싱  
     * 매 분기 마지막 영업일(3월, 6월, 9월, 12월 말)에 리밸런싱을 실행한다.
     * 장기적 관점에서 안정적인 포트폴리오 운용을 추구한다.
     * 
     * 특징:
     * - 연간 4회 리밸런싱
     * - 낮은 거래비용
     * - 장기 추세에 집중
     * - 단기 변동성 무시
     */
    QUARTERLY("분기말", "매 분기 마지막 날 리밸런싱", 3),

    /**
     * 반기말 리밸런싱
     * 6개월마다 (6월말, 12월말) 리밸런싱을 실행한다.
     * 매우 보수적인 리밸런싱 전략이다.
     * 
     * 특징:
     * - 연간 2회 리밸런싱
     * - 매우 낮은 거래비용
     * - 장기 투자 관점
     * - 시장 효율성에 대한 믿음
     */
    SEMI_ANNUALLY("반기말", "매 반기 마지막 날 리밸런싱", 6),

    /**
     * 연말 리밸런싱
     * 매년 마지막 영업일에만 리밸런싱을 실행한다.
     * 극도로 보수적인 접근법으로 거의 바이앤홀드에 가깝다.
     * 
     * 특징:
     * - 연간 1회 리밸런싱
     * - 최소한의 거래비용
     * - 장기 관점의 극대화
     * - 세금 효율적 (장기보유)
     */
    ANNUALLY("연말", "매년 마지막 날 리밸런싱", 12);

    private final String displayName;
    private final String description;
    private final int monthInterval;

    /**
     * RebalancingPeriod 생성자
     * 
     * @param displayName 화면 표시용 이름
     * @param description 상세 설명
     * @param monthInterval 개월 단위 간격
     */
    RebalancingPeriod(String displayName, String description, int monthInterval) {
        this.displayName = displayName;
        this.description = description;
        this.monthInterval = monthInterval;
    }

    /**
     * 화면 표시용 이름을 반환한다.
     * 
     * @return 표시용 이름
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 리밸런싱 주기에 대한 상세 설명을 반환한다.
     * 
     * @return 상세 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 개월 단위 간격을 반환한다.
     * 
     * @return 개월 수
     */
    public int getMonthInterval() {
        return monthInterval;
    }

    /**
     * 주어진 날짜가 리밸런싱 실행일인지 확인한다.
     * 각 주기에 따라 해당 기간의 마지막 날인지 판단한다.
     * 
     * @param date 확인할 날짜
     * @return 리밸런싱 실행일이면 true
     */
    public boolean isRebalancingDate(LocalDate date) {
        if (date == null) {
            return false;
        }

        switch (this) {
            case MONTHLY:
                // 월의 마지막 날인지 확인
                return date.equals(date.with(TemporalAdjusters.lastDayOfMonth()));

            case QUARTERLY:
                // 분기 마지막 달(3,6,9,12월)의 마지막 날인지 확인
                int month = date.getMonthValue();
                boolean isQuarterEndMonth = (month % 3 == 0);
                boolean isLastDayOfMonth = date.equals(date.with(TemporalAdjusters.lastDayOfMonth()));
                return isQuarterEndMonth && isLastDayOfMonth;

            case SEMI_ANNUALLY:
                // 반기 마지막 달(6,12월)의 마지막 날인지 확인
                boolean isSemiAnnualEndMonth = (date.getMonthValue() == 6 || date.getMonthValue() == 12);
                boolean isSemiAnnualLastDay = date.equals(date.with(TemporalAdjusters.lastDayOfMonth()));
                return isSemiAnnualEndMonth && isSemiAnnualLastDay;

            case ANNUALLY:
                // 연말(12월 마지막 날)인지 확인
                return date.getMonthValue() == 12 && 
                       date.equals(date.with(TemporalAdjusters.lastDayOfMonth()));

            default:
                return false;
        }
    }

    /**
     * 다음 리밸런싱 예정일을 계산한다.
     * 
     * @param currentDate 현재 날짜
     * @return 다음 리밸런싱 예정일
     */
    public LocalDate getNextRebalancingDate(LocalDate currentDate) {
        if (currentDate == null) {
            return null;
        }

        LocalDate nextDate = currentDate.plusDays(1);

        // 최대 2년 후까지 검색 (무한루프 방지)
        LocalDate maxDate = currentDate.plusYears(2);

        while (nextDate.isBefore(maxDate) || nextDate.isEqual(maxDate)) {
            if (isRebalancingDate(nextDate)) {
                return nextDate;
            }
            nextDate = nextDate.plusDays(1);
        }

        // 찾지 못한 경우 (이론적으로 발생하지 않아야 함)
        throw new IllegalStateException("다음 리밸런싱 날짜를 찾을 수 없습니다: " + currentDate);
    }

    /**
     * 문자열로부터 RebalancingPeriod를 찾는다.
     * 
     * @param value 검색할 문자열
     * @return 해당하는 RebalancingPeriod, 없으면 null
     */
    public static RebalancingPeriod fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String searchValue = value.trim().toLowerCase();

        for (RebalancingPeriod period : values()) {
            // enum 이름으로 비교
            if (period.name().toLowerCase().equals(searchValue)) {
                return period;
            }
            // 표시 이름으로 비교
            if (period.displayName.toLowerCase().equals(searchValue)) {
                return period;
            }
        }

        return null;
    }

    /**
     * 기본 리밸런싱 주기를 반환한다.
     * 
     * @return 기본 리밸런싱 주기 (MONTHLY)
     */
    public static RebalancingPeriod getDefault() {
        return MONTHLY;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, description);
    }
}