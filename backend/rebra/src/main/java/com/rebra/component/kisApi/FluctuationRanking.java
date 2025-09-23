package com.rebra.component.kisApi;

import com.youhogeon.finance.kis_api.api.CommonRestApi;
import com.youhogeon.finance.kis_api.api.annotation.Header;
import com.youhogeon.finance.kis_api.api.annotation.Parameter;
import com.youhogeon.finance.kis_api.api.annotation.RestApi;
import com.youhogeon.finance.kis_api.api.annotation.auth.AccountRequired;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 등락률순위[FHPST01700000]
 */
@NoArgsConstructor
@Setter
@AccountRequired(location = AccountRequired.Location.PARAMETER)
@RestApi(method = RestApi.Method.GET, path = "/uapi/domestic-stock/v1/ranking/fluctuation")
public class FluctuationRanking extends CommonRestApi<FluctuationRankingResult> {

    /**
     * [실전투자]
     * FHPST01700000 : 등락률순위
     */
    @Header
    private String trId = "FHPST01700000";

    /**
     * 조건 시장 분류 코드
     *
     * J : KRX
     * W : ELW
     * Q : ETF
     */
    @Parameter
    private String fidCondMrktDivCode = "J";

    /**
     * 조건 화면 분류 코드
     *
     * 20170 : 등락률
     */
    @Parameter
    private String fidCondScrDivCode = "20170";

    /**
     * 입력 종목코드
     *
     * 0000 : 전체
     */
    @Parameter
    private String fidInputIscd = "0000";

    /**
     * 순위 정렬 구분 코드
     *
     * 0000 : 등락률순
     */
    @Parameter
    private String fidRankSortClsCode = "0";

    /**
     * 입력 수1
     *
     * 조회할 종목 수
     */
    @Parameter
    private String fidInputCnt_1 = "0";

    /**
     * 가격 구분 코드
     *
     * 0 : 전체
     */
    @Parameter
    private String fidPrcClsCode = "01";

    /**
     * 입력 가격1
     *
     * 하한가
     */
    @Parameter
    private String fidInputPrice_1 = "";

    /**
     * 입력 가격2
     *
     * 상한가
     */
    @Parameter
    private String fidInputPrice_2 = "";

    /**
     * 거래량 수
     *
     * 최소 거래량
     */
    @Parameter
    private String fidVolCnt = "";

    /**
     * 대상 구분 코드
     *
     * 9자리, 증거금30%40%50%60%100% 신용보증금30%40%50%60%
     */
    @Parameter
    private String fidTrgtClsCode = "0";

    /**
     * 대상 제외 구분 코드
     *
     * 10자리, 투자위험/경고/주의 관리종목 정리매매 불성실공시 우선주 거래정지 ETF ETN 신용주문불가 SPAC
     */
    @Parameter
    private String fidTrgtExlsClsCode = "0";

    /**
     * 분류 구분 코드
     *
     * 0 : 전체
     */
    @Parameter
    private String fidDivClsCode = "0";

    /**
     * 등락 비율1
     *
     * 하락률 하한
     */
    @Parameter
    private String fidRsflRate1 = "";

    /**
     * 등락 비율2
     *
     * 상승률 상한
     */
    @Parameter
    private String fidRsflRate2 = "";
}
