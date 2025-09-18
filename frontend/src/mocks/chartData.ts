interface ChartApiData {
  id: number;
  date: string;
  totalValue: number;
  cumulativeReturn: number;
}

// 데이터가 있는 상태
const chartApiDataWithData: ChartApiData[] = [
  {
    id: 1001,
    date: "2024-01-15",
    totalValue: 46075000,
    cumulativeReturn: 0.0
  },
  {
    id: 1002,
    date: "2024-01-10",
    totalValue: 46312000,
    cumulativeReturn: 0.5
  },
  {
    id: 1003,
    date: "2024-01-05",
    totalValue: 49412000,
    cumulativeReturn: 7.2
  }
];

// 빈 상태 (처음 접속했을 때)
const chartApiDataEmpty: ChartApiData[] = [];

// 현재 사용할 데이터 (개발할 때 이 부분만 변경)
export const chartApiData = chartApiDataEmpty; // chartApiDataEmpty로 변경하면 빈 상태

export type { ChartApiData };