import { ApiClient } from '../../../shared/api/apiClient';
import type { Result, AppError } from '../../../shared/util/result';
import type { BuyOrderRequest, SellOrderRequest, OrderResponseData } from './types';

class StockOrderApiService extends ApiClient {
  /**
   * 매수 주문
   * @param stockCode 종목코드
   * @param orderData 주문 정보
   * @returns 주문 결과
   */
  async buyStock(stockCode: string, orderData: BuyOrderRequest): Promise<Result<OrderResponseData, AppError>> {
    return await this.post<OrderResponseData>(`/api/stocks/${stockCode}/buy`, orderData);
  }

  /**
   * 매도 주문
   * @param stockCode 종목코드
   * @param orderData 주문 정보
   * @returns 주문 결과
   */
  async sellStock(stockCode: string, orderData: SellOrderRequest): Promise<Result<OrderResponseData, AppError>> {
    return await this.post<OrderResponseData>(`/api/stocks/${stockCode}/sell`, orderData);
  }

}

export const stockOrderApi = new StockOrderApiService();
