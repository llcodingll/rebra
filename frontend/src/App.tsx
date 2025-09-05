import { useState } from 'react';
import Header from './components/Header';
import Navigation from './components/Navigation';
import MarketTicker from './components/MarketTicker';
import DashboardPage from './pages/DashboardPage';
import BacktestPage from './pages/BacktestPage';
import SearchPage from './pages/SearchPage';
import LandingPage from './pages/LandingPage';
import SignupPage from './pages/SignupPage';
import LoginPage from './pages/LoginPage';
import styles from './App.module.css';

type AuthState = 'landing' | 'signup' | 'login' | 'authenticated';
type DashboardTab = 'dashboard' | 'search' | 'backtest';

export default function App() {
  const [authState, setAuthState] = useState<AuthState>('authenticated');
  const [activeTab, setActiveTab] = useState<DashboardTab>('dashboard');

  // 인증되지 않은 상태에서는 랜딩페이지, 로그인, 회원가입 표시
  if (authState !== 'authenticated') {
    const handleSignup = () => setAuthState('signup');
    const handleLogin = () => setAuthState('login');
    const handleBack = () => setAuthState('landing');
    const handleAuthSuccess = () => setAuthState('authenticated'); // 카카오 로그인/가입 모두 여기로

    switch (authState) {
      case 'landing':
        return <LandingPage onSignup={handleSignup} onLogin={handleLogin} />;
      case 'signup':
        return (
          <SignupPage 
            onBack={handleBack} 
            onLogin={handleLogin}
            onSignupComplete={handleAuthSuccess} // 카카오 가입 완료 시 바로 인증
          />
        );
      case 'login':
        return (
          <LoginPage 
            onBack={handleBack} 
            onSignup={handleSignup}
            onLoginSuccess={handleAuthSuccess} // 카카오 로그인 완료 시 바로 인증
          />
        );
    }
  }

  // 인증된 상태에서는 대시보드 표시
  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return <DashboardPage />;
      case 'search':
        return <SearchPage />;
      case 'backtest':
        return <BacktestPage />;
      default:
        return <DashboardPage />;
    }
  };

  return (
    <div className={styles.app}>
      <Header />
      <Navigation activeTab={activeTab} onTabChange={setActiveTab} />
      <MarketTicker />
      <main className={styles.main}>
        {renderContent()}
      </main>
    </div>
  );
}