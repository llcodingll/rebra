import type { UTCTimestamp } from 'lightweight-charts';

export interface StockDetailResponse {
  holdingQuantity: string;
  purchaseAmount: string;
  averagePrice: string;
  currentValue: string;
  profitLoss: string;
  profitLossRate: string;
}

// 새로운 보유종목 API 관련 타입들
export interface StockHoldingApiResponse {
  averagePurchasePrice: number;
  purchaseAmount: number;
  holdingQuantity: number;
  orderableQuantity: number;
  dncaTotAmt: number; // 예수금총금액 (구매가능금액)
}

// UI에서 사용할 계산된 보유종목 정보
export interface StockHoldingData {
  averagePurchasePrice: number;
  purchaseAmount: number;
  holdingQuantity: number;
  orderableQuantity: number;
  evaluationAmount: number;
  evaluationProfitLoss: number;
  returnRate: number;
  fee: number; // 매매수수료
  tax: number; // 증권거래세
  availableCash: number; // 구매가능금액 (dncaTotAmt)
}

// 실시간 체결가 데이터 타입
export interface OptimizedPriceData {
  mkscShrnIscd: string; // 유가증권 단축 종목코드
  stckCntgHour: string; // 주식 체결 시간
  stckPrpr: number; // 주식 현재가
  prdyVrssSign: string; // 전일 대비 부호
  prdyVrss: number; // 전일 대비
  prdyCtrt: number; // 전일 대비율
  wghnAvrgStckPrc: number; // 가중 평균 주식 가격
  stckOprc: number; // 주식 시가
  stckHgpr: number; // 주식 최고가
  stckLwpr: number; // 주식 최저가
  askp1: number; // 매도호가1
  bidp1: number; // 매수호가1
  cntgVol: number; // 체결 거래량
  acmlVol: number; // 누적 거래량
  acmlTrPbmn: number; // 누적 거래 대금
  selnCntgCsnu: number; // 매도 체결 건수
  shnuCntgCsnu: number; // 매수 체결 건수
  ntbyCntgCsnu: number; // 순매수 체결 건수
  cttr: number; // 체결강도
  selnCntgSmtn: number; // 총 매도 수량
  shnuCntgSmtn: number; // 총 매수 수량
  ccldDvsn: string; // 체결구분
  shnuRate: number; // 매수비율
  prdyVolVrssAcmlVolRate: number; // 전일 거래량 대비 등락율
  oprcHour: string; // 시가 시간
  oprcVrssPrprSign: string; // 시가대비구분
  oprcVrssPrpr: number; // 시가대비
  hgprHour: string; // 최고가 시간
  hgprVrssPrprSign: string; // 고가대비구분
  hgprVrssPrpr: number; // 고가대비
  lwprHour: string; // 최저가 시간
  lwprVrssPrprSign: string; // 저가대비구분
  lwprVrssPrpr: number; // 저가대비
  bsopDate: string; // 영업 일자
  newMkopClsCode: string; // 신 장운영 구분 코드
  trhtYn: string; // 거래정지 여부
  askpRsqn1: number; // 매도호가 잔량1
  bidpRsqn1: number; // 매수호가 잔량1
  totalAskpRsqn: number; // 총 매도호가 잔량
  totalBidpRsqn: number; // 총 매수호가 잔량
  volTnrt: number; // 거래량 회전율
  prdySmnsHourAcmlVol: number; // 전일 동시간 누적 거래량
  prdySmnsHourAcmlVolRate: number; // 전일 동시간 누적 거래량 비율
  hourClsCode: string; // 시간 구분 코드
  mrktTrtmClsCode: string; // 임의종료구분코드
  viStndPrc: number; // 정적VI발동기준가
}

// 실시간 호가 데이터 타입
export interface OptimizedOrderbookData {
  mkscShrnIscd: string; // 유가증권 단축 종목코드
  bsopHour: string; // 영업 시간
  hourClsCode: string; // 시간 구분 코드
  askp1: number; // 매도호가1
  askp2: number; // 매도호가2
  askp3: number; // 매도호가3
  askp4: number; // 매도호가4
  askp5: number; // 매도호가5
  askp6: number; // 매도호가6
  askp7: number; // 매도호가7
  askp8: number; // 매도호가8
  askp9: number; // 매도호가9
  askp10: number; // 매도호가10
  bidp1: number; // 매수호가1
  bidp2: number; // 매수호가2
  bidp3: number; // 매수호가3
  bidp4: number; // 매수호가4
  bidp5: number; // 매수호가5
  bidp6: number; // 매수호가6
  bidp7: number; // 매수호가7
  bidp8: number; // 매수호가8
  bidp9: number; // 매수호가9
  bidp10: number; // 매수호가10
  askpRsqn1: number; // 매도호가 잔량1
  askpRsqn2: number; // 매도호가 잔량2
  askpRsqn3: number; // 매도호가 잔량3
  askpRsqn4: number; // 매도호가 잔량4
  askpRsqn5: number; // 매도호가 잔량5
  askpRsqn6: number; // 매도호가 잔량6
  askpRsqn7: number; // 매도호가 잔량7
  askpRsqn8: number; // 매도호가 잔량8
  askpRsqn9: number; // 매도호가 잔량9
  askpRsqn10: number; // 매도호가 잔량10
  bidpRsqn1: number; // 매수호가 잔량1
  bidpRsqn2: number; // 매수호가 잔량2
  bidpRsqn3: number; // 매수호가 잔량3
  bidpRsqn4: number; // 매수호가 잔량4
  bidpRsqn5: number; // 매수호가 잔량5
  bidpRsqn6: number; // 매수호가 잔량6
  bidpRsqn7: number; // 매수호가 잔량7
  bidpRsqn8: number; // 매수호가 잔량8
  bidpRsqn9: number; // 매수호가 잔량9
  bidpRsqn10: number; // 매수호가 잔량10
  totalAskpRsqn: number; // 총 매도호가 잔량
  totalBidpRsqn: number; // 총 매수호가 잔량
  ovtmTotalAskpRsqn: number; // 시간외 총 매도호가 잔량
  ovtmTotalBidpRsqn: number; // 시간외 총 매수호가 잔량
  antcCnpr: number; // 예상 체결가
  antcCnqn: number; // 예상 체결량
  antcVol: number; // 예상 거래량
  antcCntgVrss: number; // 예상 체결 대비
  antcCntgVrssSign: string; // 예상 체결 대비 부호
  antcCntgPrdyCtrt: number; // 예상 체결 전일 대비율
  acmlVol: number; // 누적 거래량
  totalAskpRsqnIcdc: number; // 총 매도호가 잔량 증감
  totalBidpRsqnIcdc: number; // 총 매수호가 잔량 증감
  ovtmTotalAskpIcdc: number; // 시간외 총 매도호가 증감
  ovtmTotalBidpIcdc: number; // 시간외 총 매수호가 증감
  stckDealClsCode: string; // 주식 매매 구분 코드
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

// =============================================================================
// 일괄 구독 API 타입 정의 (백엔드 API와 연동)
// =============================================================================

/**
 * 일괄 구독 요청 데이터
 */
export interface BulkSubscriptionRequest {
  stocks: StockSubscription[];
}

/**
 * 개별 종목 구독 정보
 */
export interface StockSubscription {
  stockCode: string;
  dataTypes: string[];
}

/**
 * 일괄 구독 응답 데이터
 */
export interface BulkSubscriptionResponse {
  success: boolean;
  message?: string;
  results: SubscriptionResult[];
  summary: SubscriptionSummary;
  sessionId?: string;
  timestamp: number;
}

/**
 * 개별 구독 결과
 */
export interface SubscriptionResult {
  stockCode: string;
  dataType: string;
  success: boolean;
  error?: string;
  timestamp: number;
}

/**
 * 구독 요약 정보
 */
export interface SubscriptionSummary {
  totalRequested: number;
  totalSuccessful: number;
  totalFailed: number;
}

/**
 * 일괄 구독 해제 요청 데이터
 */
export interface BulkUnsubscriptionRequest {
  stocks: StockUnsubscription[];
}

/**
 * 개별 종목 구독 해제 정보
 */
export interface StockUnsubscription {
  stockCode: string;
  dataTypes: string[]; // ["all"]이면 모든 타입 해제
}

/**
 * WebSocket 메시지 타입
 */
export interface WebSocketMessage<T = any> {
  type: string;
  data: T;
  timestamp: number;
}

/**
 * WebSocket 응답 메시지 타입 상수
 */
export const WS_MESSAGE_TYPES = {
  // 기본 실시간 데이터
  PRICE_UPDATE: 'price-update',
  ORDERBOOK_UPDATE: 'orderbook-update',
  STOCK_INFO: 'stock-info',

  // 구독 관리
  SUBSCRIPTION_STARTED: 'subscription-started',
  SUBSCRIPTION_STOPPED: 'subscription-stopped',
  ERROR: 'error',

  // 일괄 구독 관련
  BULK_SUBSCRIPTION_RESPONSE: 'bulk-subscription-response',
  BULK_SUBSCRIPTION_PROGRESS: 'bulk-subscription-progress',
  BULK_UNSUBSCRIPTION_RESPONSE: 'bulk-unsubscription-response',
  BULK_UNSUBSCRIPTION_ERROR: 'bulk-unsubscription-error',
} as const;

/**
 * 지원되는 데이터 타입
 */
export const SUBSCRIPTION_DATA_TYPES = {
  PRICE: 'price',
  ORDERBOOK: 'orderbook',
} as const;

export type SubscriptionDataType = (typeof SUBSCRIPTION_DATA_TYPES)[keyof typeof SUBSCRIPTION_DATA_TYPES];
