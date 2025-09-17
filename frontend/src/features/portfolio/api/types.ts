// 포트폴리오 API 관련 타입들

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

export interface PortfolioDetailResponse {
  portfolio: {
    id: number;
    name: string;
    description: string;
    account: {
      id: number;
      accountNumber: string;
      brokerName: string;
    };
    autoRebalance: boolean;
    rebalancingType: string | null;
    rebalancingPeriod: number | null;
    startDate: string | null;
    nextRebalancingDate: string | null;
    createdAt: string | null;
    updatedAt: string | null;
  };
  registeredStocks: Array<{
    stockCode: string;
    stockName: string;
    purchasePrice: number;
    quantity: number;
    currentPrice: number;
    targetWeight: number;
    thresholdPercentage: number;
    status: string;
  }>;
  unregisteredStocks: Array<{
    stockCode: string;
    stockName: string;
    purchasePrice: number;
    quantity: number;
    currentPrice: number;
  }>;
}

export interface StockRegisterRequest {
  stockCode: string;
}

export interface StockRegisterResponse {
  portfolioStockId: number;
  portfolioId: number;
  stockCode: string;
  stockName: string;
  targetWeight: number;
  thresholdPercentage: number;
  status: string;
  createdAt: string;
}

export interface StockDeleteRequest {
  stockCode: string;
}

export interface StockDeleteResponse {
  success: boolean;
  status: number;
  timestamp: string;
}