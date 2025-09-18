/**
 * 주식 검색 API 관련 타입 정의
 */

// 검색 요청 타입
export interface StockSearchRequest {
  stockName: string;
}

// API 응답의 실제 주식 아이템 타입
export interface StockSearchApiItem {
  ticker: string;
  name: string;
  date: string;
  closePrice: number;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  volume: number;
  changeRate: number;
}

// 검색 응답 타입 (배열 형태로 반환)
export type StockSearchResponse = StockSearchApiItem[];

// UI에서 사용할 간소화된 Stock 타입 (검색 테이블용)
export interface SearchableStock {
  code: string;    // ticker를 code로 매핑
  name: string;    // name 그대로 사용
  isFavorite?: boolean; // 클라이언트에서 관심종목 API로 별도 체크
}

// API 응답을 UI 타입으로 변환하는 헬퍼 함수용 타입
export interface StockSearchTransformOptions {
  favoriteStockCodes?: string[]; // 관심종목 코드 목록 (선택적)
}