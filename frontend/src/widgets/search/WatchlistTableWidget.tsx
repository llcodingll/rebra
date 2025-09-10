import React, { useState, useMemo } from 'react';
import styles from './WatchlistTableWidget.module.css';
import { stockListData } from './stockListData';
import TableLayoutContainer from './components/TableLayoutContainer';

interface WatchlistTableWidgetProps {
  onStockSelect: (stockCode: string) => void;
}

export default function WatchlistTableWidget({ onStockSelect }: WatchlistTableWidgetProps) {
  const [showAll, setShowAll] = useState(false);

  const watchlistData = useMemo(() => {
    const favorites = stockListData.filter((stock) => stock.isFavorite);
    if (showAll) {
      return stockListData;
    }
    return favorites;
  }, [showAll]);

  const favoriteCount = stockListData.filter((stock) => stock.isFavorite).length;

  const toggleFavorite = (stockCode: string, event: React.MouseEvent) => {
    event.stopPropagation();
    // TODO: 실제 관심 종목 추가/제거 로직 구현
    console.log('Toggle favorite for:', stockCode);
  };

  const controls = (
    <div className={styles.controlContainer}>
      <div className={styles.controlLeft}>
        <div className={styles.statsInfo}>
          <span className={styles.favoriteCount}>관심 종목 {favoriteCount}개</span>
        </div>
        <div className={styles.viewButtons}>
          <button className={`${styles.viewButton} ${!showAll ? styles.active : ''}`} onClick={() => setShowAll(false)}>
            관심 종목만
          </button>
          <button className={`${styles.viewButton} ${showAll ? styles.active : ''}`} onClick={() => setShowAll(true)}>
            전체 종목
          </button>
        </div>
      </div>
      <div className={styles.controlRight}>
        <button className={styles.manageButton}>
          <svg width='16' height='16' viewBox='0 0 24 24' fill='none'>
            <path
              d='M12 5v14M5 12h14'
              stroke='currentColor'
              strokeWidth='2'
              strokeLinecap='round'
              strokeLinejoin='round'
            />
          </svg>
          관심 종목 추가
        </button>
      </div>
    </div>
  );

  const table = (
    <div className={styles.stockTable}>
      {watchlistData.length === 0 && !showAll ? (
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
          <button className={styles.addButton} onClick={() => setShowAll(true)}>
            종목 추가하기
          </button>
        </div>
      ) : (
        <>
          {/* 테이블 헤더 */}
          <div className={styles.tableHeader}>
            <span className={styles.headerStock}>종목</span>
            <span className={styles.headerPrice}>현재가</span>
            <span className={styles.headerChange}>등락률</span>
            <span className={styles.headerVolume}>거래대금</span>
            <span className={styles.headerAction}>관리</span>
          </div>

          {/* 테이블 바디 */}
          <div className={styles.tableBody}>
            {watchlistData.map((stock, index) => (
              <div
                key={stock.rank}
                className={`${styles.stockRow} ${index % 2 === 0 ? styles.evenRow : ''}`}
                onClick={() => onStockSelect(stock.code)}
              >
                <div className={styles.stockInfo}>
                  <div className={styles.favoriteIcon} onClick={(e) => toggleFavorite(stock.code, e)}>
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

                <div className={styles.actionButton}>
                  <button
                    className={styles.removeButton}
                    onClick={(e) => toggleFavorite(stock.code, e)}
                    title={stock.isFavorite ? '관심 종목에서 제거' : '관심 종목에 추가'}
                  >
                    {stock.isFavorite ? '제거' : '추가'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );

  return <TableLayoutContainer controls={controls} table={table} />;
}
