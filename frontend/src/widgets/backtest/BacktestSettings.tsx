import { motion } from 'motion/react';
import { Play } from 'lucide-react';
import styles from '../../pages/backtest/BacktestCreationPage.module.css';

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
            onChange={(e) => setStartDate(e.target.value)}
            className={styles.dateInput}
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="end-date">종료 날짜</label>
          <input
            id="end-date"
            type="month"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
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