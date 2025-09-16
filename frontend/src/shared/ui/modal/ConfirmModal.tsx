import BaseModal from './BaseModal';
import styles from './ConfirmModal.module.css';

interface ConfirmModalProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel: () => void;
  type?: 'default' | 'danger' | 'warning';
}

export default function ConfirmModal({
  isOpen,
  title,
  message,
  confirmText = '확인',
  cancelText = '취소',
  onConfirm,
  onCancel,
  type = 'default'
}: ConfirmModalProps) {
  const handleConfirm = () => {
    onConfirm();
    onCancel(); // 모달 닫기
  };

  return (
    <BaseModal
      isOpen={isOpen}
      onClose={onCancel}
      title={title}
      size="small"
      showCloseButton={false}
    >
      <div className={styles.content}>
        <p className={styles.message}>
          {message.split('\n').map((line, index) => (
            <span key={index}>
              {line}
              {index < message.split('\n').length - 1 && <br />}
            </span>
          ))}
        </p>

        <div className={styles.buttonGroup}>
          <button
            className={styles.cancelButton}
            onClick={onCancel}
          >
            {cancelText}
          </button>
          <button
            className={`${styles.confirmButton} ${styles[type]}`}
            onClick={handleConfirm}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </BaseModal>
  );
}