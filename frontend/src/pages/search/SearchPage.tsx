import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAccountStore } from '../../entities/account/accountStore';
import styles from './SearchPage.module.css';
import HoldingsTableWidget from '../../widgets/search/HoldingsTableWidget';
import RankingTableWidget from '../../widgets/search/RankingTableWidget';
import SearchTableWidget from '../../widgets/search/SearchTableWidget';
import NewsWidget from '../../widgets/search/NewsWidget';

export default function SearchPage() {
  const [activeSubTab, setActiveSubTab] = useState<'ranking' | 'search' | 'holdings' | 'holdings-v2'>('ranking');
  const navigate = useNavigate();
  const { accountId } = useAccountStore();
  // const accountId = null;

  // accountId가 null일 때 접근 방지
  useEffect(() => {
    if (accountId === null) {
      alert('계좌 정보가 필요합니다.\n계좌를 등록해주세요.');
      navigate('/dashboard');
    }
  }, [accountId, navigate]);

  const handleStockSelect = (stock: { code: string; name: string }) => {
    navigate(`/search/stocks/${stock.code}`, {
      state: {
        stockCode: stock.code,
        stockName: stock.name,
      },
    });
  };

  const renderContent = () => {
    switch (activeSubTab) {
      case 'holdings':
        return <HoldingsTableWidget onStockSelect={handleStockSelect} />;
      case 'ranking':
        return <RankingTableWidget onStockSelect={handleStockSelect} />;
      case 'search':
        return <SearchTableWidget onStockSelect={handleStockSelect} />;
      default:
        return <RankingTableWidget onStockSelect={handleStockSelect} />;
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
        <div className={styles.tableArea}>{renderContent()}</div>

        {/* 우측 뉴스 영역 (30%) */}
        <div className={styles.newsArea}>
          <NewsWidget />
        </div>
      </div>
    </div>
  );
}
