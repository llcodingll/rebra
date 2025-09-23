package com.rebra.component.kisApi;

import com.youhogeon.finance.kis_api.api.CommonPageableRestResult;

import com.youhogeon.finance.kis_api.api.CommonRestResult;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class FluctuationRankingResult extends CommonRestResult {

    /** 성공 실패 여부 (0: 성공, 이외 실패) */
    private String rtCd;

    /** 응답코드 */
    private String msgCd;

    /** 응답메세지 */
    private String msg1;

    /** 응답상세 */
    private Output[] output;

    @Getter
    @ToString
    public static class Output {

        /** 주식 단축 종목코드 */
        private String stckShrnIscd;

        /** 데이터 순위 */
        private String dataRank;

        /** HTS 한글 종목명 */
        private String htsKorIsnm;

        /** 주식 현재가 */
        private String stckPrpr;

        /** 전일 대비 */
        private String prdyVrss;

        /** 전일 대비 부호 */
        private String prdyVrssSign;

        /** 전일 대비율 */
        private String prdyCtrt;

        /** 누적 거래량 */
        private String acmlVol;

        /** 주식 최고가 */
        private String stckHgpr;

        /** 최고가 시간 */
        private String hgprHour;

        /** 누적 최고가 일자 */
        private String acmlHgprDate;

        /** 주식 최저가 */
        private String stckLwpr;

        /** 최저가 시간 */
        private String lwprHour;

        /** 누적 최저가 일자 */
        private String acmlLwprDate;

        /** 저가 대비 현재가 비율 */
        private String lwprVrssPrprRate;

        /** 영업 일수 대비 현재가 비율 */
        private String dsgtDateClprVrssPrprRate;

        /** 연속 상승 일수 */
        private String cnntAscnDynu;

        /** 고가 대비 현재가 비율 */
        private String hgprVrssPrprRate;

        /** 연속 하락 일수 */
        private String cnntDownDynu;

        /** 시가 대비 부호 */
        private String oprcVrssPrprSign;

        /** 시가 대비 */
        private String oprcVrssPrpr;

        /** 시가 대비 현재가 비율 */
        private String oprcVrssPrprRate;

        /** 기간 등락 */
        private String prdRsfl;

        /** 기간 등락 비율 */
        private String prdRsflRate;
    }
}
