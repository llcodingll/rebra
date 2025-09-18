import React from 'react';
import { useStockSearch } from '../hooks/useStockSearch';
import styles from './SearchTable.module.css';

interface SearchTableProps {
  searchQuery: string;
  onStockSelect: (stock: { code: string; name: string }) => void;
  onToggleFavorite?: (stockCode: string, event: React.MouseEvent) => void;
  favoriteStockCodes?: string[]; // 관심종목 코드 목록 (선택적)
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

export default function SearchTable({
  searchQuery,
  onStockSelect,
  onToggleFavorite,
  favoriteStockCodes,
}: SearchTableProps) {
  // 검색 훅 사용
  const { searchResults, isLoading, isEmpty, hasQuery } = useStockSearch(searchQuery, 1000, favoriteStockCodes);
  const handleToggleFavorite = (stockCode: string, event: React.MouseEvent) => {
    event.stopPropagation();
    onToggleFavorite?.(stockCode, event);
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
              <div className={styles.favoriteIcon} onClick={(e) => handleToggleFavorite(stock.code, e)}>
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
    </div>
  );
}
