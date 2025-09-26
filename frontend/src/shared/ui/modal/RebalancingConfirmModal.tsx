import { InfoIcon } from 'lucide-react';
import BaseModal from './BaseModal';
import styles from './RebalancingConfirmModal.module.css';

interface RebalancingConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  startDate: Date | null;
  periodType: 'month' | 'year';
  periodValue: number;
}

export default function RebalancingConfirmModal({
  isOpen,
  onClose,
  onConfirm,
  startDate,
  periodType,
  periodValue
}: RebalancingConfirmModalProps) {
  const formatSelectedDate = () => {
    if (!startDate) return '';

    const year = startDate.getFullYear();
    const month = startDate.getMonth() + 1;
    const day = startDate.getDate();

    return `${year}년 ${month}월 ${day}일`;
  };

  const getRebalancingSchedule = () => {
    if (!startDate) return '';

    const day = startDate.getDate();
    const month = startDate.getMonth() + 1;

    if (periodType === 'month') {
      return `매달 ${day}일`;
    } else {
      return `매년 ${month}월 ${day}일`;
    }
  };

  const getPeriodText = () => {
    if (periodType === 'month') {
      return `${periodValue}개월마다`;
    } else {
      return `${periodValue}년마다`;
    }
  };

  return (
    <BaseModal
      isOpen={isOpen}
      onClose={onClose}
      title="리밸런싱 주기 설정 확인"
      size="medium"
    >
      <div className={styles.content}>
        <div className={styles.iconContainer}>
          <InfoIcon className={styles.infoIcon} />
        </div>

        <div className={styles.messageContainer}>
          <p className={styles.message}>
            선택한 날짜: <strong>{formatSelectedDate()}</strong><br />
            리밸런싱 주기: <strong>{getPeriodText()}</strong><br />
            실행 일정: <strong>{getRebalancingSchedule()}</strong>에 리밸런싱이 실행됩니다.<br /><br />
            만약 해당 날짜가 존재하지 않거나 거래일이 아닌 경우<br/><strong>가능한 다음 거래일</strong>로 자동 변경됩니다.
          </p>
        </div>

        <div className={styles.buttonGroup}>
          <button className={styles.cancelButton} onClick={onClose}>
            취소
          </button>
          <button className={styles.confirmButton} onClick={onConfirm}>
            확인
          </button>
        </div>
      </div>
    </BaseModal>
  );
}