import { ReactNode } from 'react';
import styles from './Survey.module.css';

interface SurveySectionProps {
  title: string;
  children: ReactNode;
  required?: boolean;
}

export default function SurveySection({ title, children, required = false }: SurveySectionProps) {
  return (
    <div className={styles.section}>
      <h2 className={styles.sectionTitle}>
        {required && <span className={styles.required}>*</span>}
        {title}
      </h2>
      {children}
    </div>
  );
}
