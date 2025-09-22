interface TradeData {
  id: number;
  date: string;
  cumulativeReturn: number;
}

interface MockHistoryData {
  tradeData: TradeData[];
  portfolioPercents: number[];
}

export const generateHistoryMockData = (): MockHistoryData => {
  const startDate = new Date('2024-01-01');
  const days = 90; // 3개월치 데이터
  const mockTradeData: TradeData[] = [];
  const mockPortfolioPercents: number[] = [];

  let portfolioValue = 10000000; // 1천만원 시작

  for (let i = 0; i < days; i++) {
    const currentDate = new Date(startDate);
    currentDate.setDate(startDate.getDate() + i);
    const dateStr = currentDate.toISOString().split('T')[0];

    // 포트폴리오 변동 (랜덤하지만 전체적으로 상승 추세)
    const portfolioChange = (Math.random() - 0.4) * 0.03; // 약간 상승 편향
    portfolioValue *= (1 + portfolioChange);
    const portfolioPercent = ((portfolioValue - 10000000) / 10000000) * 100;

    mockTradeData.push({
      id: i + 1,
      date: dateStr,
      cumulativeReturn: portfolioPercent
    });

    mockPortfolioPercents.push(portfolioPercent);
  }

  return { tradeData: mockTradeData, portfolioPercents: mockPortfolioPercents };
};

// 리밸런싱 날짜 생성 (매월 15일)
export const generateRebalancingDates = (): string[] => {
  return [
    '2024-01-15',
    '2024-02-15',
    '2024-03-15'
  ];
};

// 매수/매도 거래 날짜 생성
export const generateTransactionDates = () => {
  return {
    buyDates: ['2024-01-08', '2024-01-22', '2024-02-05', '2024-02-28', '2024-03-10'],
    sellDates: ['2024-01-12', '2024-01-30', '2024-02-20', '2024-03-05', '2024-03-25']
  };
};