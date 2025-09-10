import { useState } from 'react';
import styles from './SearchPage.module.css';
import HoldingsTableWidget from '../../widgets/search/HoldingsTableWidget';
import RankingTableWidget from '../../widgets/search/RankingTableWidget';
import SearchTableWidget from '../../widgets/search/SearchTableWidget';
import WatchlistTableWidget from '../../widgets/search/WatchlistTableWidget';
import NewsWidget from '../../widgets/common/NewsWidget';

export default function SearchPage() {
  const [activeSubTab, setActiveSubTab] = useState<'ranking' | 'search' | 'watchlist' | 'holdings'>('holdings');

  const handleStockSelect = (stockCode: string) => {
    // TODO: Navigate to StockDetailPage (separate page)
    console.log('Navigate to stock detail:', stockCode);
  };

  const renderContent = () => {
    switch (activeSubTab) {
      case 'holdings':
        return <HoldingsTableWidget onStockSelect={handleStockSelect} />;
      case 'ranking':
        return <RankingTableWidget onStockSelect={handleStockSelect} />;
      case 'search':
        return <SearchTableWidget onStockSelect={handleStockSelect} />;
      case 'watchlist':
        return <WatchlistTableWidget onStockSelect={handleStockSelect} />;
      default:
        return <HoldingsTableWidget onStockSelect={handleStockSelect} />;
    }
  };

  return (
    <div className={styles.searchPage}>
      {/* 서브 탭 네비게이션 */}
      <div className={styles.subTabNav}>
        <div className={styles.subTabContainer}>
          <button
            className={`${styles.subTab} ${activeSubTab === 'ranking' ? styles.active : ''}`}
            onClick={() => setActiveSubTab('ranking')}
          >
            실시간 순위
          </button>
          <button
            className={`${styles.subTab} ${activeSubTab === 'search' ? styles.active : ''}`}
            onClick={() => setActiveSubTab('search')}
          >
            종목 검색
          </button>
          <button
            className={`${styles.subTab} ${activeSubTab === 'watchlist' ? styles.active : ''}`}
            onClick={() => setActiveSubTab('watchlist')}
          >
            관심 종목
          </button>
          <button
            className={`${styles.subTab} ${activeSubTab === 'holdings' ? styles.active : ''}`}
            onClick={() => setActiveSubTab('holdings')}
          >
            보유 종목
          </button>
        </div>
        <div className={styles.timestamp}>
          <span>오늘 16:50 기준</span>
        </div>
      </div>

      {/* 메인 컨텐츠 - 7:3 레이아웃 */}
      <div className={styles.mainContent}>
        {/* 좌측 테이블 영역 (70%) */}
        <div className={styles.tableArea}>
          {renderContent()}
        </div>

        {/* 우측 뉴스 영역 (30%) */}
        <div className={styles.newsArea}>
          <NewsWidget />
        </div>
      </div>
    </div>
  );
}
