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

export interface PortfolioCreateRequest {
  name: string;
  description: string;
  accountId: number;
}

export interface PortfolioCreateResponse {
  id: number;
  name: string;
  description: string;
  account: {
    id: number;
    accountNumber: string;
    brokerName: string;
  };
  createdAt: string;
  updatedAt: string;
}

class PortfolioApi {
  private apiClient: ApiClient;

  constructor() {
    this.apiClient = new ApiClient();
  }

  getPortfolioList = async (): Promise<Result<PortfolioListResponse, AppError>> => {
    return this.apiClient.get<PortfolioListResponse>('/api/v1/portfolios');
  }

  createPortfolio = async (requestData: PortfolioCreateRequest): Promise<Result<PortfolioCreateResponse, AppError>> => {
    return this.apiClient.post<PortfolioCreateResponse>('/api/v1/portfolios', requestData);
  }
}

export const portfolioApi = new PortfolioApi();