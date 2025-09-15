import { useState } from 'react';
import DatePicker from 'react-datepicker';
import BaseModal from '../../shared/ui/modal/BaseModal';
import styles from './RebalancingPeriodModal.module.css';
import 'react-datepicker/dist/react-datepicker.css';

interface RebalancingPeriodModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function RebalancingPeriodModal({ isOpen, onClose }: RebalancingPeriodModalProps) {
  const [startDate, setStartDate] = useState<Date | null>(new Date());
  const [periodType, setPeriodType] = useState<'month' | 'year'>('month');
  const [periodValue, setPeriodValue] = useState(1);

  const handleSave = () => {
    const periodData = {
      startDate,
      periodType,
      periodValue
    };
    console.log('리밸런싱 주기 설정 저장:', periodData);
    // TODO: API 호출
    onClose();
  };

  return (
    <BaseModal
      isOpen={isOpen}
      onClose={onClose}
      title="리밸런싱 주기 설정"
      size="large"
    >
      <div className={styles.content}>
        {/* 시작일자 설정 */}
        <div className={styles.field}>
          <label className={styles.label}>시작일자</label>
          <div className={styles.datePickerWrapper}>
            <DatePicker
              selected={startDate}
              onChange={(date: Date | null) => setStartDate(date)}
              dateFormat="yyyy년 MM월 dd일"
              placeholderText="날짜를 선택하세요"
              className={styles.dateInput}
              calendarClassName={styles.calendar}
              showPopperArrow={false}
              minDate={new Date()}
              locale="ko"
            />
          </div>
        </div>

        {/* 주기 단위 선택 */}
        <div className={styles.field}>
          <label className={styles.label}>주기 단위</label>
          <div className={styles.toggleButtons}>
            <button
              type="button"
              className={`${styles.toggleButton} ${periodType === 'month' ? styles.active : ''}`}
              onClick={() => setPeriodType('month')}
            >
              월
            </button>
            <button
              type="button"
              className={`${styles.toggleButton} ${periodType === 'year' ? styles.active : ''}`}
              onClick={() => setPeriodType('year')}
            >
              년
            </button>
          </div>
        </div>

        {/* 주기 숫자 입력 */}
        <div className={styles.field}>
          <label className={styles.label}>주기</label>
          <div className={styles.periodInputGroup}>
            <input
              type="number"
              min="1"
              max={periodType === 'month' ? 12 : 10}
              className={styles.numberInput}
              value={periodValue}
              onChange={(e) => setPeriodValue(Number(e.target.value))}
            />
            <span className={styles.periodUnit}>
              {periodType === 'month' ? '개월마다' : '년마다'}
            </span>
          </div>
        </div>

        {/* 버튼 그룹 */}
        <div className={styles.buttonGroup}>
          <button className={styles.cancelButton} onClick={onClose}>
            취소
          </button>
          <button className={styles.saveButton} onClick={handleSave}>
            저장
          </button>
        </div>
      </div>
    </BaseModal>
  );
}