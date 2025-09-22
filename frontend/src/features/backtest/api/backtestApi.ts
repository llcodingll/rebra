import { ApiClient } from '../../../shared/api/apiClient';
import type { Result, AppError } from '../../../shared/util/result';
import type {
  StockHistoricalDataResponse,
  BacktestListResponse,
  PageResponse,
  BacktestCreateRequest,
  BacktestStockRequest,
  BacktestResultResponse,
  BacktestValidationResponse,
} from './types';

class BacktestApiService extends ApiClient {
  /**
   * 백테스트용 과거 주식 데이터 조회
   */
  async getStockHistoricalData(
    stockName: string,
    date: string
  ): Promise<Result<StockHistoricalDataResponse[], AppError>> {
    return await this.get<StockHistoricalDataResponse[]>(
      `/api/v1/backtests/stocks/historical?stockName=${encodeURIComponent(stockName)}&date=${date}`
    );
  }

  /**
   * 백테스트 목록 조회
   */
  async getBacktestList(
    page: number = 0,
    size: number = 10
  ): Promise<Result<PageResponse<BacktestListResponse>, AppError>> {
    return await this.get<PageResponse<BacktestListResponse>>(
      `/api/v1/backtests?page=${page}&size=${size}`
    );
  }

  /**
   * 백테스트 생성
   */
  async createBacktest(request: BacktestCreateRequest): Promise<Result<number, AppError>> {
    return await this.post<number>('/api/v1/backtests', request, {
      timeout: 60000,
      headers: {}
    } as any);
  }

  /**
   * 백테스트 검증
   */
  async validateBacktest(request: BacktestCreateRequest): Promise<Result<BacktestValidationResponse, AppError>> {
    return await this.post<BacktestValidationResponse>('/api/v1/backtests/validate', request);
  }

  /**
   * 백테스트 결과 조회
   * @param backtestId 백테스트 ID
   * @returns 백테스트 결과
   */
  async getBacktestResult(backtestId: number): Promise<Result<BacktestResultResponse, AppError>> {
    console.log(`📊 백테스트 결과 조회 API 호출: /api/v1/backtests/${backtestId}`);

    const result = await this.get<BacktestResultResponse>(`/api/v1/backtests/${backtestId}`);

    if (result.success) {
      console.log('✅ 백테스트 결과 조회 API 응답:', result.data);
    } else {
      console.error('❌ 백테스트 결과 조회 API 에러:', result.error);
    }

    return result;
  }

  /**
   * 백테스트 삭제
   * @param backtestId 백테스트 ID
   * @returns 삭제 결과
   */
  async deleteBacktest(backtestId: number): Promise<Result<void, AppError>> {
    console.log(`🗑️ 백테스트 삭제 API 호출: /api/v1/backtests/${backtestId}`);

    const result = await this.delete<void>(`/api/v1/backtests/${backtestId}`);

    if (result.success) {
      console.log('✅ 백테스트 삭제 API 응답: 성공');
    } else {
      console.error('❌ 백테스트 삭제 API 에러:', result.error);
    }

    return result;
  }
}

export const backtestApi = new BacktestApiService();

// React Query용 함수 export
export const searchStocksForBacktest = (stockName: string, date: string) =>
  backtestApi.getStockHistoricalData(stockName, date);

export const getBacktestList = (page: number = 0, size: number = 10) =>
  backtestApi.getBacktestList(page, size);

export const createBacktest = (request: BacktestCreateRequest) =>
  backtestApi.createBacktest(request);

export const validateBacktest = (request: BacktestCreateRequest) =>
  backtestApi.validateBacktest(request);

export const getBacktestResult = (backtestId: number) =>
  backtestApi.getBacktestResult(backtestId);

export const deleteBacktest = (backtestId: number) =>
  backtestApi.deleteBacktest(backtestId);

// 타입 re-export
export type {
  BacktestListResponse,
  BacktestCreateRequest,
  BacktestStockRequest,
  BacktestResultResponse,
  BacktestValidationResponse,
  PageResponse,
} from './types';