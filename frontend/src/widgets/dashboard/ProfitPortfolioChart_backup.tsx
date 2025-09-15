import { motion } from 'motion/react';
import styles from './ProfitPortfolioChart.module.css';
import { TrendingUp, TrendingDown, DollarSign } from 'lucide-react';
import DashboardChart from './CumulativeReturnsChart';
import { useState } from 'react';
import { rebalancingHistoryData } from '../../mocks/rebalancingHistory';
import { tradesByRebalanceId } from '../../mocks/rebalancingTrades';
import { chartApiData } from '../../mocks/chartData';

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

  // 현재 선택된 ID의 거래 내역 가져오기
  const getCurrentTrades = () => {
    if (!selectedId) return [];
    console.log('Looking for trades for ID:', selectedId);
    console.log('Available IDs:', Object.keys(tradesByRebalanceId));
    const trades = tradesByRebalanceId[selectedId] || [];
    console.log('Found trades:', trades);
    return trades;
  };

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
      <div className={styles.chartCard}>
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
                  <div key={trade.id} className={`${styles.tradeItem} ${trade.tradeType === 'BUY' ? styles.buyTrade : styles.sellTrade}`}>
                    <div className={styles.tradeHeader}>
                      <div className={styles.stockInfo}>
                        <span className={styles.stockName}>{trade.stockName}</span>
                        <span className={`${styles.tradeTypeBadge} ${trade.tradeType === 'BUY' ? styles.buyBadge : styles.sellBadge}`}>
                          {trade.tradeType === 'BUY' ? '매수' : '매도'}
                        </span>
                      </div>
                      {trade.tradeType === 'SELL' && (
                        <div className={styles.tradeResult}>
                          <span className={trade.profitAmount >= 0 ? styles.profit : styles.loss}>
                            손익 {trade.profitAmount >= 0 ? '+' : ''}{trade.profitAmount.toLocaleString()}원
                          </span>
                          <span className={trade.profitRate >= 0 ? styles.profitRate : styles.lossRate}>
                            {trade.profitRate >= 0 ? '+' : ''}{trade.profitRate}%
                          </span>
                        </div>
                      )}
                    </div>
                    <span className={styles.tradeDetail}>
                      {trade.executedShares}주({(trade.executedShares * trade.price).toLocaleString()}원)
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
        </div>
    </div>
  );
}