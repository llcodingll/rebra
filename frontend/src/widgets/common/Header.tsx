import { Scale, User } from 'lucide-react';
import { motion } from 'motion/react';
import styles from './Header.module.css';
import NotificationPanel from './NotificationPanel';

interface HeaderProps {
  activeTab: 'dashboard' | 'search' | 'backtest';
  onTabChange: (tab: 'dashboard' | 'search' | 'backtest') => void;
}

const tabs = [
  { id: 'dashboard', label: '대시보드' },
  { id: 'search', label: '주식 검색' },
  { id: 'backtest', label: '백테스트' },
] as const;

export default function Header({ activeTab, onTabChange }: HeaderProps) {
  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <div className={styles.leftSection}>
          {/* Logo */}
          <div className={styles.logo}>
            <div className={styles.logoIcon}>
              <Scale className={styles.logoIconSvg} />
            </div>
            <h1 className={styles.logoText}>Rebra</h1>
          </div>

          {/* Navigation */}
          <nav className={styles.navigation}>
            {tabs.map((tab) => (
              <button
                key={tab.id}
                className={`${styles.navButton} ${activeTab === tab.id ? styles.active : ''}`}
                onClick={() => onTabChange(tab.id as 'dashboard' | 'search' | 'backtest')}
              >
                <span>{tab.label}</span>
              </button>
            ))}
          </nav>
        </div>

        {/* Right section */}
        <div className={styles.rightSection}>
          <NotificationPanel />
          <motion.div
            className={styles.userAvatar}
            // whileHover={{ y: -2, boxShadow: '0 8px 25px rgba(3, 2, 19, 0.4)' }}
            whileTap={{ scale: 0.95 }}
          >
            <User className={styles.avatarIcon} />
          </motion.div>
        </div>
      </div>
    </header>
  );
}
