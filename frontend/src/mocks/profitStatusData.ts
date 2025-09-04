import imgPhoto14720996457855658Abf4Ff4E from "figma:asset/f2e0d0183a438e31fe7131ed2173548b7f21aea2.png";

export interface ProfitStatusStock {
  name: string;
  code: string;
  buyPrice: number;
  currentPrice: number;
  quantity: number;
  totalValue: number;
  profitRate: number;
  profitAmount: number;
  currentWeight?: number;
  targetWeight?: number;
  weight?: number;
  thresholdWeight?: number;
  logo: string;
}

export interface RebalancingHistoryItem {
  date: string;
  type: string;
  stockCount: number;
  buyAmount: string;
  sellAmount: string;
  status: string;
}

export interface ChartLegendItem {
  name: string;
  weight: number;
  color: string;
}

export const profitStatusStockData: ProfitStatusStock[] = [
  {
    name: '삼성전자',
    code: '005930',
    buyPrice: 68000,
    currentPrice: 71800,
    quantity: 50,
    totalValue: 3590000,
    profitRate: 5.6,
    profitAmount: 190000,
    currentWeight: 32.1,
    targetWeight: 30,
    weight: 6,
    thresholdWeight: 10,
    logo: imgPhoto14720996457855658Abf4Ff4E
  },
  {
    name: 'SK하이닉스',
    code: '000660',
    buyPrice: 85000,
    currentPrice: 89500,
    quantity: 30,
    totalValue: 2685000,
    profitRate: 5.3,
    profitAmount: 135000,
    currentWeight: 24.0,
    targetWeight: 25,
    weight: 5,
    thresholdWeight: 5,
    logo: imgPhoto14720996457855658Abf4Ff4E
  },
  {
    name: 'LG에너지솔루션',
    code: '373220',
    buyPrice: 390000,
    currentPrice: 412000,
    quantity: 15,
    totalValue: 6180000,
    profitRate: 5.6,
    profitAmount: 330000,
    currentWeight: 18.5,
    targetWeight: 20,
    weight: 4,
    thresholdWeight: 10,
    logo: imgPhoto14720996457855658Abf4Ff4E
  },
  {
    name: '삼성바이오로직스',
    code: '207940',
    buyPrice: 750000,
    currentPrice: 789000,
    quantity: 2,
    totalValue: 1578000,
    profitRate: 5.2,
    profitAmount: 78000,
    currentWeight: 14.1,
    targetWeight: 15,
    weight: 3,
    thresholdWeight: 5,
    logo: imgPhoto14720996457855658Abf4Ff4E
  },
  {
    name: 'NAVER',
    code: '035420',
    buyPrice: 175000,
    currentPrice: 183500,
    quantity: 25,
    totalValue: 4587500,
    profitRate: 4.9,
    profitAmount: 212500,
    currentWeight: 11.4,
    targetWeight: 10,
    weight: 2,
    thresholdWeight: 3,
    logo: imgPhoto14720996457855658Abf4Ff4E
  }
];

export const unregisteredStocksData: ProfitStatusStock[] = [
  {
    name: '삼성전자',
    code: '005930',
    buyPrice: 68000,
    currentPrice: 71800,
    quantity: 50,
    totalValue: 3590000,
    profitRate: 5.6,
    profitAmount: 190000,
    logo: imgPhoto14720996457855658Abf4Ff4E
  },
  {
    name: 'SK하이닉스',
    code: '000660',
    buyPrice: 85000,
    currentPrice: 89500,
    quantity: 30,
    totalValue: 2685000,
    profitRate: 5.3,
    profitAmount: 135000,
    logo: imgPhoto14720996457855658Abf4Ff4E
  },
  {
    name: 'LG에너지솔루션',
    code: '373220',
    buyPrice: 390000,
    currentPrice: 412000,
    quantity: 15,
    totalValue: 6180000,
    profitRate: 5.6,
    profitAmount: 330000,
    logo: imgPhoto14720996457855658Abf4Ff4E
  },
  {
    name: '삼성바이오로직스',
    code: '207940',
    buyPrice: 750000,
    currentPrice: 789000,
    quantity: 2,
    totalValue: 1578000,
    profitRate: 5.2,
    profitAmount: 78000,
    logo: imgPhoto14720996457855658Abf4Ff4E
  },
  {
    name: 'NAVER',
    code: '035420',
    buyPrice: 175000,
    currentPrice: 183500,
    quantity: 25,
    totalValue: 4587500,
    profitRate: 4.9,
    profitAmount: 212500,
    logo: imgPhoto14720996457855658Abf4Ff4E
  }
];

export const rebalancingHistoryData: RebalancingHistoryItem[] = [
  {
    date: '2024-11-15',
    type: '자동',
    stockCount: 3,
    buyAmount: '+2,500,000원',
    sellAmount: '-1,800,000원',
    status: '성공'
  },
  {
    date: '2024-10-30', 
    type: '수동',
    stockCount: 5,
    buyAmount: '+3,200,000원',
    sellAmount: '-2,900,000원',
    status: '성공'
  },
  {
    date: '2024-10-15',
    type: '자동',
    stockCount: 2,
    buyAmount: '+1,200,000원',
    sellAmount: '-800,000원',
    status: '성공'
  }
];

export const chartLegendData: ChartLegendItem[] = [
  { name: '삼성전자', weight: 32.1, color: '#3b82f6' },
  { name: 'SK하이닉스', weight: 24.0, color: '#10b981' },
  { name: 'LG에너지솔루션', weight: 18.5, color: '#f59e0b' },
  { name: '삼성바이오로직스', weight: 14.1, color: '#ef4444' },
  { name: 'NAVER', weight: 11.4, color: '#8b5cf6' }
];