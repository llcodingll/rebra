package com.rebra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 등락률 순위 유형을 정의하는 열거형
 * 주식의 등락률 기준 순위 분류를 나타낸다.
 */
public enum FluctuationRankingType {

    /**
     * 급상승 종목 순위
     * 전일 대비 상승률이 높은 종목들을 순서대로 정렬
     */
    RISING("RISING", "급상승", "Top rising stocks by price change rate"),

    /**
     * 급하락 종목 순위
     * 전일 대비 하락률이 높은 종목들을 순서대로 정렬
     */
    FALLING("FALLING", "급하락", "Top falling stocks by price change rate");

    private final String code;           // API 응답용 영어 코드
    private final String displayName;    // 한글 표시명
    private final String description;    // 영어 설명

    /**
     * FluctuationRankingType 생성자
     *
     * @param code API 응답에 사용될 영어 코드
     * @param displayName 화면 표시용 한글 이름
     * @param description 상세 설명
     */
    FluctuationRankingType(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * JSON 직렬화 시 사용될 값을 반환한다.
     * API 응답에서 "RISING", "FALLING" 형태로 반환된다.
     *
     * @return 영어 코드
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * 화면 표시용 한글 이름을 반환한다.
     *
     * @return 한글 표시명
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 등락률 순위 유형에 대한 상세 설명을 반환한다.
     *
     * @return 영어 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 코드 문자열로부터 해당하는 FluctuationRankingType을 찾는다.
     * JSON 역직렬화 시 사용된다.
     *
     * @param code 검색할 코드 문자열
     * @return 해당하는 FluctuationRankingType
     * @throws IllegalArgumentException 유효하지 않은 코드인 경우
     */
    @JsonCreator
    public static FluctuationRankingType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Ranking type code cannot be null or empty");
        }

        for (FluctuationRankingType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ranking type: " + code);
    }

    /**
     * 기본 등락률 순위 유형을 반환한다.
     * 설정이 없거나 잘못된 경우 사용할 기본값
     *
     * @return 기본 등락률 순위 유형 (RISING)
     */
    public static FluctuationRankingType getDefault() {
        return RISING;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, description);
    }
}