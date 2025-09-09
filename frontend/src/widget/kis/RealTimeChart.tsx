import { useEffect, useRef, useState } from 'react';
import { createChart, type IChartApi, type ISeriesApi, ColorType } from 'lightweight-charts';
import { KisClient, KisWebSocket, type KisRealTimeData } from '../../shared/kis';
import styles from './RealTimeChart.module.css';

interface RealTimeChartProps {
  appkey: string;
  appsecret: string;
  stockCode: string;
  stockName: string;
}

interface ChartData {
  time: number;
  value: number;
}

export function RealTimeChart({ appkey, appsecret, stockCode, stockName }: RealTimeChartProps) {
  const chartContainerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const seriesRef = useRef<ISeriesApi<'Line'> | null>(null);
  const kisClientRef = useRef<KisClient | null>(null);
  const webSocketRef = useRef<KisWebSocket | null>(null);
  
  const [isConnected, setIsConnected] = useState(false);
  const [currentPrice, setCurrentPrice] = useState<number | null>(null);
  const [priceChange, setPriceChange] = useState<{ amount: number; rate: number } | null>(null);
  const [volume, setVolume] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

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
      },
      timeScale: {
        borderColor: '#cccccc',
        timeVisible: true,
        secondsVisible: false,
      },
    });

    const lineSeries = chart.addLineSeries({
      color: '#2962ff',
      lineWidth: 2,
      crosshairMarkerVisible: true,
      crosshairMarkerRadius: 6,
    });

    chartRef.current = chart;
    seriesRef.current = lineSeries;

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
    const initializeWebSocket = async () => {
      try {
        setError(null);
        
        const kisClient = new KisClient(appkey, appsecret);
        kisClientRef.current = kisClient;

        const webSocket = new KisWebSocket(kisClient, stockCode);
        webSocketRef.current = webSocket;

        const handleData = (data: KisRealTimeData) => {
          if (!data.body.output || !seriesRef.current) return;

          const output = data.body.output;
          const price = parseFloat(output.STCK_PRPR);
          const changeAmount = parseFloat(output.PRDY_VRSS);
          const changeRate = parseFloat(output.PRDY_CTRT);
          const vol = parseInt(output.ACML_VOL);
          const time = Math.floor(Date.now() / 1000);

          if (!isNaN(price)) {
            setCurrentPrice(price);
            setPriceChange({ amount: changeAmount, rate: changeRate });
            setVolume(vol);

            const chartData: ChartData = {
              time,
              value: price
            };

            seriesRef.current.update(chartData);
          }
        };

        await webSocket.connect(handleData);
        setIsConnected(true);
        
      } catch (err) {
        console.error('웹소켓 초기화 실패:', err);
        setError(err instanceof Error ? err.message : '연결에 실패했습니다.');
        setIsConnected(false);
      }
    };

    if (appkey && appsecret && stockCode) {
      initializeWebSocket();
    }

    return () => {
      if (webSocketRef.current) {
        webSocketRef.current.disconnect();
      }
    };
  }, [appkey, appsecret, stockCode]);

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
          <h2 className={styles.stockName}>{stockName}</h2>
          <span className={styles.stockCode}>({stockCode})</span>
        </div>
        
        <div className={styles.statusIndicator}>
          <div className={`${styles.status} ${isConnected ? styles.connected : styles.disconnected}`}>
            {isConnected ? '실시간 연결됨' : '연결 안됨'}
          </div>
        </div>
      </div>

      {error && (
        <div className={styles.error}>
          {error}
        </div>
      )}

      <div className={styles.priceInfo}>
        {currentPrice !== null && (
          <div className={styles.currentPrice}>
            <span className={`${styles.price} ${getChangeColor()}`}>
              {formatNumber(currentPrice)}원
            </span>
            {priceChange && (
              <div className={`${styles.change} ${getChangeColor()}`}>
                <span>{priceChange.amount > 0 ? '+' : ''}{formatNumber(priceChange.amount)}</span>
                <span>({priceChange.rate > 0 ? '+' : ''}{priceChange.rate.toFixed(2)}%)</span>
              </div>
            )}
          </div>
        )}
        
        {volume !== null && (
          <div className={styles.volume}>
            거래량: {formatNumber(volume)}
          </div>
        )}
      </div>

      <div ref={chartContainerRef} className={styles.chartContainer} />
    </div>
  );
}