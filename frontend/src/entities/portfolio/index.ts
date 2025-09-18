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
  targetWeight: number; // 목표 가중치 (서버에서 받은 원본 값)
  targetPercentage: number; // 목표 비중 (가중치 기반 계산된 %)
  currentPercentage: number; // 현재 비중 (현재가 기반 계산된 %)
  thresholdPercentage: number | null;
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