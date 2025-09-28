import { useEffect, useRef } from 'react';
import styles from './OrderBook.module.css';
import LoadingSpinner from '../../shared/ui/LoadingSpinner';
import { OptimizedOrderbookData } from '../../features/stock-detail/api/types';

interface OrderBookProps {
  orderBook: OptimizedOrderbookData | null;
  stockInfo: {
    currentPrice: number;
    prevClose: number;
    high52?: number;
    low52?: number;
    upperLimit?: number;
    lowerLimit?: number;
    openPrice?: number;
    highPrice?: number;
    lowPrice?: number;
    volume?: number;
    volumeRate?: number;
  };
  onPriceClick?: (price: number) => void;
  isLoading?: boolean;
}

interface OrderBookRow {
  price: number;
  askQuantity: number | null;
  bidQuantity: number | null;
  type: 'ask' | 'bid';
}

interface TradeHistoryItem {
  price: number;
  quantity: number;
  type: 'buy' | 'sell';
  time: string;
}

export default function OrderBook({ orderBook, stockInfo, onPriceClick, isLoading = false }: OrderBookProps) {
  // 가격 비교 함수 - 전일종가 대비 색상 결정
  const getPriceColorClass = (price: number, prevClose: number) => {
    if (price > prevClose) return styles.priceUp;
    if (price < prevClose) return styles.priceDown;
    return styles.priceEqual;
  };
  const orderBookTableRef = useRef<HTMLDivElement>(null);
  const hasScrolledToCenter = useRef(false);

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  // 연속적인 20개 호가 데이터 생성
  const generateOrderBookRows = (): OrderBookRow[] => {
    if (!orderBook) return [];

    // 매도 호가 (askp10 → askp1 순서로 상단부터)
    const askPrices = [
      { price: orderBook.askp10, quantity: orderBook.askpRsqn10 },
      { price: orderBook.askp9, quantity: orderBook.askpRsqn9 },
      { price: orderBook.askp8, quantity: orderBook.askpRsqn8 },
      { price: orderBook.askp7, quantity: orderBook.askpRsqn7 },
      { price: orderBook.askp6, quantity: orderBook.askpRsqn6 },
      { price: orderBook.askp5, quantity: orderBook.askpRsqn5 },
      { price: orderBook.askp4, quantity: orderBook.askpRsqn4 },
      { price: orderBook.askp3, quantity: orderBook.askpRsqn3 },
      { price: orderBook.askp2, quantity: orderBook.askpRsqn2 },
      { price: orderBook.askp1, quantity: orderBook.askpRsqn1 },
    ];

    // 매수 호가 (bidp1 → bidp10 순서로 하단부터)
    const bidPrices = [
      { price: orderBook.bidp1, quantity: orderBook.bidpRsqn1 },
      { price: orderBook.bidp2, quantity: orderBook.bidpRsqn2 },
      { price: orderBook.bidp3, quantity: orderBook.bidpRsqn3 },
      { price: orderBook.bidp4, quantity: orderBook.bidpRsqn4 },
      { price: orderBook.bidp5, quantity: orderBook.bidpRsqn5 },
      { price: orderBook.bidp6, quantity: orderBook.bidpRsqn6 },
      { price: orderBook.bidp7, quantity: orderBook.bidpRsqn7 },
      { price: orderBook.bidp8, quantity: orderBook.bidpRsqn8 },
      { price: orderBook.bidp9, quantity: orderBook.bidpRsqn9 },
      { price: orderBook.bidp10, quantity: orderBook.bidpRsqn10 },
    ];

    // 모든 가격을 모아서 정렬 (0보다 큰 값만)
    const allPrices = new Set<number>();

    askPrices.forEach((ask) => {
      if (ask.price > 0) allPrices.add(ask.price);
    });

    bidPrices.forEach((bid) => {
      if (bid.price > 0) allPrices.add(bid.price);
    });

    // 가격 내림차순 정렬
    const sortedPrices = Array.from(allPrices).sort((a, b) => b - a);

    // 각 가격에 대해 매도/매수 잔량 맵핑
    const askMap = new Map<number, number>();
    const bidMap = new Map<number, number>();

    askPrices.forEach((ask) => {
      if (ask.price > 0) askMap.set(ask.price, ask.quantity);
    });

    bidPrices.forEach((bid) => {
      if (bid.price > 0) bidMap.set(bid.price, bid.quantity);
    });

    // 최종 행 데이터 생성
    const rows: OrderBookRow[] = sortedPrices.map((price) => {
      const askQuantity = askMap.get(price) || null;
      const bidQuantity = bidMap.get(price) || null;

      // 매도 잔량이 있으면 ask, 매수 잔량이 있으면 bid
      const type: 'ask' | 'bid' = askQuantity ? 'ask' : 'bid';

      return {
        price,
        askQuantity,
        bidQuantity,
        type,
      };
    });

    return rows;
  };

  // 현재가와 가장 가까운 호가 찾기
  const findCurrentPriceRowIndex = (rows: OrderBookRow[], currentPrice: number): number => {
    if (rows.length === 0 || currentPrice <= 0) return -1;

    let closestIndex = -1;
    let minDiff = Infinity;

    rows.forEach((row, index) => {
      const diff = Math.abs(row.price - currentPrice);
      if (diff < minDiff) {
        minDiff = diff;
        closestIndex = index;
      }
    });

    return closestIndex;
  };

  // 잔량 최대값 계산 (시각화 바 위해)
  const getMaxQuantity = (rows: OrderBookRow[]): number => {
    let max = 0;
    rows.forEach((row) => {
      if (row.askQuantity && row.askQuantity > max) max = row.askQuantity;
      if (row.bidQuantity && row.bidQuantity > max) max = row.bidQuantity;
    });
    return max;
  };

  const orderBookRows = generateOrderBookRows();
  const currentPriceRowIndex = findCurrentPriceRowIndex(orderBookRows, stockInfo.currentPrice);
  const maxQuantity = getMaxQuantity(orderBookRows);

  // 초기 로드 시에만 현재가를 중심으로 스크롤 위치 조정
  useEffect(() => {
    if (orderBookTableRef.current && currentPriceRowIndex >= 0 && !hasScrolledToCenter.current) {
      const container = orderBookTableRef.current;
      const rowHeight = 40; // CSS에서 설정한 .orderRow 높이
      const containerHeight = container.clientHeight;

      // 현재가 행이 컨테이너 중앙에 오도록 스크롤 위치 계산
      const targetScrollTop = currentPriceRowIndex * rowHeight - containerHeight / 2 + rowHeight / 2;

      container.scrollTop = Math.max(0, targetScrollTop);

      // 한 번 스크롤했음을 표시
      hasScrolledToCenter.current = true;
    }
  }, [currentPriceRowIndex]);

  // if (!orderBook) {
  //   return (
  //     <div className={styles.orderBookSection}>
  //       <div className={styles.orderBookHeader}>
  //         <h3>호가</h3>
  //       </div>
  //       <div className={styles.orderBookContent}>
  //         <div className={styles.noDataMessage}>호가 데이터를 불러오는 중...</div>
  //       </div>
  //     </div>
  //   );
  // }

  return (
    <div className={styles.orderBookSection}>
      <div className={styles.orderBookHeader}>
        <h3>호가</h3>
      </div>

      <div className={styles.orderBookContent}>
        {isLoading && (
          <div className={styles.loadingOverlay}>
            <LoadingSpinner size="medium" />
          </div>
        )}
        {/* 메인 호가 테이블 */}
        <div ref={orderBookTableRef} className={styles.orderBookTable}>
          {orderBookRows.map((row, index) => {
            const isCurrentPrice = index === currentPriceRowIndex;

            // 잔량 비율 계산
            const askQuantityPercent = row.askQuantity && maxQuantity > 0 ? (row.askQuantity / maxQuantity) * 100 : 0;
            const bidQuantityPercent = row.bidQuantity && maxQuantity > 0 ? (row.bidQuantity / maxQuantity) * 100 : 0;

            return (
              <div key={`row-${index}`} className={styles.orderRow}>
                {/* 좌측: 매도 잔량 */}
                <div className={styles.askQuantityCell}>
                  {row.askQuantity && row.askQuantity > 0 && (
                    <>
                      <div
                        className={styles.askQuantityBar}
                        style={{ width: `calc(${askQuantityPercent}% - 8px)` }}
                      ></div>
                      <span className={styles.askQuantityText}>{formatNumber(row.askQuantity)}</span>
                    </>
                  )}
                </div>

                {/* 가운데: 가격 */}
                <div
                  className={`${styles.priceCell} ${isCurrentPrice ? styles.currentPriceHighlight : ''} ${
                    onPriceClick ? styles.clickable : ''
                  }`}
                  onClick={() => onPriceClick?.(row.price)}
                >
                  <div className={`${styles.price} ${getPriceColorClass(row.price, stockInfo.prevClose)}`}>
                    {formatNumber(row.price)}
                  </div>
                  {stockInfo.prevClose > 0 && (
                    <div className={`${styles.changeRate} ${getPriceColorClass(row.price, stockInfo.prevClose)}`}>
                      {row.price > stockInfo.prevClose ? '+' : ''}
                      {(((row.price - stockInfo.prevClose) / stockInfo.prevClose) * 100).toFixed(2)}%
                    </div>
                  )}
                </div>

                {/* 우측: 매수 잔량 */}
                <div className={styles.bidQuantityCell}>
                  {row.bidQuantity && row.bidQuantity > 0 && (
                    <>
                      <div
                        className={styles.bidQuantityBar}
                        style={{ width: `calc(${bidQuantityPercent}% - 8px)` }}
                      ></div>
                      <span className={styles.bidQuantityText}>{formatNumber(row.bidQuantity)}</span>
                    </>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
