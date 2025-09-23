import { ApiClient } from '../../../shared/api/apiClient';
import type { Result, AppError } from '../../../shared/util/result';
import type { StockSearchRequest, StockSearchResponse, SearchableStock, StockSearchTransformOptions, StockRankingResponse, VolumeRankingStock, HoldingsRequest, HoldingsResponse, DisplayHoldingStock } from './types';

class StockSearchApiService extends ApiClient {
  /**
   * 종목명으로 주식 검색
   * @param params 검색 요청 파라미터
   * @returns 검색 결과
   */
  searchStocks = async (params: StockSearchRequest): Promise<Result<StockSearchResponse, AppError>> => {
    return await this.get<StockSearchResponse>(`/api/stocks/search?stockName=${encodeURIComponent(params.stockName)}`);
  };

  /**
   * 주식 랭킹 조회
   * @returns 주식 랭킹 결과
   */
  getStockRanking = async (): Promise<Result<StockRankingResponse, AppError>> => {
    return await this.get<StockRankingResponse>('/api/stocks/ranking');
  };

  /**
   * 보유 종목 조회
   * @param params 요청 파라미터
   * @returns 보유 종목 결과
   */
  getHoldings = async (params: HoldingsRequest): Promise<Result<HoldingsResponse, AppError>> => {
    return await this.get<HoldingsResponse>(`/api/stocks/holdings?accountId=${params.accountId}&page=${params.page}&size=${params.size}`);
  };
}

/**
 * API 응답을 UI에서 사용할 형태로 변환
 * @param apiData API 응답 데이터
 * @param options 변환 옵션 (관심종목 목록 등)
 * @returns UI용 검색 결과
 */
export const transformSearchResults = (
  apiData: StockSearchResponse,
  options?: StockSearchTransformOptions
): SearchableStock[] => {
  const favoriteStockCodes = options?.favoriteStockCodes || [];

  return apiData
    .map((item) => ({
      code: item.stockCode,
      name: item.stockName,
      isFavorite: favoriteStockCodes.includes(item.stockCode),
    }))
    .slice(0, 10); // 최대 10개로 제한
};

/**
 * 거래량 랭킹 API 응답을 UI에서 사용할 형태로 변환
 * @param apiData 거래량 랭킹 API 응답 데이터
 * @param favoriteStockCodes 관심종목 코드 목록 (선택적)
 * @returns UI용 거래량 랭킹 결과
 */
export const transformVolumeRankingResults = (
  apiData: StockRankingResponse,
  favoriteStockCodes?: string[]
): VolumeRankingStock[] => {
  const favorites = favoriteStockCodes || [];

  return apiData.map((item) => ({
    code: item.mksc_shrn_iscd,
    name: item.hts_kor_isnm,
    price: parseInt(item.stck_prpr, 10),
    change: parseFloat(item.prdy_ctrt),
    changePercent: item.prdy_vrss_sign === '2' || item.prdy_vrss_sign === '1', // 상승: 2, 보합: 3, 하락: 5
    volume: parseInt(item.acml_vol, 10).toLocaleString(),
    rank: parseInt(item.data_rank, 10),
    isFavorite: favorites.includes(item.mksc_shrn_iscd),
  }));
};

/**
 * KIS 수수료/세금 계산 함수
 * @param evaluationAmount 평가금액
 * @returns 수수료와 세금
 */
const calculateKISFeesAndTaxes = (evaluationAmount: number) => {
  // 매매수수료: 0.0145% (소숫점 이하 절사)
  const fee = Math.floor(evaluationAmount * 0.000145);

  // 증권거래세: 0.23% (소숫점 이하 절사)
  const tax = Math.floor(evaluationAmount * 0.0023);

  return { fee, tax };
};

/**
 * Holdings API 응답을 UI에서 사용할 형태로 변환
 * @param apiData Holdings API 응답 데이터
 * @returns UI용 보유 종목 결과
 */
export const transformHoldingsResults = (
  apiData: HoldingsResponse
): DisplayHoldingStock[] => {
  return apiData.content.holdings.map((holding) => {
    const { fee, tax } = calculateKISFeesAndTaxes(holding.evaluationAmount);

    return {
      ...holding,
      fee,
      tax,
    };
  });
};

export const stockSearchApi = new StockSearchApiService();
