import { motion } from 'motion/react';
import { Play, AlertTriangle } from 'lucide-react';
import { useConfirmModal } from '../../hooks/useModalState';
import ConfirmModal from '../../shared/ui/modal/ConfirmModal';
import styles from './BacktestSettings.module.css';

interface BacktestSettingsProps {
  backtestName: string;
  setBacktestName: (name: string) => void;
  rebalancingType: 'THRESHOLD' | 'PERIODIC';
  setRebalancingType: (type: 'THRESHOLD' | 'PERIODIC') => void;
  rebalancingPeriod: string;
  startDate: string;
  setStartDate: (date: string) => void;
  endDate: string;
  setEndDate: (date: string) => void;
  onRunBacktest: () => void;
  isRunDisabled: boolean;
  onNavigateToBacktestList?: () => void;
  isCreating?: boolean;
  portfolioCount?: number;
}

export default function BacktestSettings({
  backtestName,
  setBacktestName,
  rebalancingType,
  setRebalancingType,
  rebalancingPeriod,
  startDate,
  setStartDate,
  endDate,
  setEndDate,
  onRunBacktest,
  isRunDisabled,
  onNavigateToBacktestList,
  isCreating,
  portfolioCount = 0
}: BacktestSettingsProps) {
  const { confirmState, showConfirm, hideConfirm } = useConfirmModal();
  // 현재 날짜 정보
  const currentDate = new Date();
  const currentDateStr = currentDate.toISOString().split('T')[0]; // YYYY-MM-DD 형식

  // 최소 날짜는 2020년 9월 1일
  const minDate = "2020-09-01";

  // 어제 날짜 (종료일 최대값)
  const yesterday = new Date(currentDate);
  yesterday.setDate(yesterday.getDate() - 1);
  const maxEndDate = yesterday.toISOString().split('T')[0];

  // 어제 전날 (시작일 최대값)
  const dayBeforeYesterday = new Date(currentDate);
  dayBeforeYesterday.setDate(dayBeforeYesterday.getDate() - 2);
  const maxStartDate = dayBeforeYesterday.toISOString().split('T')[0];

  // 한국 고정 공휴일 목록 (월-일 형식)
  const koreanFixedHolidays = [
    '01-01', // 신정
    '03-01', // 삼일절
    '05-05', // 어린이날
    '06-06', // 현충일
    '08-15', // 광복절
    '10-03', // 개천절
    '10-09', // 한글날
    '12-25', // 성탄절
  ];

  // 거래일 체크 함수
  const isWeekend = (dateStr: string) => {
    const date = new Date(dateStr);
    const day = date.getDay();
    return day === 0 || day === 6; // 일요일(0) 또는 토요일(6)
  };

  const isHoliday = (dateStr: string) => {
    // dateStr에서 월-일 부분만 추출 (YYYY-MM-DD -> MM-DD)
    const monthDay = dateStr.substring(5); // "2023-08-15" -> "08-15"
    return koreanFixedHolidays.includes(monthDay);
  };

  const isNonTradingDay = (dateStr: string) => {
    return isWeekend(dateStr) || isHoliday(dateStr);
  };

  // 다음 날 계산 함수
  const getNextDay = (dateStr: string) => {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    date.setDate(date.getDate() + 1);
    return date.toISOString().split('T')[0];
  };

  // 백테스트 기간 검증 (최소 30일)
  const validatePeriod = () => {
    if (!startDate || !endDate) return { isValid: true, daysDiff: 0 };

    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = end.getTime() - start.getTime();
    const daysDiff = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    return {
      isValid: daysDiff >= 30,
      daysDiff
    };
  };

  const periodValidation = validatePeriod();

  // 실행 버튼 상태 메시지 생성
  const getButtonMessage = () => {
    if (isCreating) return '백테스트 생성 중...';
    if (!backtestName) return '테스트 이름을 입력하세요';
    if (!startDate) return '시작 날짜를 설정하세요';
    if (!endDate) return '종료 날짜를 설정하세요';
    if (!periodValidation.isValid) return '최소 30일 이상 설정하세요';
    if (portfolioCount === 0) return '종목을 추가하세요';
    return '백테스트 실행';
  };

  const handleStartDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newStartDate = e.target.value;
    setStartDate(newStartDate);

    // 종료일이 시작일과 같거나 빠르면 종료일을 다음 날로 설정
    if (endDate && newStartDate >= endDate) {
      const nextDay = getNextDay(newStartDate);
      setEndDate(nextDay);
    }
  };
  
  const handleEndDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newEndDate = e.target.value;
    
    // 종료일이 시작일과 같거나 빠르면 설정하지 않음
    if (startDate && newEndDate <= startDate) {
      return;
    }
    
    setEndDate(newEndDate);
  };

  const handleRunBacktest = () => {
    showConfirm({
      title: '백테스트 실행',
      message: '백테스트를 실행하시겠습니까?\n계산이 완료될 때까지 잠시 기다려주세요.',
      type: 'default',
      confirmText: '실행',
      cancelText: '취소',
      onConfirm: async () => {
        try {
          await onRunBacktest();

          if (onNavigateToBacktestList) {
            onNavigateToBacktestList();
          }
        } catch (error) {
          console.error('백테스트 실행 중 오류 발생:', error);
        }
      }
    });
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className={styles.settingsCard}
    >
      <div className={styles.cardHeader}>
        <h2>백테스트 설정</h2>
        <p>백테스트 실행을 위한 기본 설정을 입력해주세요</p>
      </div>

      <div className={styles.settingsGrid}>
        <div className={styles.formGroup}>
          <label htmlFor="backtest-name">테스트 이름</label>
          <input
            id="backtest-name"
            type="text"
            value={backtestName}
            onChange={(e) => setBacktestName(e.target.value)}
            placeholder="백테스트 이름을 입력하세요"
            className={styles.input}
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="rebalancing-type">리밸런싱 방식</label>
          <div className={styles.radioGroup}>
            <label className={styles.radioOption}>
              <input
                type="radio"
                name="rebalancingType"
                value="THRESHOLD"
                checked={rebalancingType === 'THRESHOLD'}
                onChange={(e) => setRebalancingType(e.target.value as 'THRESHOLD' | 'PERIODIC')}
                className={styles.radioInput}
              />
              <span className={styles.radioLabel}>임계값 기반</span>
              <span className={styles.radioDescription}>목표 비중 대비 임계값 초과 시 리밸런싱</span>
            </label>
            <label className={styles.radioOption}>
              <input
                type="radio"
                name="rebalancingType"
                value="PERIODIC"
                checked={rebalancingType === 'PERIODIC'}
                onChange={(e) => setRebalancingType(e.target.value as 'THRESHOLD' | 'PERIODIC')}
                className={styles.radioInput}
              />
              <span className={styles.radioLabel}>주기적 (월간)</span>
              <span className={styles.radioDescription}>매월 말 정기적으로 리밸런싱</span>
            </label>
          </div>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="start-date">시작 날짜</label>
          <div className={styles.dateInputContainer}>
            <input
              id="start-date"
              type="date"
              value={startDate}
              onChange={handleStartDateChange}
              min={minDate}
              max={maxStartDate}
              className={styles.dateInput}
            />
            <div className={styles.warningContainer}>
              {startDate && isNonTradingDay(startDate) && (
                <div className={styles.warningMessage}>
                  <AlertTriangle className={styles.warningIcon} />
                  <span>
                    {isWeekend(startDate) ? '주말' : '공휴일'}은 거래일이 아닙니다. 데이터 검색이 제한될 수 있습니다.
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="end-date">종료 날짜</label>
          <div className={styles.dateInputContainer}>
            <input
              id="end-date"
              type="date"
              value={endDate}
              onChange={handleEndDateChange}
              min={startDate ? getNextDay(startDate) : minDate}
              max={maxEndDate}
              className={styles.dateInput}
            />
            <div className={styles.warningContainer}>
              {startDate && endDate && !periodValidation.isValid && (
                <div className={styles.warningMessage}>
                  <AlertTriangle className={styles.warningIcon} />
                  <span>
                    백테스트 기간이 너무 짧습니다. 최소 30일 이상 설정해주세요. (현재: {periodValidation.daysDiff}일)
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className={styles.runButtonContainer}>
        <button
          className={styles.runButton}
          onClick={handleRunBacktest}
          disabled={isRunDisabled || !periodValidation.isValid}
        >
          <Play className={styles.runIcon} />
          {getButtonMessage()}
        </button>
        {isRunDisabled && !isCreating && (
          <p className={styles.runButtonHint}>
            {!backtestName && '1단계: 테스트 이름을 입력하세요'}
            {backtestName && !startDate && '2단계: 시작 날짜를 설정하세요'}
            {backtestName && startDate && !endDate && '3단계: 종료 날짜를 설정하세요'}
            {backtestName && startDate && endDate && !periodValidation.isValid && '백테스트 기간이 너무 짧습니다 (최소 30일)'}
            {backtestName && startDate && endDate && periodValidation.isValid && portfolioCount === 0 && '4단계: 왼쪽에서 종목을 검색하여 포트폴리오에 추가하세요'}
          </p>
        )}
      </div>

      <ConfirmModal
        isOpen={confirmState.isOpen}
        title={confirmState.title}
        message={confirmState.message}
        type={confirmState.type}
        confirmText={confirmState.confirmText}
        cancelText={confirmState.cancelText}
        onConfirm={confirmState.onConfirm}
        onCancel={hideConfirm}
      />
    </motion.div>
  );
}