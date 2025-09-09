import styles from './Survey.module.css';

interface Option {
  value: string;
  label: string;
}

interface SurveyRadioQuestionProps {
  label: string;
  name: string;
  options: Option[];
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
}

export default function SurveyRadioQuestion({
  label,
  name,
  options,
  value,
  onChange,
  required = false
}: SurveyRadioQuestionProps) {
  return (
    <div className={styles.question}>
      <label className={styles.questionLabel}>
        {label}
        {required && <span className={styles.required}>*</span>}
      </label>
      <div className={styles.options}>
        {options.map((option) => (
          <label key={option.value} className={styles.radioLabel}>
            <input
              type='radio'
              name={name}
              value={option.value}
              checked={value === option.value}
              onChange={(e) => onChange(e.target.value)}
            />
            {option.label}
          </label>
        ))}
      </div>
    </div>
  );
}