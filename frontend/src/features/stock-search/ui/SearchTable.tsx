import React from 'react';
import { useStockSearch } from '../hooks/useStockSearch';
import { useWatchlist, useToggleWatchlist, isWatchlistStock } from '../hooks/useWatchlist';
import WatchlistIcon from '../../../entities/stock/ui/WatchlistIcon';
import styles from './SearchTable.module.css';

interface SearchTableProps {
  searchQuery: string;
  onStockSelect: (stock: { code: string; name: string }) => void;
}

// 스켈레톤 플레이스홀더 행 컴포넌트
function SkeletonRow({ index }: { index: number }) {
  return (
    <div className={`${styles.stockRow} ${index % 2 === 0 ? styles.evenRow : ''} ${styles.skeletonRow}`}>
      <div className={styles.stockInfo}>
        <div className={`${styles.favoriteIcon} ${styles.skeletonIcon}`}></div>
        <div className={`${styles.stockName} ${styles.skeletonText}`}></div>
      </div>
    </div>
  );
}

export default function SearchTable({ searchQuery, onStockSelect }: SearchTableProps) {
  // 검색 훅 사용
  const { searchResults, isLoading, isEmpty, hasQuery } = useStockSearch(searchQuery, 1000);

  // 관심종목 관련 훅
  const { data: watchlistData } = useWatchlist();
  const toggleWatchlist = useToggleWatchlist();

  const handleToggleFavorite = (stockCode: string, event: React.MouseEvent) => {
    event.stopPropagation();
    console.log('🔄 SearchTable - 토글 시작:', stockCode);
    console.log('📋 SearchTable - 현재 watchlistData:', watchlistData);
    console.log('❤️ SearchTable - 현재 isFavorite:', isWatchlistStock(stockCode, watchlistData));
    toggleWatchlist.mutate(stockCode);
  };

  // 로딩 상태 (스켈레톤 UI)
  if (isLoading) {
    return (
      <div className={styles.stockTable}>
        <div className={styles.tableBody}>
          {Array.from({ length: 3 }, (_, index) => (
            <SkeletonRow key={`skeleton-${index}`} index={index} />
          ))}
        </div>
      </div>
    );
  }

  // 검색어가 없는 상태
  if (!hasQuery) {
    return (
      <div className={styles.stockTable}>
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
      </div>
    );
  }

  // 검색 결과가 없는 상태
  if (isEmpty) {
    return (
      <div className={styles.stockTable}>
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
      </div>
    );
  }

  // 검색 결과 표시
  return (
    <div className={styles.stockTable}>
      <div className={styles.tableBody}>
        {searchResults.map((stock, index) => (
          <div
            key={stock.code}
            className={`${styles.stockRow} ${index % 2 === 0 ? styles.evenRow : ''}`}
            onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
          >
            <div className={styles.stockInfo}>
              <WatchlistIcon
                isFavorite={isWatchlistStock(stock.code, watchlistData)}
                size={14}
                onClick={(e) => handleToggleFavorite(stock.code, e)}
                className={styles.favoriteIcon}
              />
              <span className={styles.stockName}>{stock.name}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
