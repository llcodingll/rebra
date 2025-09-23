package com.rebra.dto.response;

import com.rebra.component.kisApi.VolumeRankResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 거래량 순위 조회 응답 DTO
 */
@Getter
@AllArgsConstructor
public class VolumeRankingResponse {

    private List<VolumeRankingItem> rankings;

    /**
     * 거래량 순위 개별 항목
     */
    @Getter
    @AllArgsConstructor
    public static class VolumeRankingItem {
        private Long rank;                      // 데이터 순위
        private String stockCode;               // 종목코드
        private String stockName;               // 종목명
        private Long currentPrice;              // 현재가
        private Double priceChangeRate;         // 등락률
        private Long volume;                    // 거래량
    }

    /**
     * VolumeRankResult를 VolumeRankingResponse로 변환
     *
     * @param result KIS API 거래량순위 조회 결과
     * @return VolumeRankingResponse
     */
    public static VolumeRankingResponse from(VolumeRankResult result) {
        if (result == null || result.getOutput() == null) {
            return new VolumeRankingResponse(List.of());
        }

        List<VolumeRankingItem> rankings = Arrays.stream(result.getOutput())
                .limit(10)
                .map(output -> new VolumeRankingItem(
                        parseLongSafely(output.getDataRank()),           // 순위 (Long)
                        output.getMkscShrnIscd(),                        // 종목코드 (String)
                        output.getHtsKorIsnm(),                          // 종목명 (String)
                        parseLongSafely(output.getStckPrpr()),           // 현재가 (Long)
                        parseDoubleSafely(output.getPrdyCtrt()),         // 등락률 (Double)
                        parseLongSafely(output.getAcmlVol())             // 거래량 (Long)
                ))
                .collect(Collectors.toList());

        return new VolumeRankingResponse(rankings);
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