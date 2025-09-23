package com.rebra.dto.response;

import com.rebra.component.kisApi.FluctuationRankingResult;
import com.rebra.enums.FluctuationRankingType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 등락률 순위 조회 응답 DTO
 */
@Getter
@AllArgsConstructor
public class FluctuationRankingResponse {

    private FluctuationRankingType rankingType;     // 등락률 순위 유형 (RISING, FALLING)
    private List<FluctuationRankingItem> rankings;

    /**
     * 등락률 순위 개별 항목
     */
    @Getter
    @AllArgsConstructor
    public static class FluctuationRankingItem {
        private Long rank;                      // 데이터 순위
        private String stockCode;               // 종목코드
        private String stockName;               // 종목명
        private Long currentPrice;              // 현재가
        private Double priceChangeRate;         // 등락률
        private Long volume;                    // 거래량
    }

    /**
     * FluctuationRankingResult를 급상승 FluctuationRankingResponse로 변환
     *
     * @param result KIS API 등락률순위 조회 결과
     * @return FluctuationRankingResponse (급상승)
     */
    public static FluctuationRankingResponse fromRising(FluctuationRankingResult result) {
        return convertFromResult(result, FluctuationRankingType.RISING);
    }

    /**
     * FluctuationRankingResult를 급하락 FluctuationRankingResponse로 변환
     *
     * @param result KIS API 등락률순위 조회 결과
     * @return FluctuationRankingResponse (급하락)
     */
    public static FluctuationRankingResponse fromFalling(FluctuationRankingResult result) {
        return convertFromResult(result, FluctuationRankingType.FALLING);
    }

    /**
     * FluctuationRankingResult를 FluctuationRankingResponse로 변환하는 공통 메서드
     */
    private static FluctuationRankingResponse convertFromResult(FluctuationRankingResult result, FluctuationRankingType rankingType) {
        if (result == null || result.getOutput() == null) {
            return new FluctuationRankingResponse(rankingType, List.of());
        }

        List<FluctuationRankingItem> rankings = Arrays.stream(result.getOutput())
                .limit(10)
                .map(output -> new FluctuationRankingItem(
                        parseLongSafely(output.getDataRank()),           // 순위 (Long)
                        output.getStckShrnIscd(),                        // 종목코드 (String)
                        output.getHtsKorIsnm(),                          // 종목명 (String)
                        parseLongSafely(output.getStckPrpr()),           // 현재가 (Long)
                        parseDoubleSafely(output.getPrdyCtrt()),         // 등락률 (Double)
                        parseLongSafely(output.getAcmlVol())             // 거래량 (Long)
                ))
                .collect(Collectors.toList());

        return new FluctuationRankingResponse(rankingType, rankings);
    }

    /**
     * String을 Long으로 안전하게 파싱
     */
    private static Long parseLongSafely(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0L : Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * String을 Double로 안전하게 파싱
     */
    private static Double parseDoubleSafely(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0.0 : Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}