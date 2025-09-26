import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useMemo, useLayoutEffect, useEffect, useState } from 'react';
import Header from './Header';
import MarketTicker from '../../features/market/ui/MarketTicker';
import styles from '../../App.module.css';

type DashboardTab = 'dashboard' | 'search' | 'backtest';
export default function Layout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [tutorialTarget, setTutorialTarget] = useState<{ page: string; start: () => void } | null>(null);

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
    console.log('튜토리얼 버튼 클릭됨');
    console.log('activeTab:', activeTab);
    console.log('tutorialTarget:', tutorialTarget);
    if (tutorialTarget && tutorialTarget.page === activeTab) {
      console.log('튜토리얼 시작 호출');
      tutorialTarget.start();
    } else {
      console.log('튜토리얼 타겟이 없거나 페이지가 맞지 않음');
    }
  };

  // Tutorial target 등록을 위한 함수를 context로 제공
  const registerTutorialTarget = (page: string, startFunction: () => void) => {
    console.log('튜토리얼 타겟 등록:', page);
    setTutorialTarget({ page, start: startFunction });
  };

  // 페이지 변경 시 tutorial target 초기화 (다른 페이지로 이동할 때만)
  useEffect(() => {
    console.log('페이지 변경됨, 현재 타겟과 비교:', tutorialTarget?.page, 'vs', activeTab);
    if (tutorialTarget && tutorialTarget.page !== activeTab) {
      console.log('다른 페이지로 이동, 튜토리얼 타겟 초기화');
      setTutorialTarget(null);
    }
  }, [location.pathname, activeTab, tutorialTarget]);

  return (
    <div className={styles.app}>
      <Header activeTab={activeTab} onTabChange={handleTabChange} onTutorialClick={handleTutorialClick} />
      <MarketTicker />
      <main className={styles.main}>
        <Outlet context={{ registerTutorialTarget }} />
      </main>
    </div>
  );
}
