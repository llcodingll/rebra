import styles from './Survey.module.css';

interface SurveyTextInputProps {
  label: string;
  placeholder: string;
  value: string;
  onChange: (value: string) => void;
}

export default function SurveyTextInput({
  label,
  placeholder,
  value,
  onChange
}: SurveyTextInputProps) {
  return (
    <div className={styles.question}>
      <label className={styles.questionLabel}>{label}</label>
      <input
        type='text'
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className={styles.textInput}
      />
    </div>
  );
}