import { ApiClient } from '../../../shared/api/apiClient';
import type { Result, AppError } from '../../../shared/util/result';
import type { StockSearchRequest, StockSearchResponse, SearchableStock, StockSearchTransformOptions } from './types';

class StockSearchApiService extends ApiClient {
  /**
   * 종목명으로 주식 검색
   * @param params 검색 요청 파라미터
   * @returns 검색 결과
   */
  async searchStocks(params: StockSearchRequest): Promise<Result<StockSearchResponse, AppError>> {
    console.log(`🔍 주식 검색 API 호출: /api/stocks/search?stockName=${params.stockName}`);

    const result = await this.get<StockSearchResponse>(
      `/api/stocks/search?stockName=${encodeURIComponent(params.stockName)}`
    );

    if (result.success) {
      console.log('✅ 주식 검색 API 응답:', result.data);
    } else {
      console.error('❌ 주식 검색 API 에러:', result.error);
    }

    return result;
  }
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
      code: item.ticker,
      name: item.name,
      isFavorite: favoriteStockCodes.includes(item.ticker),
    }))
    .slice(0, 10); // 최대 10개로 제한
};

export const stockSearchApi = new StockSearchApiService();