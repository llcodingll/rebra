import styles from './PageHeader.module.css';

interface PageHeaderProps {
  title: string;
  onBack: () => void;
  backText?: string;
  className?: string;
}

export default function PageHeader({ 
  title, 
  onBack, 
  backText = "뒤로 가기",
  className = ""
}: PageHeaderProps) {
  return (
    <div className={`${styles.header} ${className}`}>
      <button className={styles.backButton} onClick={onBack}>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
          <path d="M19 12H5M12 19L5 12L12 5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
        {backText}
      </button>
      <h1 className={styles.title}>{title}</h1>
    </div>
  );
}