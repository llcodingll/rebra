import styles from './OrderBook.module.css';
import { KRXRealtimeOrderbookMessage } from '../../features/stock-detail/api/types';

interface OrderBookProps {
  orderBook: KRXRealtimeOrderbookMessage | null;
  stockInfo: {
    currentPrice: number;
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

export default function OrderBook({ orderBook, stockInfo }: OrderBookProps) {
  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  // 연속적인 20개 호가 데이터 생성
  const generateOrderBookRows = (): OrderBookRow[] => {
    if (!orderBook) return [];

    // 매도 호가 (ASKP10 → ASKP1 순서로 상단부터)
    const askPrices = [
      { price: orderBook.ASKP10, quantity: orderBook.ASKP_RSQN10 },
      { price: orderBook.ASKP9, quantity: orderBook.ASKP_RSQN9 },
      { price: orderBook.ASKP8, quantity: orderBook.ASKP_RSQN8 },
      { price: orderBook.ASKP7, quantity: orderBook.ASKP_RSQN7 },
      { price: orderBook.ASKP6, quantity: orderBook.ASKP_RSQN6 },
      { price: orderBook.ASKP5, quantity: orderBook.ASKP_RSQN5 },
      { price: orderBook.ASKP4, quantity: orderBook.ASKP_RSQN4 },
      { price: orderBook.ASKP3, quantity: orderBook.ASKP_RSQN3 },
      { price: orderBook.ASKP2, quantity: orderBook.ASKP_RSQN2 },
      { price: orderBook.ASKP1, quantity: orderBook.ASKP_RSQN1 },
    ];

    // 매수 호가 (BIDP1 → BIDP10 순서로 하단부터)
    const bidPrices = [
      { price: orderBook.BIDP1, quantity: orderBook.BIDP_RSQN1 },
      { price: orderBook.BIDP2, quantity: orderBook.BIDP_RSQN2 },
      { price: orderBook.BIDP3, quantity: orderBook.BIDP_RSQN3 },
      { price: orderBook.BIDP4, quantity: orderBook.BIDP_RSQN4 },
      { price: orderBook.BIDP5, quantity: orderBook.BIDP_RSQN5 },
      { price: orderBook.BIDP6, quantity: orderBook.BIDP_RSQN6 },
      { price: orderBook.BIDP7, quantity: orderBook.BIDP_RSQN7 },
      { price: orderBook.BIDP8, quantity: orderBook.BIDP_RSQN8 },
      { price: orderBook.BIDP9, quantity: orderBook.BIDP_RSQN9 },
      { price: orderBook.BIDP10, quantity: orderBook.BIDP_RSQN10 },
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
        {/* 메인 호가 테이블 */}
        <div className={styles.orderBookTable}>
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
                <div className={`${styles.priceCell} ${isCurrentPrice ? styles.currentPriceHighlight : ''}`}>
                  <div className={`${styles.price} ${row.type === 'ask' ? styles.askPrice : styles.bidPrice}`}>
                    {formatNumber(row.price)}
                  </div>
                  {stockInfo.currentPrice > 0 && (
                    <div
                      className={`${styles.changeRate} ${
                        row.type === 'ask' ? styles.askChangeRate : styles.bidChangeRate
                      }`}
                    >
                      {row.type === 'ask' ? '+' : ''}
                      {(((row.price - stockInfo.currentPrice) / stockInfo.currentPrice) * 100).toFixed(2)}%
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
