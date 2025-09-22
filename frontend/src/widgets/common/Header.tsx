import { Scale, User, HelpCircle } from 'lucide-react';
import { motion } from 'motion/react';
import styles from './Header.module.css';

interface HeaderProps {
  activeTab: 'dashboard' | 'search' | 'backtest';
  onTabChange: (tab: 'dashboard' | 'search' | 'backtest') => void;
  onTutorialClick?: () => void;
}

const tabs = [
  { id: 'dashboard', label: '대시보드' },
  { id: 'search', label: '주식 검색' },
  { id: 'backtest', label: '백테스트' },
] as const;

export default function Header({ activeTab, onTabChange, onTutorialClick }: HeaderProps) {
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
          {/* 튜토리얼 버튼 */}
          <motion.button
            className={styles.tutorialButton}
            onClick={onTutorialClick}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            title="튜토리얼"
          >
            <HelpCircle className={styles.tutorialIcon} />
          </motion.button>

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
