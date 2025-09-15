import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useMemo, useLayoutEffect, useEffect } from 'react';
import Header from './Header';
import MarketTicker from './MarketTicker';
import styles from '../../App.module.css';

type DashboardTab = 'dashboard' | 'search' | 'backtest';
export default function Layout() {
  const navigate = useNavigate();
  const location = useLocation();

  const activeTab = useMemo(() => {
    const path = location.pathname;
    if (path.startsWith('/dashboard/search')) return 'search';
    if (path.startsWith('/dashboard/backtest')) return 'backtest';
    return 'dashboard';
  }, [location.pathname]);

  // 백테스트 페이지에서만 스크롤 맨 위로 이동
  useLayoutEffect(() => {
    if (location.pathname.startsWith('/dashboard/backtest')) {
      window.scrollTo(0, 0);
    }
  }, [location.pathname]);

  useEffect(() => {
    if (location.pathname.startsWith('/dashboard/backtest')) {
      const timer = setTimeout(() => {
        window.scrollTo(0, 0);
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [location.pathname]);

  const handleTabChange = (tab: DashboardTab) => {
    switch (tab) {
      case 'dashboard':
        navigate('/dashboard');
        break;
      case 'search':
        navigate('/dashboard/search');
        break;
      case 'backtest':
        navigate('/dashboard/backtest');
        break;
    }
  };

  return (
    <div className={styles.app}>
      <Header activeTab={activeTab} onTabChange={handleTabChange} />
      <MarketTicker />
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  );
}
