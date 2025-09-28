import React, { useState, useEffect, useMemo, useRef } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import RealTimeChart from '../../widgets/stock-detail/RealTimeChart';
import StockBasicInfo from '../../widgets/stock-detail/StockBasicInfo';
import HoldingInfoTable from '../../features/stock-detail/ui/HoldingInfoTable';
import OrderBook from '../../widgets/stock-detail/OrderBook';
import OrderFormContainer from '../../widgets/stock-detail/order/OrderFormContainer';
import { useRealtimeStock } from '../../features/stock-detail/model/useRealtimeStock';
import { useInfiniteChartData, mergeInfiniteChartData } from '../../features/stock-detail/hooks/useInfiniteChartData';
import { useStockHolding } from '../../features/stock-detail/hooks/useStockHolding';
import { useOrderBookFallback } from '../../features/stock-detail/hooks/useOrderBookFallback';
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
  const [orderBookClickedPrice, setOrderBookClickedPrice] = useState<number | undefined>(undefined);

  // SearchPage에서 전달받은 종목 정보
  const stockInfoFromState = location.state as { stockCode: string; stockName: string } | null;

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

  // console.log(holdingData);
  // 차트 데이터에서 현재 가격 정보 가져오기 (일봉 기준)
  const { data: infiniteData, isLoading: isChartLoading } = useInfiniteChartData(stockCode, 'daily', true);
  const chartApiData = useMemo(() => {
    return mergeInfiniteChartData(infiniteData?.pages);
  }, [infiniteData?.pages]);

  // 현재가 계산 (실시간 데이터 우선, 차트 데이터 차순위, 데이터 없으면 0)
  const hasRealData = !!(realtimePrice?.stckPrpr || chartApiData?.summary?.currentPrice);
  const currentPrice = hasRealData ? realtimePrice?.stckPrpr || Number(chartApiData?.summary?.currentPrice) : 0;

  // 보유 정보 조회
  const { holdingData, isLoading: isHoldingLoading, refetch: refetchHolding } = useStockHolding(stockCode, currentPrice);

  // 디버깅: 로딩 상태 확인
  console.log('=== StockDetailPage 디버깅 ===');
  console.log('stockCode:', stockCode);
  console.log('currentPrice:', currentPrice);
  console.log('holdingData:', holdingData);
  console.log('isHoldingLoading:', isHoldingLoading);

  // 호가 데이터 폴백 (REST API + 웹소켓 조합)
  const { orderbook: fallbackOrderbook, isLoading: isOrderbookLoading } = useOrderBookFallback(stockCode, orderbook);

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

  // 전일 종가 계산 (현재가 - 전일대비)
  const prevClose = safeStockInfo.currentPrice - safeStockInfo.change;

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const formatPrice = (price: number) => {
    return `${formatNumber(price)}원`;
  };

  const handleOrderBookPriceClick = (price: number) => {
    // 호가창 클릭 시 가격을 주문 폼에 전달
    setOrderBookClickedPrice(price);
    // 잘은 시간 후 초기화 (다음 클릭이 작동하도록)
    setTimeout(() => {
      setOrderBookClickedPrice(undefined);
    }, 100);
  };

  // 로딩 상태 처리
  // if (isLoading) {
  //   return (
  //     <div className={styles.container}>
  //       <div className={styles.loadingState}>
  //         <div className={styles.loadingSpinner}></div>
  //         <p>주식 정보를 불러오는 중...</p>
  //       </div>
  //     </div>
  //   );
  // }

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
          <HoldingInfoTable
            holdingData={holdingData}
            currentPrice={safeStockInfo.currentPrice}
            isLoading={isHoldingLoading}
          />
        </div>
      </div>

      {/* 메인 콘텐츠 (3열 레이아웃) */}
      <div className={styles.mainContent}>
        {/* 좌측: 차트 */}
        <div className={styles.chartSection}>
          <RealTimeChart stockCode={safeStockInfo.code} stockName={safeStockInfo.name} realtimeData={realtimePrice} />
        </div>

        <OrderBook
          orderBook={fallbackOrderbook}
          stockInfo={{
            currentPrice: safeStockInfo.currentPrice || (hasRealData ? 0 : 71400), // 실제 데이터 있으면 0, 없으면 기본값
            prevClose: prevClose || 71400, // 전일 종가
            high52: 79800, // 임시 데이터 - 실제로는 API에서 가져와야 함
            low52: 49900,
            upperLimit: safeStockInfo.currentPrice ? Math.floor(safeStockInfo.currentPrice * 1.3) : 71400, // 상한가
            lowerLimit: safeStockInfo.currentPrice ? Math.floor(safeStockInfo.currentPrice * 0.7) : 71400, // 하한가
            openPrice: safeStockInfo.currentPrice || (hasRealData ? 0 : 71400),
            highPrice: safeStockInfo.high || safeStockInfo.currentPrice || (hasRealData ? 0 : 71400),
            lowPrice: safeStockInfo.low || safeStockInfo.currentPrice || (hasRealData ? 0 : 71400),
            volume: safeStockInfo.volume,
            volumeRate: 41.09, // 임시 데이터
          }}
          onPriceClick={handleOrderBookPriceClick}
        />

        <OrderFormContainer
          stockCode={stockCode}
          stockName={safeStockInfo.name}
          holdingData={holdingData}
          currentPrice={currentPrice}
          orderBookClickedPrice={orderBookClickedPrice}
          onRefreshHolding={refetchHolding}
        />
      </div>
    </div>
  );
}
