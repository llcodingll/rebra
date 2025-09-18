import { useState, useRef } from 'react';
import { motion } from 'motion/react';
import { DollarSign } from 'lucide-react';
import styles from './CumulativeReturnsChart.module.css';

interface TradeData {
  id: number;
  date: string;
  cumulativeReturn: number;
}

interface TooltipData {
  x: number;
  y: number;
  id: number;
  date: string;
  cumulativeReturn: number;
}

interface DashboardChartProps {
  tradeData: TradeData[];
  portfolioPercents: number[];
  kospiPercents: number[];
  chartMin: number;
  chartMax: number;
  chartRange: number;
  yAxisLabels: string[];
  createPath: (values: number[]) => string;
  onIdClick?: (id: number) => void;
}

export default function DashboardChart({
  tradeData,
  portfolioPercents,
  kospiPercents,
  chartMin,
  chartMax,
  chartRange,
  yAxisLabels,
  createPath,
  onIdClick
}: DashboardChartProps) {
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
      id: data.id,
      date: data.date,
      cumulativeReturn: data.cumulativeReturn
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
        <h2 className={styles.chartTitle}>히스토리 시점 등록 주식 수익률(변경 예정)</h2>
      </div>
      
      <div className={styles.legend}>
        <div className={styles.legendItem}>
          <div className={`${styles.legendDot} ${styles.portfolio}`}></div>
          <span>리밸런싱 전략</span>
          <span className={styles.legendValue}>+35.2%</span>
        </div>
        <div className={styles.legendItem}>
          <div className={`${styles.legendDot} ${styles.kospi}`}></div>
          <span>KOSPI</span>
          <span className={styles.legendValue}>+22.1%</span>
        </div>
      </div>

      <div className={styles.chartWrapper} ref={chartRef}>
        <div className={styles.yAxis}>
          {yAxisLabels.map((label, index) => (
            <div key={index} className={styles.yLabel}>{label}</div>
          ))}
        </div>

        <div className={styles.chartArea}>
          <div className={styles.gridLines}>
            {[0, 1, 2, 3, 4].map(i => (
              <div key={i} className={styles.gridLine}></div>
            ))}
          </div>

          <svg className={styles.chartSvg} viewBox="0 0 800 400">
            <path
              d={createPath(portfolioPercents)}
              stroke="#2563eb"
              strokeWidth="3"
              fill="none"
              className={styles.chartLine}
            />
            
            <path
              d={createPath(kospiPercents)}
              stroke="#6b7280"
              strokeWidth="2"
              fill="none"
              className={styles.chartLine}
            />

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
                  onClick={() => {
                    if (onIdClick) {
                      console.log('Chart clicked ID:', data.id);
                      onIdClick(data.id);
                    }
                  }}
                />
              );
            })}
          </svg>

          <div className={styles.xAxis}>
            {['2023-01', '2023-03', '2023-05', '2023-07', '2023-09', '2023-12'].map((month, index) => (
              <div key={index} className={styles.xLabel}>{month}</div>
            ))}
          </div>
        </div>

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
                <span>수익률: {hoveredPoint.cumulativeReturn >= 0 ? '+' : ''}{hoveredPoint.cumulativeReturn.toFixed(2)}%</span>
              </div>
              <div className={styles.tooltipDivider}></div>
            </div>
          </div>
        )}
      </div>
    </motion.div>
  );
}