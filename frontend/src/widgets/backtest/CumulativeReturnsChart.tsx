import { useState, useRef, useEffect } from 'react';
import { motion } from 'motion/react';
import { DollarSign } from 'lucide-react';
import { createChart, ColorType, LineSeries } from 'lightweight-charts';
import type { IChartApi, ISeriesApi, UTCTimestamp } from 'lightweight-charts';
import styles from './CumulativeReturnsChart.module.css';

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
}

export default function CumulativeReturnsChart({
  tradeData,
  portfolioPercents,
  buyHoldPercents,
  kospiPercents,
  chartMin,
  chartMax,
  chartRange,
  yAxisLabels
}: CumulativeReturnsChartProps) {
  const [hoveredPoint, setHoveredPoint] = useState<TooltipData | null>(null);
  const chartRef = useRef<HTMLDivElement>(null);
  const chartInstanceRef = useRef<IChartApi | null>(null);
  const portfolioSeriesRef = useRef<ISeriesApi<'Line'> | null>(null);
  const buyHoldSeriesRef = useRef<ISeriesApi<'Line'> | null>(null);
  const kospiSeriesRef = useRef<ISeriesApi<'Line'> | null>(null);


  useEffect(() => {
    if (!chartRef.current) return;

    try {
      const chart = createChart(chartRef.current, {
        width: chartRef.current.clientWidth || 800,
        height: 400,
        layout: {
          background: { type: ColorType.Solid, color: 'transparent' },
          textColor: '#374151',
          fontSize: 12,
          attributionLogo: false,
        },
        grid: {
          vertLines: { color: '#f3f4f6' },
          horzLines: { color: '#f3f4f6' },
        },
        rightPriceScale: {
          borderColor: '#e5e7eb',
          visible: false,
        },
        leftPriceScale: {
          borderColor: '#e5e7eb',
          visible: true,
        },
        timeScale: {
          borderColor: '#e5e7eb',
          timeVisible: true,
          secondsVisible: false,
        },
        crosshair: {
          mode: 0,
          vertLine: {
            color: '#6b7280',
            width: 1,
            style: 2,
          },
          horzLine: {
            color: '#6b7280',
            width: 1,
            style: 2,
          },
        },
      });

      chartInstanceRef.current = chart;

      // Use v5 API with LineSeries
      const portfolioSeries = chart.addSeries(LineSeries, {
        color: '#2563eb',
        lineWidth: 3,
      });

      const buyHoldSeries = chart.addSeries(LineSeries, {
        color: '#10b981',
        lineWidth: 2,
      });

      const kospiSeries = chart.addSeries(LineSeries, {
        color: '#6b7280',
        lineWidth: 2,
      });

      portfolioSeriesRef.current = portfolioSeries;
      buyHoldSeriesRef.current = buyHoldSeries;
      kospiSeriesRef.current = kospiSeries;

      const formatDataForLightweight = (values: number[], dates: string[]) => {
        return values.map((value, index) => {
          // dates는 이제 "2023-01-01" 형식이므로 그대로 사용
          const dateStr = dates[index];

          return {
            time: dateStr as any,
            value: value,
          };
        });
      };

      const dates = tradeData.map(data => data.date);
      const portfolioData = formatDataForLightweight(portfolioPercents, dates);
      const buyHoldData = formatDataForLightweight(buyHoldPercents, dates);
      const kospiData = formatDataForLightweight(kospiPercents, dates);

      portfolioSeries.setData(portfolioData);
      buyHoldSeries.setData(buyHoldData);
      kospiSeries.setData(kospiData);

      // 전체 데이터가 보이도록 시간 범위 설정
      chart.timeScale().fitContent();

      chart.subscribeCrosshairMove((param: any) => {
        if (!param.time || !param.point) {
          setHoveredPoint(null);
          return;
        }

        // Convert time back to original format for comparison
        const paramTime = param.time;
        let matchingDataIndex = -1;

        // Find matching data by comparing formatted dates
        for (let i = 0; i < portfolioData.length; i++) {
          if (portfolioData[i].time === paramTime) {
            matchingDataIndex = i;
            break;
          }
        }

        if (matchingDataIndex >= 0 && matchingDataIndex < tradeData.length) {
          const data = tradeData[matchingDataIndex];

          setHoveredPoint({
            x: param.point.x,
            y: param.point.y,
            date: data.date,
            buyAmount: data.buyAmount,
            sellAmount: data.sellAmount,
            portfolioValue: data.portfolioValue,
          });
        }
      });

      const handleResize = () => {
        if (chartRef.current && chart) {
          chart.applyOptions({
            width: chartRef.current.clientWidth,
          });
        }
      };

      window.addEventListener('resize', handleResize);

      return () => {
        window.removeEventListener('resize', handleResize);
        chart.remove();
      };
    } catch (error) {
      console.error('Error creating chart:', error);
    }
  }, [tradeData, portfolioPercents, buyHoldPercents, kospiPercents]);

  useEffect(() => {
    if (!chartInstanceRef.current) return;

    const handleResize = () => {
      if (chartRef.current && chartInstanceRef.current) {
        chartInstanceRef.current.applyOptions({
          width: chartRef.current.clientWidth,
        });
      }
    };

    const resizeObserver = new ResizeObserver(handleResize);
    if (chartRef.current) {
      resizeObserver.observe(chartRef.current);
    }

    return () => {
      resizeObserver.disconnect();
    };
  }, []);

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

      <div className={styles.chartWrapper}>
        {/* Lightweight Charts 컨테이너 */}
        <div className={styles.chartContainer} ref={chartRef}></div>

        {/* 툴팁 */}
        {hoveredPoint && (
          <div
            className={styles.tooltip}
            style={{
              left: hoveredPoint.x - 250,
              top: hoveredPoint.y - 120,
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