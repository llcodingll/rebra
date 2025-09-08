interface Stock {
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

export const stockData: Stock[] = [
  { 
    name: '삼성전자', 
    code: '005930', 
    sector: 'KOSPI', 
    category: '반도체', 
    price: '71,800원', 
    change: '+1.2%', 
    changeType: 'positive', 
    volume: '1250만', 
    marketCap: '429.0조' 
  },
  { 
    name: '셀트리온', 
    code: '068270', 
    sector: 'KOSPI', 
    category: '바이오', 
    price: '145,000원', 
    change: '-1.2%', 
    changeType: 'negative', 
    volume: '180만', 
    marketCap: '62.0조' 
  },
  { 
    name: '카카오', 
    code: '035720', 
    sector: 'KOSPI', 
    category: '인터넷', 
    price: '51,200원', 
    change: '+3.5%', 
    changeType: 'positive', 
    volume: '210만', 
    marketCap: '22.0조' 
  },
  { 
    name: 'LG에너지솔루션', 
    code: '373220', 
    sector: 'KOSPI', 
    category: '배터리', 
    price: '412,000원', 
    change: '+2.3%', 
    changeType: 'positive', 
    volume: '95만', 
    marketCap: '96.0조' 
  },
  { 
    name: 'SK하이닉스', 
    code: '000660', 
    sector: 'KOSPI', 
    category: '반도체', 
    price: '89,500원', 
    change: '-0.8%', 
    changeType: 'negative', 
    volume: '820만', 
    marketCap: '65.0조' 
  }
];

export const extendedStockData: Stock[] = [
  ...stockData,
  { 
    name: '네이버', 
    code: '035420', 
    sector: 'KOSPI', 
    category: '인터넷', 
    price: '183,500원', 
    change: '+0.5%', 
    changeType: 'positive', 
    volume: '315만', 
    marketCap: '30.2조' 
  },
  { 
    name: 'LG화학', 
    code: '051910', 
    sector: 'KOSPI', 
    category: '화학', 
    price: '398,500원', 
    change: '-2.1%', 
    changeType: 'negative', 
    volume: '125만', 
    marketCap: '28.1조' 
  },
  { 
    name: '삼성바이오로직스', 
    code: '207940', 
    sector: 'KOSPI', 
    category: '바이오', 
    price: '789,000원', 
    change: '+1.8%', 
    changeType: 'positive', 
    volume: '45만', 
    marketCap: '56.8조' 
  }
];

export const unregisteredStocks: Stock[] = [
  { 
    name: '현대차', 
    code: '005380', 
    sector: 'KOSPI', 
    category: '자동차', 
    price: '198,500원', 
    change: '+1.5%', 
    changeType: 'positive' 
  },
  { 
    name: 'POSCO홀딩스', 
    code: '005490', 
    sector: 'KOSPI', 
    category: '철강', 
    price: '398,000원', 
    change: '-0.3%', 
    changeType: 'negative' 
  }
];