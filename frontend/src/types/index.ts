export interface Stock {
  name: string;
  code: string;
  sector: string;
  category: string;
  price: string;
  change: string;
  changeType: 'positive' | 'negative' | 'neutral';
  volume?: string;
  marketCap?: string;
  buyPrice?: string;
  quantity?: string;
  targetWeight?: string;
  currentValue?: string;
  threshold?: string;
}

export interface NewsItem {
  title: string;
  summary: string;
  source: string;
  time: string;
  sentiment?: 'positive' | 'negative' | 'neutral';
}

export interface PortfolioItem {
  name: string;
  code: string;
  buyPrice: string;
  quantity: string;
  targetWeight: string;
  currentValue: string;
  threshold: string;
}

export interface BacktestResult {
  id: number;
  name: string;
  portfolio: string;
  period: string;
  startDate: string;
  endDate: string;
  finalReturn: string;
  maxDrawdown: string;
  sharpeRatio: string;
  volatility: string;
  benchmark: string;
  status: 'completed' | 'running' | 'failed';
  createdAt: string;
}

export interface RebalancingHistory {
  date: string;
  type: string;
  details: string;
}

export interface ChartLegendData {
  label: string;
  color: string;
  value: string;
}