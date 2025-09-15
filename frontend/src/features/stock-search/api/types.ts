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

// STOMP 메시지로 받는 실시간 데이터 타입
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