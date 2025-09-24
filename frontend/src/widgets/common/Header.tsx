import { Scale, User, HelpCircle, LogOut } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { userApi } from '../../features/user/api/userApi';
import LogoutModal from './LogoutModal';
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
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [isLogoutConfirmOpen, setIsLogoutConfirmOpen] = useState(false);
  const [isLogoutSuccessOpen, setIsLogoutSuccessOpen] = useState(false);
  const navigate = useNavigate();
  const userMenuRef = useRef<HTMLDivElement>(null);

  // 외부 클릭 시 메뉴 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
        setIsUserMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const logoutMutation = useMutation({
    mutationFn: async () => {
      const baseURL = import.meta.env.VITE_API_BASE_URL ||
                     (window.location.hostname === 'localhost' ? 'http://localhost:8080' : '');

      const response = await fetch(`${baseURL}/api/users/logout`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('로그아웃 실패');
      }

      return response.json();
    },
    onSuccess: () => {
      setIsLogoutConfirmOpen(false);
      setTimeout(() => {
        setIsLogoutSuccessOpen(true);
      }, 200);
    },
    onError: (error) => {
      setIsLogoutConfirmOpen(false);
      setTimeout(() => {
        setIsLogoutSuccessOpen(true);
      }, 200);
    }
  });

  const handleLogoutClick = () => {
    setIsUserMenuOpen(false);
    setIsLogoutConfirmOpen(true);
  };

  const handleLogoutConfirm = () => {
    logoutMutation.mutate();
  };

  const handleLogoutCancel = () => {
    setIsLogoutConfirmOpen(false);
  };

  const handleSuccessClose = () => {
    setIsLogoutSuccessOpen(false);
    window.location.href = '/landing';
  };

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

          {/* 사용자 메뉴 */}
          <div className={styles.userMenuContainer} ref={userMenuRef}>
            <motion.button
              className={styles.userAvatar}
              onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
              whileTap={{ scale: 0.95 }}
              title="사용자 메뉴"
            >
              <User className={styles.avatarIcon} />
            </motion.button>

            {/* 드롭다운 메뉴 */}
            <AnimatePresence>
              {isUserMenuOpen && (
                <motion.div
                  className={styles.userDropdown}
                  initial={{ opacity: 0, scale: 0.95, y: -10 }}
                  animate={{ opacity: 1, scale: 1, y: 0 }}
                  exit={{ opacity: 0, scale: 0.95, y: -10 }}
                  transition={{ duration: 0.15 }}
                >
                  <button
                    className={styles.dropdownItem}
                    onClick={handleLogoutClick}
                  >
                    <LogOut className={styles.dropdownIcon} />
                    로그아웃
                  </button>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>

      {/* 로그아웃 모달 */}
      <LogoutModal
        isConfirmOpen={isLogoutConfirmOpen}
        isSuccessOpen={isLogoutSuccessOpen}
        onConfirm={handleLogoutConfirm}
        onCancel={handleLogoutCancel}
        onSuccessClose={handleSuccessClose}
        isLoading={logoutMutation.isPending}
      />
    </header>
  );
}
