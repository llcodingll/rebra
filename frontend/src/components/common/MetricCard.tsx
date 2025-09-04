import { ReactNode } from 'react';
import styles from './MetricCard.module.css';

interface MetricCardProps {
  label: string;
  value: string | number;
  subtext?: string;
  valueColor?: 'default' | 'blue' | 'green' | 'red';
  subtextColor?: 'default' | 'green' | 'red';
  icon?: ReactNode;
  className?: string;
}

export default function MetricCard({ 
  label, 
  value, 
  subtext, 
  valueColor = 'default',
  subtextColor = 'default',
  icon,
  className = ""
}: MetricCardProps) {
  const getValueColorClass = (color: string) => {
    switch (color) {
      case 'blue': return styles.blue;
      case 'green': return styles.green;
      case 'red': return styles.red;
      default: return '';
    }
  };

  const getSubtextColorClass = (color: string) => {
    switch (color) {
      case 'green': return styles.green;
      case 'red': return styles.red;
      default: return '';
    }
  };

  return (
    <div className={`${styles.metricCard} ${className}`}>
      {icon && <div className={styles.icon}>{icon}</div>}
      <div className={styles.content}>
        <span className={styles.label}>{label}</span>
        <span className={`${styles.value} ${getValueColorClass(valueColor)}`}>
          {typeof value === 'number' ? value.toLocaleString() : value}
        </span>
        {subtext && (
          <span className={`${styles.subtext} ${getSubtextColorClass(subtextColor)}`}>
            {subtext}
          </span>
        )}
      </div>
    </div>
  );
}