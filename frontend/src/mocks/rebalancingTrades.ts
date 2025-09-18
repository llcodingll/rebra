interface TradeData {
  id: number;
  stockCode: string;
  stockName: string;
  tradeType: "BUY" | "SELL";
  executedShares: number;
  price: number;
  fee: number;
  profitAmount: number;
  profitRate: number;
  reason: string;
  tradeDate: string;
}

// ID별 거래 내역 데이터 (리밸런싱 ID 기준)
// 데이터가 있는 상태
const tradesByRebalanceIdWithData: { [key: number]: TradeData[] } = {
  1001: [
    {
      id: 3001,
      stockCode: "005930",
      stockName: "삼성전자",
      tradeType: "SELL",
      executedShares: 15,
      price: 162000,
      fee: 12150,
      profitAmount: 375000,
      profitRate: 2.37,
      reason: "목표 비중 조정을 위한 매도 (37.2% → 35.0%)",
      tradeDate: "2024-01-15T09:05:00Z"
    },
    {
      id: 3002,
      stockCode: "000660",
      stockName: "SK하이닉스",
      tradeType: "BUY",
      executedShares: 8,
      price: 196875,
      fee: 11812,
      profitAmount: 0,
      profitRate: 0,
      reason: "목표 비중 달성을 위한 매수 (15.8% → 18.0%)",
      tradeDate: "2024-01-15T09:10:00Z"
    },
    {
      id: 3003,
      stockCode: "066570",
      stockName: "LG전자",
      tradeType: "SELL",
      executedShares: 12,
      price: 131250,
      fee: 9450,
      profitAmount: -187500,
      profitRate: -10.66,
      reason: "포트폴리오 재구성을 위한 매도 (12.5% → 0%)",
      tradeDate: "2024-01-15T09:15:00Z"
    }
  ],
  1002: [
    {
      id: 3004,
      stockCode: "035420",
      stockName: "NAVER",
      tradeType: "BUY",
      executedShares: 5,
      price: 180000,
      fee: 6750,
      profitAmount: 0,
      profitRate: 0,
      reason: "신규 종목 추가 (0% → 8.5%)",
      tradeDate: "2024-01-10T14:35:00Z"
    },
    {
      id: 3005,
      stockCode: "207940",
      stockName: "삼성바이오로직스",
      tradeType: "SELL",
      executedShares: 2,
      price: 750000,
      fee: 22500,
      profitAmount: 525000,
      profitRate: 53.85,
      reason: "수익 실현을 위한 매도 (15.2% → 0%)",
      tradeDate: "2024-01-10T14:40:00Z"
    }
  ],
  1003: [
    {
      id: 3006,
      stockCode: "005380",
      stockName: "현대차",
      tradeType: "BUY",
      executedShares: 10,
      price: 202000,
      fee: 15150,
      profitAmount: 0,
      profitRate: 0,
      reason: "목표 비중 달성을 위한 매수 (8.3% → 15.0%)",
      tradeDate: "2024-01-05T11:20:00Z"
    },
    {
      id: 3007,
      stockCode: "051910",
      stockName: "LG화학",
      tradeType: "BUY",
      executedShares: 6,
      price: 308333,
      fee: 13875,
      profitAmount: 0,
      profitRate: 0,
      reason: "신규 종목 추가 (0% → 12.5%)",
      tradeDate: "2024-01-05T11:25:00Z"
    },
    {
      id: 3008,
      stockCode: "028260",
      stockName: "삼성물산",
      tradeType: "SELL",
      executedShares: 8,
      price: 156250,
      fee: 9375,
      profitAmount: -125000,
      profitRate: -9.09,
      reason: "포트폴리오 재구성을 위한 매도 (10.8% → 0%)",
      tradeDate: "2024-01-05T11:30:00Z"
    }
  ],
  1004: [
    {
      id: 3009,
      stockCode: "005930",
      stockName: "삼성전자",
      tradeType: "BUY",
      executedShares: 20,
      price: 154000,
      fee: 23100,
      profitAmount: 0,
      profitRate: 0,
      reason: "목표 비중 조정을 위한 매수",
      tradeDate: "2024-01-05T10:30:00Z"
    },
    {
      id: 3010,
      stockCode: "000660",
      stockName: "SK하이닉스",
      tradeType: "SELL",
      executedShares: 4,
      price: 195000,
      fee: 5850,
      profitAmount: 78000,
      profitRate: 6.5,
      reason: "목표 비중 조정을 위한 매도",
      tradeDate: "2024-01-05T12:00:00Z"
    }
  ]
};

// 빈 상태 (처음 접속했을 때)
const tradesByRebalanceIdEmpty: { [key: number]: TradeData[] } = {};

// 현재 사용할 데이터 (개발할 때 이 부분만 변경)
export const tradesByRebalanceId = tradesByRebalanceIdEmpty; // tradesByRebalanceIdEmpty로 변경하면 빈 상태

export type { TradeData };