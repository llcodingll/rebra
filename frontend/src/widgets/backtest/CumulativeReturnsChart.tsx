import { useState, useRef, useEffect } from 'react';
import { motion } from 'motion/react';
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
import styles from './CumulativeReturnsChart.module.css';

interface TradeData {
  date: string;
  buyAmount: number;
  sellAmount: number;
  portfolioValue: number;
  cashBalance: number;
  dailyBorrowingInterest: number;
  buyHoldValue: number;
}

interface TooltipData {
  x: number;
  y: number;
  date: string;
  buyAmount: number;
  sellAmount: number;
  portfolioValue: number;
  cashBalance: number;
  dailyBorrowingInterest: number;
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
      ctx.setLineDash([2 * scope.horizontalPixelRatio, 2 * scope.horizontalPixelRatio]); // 촘촘한 점선 패턴

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
  tradeData: TradeData[];
  portfolioPercents: number[];
  buyHoldPercents: number[];
  kospiPercents: number[];
  chartMin: number;
  chartMax: number;
  chartRange: number;
  yAxisLabels: string[];
  portfolioFinalReturn?: number;
  buyHoldFinalReturn?: number;
  kospiFinalReturn?: number;
  rebalancingDates?: string[];
  showKospi?: boolean;
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
  portfolioFinalReturn,
  buyHoldFinalReturn,
  kospiFinalReturn,
  rebalancingDates = [],
  showKospi = true
}: CumulativeReturnsChartProps) {
  const [hoveredPoint, setHoveredPoint] = useState<TooltipData | null>(null);

  // 억 단위 이상 간소화 포맷팅
  const formatCompactPrice = (price: number): string => {
    if (price >= 100000000) { // 1억 이상
      const eok = price / 100000000;
      return `${eok.toFixed(1)}억원`;
    }
    return `${price.toLocaleString()}원`;
  };
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
        height: 700,
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

      const portfolioSeries = chart.addSeries(LineSeries, {
        color: '#2563eb',
        lineWidth: 3,
      });

      const buyHoldSeries = chart.addSeries(LineSeries, {
        color: '#10b981',
        lineWidth: 2,
      });

      const kospiSeries = showKospi ? chart.addSeries(LineSeries, {
        color: '#6b7280',
        lineWidth: 2,
      }) : null;

      portfolioSeriesRef.current = portfolioSeries;
      buyHoldSeriesRef.current = buyHoldSeries;
      kospiSeriesRef.current = kospiSeries;

      const formatDataForLightweight = (values: number[], dates: string[]) => {
        return values.map((value, index) => {
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
      if (showKospi && kospiSeries) {
        kospiSeries.setData(kospiData);
      }

      // 리밸런싱 세로선 추가
      if (rebalancingDates.length > 0) {
        rebalancingDates.forEach(date => {
          const vertLine = new VertLine(chart, portfolioSeries, date as Time, {
            color: '#ef4444',
            width: 0.5
          });
          portfolioSeries.attachPrimitive(vertLine);
        });
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

        if (matchingDataIndex >= 0 && matchingDataIndex < tradeData.length) {
          const data = tradeData[matchingDataIndex];

          setHoveredPoint({
            x: param.point.x,
            y: param.point.y,
            date: data.date,
            buyAmount: data.buyAmount,
            sellAmount: data.sellAmount,
            portfolioValue: data.portfolioValue,
            cashBalance: data.cashBalance,
            dailyBorrowingInterest: data.dailyBorrowingInterest,
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
      // Chart creation failed
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
            <span className={`${styles.legendValue} ${portfolioFinalReturn && portfolioFinalReturn > 0 ? styles.positive : portfolioFinalReturn && portfolioFinalReturn < 0 ? styles.negative : ''}`}>
              {portfolioFinalReturn !== undefined
                ? `${portfolioFinalReturn >= 0 ? '+' : ''}${portfolioFinalReturn.toFixed(1)}%`
                : '+0.0%'
              }
            </span>
          </div>
          <div className={styles.legendItem}>
            <div className={`${styles.legendDot} ${styles.buyhold}`}></div>
            <span>Buy & Hold</span>
            <span className={`${styles.legendValue} ${buyHoldFinalReturn && buyHoldFinalReturn > 0 ? styles.positive : buyHoldFinalReturn && buyHoldFinalReturn < 0 ? styles.negative : ''}`}>
              {buyHoldFinalReturn !== undefined
                ? `${buyHoldFinalReturn >= 0 ? '+' : ''}${buyHoldFinalReturn.toFixed(1)}%`
                : '+0.0%'
              }
            </span>
          </div>
          {showKospi && (
            <div className={styles.legendItem}>
              <div className={`${styles.legendDot} ${styles.kospi}`}></div>
              <span>KOSPI</span>
              <span className={`${styles.legendValue} ${kospiFinalReturn && kospiFinalReturn > 0 ? styles.positive : kospiFinalReturn && kospiFinalReturn < 0 ? styles.negative : ''}`}>
                {kospiFinalReturn !== undefined
                  ? `${kospiFinalReturn >= 0 ? '+' : ''}${kospiFinalReturn.toFixed(1)}%`
                  : '+0.0%'
                }
              </span>
            </div>
          )}
          {rebalancingDates && rebalancingDates.length > 0 && (
            <div className={styles.legendItem}>
              <div style={{
                width: '12px',
                height: '2px',
                backgroundColor: '#ef4444',
                borderStyle: 'dashed',
                borderWidth: '1px 0',
                borderColor: '#ef4444'
              }}></div>
              <span>리밸런싱 실행일</span>
            </div>
          )}
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
              top: hoveredPoint.y - 160,
            }}
          >
            <div className={styles.tooltipDate}>{hoveredPoint.date}</div>
            <div className={styles.tooltipContent}>
              <div className={styles.tooltipRow}>
                <span>매수액: {formatCompactPrice(hoveredPoint.buyAmount)}</span>
              </div>
              <div className={styles.tooltipRow}>
                <span>매도액: {formatCompactPrice(hoveredPoint.sellAmount)}</span>
              </div>
              <div className={styles.tooltipDivider}></div>
              <div className={styles.tooltipRow}>
                <span className={styles.tooltipValue}>
                  포트폴리오 가치: {formatCompactPrice(hoveredPoint.portfolioValue)}
                </span>
              </div>
              <div className={styles.tooltipRow}>
                <span className={styles.tooltipValue}>
                  현금 잔고: {formatCompactPrice(hoveredPoint.cashBalance)}
                </span>
              </div>
              <div className={styles.tooltipRow}>
                <span className={styles.tooltipValue}>
                  일일 대출 이자: {formatCompactPrice(hoveredPoint.dailyBorrowingInterest)}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>
    </motion.div>
  );
}