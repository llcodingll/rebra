import { useState, useRef, useEffect } from 'react';
import styles from './RankingTableWidget.module.css';
import TableLayoutContainer from './components/TableLayoutContainer';
import { useStockRanking, type RankingType } from '../../features/stock-search/hooks/useStockRanking';

interface RankingTableWidgetProps {
  onStockSelect: (stock: { code: string; name: string }) => void;
}

export default function RankingTableWidget({ onStockSelect }: RankingTableWidgetProps) {
  const [sortType, setSortType] = useState<RankingType>('volume');
  const buttonsContainerRef = useRef<HTMLDivElement>(null);
  const underlineRef = useRef<HTMLDivElement>(null);

  // 선택된 탭에 따른 실제 API 데이터 조회
  const { rankingData, isLoading, error } = useStockRanking(sortType);

  // API 데이터를 최대 10개로 제한
  const sortedData = rankingData.slice(0, 10);

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
        {isLoading ? (
          <div className={styles.loadingMessage}>로딩 중...</div>
        ) : error ? (
          <div className={styles.errorMessage}>데이터를 불러올 수 없습니다.</div>
        ) : sortedData.length > 0 ? (
          sortedData.map((stock, index) => (
            <div
              key={stock.stockCode}
              className={`${styles.stockRow} ${index % 2 === 0 ? styles.evenRow : ''}`}
              onClick={() => onStockSelect({ code: stock.stockCode, name: stock.stockName })}
            >
              <div className={styles.stockInfo}>
                <div className={styles.favoriteIcon}>
                  {/* 임시로 모든 종목을 비관심종목으로 표시 - 나중에 관심종목 API 연동 시 수정 예정 */}
                  <svg width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='#999' strokeWidth='2'>
                    <path d='M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z' />
                  </svg>
                </div>
                <div className={styles.rank}>{stock.rank}</div>
                <span className={styles.stockName}>{stock.stockName}</span>
              </div>

              <div className={styles.price}>{stock.currentPrice.toLocaleString()}</div>

              <div className={`${styles.change} ${stock.priceChangeRate >= 0 ? styles.positive : styles.negative}`}>
                {stock.priceChangeRate >= 0 ? '+' : ''}
                {stock.priceChangeRate.toFixed(1)}%
              </div>

              <div className={styles.volume}>{stock.volume.toLocaleString()}</div>
            </div>
          ))
        ) : (
          <div className={styles.emptyMessage}>데이터가 없습니다.</div>
        )}
      </div>
    </div>
  );

  return <TableLayoutContainer controls={controls} table={table} />;
}
