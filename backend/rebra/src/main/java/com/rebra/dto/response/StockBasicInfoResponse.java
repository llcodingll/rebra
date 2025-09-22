package com.rebra.dto.response;

import com.rebra.dto.external.FssStockBasicInfoResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "주식 기본 정보 응답")
public class StockBasicInfoResponse {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Schema(description = "종목 코드 (6자리)", example = "005930")
    private String stockCode;

    @Schema(description = "종목명", example = "삼성전자")
    private String stockName;

    @Schema(description = "ISIN 코드", example = "KR7005930003")
    private String isinCode;

    @Schema(description = "ISIN 코드명", example = "삼성전자")
    private String isinCodeName;

    @Schema(description = "법인등록번호", example = "1301110006246")
    private String corporateRegistrationNumber;

    @Schema(description = "유가증권종목종류코드", example = "0101")
    private String securitiesTypeCode;

    @Schema(description = "유가증권종목종류코드명", example = "보통주")
    private String securitiesTypeName;

    @Schema(description = "주식액면가", example = "5000.00")
    private BigDecimal parValue;

    @Schema(description = "발행주식수", example = "5969782550")
    private Long issuedShares;

    @Schema(description = "상장일자", example = "1975-06-11")
    private LocalDate listingDate;

    @Schema(description = "상장폐지일자", example = "null")
    private LocalDate delistingDate;

    @Schema(description = "예탁등록일자", example = "1999-07-01")
    private LocalDate depositRegistrationDate;

    @Schema(description = "예탁취소일자", example = "null")
    private LocalDate depositCancellationDate;

    @Schema(description = "발행형태구분명", example = "예탁증권")
    private String issueTypeName;

    @Schema(description = "기준일자", example = "2024-01-01")
    private LocalDate baseDate;

    public static StockBasicInfoResponse from(FssStockBasicInfoResponse.StockBasicItem item) {
        return StockBasicInfoResponse.builder()
                .stockCode(item.getItmsShrtnCd())
                .stockName(item.getStckIssuCmpyNm())
                .isinCode(item.getIsinCd())
                .isinCodeName(item.getIsinCdNm())
                .corporateRegistrationNumber(item.getCrno())
                .securitiesTypeCode(item.getScrsItmsKcd())
                .securitiesTypeName(item.getScrsItmsKcdNm())
                .parValue(parsePrice(item.getStckParPrc()))
                .issuedShares(parseLong(item.getIssuStckCnt()))
                .listingDate(parseDate(item.getLstgDt()))
                .delistingDate(parseDate(item.getLstgAbolDt()))
                .depositRegistrationDate(parseDate(item.getDpsgRegDt()))
                .depositCancellationDate(parseDate(item.getDpsgCanDt()))
                .issueTypeName(item.getIssuFrmtClsfNm())
                .baseDate(parseDate(item.getBasDt()))
                .build();
    }

    private static BigDecimal parsePrice(String priceStr) {
        if (!StringUtils.hasText(priceStr)) {
            return BigDecimal.ZERO;
        }
        try {
            String cleanPrice = priceStr.replaceAll(",", "");
            return new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static Long parseLong(String longStr) {
        if (!StringUtils.hasText(longStr)) {
            return 0L;
        }
        try {
            String cleanLong = longStr.replaceAll(",", "");
            return Long.parseLong(cleanLong);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr) || "NULL".equalsIgnoreCase(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}