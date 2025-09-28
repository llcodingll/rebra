import { useState } from 'react';
import DatePicker from 'react-datepicker';
import { HelpCircle } from 'lucide-react';
import BaseModal from '../../shared/ui/modal/BaseModal';
import RebalancingConfirmModal from '../../shared/ui/modal/RebalancingConfirmModal';
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
  const [showTooltip, setShowTooltip] = useState(false);
  const [showPeriodTooltip, setShowPeriodTooltip] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);

  const handleSave = () => {
    setShowConfirmModal(true);
  };

  const handleConfirm = () => {
    const periodData = {
      startDate,
      periodType,
      periodValue
    };
    console.log('리밸런싱 주기 설정 저장:', periodData);
    // TODO: API 호출
    setShowConfirmModal(false);
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
          <div className={styles.labelWithTooltip}>
            <label className={styles.label}>시작일자</label>
            <div
              className={styles.helpIconContainer}
              onMouseEnter={() => setShowTooltip(true)}
              onMouseLeave={() => setShowTooltip(false)}
            >
              <HelpCircle className={styles.helpIcon} />
              {showTooltip && (
                <div className={styles.tooltip}>
                  선택한 해당 날짜를 기준으로 리밸런싱이 이뤄집니다. <br />만약 해당 날짜가 존재하지 않거나 거래일이 아닌 경우 가능한 거래의 다음 일자로 거래가 넘어갑니다.
                </div>
              )}
            </div>
          </div>
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
          <div className={styles.labelWithTooltip}>
            <label className={styles.label}>주기 단위</label>
            <div
              className={styles.helpIconContainer}
              onMouseEnter={() => setShowPeriodTooltip(true)}
              onMouseLeave={() => setShowPeriodTooltip(false)}
            >
              <HelpCircle className={styles.helpIcon} />
              {showPeriodTooltip && (
                <div className={styles.tooltip}>
                  월의 경우에는 일자, 년의 경우에는 월과 일자를 기준으로 리밸런싱 일자가 설정됩니다.
                </div>
              )}
            </div>
          </div>
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

      <RebalancingConfirmModal
        isOpen={showConfirmModal}
        onClose={() => setShowConfirmModal(false)}
        onConfirm={handleConfirm}
        startDate={startDate}
        periodType={periodType}
        periodValue={periodValue}
      />
    </BaseModal>
  );
}