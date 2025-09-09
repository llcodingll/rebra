import { useState } from 'react';
import styles from './SearchPage.module.css';
import StockListWidget from '../../widgets/search/StockListWidget';

export default function SearchPage() {
  const [activeSubTab, setActiveSubTab] = useState<'ranking' | 'search' | 'watchlist' | 'holdings'>('holdings');

  const handleStockSelect = (stockCode: string) => {
    // TODO: Navigate to StockDetailPage (separate page)
    console.log('Navigate to stock detail:', stockCode);
  };

  const renderContent = () => {
    switch (activeSubTab) {
      case 'holdings':
        return <StockListWidget onStockSelect={handleStockSelect} />;
      case 'ranking':
        return <div className={styles.placeholder}>실시간 순위 (구현 예정)</div>;
      case 'search':
        return <div className={styles.placeholder}>종목 검색 (구현 예정)</div>;
      case 'watchlist':
        return <div className={styles.placeholder}>관심 종목 (구현 예정)</div>;
      default:
        return <StockListWidget onStockSelect={handleStockSelect} />;
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

      {/* 메인 컨텐츠 */}
      <div className={styles.content}>{renderContent()}</div>
    </div>
  );
}
