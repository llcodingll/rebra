import { ApiClient } from '../../../shared/api/apiClient';
import type { StockDetailResponse, DailyChartData, ChartDataRequest } from './types';
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

  /**
   * 일봉 차트 데이터 조회
   * @param params 종목코드, 시작날짜, 종료날짜
   * @returns 일봉 차트 데이터
   */
  async getDailyChartData(params: ChartDataRequest): Promise<Result<DailyChartData, AppError>> {
    console.log(`📊 일봉 차트 데이터 API 호출: /api/stocks/${params.stockCode}/chart/daily`);

    const result = await this.get<DailyChartData>(
      `/api/stocks/${params.stockCode}/chart/daily?startDate=${params.startDate}&endDate=${params.endDate}`
    );

    if (result.success) {
      console.log('✅ 일봉 차트 데이터 API 응답:', result.data);
    } else {
      console.error('❌ 일봉 차트 데이터 API 에러:', result.error);
    }

    return result;
  }
}

export const stockApi = new StockApiService();
