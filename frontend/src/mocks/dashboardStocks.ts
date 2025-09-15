export interface DashboardStock {
  name: string;
  code: string;
  buyPrice: string;
  currentPrice: string;
  quantity: string;
  value: string;
  return: string;
  returnAmount: string;
  currentWeight: string;
  targetWeight: string;
  weight: string;
  threshold: string;
  type: 'registered' | 'unregistered';
}

// 데이터가 있는 상태
const dashboardStockDataWithData: DashboardStock[] = [
  {
    name: '삼성전자',
    code: '005930',
    buyPrice: '68,000',
    currentPrice: '71,800',
    quantity: '50주',
    value: '3,590,000원',
    return: '+5.6%',
    returnAmount: '(+190,000원)',
    currentWeight: '32.1%',
    targetWeight: '30',
    weight: '6',
    threshold: '10',
    type: 'registered'
  },
  {
    name: 'SK하이닉스',
    code: '000660',
    buyPrice: '85,000',
    currentPrice: '89,500',
    quantity: '30주',
    value: '2,685,000원',
    return: '+5.3%',
    returnAmount: '(+135,000원)',
    currentWeight: '24.0%',
    targetWeight: '25',
    weight: '5',
    threshold: '5',
    type: 'registered'
  },
  {
    name: 'LG에너지솔루션',
    code: '373220',
    buyPrice: '390,000',
    currentPrice: '412,000',
    quantity: '15주',
    value: '6,180,000원',
    return: '+5.6%',
    returnAmount: '(+330,000원)',
    currentWeight: '18.5%',
    targetWeight: '20',
    weight: '4',
    threshold: '10',
    type: 'registered'
  },
  {
    name: '삼성바이오로직스',
    code: '207940',
    buyPrice: '750,000',
    currentPrice: '789,000',
    quantity: '2주',
    value: '1,578,000원',
    return: '+5.2%',
    returnAmount: '(+78,000원)',
    currentWeight: '14.1%',
    targetWeight: '15',
    weight: '3',
    threshold: '5',
    type: 'registered'
  },
  {
    name: 'NAVER',
    code: '035420',
    buyPrice: '175,000',
    currentPrice: '183,500',
    quantity: '25주',
    value: '4,587,500원',
    return: '+4.9%',
    returnAmount: '(+212,500원)',
    currentWeight: '11.4%',
    targetWeight: '10',
    weight: '2',
    threshold: '3',
    type: 'unregistered'
  },
  {
    name: '카카오',
    code: '035720',
    buyPrice: '45,000',
    currentPrice: '48,200',
    quantity: '40주',
    value: '1,928,000원',
    return: '+7.1%',
    returnAmount: '(+128,000원)',
    currentWeight: '0%',
    targetWeight: '0',
    weight: '0',
    threshold: '0',
    type: 'unregistered'
  }
];

// 빈 상태 (처음 접속했을 때)
const dashboardStockDataEmpty: DashboardStock[] = [];

// 현재 사용할 데이터 (개발할 때 이 부분만 변경)
export const dashboardStockData = dashboardStockDataWithData; // dashboardStockDataEmpty로 변경하면 빈 상태