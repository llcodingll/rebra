import React, { useState } from 'react';
import { useParams } from 'react-router';
import RealTimeChart from '../../widgets/stock-detail/RealTimeChart';
import StockBasicInfo from '../../widgets/stock-detail/StockBasicInfo';
import HoldingInfoTable from '../../widgets/stock-detail/HoldingInfoTable';
import OrderBook from '../../widgets/stock-detail/OrderBook';
import OrderForm from '../../widgets/stock-detail/OrderForm';
import { useRealtimeStock } from '../../features/stock-search/model/useRealtimeStock';
import { isDevMode } from '../../features/stock-search/lib/mockData';
import styles from './StockDetailPage.module.css';

interface OrderBookItem {
  price: number;
  quantity: number;
  size: number;
}

export default function StockDetailPage() {
  const { symbol } = useParams<{ symbol: string }>();
  const [quantity, setQuantity] = useState(0);
  const [selectedRatio, setSelectedRatio] = useState<number | null>(null);
  const [orderPrice, setOrderPrice] = useState(71400);

  // 실시간 주식 데이터 연동
  const stockCode = symbol || '005930';
  const { stockInfo, realtimePrice, orderbook, isConnected, isLoading, error } = useRealtimeStock(stockCode);

  // 실시간 가격 업데이트 시 주문가격도 업데이트
  React.useEffect(() => {
    if (realtimePrice?.currentPrice) {
      setOrderPrice(realtimePrice.currentPrice);
    }
  }, [realtimePrice]);

  // 실제 주식 정보 (실시간 데이터 기반) + 안전한 기본값
  const displayStockInfo = stockInfo
    ? {
        code: stockInfo.stockCode,
        name: stockInfo.stockName,
        currentPrice: realtimePrice?.currentPrice || 0,
        change: realtimePrice?.change || 0,
        changePercent: realtimePrice?.changePercent || 0,
        prevClose: (realtimePrice?.currentPrice || 0) - (realtimePrice?.change || 0),
        volume: realtimePrice?.volume || 0,
        amount: 0, // API에서 제공되지 않으면 기본값
        high: 0, // API에서 제공되지 않으면 기본값
        low: 0, // API에서 제공되지 않으면 기본값
      }
    : null;

  // 안전한 주식 정보 (null 체크 완료)
  const safeStockInfo = displayStockInfo || {
    code: stockCode,
    name: '로딩 중...',
    currentPrice: 0,
    change: 0,
    changePercent: 0,
    prevClose: 0,
    volume: 0,
    amount: 0,
    high: 0,
    low: 0,
  };

  // 보유 현황 데이터
  const holdingData = {
    buyPrice: 54747,
    profitLoss: 166530,
    profitRate: 31.24,
    buyAmount: 547470,
    evaluationAmount: 714000,
    holdingQuantity: 10,
    availableQuantity: 10,
    fee: 1234,
    tax: 1234,
  };

  // 실시간 호가 데이터 (fallback 포함)
  const displayOrderBook = orderbook
    ? {
        asks: orderbook.asks.map((item) => ({
          price: item.price,
          quantity: item.quantity,
          size: item.size ?? 0, // size가 없으면 0으로 기본값 설정
        })),
        bids: orderbook.bids.map((item) => ({
          price: item.price,
          quantity: item.quantity,
          size: item.size ?? 0, // size가 없으면 0으로 기본값 설정
        })),
      }
    : {
        asks: [
          { price: 72000, quantity: 119417, size: 1.34 },
          { price: 71800, quantity: 329778, size: 3.68 },
          { price: 71600, quantity: 244413, size: 2.73 },
          { price: 71400, quantity: 181658, size: 2.03 },
          { price: 71200, quantity: 187845, size: 2.1 },
        ],
        bids: [
          { price: 71000, quantity: 114635, size: 1.28 },
          { price: 70800, quantity: 19452, size: 0.22 },
          { price: 70600, quantity: 329778, size: 3.68 },
          { price: 70400, quantity: 244413, size: 2.73 },
          { price: 70200, quantity: 181658, size: 2.03 },
        ],
      };

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const formatPrice = (price: number) => {
    return `${formatNumber(price)}원`;
  };

  const handleOrderSubmit = () => {
    console.log('주문 제출:', { stockCode, quantity, orderPrice });
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
  if (error && !isDevMode()) {
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
  if (error && !isDevMode() && !isLoading && !stockInfo) {
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
      {/* 연결 상태 및 개발 모드 표시 */}
      <div className={styles.connectionStatus}>
        <div className={styles.statusLeft}>
          {isDevMode() ? (
            <span className={styles.devMode}>
              🛠️ 개발 모드 | 목업 데이터
            </span>
          ) : (
            <span className={isConnected ? styles.connected : styles.disconnected}>
              {isConnected ? '🟢 실시간 연결됨' : '🔴 연결 중...'}
            </span>
          )}
          {error && isDevMode() && (
            <span className={styles.devError}>
              ⚠️ {error}
            </span>
          )}
        </div>
        <div className={styles.statusRight}>
          <span className={styles.stockCode}>
            {safeStockInfo.name} ({safeStockInfo.code})
          </span>
        </div>
      </div>

      {/* 주식 정보 및 보유 현황 섹션 */}
      <div className={styles.stockInfoSection}>
        <div className={styles.stockBasicInfoWrapper}>
          <StockBasicInfo
            stockInfo={safeStockInfo}
            realTimePrice={realtimePrice?.currentPrice || null}
            realTimePriceChange={
              realtimePrice
                ? {
                    amount: realtimePrice.change,
                    rate: realtimePrice.changePercent,
                  }
                : null
            }
          />
        </div>

        <div className={styles.holdingInfoWrapper}>
          <HoldingInfoTable holdingData={holdingData} />
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
            onPriceUpdate={(price, change) => {
              // 실시간 차트에서 오는 업데이트는 이제 사용하지 않음 (STOMP로 대체)
              setOrderPrice(price);
            }}
          />
        </div>

        <OrderBook orderBook={displayOrderBook} stockInfo={safeStockInfo} />

        <OrderForm
          orderPrice={orderPrice}
          onPriceAdjust={handlePriceAdjust}
          onQuantityChange={setQuantity}
          onRatioSelect={handleRatioSelect}
          onOrderSubmit={handleOrderSubmit}
          quantity={quantity}
          selectedRatio={selectedRatio}
        />
      </div>
    </div>
  );
}
