import { useEffect, useRef, useState } from 'react';
import { createChart, ColorType, CandlestickSeries, HistogramSeries, CrosshairMode } from 'lightweight-charts';
import type { IChartApi, ISeriesApi, UTCTimestamp } from 'lightweight-charts';
import type { RealtimePriceMessage } from '../../features/stock-search/api/types';
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
  const priceChartRef = useRef<IChartApi | null>(null);
  const volumeChartRef = useRef<IChartApi | null>(null);
  const priceSeriesRef = useRef<ISeriesApi<'Candlestick'> | null>(null);
  const volumeSeriesRef = useRef<ISeriesApi<'Histogram'> | null>(null);
  const simulationIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const syncingRef = useRef<boolean>(false); // 무한 루프 방지
  const syncTimeoutRef = useRef<number | null>(null); // 플래그 리셋 타이머

  const [selectedPeriod, setSelectedPeriod] = useState<string>('일');
  const [currentPrice, setCurrentPrice] = useState<number | null>(null);
  const [priceChange, setPriceChange] = useState<{ amount: number; rate: number } | null>(null);
  const [volume, setVolume] = useState<number | null>(null);
  const [candleData, setCandleData] = useState<CandleData[]>([]);
  const [volumeData, setVolumeData] = useState<VolumeData[]>([]);
  const [activeChart, setActiveChart] = useState<'price' | 'volume' | null>(null);

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

  // 하루 동안의 과거 데이터 생성 (9:00 ~ 현재)
  const generateHistoricalData = (basePrice: number) => {
    const now = new Date();
    const today9AM = new Date();
    today9AM.setHours(9, 0, 0, 0);

    const minutesSince9AM = Math.floor((now.getTime() - today9AM.getTime()) / (1000 * 60));
    const totalMinutes = Math.min(minutesSince9AM, 390); // 9:00~15:30 = 390분

    const candleData: CandleData[] = [];
    const volumeData: VolumeData[] = [];

    let currentPrice = basePrice;

    // 5분 단위 캔들로 생성 (더 실제적인 차트)
    const candleInterval = 5; // 5분 간격
    const totalCandles = Math.floor(totalMinutes / candleInterval);

    for (let i = 0; i <= totalCandles; i++) {
      const time = new Date(today9AM.getTime() + i * candleInterval * 60 * 1000);
      const timestamp = Math.floor(time.getTime() / 1000) as UTCTimestamp;

      const open = currentPrice;

      // 캔들 내에서 고가/저가 생성
      const volatility = 0.02; // 2% 변동성
      const high = open * (1 + Math.random() * volatility);
      const low = open * (1 - Math.random() * volatility);

      // 종가 생성 (-1% ~ +1%)
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

      // 거래량 (상승/하락에 따른 색상)
      const volumeValue = Math.floor(Math.random() * 1000000) + 100000;
      const color = close >= open ? '#dc2626' : '#2563eb';

      volumeData.push({
        time: timestamp,
        value: volumeValue,
        color: color,
      });
    }

    return { candleData, volumeData, currentPrice };
  };

  // 시뮬레이션 시작
  const startSimulation = () => {
    if (!priceSeriesRef.current || !volumeSeriesRef.current) return;

    const initialPrice = getInitialPrice(stockCode);
    const { candleData, volumeData, currentPrice } = generateHistoricalData(initialPrice);

    // 차트에 과거 데이터 설정
    priceSeriesRef.current.setData(candleData);
    volumeSeriesRef.current.setData(volumeData);

    setCandleData(candleData);
    setVolumeData(volumeData);
    setCurrentPrice(Math.round(currentPrice));

    // 초기 변동률 계산
    const priceChangeFromInitial = currentPrice - initialPrice;
    const priceRateFromInitial = (priceChangeFromInitial / initialPrice) * 100;

    const changeData = {
      amount: Math.round(priceChangeFromInitial),
      rate: parseFloat(priceRateFromInitial.toFixed(2)),
    };
    setPriceChange(changeData);

    // 부모 컴포넌트에 실시간 데이터 전달
    if (onPriceUpdate) {
      onPriceUpdate(Math.round(currentPrice), changeData);
    }

    setVolume(volumeData[volumeData.length - 1]?.value || 0);

    // 실시간 업데이트 (현재 캔들만 갱신)
    simulationIntervalRef.current = setInterval(() => {
      if (!priceSeriesRef.current || !volumeSeriesRef.current) return;

      const lastCandle = candleData[candleData.length - 1];
      if (!lastCandle) return;

      // 현재 캔들의 close 가격을 업데이트
      const changePercent = (Math.random() - 0.5) * 0.002;
      const newClose = Math.max(lastCandle.close * (1 + changePercent), initialPrice * 0.8);

      // 고가/저가 업데이트
      const updatedCandle: CandleData = {
        ...lastCandle,
        high: Math.max(lastCandle.high, newClose),
        low: Math.min(lastCandle.low, newClose),
        close: Math.round(newClose),
      };

      // 마지막 캔들만 업데이트
      priceSeriesRef.current.update(updatedCandle);

      // 거래량도 업데이트
      const newVolumeValue = Math.floor(Math.random() * 500000) + 50000;
      const volumeColor = newClose >= lastCandle.open ? '#dc2626' : '#2563eb';

      const updatedVolumeData: VolumeData = {
        time: lastCandle.time,
        value: newVolumeValue,
        color: volumeColor,
      };

      volumeSeriesRef.current.update(updatedVolumeData);

      // 상태 업데이트
      setCandleData((prev) => {
        const newData = [...prev];
        newData[newData.length - 1] = updatedCandle;
        return newData;
      });

      setVolumeData((prev) => {
        const newData = [...prev];
        newData[newData.length - 1] = updatedVolumeData;
        return newData;
      });

      setCurrentPrice(Math.round(newClose));
      setVolume(newVolumeValue);

      const priceChangeFromInitial = newClose - initialPrice;
      const priceRateFromInitial = (priceChangeFromInitial / initialPrice) * 100;

      const updateChangeData = {
        amount: Math.round(priceChangeFromInitial),
        rate: parseFloat(priceRateFromInitial.toFixed(2)),
      };
      setPriceChange(updateChangeData);

      // 실시간 업데이트를 부모에게 전달
      if (onPriceUpdate) {
        onPriceUpdate(Math.round(newClose), updateChangeData);
      }
    }, 3000); // 3초마다 업데이트
  };

  // 시뮬레이션 중지
  const stopSimulation = () => {
    if (simulationIntervalRef.current) {
      clearInterval(simulationIntervalRef.current);
      simulationIntervalRef.current = null;
    }
  };

  useEffect(() => {
    if (!priceChartContainerRef.current || !volumeChartContainerRef.current) return;

    // 가격 차트 (캔들스틱)
    const priceChart = createChart(priceChartContainerRef.current, {
      width: priceChartContainerRef.current.clientWidth,
      height: 280,
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
        borderColor: '#cccccc',
        scaleMargins: {
          top: 0.1,
          bottom: 0.1,
        },
      },
      timeScale: {
        visible: false, // 상단 차트는 시간축 숨김
      },
      crosshair: {
        mode: CrosshairMode.Normal,
        vertLine: {
          visible: true,
          color: '#2962ff',
          width: 1,
          labelVisible: true,
        },
        horzLine: {
          visible: true,
          color: '#2962ff',
          width: 1,
          labelVisible: true,
        },
      },
    });

    // 거래량 차트
    const volumeChart = createChart(volumeChartContainerRef.current, {
      width: volumeChartContainerRef.current.clientWidth,
      height: 120,
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
        borderColor: '#cccccc',
        scaleMargins: {
          top: 0.1,
          bottom: 0.1,
        },
      },
      timeScale: {
        borderColor: '#cccccc',
        timeVisible: true,
        secondsVisible: false,
        visible: true, // 하단 차트만 시간축 표시
      },
      crosshair: {
        mode: CrosshairMode.Normal,
        vertLine: {
          visible: true,
          color: '#2962ff',
          width: 1,
          labelVisible: true,
        },
        horzLine: {
          visible: true,
          color: '#2962ff',
          width: 1,
          labelVisible: true,
        },
      },
    });

    // 캔들스틱 시리즈
    const priceSeries = priceChart.addSeries(CandlestickSeries, {
      upColor: '#dc2626',
      downColor: '#2563eb',
      borderVisible: false,
      wickUpColor: '#dc2626',
      wickDownColor: '#2563eb',
    });

    // 거래량 히스토그램 시리즈
    const volumeSeries = volumeChart.addSeries(HistogramSeries, {
      color: '#dc2626',
      priceFormat: {
        type: 'volume',
      },
    });

    priceChartRef.current = priceChart;
    volumeChartRef.current = volumeChart;
    priceSeriesRef.current = priceSeries;
    volumeSeriesRef.current = volumeSeries;

    // 차트 동기화 설정
    // 1. 논리적 범위 기반 동기화 (줌/스크롤)
    priceChart.timeScale().subscribeVisibleTimeRangeChange(() => {
      if (syncingRef.current) return; // 무한 루프 방지

      try {
        syncingRef.current = true;

        // 논리적 범위를 가져와서 동기화
        const logicalRange = priceChart.timeScale().getVisibleLogicalRange();
        if (logicalRange && logicalRange.from !== null && logicalRange.to !== null && volumeChart) {
          volumeChart.timeScale().setVisibleLogicalRange(logicalRange);
        }
      } catch (error) {
        console.warn('가격 차트 → 거래량 차트 동기화 실패:', error);
      } finally {
        // 기존 타이머 취소
        if (syncTimeoutRef.current) {
          cancelAnimationFrame(syncTimeoutRef.current);
        }

        // 다음 프레임에서 플래그 리셋
        syncTimeoutRef.current = requestAnimationFrame(() => {
          syncingRef.current = false;
          syncTimeoutRef.current = null;
        });
      }
    });

    volumeChart.timeScale().subscribeVisibleTimeRangeChange(() => {
      if (syncingRef.current) return; // 무한 루프 방지

      try {
        syncingRef.current = true;

        // 논리적 범위를 가져와서 동기화
        const logicalRange = volumeChart.timeScale().getVisibleLogicalRange();
        if (logicalRange && logicalRange.from !== null && logicalRange.to !== null && priceChart) {
          priceChart.timeScale().setVisibleLogicalRange(logicalRange);
        }
      } catch (error) {
        console.warn('거래량 차트 → 가격 차트 동기화 실패:', error);
      } finally {
        // 기존 타이머 취소
        if (syncTimeoutRef.current) {
          cancelAnimationFrame(syncTimeoutRef.current);
        }

        // 다음 프레임에서 플래그 리셋
        syncTimeoutRef.current = requestAnimationFrame(() => {
          syncingRef.current = false;
          syncTimeoutRef.current = null;
        });
      }
    });

    // 2. 크로스헤어는 차트별 설정으로만 제어 (별도 동기화 불필요)
    // updateCrosshairMode 함수에서 마우스 위치에 따라 자동 제어됨

    const handleResize = () => {
      if (priceChartContainerRef.current && priceChartRef.current) {
        priceChartRef.current.applyOptions({
          width: priceChartContainerRef.current.clientWidth,
        });
      }
      if (volumeChartContainerRef.current && volumeChartRef.current) {
        volumeChartRef.current.applyOptions({
          width: volumeChartContainerRef.current.clientWidth,
        });
      }
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);

      // 동기화 타이머 정리
      if (syncTimeoutRef.current) {
        cancelAnimationFrame(syncTimeoutRef.current);
        syncTimeoutRef.current = null;
      }
      syncingRef.current = false;

      priceChart.remove();
      volumeChart.remove();
    };
  }, []);

  useEffect(() => {
    // 차트가 준비되면 시뮬레이션 시작
    if (stockCode && priceSeriesRef.current && volumeSeriesRef.current) {
      startSimulation();
    }

    return () => {
      if (simulationIntervalRef.current) {
        clearInterval(simulationIntervalRef.current);
        simulationIntervalRef.current = null;
      }
    };
  }, [stockCode, priceSeriesRef.current, volumeSeriesRef.current]);

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const timePeriods = ['일', '주', '월', '년'];

  const handlePeriodChange = (period: string) => {
    setSelectedPeriod(period);
    // TODO: 기간별 데이터 로딩 로직 추가
  };

  // 크로스헤어 모드 업데이트 함수
  const updateCrosshairMode = (active: 'price' | 'volume' | null) => {
    // 비동기 실행으로 차트 준비 상태 보장
    requestAnimationFrame(() => {
      if (!priceChartRef.current || !volumeChartRef.current) {
        console.warn('차트 인스턴스가 준비되지 않음');
        return;
      }

      console.log(`크로스헤어 모드 변경: ${active}`);
      console.log('차트 준비 상태:', {
        priceChart: !!priceChartRef.current,
        volumeChart: !!volumeChartRef.current,
      });

      try {
        if (active === 'price') {
          console.log('상단: 완전한 크로스헤어, 하단: 수직선만');

          // 가격 차트: 완전한 크로스헤어
          priceChartRef.current.applyOptions({
            crosshair: {
              mode: CrosshairMode.Normal,
              vertLine: {
                visible: true,
                color: '#ff0000', // 빨간색으로 더 뚜렷하게
                width: 2,
                style: 0, // 실선
                labelVisible: true,
              },
              horzLine: {
                visible: true,
                color: '#ff0000', // 빨간색으로 더 뚜렷하게
                width: 2,
                style: 0, // 실선
                labelVisible: true,
              },
            },
          });

          // 거래량 차트: 수직선만
          volumeChartRef.current.applyOptions({
            crosshair: {
              mode: CrosshairMode.Normal,
              vertLine: {
                visible: true,
                color: '#00ff00', // 초록색으로 구분
                width: 2,
                style: 0, // 실선
                labelVisible: true,
              },
              horzLine: {
                visible: false,
                color: '#transparent',
                width: 0,
              },
            },
          });
        } else if (active === 'volume') {
          console.log('상단: 수직선만, 하단: 완전한 크로스헤어');

          // 가격 차트: 수직선만
          priceChartRef.current.applyOptions({
            crosshair: {
              mode: CrosshairMode.Normal,
              vertLine: {
                visible: true,
                color: '#00ff00', // 초록색으로 구분
                width: 2,
                style: 0, // 실선
                labelVisible: true,
              },
              horzLine: {
                visible: false,
                color: 'transparent',
                width: 0,
              },
            },
          });

          // 거래량 차트: 완전한 크로스헤어
          volumeChartRef.current.applyOptions({
            crosshair: {
              mode: CrosshairMode.Normal,
              vertLine: {
                visible: true,
                color: '#ff0000', // 빨간색으로 더 뚜렷하게
                width: 2,
                style: 0, // 실선
                labelVisible: true,
              },
              horzLine: {
                visible: true,
                color: '#ff0000', // 빨간색으로 더 뚜렷하게
                width: 2,
                style: 0, // 실선
                labelVisible: true,
              },
            },
          });
        } else {
          console.log('크로스헤어 모두 숨김');

          // 두 차트 모두 크로스헤어 완전히 숨김
          priceChartRef.current.applyOptions({
            crosshair: {
              mode: CrosshairMode.Normal,
              vertLine: {
                visible: false,
                color: 'transparent',
                width: 0,
              },
              horzLine: {
                visible: false,
                color: 'transparent',
                width: 0,
              },
            },
          });

          volumeChartRef.current.applyOptions({
            crosshair: {
              mode: CrosshairMode.Normal,
              vertLine: {
                visible: false,
                color: 'transparent',
                width: 0,
              },
              horzLine: {
                visible: false,
                color: 'transparent',
                width: 0,
              },
            },
          });
        }
      } catch (error) {
        console.error('크로스헤어 모드 업데이트 실패:', error);
      }
    });
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.timeSelector}>
          {timePeriods.map((period) => (
            <button
              key={period}
              className={`${styles.timeButton} ${selectedPeriod === period ? styles.active : ''}`}
              onClick={() => handlePeriodChange(period)}
            >
              {period}
            </button>
          ))}
        </div>
      </div>

      <div className={styles.chartContainer}>
        <div
          ref={priceChartContainerRef}
          className={styles.priceChart}
          onMouseEnter={() => {
            setActiveChart('price');
            updateCrosshairMode('price');
          }}
          onMouseLeave={() => {
            setActiveChart(null);
            updateCrosshairMode(null);
          }}
        />
        <div
          ref={volumeChartContainerRef}
          className={styles.volumeChart}
          onMouseEnter={() => {
            setActiveChart('volume');
            updateCrosshairMode('volume');
          }}
          onMouseLeave={() => {
            setActiveChart(null);
            updateCrosshairMode(null);
          }}
        />
      </div>
    </div>
  );
}
