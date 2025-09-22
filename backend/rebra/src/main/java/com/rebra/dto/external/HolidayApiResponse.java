package com.rebra.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayApiResponse {

    @JsonProperty("response")
    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("items")
        @JsonDeserialize(using = ItemsDeserializer.class)
        private Items items;

        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        @JsonProperty("item")
        @JsonDeserialize(using = ItemDeserializer.class)
        private List<HolidayItem> item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HolidayItem {
        @JsonProperty("locdate")
        private String locdate; // 날짜 (YYYYMMDD)

        @JsonProperty("seq")
        private String seq; // 순번

        @JsonProperty("dateKind")
        private String dateKind; // 종류

        @JsonProperty("isHoliday")
        private String isHoliday; // 공공기관 휴일여부 (Y/N)

        @JsonProperty("dateName")
        private String dateName; // 명칭
    }
}