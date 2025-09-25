import { useState } from 'react';
import { motion } from 'motion/react';
import styles from './AssetPortfolioChart.module.css';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import { TrendingUp, TrendingDown, BarChart3, Target, AlertCircle } from 'lucide-react';

import type { Stock } from '../../entities/portfolio';

interface AssetPortfolioChartProps {
  data: Stock[];
}

// 커스텀 툴팁 컴포넌트
const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className={styles.tooltip}>
        <p className={styles.tooltipTitle}>{data.name}</p>
        <p className={styles.tooltipValue}>비중: {data.value.toFixed(2)}%</p>
      </div>
    );
  }
  return null;
};

export default function AssetPortfolioChart({ data }: AssetPortfolioChartProps) {
  const [selectedStock, setSelectedStock] = useState(data[0] || null);
  const [hoveredSegment, setHoveredSegment] = useState<string | null>(null);
  console.log("AssetPortfolioChart 렌더링", data.length);

  // 등록된 주식이 없는 경우 빈 상태 표시
  if (data.length === 0) {
    return (
      <div className={styles.chartContainer}>
        <motion.div 
                        initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
          className={styles.chartCard}
        >
          <div className={styles.header}>
            <div className={styles.headerLeft}>
              <BarChart3 className={styles.headerIcon} />
              <h2 className={styles.title}>포트폴리오 구성</h2>
            </div>
          </div>
          
          <div className={styles.emptyState}>
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className={styles.emptyStateContent}
            >
              <BarChart3 className={styles.emptyStateIcon} />
              <h3 className={styles.emptyStateTitle}>포트폴리오에 주식을 등록해보세요!</h3>
              <p className={styles.emptyStateDescription}>
                주식을 등록하면 포트폴리오 구성과 수익률을 확인할 수 있습니다.
              </p>
            </motion.div>
          </div>
        </motion.div>
      </div>
    );
  }

  // 총 평가액 계산
  const totalValue = data.reduce((sum, stock) => {
    return sum + stock.totalValue;
  }, 0);

  // 총 수익 계산
  const totalReturn = data.reduce((sum, stock) => {
    return sum + stock.profitLoss;
  }, 0);

  const totalReturnPercent = ((totalReturn / (totalValue - totalReturn)) * 100).toFixed(1);
  const isPositiveReturn = totalReturn >= 0;

  // Recharts용 데이터 변환
  const chartData = data.map((stock, index) => ({
    name: stock.name,
    code: stock.code,
    value: stock.currentPercentage,
    color: `hsl(${index * 60}, 70%, 50%)`
  }));

  // 주식 테마에 맞는 색상 팔레트
  const COLORS = [
    '#2563eb', // 파란색 (신뢰감)
    '#10b981', // 초록색 (성장)
    '#f59e0b', // 주황색 (에너지)
    '#ef4444', // 빨간색 (주의)
    '#8b5cf6', // 보라색 (혁신)
    '#06b6d4', // 청록색 (안정)
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
    // 클릭된 차트 엔트리의 이름으로 실제 주식 데이터 찾기
    const clickedStock = data.find(stock => stock.name === chartEntry.name);
    if (clickedStock) {
      setSelectedStock(clickedStock);
    }
  };

  return (
    <div className={styles.chartContainer}>
      <div className={styles.chartCard}>
        {/* 헤더 */}
        <div className={styles.header}>
          <div className={styles.headerLeft}>
            <BarChart3 className={styles.headerIcon} />
            <h2 className={styles.title}>포트폴리오 구성</h2>
          </div>
          <div className={styles.headerBadge}>
            <span>총 {data.length}개 종목</span>
          </div>
        </div>

        {/* 메인 컨텐츠 */}
        <div className={styles.content}>
          {/* 왼쪽: 총 평가액 + 범례 */}
          <div className={styles.leftPanel}>
            {/* 총 평가액 정보 */}
            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className={styles.totalValueSection}
            >
              <div className={styles.totalValueHeader}>
                <h3>총 평가액</h3>
                <div className={styles.returnIcon}>
                  {isPositiveReturn ? (
                    <TrendingUp className={styles.iconPositive} />
                  ) : (
                    <TrendingDown className={styles.iconNegative} />
                  )}
                </div>
              </div>
              <div className={styles.amount}>{totalValue.toLocaleString()}원</div>
              <div className={`${styles.returnInfo} ${isPositiveReturn ? styles.positive : styles.negative}`}>
                <span className={styles.returnAmount}>
                  {totalReturn >= 0 ? '+' : ''}{Math.floor(totalReturn).toLocaleString()}원
                </span>
                <span className={styles.returnPercent}>
                  ({totalReturn >= 0 ? '+' : ''}{totalReturnPercent}%)
                </span>
              </div>
            </motion.div>

            {/* 범례 */}
            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.3 }}
              className={styles.legend}
            >
              <h4 className={styles.legendTitle}>포트폴리오 구성</h4>
              <div className={styles.legendList}>
                {data.map((stock, index) => (
                  <motion.div
                    key={index}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.1 + 0.4 }}
                    className={`${styles.legendItem} ${
                      selectedStock?.code === stock.code ? styles.selected : ''
                    } ${hoveredSegment === stock.name ? styles.hovered : ''}`}
                    onClick={() => handleStockSelect(stock)}
                  >
                    <div 
                      className={styles.legendColor} 
                      style={{ backgroundColor: COLORS[index % COLORS.length] }}
                    />
                    <div className={styles.legendContent}>
                      <span className={styles.legendName}>{stock.name}</span>
                      <span className={styles.legendCode}>{stock.code}</span>
                    </div>
                    <div className={styles.legendValues}>
                      <span className={styles.legendWeight}>{stock.currentPercentage.toFixed(1)}%</span>
                      <span className={`${styles.legendReturn} ${
                        stock.profitLossRate >= 0 ? styles.positive : styles.negative
                      }`}>
                        {stock.profitLossRate >= 0 ? '+' : ''}{stock.profitLossRate.toFixed(1)}%
                      </span>
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
                  <span className={styles.currentPrice}>{selectedStock.currentPrice.toLocaleString()}원</span>
                  <span className={`${styles.returnRate} ${
                    selectedStock.profitLossRate >= 0 ? styles.positive : styles.negative
                  }`}>
                    {selectedStock.profitLossRate >= 0 ? '+' : ''}{selectedStock.profitLossRate.toFixed(1)}%
                  </span>
                </div>
              </div>

              <div className={styles.stockMetrics}>
                <div className={styles.metricRow}>
                  <div className={styles.metric}>
                    <span className={styles.metricLabel}>보유 수량</span>
                    <span className={styles.metricValue}>{selectedStock.quantity}주</span>
                  </div>
                  <div className={styles.metric}>
                    <span className={styles.metricLabel}>평가 금액</span>
                    <span className={styles.metricValue}>{selectedStock.totalValue.toLocaleString()}원</span>
                  </div>
                </div>
                
              </div>

            {/* 현재 비중 */}
            <div className={styles.targetInfo}>
              <div className={styles.targetHeader}>
                <span className={styles.targetLabel}>현재 비중</span>
                <span className={styles.targetValue}>{selectedStock.currentPercentage.toFixed(1)}%</span>
              </div>
              <div className={styles.targetBar}>
                <div
                  className={styles.targetFill}
                  style={{
                    width: `${selectedStock.currentPercentage || 0}%`,
                    background: '#3b82f6'
                  }}
                ></div>
              </div>
            </div>

            {/* 목표 비중 */}
            <div className={styles.targetInfo}>
              <div className={styles.targetHeader}>
                <span className={styles.targetLabel}>목표 비중</span>
                {selectedStock.targetPercentage ? (
                  <span className={styles.targetValue}>{selectedStock.targetPercentage.toFixed(1)}%</span>
                ) : (
                  <span className={styles.notSetValue}>아직 설정되지 않았습니다</span>
                )}
              </div>
              <div className={styles.targetBar}>
                <div
                  className={styles.targetFill}
                  style={{
                    width: `${selectedStock.targetPercentage || 0}%`,
                    background: selectedStock.targetPercentage ? '#3b82f6' : '#e2e8f0'
                  }}
                ></div>
              </div>
            </div>

                <div className={styles.weightItem}>
                  <div className={styles.weightHeader}>
                    <span className={styles.weightLabel}>임계값</span>
                    {selectedStock.thresholdPercentage ? (
                      <span className={styles.weightValue}>{selectedStock.thresholdPercentage}%</span>
                    ) : (
                      <span className={styles.notSetValue}>아직 설정되지 않았습니다</span>
                    )}
                  </div>
                  <div className={styles.weightBar}>
                    <div
                      className={styles.weightFill}
                      style={{
                        width: `${selectedStock.thresholdPercentage || 0}%`,
                        backgroundColor: '#ef4444'
                      }}
                    />
                  </div>
                </div>

              {/* 리밸런싱 정보 */}
              <div className={styles.rebalancingInfo}>
                <div className={styles.rebalancingHeader}>
                  <AlertCircle className={styles.rebalancingIcon} />
                  <span>리밸런싱 필요량</span>
                </div>
                <div className={styles.rebalancingValue}>
                  {(selectedStock.currentPercentage - selectedStock.targetPercentage).toFixed(1)}%
                </div>
              </div>
            </motion.div>
          )}
        </div>
      </div>
    </div>
  );
}
