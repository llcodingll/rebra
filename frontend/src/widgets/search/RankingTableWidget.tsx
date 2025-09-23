import { useState, useRef, useEffect } from 'react';
import styles from './RankingTableWidget.module.css';
import TableLayoutContainer from './components/TableLayoutContainer';
import { useStockRanking } from '../../features/stock-search/hooks/useStockRanking';
import { transformVolumeRankingResults } from '../../features/stock-search/api/stockSearchApi';

interface RankingTableWidgetProps {
  onStockSelect: (stock: { code: string; name: string }) => void;
}

type SortType = 'volume' | 'rising' | 'falling';

export default function RankingTableWidget({ onStockSelect }: RankingTableWidgetProps) {
  const [sortType, setSortType] = useState<SortType>('volume');
  const buttonsContainerRef = useRef<HTMLDivElement>(null);
  const underlineRef = useRef<HTMLDivElement>(null);

  // 실제 API 데이터 조회 (거래량 탭만 실제 데이터 사용)
  const { rankingData, isLoading, error } = useStockRanking();

  // API 데이터를 UI 형태로 변환
  const volumeRankingStocks = rankingData ? transformVolumeRankingResults(rankingData) : [];

  // 거래량 탭일 때는 실제 API 데이터, 나머지는 빈 배열
  const sortedData = sortType === 'volume' ? volumeRankingStocks.slice(0, 10) : [];

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
              key={stock.code}
              className={`${styles.stockRow} ${index % 2 === 0 ? styles.evenRow : ''}`}
              onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
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
          ))
        ) : (
          <div className={styles.emptyMessage}>
            {sortType === 'volume' ? '데이터가 없습니다.' : '해당 탭은 준비 중입니다.'}
          </div>
        )}
      </div>
    </div>
  );

  return <TableLayoutContainer controls={controls} table={table} />;
}
