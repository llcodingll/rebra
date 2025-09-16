import { ReactNode } from 'react';
import styles from './BaseModal.module.css';

interface BaseModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
  size?: 'small' | 'medium' | 'large';
  showCloseButton?: boolean;
}

export default function BaseModal({
  isOpen,
  onClose,
  title,
  children,
  size = 'medium',
  showCloseButton = true
}: BaseModalProps) {
  if (!isOpen) return null;

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div className={styles.overlay} onClick={handleBackdropClick}>
      <div className={`${styles.modal} ${styles[size]}`}>
        {(title || showCloseButton) && (
          <div className={styles.header}>
            {title && <h2 className={styles.title}>{title}</h2>}
            {showCloseButton && (
              <button className={styles.closeButton} onClick={onClose}>
                ×
              </button>
            )}
          </div>
        )}

        <div className={styles.content}>
          {children}
        </div>
      </div>
    </div>
  );
}