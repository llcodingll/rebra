export interface StockHistoricalDataResponse {
  ticker: string;
  name: string;
  date: string;
  closePrice: number;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  volume: number;
  changeRate: number;
}

export interface BacktestListResponse {
  id: number;
  testName: string;
  startDate: string;
  endDate: string;
  rebalancingType: 'MANUAL' | 'PERIODIC';
  rebalancingPeriod: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  createdAt: string;
  errorMessage?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface BacktestCreateRequest {
  testName: string;
  startDate: string;
  endDate: string;
  rebalancingType: 'MANUAL' | 'PERIODIC';
  rebalancingPeriod?: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
  stocks: BacktestStockRequest[];
}

export interface BacktestStockRequest {
  ticker: string;
  name: string;
  weight: number;
  thresholdPercentage?: number;
  shares: number;
}

export interface BacktestResultResponse {
  id: number;
  testName: string;
  startDate: string;
  endDate: string;
  rebalancingType: 'MANUAL' | 'PERIODIC';
  rebalancingPeriod: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  createdAt: string;
  errorMessage?: string;
  summary?: BacktestSummaryResponse;
  details?: BacktestDetailResponse[];
  portfolioStocks?: BacktestStockResponse[];
}

export interface BacktestSummaryResponse {
  finalValue: number;
  totalReturn: number;
  buyHoldReturn: number;
  excessReturn: number;
  periodGrowthRate: number;
  rebalancingCount: number;
  totalFee: number;
  totalBorrowingCost: number;
  maxBorrowingAmount: number;
  minCashBalance: number;
  maxDrawdown: number;
  volatility: number;
  sharpeRatio: number;
  timeWeightedReturn: number;
  totalReturnPercentage: number;
  buyHoldReturnPercentage: number;
  excessReturnPercentage: number;
  annualizedReturnPercentage: number;
  maxDrawdownPercentage: number;
  volatilityPercentage: number;
}

export interface BacktestDetailResponse {
  date?: string;
  buyAmount?: number;
  sellAmount?: number;
  portfolioValue?: number;
  // 추가 필드들은 백엔드 응답에 따라 확장 가능
}

export interface BacktestStockResponse {
  stockCode: string;
  stockName: string;
  stockType: string;
  targetWeight: number;
  thresholdPercentage: number;
  finalShares: number;
  isActive: boolean;
}

export interface BacktestValidationResponse {
  isValid: boolean;
  summary?: ValidationSummary;
  stockValidations?: StockValidation[];
  warnings?: string[];
  errors?: string[];
}

export interface ValidationSummary {
  actualStartDate: string;
  actualEndDate: string;
  totalTradingDays: number;
  expectedRebalancingCount: number;
  overallDataCoverage: number;
  validStockCount: number;
  totalStockCount: number;
  rebalancingDates: string[];
}

export interface StockValidation {
  ticker: string;
  name: string;
  isValid: boolean;
  dataStartDate: string;
  dataEndDate: string;
  dataCoverage: number;
  dataCount: number;
  issue?: string;
  suggestion?: string;
}