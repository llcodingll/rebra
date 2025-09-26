import React, { useState } from 'react';
import styles from './SearchTableWidget.module.css';
import TableLayoutContainer from './components/TableLayoutContainer';
import SearchTable from '../../features/stock-search/ui/SearchTable';
import { useWatchlist, useToggleWatchlist, isWatchlistStock } from '../../features/stock-search/hooks/useWatchlist';
import WatchlistIcon from '../../entities/stock/ui/WatchlistIcon';

interface SearchTableWidgetProps {
  onStockSelect: (stock: { code: string; name: string }) => void;
}

export default function SearchTableWidget({ onStockSelect }: SearchTableWidgetProps) {
  const [searchQuery, setSearchQuery] = useState('');

  // 관심종목 API 연동
  const { data: watchlistData = [], isLoading: isWatchlistLoading, error: watchlistError } = useWatchlist();
  const toggleWatchlist = useToggleWatchlist();

  const toggleFavorite = (stockCode: string, event: React.MouseEvent) => {
    event.stopPropagation();
    toggleWatchlist.mutate(stockCode);
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
        <SearchTable searchQuery={searchQuery} onStockSelect={onStockSelect} />
      </div>

      {/* 오른쪽 관심 종목 테이블 */}
      <div className={styles.rightTable}>
        <div className={styles.stockTable}>
          {/* 로딩 상태 */}
          {isWatchlistLoading && (
            <div className={styles.emptyState}>
              <p className={styles.emptyText}>관심 종목을 불러오는 중...</p>
            </div>
          )}

          {/* 에러 상태 */}
          {watchlistError && (
            <div className={styles.emptyState}>
              <p className={styles.emptyText}>관심 종목을 불러올 수 없습니다</p>
            </div>
          )}

          {/* 데이터 표시 */}
          {!isWatchlistLoading && !watchlistError && (
            <>
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
                      key={stock.stockCode}
                      className={`${styles.stockRow} ${index % 2 === 0 ? styles.evenRow : ''}`}
                      onClick={() => onStockSelect({ code: stock.stockCode, name: stock.stockName })}
                    >
                      <div className={styles.stockInfo}>
                        <WatchlistIcon
                          isFavorite={isWatchlistStock(stock.stockCode, watchlistData)}
                          size={16}
                          onClick={(e) => toggleFavorite(stock.stockCode, e)}
                          className={styles.favoriteIcon}
                        />
                        <span className={styles.stockName}>{stock.stockName}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );

  return <TableLayoutContainer controls={controls} table={table} />;
}
