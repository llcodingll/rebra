import { ApiClient } from '../../../shared/api/apiClient';
import type { Result } from '../../../shared/util/result';
import type { AppError } from '../../../shared/util/appErrors';

export interface PortfolioItem {
  portfolioId: number;
  name: string;
  description: string;
  registeredStockCount: number;
  totalReturnRate: number;
  isAccountConnected: boolean;
  createdAt: string;
  accountType: 'MOCK' | 'REAL';
}

export interface PortfolioListResponse {
  totalCount: number;
  portfolios: PortfolioItem[];
}

class PortfolioApi {
  private apiClient: ApiClient;

  constructor() {
    this.apiClient = new ApiClient();
  }

  async getPortfolioList(): Promise<Result<PortfolioListResponse, AppError>> {
    return this.apiClient.get<PortfolioListResponse>('/api/v1/portfolios');
  }
}

export const portfolioApi = new PortfolioApi();