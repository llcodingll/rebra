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

  // 가격 변동에 따른 색상 클래스 결정
  const getPriceChangeClass = (amount: number) => {
    if (amount > 0) return styles.priceChangePositive;
    if (amount < 0) return styles.priceChangeNegative;
    return styles.priceChangeNeutral;
  };

  // 실제 사용할 가격 변동 데이터
  const changeAmount = realTimePriceChange ? realTimePriceChange.amount : stockInfo.change;
  const changeRate = realTimePriceChange ? realTimePriceChange.rate : stockInfo.changePercent;

  return (
    <div className={styles.stockBasicInfo}>
      <div className={styles.stockTitle}>
        <h1 className={styles.stockName}>{stockInfo.name}</h1>
        <span className={styles.stockCode}>{stockInfo.code}</span>
      </div>
      <div className={styles.priceInfo}>
        <span className={styles.currentPrice}>{formatPrice(realTimePrice || stockInfo.currentPrice)}</span>
        <div className={`${styles.priceChange} ${getPriceChangeClass(changeAmount)}`}>
          <span className={styles.changeAmount}>
            {changeAmount > 0 ? '+' : ''}
            {formatNumber(changeAmount)}원
          </span>
          <span className={styles.changePercent}>
            ({changeRate > 0 ? '+' : ''}
            {(changeRate ?? 0).toFixed(2)}%)
          </span>
        </div>
      </div>
    </div>
  );
}
