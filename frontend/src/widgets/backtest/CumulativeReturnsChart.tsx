import { useState, useRef } from 'react';
import { motion } from 'motion/react';
import { DollarSign } from 'lucide-react';
import styles from '../../pages/backtest/BacktestResultsPage.module.css';

interface TradeData {
  date: string;
  buyAmount: number;
  sellAmount: number;
  portfolioValue: number;
}

interface TooltipData {
  x: number;
  y: number;
  date: string;
  buyAmount: number;
  sellAmount: number;
  portfolioValue: number;
}

interface CumulativeReturnsChartProps {
  tradeData: TradeData[];
  portfolioPercents: number[];
  buyHoldPercents: number[];
  kospiPercents: number[];
  chartMin: number;
  chartMax: number;
  chartRange: number;
  yAxisLabels: string[];
  createPath: (values: number[]) => string;
}

export default function CumulativeReturnsChart({
  tradeData,
  portfolioPercents,
  buyHoldPercents,
  kospiPercents,
  chartMin,
  chartMax,
  chartRange,
  yAxisLabels,
  createPath
}: CumulativeReturnsChartProps) {
  const [hoveredPoint, setHoveredPoint] = useState<TooltipData | null>(null);
  const chartRef = useRef<HTMLDivElement>(null);

  const handleMouseMove = (e: React.MouseEvent, data: TradeData) => {
    if (!chartRef.current) return;
    
    const rect = chartRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    setHoveredPoint({
      x,
      y,
      date: data.date,
      buyAmount: data.buyAmount,
      sellAmount: data.sellAmount,
      portfolioValue: data.portfolioValue
    });
  };

  const handleMouseLeave = () => {
    setHoveredPoint(null);
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6 }}
      className={styles.chartCard}
    >
      <div className={styles.chartHeader}>
        <h2 className={styles.chartTitle}>누적 수익률 비교</h2>
        <div className={styles.legend}>
          <div className={styles.legendItem}>
            <div className={`${styles.legendDot} ${styles.portfolio}`}></div>
            <span>리밸런싱 전략</span>
            <span className={styles.legendValue}>+35.2%</span>
          </div>
          <div className={styles.legendItem}>
            <div className={`${styles.legendDot} ${styles.buyhold}`}></div>
            <span>Buy & Hold</span>
            <span className={styles.legendValue}>+28.9%</span>
          </div>
          <div className={styles.legendItem}>
            <div className={`${styles.legendDot} ${styles.kospi}`}></div>
            <span>KOSPI</span>
            <span className={styles.legendValue}>+22.1%</span>
          </div>
        </div>
      </div>

      <div className={styles.chartWrapper} ref={chartRef}>
        {/* Y축 라벨 */}
        <div className={styles.yAxis}>
          {yAxisLabels.map((label, index) => (
            <div key={index} className={styles.yLabel}>{label}</div>
          ))}
        </div>

        {/* 차트 영역 */}
        <div className={styles.chartArea}>
          {/* 그리드 라인 */}
          <div className={styles.gridLines}>
            {[0, 1, 2, 3, 4].map(i => (
              <div key={i} className={styles.gridLine}></div>
            ))}
          </div>

          {/* SVG 차트 */}
          <svg className={styles.chartSvg} viewBox="0 0 800 400">
            {/* 포트폴리오 라인 */}
            <path
              d={createPath(portfolioPercents)}
              stroke="#2563eb"
              strokeWidth="3"
              fill="none"
              className={styles.chartLine}
            />
            
            {/* Buy & Hold 라인 */}
            <path
              d={createPath(buyHoldPercents)}
              stroke="#10b981"
              strokeWidth="2"
              fill="none"
              className={styles.chartLine}
            />
            
            {/* KOSPI 라인 */}
            <path
              d={createPath(kospiPercents)}
              stroke="#6b7280"
              strokeWidth="2"
              fill="none"
              className={styles.chartLine}
            />

            {/* 인터랙티브 포인트 */}
            {tradeData.map((data, index) => {
              const x = (index / (tradeData.length - 1)) * 720 + 40;
              const y = 360 - ((portfolioPercents[index] - chartMin) / chartRange) * 320;
              
              return (
                <circle
                  key={index}
                  cx={x}
                  cy={y}
                  r="6"
                  fill="#ffffff"
                  stroke="#2563eb"
                  strokeWidth="2"
                  className={styles.chartPoint}
                  onMouseMove={(e) => handleMouseMove(e as any, data)}
                  onMouseLeave={handleMouseLeave}
                />
              );
            })}
          </svg>

          {/* X축 라벨 */}
          <div className={styles.xAxis}>
            {['2023-01', '2023-03', '2023-05', '2023-07', '2023-09', '2023-12'].map((month, index) => (
              <div key={index} className={styles.xLabel}>{month}</div>
            ))}
          </div>
        </div>

        {/* 툴팁 */}
        {hoveredPoint && (
          <div 
            className={styles.tooltip}
            style={{
              left: hoveredPoint.x + 10,
              top: hoveredPoint.y - 10,
            }}
          >
            <div className={styles.tooltipDate}>{hoveredPoint.date}</div>
            <div className={styles.tooltipContent}>
              <div className={styles.tooltipRow}>
                <DollarSign className={styles.tooltipIcon} />
                <span>매수액: +{hoveredPoint.buyAmount.toLocaleString()}원</span>
              </div>
              <div className={styles.tooltipRow}>
                <DollarSign className={styles.tooltipIcon} />
                <span>매도액: -{hoveredPoint.sellAmount.toLocaleString()}원</span>
              </div>
              <div className={styles.tooltipDivider}></div>
              <div className={styles.tooltipRow}>
                <span className={styles.tooltipValue}>
                  포트폴리오 가치: {hoveredPoint.portfolioValue.toLocaleString()}원
                </span>
              </div>
            </div>
          </div>
        )}
      </div>
    </motion.div>
  );
}