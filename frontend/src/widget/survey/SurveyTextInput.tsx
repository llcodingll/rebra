import styles from './Survey.module.css';

interface SurveyTextInputProps {
  label: string;
  placeholder: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  showDuplicateCheck?: boolean;
  onDuplicateCheck?: () => void;
  duplicateCheckStatus?: 'none' | 'checking' | 'available' | 'unavailable';
}

export default function SurveyTextInput({
  label,
  placeholder,
  value,
  onChange,
  required = false,
  showDuplicateCheck = false,
  onDuplicateCheck,
  duplicateCheckStatus = 'none'
}: SurveyTextInputProps) {
  return (
    <div className={styles.question}>
      <label className={styles.questionLabel}>
        {label}
        {required && <span className={styles.required}>*</span>}
      </label>
      <div className={styles.inputGroup}>
        <input
          type='text'
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          className={styles.textInput}
        />
        {showDuplicateCheck && (
          <button
            type="button"
            onClick={onDuplicateCheck}
            className={styles.duplicateCheckButton}
            disabled={!value || duplicateCheckStatus === 'checking'}
          >
            {duplicateCheckStatus === 'checking' ? '확인 중...' : '중복 확인'}
          </button>
        )}
      </div>
      {duplicateCheckStatus === 'available' && (
        <div className={styles.statusMessage + ' ' + styles.available}>
          사용 가능한 닉네임입니다.
        </div>
      )}
      {duplicateCheckStatus === 'unavailable' && (
        <div className={styles.statusMessage + ' ' + styles.unavailable}>
          이미 사용 중인 닉네임입니다.
        </div>
      )}
    </div>
  );
}