import { ReactNode } from 'react';
import styles from './Survey.module.css';

interface SurveySectionProps {
  title: string;
  children: ReactNode;
}

export default function SurveySection({ title, children }: SurveySectionProps) {
  return (
    <div className={styles.section}>
      <h2 className={styles.sectionTitle}>{title}</h2>
      {children}
    </div>
  );
}