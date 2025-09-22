package com.rebra.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FssStockBasicInfoResponse {

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
        private List<StockBasicItem> item;
    }

    @Getter
    @NoArgsConstructor
    public static class StockBasicItem {
        @JsonProperty("basDt")
        private String basDt; // 기준일자 (YYYYMMDD)

        @JsonProperty("crno")
        private String crno; // 법인등록번호

        @JsonProperty("isinCd")
        private String isinCd; // ISIN코드

        @JsonProperty("itmsShrtnCd")
        private String itmsShrtnCd; // 종목단축코드 (6자리)

        @JsonProperty("stckIssuCmpyNm")
        private String stckIssuCmpyNm; // 주식발행회사명

        @JsonProperty("isinCdNm")
        private String isinCdNm; // ISIN코드명

        @JsonProperty("scrsItmsKcd")
        private String scrsItmsKcd; // 유가증권종목종류코드

        @JsonProperty("scrsItmsKcdNm")
        private String scrsItmsKcdNm; // 유가증권종목종류코드명

        @JsonProperty("stckParPrc")
        private String stckParPrc; // 주식액면가

        @JsonProperty("issuStckCnt")
        private String issuStckCnt; // 발행주식수

        @JsonProperty("lstgDt")
        private String lstgDt; // 상장일자

        @JsonProperty("lstgAbolDt")
        private String lstgAbolDt; // 상장폐지일자

        @JsonProperty("dpsgRegDt")
        private String dpsgRegDt; // 예탁등록일자

        @JsonProperty("dpsgCanDt")
        private String dpsgCanDt; // 예탁취소일자

        @JsonProperty("issuFrmtClsfNm")
        private String issuFrmtClsfNm; // 발행형태구분명
    }
}