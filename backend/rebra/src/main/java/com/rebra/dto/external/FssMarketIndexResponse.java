package com.rebra.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FssMarketIndexResponse {

    @JsonProperty("response")
    private ResponseWrapper response;

    @Getter
    @Setter
    public static class ResponseWrapper {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;
    }

    @Getter
    @Setter
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Getter
    @Setter
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
    @Setter
    public static class Items {
        @JsonProperty("item")
        private List<MarketIndexItem> item;
    }

    @Getter
    @Setter
    public static class MarketIndexItem {
        @JsonProperty("basDt")
        private String basDt; // 기준일자

        @JsonProperty("idxNm")
        private String idxNm; // 지수명

        @JsonProperty("idxCsf")
        private String idxCsf; // 지수분류

        @JsonProperty("epyItmsCnt")
        private Integer epyItmsCnt; // 채용종목수

        @JsonProperty("clpr")
        private String clpr; // 종가

        @JsonProperty("vs")
        private String vs; // 전일대비

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

        @JsonProperty("lstgMrktTotAmt")
        private String lstgMrktTotAmt; // 상장시가총액

        @JsonProperty("lsYrEdVsFltRg")
        private String lsYrEdVsFltRg; // 전년말대비 등락폭

        @JsonProperty("lsYrEdVsFltRt")
        private String lsYrEdVsFltRt; // 전년말대비 등락률

        @JsonProperty("yrWRcrdHgst")
        private String yrWRcrdHgst; // 연중기록최고

        @JsonProperty("yrWRcrdHgstDt")
        private String yrWRcrdHgstDt; // 연중기록최고일자

        @JsonProperty("yrWRcrdLwst")
        private String yrWRcrdLwst; // 연중기록최저

        @JsonProperty("yrWRcrdLwstDt")
        private String yrWRcrdLwstDt; // 연중기록최저일자

        @JsonProperty("basPntm")
        private String basPntm; // 기준시점

        @JsonProperty("basIdx")
        private String basIdx; // 기준지수
    }
}