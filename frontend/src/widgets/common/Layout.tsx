import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useMemo, useLayoutEffect, useEffect, useState, useCallback } from 'react';
import Header from './Header';
import MarketTicker from '../../features/market/ui/MarketTicker';
import styles from '../../App.module.css';

type DashboardTab = 'dashboard' | 'search' | 'backtest';
export default function Layout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [tutorialStates, setTutorialStates] = useState({
    dashboard: false,
    search: false,
    backtest: false,
  });

  const activeTab = useMemo(() => {
    const path = location.pathname;
    if (path.startsWith('/search')) return 'search';
    if (path.startsWith('/backtest')) return 'backtest';
    return 'dashboard';
  }, [location.pathname]);

  // 페이지 간 이동 시에만 스크롤 맨 위로 이동 (같은 페이지 내 상태 변경 제외)
  useLayoutEffect(() => {
    const scrollToTop = () => {
      window.scrollTo({ top: 0, behavior: 'instant' });
      document.documentElement.scrollTop = 0;
      document.body.scrollTop = 0;

      const root = document.getElementById('root');
      if (root) root.scrollTop = 0;

      const main = document.querySelector('main');
      if (main) main.scrollTop = 0;
    };

    // 실제 페이지 경로가 변경될 때만 스크롤 (쿼리 파라미터나 해시 변경 제외)
    scrollToTop();
  }, [location.pathname]);

  const handleTabChange = (tab: DashboardTab) => {
    switch (tab) {
      case 'dashboard':
        navigate('/dashboard');
        break;
      case 'search':
        navigate('/search');
        break;
      case 'backtest':
        navigate('/backtest');
        break;
    }
  };

  const handleTutorialClick = () => {
    console.log('튜토리얼 버튼 클릭됨', activeTab);
    setTutorialStates(prev => ({
      ...prev,
      [activeTab]: true
    }));
  };

  const closeTutorial = (page: DashboardTab) => {
    setTutorialStates(prev => ({
      ...prev,
      [page]: false
    }));
  };

  return (
    <div className={styles.app}>
      <Header activeTab={activeTab} onTabChange={handleTabChange} onTutorialClick={handleTutorialClick} />
      <MarketTicker />
      <main className={styles.main}>
        <Outlet context={{ tutorialStates, closeTutorial }} />
      </main>
    </div>
  );
}
