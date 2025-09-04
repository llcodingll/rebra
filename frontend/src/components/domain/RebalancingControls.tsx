import { useState } from 'react';
import styles from './RebalancingControls.module.css';

interface RebalancingControlsProps {
  onExecute?: () => void;
  onToggleAuto?: (enabled: boolean) => void;
  onPeriodChange?: (period: string, customMonths?: number) => void;
  playIcon?: string;
  initialAutoEnabled?: boolean;
  initialPeriod?: string;
  initialCustomMonths?: number;
}

export default function RebalancingControls({
  onExecute,
  onToggleAuto,
  onPeriodChange,
  playIcon,
  initialAutoEnabled = false,
  initialPeriod = '월간',
  initialCustomMonths = 3
}: RebalancingControlsProps) {
  const [autoEnabled, setAutoEnabled] = useState(initialAutoEnabled);
  const [selectedPeriod, setSelectedPeriod] = useState(initialPeriod);
  const [customMonths, setCustomMonths] = useState(initialCustomMonths);

  const handleExecute = () => {
    onExecute?.();
  };

  const handleToggleAuto = () => {
    const newEnabled = !autoEnabled;
    setAutoEnabled(newEnabled);
    onToggleAuto?.(newEnabled);
  };

  const handlePeriodClick = (period: string) => {
    setSelectedPeriod(period);
    onPeriodChange?.(period, customMonths);
  };

  const handleCustomMonthsChange = (value: number) => {
    setCustomMonths(value);
    onPeriodChange?.(selectedPeriod, value);
  };

  const handleSave = () => {
    onPeriodChange?.(selectedPeriod, customMonths);
  };

  return (
    <div className={styles.rebalancingControls}>
      <div className={styles.controlGroup}>
        <h3>즉시 실행</h3>
        <button className={styles.executeButton} onClick={handleExecute}>
          {playIcon ? (
            <img src={playIcon} alt="실행" className={styles.playIcon} />
          ) : (
            <svg width="19" height="19" viewBox="0 0 19 19" fill="none">
              <path d="M4.75589 2.38672L15.7495 9.33007L4.75589 16.2734V2.38672Z" fill="white" stroke="white" strokeWidth="1.38867" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          )}
          지금 리벨런싱 실행
        </button>
      </div>

      <div className={styles.controlGroup}>
        <h3>자동 리벨런싱</h3>
        <div className={styles.toggleContainer}>
          <div 
            className={`${styles.toggle} ${autoEnabled ? styles.enabled : ''}`}
            onClick={handleToggleAuto}
          >
            <div className={styles.toggleTrack}></div>
            <div className={styles.toggleThumb}></div>
          </div>
          <span className={styles.toggleLabel}>활성화</span>
        </div>
      </div>

      <div className={styles.controlGroup}>
        <h3>리밸런싱 주기</h3>
        <div className={styles.periodButtons}>
          <button 
            className={`${styles.periodButton} ${selectedPeriod === '주간' ? styles.active : ''}`}
            onClick={() => handlePeriodClick('주간')}
          >
            주간
          </button>
          <button 
            className={`${styles.periodButton} ${selectedPeriod === '월간' ? styles.active : ''}`}
            onClick={() => handlePeriodClick('월간')}
          >
            월간
          </button>
          <button 
            className={`${styles.periodButton} ${selectedPeriod === '연간' ? styles.active : ''}`}
            onClick={() => handlePeriodClick('연간')}
          >
            연간
          </button>
          <input 
            type="number" 
            className={styles.periodInput} 
            value={customMonths}
            onChange={(e) => handleCustomMonthsChange(parseInt(e.target.value) || 1)}
            min="1"
            max="12"
          />
          <span>개월마다</span>
          <button className={styles.saveButton} onClick={handleSave}>
            저장
          </button>
        </div>
      </div>
    </div>
  );
}