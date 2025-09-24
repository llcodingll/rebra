import { useEffect, useRef, useState, useCallback, useMemo } from 'react';
import { createChart, ColorType, CandlestickSeries, HistogramSeries, CrosshairMode } from 'lightweight-charts';
import type { IChartApi, ISeriesApi, UTCTimestamp } from 'lightweight-charts';
import type { OptimizedPriceData } from '../../features/stock-detail/api/types';
import { useInfiniteChartData, mergeInfiniteChartData } from '../../features/stock-detail/hooks/useInfiniteChartData';
import type { ChartPeriodType } from '../../features/stock-detail/utils/dateUtils';
import { transformChartData } from '../../features/stock-detail/utils/chartDataTransform';
import styles from './RealTimeChart.module.css';

interface RealTimeChartProps {
  stockCode: string;
  stockName: string;
  realtimeData?: OptimizedPriceData | null;
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

export default function RealTimeChart({ stockCode, stockName, realtimeData }: RealTimeChartProps) {
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
  const loadingMoreDataRef = useRef<boolean>(false); // 데이터 로딩 중복 방지
  const candleDataRef = useRef<CandleData[]>([]); // 최신 candleData 참조용
  const lastCandleRef = useRef<CandleData | null>(null); // 실시간 업데이트용 마지막 캔들
  const lastVolumeRef = useRef<VolumeData | null>(null); // 실시간 업데이트용 마지막 볼륨

  const [selectedPeriod, setSelectedPeriod] = useState<ChartPeriodType>('daily');
  const [currentPrice, setCurrentPrice] = useState<number | null>(null);
  const [priceChange, setPriceChange] = useState<{ amount: number; rate: number } | null>(null);
  const [volume, setVolume] = useState<number | null>(null);
  const [candleData, setCandleData] = useState<CandleData[]>([]);
  const [volumeData, setVolumeData] = useState<VolumeData[]>([]);

  // API에서 차트 데이터 가져오기 (무한 스크롤)
  const {
    data: infiniteData,
    isLoading,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useInfiniteChartData(stockCode, selectedPeriod);

  // 무한 쿼리 데이터를 병합 (메모이제이션으로 불필요한 재계산 방지)
  const chartApiData = useMemo(() => {
    return mergeInfiniteChartData(infiniteData?.pages);
  }, [infiniteData?.pages]);

  // 무한 스크롤 핸들러 (스로틀링 적용)
  const handleVisibleRangeChange = useCallback(
    (timeRange: any) => {
      if (!timeRange || loadingMoreDataRef.current || !hasNextPage || isFetchingNextPage) {
        return;
      }

      // 현재 보이는 범위의 시작점과 전체 데이터의 시작점 비교
      const visibleStart = timeRange.from;
      const dataStart = candleDataRef.current.length > 0 ? candleDataRef.current[0].time : null;

      if (dataStart && visibleStart && visibleStart <= dataStart + 5) {
        // 5초 여유값으로 트리거
        loadingMoreDataRef.current = true;

        fetchNextPage().finally(() => {
          // 로딩 완료 후 플래그 해제 (딜레이로 중복 호출 방지)
          setTimeout(() => {
            loadingMoreDataRef.current = false;
          }, 1000);
        });
      }
    },
    [hasNextPage, isFetchingNextPage, fetchNextPage]
  );

  // 실시간 데이터 업데이트
  useEffect(() => {
    if (realtimeData && priceSeriesRef.current && volumeSeriesRef.current && initialDataSetRef.current) {
      const price = Number(realtimeData.stckPrpr);
      const volume = Number(realtimeData.acmlVol);

      if (price > 0 && lastCandleRef.current && lastVolumeRef.current) {
        const lastCandle = lastCandleRef.current;
        const lastVolume = lastVolumeRef.current;

        const updatedCandle: CandleData = {
          ...lastCandle,
          close: price,
          high: Math.max(lastCandle.high, price),
          low: Math.min(lastCandle.low, price),
        };

        const updatedVolume: VolumeData = {
          ...lastVolume,
          value: volume,
          color: price >= lastCandle.open ? '#ea3939' : '#3b82f6',
        };

        // ref 업데이트
        lastCandleRef.current = updatedCandle;
        lastVolumeRef.current = updatedVolume;

        // 차트 업데이트
        try {
          const currentCandleData = candleDataRef.current;
          const updatedCandleArray = [...currentCandleData];
          const updatedVolumeArray = [...volumeData];

          if (updatedCandleArray.length > 0) {
            updatedCandleArray[updatedCandleArray.length - 1] = updatedCandle;
            updatedVolumeArray[updatedVolumeArray.length - 1] = updatedVolume;

            priceSeriesRef.current.setData(updatedCandleArray);
            volumeSeriesRef.current.setData(updatedVolumeArray);
            candleDataRef.current = updatedCandleArray;
          }
        } catch (error) {
          console.error('차트 업데이트 실패:', error);
        }

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
      upColor: '#ea3939',
      downColor: '#3b82f6',
      borderVisible: false,
      wickUpColor: '#ea3939',
      wickDownColor: '#3b82f6',
      priceFormat: {
        type: 'price',
        precision: 0,
        minMove: 1,
      },
    });

    const volumeSeries = volumeChart.addSeries(HistogramSeries, {
      color: '#ea3939',
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

    // 무한 스크롤 이벤트는 별도 useEffect에서 관리

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
  }, []); // 의존성 배열에서 handleVisibleRangeChange 제거

  // candleData ref 업데이트
  useEffect(() => {
    candleDataRef.current = candleData;
  }, [candleData]);

  // 이벤트 핸들러 등록 (차트가 생성된 후)
  useEffect(() => {
    if (priceChartRef.current && volumeChartRef.current && handleVisibleRangeChange) {
      const priceChart = priceChartRef.current;
      const volumeChart = volumeChartRef.current;

      // 무한 스크롤을 위한 가시 시간 범위 변경 감지 재등록
      priceChart.timeScale().subscribeVisibleTimeRangeChange(handleVisibleRangeChange);
      volumeChart.timeScale().subscribeVisibleTimeRangeChange(handleVisibleRangeChange);

      return () => {
        // 정리 시 이벤트 해제
        priceChart.timeScale().unsubscribeVisibleTimeRangeChange(handleVisibleRangeChange);
        volumeChart.timeScale().unsubscribeVisibleTimeRangeChange(handleVisibleRangeChange);
      };
    }
  }, [handleVisibleRangeChange]);

  // 초기 차트 데이터 설정 플래그
  const initialDataSetRef = useRef<boolean>(false);

  // API 데이터를 차트 데이터로 변환 및 초기 설정
  useEffect(() => {
    if (chartApiData) {
      const { candleData, volumeData, summary } = transformChartData(chartApiData);

      setCandleData(candleData);
      setVolumeData(volumeData);

      // 차트 시리즈가 준비되면 직접 초기 데이터 설정
      if (priceSeriesRef.current && volumeSeriesRef.current && candleData.length > 0) {
        // 동기화 차단
        panSyncingRef.current = true;

        // 초기 데이터 설정
        priceSeriesRef.current.setData(candleData);
        volumeSeriesRef.current.setData(volumeData);

        // 실시간 업데이트용 ref에 마지막 데이터 저장
        lastCandleRef.current = candleData[candleData.length - 1];
        lastVolumeRef.current = volumeData[volumeData.length - 1];
        candleDataRef.current = candleData; // candleDataRef도 업데이트

        initialDataSetRef.current = true; // 초기 설정 완료 표시

        // 잠시 후 자동 피팅이 완료되면 동기화 재개
        setTimeout(() => {
          if (priceChartRef.current && volumeChartRef.current) {
            const priceVisibleRange = priceChartRef.current.timeScale().getVisibleLogicalRange();
            if (priceVisibleRange) {
              volumeChartRef.current.timeScale().setVisibleLogicalRange(priceVisibleRange);
            }
          }
          panSyncingRef.current = false;
        }, 100);
      }

      // 현재 가격 정보 설정
      const currentPrice = Number(summary.currentPrice);
      const priceChange = Number(summary.priceChange);
      const changeRate = Number(summary.changeRate);

      setCurrentPrice(currentPrice);
      setPriceChange({
        amount: priceChange,
        rate: changeRate,
      });

    }
  }, [chartApiData]);

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
