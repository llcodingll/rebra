import { ApiClient } from '../../../shared/api/apiClient';
import type { Result, AppError } from '../../../shared/util/result';
import { calculateKISFeesAndTaxes, floorPrice } from '../../../shared/util/calculation';
import type { StockSearchRequest, StockSearchResponse, SearchableStock, StockSearchTransformOptions, VolumeRankingApiResponse, HoldingsRequest, HoldingsResponse, DisplayHoldingStock, WatchlistResponse, WatchlistToggleRequest, WatchlistToggleResponse } from './types';

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
   * 거래량 랭킹 조회
   * @param accountId 계정 ID
   * @returns 거래량 랭킹 결과
   */
  getStockRanking = async (accountId: number): Promise<Result<VolumeRankingApiResponse, AppError>> => {
    return await this.get<VolumeRankingApiResponse>(`/api/v1/rankings/volume?accountId=${accountId}`);
  };

  /**
   * 급상승 랭킹 조회
   * @param accountId 계정 ID
   * @returns 급상승 랭킹 결과
   */
  getRisingStockRanking = async (accountId: number): Promise<Result<VolumeRankingApiResponse, AppError>> => {
    return await this.get<VolumeRankingApiResponse>(`/api/v1/rankings/fluctuation/rising?accountId=${accountId}`);
  };

  /**
   * 급하락 랭킹 조회
   * @param accountId 계정 ID
   * @returns 급하락 랭킹 결과
   */
  getFallingStockRanking = async (accountId: number): Promise<Result<VolumeRankingApiResponse, AppError>> => {
    return await this.get<VolumeRankingApiResponse>(`/api/v1/rankings/fluctuation/falling?accountId=${accountId}`);
  };

  /**
   * 보유 종목 조회
   * @param params 요청 파라미터
   * @returns 보유 종목 결과
   */
  getHoldings = async (params: HoldingsRequest): Promise<Result<HoldingsResponse, AppError>> => {
    return await this.get<HoldingsResponse>(`/api/stocks/holdings?accountId=${params.accountId}&page=${params.page}&size=${params.size}`);
  };

  /**
   * 관심종목 조회
   * @param accountId 계정 ID
   * @returns 관심종목 목록
   */
  getWatchlist = async (accountId: number): Promise<Result<WatchlistResponse, AppError>> => {
    return await this.get<WatchlistResponse>(`/api/stocks/watchlist?accountId=${accountId}`);
  };

  /**
   * 관심종목 토글 (추가/제거)
   * @param params 토글 요청 파라미터
   * @returns 토글 결과
   */
  toggleWatchlist = async (params: WatchlistToggleRequest): Promise<Result<WatchlistToggleResponse, AppError>> => {
    // Query Parameter 방식으로 변경 (백엔드가 파라미터를 인식하지 못하는 문제 해결)
    return await this.post<WatchlistToggleResponse>(
      `/api/stocks/watchlist/toggle?stockCode=${encodeURIComponent(params.stockCode)}&accountId=${params.accountId}`
    );
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
      averagePurchasePrice: floorPrice(holding.averagePurchasePrice), // 매입가 소수점 버림
      fee,
      tax,
    };
  });
};

export const stockSearchApi = new StockSearchApiService();
