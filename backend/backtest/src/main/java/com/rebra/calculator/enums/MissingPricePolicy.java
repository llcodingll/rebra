package com.rebra.calculator.enums;

/**
 * 가격 데이터가 누락된 경우의 처리 정책을 정의하는 열거형
 * 거래정지, 상장폐지 등으로 특정 날짜에 종목의 가격 데이터가 없을 때 사용
 */
public enum MissingPricePolicy {
    
    /**
     * 해당 종목을 리밸런싱에서 제외
     * 가격이 없는 종목은 매매하지 않고, 기존 보유량 유지
     * 목표 비중은 나머지 종목들로 재분배
     */
    SKIP_STOCK("종목 제외"),
    
    /**
     * 이전 거래일의 가격을 사용
     * 직전 유효한 가격을 찾아서 현재 가격으로 간주
     * 연속으로 여러 일 거래정지인 경우에도 적용
     */
    USE_PREVIOUS("이전 가격 사용"),
    
    /**
     * 해당 날짜의 리밸런싱을 중단
     * 가격 누락 종목이 있으면 그 날짜의 리밸런싱을 수행하지 않음
     * 다음 거래일에 다시 리밸런싱 조건을 확인
     */
    HALT_REBALANCING("리밸런싱 중단");
    
    private final String description;
    
    MissingPricePolicy(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 정책 설명을 포함한 상세 정보를 반환한다
     * 
     * @return 정책 상세 정보
     */
    public String getDetailedInfo() {
        switch (this) {
            case SKIP_STOCK:
                return "종목 제외: 가격이 없는 종목은 리밸런싱에서 제외하고 기존 보유량을 유지합니다. "
                     + "목표 비중은 유효한 종목들로 재분배됩니다.";
                     
            case USE_PREVIOUS:
                return "이전 가격 사용: 직전 유효한 거래일의 가격을 현재 가격으로 사용합니다. "
                     + "거래정지가 길어져도 계속 적용됩니다.";
                     
            case HALT_REBALANCING:
                return "리밸런싱 중단: 가격 누락 종목이 하나라도 있으면 해당 날짜의 리밸런싱을 수행하지 않습니다. "
                     + "다음 거래일에 다시 조건을 확인합니다.";
                     
            default:
                return description;
        }
    }
}