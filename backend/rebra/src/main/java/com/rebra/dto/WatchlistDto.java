package com.rebra.dto;

import jakarta.validation.constraints.NotBlank;

public class WatchlistDto {

    @NotBlank(message = "종목 id는 필수입니다.")
    private String stockId;

    private String stockName;
}
