import styles from './LoadingSpinner.module.css';

interface LoadingSpinnerProps {
  size?: 'small' | 'medium' | 'large';
  className?: string;
}

export default function LoadingSpinner({ size = 'medium', className }: LoadingSpinnerProps) {
  const sizeClass = {
    small: styles.small,
    medium: styles.medium,
    large: styles.large,
  }[size];

  return (
    <div className={`${styles.spinner} ${sizeClass} ${className || ''}`} aria-label="로딩 중">
      <div className={styles.spinnerCircle}></div>
    </div>
  );
}