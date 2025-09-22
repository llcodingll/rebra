import { useState, useRef, useEffect } from 'react';
import { motion } from 'motion/react';
import { DollarSign } from 'lucide-react';
import { createChart, ColorType, LineSeries, createSeriesMarkers } from 'lightweight-charts';
import type { IChartApi, ISeriesApi, SeriesMarker } from 'lightweight-charts';
import { generateHistoryMockData, generateRebalancingDates } from '../../mocks/dashboard/historyChart';
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
  eventType?: string;
}

interface DashboardChartProps {
  tradeData: TradeData[];
  portfolioPercents: number[];
  onIdClick?: (id: number) => void;
}

export default function DashboardChart({
  tradeData,
  portfolioPercents,
  onIdClick
}: DashboardChartProps) {
  const [hoveredPoint, setHoveredPoint] = useState<TooltipData | null>(null);
  const chartRef = useRef<HTMLDivElement>(null);
  const chartApiRef = useRef<IChartApi | null>(null);
  const portfolioSeriesRef = useRef<ISeriesApi<'Line'> | null>(null);


  useEffect(() => {
    if (!chartRef.current) return;

    // 목데이터 강제 사용 (테스트용)
    const mockData = generateHistoryMockData();
    const actualTradeData = mockData.tradeData;
    const actualPortfolioPercents = mockData.portfolioPercents;

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

    chartApiRef.current = chart;

    const portfolioSeries = chart.addSeries(LineSeries, {
      color: '#2563eb',
      lineWidth: 3,
    });

    portfolioSeriesRef.current = portfolioSeries;

    const portfolioData = actualPortfolioPercents.map((value, index) => ({
      time: actualTradeData[index].date as any,
      value: value,
    }));

    portfolioSeries.setData(portfolioData);

    // 리밸런싱 날짜 가져오기
    const rebalancingDates = generateRebalancingDates();

    // createSeriesMarkers API를 사용한 리밸런싱 마커 추가
    console.log('리밸런싱 날짜들:', rebalancingDates);
    console.log('실제 거래 데이터:', actualTradeData.map(d => d.date));

    if (rebalancingDates.length > 0) {
      const markers: SeriesMarker[] = rebalancingDates.map(date => {
        const dataIndex = actualTradeData.findIndex(d => d.date === date);
        console.log(`날짜 ${date}의 데이터 인덱스:`, dataIndex);

        if (dataIndex >= 0) {
          return {
            time: date,
            position: 'aboveBar' as const,
            color: '#2563eb',
            shape: 'arrowDown' as const,
            text: '📊 히스토리',
          };
        }
        return null;
      }).filter(marker => marker !== null) as SeriesMarker[];

      console.log('생성된 마커들:', markers);

      if (markers.length > 0) {
        try {
          createSeriesMarkers(portfolioSeries, markers);
          console.log('마커 생성 성공!');
        } catch (error) {
          console.log('마커 생성 실패:', error);
        }
      }
    }

    chart.timeScale().fitContent();

    chart.subscribeCrosshairMove((param: any) => {
      if (!param.time || !param.point) {
        setHoveredPoint(null);
        return;
      }

      const paramTime = param.time;
      let matchingDataIndex = -1;

      for (let i = 0; i < portfolioData.length; i++) {
        if (portfolioData[i].time === paramTime) {
          matchingDataIndex = i;
          break;
        }
      }

      if (matchingDataIndex >= 0 && matchingDataIndex < actualTradeData.length) {
        const data = actualTradeData[matchingDataIndex];

        // 이벤트 체크 (리밸런싱만)
        let eventType = '';
        if (rebalancingDates.includes(paramTime)) {
          eventType = '리밸런싱';
        }

        setHoveredPoint({
          x: param.point.x,
          y: param.point.y,
          id: data.id,
          date: data.date,
          cumulativeReturn: data.cumulativeReturn,
          eventType: eventType // 이벤트 타입 추가
        });
      }
    });

    chart.subscribeClick((param: any) => {
      if (!param.time) return;

      const paramTime = param.time;
      let matchingDataIndex = -1;

      for (let i = 0; i < portfolioData.length; i++) {
        if (portfolioData[i].time === paramTime) {
          matchingDataIndex = i;
          break;
        }
      }

      if (matchingDataIndex >= 0 && matchingDataIndex < actualTradeData.length) {
        const data = actualTradeData[matchingDataIndex];

        // 이벤트 체크 (리밸런싱만)
        let eventType = '';
        if (rebalancingDates.includes(paramTime)) {
          eventType = '리밸런싱';
        }

        if (eventType) {
          // 이벤트 시점 클릭 시 alert
          alert(`${eventType} 이벤트 발생!\n날짜: ${data.date}\n수익률: ${data.cumulativeReturn >= 0 ? '+' : ''}${data.cumulativeReturn.toFixed(2)}%`);
        } else if (onIdClick) {
          // 일반 데이터 포인트 클릭 시 기존 동작
          console.log('Chart clicked ID:', data.id);
          onIdClick(data.id);
        } else {
          // 일반 날짜 클릭 시 정보 표시
          alert(`날짜: ${data.date}\n수익률: ${data.cumulativeReturn >= 0 ? '+' : ''}${data.cumulativeReturn.toFixed(2)}%`);
        }
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
  }, [tradeData, portfolioPercents, onIdClick]);

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
          <span>포트폴리오 수익률</span>
          <span className={styles.legendValue}>+35.2%</span>
        </div>
      </div>

      <div className={styles.chartWrapper} style={{ position: 'relative' }}>
        <div ref={chartRef} style={{ width: '100%', height: '400px' }} />

        {hoveredPoint && (
          <div
            className={styles.tooltip}
            style={{
              position: 'absolute',
              left: hoveredPoint.x + 10,
              top: hoveredPoint.y - 10,
              pointerEvents: 'none',
              zIndex: 1000,
            }}
          >
            <div className={styles.tooltipDate}>{hoveredPoint.date}</div>
            <div className={styles.tooltipContent}>
              <div className={styles.tooltipRow}>
                <DollarSign className={styles.tooltipIcon} />
                <span>수익률: {hoveredPoint.cumulativeReturn >= 0 ? '+' : ''}{hoveredPoint.cumulativeReturn.toFixed(2)}%</span>
              </div>
              {hoveredPoint.eventType && (
                <>
                  <div className={styles.tooltipDivider}></div>
                  <div className={styles.tooltipRow}>
                    <span style={{
                      color: '#2563eb',
                      fontWeight: 'bold'
                    }}>
                      📌 {hoveredPoint.eventType} 시점
                    </span>
                  </div>
                </>
              )}
              <div className={styles.tooltipDivider}></div>
            </div>
          </div>
        )}
      </div>
    </motion.div>
  );
}