import { ReactNode } from 'react';
import styles from './TableLayoutContainer.module.css';

interface TableLayoutContainerProps {
  controls: ReactNode;
  table: ReactNode;
}

export default function TableLayoutContainer({ controls, table }: TableLayoutContainerProps) {
  return (
    <div className={styles.tableLayoutContainer}>
      <div className={styles.controlsArea}>
        {controls}
      </div>
      
      <div className={styles.divider}></div>
      
      <div className={styles.tableArea}>
        {table}
      </div>
    </div>
  );
}