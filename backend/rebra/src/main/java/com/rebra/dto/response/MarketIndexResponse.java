package com.rebra.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MarketIndexResponse {
    
    private LocalDate dataDate; // 실제 데이터 날짜 (마지막 거래일)
    private LocalDate requestDate; // 요청 날짜
    private List<MarketIndexInfo> indices;

    @Getter
    @Builder
    public static class MarketIndexInfo {
        private String indexName; // 지수명
        private String indexCode; // 지수 코드
        private String currentPrice; // 현재 지수
        private String changeAmount; // 전일대비 변화량
        private String changeRate; // 등락률 (%)
        private String openPrice; // 시가
        private String highPrice; // 고가
        private String lowPrice; // 저가
        private String tradingVolume; // 거래량
        private String tradingValue; // 거래대금
        private String marketCap; // 상장시가총액
        private Integer listedStockCount; // 채용종목수
        private String yearHighPrice; // 연중 최고
        private String yearHighDate; // 연중 최고일
        private String yearLowPrice; // 연중 최저
        private String yearLowDate; // 연중 최저일
    }
}