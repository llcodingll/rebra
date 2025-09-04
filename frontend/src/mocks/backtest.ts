import { BacktestResult, RebalancingHistory, ChartLegendData } from '../types';

export const backtestData: BacktestResult[] = Array(10).fill(null).map((_, index) => ({
  id: index + 1,
  name: `백테스트 ${index + 1}`,
  portfolio: `포트폴리오 ${String.fromCharCode(65 + (index % 5))}`,
  period: ['주간', '월간', '분기', '반기', '연간'][index % 5],
  startDate: `2023년 ${String(index + 1).padStart(2, '0')}월`,
  endDate: `2024년 ${String(index + 1).padStart(2, '0')}월`,
  finalReturn: `${(Math.random() * 40 - 10).toFixed(1)}%`,
  maxDrawdown: `-${(Math.random() * 15 + 5).toFixed(1)}%`,
  sharpeRatio: (Math.random() * 2 + 0.5).toFixed(2),
  volatility: `${(Math.random() * 20 + 10).toFixed(1)}%`,
  benchmark: 'KOSPI',
  status: ['completed', 'running', 'failed'][Math.floor(Math.random() * 3)] as 'completed' | 'running' | 'failed',
  createdAt: `2024년 ${String(Math.floor(Math.random() * 12) + 1).padStart(2, '0')}월 ${String(Math.floor(Math.random() * 28) + 1).padStart(2, '0')}일`
}));

// Legacy format for BacktestPage compatibility
export interface LegacyBacktest {
  name: string;
  date: string;
  period: string;
  totalReturn: string;
  maxDrawdown: string;
  sharpeRatio: string;
  status: string;
}

export const legacyBacktestData: LegacyBacktest[] = Array(10).fill(null).map((_, index) => ({
  name: '삼성전자 + SK하이닉스 포트폴리오',
  date: '2024-01-15',
  period: '2023.01 ~ 2024.01',
  totalReturn: '+24.5%',
  maxDrawdown: '-8.2%',
  sharpeRatio: '1.45',
  status: '완료'
}));

export const rebalancingHistory: RebalancingHistory[] = [
  {
    date: "2023.12.15",
    type: "자동 리밸런싱",
    details: "삼성전자 비중 조정 (32% → 30%)"
  },
  {
    date: "2023.11.20",
    type: "수동 리밸런싱", 
    details: "SK하이닉스 신규 편입 (25%)"
  },
  {
    date: "2023.10.10",
    type: "자동 리밸런싱",
    details: "LG에너지솔루션 비중 조정 (18% → 20%)"
  },
  {
    date: "2023.09.05",
    type: "수동 리밸런싱",
    details: "삼성바이오로직스 편입 (15%)"
  }
];

export const chartLegendData: ChartLegendData[] = [
  { label: "삼성전자", color: "#3B82F6", value: "30%" },
  { label: "SK하이닉스", color: "#8B5CF6", value: "25%" },
  { label: "LG에너지솔루션", color: "#10B981", value: "20%" },
  { label: "삼성바이오로직스", color: "#F59E0B", value: "15%" },
  { label: "NAVER", color: "#EF4444", value: "10%" }
];