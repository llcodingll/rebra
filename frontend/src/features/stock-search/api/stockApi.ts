import { ApiClient } from '../../../shared/api/apiClient';
import type { StockDetailResponse } from './types';
import type { Result, AppError } from '../../../shared/util/result';

class StockApiService extends ApiClient {
  /**
   * 주식 상세 정보 및 실시간 WebSocket 연결 정보 조회
   * @param stockCode 주식 코드 (예: "005930")
   * @returns 주식 정보, 실시간 데이터, WebSocket 연결 정보
   */
  async getStockDetail(stockCode: string): Promise<Result<StockDetailResponse, AppError>> {
    console.log(`🔍 주식 상세 정보 API 호출: /api/stocks/${stockCode}`);

    const result = await this.get<StockDetailResponse>(`/api/stocks/${stockCode}`);

    if (result.success) {
      console.log('✅ 주식 상세 정보 API 응답:', result.data);
    } else {
      console.error('❌ 주식 상세 정보 API 에러:', result.error);
    }

    return result;
  }
}

export const stockApi = new StockApiService();
