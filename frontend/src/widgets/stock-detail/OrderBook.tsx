import styles from './OrderBook.module.css';

interface OrderBookItem {
  price: number;
  quantity: number;
  size: number;
}

interface OrderBookData {
  asks: OrderBookItem[];
  bids: OrderBookItem[];
}

interface OrderBookProps {
  orderBook: OrderBookData;
  stockInfo: {
    currentPrice: number;
  };
}

export default function OrderBook({ orderBook, stockInfo }: OrderBookProps) {
  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  return (
    <div className={styles.orderBookSection}>
      <div className={styles.orderBookHeader}>
        <h3>호가</h3>
        <div className={styles.orderBookStats}>
          <span className={styles.bidTotal}>매수 총량: 134.99%</span>
          <span className={styles.askTotal}>매도 총량: 10.33%</span>
        </div>
      </div>
      
      <div className={styles.orderBook}>
        {/* 매도 호가 (상단) */}
        <div className={styles.asks}>
          {orderBook.asks.slice().reverse().map((ask, index) => (
            <div key={index} className={styles.orderItem}>
              <div className={styles.askBar} style={{ width: `${ask.size * 20}%` }}></div>
              <span className={styles.quantity}>{formatNumber(ask.quantity)}</span>
              <span className={styles.askPrice}>{formatNumber(ask.price)}</span>
              <span className={styles.size}>{ask.size}%</span>
            </div>
          ))}
        </div>

        {/* 현재가 */}
        <div className={styles.currentPriceRow}>
          <span className={styles.currentPrice}>{formatNumber(stockInfo.currentPrice)}</span>
          <span className={styles.spread}>±{formatNumber(200)}</span>
        </div>

        {/* 매수 호가 (하단) */}
        <div className={styles.bids}>
          {orderBook.bids.map((bid, index) => (
            <div key={index} className={styles.orderItem}>
              <div className={styles.bidBar} style={{ width: `${bid.size * 20}%` }}></div>
              <span className={styles.quantity}>{formatNumber(bid.quantity)}</span>
              <span className={styles.bidPrice}>{formatNumber(bid.price)}</span>
              <span className={styles.size}>{bid.size}%</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}