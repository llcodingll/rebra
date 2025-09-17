import React, { useState, useMemo } from 'react';
import styles from './SearchTableWidget.module.css';
import { stockListData } from './stockListData';
import TableLayoutContainer from './components/TableLayoutContainer';

interface SearchTableWidgetProps {
  onStockSelect: (stock: { code: string; name: string }) => void;
}

export default function SearchTableWidget({ onStockSelect }: SearchTableWidgetProps) {
  const [searchQuery, setSearchQuery] = useState('');

  const filteredData = useMemo(() => {
    if (!searchQuery.trim()) return [];

    return stockListData
      .filter((stock) => {
        return stock.name.toLowerCase().includes(searchQuery.toLowerCase());
      })
      .slice(0, 10);
  }, [searchQuery]);

  const watchlistData = useMemo(() => {
    return stockListData.filter((stock) => stock.isFavorite);
  }, []);

  const toggleFavorite = (stockCode: string, event: React.MouseEvent) => {
    event.stopPropagation();
    // TODO: 실제 관심 종목 추가/제거 로직 구현
    console.log('Toggle favorite for:', stockCode);
  };

  const controls = (
    <div className={styles.controlContainer}>
      <div className={styles.leftSection}>
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
      <div className={styles.rightSection}>
        <span className={styles.sectionTitle}>관심 종목</span>
      </div>
    </div>
  );

  const table = (
    <div className={styles.splitTableContainer}>
      {/* 왼쪽 검색 테이블 */}
      <div className={styles.leftTable}>
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
            <div className={styles.tableBody}>
              {filteredData.map((stock, index) => (
                <div
                  key={stock.rank}
                  className={`${styles.stockRow} ${index % 2 === 0 ? styles.evenRow : ''}`}
                  onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
                >
                  <div className={styles.stockInfo}>
                    <div className={styles.favoriteIcon} onClick={(e) => toggleFavorite(stock.code, e)}>
                      {stock.isFavorite ? (
                        <svg width='14' height='14' viewBox='0 0 24 24' fill='#ef1515'>
                          <path d='M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z' />
                        </svg>
                      ) : (
                        <svg width='14' height='14' viewBox='0 0 24 24' fill='none' stroke='#999' strokeWidth='2'>
                          <path d='M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z' />
                        </svg>
                      )}
                    </div>
                    <span className={styles.stockName}>{stock.name}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* 오른쪽 관심 종목 테이블 */}
      <div className={styles.rightTable}>
        <div className={styles.stockTable}>
          {watchlistData.length === 0 ? (
            <div className={styles.emptyState}>
              <div className={styles.emptyIcon}>
                <svg width='48' height='48' viewBox='0 0 20 20' fill='none'>
                  <path
                    d='M10 15.27L16.18 19l-1.64-7.03L20 7.24l-7.19-.61L10 0 7.19 6.63 0 7.24l5.46 4.73L3.82 19z'
                    fill='#d1d5db'
                  />
                </svg>
              </div>
              <p className={styles.emptyText}>등록된 관심 종목이 없습니다</p>
            </div>
          ) : (
            <div className={styles.tableBody}>
              {watchlistData.map((stock, index) => (
                <div
                  key={stock.rank}
                  className={`${styles.stockRow} ${index % 2 === 0 ? styles.evenRow : ''}`}
                  onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
                >
                  <div className={styles.stockInfo}>
                    <div className={styles.favoriteIcon} onClick={(e) => toggleFavorite(stock.code, e)}>
                      <svg width='14' height='14' viewBox='0 0 24 24' fill='#ef1515'>
                        <path d='M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z' />
                      </svg>
                    </div>
                    <span className={styles.stockName}>{stock.name}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );

  return <TableLayoutContainer controls={controls} table={table} />;
}
