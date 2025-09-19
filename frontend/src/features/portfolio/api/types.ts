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
    thresholdPercentage: number | null;
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

export interface StockUpdateRequest {
  stocks: Array<{
    stockCode: string;
    targetWeight: number;
    thresholdPercentage: number;
  }>;
}

export interface StockUpdateResponse {
  success: boolean;
  status: number;
  data: Array<{
    portfolioStockId: number;
    portfolioId: number;
    stockCode: string;
    stockName: string;
    targetWeight: number;
    thresholdPercentage: number;
    status: string;
    createdAt: string;
  }>;
  errorCode?: string;
  errorMessage?: string;
  errorData?: string;
  timestamp: string;
}

export interface RebalancingHistoryItem {
  orderId: number | null;
  executedAt: string;
  cumulativeReturn: number;
}

export interface RebalancingHistoryResponse {
  success: boolean;
  status: number;
  data: RebalancingHistoryItem[];
  errorCode?: string;
  errorMessage?: string;
  errorData?: string;
  timestamp: string;
}

export interface ChartDataPoint {
  id: number;
  date: string;
  cumulativeReturn: number;
}

export interface RebalancingHistoryTableItem {
  orderId: number;
  executionType: 'AUTO' | 'MANUAL';
  totalStocks: number;
  totalBuyAmount: number;
  totalSellAmount: number;
  totalPortfolioValue: number;
  status: string;
  executedAt: string;
}

export interface RebalancingHistorySummary {
  period: {
    startDate: string;
    endDate: string;
  };
  totalRebalances: number;
  totalBuyAmount: number;
  totalSellAmount: number;
  autoRebalances: number;
  manualRebalances: number;
}

export interface PageInfo {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface RebalancingHistoryTableResponse {
  success: boolean;
  status: number;
  data: {
    success: boolean;
    message: string;
    content: {
      histories: RebalancingHistoryTableItem[];
      summary: RebalancingHistorySummary;
    };
    pageInfo: PageInfo;
    timestamp: string;
  };
  errorCode?: string;
  errorMessage?: string;
  errorData?: string;
  timestamp: string;
}

export interface TradeDetail {
  tradeId: number;
  stockCode: string;
  stockName: string;
  tradeType: string;
  executedShares: number;
  price: number;
  fee: number;
  profitAmount: number;
  profitRate: number;
  reason: string;
  tradeDate: string;
}

export interface RebalancingHistoryDetailResponse {
  success: boolean;
  status: number;
  data: {
    orderId: number;
    executionType: 'AUTO' | 'MANUAL';
    executedAt: string;
    cumulativeReturn: number;
    totalPortfolioValue: number;
    trades: TradeDetail[];
  };
  errorCode?: string;
  errorMessage?: string;
  errorData?: string;
  timestamp: string;
}

export interface AutoRebalancingRequest {
  autoRebalancing: boolean;
}

export interface AutoRebalancingResponse {
  success: boolean;
  status: number;
  data: {
    portfolioId: number;
    name: string;
    description: string;
    message: string;
    updatedAt: string;
  };
  errorCode?: string;
  errorMessage?: string;
  errorData?: string;
  timestamp: string;
}

export interface RebalancingExecuteResponse {
  success: boolean;
  status: number;
  data: {
    success: boolean;
    rebalancingOrderId: number;
    executionTime: string;
    totalBuyAmount: number;
    totalSellAmount: number;
    totalPortfolioValue: number;
    orderResults: {
      stockCode: string;
      stockName: string;
      orderType: string;
      quantity: number;
      price: number;
      success: boolean;
      orderId: string;
      errorMessage: string;
    }[];
    failureReason: string;
  };
  errorCode?: string;
  errorMessage?: string;
  errorData?: string;
  timestamp: string;
}