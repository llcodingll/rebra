import React from 'react';
import styles from './BacktestRow.module.css';
import { Trash2, TrendingUp, TrendingDown } from 'lucide-react';

interface BacktestRowProps {
  name: string;
  date: string;
  period: string;
  backtestPeriod: string;
  status: string;
  onBacktestClick?: () => void;
  onDelete?: () => void;
}

export default function BacktestRow({
  name,
  date,
  period,
  backtestPeriod,
  status,
  onBacktestClick,
  onDelete
}: BacktestRowProps) {

  const getStatusColor = (status: string) => {
    switch(status) {
      case '완료': return styles.statusCompleted;
      case '계산 중': return styles.statusInProgress;
      case '진행중': return styles.statusInProgress;
      case '실패': return styles.statusFailed;
      default: return styles.statusDefault;
    }
  };
  
  return (
    <tr
      className={`${styles.dataRow} ${onBacktestClick ? styles.clickable : ''}`}
      onClick={onBacktestClick}
    >
      <td className={styles.nameCell}>
        <div className={styles.nameMain}>{name}</div>
      </td>

      <td className={styles.dateCell}>
        <div className={styles.dateMain}>{date}</div>
      </td>

      <td className={styles.periodCell}>
        <span className={styles.periodBadge}>{period}</span>
      </td>

      <td className={styles.backtestPeriodCell}>
        <div className={styles.backtestPeriodMain}>{backtestPeriod}</div>
      </td>

      <td className={styles.statusCell}>
        <span className={`${styles.statusBadge} ${getStatusColor(status)}`}>
          {status}
        </span>
      </td>

      <td className={styles.actionCell} onClick={(e) => e.stopPropagation()}>
        <button
          className={styles.deleteButton}
          onClick={onDelete}
          title="백테스트 삭제"
        >
          <Trash2 className={styles.deleteIcon} />
        </button>
      </td>
    </tr>
  );
}