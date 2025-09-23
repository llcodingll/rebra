import React, { useState, useEffect, useMemo } from 'react';
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

  // 실시간 주식 데이터 연동
  const stockCode = symbol || '005930';
  const { stockInfo, realtimePrice, orderbook, isConnected, isLoading, error, subscriptionStatus, disconnect, reconnect } =
    useRealtimeStock(stockCode);

  // 차트 데이터에서 현재 가격 정보 가져오기 (일봉 기준)
  const { data: infiniteData } = useInfiniteChartData(stockCode, 'daily', true);
  const chartApiData = useMemo(() => {
    return mergeInfiniteChartData(infiniteData?.pages);
  }, [infiniteData?.pages]);

  // 실시간 가격 업데이트 시 주문가격도 업데이트
  useEffect(() => {
    if (realtimePrice?.currentPrice) {
      setOrderPrice(realtimePrice.currentPrice);
    }
  }, [realtimePrice]);

  // 차트 데이터 로드 시 초기 주문가격 설정
  useEffect(() => {
    if (chartApiData?.summary?.currentPrice && !realtimePrice?.currentPrice) {
      setOrderPrice(Number(chartApiData.summary.currentPrice));
    }
  }, [chartApiData, realtimePrice]);

  // 실제 주식 정보 (차트 API 데이터 우선, 실시간 데이터는 보조) + SearchPage에서 전달받은 정보 우선 사용
  const displayStockInfo =
    chartApiData || stockInfo
      ? {
          code: stockInfoFromState?.stockCode || chartApiData?.stockCode || stockInfo?.stockCode || stockCode,
          name: stockInfoFromState?.stockName || chartApiData?.stockName || stockInfo?.stockName || '로딩 중...',
          currentPrice: realtimePrice?.currentPrice || Number(chartApiData?.summary?.currentPrice) || 0,
          change: realtimePrice?.change || Number(chartApiData?.summary?.priceChange) || 0,
          changePercent: realtimePrice?.changePercent || Number(chartApiData?.summary?.changeRate) || 0,
          prevClose:
            (realtimePrice?.currentPrice || Number(chartApiData?.summary?.currentPrice) || 0) -
            (realtimePrice?.change || Number(chartApiData?.summary?.priceChange) || 0),
          volume: realtimePrice?.volume || Number(chartApiData?.summary?.volume) || 0,
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
      {!isDevMode() && (
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
            realTimePrice={realtimePrice?.currentPrice || Number(chartApiData?.summary?.currentPrice) || null}
            realTimePriceChange={
              realtimePrice
                ? {
                    amount: realtimePrice.change,
                    rate: realtimePrice.changePercent,
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
          <HoldingInfoTable
            stockCode={stockCode}
            currentPrice={safeStockInfo.currentPrice}
          />
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

        <OrderBook
          orderBook={{
            // KRX 호가 데이터 더미
            MKSC_SHRN_ISCD: stockCode,
            BSOP_HOUR: '150000',
            HOUR_CLS_CODE: '0',
            // 기준 가격 (현재가가 0이면 기본값 사용)
            ...((): any => {
              const basePrice = safeStockInfo.currentPrice || 71400; // 기본값 71,400원
              return {
                // 매도 호가 (10개)
                ASKP1: basePrice + 100,
                ASKP2: basePrice + 200,
                ASKP3: basePrice + 300,
                ASKP4: basePrice + 400,
                ASKP5: basePrice + 500,
                ASKP6: basePrice + 600,
                ASKP7: basePrice + 700,
                ASKP8: basePrice + 800,
                ASKP9: basePrice + 900,
                ASKP10: basePrice + 1000,
                // 매수 호가 (10개)
                BIDP1: basePrice - 100,
                BIDP2: basePrice - 200,
                BIDP3: basePrice - 300,
                BIDP4: basePrice - 400,
                BIDP5: basePrice - 500,
                BIDP6: basePrice - 600,
                BIDP7: basePrice - 700,
                BIDP8: basePrice - 800,
                BIDP9: basePrice - 900,
                BIDP10: basePrice - 1000,
              };
            })(),
            // 매도 호가 잔량 (10개)
            ASKP_RSQN1: 125430,
            ASKP_RSQN2: 234567,
            ASKP_RSQN3: 156789,
            ASKP_RSQN4: 89432,
            ASKP_RSQN5: 234123,
            ASKP_RSQN6: 167543,
            ASKP_RSQN7: 98234,
            ASKP_RSQN8: 187432,
            ASKP_RSQN9: 234567,
            ASKP_RSQN10: 134256,
            // 매수 호가 잔량 (10개)
            BIDP_RSQN1: 187654,
            BIDP_RSQN2: 134567,
            BIDP_RSQN3: 98432,
            BIDP_RSQN4: 176543,
            BIDP_RSQN5: 123456,
            BIDP_RSQN6: 198765,
            BIDP_RSQN7: 87432,
            BIDP_RSQN8: 156789,
            BIDP_RSQN9: 234567,
            BIDP_RSQN10: 98234,
            // 총 잔량
            TOTAL_ASKP_RSQN: 1664235,
            TOTAL_BIDP_RSQN: 1431987,
            OVTM_TOTAL_ASKP_RSQN: 0,
            OVTM_TOTAL_BIDP_RSQN: 0,
            // 예상 체결 정보
            ANTC_CNPR: safeStockInfo.currentPrice,
            ANTC_CNQN: 123456,
            ANTC_VOL: 987654,
            ANTC_CNTG_VRSS: 100,
            ANTC_CNTG_VRSS_SIGN: '2',
            ANTC_CNTG_PRDY_CTRT: 1.23,
            ACML_VOL: safeStockInfo.volume,
            TOTAL_ASKP_RSQN_ICDC: 12345,
            TOTAL_BIDP_RSQN_ICDC: -6789,
            OVTM_TOTAL_ASKP_ICDC: 0,
            OVTM_TOTAL_BIDP_ICDC: 0,
            STCK_DEAL_CLS_CODE: '1',
          }}
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
