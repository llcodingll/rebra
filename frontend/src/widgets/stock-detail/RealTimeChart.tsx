import { useEffect, useRef, useState } from 'react';
import { createChart, ColorType, LineSeries, HistogramSeries } from 'lightweight-charts';
import type { IChartApi, ISeriesApi, UTCTimestamp } from 'lightweight-charts';
import styles from './RealTimeChart.module.css';

interface RealTimeChartProps {
  stockCode: string;
  stockName: string;
  onPriceUpdate?: (price: number, change: { amount: number; rate: number }) => void;
}

interface ChartData {
  time: UTCTimestamp;
  value: number;
}

interface VolumeData {
  time: UTCTimestamp;
  value: number;
  color?: string;
}

export default function RealTimeChart({ stockCode, stockName, onPriceUpdate }: RealTimeChartProps) {
  const chartContainerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const priceSeriesRef = useRef<ISeriesApi<'Line'> | null>(null);
  const volumeSeriesRef = useRef<ISeriesApi<'Histogram'> | null>(null);
  const simulationIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const [currentPrice, setCurrentPrice] = useState<number | null>(null);
  const [priceChange, setPriceChange] = useState<{ amount: number; rate: number } | null>(null);
  const [volume, setVolume] = useState<number | null>(null);
  const [chartData, setChartData] = useState<ChartData[]>([]);
  const [volumeData, setVolumeData] = useState<VolumeData[]>([]);

  // 종목별 초기 가격 설정
  const getInitialPrice = (code: string) => {
    const prices: Record<string, number> = {
      '005930': 71400,  // 삼성전자
      '000660': 125000, // SK하이닉스
      '035420': 180000, // NAVER
      '051910': 420000, // LG화학
      '006400': 250000, // 삼성SDI
      '028260': 45000,  // 삼성물산
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
    
    const priceData: ChartData[] = [];
    const volumeData: VolumeData[] = [];
    
    let currentPrice = basePrice;
    let prevPrice = basePrice;
    
    // 9:00부터 현재까지의 데이터 생성
    for (let i = 0; i <= totalMinutes; i++) {
      const time = new Date(today9AM.getTime() + i * 60 * 1000);
      const timestamp = Math.floor(time.getTime() / 1000) as UTCTimestamp;
      
      // 가격 변동 (-0.5% ~ +0.5% per minute)
      const changePercent = (Math.random() - 0.5) * 0.01;
      currentPrice = Math.max(currentPrice * (1 + changePercent), basePrice * 0.8);
      
      priceData.push({
        time: timestamp,
        value: currentPrice
      });
      
      // 거래량 (상승/하락에 따른 색상)
      const volumeValue = Math.floor(Math.random() * 1000000) + 100000;
      const color = currentPrice >= prevPrice ? '#dc2626' : '#2563eb';
      
      volumeData.push({
        time: timestamp,
        value: volumeValue,
        color: color
      });
      
      prevPrice = currentPrice;
    }
    
    return { priceData, volumeData, currentPrice };
  };

  // 시뮬레이션 시작
  const startSimulation = () => {
    if (!priceSeriesRef.current || !volumeSeriesRef.current) return;

    const initialPrice = getInitialPrice(stockCode);
    const { priceData, volumeData, currentPrice } = generateHistoricalData(initialPrice);
    
    // 차트에 과거 데이터 설정
    priceSeriesRef.current.setData(priceData);
    volumeSeriesRef.current.setData(volumeData);
    
    setChartData(priceData);
    setVolumeData(volumeData);
    setCurrentPrice(Math.round(currentPrice));
    
    // 초기 변동률 계산
    const priceChangeFromInitial = currentPrice - initialPrice;
    const priceRateFromInitial = (priceChangeFromInitial / initialPrice) * 100;
    
    const changData = {
      amount: Math.round(priceChangeFromInitial),
      rate: parseFloat(priceRateFromInitial.toFixed(2))
    };
    setPriceChange(changData);
    
    // 부모 컴포넌트에 실시간 데이터 전달
    if (onPriceUpdate) {
      onPriceUpdate(Math.round(currentPrice), changData);
    }
    
    setVolume(volumeData[volumeData.length - 1]?.value || 0);

    // 실시간 업데이트 (현재 시점만 갱신)
    simulationIntervalRef.current = setInterval(() => {
      if (!priceSeriesRef.current || !volumeSeriesRef.current) return;

      const now = Math.floor(Date.now() / 1000) as UTCTimestamp;
      const lastPrice = chartData[chartData.length - 1]?.value || currentPrice;
      
      // 작은 변동 (-0.1% ~ +0.1%)
      const changePercent = (Math.random() - 0.5) * 0.002;
      const newPrice = Math.max(lastPrice * (1 + changePercent), initialPrice * 0.8);
      
      const newPriceData: ChartData = {
        time: now,
        value: newPrice
      };
      
      const newVolumeValue = Math.floor(Math.random() * 500000) + 50000;
      const volumeColor = newPrice >= lastPrice ? '#dc2626' : '#2563eb';
      
      const newVolumeData: VolumeData = {
        time: now,
        value: newVolumeValue,
        color: volumeColor
      };

      // 차트 업데이트 (가장 오른쪽 점만)
      priceSeriesRef.current.update(newPriceData);
      volumeSeriesRef.current.update(newVolumeData);
      
      // 상태 업데이트
      setChartData(prev => [...prev, newPriceData]);
      setVolumeData(prev => [...prev, newVolumeData]);
      setCurrentPrice(Math.round(newPrice));
      setVolume(newVolumeValue);
      
      const priceChangeFromInitial = newPrice - initialPrice;
      const priceRateFromInitial = (priceChangeFromInitial / initialPrice) * 100;
      
      const changeData = {
        amount: Math.round(priceChangeFromInitial),
        rate: parseFloat(priceRateFromInitial.toFixed(2))
      };
      setPriceChange(changeData);
      
      // 실시간 업데이트를 부모에게 전달
      if (onPriceUpdate) {
        onPriceUpdate(Math.round(newPrice), changeData);
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
    if (!chartContainerRef.current) return;

    const chart = createChart(chartContainerRef.current, {
      width: chartContainerRef.current.clientWidth,
      height: 400,
      layout: {
        background: { type: ColorType.Solid, color: '#ffffff' },
        textColor: '#333',
      },
      grid: {
        vertLines: { color: '#f0f0f0' },
        horzLines: { color: '#f0f0f0' },
      },
      rightPriceScale: {
        borderColor: '#cccccc',
        scaleMargins: {
          top: 0.3,
          bottom: 0.25,
        },
      },
      timeScale: {
        borderColor: '#cccccc',
        timeVisible: true,
        secondsVisible: false,
      },
    });

    // 가격 라인 시리즈
    const priceSeries = chart.addSeries(LineSeries, {
      color: '#2962ff',
      lineWidth: 2,
      crosshairMarkerVisible: true,
      crosshairMarkerRadius: 4,
      priceScaleId: 'right',
    });

    // 거래량 히스토그램 시리즈
    const volumeSeries = chart.addSeries(HistogramSeries, {
      color: '#dc2626',
      priceFormat: {
        type: 'volume',
      },
      priceScaleId: '',
    });

    chartRef.current = chart;
    priceSeriesRef.current = priceSeries;
    volumeSeriesRef.current = volumeSeries;

    const handleResize = () => {
      if (chartContainerRef.current && chartRef.current) {
        chartRef.current.applyOptions({
          width: chartContainerRef.current.clientWidth,
        });
      }
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      chart.remove();
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

  const getChangeColor = () => {
    if (!priceChange) return styles.neutral;
    if (priceChange.amount > 0) return styles.positive;
    if (priceChange.amount < 0) return styles.negative;
    return styles.neutral;
  };

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.stockInfo}>
          <span className={styles.label}>시작 고가 저가 종가</span>
          {currentPrice !== null && priceChange && (
            <span className={`${styles.priceData} ${getChangeColor()}`}>
              {formatNumber(currentPrice)}원 ({priceChange.rate > 0 ? '+' : ''}{priceChange.rate.toFixed(2)}%, {new Date().toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit' })})
            </span>
          )}
        </div>
      </div>

      <div ref={chartContainerRef} className={styles.chartContainer} />
    </div>
  );
}