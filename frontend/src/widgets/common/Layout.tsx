import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useMemo } from 'react';
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
