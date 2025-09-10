import styles from './StockBasicInfo.module.css';

interface StockBasicInfoProps {
  stockInfo: {
    code: string;
    name: string;
    currentPrice: number;
    change: number;
    changePercent: number;
  };
  realTimePrice?: number | null;
  realTimePriceChange?: { amount: number; rate: number } | null;
}

export default function StockBasicInfo({ stockInfo, realTimePrice, realTimePriceChange }: StockBasicInfoProps) {
  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const formatPrice = (price: number) => {
    return `${formatNumber(price)}원`;
  };

  return (
    <div className={styles.stockBasicInfo}>
      <div className={styles.stockTitle}>
        <h1 className={styles.stockName}>{stockInfo.name}</h1>
        <span className={styles.stockCode}>{stockInfo.code}</span>
      </div>
      <div className={styles.priceInfo}>
        <span className={styles.currentPrice}>{formatPrice(realTimePrice || stockInfo.currentPrice)}</span>
        <div className={styles.priceChange}>
          {realTimePriceChange ? (
            <>
              <span className={styles.changeAmount}>
                {realTimePriceChange.amount > 0 ? '+' : ''}
                {formatNumber(realTimePriceChange.amount)}원
              </span>
              <span className={styles.changePercent}>
                ({realTimePriceChange.rate > 0 ? '+' : ''}
                {realTimePriceChange.rate.toFixed(2)}%)
              </span>
            </>
          ) : (
            <>
              <span className={styles.changeAmount}>+{formatNumber(stockInfo.change)}원</span>
              <span className={styles.changePercent}>(+{stockInfo.changePercent}%)</span>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
