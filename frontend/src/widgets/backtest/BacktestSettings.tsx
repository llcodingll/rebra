import { motion } from 'motion/react';
import { Play } from 'lucide-react';
import styles from './BacktestSettings.module.css';

interface BacktestSettingsProps {
  backtestName: string;
  setBacktestName: (name: string) => void;
  rebalancingPeriod: string;
  startDate: string;
  setStartDate: (date: string) => void;
  endDate: string;
  setEndDate: (date: string) => void;
  onRunBacktest: () => void;
  isRunDisabled: boolean;
}

export default function BacktestSettings({
  backtestName,
  setBacktestName,
  rebalancingPeriod,
  startDate,
  setStartDate,
  endDate,
  setEndDate,
  onRunBacktest,
  isRunDisabled
}: BacktestSettingsProps) {
  // 현재 날짜 정보
  const currentDate = new Date();
  const currentMonth = currentDate.getMonth() + 1; // 0-based이므로 +1
  const currentYear = currentDate.getFullYear();
  const currentMonthStr = `${currentYear}-${currentMonth.toString().padStart(2, '0')}`;
  
  // 최소 날짜는 2020년 9월
  const minDate = "2020-09";
  
  // 시작일 최대값 (현재 달 -1)
  const getPrevMonth = (dateStr: string) => {
    const [year, month] = dateStr.split('-').map(Number);
    const prevMonth = month === 1 ? 12 : month - 1;
    const prevYear = month === 1 ? year - 1 : year;
    return `${prevYear}-${prevMonth.toString().padStart(2, '0')}`;
  };
  
  const maxStartDate = getPrevMonth(currentMonthStr);
  
  // 다음 달 계산 함수
  const getNextMonth = (dateStr: string) => {
    if (!dateStr) return "";
    const [year, month] = dateStr.split('-').map(Number);
    const nextMonth = month === 12 ? 1 : month + 1;
    const nextYear = month === 12 ? year + 1 : year;
    return `${nextYear}-${nextMonth.toString().padStart(2, '0')}`;
  };

  const handleStartDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newStartDate = e.target.value;
    setStartDate(newStartDate);
    
    // 종료일이 시작일과 같거나 빠르면 종료일을 다음 달로 설정
    if (endDate && newStartDate >= endDate) {
      const nextMonth = getNextMonth(newStartDate);
      setEndDate(nextMonth);
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
          <label htmlFor="rebalancing-period">리밸런싱 주기</label>
          <div className={styles.fixedValue}>
            <span>{rebalancingPeriod}</span>
          </div>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="start-date">시작 날짜</label>
          <input
            id="start-date"
            type="month"
            value={startDate}
            onChange={handleStartDateChange}
            min={minDate}
            max={maxStartDate}
            className={styles.dateInput}
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="end-date">종료 날짜</label>
          <input
            id="end-date"
            type="month"
            value={endDate}
            onChange={handleEndDateChange}
            min={startDate ? getNextMonth(startDate) : minDate}
            max={currentMonthStr}
            className={styles.dateInput}
          />
        </div>
      </div>

      <button 
        className={styles.runButton}
        onClick={onRunBacktest}
        disabled={isRunDisabled}
      >
        <Play className={styles.runIcon} />
        백테스트 실행
      </button>
    </motion.div>
  );
}