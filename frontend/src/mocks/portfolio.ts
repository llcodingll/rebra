interface PortfolioItem {
  name: string;
  code: string;
  buyPrice: string;
  quantity: string;
  targetWeight: string;
  currentValue: string;
  threshold: string;
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