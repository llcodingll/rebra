package com.rebra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 거래 히스토리 응답 DTO
 * 특정 일자의 거래 기록 목록을 반환
 */
@Getter
@AllArgsConstructor
public class TradeHistoryResponse {

    /**
     * 거래 일자
     */
    private LocalDate date;

    /**
     * 해당 일자의 거래 목록
     */
    private List<TradeDetailResponse> trades;

    /**
     * 정적 팩토리 메서드
     *
     * @param date 거래 일자
     * @param trades 거래 목록
     * @return TradeHistoryResponse
     */
    public static TradeHistoryResponse of(LocalDate date, List<TradeDetailResponse> trades) {
        return new TradeHistoryResponse(date, trades);
    }
}