interface RebalanceExecutionData {
  id: number;
  portfolioId: number;
  executionType: "AUTO" | "MANUAL";
  totalStocks: number;
  totalBuyAmount: number;
  totalSellAmount: number;
  status: "SUCCESS" | "FAILED" | "PENDING";
  executedAt: string;
}

interface RebalancingSummary {
  period: {
    startDate: string;
    endDate: string;
  };
  totalRebalances: number;
  totalBuyAmount: number;
  totalSellAmount: number;
  autoRebalances: number;
  manualRebalances: number;
}

interface RebalancingHistoryResponse {
  success: boolean;
  status: number;
  data: {
    content: RebalanceExecutionData[];
    pageable: {
      pageNumber: number;
      pageSize: number;
      totalElements: number;
      totalPages: number;
    };
    summary: RebalancingSummary;
  };
  timestamp: string;
}

// 데이터가 있는 상태
const rebalancingHistoryDataWithData: RebalancingHistoryResponse = {
  success: true,
  status: 200,
  data: {
    content: [
      {
        id: 1001,
        portfolioId: 1,
        executionType: "AUTO",
        totalStocks: 3,
        totalBuyAmount: 1575000, // 삼성전자 매도 + SK하이닉스 매수 - LG전자 매도
        totalSellAmount: -1575000,
        status: "SUCCESS",
        executedAt: "2024-01-15T09:00:00Z"
      },
      {
        id: 1002,
        portfolioId: 1,
        executionType: "MANUAL",
        totalStocks: 2,
        totalBuyAmount: 900000, // NAVER 매수
        totalSellAmount: -1500000, // 삼성바이오로직스 매도
        status: "SUCCESS",
        executedAt: "2024-01-10T14:30:00Z"
      },
      {
        id: 1003,
        portfolioId: 1,
        executionType: "AUTO",
        totalStocks: 3,
        totalBuyAmount: 4040000, // 현대차 + LG화학 매수
        totalSellAmount: -1250000, // 삼성물산 매도
        status: "SUCCESS",
        executedAt: "2024-01-05T11:15:00Z"
      }
    ],
    pageable: {
      pageNumber: 0,
      pageSize: 20,
      totalElements: 3,
      totalPages: 1
    },
    summary: {
      period: {
        startDate: "2024-01-01",
        endDate: "2024-01-31"
      },
      totalRebalances: 3,
      totalBuyAmount: 6515000,
      totalSellAmount: -4325000,
      autoRebalances: 2,
      manualRebalances: 1
    }
  },
  timestamp: "2024-01-31T18:00:00Z"
};

// 빈 상태 (처음 접속했을 때)
const rebalancingHistoryDataEmpty: RebalancingHistoryResponse = {
  success: true,
  status: 200,
  data: {
    content: [],
    pageable: {
      pageNumber: 0,
      pageSize: 20,
      totalElements: 0,
      totalPages: 0
    },
    summary: {
      period: {
        startDate: "2024-01-01",
        endDate: "2024-01-31"
      },
      totalRebalances: 0,
      totalBuyAmount: 0,
      totalSellAmount: 0,
      autoRebalances: 0,
      manualRebalances: 0
    }
  },
  timestamp: new Date().toISOString()
};

// 현재 사용할 데이터 (개발할 때 이 부분만 변경)
export const rebalancingHistoryData = rebalancingHistoryDataWithData; // rebalancingHistoryDataEmpty로 변경하면 빈 상태

export type { RebalanceExecutionData, RebalancingSummary, RebalancingHistoryResponse };