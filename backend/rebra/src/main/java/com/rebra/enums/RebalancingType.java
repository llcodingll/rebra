package com.rebra.enums;

/**
 * 리밸런싱 실행 조건을 정의하는 열거형
 * 백테스트에서 언제 리밸런싱을 실행할지 결정하는 전략을 나타낸다.
 */
public enum RebalancingType {

    /**
     * 임계값 기반 리밸런싱
     * 목표 비중에서 설정된 임계값(%)만큼 벗어날 때만 리밸런싱을 실행한다.
     * 예: 목표 비중이 30%인데 현재 비중이 35% (임계값 5% 초과)가 되면 리밸런싱
     * 
     * 장점:
     * - 불필요한 거래를 줄여 거래비용 절약
     * - 시장 변동성에 따른 자연스러운 비중 조정 허용
     * 
     * 단점:
     * - 장기간 리밸런싱이 발생하지 않을 수 있음
     * - 임계값 설정에 따라 결과가 크게 달라질 수 있음
     */
    THRESHOLD("임계값 기반", "목표 비중에서 임계값만큼 벗어날 때 리밸런싱"),

    /**
     * 주기적 리밸런싱
     * 설정된 주기(월말, 분기말 등)마다 무조건 리밸런싱을 실행한다.
     * 현재 비중과 관계없이 정해진 일정에 따라 목표 비중으로 조정한다.
     * 
     * 장점:
     * - 규칙적이고 예측 가능한 리밸런싱
     * - 장기적으로 일관된 포트폴리오 유지
     * - 감정적 판단 배제
     * 
     * 단점:
     * - 불필요한 거래 발생 가능성
     * - 거래비용 증가
     * - 시장 상황을 고려하지 않음
     */
    PERIODIC("주기적", "설정된 주기마다 무조건 리밸런싱");

    private final String displayName;
    private final String description;

    /**
     * RebalancingType 생성자
     * 
     * @param displayName 화면 표시용 이름
     * @param description 상세 설명
     */
    RebalancingType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
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
     * 리밸런싱 유형에 대한 상세 설명을 반환한다.
     * 
     * @return 상세 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 문자열로부터 RebalancingType을 찾는다.
     * 대소문자를 구분하지 않으며, 표시 이름으로도 검색 가능하다.
     * 
     * @param value 검색할 문자열
     * @return 해당하는 RebalancingType, 없으면 null
     */
    public static RebalancingType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String searchValue = value.trim().toLowerCase();

        for (RebalancingType type : values()) {
            // enum 이름으로 비교
            if (type.name().toLowerCase().equals(searchValue)) {
                return type;
            }
            // 표시 이름으로 비교
            if (type.displayName.toLowerCase().equals(searchValue)) {
                return type;
            }
        }

        return null;
    }

    /**
     * 기본 리밸런싱 유형을 반환한다.
     * 설정이 없거나 잘못된 경우 사용할 기본값
     * 
     * @return 기본 리밸런싱 유형 (THRESHOLD)
     */
    public static RebalancingType getDefault() {
        return THRESHOLD;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, description);
    }
}