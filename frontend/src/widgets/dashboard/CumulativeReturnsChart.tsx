import { useState, useRef, useEffect } from 'react';
import { motion } from 'motion/react';
import { DollarSign } from 'lucide-react';
import { createChart, ColorType, LineSeries } from 'lightweight-charts';
import type {
  IChartApi,
  ISeriesApi,
  UTCTimestamp,
  ISeriesPrimitive,
  IPrimitivePaneView,
  IPrimitivePaneRenderer,
  Coordinate,
  Time
} from 'lightweight-charts';
import { useApi } from '../../shared/hook/useApi';
import { portfolioApi } from '../../features/portfolio/api/portfolioApi';
import { generatePerformanceMockData } from '../../mocks/dashboard/performanceData';
import type { PerformanceDataPoint } from '../../features/portfolio/api/types';
import styles from './CumulativeReturnsChart.module.css';

interface TooltipData {
  x: number;
  y: number;
  date: string;
  totalValue: number;
  eventTypes: string[];
}

// 세로선 옵션 인터페이스
interface VertLineOptions {
  color: string;
  width: number;
}

// 세로선 렌더러
class VertLinePaneRenderer implements IPrimitivePaneRenderer {
  _x: Coordinate | null = null;
  _options: VertLineOptions;

  constructor(x: Coordinate | null, options: VertLineOptions) {
    this._x = x;
    this._options = options;
  }

  draw(target: any) {
    if (this._x === null) return;

    target.useBitmapCoordinateSpace((scope: any) => {
      const ctx = scope.context;
      const x = Math.round(this._x! * scope.horizontalPixelRatio);

      ctx.save();
      ctx.strokeStyle = this._options.color;
      ctx.lineWidth = this._options.width * scope.horizontalPixelRatio;
      ctx.setLineDash([2 * scope.horizontalPixelRatio, 2 * scope.horizontalPixelRatio]);

      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, scope.bitmapSize.height);
      ctx.stroke();

      ctx.restore();
    });
  }
}

// 세로선 뷰
class VertLinePaneView implements IPrimitivePaneView {
  _source: VertLine;
  _x: Coordinate | null = null;
  _options: VertLineOptions;

  constructor(source: VertLine, options: VertLineOptions) {
    this._source = source;
    this._options = options;
  }

  update() {
    const timeScale = this._source._chart.timeScale();
    this._x = timeScale.timeToCoordinate(this._source._time);
  }

  renderer() {
    return new VertLinePaneRenderer(this._x, this._options);
  }
}

// 메인 세로선 클래스
class VertLine implements ISeriesPrimitive<Time> {
  _chart: IChartApi;
  _series: ISeriesApi<any>;
  _time: Time;
  _paneViews: VertLinePaneView[];
  _options: VertLineOptions;

  constructor(chart: IChartApi, series: ISeriesApi<any>, time: Time, options: VertLineOptions) {
    this._chart = chart;
    this._series = series;
    this._time = time;
    this._options = options;
    this._paneViews = [new VertLinePaneView(this, options)];
  }

  updateAllViews() {
    this._paneViews.forEach(pw => pw.update());
  }

  paneViews() {
    return this._paneViews;
  }

  priceAxisViews() {
    return [];
  }

  timeAxisViews() {
    return [];
  }

  hitTest() {
    return null;
  }
}

interface CumulativeReturnsChartProps {
  portfolioId?: number;
  onDateClick?: (date: string) => void;
}

export default function CumulativeReturnsChart({
  portfolioId,
  onDateClick
}: CumulativeReturnsChartProps) {
  const [hoveredPoint, setHoveredPoint] = useState<TooltipData | null>(null);
  const chartRef = useRef<HTMLDivElement>(null);
  const chartApiRef = useRef<IChartApi | null>(null);
  const portfolioSeriesRef = useRef<ISeriesApi<'Line'> | null>(null);

  // API로 성과 메트릭 데이터 조회
  const { data: performanceData, isLoading, error } = useApi({
    queryKey: ['portfolio-performance', portfolioId],
    apiFunction: () => portfolioId ? portfolioApi.getPerformanceMetrics(portfolioId) : Promise.reject('No portfolio ID'),
    enabled: !!portfolioId,
  });

  console.log('performanceData:', performanceData);
  console.log('portfolioId:', portfolioId);
  console.log('isLoading:', isLoading);
  console.log('error:', error);

  // 목데이터 사용 (임시) - useState로 한번만 생성
  // const [performanceData] = useState(() => generatePerformanceMockData());
  // const isLoading = false;
  // const error = null;

  useEffect(() => {
    if (!chartRef.current || !performanceData?.performanceData) return;

    const chartData = performanceData.performanceData;

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

    // 평가액 기준으로 차트 데이터 생성
    const lineData = chartData
      .map((dataPoint, index) => ({
        time: dataPoint.metricDate as any,
        value: dataPoint.totalValue,
        originalIndex: index
      }))
      // 시간순으로 정렬
      .sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime())
      // 중복된 시간값 제거 (같은 날짜가 여러개면 마지막 값만 사용)
      .reduce((acc, current) => {
        const existing = acc.find(item => item.time === current.time);
        if (existing) {
          // 같은 시간이 있으면 더 큰 originalIndex를 가진 것으로 교체
          if (current.originalIndex > existing.originalIndex) {
            const index = acc.indexOf(existing);
            acc[index] = current;
          }
        } else {
          acc.push(current);
        }
        return acc;
      }, [] as any[])
      .slice(0, -1) // 마지막 데이터 포인트 제거 (오늘 날짜 제외)
      .map(({ originalIndex, ...item }) => item); // originalIndex 제거

    console.log('lineData:', lineData);
    portfolioSeries.setData(lineData);

    // 이벤트가 있는 날짜에 세로선 추가
    chartData.forEach((dataPoint) => {
      const hasEvent = dataPoint.compositionChanged || dataPoint.rebalanced || dataPoint.sold || dataPoint.bought;

      if (hasEvent) {
        // 이벤트 타입에 따른 색상 결정
        let color = '#3b82f6'; // 기본 파란색

        // 구성 변경인 경우만 빨간색, 나머지는 모두 파란색
        if (dataPoint.compositionChanged) {
          color = '#ef4444'; // 빨간색 - 구성 변경
        }

        const vertLine = new VertLine(chart, portfolioSeries, dataPoint.metricDate as any, {
          color: color,
          width: 2
        });

        portfolioSeries.attachPrimitive(vertLine);
      }
    });

    chart.timeScale().fitContent();

    chart.subscribeCrosshairMove((param: any) => {
      if (!param.time || !param.point) {
        setHoveredPoint(null);
        return;
      }

      const paramTime = param.time;
      const matchingDataPoint = chartData.find(d => d.metricDate === paramTime);

      if (matchingDataPoint) {
        // 이벤트 타입들 확인
        const eventTypes: string[] = [];
        if (matchingDataPoint.compositionChanged) eventTypes.push('구성 변경');
        if (matchingDataPoint.rebalanced) eventTypes.push('리밸런싱');
        if (matchingDataPoint.sold) eventTypes.push('매도');
        if (matchingDataPoint.bought) eventTypes.push('매수');

        console.log('Hover 데이터:', {
          date: matchingDataPoint.metricDate,
          compositionChanged: matchingDataPoint.compositionChanged,
          rebalanced: matchingDataPoint.rebalanced,
          sold: matchingDataPoint.sold,
          bought: matchingDataPoint.bought,
          eventTypes: eventTypes
        });

        setHoveredPoint({
          x: param.point.x,
          y: param.point.y,
          date: matchingDataPoint.metricDate,
          totalValue: matchingDataPoint.totalValue,
          eventTypes: eventTypes
        });
      }
    });

    chart.subscribeClick((param: any) => {
      if (!param.time) return;

      const paramTime = param.time;
      const matchingDataPoint = chartData.find(d => d.metricDate === paramTime);

      if (matchingDataPoint) {
        // 이벤트 타입들 확인
        const eventTypes: string[] = [];
        if (matchingDataPoint.compositionChanged) eventTypes.push('구성 변경');
        if (matchingDataPoint.rebalanced) eventTypes.push('리밸런싱');
        if (matchingDataPoint.sold) eventTypes.push('매도');
        if (matchingDataPoint.bought) eventTypes.push('매수');

        if (eventTypes.length > 0) {
          // 매수, 매도, 리밸런싱이 있는 경우 거래 내역 조회 (구성 변경 여부와 상관없이)
          const hasTradeEvents = matchingDataPoint.rebalanced || matchingDataPoint.sold || matchingDataPoint.bought;
          if (hasTradeEvents && onDateClick) {
            onDateClick(matchingDataPoint.metricDate);
          }
        }
      }
    });
    // 크기 조절
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
  }, [performanceData]);

  if (isLoading) {
    return (
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className={styles.chartCard}
      >
        <div className={styles.chartHeader}>
          <h2 className={styles.chartTitle}>포트폴리오 평가액 히스토리</h2>
        </div>
        <div style={{ padding: '100px', textAlign: 'center' }}>
          데이터를 불러오는 중...
        </div>
      </motion.div>
    );
  }

  if (error || !performanceData) {
    return (
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className={styles.chartCard}
      >
        <div className={styles.chartHeader}>
          <h2 className={styles.chartTitle}>포트폴리오 평가액 히스토리</h2>
        </div>
        <div style={{ padding: '100px', textAlign: 'center' }}>
          데이터를 불러올 수 없습니다.
        </div>
      </motion.div>
    );
  }

  const portfolioData = performanceData;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6 }}
      className={styles.chartCard}
    >
      <div className={styles.chartHeader}>
        <h2 className={styles.chartTitle}>포트폴리오 평가액 히스토리</h2>
      </div>

      <div className={styles.legend}>
        <div className={styles.legendItem}>
          <div className={`${styles.legendDot} ${styles.portfolio}`}></div>
          <span>{portfolioData.portfolioName}</span>
          <span className={styles.legendValue}>
            {portfolioData.statistics.finalValue.toLocaleString()}원
          </span>
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
                <span>평가액: {hoveredPoint.totalValue.toLocaleString()}원</span>
              </div>
              {hoveredPoint.eventTypes.length > 0 && (
                <>
                  <div className={styles.tooltipDivider}></div>
                  <div className={styles.tooltipRow} style={{ flexDirection: 'column', alignItems: 'flex-start' }}>
                    {hoveredPoint.eventTypes.includes('구성 변경') && (
                      <span style={{
                        color: '#ef4444',
                        fontWeight: 'bold',
                        marginBottom: '4px'
                      }}>
                        ⚠️ 구성 변경에 따른 평가액 변경이 포함되어 있습니다.
                      </span>
                    )}
                    {hoveredPoint.eventTypes.filter(type => type !== '구성 변경').length > 0 && (
                      <span style={{
                        color: '#2563eb',
                        fontWeight: 'bold'
                      }}>
                        📌 {hoveredPoint.eventTypes.filter(type => type !== '구성 변경').join(', ')}
                      </span>
                    )}
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