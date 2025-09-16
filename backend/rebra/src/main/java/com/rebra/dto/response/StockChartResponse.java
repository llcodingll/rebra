package com.rebra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주식 차트 데이터 응답")
public class StockChartResponse {

    @Schema(description = "차트 기간 타입", example = "D")
    private String periodType;

    @Schema(description = "차트 기간 설명", example = "일봉")
    private String periodDescription;

    @Schema(description = "조회 시작일", example = "20240101")
    private String startDate;

    @Schema(description = "조회 종료일", example = "20241231")
    private String endDate;

    @Schema(description = "차트 데이터 목록")
    private List<ChartDataPoint> chartData;

    @Schema(description = "종목 요약 정보")
    private StockSummary summary;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "차트 데이터 포인트")
    public static class ChartDataPoint {

        @Schema(description = "거래일", example = "20240101")
        private String tradingDate;

        @Schema(description = "시가", example = "70000")
        private String openPrice;

        @Schema(description = "고가", example = "72000")
        private String highPrice;

        @Schema(description = "저가", example = "69000")
        private String lowPrice;

        @Schema(description = "종가", example = "71000")
        private String closePrice;

        @Schema(description = "거래량", example = "15000000")
        private String volume;

        @Schema(description = "거래대금", example = "1065000000000")
        private String tradingValue;

        @Schema(description = "전일대비", example = "1000")
        private String priceChange;

        @Schema(description = "전일대비 부호", example = "2")
        private String changeSign;

        @Schema(description = "등락률", example = "1.43")
        private String changeRate;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "종목 요약 정보")
    public static class StockSummary {

        @Schema(description = "현재가", example = "71000")
        private String currentPrice;

        @Schema(description = "전일대비", example = "1000")
        private String priceChange;

        @Schema(description = "전일대비율", example = "1.43")
        private String changeRate;

        @Schema(description = "전일대비 부호", example = "2")
        private String changeSign;

        @Schema(description = "거래량", example = "15000000")
        private String volume;

        @Schema(description = "시가총액", example = "425000000000000")
        private String marketCap;

        @Schema(description = "PER", example = "12.5")
        private String per;

        @Schema(description = "PBR", example = "0.8")
        private String pbr;
    }

    @Schema(description = "차트 기간 타입 열거형")
    public enum PeriodType {
        DAILY("D", "일봉"),
        WEEKLY("W", "주봉"),
        MONTHLY("M", "월봉"),
        YEARLY("Y", "년봉");

        private final String code;
        private final String description;

        PeriodType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static PeriodType fromCode(String code) {
            for (PeriodType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown period type code: " + code);
        }
    }
}