import { useState } from 'react';
import { motion } from 'motion/react';
import styles from './ProfitPortfolioChart.module.css';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';
import { TrendingUp, TrendingDown, DollarSign, PieChart as PieChartIcon, Award, AlertTriangle } from 'lucide-react';

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

// 커스텀 툴팁 컴포넌트
const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className={styles.tooltip}>
        <p className={styles.tooltipTitle}>{data.name}</p>
        <p className={styles.tooltipValue}>수익률: {data.value}%</p>
        <p className={styles.tooltipAmount}>수익금: {data.amount}</p>
      </div>
    );
  }
  return null;
};

export default function ProfitPortfolioChart({ data }: ProfitPortfolioChartProps) {
  const [selectedStock, setSelectedStock] = useState(data[0] || null);
  const [hoveredSegment, setHoveredSegment] = useState<string | null>(null);
  const [chartType, setChartType] = useState<'pie' | 'bar'>('pie');

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

  // 수익률 기준으로 정렬
  const sortedByProfit = [...data].sort((a, b) => {
    const aReturn = parseFloat(a.return.replace(/[^0-9.-]/g, ''));
    const bReturn = parseFloat(b.return.replace(/[^0-9.-]/g, ''));
    return bReturn - aReturn;
  });

  // 수익/손실 종목 분류
  const profitStocks = data.filter(stock => parseFloat(stock.return.replace(/[^0-9.-]/g, '')) > 0);
  const lossStocks = data.filter(stock => parseFloat(stock.return.replace(/[^0-9.-]/g, '')) < 0);

  // Recharts용 데이터 변환 (수익률 기준)
  const chartData = sortedByProfit.map((stock, index) => ({
    name: stock.name,
    code: stock.code,
    value: Math.abs(parseFloat(stock.return.replace(/[^0-9.-]/g, ''))),
    amount: stock.returnAmount,
    isPositive: parseFloat(stock.return.replace(/[^0-9.-]/g, '')) >= 0,
    color: parseFloat(stock.return.replace(/[^0-9.-]/g, '')) >= 0 
      ? `hsl(${120 + index * 30}, 70%, 50%)` 
      : `hsl(${0 + index * 15}, 70%, 50%)`
  }));

  // 수익성 기준 색상 팔레트
  const PROFIT_COLORS = [
    '#10b981', // 진한 초록
    '#34d399', // 밝은 초록  
    '#6ee7b7', // 연한 초록
  ];
  
  const LOSS_COLORS = [
    '#ef4444', // 진한 빨강
    '#f87171', // 밝은 빨강
    '#fca5a5', // 연한 빨강
  ];

  const handleStockSelect = (stock: Stock) => {
    setSelectedStock(stock);
  };

  const handleMouseEnter = (data: any, index: number) => {
    setHoveredSegment(data.name);
  };

  const handleMouseLeave = () => {
    setHoveredSegment(null);
  };

  const handlePieClick = (chartEntry: any, index: number) => {
    const clickedStock = data.find(stock => stock.name === chartEntry.name);
    if (clickedStock) {
      setSelectedStock(clickedStock);
    }
  };

  const getStockColor = (stock: Stock, index: number) => {
    const returnValue = parseFloat(stock.return.replace(/[^0-9.-]/g, ''));
    if (returnValue >= 0) {
      return PROFIT_COLORS[index % PROFIT_COLORS.length];
    } else {
      return LOSS_COLORS[index % LOSS_COLORS.length];
    }
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
          <div className={styles.headerControls}>
            <div className={styles.chartToggle}>
              <button 
                className={`${styles.toggleBtn} ${chartType === 'pie' ? styles.active : ''}`}
                onClick={() => setChartType('pie')}
              >
                <PieChartIcon size={16} />
                파이차트
              </button>
              <button 
                className={`${styles.toggleBtn} ${chartType === 'bar' ? styles.active : ''}`}
                onClick={() => setChartType('bar')}
              >
                <TrendingUp size={16} />
                막대차트
              </button>
            </div>
            <div className={styles.headerBadge}>
              <span>수익 {profitStocks.length}개 | 손실 {lossStocks.length}개</span>
            </div>
          </div>
        </div>

        {/* 메인 컨텐츠 */}
        <div className={styles.content}>
          {/* 왼쪽: 수익률 요약 + 순위 */}
          <div className={styles.leftPanel}>
            {/* 총 수익률 정보 */}
            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className={styles.profitSummarySection}
            >
              <div className={styles.profitSummaryHeader}>
                <h3>총 수익률</h3>
                <div className={styles.returnIcon}>
                  {isPositiveReturn ? (
                    <TrendingUp className={styles.iconPositive} />
                  ) : (
                    <TrendingDown className={styles.iconNegative} />
                  )}
                </div>
              </div>
              <div className={`${styles.profitPercent} ${isPositiveReturn ? styles.positive : styles.negative}`}>
                {totalReturn >= 0 ? '+' : ''}{totalReturnPercent}%
              </div>
              <div className={`${styles.profitAmount} ${isPositiveReturn ? styles.positive : styles.negative}`}>
                {totalReturn >= 0 ? '+' : ''}{totalReturn.toLocaleString()}원
              </div>
              <div className={styles.totalValue}>
                총 평가액: {totalValue.toLocaleString()}원
              </div>
            </motion.div>

            {/* 수익률 순위 */}
            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.3 }}
              className={styles.rankingSection}
            >
              <h4 className={styles.rankingTitle}>
                <Award className={styles.rankingIcon} />
                수익률 순위
              </h4>
              <div className={styles.rankingList}>
                {sortedByProfit.map((stock, index) => {
                  const returnValue = parseFloat(stock.return.replace(/[^0-9.-]/g, ''));
                  const isPositive = returnValue >= 0;
                  return (
                    <motion.div
                      key={index}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.1 + 0.4 }}
                      className={`${styles.rankingItem} ${
                        selectedStock?.code === stock.code ? styles.selected : ''
                      } ${hoveredSegment === stock.name ? styles.hovered : ''}`}
                      onClick={() => handleStockSelect(stock)}
                    >
                      <div className={styles.rankNumber}>{index + 1}</div>
                      <div 
                        className={styles.rankColor} 
                        style={{ backgroundColor: getStockColor(stock, index) }}
                      />
                      <div className={styles.rankContent}>
                        <span className={styles.rankName}>{stock.name}</span>
                        <span className={styles.rankCode}>{stock.code}</span>
                      </div>
                      <div className={styles.rankValues}>
                        <span className={`${styles.rankReturn} ${isPositive ? styles.positive : styles.negative}`}>
                          {stock.return}
                        </span>
                        <span className={styles.rankWeight}>{stock.currentWeight}</span>
                      </div>
                    </motion.div>
                  );
                })}
              </div>
            </motion.div>
          </div>

          {/* 중앙: 차트 */}
          <motion.div 
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className={styles.chartWrapper}
          >
            <div className={styles.chartInner}>
              <ResponsiveContainer width="100%" height="100%">
                {chartType === 'pie' ? (
                  <PieChart>
                    <Pie
                      data={chartData}
                      innerRadius={80}
                      outerRadius={140}
                      paddingAngle={3}
                      dataKey="value"
                      strokeWidth={0}
                      onMouseEnter={handleMouseEnter}
                      onMouseLeave={handleMouseLeave}
                      onClick={handlePieClick}
                    >
                      {chartData.map((entry, index) => (
                        <Cell 
                          key={`cell-${index}`} 
                          fill={entry.isPositive ? PROFIT_COLORS[index % PROFIT_COLORS.length] : LOSS_COLORS[index % LOSS_COLORS.length]}
                          style={{
                            filter: hoveredSegment === entry.name 
                              ? 'drop-shadow(0 8px 16px rgba(0,0,0,0.2)) brightness(1.1)' 
                              : 'drop-shadow(0 4px 8px rgba(0,0,0,0.1))',
                            cursor: 'pointer',
                            transition: 'all 0.3s ease'
                          }}
                        />
                      ))}
                    </Pie>
                    <Tooltip content={<CustomTooltip />} />
                  </PieChart>
                ) : (
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis 
                      dataKey="code" 
                      tick={{ fontSize: 12 }}
                      interval={0}
                      angle={-45}
                      textAnchor="end"
                      height={80}
                    />
                    <YAxis 
                      tick={{ fontSize: 12 }}
                      label={{ value: '수익률 (%)', angle: -90, position: 'insideLeft' }}
                    />
                    <Tooltip content={<CustomTooltip />} />
                    <Bar 
                      dataKey="value"
                      fill={(entry: any) => entry.isPositive ? '#10b981' : '#ef4444'}
                      radius={[4, 4, 0, 0]}
                    >
                      {chartData.map((entry, index) => (
                        <Cell 
                          key={`bar-${index}`}
                          fill={entry.isPositive ? '#10b981' : '#ef4444'}
                        />
                      ))}
                    </Bar>
                  </BarChart>
                )}
              </ResponsiveContainer>
            </div>
          </motion.div>

          {/* 오른쪽: 상세 정보 */}
          {selectedStock && (
            <motion.div 
              key={selectedStock.code}
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5 }}
              className={styles.detailPanel}
            >
              <div className={styles.stockHeader}>
                <div className={styles.stockTitleRow}>
                  <h4 className={styles.stockName}>{selectedStock.name}</h4>
                  <span className={styles.stockCode}>{selectedStock.code}</span>
                </div>
                <div className={styles.stockPrice}>
                  <span className={styles.currentPrice}>{selectedStock.currentPrice}</span>
                </div>
              </div>

              {/* 수익 정보 */}
              <div className={styles.profitInfo}>
                <div className={styles.profitHeader}>
                  <span>수익 정보</span>
                  {parseFloat(selectedStock.return.replace(/[^0-9.-]/g, '')) >= 0 ? (
                    <TrendingUp className={styles.profitIcon} />
                  ) : (
                    <TrendingDown className={styles.lossIcon} />
                  )}
                </div>
                <div className={styles.profitMetrics}>
                  <div className={styles.profitRow}>
                    <span className={styles.profitLabel}>수익률</span>
                    <span className={`${styles.profitValue} ${
                      selectedStock.return.startsWith('+') ? styles.positive : styles.negative
                    }`}>
                      {selectedStock.return}
                    </span>
                  </div>
                  <div className={styles.profitRow}>
                    <span className={styles.profitLabel}>수익금</span>
                    <span className={`${styles.profitValue} ${
                      selectedStock.returnAmount.startsWith('+') ? styles.positive : styles.negative
                    }`}>
                      {selectedStock.returnAmount}
                    </span>
                  </div>
                </div>
              </div>

              <div className={styles.stockMetrics}>
                <div className={styles.metricRow}>
                  <div className={styles.metric}>
                    <span className={styles.metricLabel}>매수가</span>
                    <span className={styles.metricValue}>{selectedStock.buyPrice}</span>
                  </div>
                  <div className={styles.metric}>
                    <span className={styles.metricLabel}>보유 수량</span>
                    <span className={styles.metricValue}>{selectedStock.quantity}</span>
                  </div>
                </div>
                
                <div className={styles.metricRow}>
                  <div className={styles.metric}>
                    <span className={styles.metricLabel}>평가 금액</span>
                    <span className={styles.metricValue}>{selectedStock.value}</span>
                  </div>
                  <div className={styles.metric}>
                    <span className={styles.metricLabel}>포트폴리오 비중</span>
                    <span className={styles.metricValue}>{selectedStock.currentWeight}</span>
                  </div>
                </div>
              </div>

              {/* 투자 전략 알림 */}
              <div className={styles.strategyInfo}>
                <div className={styles.strategyHeader}>
                  <AlertTriangle className={styles.strategyIcon} />
                  <span>투자 전략</span>
                </div>
                <div className={styles.strategyContent}>
                  {parseFloat(selectedStock.return.replace(/[^0-9.-]/g, '')) > 15 ? (
                    <p className={styles.strategyText}>
                      높은 수익률을 기록하고 있습니다. 일부 매도를 고려해보세요.
                    </p>
                  ) : parseFloat(selectedStock.return.replace(/[^0-9.-]/g, '')) < -10 ? (
                    <p className={styles.strategyText}>
                      손실이 큰 상황입니다. 손절 또는 추가 매수를 검토하세요.
                    </p>
                  ) : (
                    <p className={styles.strategyText}>
                      안정적인 수익률을 유지하고 있습니다. 현재 포지션을 유지하세요.
                    </p>
                  )}
                </div>
              </div>
            </motion.div>
          )}
        </div>
      </motion.div>
    </div>
  );
}