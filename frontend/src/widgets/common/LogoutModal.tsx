import { LogOut, CheckCircle } from 'lucide-react';
import { motion } from 'motion/react';
import BaseModal from '../../shared/ui/modal/BaseModal';
import styles from './LogoutModal.module.css';

interface LogoutModalProps {
  isConfirmOpen: boolean;
  isSuccessOpen: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  onSuccessClose: () => void;
  isLoading?: boolean;
}

export default function LogoutModal({
  isConfirmOpen,
  isSuccessOpen,
  onConfirm,
  onCancel,
  onSuccessClose,
  isLoading = false
}: LogoutModalProps) {
  return (
    <>
      {/* 로그아웃 확인 모달 */}
      <BaseModal
        isOpen={isConfirmOpen}
        onClose={onCancel}
        size="small"
        showCloseButton={false}
      >
        <div className={styles.confirmModal}>
          <div className={styles.iconContainer}>
            <LogOut className={styles.logoutIcon} />
          </div>

          <h3 className={styles.title}>로그아웃</h3>
          <p className={styles.message}>
            정말로 로그아웃하시겠습니까?
          </p>

          <div className={styles.buttonGroup}>
            <button
              className={styles.cancelButton}
              onClick={onCancel}
              disabled={isLoading}
            >
              취소
            </button>
            <motion.button
              className={styles.confirmButton}
              onClick={onConfirm}
              disabled={isLoading}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
            >
              {isLoading ? '로그아웃 중...' : '로그아웃'}
            </motion.button>
          </div>
        </div>
      </BaseModal>

      {/* 로그아웃 성공 모달 */}
      <BaseModal
        isOpen={isSuccessOpen}
        onClose={onSuccessClose}
        size="small"
        showCloseButton={false}
      >
        <div className={styles.successModal}>
          <div className={styles.successIconContainer}>
            <CheckCircle className={styles.successIcon} />
          </div>

          <h3 className={styles.successTitle}>로그아웃 완료</h3>
          <p className={styles.successMessage}>
            로그아웃되었습니다.
          </p>

          <motion.button
            className={styles.successButton}
            onClick={onSuccessClose}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
          >
            확인
          </motion.button>
        </div>
      </BaseModal>
    </>
  );
}