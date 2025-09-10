import { useState } from 'react';
import styles from './RankingTableWidget.module.css';
import { stockListData } from './stockListData';
import TableLayoutContainer from './components/TableLayoutContainer';

interface RankingTableWidgetProps {
  onStockSelect: (stockCode: string) => void;
}

type SortType = 'volume' | 'change' | 'price';

export default function RankingTableWidget({ onStockSelect }: RankingTableWidgetProps) {
  const [sortType, setSortType] = useState<SortType>('volume');

  const sortedData = [...stockListData].sort((a, b) => {
    switch (sortType) {
      case 'volume':
        return parseFloat(b.volume.replace(/[^\d.-]/g, '')) - parseFloat(a.volume.replace(/[^\d.-]/g, ''));
      case 'change':
        return Math.abs(b.change) - Math.abs(a.change);
      case 'price':
        return b.price - a.price;
      default:
        return 0;
    }
  });

  const controls = (
    <div className={styles.controlContainer}>
      <div className={styles.sortButtons}>
        <button
          className={`${styles.sortButton} ${sortType === 'volume' ? styles.active : ''}`}
          onClick={() => setSortType('volume')}
        >
          거래량순
        </button>
        <button
          className={`${styles.sortButton} ${sortType === 'change' ? styles.active : ''}`}
          onClick={() => setSortType('change')}
        >
          등락률순
        </button>
        <button
          className={`${styles.sortButton} ${sortType === 'price' ? styles.active : ''}`}
          onClick={() => setSortType('price')}
        >
          현재가순
        </button>
      </div>
    </div>
  );

  const table = (
    <div className={styles.stockTable}>
      {/* 테이블 헤더 */}
      <div className={styles.tableHeader}>
        <span className={styles.headerRank}>순위</span>
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
            <div className={styles.rank}>{index + 1}</div>

            <div className={styles.stockInfo}>
              <div className={styles.favoriteIcon}>
                {stock.isFavorite ? (
                  <svg width='16' height='16' viewBox='0 0 20 20' fill='#ef1515'>
                    <path d='M10 15.27L16.18 19l-1.64-7.03L20 7.24l-7.19-.61L10 0 7.19 6.63 0 7.24l5.46 4.73L3.82 19z' />
                  </svg>
                ) : (
                  <svg width='16' height='16' viewBox='0 0 20 20' fill='#666'>
                    <path d='M10 15.27L16.18 19l-1.64-7.03L20 7.24l-7.19-.61L10 0 7.19 6.63 0 7.24l5.46 4.73L3.82 19z' />
                  </svg>
                )}
              </div>
              <span className={styles.stockName}>{stock.name}</span>
            </div>

            <div className={styles.price}>{stock.price.toLocaleString()}</div>

            <div className={`${styles.change} ${stock.changePercent ? styles.positive : styles.negative}`}>
              {stock.changePercent ? '+' : ''}
              {stock.change}%
            </div>

            <div className={styles.volume}>{stock.volume}</div>
          </div>
        ))}
      </div>
    </div>
  );

  return <TableLayoutContainer controls={controls} table={table} />;
}
