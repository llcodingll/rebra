import { Trash2, AlertTriangle } from 'lucide-react';
import { motion } from 'motion/react';
import BaseModal from '../../shared/ui/modal/BaseModal';
import styles from './DeleteConfirmModal.module.css';

interface DeleteConfirmModalProps {
  isOpen: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  title: string;
  message: string;
  isLoading?: boolean;
}

export default function DeleteConfirmModal({
  isOpen,
  onConfirm,
  onCancel,
  title,
  message,
  isLoading = false
}: DeleteConfirmModalProps) {
  return (
    <BaseModal
      isOpen={isOpen}
      onClose={onCancel}
      size="small"
      showCloseButton={false}
    >
      <div className={styles.confirmModal}>
        <div className={styles.iconContainer}>
          <AlertTriangle className={styles.alertIcon} />
        </div>

        <h3 className={styles.title}>백테스트 삭제</h3>
        <p className={styles.message}>
          "{title}" 백테스트를 정말로 삭제하시겠습니까?
          <br />
          <span className={styles.warningText}>이 작업은 되돌릴 수 없습니다.</span>
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
            <Trash2 className={styles.buttonIcon} />
            {isLoading ? '삭제 중...' : '삭제'}
          </motion.button>
        </div>
      </div>
    </BaseModal>
  );
}