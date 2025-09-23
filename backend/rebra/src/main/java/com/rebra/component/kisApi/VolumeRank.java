package com.rebra.component.kisApi;

import com.youhogeon.finance.kis_api.api.CommonRestApi;
import com.youhogeon.finance.kis_api.api.annotation.Header;
import com.youhogeon.finance.kis_api.api.annotation.Parameter;
import com.youhogeon.finance.kis_api.api.annotation.RestApi;
import com.youhogeon.finance.kis_api.api.annotation.auth.AccountRequired;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 거래량순위[FHPST01710000]
 */
@NoArgsConstructor
@Setter
@AccountRequired(location = AccountRequired.Location.PARAMETER)
@RestApi(method = RestApi.Method.GET, path = "/uapi/domestic-stock/v1/quotations/volume-rank")
public class VolumeRank extends CommonRestApi<VolumeRankResult> {
//    InquireAskingPriceExpCcnResult;
//            InquireAskingPriceExpCcnApi
    /**
     * [실전투자]
     * FHPST01710000 : 거래량순위
     */
    @Header
    private String trId = "FHPST01710000";

    /**
     * 조건 시장 분류 코드
     *
     * J : KRX
     * NX : NXT
     * UN : 통합
     * W : ELW
     */
    @Parameter
    private String fidCondMrktDivCode = "J";

    /**
     * 조건 화면 분류 코드
     *
     * 20171 : 고정값
     */
    @Parameter
    private String fidCondScrDivCode = "20171";

    /**
     * 입력 종목코드
     *
     * 0000 : 전체
     * 기타 : 업종코드
     */
    @Parameter
    private String fidInputIscd = "0000";

    /**
     * 분류 구분 코드
     *
     * 0 : 전체
     * 1 : 보통주
     * 2 : 우선주
     */
    @Parameter
    private String fidDivClsCode = "0";

    /**
     * 소속 구분 코드
     *
     * 0 : 평균거래량
     * 1 : 거래증가율
     * 2 : 평균거래회전율
     * 3 : 거래금액순
     * 4 : 평균거래금액회전율
     */
    @Parameter
    private String fidBlngClsCode = "0";

    /**
     * 대상 구분 코드
     *
     * 9자리, 증거금 30%40%50%60%100% 신용보증금 30%40%50%60%
     */
    @Parameter
    private String fidTrgtClsCode = "111111111";

    /**
     * 대상 제외 구분 코드
     *
     * 10자리, 투자위험/경고/주의 관리종목 정리매매 불성실공시 우선주 거래정지 ETF ETN 신용주문불가 SPAC
     */
    @Parameter
    private String fidTrgtExlsClsCode = "0000000000";

    /**
     * 입력 가격1
     *
     * 가격 ~, 전체 가격 대상시 공란
     */
    @Parameter
    private String fidInputPrice_1 = "";

    /**
     * 입력 가격2
     *
     * ~ 가격, 전체 가격 대상시 공란
     */
    @Parameter
    private String fidInputPrice_2 = "";

    /**
     * 거래량 수
     *
     * 거래량 ~, 전체 거래량 대상시 공란
     */
    @Parameter
    private String fidVolCnt = "";

    /**
     * 입력 날짜1
     *
     * 공란
     */
    @Parameter
    private String fidInputDate_1 = "";
}
