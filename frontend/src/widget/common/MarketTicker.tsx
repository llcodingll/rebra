import styles from './MarketTicker.module.css';

export default function MarketTicker() {
  const marketData = [
    {
      name: 'KOSPI',
      value: '2,486.669',
      change: '-8.45',
      changePercent: '(-0.31%)',
      isNegative: true
    },
    {
      name: 'KOSDAQ',
      value: '742.308',
      change: '-3.39',
      changePercent: '(-0.73%)',
      isNegative: true
    },
    {
      name: 'KOSPI200',
      value: '338.582',
      change: '-3.94',
      changePercent: '(-0.90%)',
      isNegative: true
    },
    {
      name: 'USD/KRW',
      value: '1,347.836',
      change: '-5.12',
      changePercent: '(-0.36%)',
      isNegative: true
    }
  ];

  return (
    <div className={styles.ticker}>
      <div className={styles.container}>
        <div className={styles.scrollContainer}>
          {marketData.map((item, index) => (
            <div key={index} className={styles.tickerItem}>
              <div className={styles.itemName}>{item.name}</div>
              <div className={styles.itemValue}>{item.value}</div>
              <div className={styles.changeContainer}>
                <div className={styles.changeIcon}>
                  <svg viewBox="0 0 11 6" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9.75 5.375L6.03125 1.65625L3.84375 3.84375L1 1" stroke="#E7000B" strokeWidth="0.875" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </div>
                <div className={styles.changeText}>
                  <span className={styles.changeValue}>{item.change}</span>
                  <span className={styles.changePercent}>{item.changePercent}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
        <div className={styles.updateInfo}>
          • 10초마다 자동 업데이트
        </div>
      </div>
    </div>
  );
}