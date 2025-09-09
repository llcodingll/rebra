export interface KisTokenRequest {
  grant_type: 'client_credentials';
  appkey: string;
  appsecret: string;
}

export interface KisTokenResponse {
  access_token: string;
  token_type: string;
  expires_in: number;
}

export interface KisWebSocketApprovalRequest {
  custtype: 'P';
  tr_type: '1';
  content: {
    tr_id: 'H0STCNT0';
    tr_key: string;
  };
}

export interface KisWebSocketApprovalResponse {
  header: {
    tr_id: string;
    tr_key: string;
    encrypt: string;
  };
  body: {
    rt_cd: string;
    msg_cd: string;
    msg1: string;
  };
}

export interface KisRealTimeData {
  header: {
    tr_id: string;
    tr_key: string;
  };
  body: {
    rt_cd: string;
    msg_cd: string;
    msg1: string;
    output?: {
      MKSC_SHRN_ISCD: string; // 종목코드
      STCK_CNTG_HOUR: string; // 체결시간
      STCK_PRPR: string; // 현재가
      PRDY_VRSS_SIGN: string; // 전일대비부호
      PRDY_VRSS: string; // 전일대비
      PRDY_CTRT: string; // 전일대비율
      WGHN_AVRG_STCK_PRC: string; // 가중평균가
      STCK_OPRC: string; // 시가
      STCK_HGPR: string; // 고가
      STCK_LWPR: string; // 저가
      ASKP1: string; // 매도호가1
      BIDP1: string; // 매수호가1
      CNTG_VOL: string; // 체결거래량
      ACML_VOL: string; // 누적거래량
      ACML_TR_PBMN: string; // 누적거래대금
      SELN_CNTG_CSNU: string; // 매도체결건수
      SHNU_CNTG_CSNU: string; // 매수체결건수
      NTBY_CNTG_CSNU: string; // 순매수체결건수
      CTTR: string; // 체결강도
      SELN_CNTG_SMTN: string; // 매도체결수량
      SHNU_CNTG_SMTN: string; // 매수체결수량
      CCLD_DVSN: string; // 체결구분
      SHNU_RATE: string; // 매수비율
      PRDY_VOL_VRSS_ACML_VOL_RATE: string; // 전일거래량대비등락율
    };
  };
}