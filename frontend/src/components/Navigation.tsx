import styles from './Navigation.module.css';
import { imgVector, imgVector1, imgVector2, imgVector3, imgVector4, imgVector5, imgVector6, imgVector7, imgVector8 } from '../imports/svg-jp00s';

interface NavigationProps {
  activeTab: 'dashboard' | 'search' | 'backtest';
  onTabChange: (tab: 'dashboard' | 'search' | 'backtest') => void;
}

export default function Navigation({ activeTab, onTabChange }: NavigationProps) {
  return (
    <nav className={styles.nav}>
      <div className={styles.container}>
        <button 
          className={`${styles.navButton} ${activeTab === 'dashboard' ? styles.active : ''}`}
          onClick={() => onTabChange('dashboard')}
        >
          <div className={styles.navIcon}>
            <svg viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M1 1V10.3333C1 10.6428 1.12292 10.9395 1.34171 11.1583C1.5605 11.3771 1.85725 11.5 2.16667 11.5H11.5" stroke="currentColor" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M1 5.66667V1" stroke="currentColor" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M1 8V1" stroke="currentColor" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M1 2.75V1" stroke="currentColor" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <span>대시보드</span>
        </button>

        <button 
          className={`${styles.navButton} ${activeTab === 'search' ? styles.active : ''}`}
          onClick={() => onTabChange('search')}
        >
          <div className={styles.navIcon}>
            <svg viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M5.66667 10.3333C8.244 10.3333 10.3333 8.244 10.3333 5.66667C10.3333 3.08934 8.244 1 5.66667 1C3.08934 1 1 3.08934 1 5.66667C1 8.244 3.08934 10.3333 5.66667 10.3333Z" stroke="currentColor" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M3.50833 3.50833L1 1" stroke="currentColor" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <span>주식 검색</span>
        </button>

        <button 
          className={`${styles.navButton} ${activeTab === 'backtest' ? styles.active : ''}`}
          onClick={() => onTabChange('backtest')}
        >
          <div className={styles.navIcon}>
            <svg viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3.91667 1V11.2083C3.91667 12.025 3.275 12.6667 2.45833 12.6667C1.64167 12.6667 1 12.025 1 11.2083V1" stroke="currentColor" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M1 1H5.08333" stroke="currentColor" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M3.91667 1H1" stroke="currentColor" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <span>백테스트</span>
        </button>
      </div>
    </nav>
  );
}