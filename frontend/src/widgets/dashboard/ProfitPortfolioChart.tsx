import { motion } from 'motion/react';
import styles from './ProfitPortfolioChart.module.css';
import { TrendingUp, TrendingDown, DollarSign } from 'lucide-react';
import DashboardChart from './CumulativeReturnsChart';
import { useState } from 'react';

interface Stock {
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

interface ProfitPortfolioChartProps {
  data: Stock[];
}


export default function ProfitPortfolioChart({ data }: ProfitPortfolioChartProps) {
  const [selectedId, setSelectedId] = useState<number | null>(null);

  // 등록된 주식이 없는 경우 빈 상태 표시
  if (data.length === 0) {
    return (
      <div className={styles.chartContainer}>
        <motion.div 
          initial={{ opacity: 1, y: 0 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className={styles.chartCard}
        >
          <div className={styles.header}>
            <div className={styles.headerLeft}>
              <DollarSign className={styles.headerIcon} />
              <h2 className={styles.title}>수익률 분석</h2>
            </div>
          </div>
          
          <div className={styles.emptyState}>
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className={styles.emptyStateContent}
            >
              <TrendingUp className={styles.emptyStateIcon} />
              <h3 className={styles.emptyStateTitle}>포트폴리오에 주식을 등록해보세요!</h3>
              <p className={styles.emptyStateDescription}>
                주식을 등록하면 수익률과 리밸런싱 분석을 확인할 수 있습니다.
              </p>
            </motion.div>
          </div>
        </motion.div>
      </div>
    );
  }

  // 총 평가액 계산
  const totalValue = data.reduce((sum, stock) => {
    const value = parseInt(stock.value.replace(/[^0-9]/g, ''));
    return sum + value;
  }, 0);

  // 총 수익 계산 
  const totalReturn = data.reduce((sum, stock) => {
    const returnAmount = parseInt(stock.returnAmount.replace(/[^0-9+-]/g, ''));
    return sum + returnAmount;
  }, 0);

  const totalReturnPercent = ((totalReturn / (totalValue - totalReturn)) * 100).toFixed(1);
  const isPositiveReturn = totalReturn >= 0;

  // API 응답 구조에 맞춘 리밸런싱 히스토리 목데이터 (차트 데이터와 동일한 ID)
  const rebalanceHistoryData = {
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

  // ID별 거래 내역 데이터 (리밸런싱 ID 기준)
  const tradesByRebalanceId: { [key: number]: any[] } = {
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
        executedShares: 20,
        price: 115000,
        fee: 11500,
        profitAmount: 32320,
        profitRate: 7.1,
        reason: "목표 비중 조정을 위한 매수 (22.8% → 25.0%)",
        tradeDate: "2024-01-15T09:10:00Z"
      },
      {
        id: 3003,
        stockCode: "066570",
        stockName: "LG전자",
        tradeType: "SELL",
        executedShares: 12,
        price: 85000,
        fee: 5100,
        profitAmount: -45000,
        profitRate: -4.2,
        reason: "목표 비중 조정을 위한 매도",
        tradeDate: "2024-01-15T10:15:00Z"
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
        fee: 4500,
        profitAmount: -15000,
        profitRate: -1.7,
        reason: "목표 비중 조정을 위한 매수",
        tradeDate: "2024-01-10T14:30:00Z"
      },
      {
        id: 3005,
        stockCode: "207940",
        stockName: "삼성바이오로직스",
        tradeType: "SELL",
        executedShares: 2,
        price: 750000,
        fee: 7500,
        profitAmount: 125000,
        profitRate: 8.9,
        reason: "목표 비중 조정을 위한 매도",
        tradeDate: "2024-01-10T15:00:00Z"
      }
    ],
    1003: [
      {
        id: 3006,
        stockCode: "005380",
        stockName: "현대차",
        tradeType: "BUY",
        executedShares: 8,
        price: 190000,
        fee: 7600,
        profitAmount: 0,
        profitRate: 0,
        reason: "목표 비중 조정을 위한 매수",
        tradeDate: "2024-01-05T11:15:00Z"
      },
      {
        id: 3007,
        stockCode: "051910",
        stockName: "LG화학",
        tradeType: "BUY",
        executedShares: 6,
        price: 420000,
        fee: 12600,
        profitAmount: 0,
        profitRate: 0,
        reason: "목표 비중 조정을 위한 매수",
        tradeDate: "2024-01-05T11:30:00Z"
      },
      {
        id: 3008,
        stockCode: "028260",
        stockName: "삼성물산",
        tradeType: "SELL",
        executedShares: 10,
        price: 125000,
        fee: 6250,
        profitAmount: 78000,
        profitRate: 6.5,
        reason: "목표 비중 조정을 위한 매도",
        tradeDate: "2024-01-05T12:00:00Z"
      }
    ]
  };

  // 현재 선택된 ID의 거래 내역 가져오기
  const getCurrentTrades = () => {
    if (!selectedId) return [];
    console.log('Looking for trades for ID:', selectedId);
    console.log('Available IDs:', Object.keys(tradesByRebalanceId));
    const trades = tradesByRebalanceId[selectedId] || [];
    console.log('Found trades:', trades);
    return trades;
  };

  // 차트 데이터 구조 (리밸런싱 히스토리와 동일한 ID 사용)
  const chartApiData = [
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

  // 차트 데이터 변환
  const chartData = chartApiData.map((item) => ({
    id: item.id,
    date: new Date(item.date).toLocaleDateString('ko-KR', { year: 'numeric', month: 'short' }),
    cumulativeReturn: item.cumulativeReturn,
    totalValue: item.totalValue
  }));

  // 성과 데이터 계산
  const portfolioPercents = chartApiData.map((item) => 100 + item.cumulativeReturn);
  const kospiValues = chartApiData.map((_, i) => 100 + i * 1.8);
  const kospiPercents = kospiValues;

  // 차트 스케일 계산
  const allValues = [...portfolioPercents, ...kospiPercents];
  const minValue = Math.min(...allValues);
  const maxValue = Math.max(...allValues);
  const valueRange = maxValue - minValue;
  const padding = valueRange * 0.1;
  const chartMin = Math.max(minValue - padding, 95);
  const chartMax = maxValue + padding;
  const chartRange = chartMax - chartMin;

  // Y축 라벨 생성
  const yAxisLabels: string[] = [];
  for (let i = 0; i <= 4; i++) {
    const value = chartMin + (chartRange / 4) * i;
    yAxisLabels.unshift(value.toFixed(1) + '%');
  }

  // SVG 경로 생성
  const createPath = (values: number[]) => {
    const width = 800;
    const height = 400;
    const padding = 40;

    return values
      .map((value, index) => {
        const x = (index / (values.length - 1)) * (width - 2 * padding) + padding;
        const y = height - padding - ((value - chartMin) / chartRange) * (height - 2 * padding);
        return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
      })
      .join(' ');
  };

  return (
    <div className={styles.chartContainer}>
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className={styles.chartCard}
      >
        {/* 헤더 */}
        <div className={styles.header}>
          <div className={styles.headerLeft}>
            <DollarSign className={styles.headerIcon} />
            <h2 className={styles.title}>수익률 분석</h2>
          </div>
          <div className={styles.headerBadge}>
            <span>총 {data.length}개 종목</span>
          </div>
        </div>

        {/* 메인 컨텐츠 */}
        <div className={styles.content}>
          {/* 좌측 컬럼 */}
          <div className={styles.leftColumn}>
            {/* 총 수익률 정보 */}
            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className={styles.totalValueSection}
            >
              <div className={styles.totalValueHeader}>
                <h3>총 수익률</h3>
                <div className={styles.returnIcon}>
                  {isPositiveReturn ? (
                    <TrendingUp className={styles.iconPositive} />
                  ) : (
                    <TrendingDown className={styles.iconNegative} />
                  )}
                </div>
              </div>
              <div className={styles.amount}>{isPositiveReturn ? '+' : ''}{totalReturnPercent}%</div>
              <div className={`${styles.returnInfo} ${isPositiveReturn ? styles.positive : styles.negative}`}>
                <span className={styles.returnAmount}>
                  {totalReturn >= 0 ? '+' : ''}{totalReturn.toLocaleString()}원
                </span>
                <span className={styles.returnPercent}>
                  (총 평가액: {totalValue.toLocaleString()}원)
                </span>
              </div>
            </motion.div>

            {/* 거래 내역 */}
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.3 }}
              className={styles.recentTrades}
            >
              <h3 className={styles.tradesTitle}>
                {selectedId ? `거래 내역 (ID: ${selectedId})` : '거래 내역을 보려면 차트나 히스토리를 클릭하세요'}
              </h3>
              {getCurrentTrades().length > 0 ? (
                getCurrentTrades().map((trade) => (
                  <div key={trade.id} className={styles.tradeItem}>
                    <div className={styles.tradeHeader}>
                      <span className={styles.stockName}>{trade.stockName}</span>
                      <div className={styles.tradeResult}>
                        <span className={trade.profitAmount >= 0 ? styles.profit : styles.loss}>
                          손익 {trade.profitAmount >= 0 ? '+' : ''}{trade.profitAmount.toLocaleString()}원
                        </span>
                        <span className={trade.profitRate >= 0 ? styles.profitRate : styles.lossRate}>
                          {trade.profitRate >= 0 ? '+' : ''}{trade.profitRate}%
                        </span>
                      </div>
                    </div>
                    <span className={styles.tradeDetail}>
                      {trade.executedShares}주({(trade.executedShares * trade.price).toLocaleString()}원) {trade.tradeType === 'BUY' ? '매수' : '매도'}
                    </span>
                  </div>
                ))
              ) : selectedId ? (
                <div className={styles.noTrades}>해당 리밸런싱에 거래 내역이 없습니다.</div>
              ) : (
                <div className={styles.noTrades}>차트의 점이나 히스토리 행을 클릭해서 거래 내역을 확인하세요.</div>
              )}
            </motion.div>
          </div>

          {/* 차트 */}
          <motion.div 
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className={styles.chartWrapper}
          >
            <DashboardChart
              tradeData={chartData}
              portfolioPercents={portfolioPercents}
              kospiPercents={kospiPercents}
              chartMin={chartMin}
              chartMax={chartMax}
              chartRange={chartRange}
              yAxisLabels={yAxisLabels}
              createPath={createPath}
              onIdClick={setSelectedId}
            />
          </motion.div>

          {/* 리밸런싱 히스토리 */}
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.4 }}
            className={styles.historyContainer}
          >
            <div className={styles.historyHeader}>
              <h3>리밸런싱 히스토리</h3>
            </div>
            
            <div className={styles.historyTable}>
              <div className={styles.historyTableHead}>
                <div className={styles.historyColumnHeader}>실행 일시</div>
                <div className={styles.historyColumnHeader}>유형</div>
                <div className={styles.historyColumnHeader}>거래 종목</div>
                <div className={styles.historyColumnHeader}>매수 금액</div>
                <div className={styles.historyColumnHeader}>매도 금액</div>
              </div>

              <div className={styles.historyTableBody}>
                {rebalanceHistoryData.data.content.map((item) => (
                  <div 
                    key={item.id} 
                    className={styles.historyRow}
                    onClick={() => {
                      console.log('History clicked ID:', item.id);
                      setSelectedId(item.id);
                    }}
                  >
                    <div className={styles.historyCell}>
                      {new Date(item.executedAt).toLocaleDateString('ko-KR')}
                    </div>
                    <div className={styles.historyCell}>
                      <span className={`${styles.typeBadge} ${item.executionType === 'AUTO' ? styles.auto : styles.manual}`}>
                        {item.executionType === 'AUTO' ? '자동' : '수동'}
                      </span>
                    </div>
                    <div className={styles.historyCell}>{item.totalStocks}개</div>
                    <div className={styles.historyCell}>
                      <span className={styles.buyAmount}>{item.totalBuyAmount.toLocaleString()}원</span>
                    </div>
                    <div className={styles.historyCell}>
                      <span className={styles.sellAmount}>{Math.abs(item.totalSellAmount).toLocaleString()}원</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </motion.div>
        </div>
      </motion.div>
    </div>
  );
}