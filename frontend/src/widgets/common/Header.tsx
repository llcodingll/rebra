import styles from './Header.module.css';
import { imgSvg, imgSvg1 } from '../../assets/imports/svg-jp00s';
import imgPhoto14720996457855658Abf4Ff4E from 'figma:asset/f2e0d0183a438e31fe7131ed2173548b7f21aea2.png';

interface HeaderProps {
  activeTab: 'dashboard' | 'search' | 'backtest';
  onTabChange: (tab: 'dashboard' | 'search' | 'backtest') => void;
}

export default function Header({ activeTab, onTabChange }: HeaderProps) {
  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <div className={styles.leftSection}>
          <div className={styles.logo}>
            <div className={styles.logoIcon}>
              <span>R</span>
            </div>
            <h1 className={styles.logoText}>Rebra</h1>
          </div>

          {/* Navigation tabs integrated into header */}
          <nav className={styles.navigation}>
            <button
              className={`${styles.navButton} ${activeTab === 'dashboard' ? styles.active : ''}`}
              onClick={() => onTabChange('dashboard')}
            >
              <span>대시보드</span>
            </button>

            <button
              className={`${styles.navButton} ${activeTab === 'search' ? styles.active : ''}`}
              onClick={() => onTabChange('search')}
            >
              <span>주식 검색</span>
            </button>

            <button
              className={`${styles.navButton} ${activeTab === 'backtest' ? styles.active : ''}`}
              onClick={() => onTabChange('backtest')}
            >
              <span>백테스트</span>
            </button>
          </nav>
        </div>

        <div className={styles.rightSection}>
          <div className={styles.iconButton}>
            <img src={imgSvg1} alt='알림' />
            <div className={styles.badge}>3</div>
          </div>
          <div className={styles.userInfo}>
            <div
              className={styles.avatar}
              style={{ backgroundImage: `url(${imgPhoto14720996457855658Abf4Ff4E})` }}
            ></div>
          </div>
        </div>
      </div>
    </header>
  );
}
