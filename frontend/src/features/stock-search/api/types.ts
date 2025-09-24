/**
 * 주식 검색 API 관련 타입 정의
 */

// 검색 요청 타입
export interface StockSearchRequest {
  stockName: string;
}

// API 응답 타입
export interface StockSearchApiItem {
  stockCode: string;
  stockName: string;
}

// 검색 응답 타입 (배열 형태로 반환)
export type StockSearchResponse = StockSearchApiItem[];

// UI에서 사용할 간소화된 Stock 타입 (검색 테이블용)
export interface SearchableStock {
  code: string; // ticker를 code로 매핑
  name: string; // name 그대로 사용
  isFavorite?: boolean; // 클라이언트에서 관심종목 API로 별도 체크
}

// 새로운 거래량 랭킹 API 응답 타입
export interface VolumeRankingApiItem {
  rank: number;
  stockCode: string;
  stockName: string;
  currentPrice: number;
  priceChangeRate: number;
  volume: number;
}

export interface VolumeRankingApiResponse {
  rankings: VolumeRankingApiItem[];
}

// API 응답을 UI 타입으로 변환하는 헬퍼 함수용 타입
export interface StockSearchTransformOptions {
  favoriteStockCodes?: string[]; // 관심종목 코드 목록 (선택적)
}

// Holdings API 관련 타입들
export interface HoldingStock {
  stockCode: string;
  stockName: string;
  averagePurchasePrice: number;
  currentPrice: number;
  evaluationProfitLoss: number;
  returnRate: number;
  priceChange: number;
  changeRate: number;
  purchaseAmount: number;
  evaluationAmount: number;
  holdingQuantity: number;
  orderableQuantity: number;
}

export interface HoldingsRequest {
  accountId: number;
  page: number;
  size: number;
}

export interface HoldingsResponse {
  content: {
    holdings: HoldingStock[];
  };
}

// UI에서 사용할 보유 종목 타입 (수수료/세금 포함)
export interface DisplayHoldingStock extends HoldingStock {
  fee: number; // 매매수수료 (0.0145%)
  tax: number; // 증권거래세 (0.23%)
}
