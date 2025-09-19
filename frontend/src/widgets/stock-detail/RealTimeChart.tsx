import { useEffect, useRef, useState } from 'react';
import { createChart, ColorType, CandlestickSeries, HistogramSeries, CrosshairMode } from 'lightweight-charts';
import type { IChartApi, ISeriesApi, UTCTimestamp } from 'lightweight-charts';
import type { RealtimePriceMessage } from '../../features/stock-detail/api/types';
import { useStockChartData } from '../../features/stock-detail/hooks/useStockChartData';
import type { ChartPeriodType } from '../../features/stock-detail/utils/dateUtils';
import { transformChartData } from '../../features/stock-detail/utils/chartDataTransform';
import styles from './RealTimeChart.module.css';

interface RealTimeChartProps {
  stockCode: string;
  stockName: string;
  realtimeData?: RealtimePriceMessage | null;
  onPriceUpdate?: (price: number, change: { amount: number; rate: number }) => void;
}

interface CandleData {
  time: UTCTimestamp;
  open: number;
  high: number;
  low: number;
  close: number;
}

interface VolumeData {
  time: UTCTimestamp;
  value: number;
  color?: string;
}

export default function RealTimeChart({ stockCode, stockName, realtimeData, onPriceUpdate }: RealTimeChartProps) {
  const priceChartContainerRef = useRef<HTMLDivElement>(null);
  const volumeChartContainerRef = useRef<HTMLDivElement>(null);
  const chartContainerRef = useRef<HTMLDivElement>(null);
  const priceChartRef = useRef<IChartApi | null>(null);
  const volumeChartRef = useRef<IChartApi | null>(null);
  const priceSeriesRef = useRef<ISeriesApi<'Candlestick'> | null>(null);
  const volumeSeriesRef = useRef<ISeriesApi<'Histogram'> | null>(null);
  const simulationIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const panSyncingRef = useRef<boolean>(false); // 줌/드래그 동기화용
  const crosshairSyncingRef = useRef<boolean>(false); // 크로스헤어 동기화용
  const resizeObserverRef = useRef<ResizeObserver | null>(null);

  const [selectedPeriod, setSelectedPeriod] = useState<ChartPeriodType>('daily');
  const [currentPrice, setCurrentPrice] = useState<number | null>(null);
  const [priceChange, setPriceChange] = useState<{ amount: number; rate: number } | null>(null);
  const [volume, setVolume] = useState<number | null>(null);
  const [candleData, setCandleData] = useState<CandleData[]>([]);
  const [volumeData, setVolumeData] = useState<VolumeData[]>([]);

  // API 연동 모드 전환 (개발 중 편의를 위한 분기)
  const USE_API_DATA = false; // true: API 데이터 사용, false: 시뮬레이션 데이터 사용

  // API에서 차트 데이터 가져오기
  const { data: chartApiData, isLoading, error } = useStockChartData(stockCode, selectedPeriod);

  // 종목별 초기 가격 설정
  const getInitialPrice = (code: string) => {
    const prices: Record<string, number> = {
      '005930': 71400, // 삼성전자
      '000660': 125000, // SK하이닉스
      '035420': 180000, // NAVER
      '051910': 420000, // LG화학
      '006400': 250000, // 삼성SDI
      '028260': 45000, // 삼성물산
      '012330': 250000, // 현대모비스
      '207940': 850000, // 삼성바이오로직스
    };
    return prices[code] || 50000;
  };

  // 150개 데이터 생성
  const generateHistoricalData = (basePrice: number) => {
    const now = new Date();
    const startTime = new Date();
    startTime.setHours(9, 0, 0, 0);

    const candleData: CandleData[] = [];
    const volumeData: VolumeData[] = [];

    let currentPrice = basePrice;
    const totalCandles = 150;

    for (let i = 0; i < totalCandles; i++) {
      const time = new Date(startTime.getTime() + i * 5 * 60 * 1000);
      const timestamp = Math.floor(time.getTime() / 1000) as UTCTimestamp;

      const open = currentPrice;
      const volatility = 0.02;
      const high = open * (1 + Math.random() * volatility);
      const low = open * (1 - Math.random() * volatility);
      const changePercent = (Math.random() - 0.5) * 0.02;
      const close = Math.max(open * (1 + changePercent), basePrice * 0.8);

      currentPrice = close;

      candleData.push({
        time: timestamp,
        open: Math.round(open),
        high: Math.round(Math.max(open, high, close)),
        low: Math.round(Math.min(open, low, close)),
        close: Math.round(close),
      });

      const volumeValue = Math.round(Math.random() * 1000000 + 100000);
      volumeData.push({
        time: timestamp,
        value: volumeValue,
        color: close >= open ? '#dc2626' : '#2563eb',
      });
    }

    setCandleData(candleData);
    setVolumeData(volumeData);
    setCurrentPrice(Math.round(currentPrice));

    const firstPrice = candleData[0]?.close || basePrice;
    const change = currentPrice - firstPrice;
    const changeRate = (change / firstPrice) * 100;

    setPriceChange({
      amount: Math.round(change),
      rate: Number(changeRate.toFixed(2)),
    });

    if (onPriceUpdate) {
      onPriceUpdate(Math.round(currentPrice), {
        amount: Math.round(change),
        rate: Number(changeRate.toFixed(2)),
      });
    }
  };

  // 실시간 데이터 업데이트
  useEffect(() => {
    if (realtimeData) {
      const price = Number(realtimeData.currentPrice);
      const volume = Number(realtimeData.volume);

      if (price > 0) {
        setCandleData((prev) => {
          if (prev.length === 0) return prev;

          const newData = [...prev];
          const lastCandle = newData[newData.length - 1];

          const updatedCandle: CandleData = {
            ...lastCandle,
            close: price,
            high: Math.max(lastCandle.high, price),
            low: Math.min(lastCandle.low, price),
          };

          newData[newData.length - 1] = updatedCandle;
          return newData;
        });

        setVolumeData((prev) => {
          if (prev.length === 0) return prev;
          const newData = [...prev];
          const lastVolume = newData[newData.length - 1];

          newData[newData.length - 1] = {
            ...lastVolume,
            value: volume,
          };

          return newData;
        });

        setCurrentPrice(price);
      }
    }
  }, [realtimeData]);

  // 차트 크기 동기화 함수
  const resizeCharts = () => {
    if (!chartContainerRef.current || !priceChartRef.current || !volumeChartRef.current) return;

    // 차트 컨테이너의 실제 클라이언트 너비 사용 (padding 제외)
    const containerWidth = chartContainerRef.current.clientWidth;

    priceChartRef.current.applyOptions({ width: containerWidth });
    volumeChartRef.current.applyOptions({ width: containerWidth });
  };

  // 차트 초기화
  useEffect(() => {
    if (!priceChartContainerRef.current || !volumeChartContainerRef.current || !chartContainerRef.current) return;

    // 초기 차트 컨테이너 크기 계산
    const containerWidth = chartContainerRef.current.clientWidth || 800;
    const priceHeight = priceChartContainerRef.current.clientHeight || 240;
    const volumeHeight = volumeChartContainerRef.current.clientHeight || 160;

    // 가격 차트 생성
    const priceChart = createChart(priceChartContainerRef.current, {
      width: containerWidth,
      height: priceHeight,
      layout: {
        background: { type: ColorType.Solid, color: '#ffffff' },
        textColor: '#333',
        attributionLogo: false,
      },
      grid: {
        vertLines: { color: '#f0f0f0' },
        horzLines: { color: '#f0f0f0' },
      },
      rightPriceScale: {
        visible: true,
        borderColor: '#cccccc',
        scaleMargins: { top: 0.1, bottom: 0.1 },
        minimumWidth: 80,
      },
      timeScale: {
        visible: false,
        rightOffset: 36, // timeScale 높이(약 24px) 보정
        barSpacing: 6,
        minBarSpacing: 0.5,
        shiftVisibleRangeOnNewBar: false,
      },
      crosshair: {
        mode: CrosshairMode.Hidden, // 크로스헤어 완전히 숨기기
        // vertLine: { width: 1, color: '#9598A1', style: 0 },
        // horzLine: { width: 1, color: '#9598A1', style: 0 },
      },
      handleScroll: {
        mouseWheel: true,
        pressedMouseMove: true,
        horzTouchDrag: true,
        vertTouchDrag: false, // 세로 드래그 비활성화
      },
      handleScale: {
        axisPressedMouseMove: {
          time: true,
          price: false, // 가격 축 드래그 비활성화
        },
        axisDoubleClickReset: {
          time: true,
          price: false, // 가격 축 더블클릭 리셋 비활성화
        },
        mouseWheel: true,
        pinch: true,
      },
    });

    // 거래량 차트 생성
    const volumeChart = createChart(volumeChartContainerRef.current, {
      width: containerWidth,
      height: volumeHeight,
      layout: {
        background: { type: ColorType.Solid, color: '#ffffff' },
        textColor: '#333',
        attributionLogo: false,
      },
      grid: {
        vertLines: { color: '#f0f0f0' },
        horzLines: { color: '#f0f0f0' },
      },
      rightPriceScale: {
        visible: true,
        borderColor: '#cccccc',
        scaleMargins: { top: 0.1, bottom: 0.1 },
        minimumWidth: 80,
      },
      timeScale: {
        visible: true,
        borderColor: '#cccccc',
        rightOffset: 12,
        barSpacing: 6,
        minBarSpacing: 0.5,
        shiftVisibleRangeOnNewBar: false,
      },
      crosshair: {
        mode: CrosshairMode.Hidden, // 크로스헤어 완전히 숨기기
        // vertLine: { width: 1, color: '#9598A1', style: 0 },
        // horzLine: { visible: false }, // 기본적으로 수직선만
      },
      handleScroll: {
        mouseWheel: true,
        pressedMouseMove: true,
        horzTouchDrag: true,
        vertTouchDrag: false, // 세로 드래그 비활성화
      },
      handleScale: {
        axisPressedMouseMove: {
          time: true,
          price: false, // 가격 축 드래그 비활성화
        },
        axisDoubleClickReset: {
          time: true,
          price: false, // 가격 축 더블클릭 리셋 비활성화
        },
        mouseWheel: true,
        pinch: true,
      },
    });

    // 시리즈 추가
    const priceSeries = priceChart.addSeries(CandlestickSeries, {
      upColor: '#dc2626',
      downColor: '#2563eb',
      borderVisible: false,
      wickUpColor: '#dc2626',
      wickDownColor: '#2563eb',
      priceFormat: {
        type: 'price',
        precision: 0,
        minMove: 1,
      },
    });

    const volumeSeries = volumeChart.addSeries(HistogramSeries, {
      color: '#dc2626',
      priceFormat: { type: 'volume' },
    });

    priceChartRef.current = priceChart;
    volumeChartRef.current = volumeChart;
    priceSeriesRef.current = priceSeries;
    volumeSeriesRef.current = volumeSeries;

    // 크로스헤어 비활성화로 인해 주석처리
    // // 공식 TradingView 크로스헤어 동기화
    // const getCrosshairDataPoint = (series: any, param: any) => {
    //   if (!param.time) return null;
    //   return param.seriesData.get(series) || null;
    // };

    // const syncCrosshair = (chart: any, series: any, dataPoint: any, showHorzLine: boolean = false) => {
    //   if (crosshairSyncingRef.current) return;

    //   crosshairSyncingRef.current = true;

    //   if (dataPoint) {
    //     // 수직선만 또는 완전한 크로스헤어 설정
    //     if (showHorzLine) {
    //       // 완전한 크로스헤어 (활성 차트)
    //       chart.applyOptions({
    //         crosshair: {
    //           mode: CrosshairMode.Normal,
    //           vertLine: { width: 1, color: '#9598A1', style: 0 },
    //           horzLine: { width: 1, color: '#9598A1', style: 0 },
    //         },
    //       });
    //     } else {
    //       // 수직선만 (비활성 차트)
    //       chart.applyOptions({
    //         crosshair: {
    //           mode: CrosshairMode.Normal,
    //           vertLine: { width: 1, color: '#9598A1', style: 0 },
    //           horzLine: { visible: false },
    //         },
    //       });
    //     }
    //     chart.setCrosshairPosition(dataPoint.value, dataPoint.time, series);
    //   } else {
    //     chart.clearCrosshairPosition();
    //   }

    //   setTimeout(() => {
    //     crosshairSyncingRef.current = false;
    //   }, 10);
    // };

    // // Price → Volume 동기화 (수직선만)
    // priceChart.subscribeCrosshairMove((param) => {
    //   if (crosshairSyncingRef.current || !volumeChartRef.current || !volumeSeriesRef.current) return;

    //   const dataPoint = getCrosshairDataPoint(priceSeries, param);
    //   syncCrosshair(volumeChartRef.current, volumeSeriesRef.current, dataPoint, false); // 수직선만
    // });

    // // Volume → Price 동기화 (완전한 크로스헤어)
    // volumeChart.subscribeCrosshairMove((param) => {
    //   if (crosshairSyncingRef.current || !priceChartRef.current || !priceSeriesRef.current) return;

    //   const dataPoint = getCrosshairDataPoint(volumeSeries, param);
    //   syncCrosshair(priceChartRef.current, priceSeriesRef.current, dataPoint, true); // 완전한 크로스헤어
    // });

    // 단일 논리적 범위 동기화 (줌과 드래그 모두 처리)
    priceChart.timeScale().subscribeVisibleLogicalRangeChange((logicalRange) => {
      if (panSyncingRef.current || !logicalRange || !volumeChartRef.current) return;

      panSyncingRef.current = true;
      volumeChartRef.current.timeScale().setVisibleLogicalRange(logicalRange);
      panSyncingRef.current = false;
    });

    volumeChart.timeScale().subscribeVisibleLogicalRangeChange((logicalRange) => {
      if (panSyncingRef.current || !logicalRange || !priceChartRef.current) return;

      panSyncingRef.current = true;
      priceChartRef.current.timeScale().setVisibleLogicalRange(logicalRange);
      panSyncingRef.current = false;
    });

    // ResizeObserver 설정
    if (chartContainerRef.current) {
      resizeObserverRef.current = new ResizeObserver(() => {
        resizeCharts();
      });
      resizeObserverRef.current.observe(chartContainerRef.current);
    }

    return () => {
      priceChart.remove();
      volumeChart.remove();

      // ResizeObserver 정리
      if (resizeObserverRef.current) {
        resizeObserverRef.current.disconnect();
        resizeObserverRef.current = null;
      }
    };
  }, []);

  // 데이터 설정 (자연스러운 자동 피팅 활용)
  useEffect(() => {
    if (priceSeriesRef.current && volumeSeriesRef.current && candleData.length > 0) {
      // 동기화 차단
      panSyncingRef.current = true;

      // 데이터 설정 (TradingView 자동 피팅 활용)
      priceSeriesRef.current.setData(candleData);
      volumeSeriesRef.current.setData(volumeData);

      // 잠시 후 자동 피팅이 완료되면 동기화 재개 및 시간축 동기화
      setTimeout(() => {
        if (priceChartRef.current && volumeChartRef.current) {
          // 가격 차트의 현재 보이는 범위를 거래량 차트에 적용
          const priceVisibleRange = priceChartRef.current.timeScale().getVisibleLogicalRange();
          if (priceVisibleRange) {
            volumeChartRef.current.timeScale().setVisibleLogicalRange(priceVisibleRange);
          }
        }
        panSyncingRef.current = false;
      }, 100);
    }
  }, [candleData, volumeData]);

  // API 데이터를 차트 데이터로 변환
  useEffect(() => {
    if (USE_API_DATA && chartApiData) {
      const { candleData, volumeData, summary } = transformChartData(chartApiData);

      setCandleData(candleData);
      setVolumeData(volumeData);

      // 현재 가격 정보 설정
      const currentPrice = Number(summary.currentPrice);
      const priceChange = Number(summary.priceChange);
      const changeRate = Number(summary.changeRate);

      setCurrentPrice(currentPrice);
      setPriceChange({
        amount: priceChange,
        rate: changeRate,
      });

      if (onPriceUpdate) {
        onPriceUpdate(currentPrice, {
          amount: priceChange,
          rate: changeRate,
        });
      }
    }
  }, [USE_API_DATA, chartApiData, onPriceUpdate]);

  // 시뮬레이션 시작 (API 모드가 아닐 때만)
  useEffect(() => {
    if (!USE_API_DATA) {
      const basePrice = getInitialPrice(stockCode);
      generateHistoricalData(basePrice);
    }
  }, [USE_API_DATA, stockCode]);

  const periods: { label: string; value: ChartPeriodType }[] = [
    { label: '일', value: 'daily' },
    { label: '주', value: 'weekly' },
    { label: '월', value: 'monthly' },
    { label: '년', value: 'yearly' },
  ];

  return (
    <div className={styles.container}>
      {/* 헤더 */}
      <div className={styles.header}>
        <div className={styles.timeSelector}>
          {periods.map((period) => (
            <button
              key={period.value}
              className={`${styles.timeButton} ${selectedPeriod === period.value ? styles.active : ''}`}
              onClick={() => setSelectedPeriod(period.value)}
            >
              {period.label}
            </button>
          ))}
        </div>
      </div>

      {/* 차트 컨테이너 */}
      <div ref={chartContainerRef} className={styles.chartContainer}>
        <div ref={priceChartContainerRef} className={styles.priceChart} />
        <div ref={volumeChartContainerRef} className={styles.volumeChart} />
      </div>
    </div>
  );
}
