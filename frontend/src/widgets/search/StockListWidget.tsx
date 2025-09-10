import { useState } from 'react';
import styles from './StockListWidget.module.css';
import { stockListData } from './stockListData';
import Pagination from '../common/Pagination';

interface StockListWidgetProps {
  onStockSelect: (stockCode: string) => void;
}

export default function StockListWidget({ onStockSelect }: StockListWidgetProps) {
  const [currentPage, setCurrentPage] = useState(2);
  const totalPages = 26;


  return (
    <div className={styles.stockListWidget}>
      {/* 페이지네이션 - 테이블 위로 이동 */}
      <div className={styles.paginationContainer}>
        <Pagination 
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
          showFirstLast={true}
          showNumbers={true}
        />
      </div>

      {/* 구분선 - 페이지네이션 아래, 테이블 위에 위치 */}
      <div className={styles.divider}></div>

      {/* 주식 테이블 */}
      <div className={styles.stockTable}>
        {/* 테이블 헤더 */}
        <div className={styles.tableHeader}>
          <span className={styles.headerRank}>종목</span>
          <span className={styles.headerPrice}>현재가</span>
          <span className={styles.headerChange}>등락률</span>
          <span className={styles.headerVolume}>거래대금</span>
        </div>

        {/* 테이블 바디 */}
        <div className={styles.tableBody}>
          {stockListData.map((stock, index) => (
            <div
              key={stock.rank}
              className={`${styles.stockRow} ${index % 2 === 1 ? styles.evenRow : ''}`}
              onClick={() => onStockSelect(stock.code)}
            >
              <div className={styles.stockInfo}>
                <div className={styles.favoriteIcon}>
                  {stock.isFavorite ? (
                    <svg width="16" height="16" viewBox="0 0 20 20" fill="#ef1515">
                      <path d="M10 15.27L16.18 19l-1.64-7.03L20 7.24l-7.19-.61L10 0 7.19 6.63 0 7.24l5.46 4.73L3.82 19z"/>
                    </svg>
                  ) : (
                    <svg width="16" height="16" viewBox="0 0 20 20" fill="#666">
                      <path d="M10 15.27L16.18 19l-1.64-7.03L20 7.24l-7.19-.61L10 0 7.19 6.63 0 7.24l5.46 4.73L3.82 19z"/>
                    </svg>
                  )}
                </div>
                <span className={styles.rank}>{stock.rank}</span>
                <span className={styles.stockName}>{stock.name}</span>
              </div>
              
              <div className={styles.price}>
                {stock.price.toLocaleString()}
              </div>
              
              <div className={`${styles.change} ${stock.changePercent ? styles.positive : styles.negative}`}>
                {stock.changePercent ? '+' : ''}{stock.change}%
              </div>
              
              <div className={styles.volume}>
                {stock.volume}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}