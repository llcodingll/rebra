import { ApiClient } from '../../../shared/api/apiClient';
import type { Result } from '../../../shared/util/result';
import type { AppError } from '../../../shared/util/appErrors';
import type { MarketIndexResponse } from './types';

const apiClient = new ApiClient();

export const getMarketIndex = (): Promise<Result<MarketIndexResponse, AppError>> => {
  return apiClient.get<MarketIndexResponse>('/api/market-index/latest');
};