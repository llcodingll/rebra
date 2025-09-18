import type { UTCTimestamp } from 'lightweight-charts';

export interface StockDetailResponse {
  success: boolean;
  status: number;
  data: {
    stock: StockInfo;
    realtime: RealtimeData;
    webSocketInfo: WebSocketInfo;
  };
  errorCode?: string;
  errorMessage?: string;
  errorData?: string;
  timestamp: string;
}

export interface StockInfo {
  id: number;
  stockCode: string;
  stockName: string;
  stockType: string;
  isActive: boolean;
}

export interface RealtimeData {
  currentPrice: PriceData;
  orderbook: OrderbookData;
}

export interface PriceData {
  stockCode: string;
  timestamp: string;
  priceData: string; // 실제 구조 확인 후 파싱 필요
}

export interface OrderbookData {
  stockCode: string;
  timestamp: string;
  orderbookData: string; // 실제 구조 확인 후 파싱 필요
}

export interface WebSocketInfo {
  priceChannel: string;
  orderbookChannel: string;
  endpoint: string;
}

// KRX 국내주식 실시간 체결가 데이터 타입
export interface KRXRealtimePriceMessage {
  MKSC_SHRN_ISCD: string;     // 유가증권 단축 종목코드
  STCK_CNTG_HOUR: string;     // 주식 체결 시간
  STCK_PRPR: number;          // 주식 현재가
  PRDY_VRSS_SIGN: string;     // 전일 대비 부호
  PRDY_VRSS: number;          // 전일 대비
  PRDY_CTRT: number;          // 전일 대비율
  WGHN_AVRG_STCK_PRC: number; // 가중 평균 주식 가격
  STCK_OPRC: number;          // 주식 시가
  STCK_HGPR: number;          // 주식 최고가
  STCK_LWPR: number;          // 주식 최저가
  ASKP1: number;              // 매도호가1
  BIDP1: number;              // 매수호가1
  CNTG_VOL: number;           // 체결 거래량
  ACML_VOL: number;           // 누적 거래량
  ACML_TR_PBMN: number;       // 누적 거래 대금
  SELN_CNTG_CSNU: number;     // 매도 체결 건수
  SHNU_CNTG_CSNU: number;     // 매수 체결 건수
  NTBY_CNTG_CSNU: number;     // 순매수 체결 건수
  CTTR: number;               // 체결강도
  SELN_CNTG_SMTN: number;     // 총 매도 수량
  SHNU_CNTG_SMTN: number;     // 총 매수 수량
  CCLD_DVSN: string;          // 체결구분
  SHNU_RATE: number;          // 매수비율
  PRDY_VOL_VRSS_ACML_VOL_RATE: number; // 전일 거래량 대비 등락율
  OPRC_HOUR: string;          // 시가 시간
  OPRC_VRSS_PRPR_SIGN: string; // 시가대비구분
  OPRC_VRSS_PRPR: number;     // 시가대비
  HGPR_HOUR: string;          // 최고가 시간
  HGPR_VRSS_PRPR_SIGN: string; // 고가대비구분
  HGPR_VRSS_PRPR: number;     // 고가대비
  LWPR_HOUR: string;          // 최저가 시간
  LWPR_VRSS_PRPR_SIGN: string; // 저가대비구분
  LWPR_VRSS_PRPR: number;     // 저가대비
  BSOP_DATE: string;          // 영업 일자
  NEW_MKOP_CLS_CODE: string;  // 신 장운영 구분 코드
  TRHT_YN: string;            // 거래정지 여부
  ASKP_RSQN1: number;         // 매도호가 잔량1
  BIDP_RSQN1: number;         // 매수호가 잔량1
  TOTAL_ASKP_RSQN: number;    // 총 매도호가 잔량
  TOTAL_BIDP_RSQN: number;    // 총 매수호가 잔량
  VOL_TNRT: number;           // 거래량 회전율
  PRDY_SMNS_HOUR_ACML_VOL: number; // 전일 동시간 누적 거래량
  PRDY_SMNS_HOUR_ACML_VOL_RATE: number; // 전일 동시간 누적 거래량 비율
  HOUR_CLS_CODE: string;      // 시간 구분 코드
  MRKT_TRTM_CLS_CODE: string; // 임의종료구분코드
  VI_STND_PRC: number;        // 정적VI발동기준가
}

// KRX 국내주식 실시간 호가 데이터 타입
export interface KRXRealtimeOrderbookMessage {
  MKSC_SHRN_ISCD: string;     // 유가증권 단축 종목코드
  BSOP_HOUR: string;          // 영업 시간
  HOUR_CLS_CODE: string;      // 시간 구분 코드
  ASKP1: number; ASKP2: number; ASKP3: number; ASKP4: number; ASKP5: number;
  ASKP6: number; ASKP7: number; ASKP8: number; ASKP9: number; ASKP10: number; // 매도호가1-10
  BIDP1: number; BIDP2: number; BIDP3: number; BIDP4: number; BIDP5: number;
  BIDP6: number; BIDP7: number; BIDP8: number; BIDP9: number; BIDP10: number; // 매수호가1-10
  ASKP_RSQN1: number; ASKP_RSQN2: number; ASKP_RSQN3: number; ASKP_RSQN4: number; ASKP_RSQN5: number;
  ASKP_RSQN6: number; ASKP_RSQN7: number; ASKP_RSQN8: number; ASKP_RSQN9: number; ASKP_RSQN10: number; // 매도호가 잔량1-10
  BIDP_RSQN1: number; BIDP_RSQN2: number; BIDP_RSQN3: number; BIDP_RSQN4: number; BIDP_RSQN5: number;
  BIDP_RSQN6: number; BIDP_RSQN7: number; BIDP_RSQN8: number; BIDP_RSQN9: number; BIDP_RSQN10: number; // 매수호가 잔량1-10
  TOTAL_ASKP_RSQN: number;    // 총 매도호가 잔량
  TOTAL_BIDP_RSQN: number;    // 총 매수호가 잔량
  OVTM_TOTAL_ASKP_RSQN: number; // 시간외 총 매도호가 잔량
  OVTM_TOTAL_BIDP_RSQN: number; // 시간외 총 매수호가 잔량
  ANTC_CNPR: number;          // 예상 체결가
  ANTC_CNQN: number;          // 예상 체결량
  ANTC_VOL: number;           // 예상 거래량
  ANTC_CNTG_VRSS: number;     // 예상 체결 대비
  ANTC_CNTG_VRSS_SIGN: string; // 예상 체결 대비 부호
  ANTC_CNTG_PRDY_CTRT: number; // 예상 체결 전일 대비율
  ACML_VOL: number;           // 누적 거래량
  TOTAL_ASKP_RSQN_ICDC: number; // 총 매도호가 잔량 증감
  TOTAL_BIDP_RSQN_ICDC: number; // 총 매수호가 잔량 증감
  OVTM_TOTAL_ASKP_ICDC: number; // 시간외 총 매도호가 증감
  OVTM_TOTAL_BIDP_ICDC: number; // 시간외 총 매수호가 증감
  STCK_DEAL_CLS_CODE: string; // 주식 매매 구분 코드
}

// 기존 호환용 타입 (기존 컴포넌트에서 사용)
export interface RealtimePriceMessage {
  stockCode: string;
  currentPrice: number;
  change: number;
  changePercent: number;
  volume: number;
  timestamp: string;
}

export interface RealtimeOrderbookMessage {
  stockCode: string;
  asks: OrderBookItem[];
  bids: OrderBookItem[];
  timestamp: string;
}

export interface OrderBookItem {
  price: number;
  quantity: number;
  size: number;
}

// Chart 데이터 타입 (lightweight-charts 호환)
export interface ChartPriceData {
  time: number; // UTCTimestamp
  value: number;
}

export interface ChartVolumeData {
  time: number; // UTCTimestamp
  value: number;
  color?: string;
}

// 차트 데이터 (일봉/주봉/월봉/연봉 공통)
export interface StockChartData {
  stockCode: string;
  stockName: string;
  periodType: string;
  periodDescription: string;
  startDate: string;
  endDate: string;
  chartData: StockChartItem[];
  summary: {
    currentPrice: string;
    priceChange: string;
    changeRate: string;
    changeSign: string;
    volume: string;
    marketCap: string;
    per: string;
    pbr: string;
  };
}

export interface StockChartItem {
  tradingDate: string; // YYYYMMDD 형식
  openPrice: string;
  highPrice: string;
  lowPrice: string;
  closePrice: string;
  volume: string;
  tradingValue: string;
  priceChange: string;
  changeSign: string;
  changeRate: string;
}

// TradingView 차트용 변환된 데이터 타입
export interface CandleData {
  time: UTCTimestamp;
  open: number;
  high: number;
  low: number;
  close: number;
}

export interface VolumeData {
  time: UTCTimestamp;
  value: number;
  color?: string;
}

// 차트 데이터 조회 요청 파라미터
export interface ChartDataRequest {
  stockCode: string;
  startDate: string; // YYYYMMDD
  endDate: string; // YYYYMMDD
}