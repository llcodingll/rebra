package com.rebra.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistDto {

    @NotBlank(message = "종목 코드는 필수입니다.")
    private String stockCode;

    private String stockName;
}
