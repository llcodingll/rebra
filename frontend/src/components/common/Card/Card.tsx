import { ReactNode } from 'react';
import styles from './Card.module.css';

interface CardProps {
  children: ReactNode;
  title?: string;
  subtitle?: string;
  headerAction?: ReactNode;
  variant?: 'default' | 'outlined' | 'elevated';
  padding?: 'none' | 'sm' | 'md' | 'lg';
  className?: string;
  onClick?: () => void;
  hoverable?: boolean;
}

interface CardHeaderProps {
  children: ReactNode;
  className?: string;
}

interface CardBodyProps {
  children: ReactNode;
  className?: string;
}

interface CardFooterProps {
  children: ReactNode;
  className?: string;
}

export function CardHeader({ children, className = '' }: CardHeaderProps) {
  return (
    <div className={`${styles.header} ${className}`}>
      {children}
    </div>
  );
}

export function CardBody({ children, className = '' }: CardBodyProps) {
  return (
    <div className={`${styles.body} ${className}`}>
      {children}
    </div>
  );
}

export function CardFooter({ children, className = '' }: CardFooterProps) {
  return (
    <div className={`${styles.footer} ${className}`}>
      {children}
    </div>
  );
}

export default function Card({
  children,
  title,
  subtitle,
  headerAction,
  variant = 'default',
  padding = 'md',
  className = '',
  onClick,
  hoverable = false
}: CardProps) {
  const cardClasses = [
    styles.card,
    styles[variant],
    styles[`padding-${padding}`],
    (onClick || hoverable) && styles.hoverable,
    onClick && styles.clickable,
    className
  ].filter(Boolean).join(' ');

  const hasHeader = title || subtitle || headerAction;

  return (
    <div className={cardClasses} onClick={onClick}>
      {hasHeader && (
        <CardHeader>
          <div className={styles.headerContent}>
            <div className={styles.headerText}>
              {title && <h3 className={styles.title}>{title}</h3>}
              {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
            </div>
            {headerAction && (
              <div className={styles.headerAction}>
                {headerAction}
              </div>
            )}
          </div>
        </CardHeader>
      )}
      
      <CardBody>
        {children}
      </CardBody>
    </div>
  );
}