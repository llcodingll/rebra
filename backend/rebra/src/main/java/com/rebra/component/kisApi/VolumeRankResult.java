package com.rebra.component.kisApi;

import com.youhogeon.finance.kis_api.api.CommonPageableRestResult;

import com.youhogeon.finance.kis_api.api.CommonRestResult;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class VolumeRankResult extends CommonRestResult {

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

        /** HTS 한글 종목명 */
        private String htsKorIsnm;

        /** 가중권 단축 종목코드 */
        private String mkscShrnIscd;

        /** 데이터 순위 */
        private String dataRank;

        /** 주식 현재가 */
        private String stckPrpr;

        /** 전일 대비 부호 */
        private String prdyVrssSign;

        /** 전일 대비 */
        private String prdyVrss;

        /** 전일 대비율 */
        private String prdyCtrt;

        /** 누적 거래량 */
        private String acmlVol;

        /** 전일 거래량 */
        private String prdyVol;

        /** 상장 주식수 */
        private String lstnStcn;

        /** 평균 거래량 */
        private String avrgVol;

        /** 전일종가대비현재가(%) */
        private String nBefrClprVrssPrprRate;

        /** 거래량증가율 */
        private String volInrt;

        /** 거래량회전율 */
        private String volTnrt;

        /** N일 거래량회전율 */
        private String ndayVolTnrt;

        /** 평균 거래 대금 */
        private String avrgTrPbmn;

        /** 거래대금회전율 */
        private String trPbmnTnrt;

        /** N일 거래대금회전율 */
        private String ndayTrPbmnTnrt;

        /** 누적 거래 대금 */
        private String acmlTrPbmn;
    }
}
