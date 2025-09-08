import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Header from './Header';
import Navigation from './Navigation';
import MarketTicker from './MarketTicker';
import styles from '../../App.module.css';

type DashboardTab = 'dashboard' | 'search' | 'backtest';

export default function Layout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [activeTab, setActiveTab] = useState<DashboardTab>('dashboard');

  useEffect(() => {
    const path = location.pathname;
    if (path.startsWith('/dashboard/search')) {
      setActiveTab('search');
    } else if (path.startsWith('/dashboard/backtest')) {
      setActiveTab('backtest');
    } else {
      setActiveTab('dashboard');
    }
  }, [location.pathname]);

  const handleTabChange = (tab: DashboardTab) => {
    setActiveTab(tab);
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
      <Header />
      <Navigation activeTab={activeTab} onTabChange={handleTabChange} />
      <MarketTicker />
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  );
}