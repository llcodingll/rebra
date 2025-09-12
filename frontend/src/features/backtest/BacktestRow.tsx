import React from 'react';
import styles from './BacktestRow.module.css';
import { Trash2 } from 'lucide-react';

interface BacktestRowProps {
  name: string;
  date: string;
  period: string;
  totalReturn: string;
  maxDrawdown: string;
  sharpeRatio: string;
  status: string;
  onBacktestClick?: () => void;
}

export default function BacktestRow({ 
  name,
  date,
  period,
  totalReturn,
  maxDrawdown,
  sharpeRatio,
  status,
  onBacktestClick
}: BacktestRowProps) {
  return (
    <tr 
      className={`${styles.dataRow} ${styles.clickable}`}
      onClick={onBacktestClick}
    >
      <td className={styles.nameCell}>
        {name}
      </td>
      <td className={styles.dateCell}>
        {date}
      </td>
      <td className={styles.periodCell}>
        {period}
      </td>
      <td className={styles.returnCell}>
        {totalReturn}
      </td>
      <td className={styles.drawdownCell}>
        {maxDrawdown}
      </td>
      <td className={styles.sharpeCell}>
        {sharpeRatio}
      </td>
      <td className={styles.statusCell}>
        <span className={styles.statusBadge}>
          {status}
        </span>
      </td>
      <td className={styles.actionCell} onClick={(e) => e.stopPropagation()}>
        <button className={styles.deleteButton} title="삭제">
          <Trash2 className={styles.deleteIcon} />
        </button>
      </td>
    </tr>
  );
}