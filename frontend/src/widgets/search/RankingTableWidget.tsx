import { useState, useRef, useEffect, useCallback } from 'react';
import styles from './RankingTableWidget.module.css';
import TableLayoutContainer from './components/TableLayoutContainer';
import { useStockRanking, type RankingType } from '../../features/stock-search/hooks/useStockRanking';
import { useWatchlist, useToggleWatchlist, isWatchlistStock } from '../../features/stock-search/hooks/useWatchlist';
import WatchlistIcon from '../../entities/stock/ui/WatchlistIcon';
import type { VolumeRankingApiItem } from '../../features/stock-search/api/types';

interface RankingTableWidgetProps {
  onStockSelect: (stock: { code: string; name: string }) => void;
}

export default function RankingTableWidget({ onStockSelect }: RankingTableWidgetProps) {
  const [sortType, setSortType] = useState<RankingType>('volume');
  const buttonsContainerRef = useRef<HTMLDivElement>(null);
  const underlineRef = useRef<HTMLDivElement>(null);
  const previousDataByTabRef = useRef<Record<RankingType, VolumeRankingApiItem[]>>({
    volume: [],
    rising: [],
    falling: [],
  });
  const previousSortTypeRef = useRef<RankingType>(sortType);
  const alphaStatesRef = useRef<Map<string, { alpha: number; intervalId: number }>>(new Map());

  // 알파값 점진적 감소 함수
  const startAlphaDecrement = useCallback((stockCode: string) => {
    // 기존 애니메이션이 있다면 정리
    const existingState = alphaStatesRef.current.get(stockCode);
    if (existingState) {
      clearInterval(existingState.intervalId);
    }

    // 요소 찾기
    const element = document.querySelector(`[data-stock="${stockCode}"]`) as HTMLElement;
    if (!element) return;

    // 알파값을 즉시 0.15로 설정
    element.style.setProperty('--highlight-alpha', '0.15');

    let alpha = 0.1;
    const decrement = 0.1 / (1500 / 16); // 2초간 60fps로 감소

    const intervalId = setInterval(() => {
      alpha -= decrement;
      if (alpha <= 0) {
        alpha = 0;
        clearInterval(intervalId);
        alphaStatesRef.current.delete(stockCode);
      }
      element.style.setProperty('--highlight-alpha', alpha.toString());
    }, 16) as unknown as number;

    // 상태 저장
    alphaStatesRef.current.set(stockCode, { alpha, intervalId });
  }, []);

  // 모든 애니메이션 정리 함수
  const clearAllAnimations = useCallback(() => {
    alphaStatesRef.current.forEach(({ intervalId }, stockCode) => {
      clearInterval(intervalId);
      const element = document.querySelector(`[data-stock="${stockCode}"]`) as HTMLElement;
      if (element) {
        element.style.setProperty('--highlight-alpha', '0');
      }
    });
    alphaStatesRef.current.clear();
  }, []);

  // 선택된 탭에 따른 실제 API 데이터 조회
  const { rankingData, isLoading, error } = useStockRanking(sortType);

  // 관심종목 관련 훅
  const { data: watchlistData } = useWatchlist();
  const toggleWatchlist = useToggleWatchlist();

  const handleToggleFavorite = (stockCode: string, event: React.MouseEvent) => {
    event.stopPropagation();
    toggleWatchlist.mutate(stockCode);
  };

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

  // 탭 변경 시 애니메이션 상태 초기화
  useEffect(() => {
    if (previousSortTypeRef.current !== sortType) {
      clearAllAnimations();
      previousSortTypeRef.current = sortType;
    }
  }, [sortType, clearAllAnimations]);

  // 등락률 변경 감지 및 애니메이션 실행
  useEffect(() => {
    const currentData = rankingData;
    const previousData = previousDataByTabRef.current[sortType];

    // 이전 데이터가 존재할 때만 비교
    if (previousData && previousData.length > 0) {
      const changedStocks = new Set<string>();

      currentData.forEach((currentStock) => {
        const previousStock = previousData.find((stock) => stock.stockCode === currentStock.stockCode);
        if (previousStock && previousStock.priceChangeRate !== currentStock.priceChangeRate) {
          changedStocks.add(currentStock.stockCode);
        }
      });

      if (changedStocks.size > 0) {
        // 변경된 각 종목에 대해 알파값 애니메이션 시작
        changedStocks.forEach((stockCode) => {
          startAlphaDecrement(stockCode);
        });
      }
    }

    // 비교 완료 후 현재 탭의 데이터를 이전 데이터로 저장
    previousDataByTabRef.current[sortType] = [...currentData];
  }, [rankingData, sortType, startAlphaDecrement]);

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
              className={`${styles.stockRow} ${stock.rank % 2 === 0 ? styles.evenRow : ''}`}
              onClick={() => onStockSelect({ code: stock.stockCode, name: stock.stockName })}
            >
              <div className={styles.stockInfo}>
                <WatchlistIcon
                  isFavorite={isWatchlistStock(stock.stockCode, watchlistData)}
                  size={16}
                  onClick={(e) => handleToggleFavorite(stock.stockCode, e)}
                  className={styles.favoriteIcon}
                />
                <div className={styles.rank}>{stock.rank}</div>
                <span className={styles.stockName}>{stock.stockName}</span>
              </div>

              <div className={styles.price}>{stock.currentPrice.toLocaleString()}</div>

              <div className={`${styles.change} ${stock.priceChangeRate >= 0 ? styles.positive : styles.negative}`}>
                <span data-stock={stock.stockCode} className={styles.highlight}>
                  {stock.priceChangeRate >= 0 ? '+' : ''}
                  {stock.priceChangeRate.toFixed(1)}%
                </span>
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
