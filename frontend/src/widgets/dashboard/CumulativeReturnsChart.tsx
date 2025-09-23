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
}

export default function CumulativeReturnsChart({
  portfolioId
}: CumulativeReturnsChartProps) {
  const [hoveredPoint, setHoveredPoint] = useState<TooltipData | null>(null);
  const chartRef = useRef<HTMLDivElement>(null);
  const chartApiRef = useRef<IChartApi | null>(null);
  const portfolioSeriesRef = useRef<ISeriesApi<'Line'> | null>(null);

  // API로 성과 메트릭 데이터 조회 (주석 처리 - 추후 사용 예정)
  // const { data: performanceData, isLoading, error } = useApi({
  //   queryKey: ['portfolio-performance', portfolioId],
  //   apiFunction: () => portfolioId ? portfolioApi.getPerformanceMetrics(portfolioId) : Promise.reject('No portfolio ID'),
  //   enabled: !!portfolioId,
  // });

  // 목데이터 사용 (임시) - useState로 한번만 생성
  const [performanceData] = useState(() => generatePerformanceMockData());
  const isLoading = false;
  const error = null;

  useEffect(() => {
    if (!chartRef.current || !performanceData?.data?.performanceData) return;

    const chartData = performanceData.data.performanceData;

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
    const lineData = chartData.map((dataPoint) => ({
      time: dataPoint.metricDate as any,
      value: dataPoint.totalValue,
    }));

    portfolioSeries.setData(lineData);

    // 이벤트가 있는 날짜에 세로선 추가
    chartData.forEach((dataPoint) => {
      const hasEvent = dataPoint.compositionChanged || dataPoint.rebalanced || dataPoint.sold || dataPoint.bought;

      if (hasEvent) {
        // 이벤트 타입에 따른 색상 결정
        let color = '#6b7280'; // 기본 회색
        if (dataPoint.rebalanced) color = '#ef4444'; // 빨간색 - 리밸런싱
        else if (dataPoint.sold) color = '#f59e0b'; // 주황색 - 매도
        else if (dataPoint.bought) color = '#10b981'; // 초록색 - 매수
        else if (dataPoint.compositionChanged) color = '#3b82f6'; // 파란색 - 구성 변경

        const vertLine = new VertLine(chart, portfolioSeries, dataPoint.metricDate as any, {
          color: color,
          width: 1
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
          // 이벤트 시점 클릭 시 alert
          alert(`이벤트 발생!\n날짜: ${matchingDataPoint.metricDate}\n평가액: ${matchingDataPoint.totalValue.toLocaleString()}원\n이벤트: ${eventTypes.join(', ')}`);
        } else {
          // 일반 날짜 클릭 시 정보 표시
          alert(`날짜: ${matchingDataPoint.metricDate}\n평가액: ${matchingDataPoint.totalValue.toLocaleString()}원`);
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

  if (error || !performanceData?.data) {
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

  const portfolioData = performanceData.data;

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
                  <div className={styles.tooltipRow}>
                    <span style={{
                      color: '#2563eb',
                      fontWeight: 'bold'
                    }}>
                      📌 {hoveredPoint.eventTypes.join(', ')}
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