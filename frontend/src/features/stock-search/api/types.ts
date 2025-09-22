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

// 주식 랭킹 API 응답 아이템 타입
export interface StockRankingApiItem {
  hts_kor_isnm: string; // 종목명
  mksc_shrn_iscd: string; // 종목코드
  stck_prpr: string; // 현재가
  prdy_vrss: string; // 전일대비
  prdy_vrss_sign: string; // 전일대비부호
  prdy_ctrt: string; // 전일대비율
  acml_vol: string; // 누적거래량
  acml_tr_pbmn: string; // 누적거래대금
  ssts_yn: string; // 정지여부
  data_rank: string; // 데이터순위
}

// 주식 랭킹 API 응답 타입 (배열)
export type StockRankingResponse = StockRankingApiItem[];

// UI에서 사용할 거래량 랭킹 Stock 타입 (RankingTableWidget용)
export interface VolumeRankingStock {
  code: string; // 종목코드 (mksc_shrn_iscd)
  name: string; // 종목명 (hts_kor_isnm)
  price: number; // 현재가 (stck_prpr)
  change: number; // 전일대비율 (prdy_ctrt)
  changePercent: boolean; // 상승/하락 여부 (prdy_vrss_sign 기반)
  volume: string; // 거래량 포맷된 문자열 (acml_vol)
  rank: number; // 순위 (data_rank)
  isFavorite?: boolean; // 관심종목 여부 (UI용)
}

// API 응답을 UI 타입으로 변환하는 헬퍼 함수용 타입
export interface StockSearchTransformOptions {
  favoriteStockCodes?: string[]; // 관심종목 코드 목록 (선택적)
}
