package com.rebra.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FssStockPriceResponse {

    @JsonProperty("response")
    private Response response;

    @Getter
    @NoArgsConstructor
    public static class Response {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    public static class Body {
        @JsonProperty("numOfRows")
        private int numOfRows;

        @JsonProperty("pageNo")
        private int pageNo;

        @JsonProperty("totalCount")
        private int totalCount;

        @JsonProperty("items")
        private Items items;
    }

    @Getter
    @NoArgsConstructor
    public static class Items {
        @JsonProperty("item")
        private List<StockItem> item;
    }

    @Getter
    @NoArgsConstructor
    public static class StockItem {
        @JsonProperty("basDt")
        private String basDt; // 기준일자 (YYYYMMDD)

        @JsonProperty("srtnCd")
        private String srtnCd; // 단축코드 (6자리)

        @JsonProperty("isinCd")
        private String isinCd; // ISIN코드

        @JsonProperty("itmsNm")
        private String itmsNm; // 종목명

        @JsonProperty("mrktCtg")
        private String mrktCtg; // 시장구분 (KOSPI/KOSDAQ/KONEX)

        @JsonProperty("clpr")
        private String clpr; // 종가

        @JsonProperty("vs")
        private String vs; // 대비

        @JsonProperty("fltRt")
        private String fltRt; // 등락률

        @JsonProperty("mkp")
        private String mkp; // 시가

        @JsonProperty("hipr")
        private String hipr; // 고가

        @JsonProperty("lopr")
        private String lopr; // 저가

        @JsonProperty("trqu")
        private String trqu; // 거래량

        @JsonProperty("trPrc")
        private String trPrc; // 거래대금

        @JsonProperty("lstgStCnt")
        private String lstgStCnt; // 상장주식수

        @JsonProperty("mrktTotAmt")
        private String mrktTotAmt; // 시가총액
    }
}