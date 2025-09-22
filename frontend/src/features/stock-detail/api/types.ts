import type { UTCTimestamp } from 'lightweight-charts';

export interface StockDetailResponse {
  holdingQuantity: string;
  purchaseAmount: string;
  averagePrice: string;
  currentValue: string;
  profitLoss: string;
  profitLossRate: string;
}

// 최적화된 실시간 체결가 데이터 타입
export interface OptimizedPriceData {
  // 종목 정보
  stockCode: string;              // 종목 코드

  // 현재가 정보
  stckPrpr: string;              // 주식 현재가
  prdyVrssSign: string;          // 전일 대비 부호 (1: 상한, 2: 상승, 3: 보합, 4: 하한, 5: 하락)
  prdyVrss: string;              // 전일 대비
  prdyCtrt: string;              // 전일 대비율

  // 시고저가 정보
  stckOprc: string;              // 주식 시가
  stckHgpr: string;              // 주식 고가
  stckLwpr: string;              // 주식 저가

  // 거래량 정보
  cntgVol: string;               // 체결 거래량
  acmlVol: string;               // 누적 거래량
  acmlTrPbmn: string;            // 누적 거래 대금

  // 호가 정보 (1차)
  askp1: string;                 // 매도호가 1
  bidp1: string;                 // 매수호가 1
  askpRsqn1: string;             // 매도호가 잔량 1
  bidpRsqn1: string;             // 매수호가 잔량 1

  // 시간 정보
  timestamp: number;             // 데이터 수신 시간 (milliseconds)
}

// 최적화된 실시간 호가 데이터 타입
export interface OptimizedOrderbookData {
  // 종목 정보
  stockCode: string;              // 종목 코드

  // 매도 호가 (1~10차)
  askp1: string;                 // 매도호가 1차
  askp2: string;                 // 매도호가 2차
  askp3: string;                 // 매도호가 3차
  askp4: string;                 // 매도호가 4차
  askp5: string;                 // 매도호가 5차
  askp6: string;                 // 매도호가 6차
  askp7: string;                 // 매도호가 7차
  askp8: string;                 // 매도호가 8차
  askp9: string;                 // 매도호가 9차
  askp10: string;                // 매도호가 10차

  // 매수 호가 (1~10차)
  bidp1: string;                 // 매수호가 1차
  bidp2: string;                 // 매수호가 2차
  bidp3: string;                 // 매수호가 3차
  bidp4: string;                 // 매수호가 4차
  bidp5: string;                 // 매수호가 5차
  bidp6: string;                 // 매수호가 6차
  bidp7: string;                 // 매수호가 7차
  bidp8: string;                 // 매수호가 8차
  bidp9: string;                 // 매수호가 9차
  bidp10: string;                // 매수호가 10차

  // 매도 호가 잔량 (1~10차)
  askpRsqn1: string;             // 매도호가 잔량 1차
  askpRsqn2: string;             // 매도호가 잔량 2차
  askpRsqn3: string;             // 매도호가 잔량 3차
  askpRsqn4: string;             // 매도호가 잔량 4차
  askpRsqn5: string;             // 매도호가 잔량 5차
  askpRsqn6: string;             // 매도호가 잔량 6차
  askpRsqn7: string;             // 매도호가 잔량 7차
  askpRsqn8: string;             // 매도호가 잔량 8차
  askpRsqn9: string;             // 매도호가 잔량 9차
  askpRsqn10: string;            // 매도호가 잔량 10차

  // 매수 호가 잔량 (1~10차)
  bidpRsqn1: string;             // 매수호가 잔량 1차
  bidpRsqn2: string;             // 매수호가 잔량 2차
  bidpRsqn3: string;             // 매수호가 잔량 3차
  bidpRsqn4: string;             // 매수호가 잔량 4차
  bidpRsqn5: string;             // 매수호가 잔량 5차
  bidpRsqn6: string;             // 매수호가 잔량 6차
  bidpRsqn7: string;             // 매수호가 잔량 7차
  bidpRsqn8: string;             // 매수호가 잔량 8차
  bidpRsqn9: string;             // 매수호가 잔량 9차
  bidpRsqn10: string;            // 매수호가 잔량 10차

  // 총 잔량
  totalAskpRsqn: string;         // 총 매도호가 잔량
  totalBidpRsqn: string;         // 총 매수호가 잔량

  // 시간 정보
  timestamp: number;             // 데이터 수신 시간 (milliseconds)
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