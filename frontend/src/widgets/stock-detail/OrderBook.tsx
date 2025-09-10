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

  // 임시 데이터 (실제로는 props로 받아야 함)
  const tradeInfo = {
    high: 99800,
    low: 48400, 
    volume: 68800,
    value: 69800,
    previousClose: 68500,
    totalVolume: 1063950525,
    foreignRatio: 73.50,
    institutionalRatio: 69450
  };

  return (
    <div className={styles.orderBookSection}>
      <div className={styles.orderBookHeader}>
        <h3>호가</h3>
      </div>
      
      <div className={styles.orderBookContent}>
        {/* 우측 상단 거래정보 박스 */}
        <div className={styles.tradeInfoBox}>
          <div className={styles.tradeInfoItem}>
            <span className={styles.infoLabel}>상승VI</span>
            <span className={styles.infoValue}>-</span>
          </div>
          <div className={styles.tradeInfoItem}>
            <span className={styles.infoLabel}>하향VI</span>
            <span className={styles.infoValue}>-</span>
          </div>
          <div className={styles.tradeInfoItem}>
            <span className={styles.infoLabel}>시작</span>
            <span className={styles.infoValue}>{formatNumber(tradeInfo.volume)}</span>
          </div>
          <div className={styles.tradeInfoItem}>
            <span className={styles.infoLabel}>최고</span>
            <span className={styles.infoValue}>{formatNumber(tradeInfo.value)}</span>
          </div>
          <div className={styles.tradeInfoItem}>
            <span className={styles.infoLabel}>최저</span>
            <span className={styles.infoValue}>{formatNumber(tradeInfo.previousClose)}</span>
          </div>
          <div className={styles.tradeInfoItem}>
            <span className={styles.infoLabel}>거래량</span>
            <span className={styles.infoValue}>{formatNumber(tradeInfo.totalVolume)}</span>
          </div>
          <div className={styles.tradeInfoItem}>
            <span className={styles.infoLabel}>외인비다</span>
            <span className={styles.infoValue}>{tradeInfo.foreignRatio}%</span>
          </div>
          <div className={styles.tradeInfoItem}>
            <span className={styles.infoLabel}>증권사</span>
            <span className={styles.infoValue}>-</span>
          </div>
        </div>

        {/* 좌측 하단 체결강도 박스 */}
        <div className={styles.strengthBox}>
          <div className={styles.strengthLabel}>체결강도</div>
          <div className={styles.strengthValue}>228.00%</div>
        </div>

        {/* 메인 호가 테이블 (스크롤 가능) */}
        <div className={styles.orderBookTable}>
          {/* 매도 호가 (전체) */}
          {orderBook.asks.slice().reverse().map((ask, index) => (
            <div key={`ask-${index}`} className={styles.orderRow}>
              <div className={styles.askQuantityCell}>{formatNumber(ask.quantity)}</div>
              <div className={styles.priceCell}>
                <div className={styles.price}>{formatNumber(ask.price)}</div>
                <div className={styles.changeRate}>+{((ask.price - stockInfo.currentPrice) / stockInfo.currentPrice * 100).toFixed(2)}%</div>
              </div>
              <div className={styles.emptyCell}></div>
            </div>
          ))}
          
          {/* 현재가 행 */}
          <div className={styles.currentPriceRow}>
            <div className={styles.emptyCell}></div>
            <div className={styles.currentPriceCell}>
              <span className={styles.currentIcon}>ㅁ</span>
              <span className={styles.currentPrice}>{formatNumber(stockInfo.currentPrice)}</span>
            </div>
            <div className={styles.currentVolumeCell}>394</div>
          </div>
          
          {/* 매수 호가 (전체) */}
          {orderBook.bids.map((bid, index) => (
            <div key={`bid-${index}`} className={styles.orderRow}>
              <div className={styles.emptyCell}></div>
              <div className={styles.priceCell}>
                <div className={styles.price}>{formatNumber(bid.price)}</div>
                <div className={styles.changeRate}>-{((stockInfo.currentPrice - bid.price) / stockInfo.currentPrice * 100).toFixed(2)}%</div>
              </div>
              <div className={styles.bidQuantityCell}>{formatNumber(bid.quantity)}</div>
            </div>
          ))}
        </div>
      </div>
      
    </div>
  );
}