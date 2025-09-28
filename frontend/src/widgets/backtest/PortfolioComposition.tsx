import { useState } from 'react';
import { motion } from 'motion/react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import { PieChart as PieChartIcon, TrendingUp, TrendingDown, BarChart3 } from 'lucide-react';
import styles from './PortfolioComposition.module.css';

interface PortfolioStock {
  stockCode: string;
  stockName: string;
  stockType: string;
  targetWeight: number;
  thresholdPercentage: number | null;
  finalShares: number | null;
  isActive: boolean;
}

interface PortfolioCompositionProps {
  portfolioStocks: PortfolioStock[];
}

// 커스텀 툴팁 컴포넌트
const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className={styles.tooltip}>
        <p className={styles.tooltipTitle}>{data.name}</p>
        <p className={styles.tooltipValue}>비중: {data.value}%</p>
      </div>
    );
  }
  return null;
};

export default function PortfolioComposition({ portfolioStocks }: PortfolioCompositionProps) {
  const [selectedStock, setSelectedStock] = useState(portfolioStocks[0] || null);
  const [hoveredSegment, setHoveredSegment] = useState<string | null>(null);

  if (!portfolioStocks || portfolioStocks.length === 0) {
    return null;
  }


  const COLORS = [
    '#2563eb', // 파란색 
    '#10b981', // 초록색 
    '#f59e0b', // 주황색
    '#ef4444', // 빨간색 
    '#8b5cf6', // 보라색 
    '#06b6d4', // 청록색
  ];

  // Recharts용 데이터 변환
  const chartData = portfolioStocks.map((stock, index) => ({
    name: stock.stockName,
    code: stock.stockCode,
    value: stock.targetWeight,
    color: COLORS[index % COLORS.length]
  }));

  const handleStockSelect = (stock: PortfolioStock) => {
    setSelectedStock(stock);
  };

  const handleMouseEnter = (data: any, index: number) => {
    setHoveredSegment(data.name);
  };

  const handleMouseLeave = () => {
    setHoveredSegment(null);
  };

  const handlePieClick = (chartEntry: any, index: number) => {
    // 클릭된 차트 엔트리의 이름으로 실제 주식 데이터 찾기
    const clickedStock = portfolioStocks.find(stock => stock.stockName === chartEntry.name);
    if (clickedStock) {
      setSelectedStock(clickedStock);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className={styles.compositionCard}
    >
      <div className={styles.cardHeader}>
        <div className={styles.headerLeft}>
          <PieChartIcon className={styles.headerIcon} />
          <h3>백테스트 구성</h3>
        </div>
        <div className={styles.headerBadge}>
          <span>총 {portfolioStocks.length}개 종목</span>
        </div>
      </div>

      <div className={styles.content}>
        {/* 왼쪽: 범례 */}
        <div className={styles.leftPanel}>
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            className={styles.legend}
          >
            <h4 className={styles.legendTitle}>백테스트 구성</h4>
            <div className={styles.legendList}>
              {portfolioStocks.map((stock, index) => (
                <motion.div
                  key={index}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.1 + 0.4 }}
                  className={`${styles.legendItem} ${
                    selectedStock?.stockCode === stock.stockCode ? styles.selected : ''
                  } ${hoveredSegment === stock.stockName ? styles.hovered : ''}`}
                  onClick={() => handleStockSelect(stock)}
                >
                  <div
                    className={styles.legendColor}
                    style={{ backgroundColor: COLORS[index % COLORS.length] }}
                  />
                  <div className={styles.legendContent}>
                    <span className={styles.legendName}>{stock.stockName}</span>
                    <span className={styles.legendCode}>{stock.stockCode}</span>
                  </div>
                  <div className={styles.legendValues}>
                    <span className={styles.legendWeight}>{stock.targetWeight.toFixed(1)}%</span>
                  </div>
                </motion.div>
              ))}
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
              <PieChart>
                <Pie
                  data={chartData}
                  innerRadius={60}
                  outerRadius={120}
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
                      fill={COLORS[index % COLORS.length]}
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
            </ResponsiveContainer>
          </div>
        </motion.div>

        {/* 오른쪽: 상세 정보 */}
        {selectedStock && (
          <motion.div
            key={selectedStock.stockCode}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5 }}
            className={styles.detailPanel}
          >
            <div className={styles.stockHeader}>
              <div className={styles.stockTitleRow}>
                <h4 className={styles.stockName}>{selectedStock.stockName}</h4>
                <span className={styles.stockCode}>{selectedStock.stockCode}</span>
              </div>
            </div>

            <div className={styles.stockMetrics}>
              <div className={styles.metric}>
                <span className={styles.metricLabel}>목표 비중</span>
                <span className={styles.metricValue}>{selectedStock.targetWeight.toFixed(1)}%</span>
              </div>

              {selectedStock.thresholdPercentage && (
                <div className={styles.metric}>
                  <span className={styles.metricLabel}>임계값</span>
                  <span className={styles.metricValue}>{selectedStock.thresholdPercentage * 100}%</span>
                </div>
              )}

              <div className={styles.metric}>
                <span className={styles.metricLabel}>종목 유형</span>
                <span className={styles.metricValue}>{selectedStock.stockType}</span>
              </div>
            </div>

            <div className={styles.targetInfo}>
              <div className={styles.targetHeader}>
                <span className={styles.targetLabel}>비중 시각화</span>
              </div>
              <div className={styles.targetBar}>
                <div
                  className={styles.targetFill}
                  style={{
                    width: `${selectedStock.targetWeight}%`,
                    background: COLORS[portfolioStocks.findIndex(s => s.stockCode === selectedStock.stockCode) % COLORS.length]
                  }}
                ></div>
              </div>
            </div>
          </motion.div>
        )}
      </div>
    </motion.div>
  );
}