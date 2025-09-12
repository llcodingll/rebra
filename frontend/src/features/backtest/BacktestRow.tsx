import React from 'react';
import styles from './BacktestRow.module.css';
import { Trash2, TrendingUp, TrendingDown } from 'lucide-react';

interface BacktestRowProps {
  name: string;
  date: string;
  period: string;
  totalReturn: string;
  maxDrawdown: string;
  sharpeRatio: string;
  annualReturn: string;
  volatility: string;
  winRate: string;
  status: string;
  onBacktestClick?: () => void;
  onDelete?: () => void;
}

export default function BacktestRow({ 
  name,
  date,
  period,
  totalReturn,
  maxDrawdown,
  sharpeRatio,
  annualReturn,
  volatility,
  winRate,
  status,
  onBacktestClick,
  onDelete
}: BacktestRowProps) {
  const isPositiveReturn = !totalReturn.startsWith('-');
  const returnValue = parseFloat(totalReturn.replace('%', ''));
  
  // 한국 주식시장 표준: 상승=빨강, 하락=파랑
  const getReturnColor = (value: number) => {
    if (value > 0) return styles.positiveReturn;
    if (value < 0) return styles.negativeReturn;
    return styles.neutralReturn;
  };

  const getStatusColor = (status: string) => {
    switch(status) {
      case '완료': return styles.statusCompleted;
      case '진행중': return styles.statusInProgress;
      case '실패': return styles.statusFailed;
      default: return styles.statusDefault;
    }
  };
  
  return (
    <tr 
      className={`${styles.dataRow} ${styles.clickable}`}
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
      
      <td className={styles.returnCell}>
        <div className={`${styles.returnMain} ${getReturnColor(returnValue)}`}>
          {isPositiveReturn ? (
            <TrendingUp className={styles.trendIcon} />
          ) : (
            <TrendingDown className={styles.trendIcon} />
          )}
          {totalReturn}
        </div>
        <div className={styles.returnSubtext}>연평균: {annualReturn}</div>
      </td>
      
      <td className={styles.drawdownCell}>
        <div className={styles.drawdownMain}>{maxDrawdown}</div>
        <div className={styles.drawdownSubtext}>최대손실</div>
      </td>
      
      <td className={styles.sharpeCell}>
        <div className={styles.sharpeMain}>{sharpeRatio}</div>
      </td>

      <td className={styles.volatilityCell}>
        <div className={styles.volatilityMain}>{volatility}</div>
      </td>

      <td className={styles.winRateCell}>
        <div className={styles.winRateMain}>{winRate}</div>
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