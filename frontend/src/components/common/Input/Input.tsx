import { ReactNode, InputHTMLAttributes, forwardRef } from 'react';
import styles from './Input.module.css';

type InputVariant = 'default' | 'search' | 'date' | 'number';
type InputSize = 'sm' | 'md' | 'lg';

interface InputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'size'> {
  variant?: InputVariant;
  size?: InputSize;
  label?: string;
  error?: string;
  helperText?: string;
  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
  fullWidth?: boolean;
  className?: string;
  containerClassName?: string;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({
    variant = 'default',
    size = 'md',
    label,
    error,
    helperText,
    leftIcon,
    rightIcon,
    fullWidth = false,
    className = '',
    containerClassName = '',
    disabled,
    ...props
  }, ref) => {
    const containerClasses = [
      styles.container,
      fullWidth && styles.fullWidth,
      containerClassName
    ].filter(Boolean).join(' ');

    const inputClasses = [
      styles.input,
      styles[variant],
      styles[size],
      leftIcon && styles.hasLeftIcon,
      rightIcon && styles.hasRightIcon,
      error && styles.error,
      disabled && styles.disabled,
      className
    ].filter(Boolean).join(' ');

    return (
      <div className={containerClasses}>
        {label && (
          <label className={styles.label}>
            {label}
            {props.required && <span className={styles.required}>*</span>}
          </label>
        )}
        
        <div className={styles.inputWrapper}>
          {leftIcon && (
            <div className={styles.leftIcon}>
              {leftIcon}
            </div>
          )}
          
          <input
            ref={ref}
            className={inputClasses}
            disabled={disabled}
            {...props}
          />
          
          {rightIcon && (
            <div className={styles.rightIcon}>
              {rightIcon}
            </div>
          )}
        </div>
        
        {error && (
          <div className={styles.errorText}>
            {error}
          </div>
        )}
        
        {!error && helperText && (
          <div className={styles.helperText}>
            {helperText}
          </div>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export default Input;