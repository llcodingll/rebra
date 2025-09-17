// 포트폴리오 도메인 엔티티 타입들

export interface Stock {
  id: string;
  name: string;
  code: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  totalValue: number;
  profitLoss: number;
  profitLossRate: number;
  type: 'registered' | 'unregistered';
  targetWeight: number;
  currentWeight: number;
  thresholdPercentage: number;
}

export interface Portfolio {
  id: string;
  name: string;
  description: string;
  stockCount: number;
  return: string;
  createdDate: string;
  returnPositive: boolean;
  accountType: 'MOCK' | 'REAL';
}