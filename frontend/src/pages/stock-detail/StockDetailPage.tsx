import React, { useState, useEffect, useMemo, useRef } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import RealTimeChart from '../../widgets/stock-detail/RealTimeChart';
import StockBasicInfo from '../../widgets/stock-detail/StockBasicInfo';
import HoldingInfoTable from '../../features/stock-detail/ui/HoldingInfoTable';
import OrderBook from '../../widgets/stock-detail/OrderBook';
import OrderFormContainer from '../../widgets/stock-detail/order/OrderFormContainer';
import { useRealtimeStock } from '../../features/stock-detail/model/useRealtimeStock';
import { useInfiniteChartData, mergeInfiniteChartData } from '../../features/stock-detail/hooks/useInfiniteChartData';
import { isDevMode } from '../../features/stock-detail/lib/mockData';
import styles from './StockDetailPage.module.css';

interface OrderBookItem {
  price: number;
  quantity: number;
  size: number;
}

export default function StockDetailPage() {
  const { symbol } = useParams<{ symbol: string }>();
  const location = useLocation();

  // SearchPage에서 전달받은 종목 정보
  const stockInfoFromState = location.state as { stockCode: string; stockName: string } | null;
  const [quantity, setQuantity] = useState(0);
  const [selectedRatio, setSelectedRatio] = useState<number | null>(null);
  const [orderPrice, setOrderPrice] = useState(71400);
  const isPriceInitialized = useRef(false);

  // 실시간 주식 데이터 연동
  const stockCode = symbol || '005930';
  const {
    stockInfo,
    realtimePrice,
    orderbook,
    isConnected,
    isLoading,
    error,
    subscriptionStatus,
    disconnect,
    reconnect,
  } = useRealtimeStock(stockCode);

  // 차트 데이터에서 현재 가격 정보 가져오기 (일봉 기준)
  const { data: infiniteData } = useInfiniteChartData(stockCode, 'daily', true);
  const chartApiData = useMemo(() => {
    return mergeInfiniteChartData(infiniteData?.pages);
  }, [infiniteData?.pages]);

  // 초기 한 번만 가격 설정 (실시간 데이터나 차트 데이터 중 먼저 로드되는 것으로 설정)
  useEffect(() => {
    if (!isPriceInitialized.current) {
      if (realtimePrice?.stckPrpr) {
        setOrderPrice(realtimePrice.stckPrpr);
        isPriceInitialized.current = true;
      } else if (chartApiData?.summary?.currentPrice) {
        setOrderPrice(Number(chartApiData.summary.currentPrice));
        isPriceInitialized.current = true;
      }
    }
  }, [realtimePrice, chartApiData]);



  // 실제 주식 정보 (차트 API 데이터 우선, 실시간 데이터는 보조) + SearchPage에서 전달받은 정보 우선 사용
  const displayStockInfo =
    chartApiData || stockInfo
      ? {
          code: stockInfoFromState?.stockCode || chartApiData?.stockCode || stockInfo?.stockCode || stockCode,
          name: stockInfoFromState?.stockName || chartApiData?.stockName || stockInfo?.stockName || '로딩 중...',
          currentPrice: realtimePrice?.stckPrpr || Number(chartApiData?.summary?.currentPrice) || 0,
          change: realtimePrice?.prdyVrss || Number(chartApiData?.summary?.priceChange) || 0,
          changePercent: realtimePrice?.prdyCtrt || Number(chartApiData?.summary?.changeRate) || 0,
          prevClose:
            (realtimePrice?.stckPrpr || Number(chartApiData?.summary?.currentPrice) || 0) -
            (realtimePrice?.prdyVrss || Number(chartApiData?.summary?.priceChange) || 0),
          volume: realtimePrice?.acmlVol || Number(chartApiData?.summary?.volume) || 0,
          amount: 0, // API에서 제공되지 않으면 기본값
          high: 0, // API에서 제공되지 않으면 기본값
          low: 0, // API에서 제공되지 않으면 기본값
        }
      : null;

  // 안전한 주식 정보 (null 체크 완료) + SearchPage에서 전달받은 정보로 폴백
  const safeStockInfo = displayStockInfo || {
    code: stockInfoFromState?.stockCode || stockCode,
    name: stockInfoFromState?.stockName || '로딩 중...',
    currentPrice: 0,
    change: 0,
    changePercent: 0,
    prevClose: 0,
    volume: 0,
    amount: 0,
    high: 0,
    low: 0,
  };

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const formatPrice = (price: number) => {
    return `${formatNumber(price)}원`;
  };

  const handlePriceAdjust = (direction: 'up' | 'down') => {
    const step = 100;
    setOrderPrice((prev) => (direction === 'up' ? prev + step : Math.max(prev - step, 0)));
  };

  const handleRatioSelect = (ratio: number) => {
    setSelectedRatio(ratio);
    // 임시로 계산된 수량 (실제로는 보유 자금 기준으로 계산)
    const maxAffordable = Math.floor(1000000 / orderPrice);
    setQuantity(Math.floor(maxAffordable * (ratio / 100)));
  };

  const handleOrderBookPriceClick = (price: number) => {
    setOrderPrice(price);
  };

  // 로딩 상태 처리
  if (isLoading) {
    return (
      <div className={styles.container}>
        <div className={styles.loadingState}>
          <div className={styles.loadingSpinner}>⏳</div>
          <p>📊 주식 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  // 에러 상태 처리 (개발 모드가 아닐 때만)
  if (error && isDevMode()) {
    return (
      <div className={styles.container}>
        <div className={styles.errorState}>
          <div className={styles.errorIcon}>❌</div>
          <p>{error}</p>
          <button onClick={() => window.location.reload()} className={styles.retryButton}>
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  // 심각한 에러가 있고 개발 모드가 아닐 때만 에러 화면 표시
  if (error && isDevMode() && !isLoading && !stockInfo) {
    return (
      <div className={styles.container}>
        <div className={styles.noDataState}>
          <p>📈 주식 정보를 불러올 수 없습니다</p>
          <button onClick={() => window.location.reload()} className={styles.retryButton}>
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      {/* STOMP 연결 디버깅 패널 */}
      {isDevMode() && (
        <div className={styles.debugPanel}>
          <div className={styles.debugHeader}>
            <h3>🔌 STOMP 연결 상태</h3>
            <div className={styles.debugControls}>
              <button onClick={disconnect} className={styles.disconnectBtn} disabled={!isConnected}>
                연결 해제
              </button>
            </div>
          </div>

          <div className={styles.debugContent}>
            <div className={styles.debugRow}>
              <span className={styles.debugLabel}>STOMP 연결:</span>
              <span className={isConnected ? styles.statusOk : styles.statusError}>
                {isConnected ? '✅ 연결됨' : '❌ 연결 안됨'}
              </span>
            </div>

            <div className={styles.debugRow}>
              <span className={styles.debugLabel}>일괄 구독 요청:</span>
              <span className={subscriptionStatus.requested ? styles.statusOk : styles.statusError}>
                {subscriptionStatus.requested ? '✅ 요청됨' : '❌ 요청 안됨'}
              </span>
            </div>

            <div className={styles.debugRow}>
              <span className={styles.debugLabel}>구독 성공:</span>
              <span className={subscriptionStatus.successful ? styles.statusOk : styles.statusError}>
                {subscriptionStatus.successful ? '✅ 성공' : '❌ 실패'}
              </span>
            </div>

            <div className={styles.debugRow}>
              <span className={styles.debugLabel}>로딩 상태:</span>
              <span className={styles.debugValue}>{isLoading ? '⏳ 로딩 중...' : '✅ 완료'}</span>
            </div>

            {subscriptionStatus.lastUpdate && (
              <div className={styles.debugRow}>
                <span className={styles.debugLabel}>마지막 업데이트:</span>
                <span className={styles.debugValue}>
                  {new Date(subscriptionStatus.lastUpdate).toLocaleTimeString()}
                </span>
              </div>
            )}

            {error && (
              <div className={styles.debugRow}>
                <span className={styles.debugLabel}>오류:</span>
                <span className={styles.statusError}>{error}</span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* 주식 정보 및 보유 현황 섹션 */}
      <div className={styles.stockInfoSection}>
        <div className={styles.stockBasicInfoWrapper}>
          <StockBasicInfo
            stockInfo={safeStockInfo}
            realTimePrice={realtimePrice?.stckPrpr || Number(chartApiData?.summary?.currentPrice) || null}
            realTimePriceChange={
              realtimePrice
                ? {
                    amount: realtimePrice.prdyVrss,
                    rate: realtimePrice.prdyCtrt,
                  }
                : chartApiData?.summary
                ? {
                    amount: Number(chartApiData.summary.priceChange),
                    rate: Number(chartApiData.summary.changeRate),
                  }
                : null
            }
          />
        </div>

        <div className={styles.holdingInfoWrapper}>
          <HoldingInfoTable stockCode={stockCode} currentPrice={safeStockInfo.currentPrice} />
        </div>
      </div>

      {/* 메인 콘텐츠 (3열 레이아웃) */}
      <div className={styles.mainContent}>
        {/* 좌측: 차트 */}
        <div className={styles.chartSection}>
          <RealTimeChart
            stockCode={safeStockInfo.code}
            stockName={safeStockInfo.name}
            realtimeData={realtimePrice}
          />
        </div>

        <OrderBook
          orderBook={orderbook}
          stockInfo={{
            currentPrice: safeStockInfo.currentPrice || 71400, // 기본값 확보
            high52: 79800, // 임시 데이터 - 실제로는 API에서 가져와야 함
            low52: 49900,
            upperLimit: Math.floor(safeStockInfo.currentPrice * 1.3), // 상한가 (30% 상승)
            lowerLimit: Math.floor(safeStockInfo.currentPrice * 0.7), // 하한가 (30% 하락)
            openPrice: safeStockInfo.currentPrice,
            highPrice: safeStockInfo.high || safeStockInfo.currentPrice,
            lowPrice: safeStockInfo.low || safeStockInfo.currentPrice,
            volume: safeStockInfo.volume,
            volumeRate: 41.09, // 임시 데이터
          }}
          onPriceClick={handleOrderBookPriceClick}
        />

        <OrderFormContainer
          stockCode={stockCode}
          orderPrice={orderPrice}
          onPriceChange={setOrderPrice}
          onPriceAdjust={handlePriceAdjust}
          onQuantityChange={setQuantity}
          onRatioSelect={handleRatioSelect}
          quantity={quantity}
          selectedRatio={selectedRatio}
        />
      </div>
    </div>
  );
}
