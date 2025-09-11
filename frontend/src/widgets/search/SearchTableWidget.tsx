import { useState, useMemo } from 'react';
import styles from './SearchTableWidget.module.css';
import { stockListData } from './stockListData';
import TableLayoutContainer from './components/TableLayoutContainer';

interface SearchTableWidgetProps {
  onStockSelect: (stockCode: string) => void;
}

export default function SearchTableWidget({ onStockSelect }: SearchTableWidgetProps) {
  const [searchQuery, setSearchQuery] = useState('');

  const filteredData = useMemo(() => {
    if (!searchQuery.trim()) return [];

    return stockListData.filter((stock) => {
      return stock.name.toLowerCase().includes(searchQuery.toLowerCase());
    });
  }, [searchQuery]);

  const controls = (
    <div className={styles.controlContainer}>
      <div className={styles.searchInputContainer}>
        <input
          type='text'
          placeholder='종목명을 입력하세요'
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className={styles.searchInput}
        />
        <div className={styles.searchIcon}>
          <svg width='20' height='20' viewBox='0 0 24 24' fill='none'>
            <path
              d='M21 21l-4.35-4.35M19 11a8 8 0 1 1-16 0 8 8 0 0 1 16 0z'
              stroke='#6b7280'
              strokeWidth='2'
              strokeLinecap='round'
              strokeLinejoin='round'
            />
          </svg>
        </div>
      </div>
    </div>
  );

  const table = (
    <div className={styles.stockTable}>
      {searchQuery.trim() === '' ? (
        <div className={styles.emptyState}>
          <div className={styles.emptyIcon}>
            <svg width='48' height='48' viewBox='0 0 24 24' fill='none'>
              <path
                d='M21 21l-4.35-4.35M19 11a8 8 0 1 1-16 0 8 8 0 0 1 16 0z'
                stroke='#d1d5db'
                strokeWidth='2'
                strokeLinecap='round'
                strokeLinejoin='round'
              />
            </svg>
          </div>
          <p className={styles.emptyText}>검색어를 입력해주세요</p>
        </div>
      ) : filteredData.length === 0 ? (
        <div className={styles.emptyState}>
          <div className={styles.emptyIcon}>
            <svg width='48' height='48' viewBox='0 0 24 24' fill='none'>
              <circle cx='12' cy='12' r='10' stroke='#d1d5db' strokeWidth='2' />
              <line x1='15' y1='9' x2='9' y2='15' stroke='#d1d5db' strokeWidth='2' />
              <line x1='9' y1='9' x2='15' y2='15' stroke='#d1d5db' strokeWidth='2' />
            </svg>
          </div>
          <p className={styles.emptyText}>검색 결과가 없습니다</p>
        </div>
      ) : (
        <>
          {/* 테이블 헤더 */}
          <div className={styles.tableHeader}>
            <span className={styles.headerStock}>종목</span>
            <span className={styles.headerPrice}>현재가</span>
            <span className={styles.headerChange}>등락률</span>
            <span className={styles.headerVolume}>거래대금</span>
          </div>

          {/* 테이블 바디 */}
          <div className={styles.tableBody}>
            {filteredData.map((stock, index) => (
              <div
                key={stock.rank}
                className={`${styles.stockRow} ${index % 2 === 0 ? styles.evenRow : ''}`}
                onClick={() => onStockSelect(stock.code)}
              >
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
        </>
      )}
    </div>
  );

  return <TableLayoutContainer controls={controls} table={table} />;
}
