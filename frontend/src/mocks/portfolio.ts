export interface PortfolioItem {
  name: string;
  code: string;
  buyPrice: string;
  quantity: string;
  targetWeight: string;
  currentValue: string;
  threshold: string;
}

export interface Portfolio {
  id: string;
  name: string;
  return: string;
  stockCount: number;
  createdDate: string;
  returnPositive: boolean;
  description?: string;
  accountType?: 'MOCK' | 'REAL';
}


export const portfolioData: PortfolioItem[] = [
  { 
    name: '삼성전자', 
    code: '005930', 
    buyPrice: '68,000', 
    quantity: '30', 
    targetWeight: '30', 
    currentValue: '3,590,000원', 
    threshold: '25' 
  },
  { 
    name: 'SK하이닉스', 
    code: '000660', 
    buyPrice: '85,000', 
    quantity: '30', 
    targetWeight: '25', 
    currentValue: '2,685,000원', 
    threshold: '25' 
  },
  { 
    name: 'LG에너지솔루션', 
    code: '373220', 
    buyPrice: '390,000', 
    quantity: '15', 
    targetWeight: '20', 
    currentValue: '6,180,000원', 
    threshold: '25' 
  },
  { 
    name: '삼성바이오로직스', 
    code: '207940', 
    buyPrice: '750,000', 
    quantity: '2', 
    targetWeight: '15', 
    currentValue: '1,578,000원', 
    threshold: '25' 
  },
  { 
    name: 'NAVER', 
    code: '035420', 
    buyPrice: '175,000', 
    quantity: '25', 
    targetWeight: '10', 
    currentValue: '4,587,500원', 
    threshold: '25' 
  }
];

// 데이터가 있는 상태
const portfolioListWithData: Portfolio[] = [
  {
    id: 'portfolio-1',
    name: '반도체 포트폴리오',
    return: '+24.5%',
    stockCount: 5,
    createdDate: '2024-01-15',
    returnPositive: true,
    description: '반도체 대장주 중심'
  },
  {
    id: 'portfolio-2',
    name: '배당 중심 포트폴리오',
    return: '+18.2%',
    stockCount: 5,
    createdDate: '2024-02-10',
    returnPositive: true,
    description: '안정적인 배당 수익'
  },
  {
    id: 'portfolio-3',
    name: '성장주 포트폴리오',
    return: '+32.8%',
    stockCount: 8,
    createdDate: '2024-03-05',
    returnPositive: true,
    description: '고성장 기업 투자'
  },
  {
    id: 'portfolio-4',
    name: '안전자산 포트폴리오',
    return: '+12.1%',
    stockCount: 4,
    createdDate: '2024-01-20',
    returnPositive: true,
    description: '리스크 최소화'
  },
  {
    id: 'portfolio-5',
    name: '테크주 포트폴리오',
    return: '+28.9%',
    stockCount: 6,
    createdDate: '2024-02-28',
    returnPositive: true,
    description: '기술 혁신 기업'
  },
  {
    id: 'portfolio-6',
    name: '글로벌 포트폴리오',
    return: '-5.2%',
    stockCount: 12,
    createdDate: '2024-03-15',
    returnPositive: false,
    description: '해외 주식 분산투자'
  }
];

// 빈 상태 (처음 접속했을 때)
const portfolioListEmpty: Portfolio[] = [];

// 현재 사용할 데이터 (개발할 때 이 부분만 변경)
export const portfolioList = portfolioListEmpty; 
// portfolioListEmpty로 변경하면 빈 상태
//