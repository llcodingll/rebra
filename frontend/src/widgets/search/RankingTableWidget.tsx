import { useState, useRef, useEffect } from 'react';
import styles from './RankingTableWidget.module.css';
import { stockListData } from './stockListData';
import TableLayoutContainer from './components/TableLayoutContainer';

interface RankingTableWidgetProps {
  onStockSelect: (stockCode: string) => void;
}

type SortType = 'volume' | 'rising' | 'falling';

export default function RankingTableWidget({ onStockSelect }: RankingTableWidgetProps) {
  const [sortType, setSortType] = useState<SortType>('volume');
  const buttonsContainerRef = useRef<HTMLDivElement>(null);
  const underlineRef = useRef<HTMLDivElement>(null);

  const sortedData = [...stockListData]
    .sort((a, b) => {
      switch (sortType) {
        case 'volume':
          return parseFloat(b.volume.replace(/[^\d.-]/g, '')) - parseFloat(a.volume.replace(/[^\d.-]/g, ''));
        case 'rising':
          // 급상승: 양수 변화율만, 큰 순서대로
          const aRising = a.change > 0 ? a.change : -Infinity;
          const bRising = b.change > 0 ? b.change : -Infinity;
          return bRising - aRising;
        case 'falling':
          // 급하락: 음수 변화율만, 절댓값 큰 순서대로
          const aFalling = a.change < 0 ? Math.abs(a.change) : -Infinity;
          const bFalling = b.change < 0 ? Math.abs(b.change) : -Infinity;
          return bFalling - aFalling;
        default:
          return 0;
      }
    })
    .slice(0, 10);

  useEffect(() => {
    const moveUnderline = () => {
      if (!buttonsContainerRef.current || !underlineRef.current) return;

      const activeButton = buttonsContainerRef.current.querySelector(`.${styles.active}`) as HTMLElement;
      if (!activeButton) return;

      const containerRect = buttonsContainerRef.current.getBoundingClientRect();
      const buttonRect = activeButton.getBoundingClientRect();

      const left = buttonRect.left - containerRect.left;
      const width = buttonRect.width;

      underlineRef.current.style.transform = `translateX(${left}px)`;
      underlineRef.current.style.width = `${width}px`;
    };

    moveUnderline();
  }, [sortType]);

  const controls = (
    <div className={styles.controlContainer}>
      <div className={styles.sortButtons} ref={buttonsContainerRef}>
        <button
          className={`${styles.sortButton} ${sortType === 'volume' ? styles.active : ''}`}
          onClick={() => setSortType('volume')}
        >
          거래량
        </button>
        <button
          className={`${styles.sortButton} ${sortType === 'rising' ? styles.active : ''}`}
          onClick={() => setSortType('rising')}
        >
          급상승
        </button>
        <button
          className={`${styles.sortButton} ${sortType === 'falling' ? styles.active : ''}`}
          onClick={() => setSortType('falling')}
        >
          급하락
        </button>
        <div className={styles.underline} ref={underlineRef}></div>
      </div>
    </div>
  );

  const table = (
    <div className={styles.stockTable}>
      {/* 테이블 헤더 */}
      <div className={styles.tableHeader}>
        <span className={styles.headerStock}>종목</span>
        <span className={styles.headerPrice}>현재가</span>
        <span className={styles.headerChange}>등락률</span>
        <span className={styles.headerVolume}>거래량</span>
      </div>

      {/* 테이블 바디 */}
      <div className={styles.tableBody}>
        {sortedData.map((stock, index) => (
          <div
            key={stock.rank}
            className={`${styles.stockRow} ${index % 2 === 0 ? styles.evenRow : ''}`}
            onClick={() => onStockSelect(stock.code)}
          >
            <div className={styles.stockInfo}>
              <div className={styles.favoriteIcon}>
                {stock.isFavorite ? (
                  <svg width='16' height='16' viewBox='0 0 24 24' fill='#ef1515'>
                    <path d='M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z' />
                  </svg>
                ) : (
                  <svg width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='#999' strokeWidth='2'>
                    <path d='M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z' />
                  </svg>
                )}
              </div>
              <div className={styles.rank}>{index + 1}</div>
              <span className={styles.stockName}>{stock.name}</span>
            </div>

            <div className={styles.price}>{stock.price.toLocaleString()}</div>

            <div className={`${styles.change} ${stock.changePercent ? styles.positive : styles.negative}`}>
              {stock.changePercent ? '+' : ''}
              {stock.change.toFixed(1)}%
            </div>

            <div className={styles.volume}>{stock.volume}</div>
          </div>
        ))}
      </div>
    </div>
  );

  return <TableLayoutContainer controls={controls} table={table} />;
}
