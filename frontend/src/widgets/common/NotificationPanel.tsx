import { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Bell, TrendingUp, AlertCircle, CheckCircle, X } from 'lucide-react';
import styles from './NotificationPanel.module.css';

interface Notification {
  id: string;
  type: 'info' | 'success' | 'warning' | 'error';
  title: string;
  message: string;
  time: string;
  isRead: boolean;
}

const mockNotifications: Notification[] = [
  {
    id: '1',
    type: 'success',
    title: '매수 주문 체결',
    message: 'AAPL 10주 매수 주문이 성공적으로 체결되었습니다.',
    time: '5분 전',
    isRead: false
  },
  {
    id: '2',
    type: 'info',
    title: '목표가 도달',
    message: 'TSLA 주가가 설정한 목표가 $250에 도달했습니다.',
    time: '1시간 전',
    isRead: false
  },
  {
    id: '3',
    type: 'warning',
    title: '백테스트 완료',
    message: '포트폴리오 백테스트가 완료되었습니다. 결과를 확인해보세요.',
    time: '2시간 전',
    isRead: true
  }
];

export default function NotificationPanel() {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState(mockNotifications);
  
  const unreadCount = notifications.filter(n => !n.isRead).length;

  const markAsRead = (id: string) => {
    setNotifications(prev => 
      prev.map(n => n.id === id ? { ...n, isRead: true } : n)
    );
  };

  const markAllAsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
  };

  const getIcon = (type: string) => {
    switch (type) {
      case 'success': return <CheckCircle className={styles.successIcon} />;
      case 'warning': return <AlertCircle className={styles.warningIcon} />;
      case 'error': return <AlertCircle className={styles.errorIcon} />;
      default: return <TrendingUp className={styles.infoIcon} />;
    }
  };

  return (
    <div className={styles.container}>
      <motion.button
        className={styles.trigger}
        onClick={() => setIsOpen(!isOpen)}
        whileHover={{ y: -1 }}
        whileTap={{ scale: 0.95 }}
      >
        <div className={styles.notificationIcon}>
          <Bell className={styles.bellIcon} />
          <div className={styles.iconGlow} />
        </div>
        {unreadCount > 0 && (
          <motion.div 
            className={styles.counter}
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: "spring", stiffness: 300, damping: 20 }}
          >
            {unreadCount > 99 ? '99+' : unreadCount}
          </motion.div>
        )}
      </motion.button>

      <AnimatePresence>
        {isOpen && (
          <>
            <motion.div
              className={styles.backdrop}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setIsOpen(false)}
            />
            <motion.div
              className={styles.panel}
              initial={{ opacity: 0, y: -10, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -10, scale: 0.95 }}
              transition={{ type: "spring", stiffness: 300, damping: 25 }}
            >
              <div className={styles.header}>
                <h3 className={styles.title}>알림</h3>
                <div className={styles.actions}>
                  {unreadCount > 0 && (
                    <button 
                      className={styles.markAllButton}
                      onClick={markAllAsRead}
                    >
                      모두 읽음
                    </button>
                  )}
                  <button 
                    className={styles.closeButton}
                    onClick={() => setIsOpen(false)}
                  >
                    <X className={styles.closeIcon} />
                  </button>
                </div>
              </div>
              
              <div className={styles.content}>
                {notifications.length === 0 ? (
                  <div className={styles.empty}>
                    <div className={styles.emptyIcon}>
                      <Bell />
                    </div>
                    <p>새로운 알림이 없습니다</p>
                  </div>
                ) : (
                  <div className={styles.list}>
                    {notifications.map((notification) => (
                      <motion.div
                        key={notification.id}
                        className={`${styles.item} ${!notification.isRead ? styles.unread : ''}`}
                        onClick={() => markAsRead(notification.id)}
                        whileHover={{ backgroundColor: "rgba(3, 2, 19, 0.02)" }}
                        layout
                      >
                        <div className={styles.iconWrapper}>
                          {getIcon(notification.type)}
                        </div>
                        <div className={styles.textContent}>
                          <h4 className={styles.notificationTitle}>
                            {notification.title}
                          </h4>
                          <p className={styles.message}>
                            {notification.message}
                          </p>
                          <span className={styles.time}>
                            {notification.time}
                          </span>
                        </div>
                        {!notification.isRead && (
                          <div className={styles.unreadDot} />
                        )}
                      </motion.div>
                    ))}
                  </div>
                )}
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  );
}