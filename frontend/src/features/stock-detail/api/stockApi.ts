import { ApiClient } from '../../../shared/api/apiClient';
import type { StockDetailResponse, StockChartData, ChartDataRequest } from './types';
import type { Result, AppError } from '../../../shared/util/result';

class StockApiService extends ApiClient {
  /**
   * 주식 보유 정보 조회
   * @param stockCode 주식 코드 (예: "005930")
   * @returns 보유 정보 (수량, 매입금액, 평균가, 현재가치, 손익 등)
   */
  getStockDetail = async (stockCode: string): Promise<Result<StockDetailResponse, AppError>> => {
    return this.get<StockDetailResponse>(`/api/stocks/${stockCode}`);
  }

  /**
   * 일봉 차트 데이터 조회
   * @param params 종목코드, 시작날짜, 종료날짜
   * @returns 일봉 차트 데이터
   */
  getDailyChartData = async (params: ChartDataRequest): Promise<Result<StockChartData, AppError>> => {
    return this.get<StockChartData>(
      `/api/stocks/${params.stockCode}/chart/daily?startDate=${params.startDate}&endDate=${params.endDate}`
    );
  }

  /**
   * 주봉 차트 데이터 조회
   * @param params 종목코드, 시작날짜, 종료날짜
   * @returns 주봉 차트 데이터
   */
  getWeeklyChartData = async (params: ChartDataRequest): Promise<Result<StockChartData, AppError>> => {
    return this.get<StockChartData>(
      `/api/stocks/${params.stockCode}/chart/weekly?startDate=${params.startDate}&endDate=${params.endDate}`
    );
  }

  /**
   * 월봉 차트 데이터 조회
   * @param params 종목코드, 시작날짜, 종료날짜
   * @returns 월봉 차트 데이터
   */
  getMonthlyChartData = async (params: ChartDataRequest): Promise<Result<StockChartData, AppError>> => {
    return this.get<StockChartData>(
      `/api/stocks/${params.stockCode}/chart/monthly?startDate=${params.startDate}&endDate=${params.endDate}`
    );
  }

  /**
   * 연봉 차트 데이터 조회
   * @param params 종목코드, 시작날짜, 종료날짜
   * @returns 연봉 차트 데이터
   */
  getYearlyChartData = async (params: ChartDataRequest): Promise<Result<StockChartData, AppError>> => {
    return this.get<StockChartData>(
      `/api/stocks/${params.stockCode}/chart/yearly?startDate=${params.startDate}&endDate=${params.endDate}`
    );
  }
}

export const stockApi = new StockApiService();
