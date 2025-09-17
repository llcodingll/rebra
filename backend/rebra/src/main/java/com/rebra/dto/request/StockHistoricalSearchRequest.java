package com.rebra.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "주식 과거 데이터 검색 요청")
public class StockHistoricalSearchRequest {

    @NotBlank(message = "종목명은 필수입니다")
    @Schema(description = "검색할 종목명", example = "삼성전자", required = true)
    private String stockName;

    @NotNull(message = "조회 날짜는 필수입니다")
    @Schema(description = "조회할 날짜", example = "2023-01-01", required = true)
    private LocalDate date;
}