import { Scale, User } from 'lucide-react';
import { motion } from 'motion/react';
import { useRef, useEffect, useState } from 'react';
import styles from './Header.module.css';
import NotificationPanel from './NotificationPanel';

interface HeaderProps {
  activeTab: 'dashboard' | 'search' | 'backtest';
  onTabChange: (tab: 'dashboard' | 'search' | 'backtest') => void;
}

const tabs = [
  { id: 'dashboard', label: '대시보드' },
  { id: 'search', label: '주식 검색' },
  { id: 'backtest', label: '백테스트' }
] as const;

export default function Header({ activeTab, onTabChange }: HeaderProps) {
  const [tabPositions, setTabPositions] = useState<{ [key: string]: { left: number; width: number } }>({});
  const tabRefs = useRef<{ [key: string]: HTMLButtonElement | null }>({});

  useEffect(() => {
    const calculatePositions = () => {
      const positions: { [key: string]: { left: number; width: number } } = {};

      tabs.forEach((tab) => {
        const element = tabRefs.current[tab.id];
        if (element) {
          const rect = element.getBoundingClientRect();
          const parentRect = element.parentElement?.getBoundingClientRect();
          if (parentRect) {
            // span 요소의 실제 위치와 크기를 기준으로 계산
            const span = element.querySelector('span');
            if (span) {
              const spanRect = span.getBoundingClientRect();
              positions[tab.id] = {
                left: spanRect.left - parentRect.left,
                width: spanRect.width
              };
            } else {
              // fallback: 패딩을 고려한 계산
              positions[tab.id] = {
                left: rect.left - parentRect.left + 24, // 패딩 24px 고려
                width: rect.width - 48 // 양쪽 패딩 제거
              };
            }
          }
        }
      });

      setTabPositions(positions);
    };

    calculatePositions();

    // 윈도우 리사이즈 시 재계산
    window.addEventListener('resize', calculatePositions);
    return () => window.removeEventListener('resize', calculatePositions);
  }, [activeTab]); // activeTab 변경시에도 재계산

  const activeTabPosition = tabPositions[activeTab];

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
                ref={(el) => tabRefs.current[tab.id] = el}
                className={`${styles.navButton} ${activeTab === tab.id ? styles.active : ''}`}
                onClick={() => onTabChange(tab.id as 'dashboard' | 'search' | 'backtest')}
              >
                <span>{tab.label}</span>
              </button>
            ))}
            
            {/* Animated underline */}
            {activeTabPosition && (
              <motion.div
                className={styles.underline}
                layoutId="activeTab"
                initial={false}
                animate={{
                  left: activeTabPosition.left,
                  width: activeTabPosition.width
                }}
                transition={{
                  type: "spring",
                  stiffness: 380,
                  damping: 30
                }}
              />
            )}
          </nav>
        </div>

        {/* Right section */}
        <div className={styles.rightSection}>
          <NotificationPanel />
          <motion.div 
            className={styles.userAvatar}
            whileHover={{ y: -2, boxShadow: "0 8px 25px rgba(3, 2, 19, 0.4)" }}
            whileTap={{ scale: 0.95 }}
          >
            <User className={styles.avatarIcon} />
          </motion.div>
        </div>
      </div>
    </header>
  );
}