import { ApiClient } from '../../../shared/api/apiClient';
import type { Result, AppError } from '../../../shared/util/result';
import type { BuyOrderRequest, SellOrderRequest, OrderResponseData } from './types';

class StockOrderApiService extends ApiClient {
  /**
   * 매수 주문
   * @param stockCode 종목코드
   * @param stockName 종목명
   * @param orderData 주문 정보
   * @returns 주문 결과
   */
  async buyStock(stockCode: string, stockName: string, orderData: BuyOrderRequest): Promise<Result<OrderResponseData, AppError>> {
    const requestData = { ...orderData, stockCode, stockName };
    return await this.post<OrderResponseData>(`/api/stocks/buy`, requestData);
  }

  /**
   * 매도 주문
   * @param stockCode 종목코드
   * @param stockName 종목명
   * @param orderData 주문 정보
   * @returns 주문 결과
   */
  async sellStock(stockCode: string, stockName: string, orderData: SellOrderRequest): Promise<Result<OrderResponseData, AppError>> {
    const requestData = { ...orderData, stockCode, stockName };
    return await this.post<OrderResponseData>(`/api/stocks/sell`, requestData);
  }
}

export const stockOrderApi = new StockOrderApiService();
