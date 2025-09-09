import styles from './Survey.module.css';

interface Option {
  value: string;
  label: string;
}

interface SurveyCheckboxQuestionProps {
  label: string;
  options: Option[];
  selectedValues: string[];
  onChange: (value: string, checked: boolean) => void;
  required?: boolean;
}

export default function SurveyCheckboxQuestion({
  label,
  options,
  selectedValues,
  onChange,
  required = false
}: SurveyCheckboxQuestionProps) {
  return (
    <div className={styles.question}>
      <label className={styles.questionLabel}>
        {label}
        {required && <span className={styles.required}>*</span>}
      </label>
      <div className={styles.options}>
        {options.map((option) => (
          <label key={option.value} className={styles.checkboxLabel}>
            <input
              type='checkbox'
              checked={selectedValues.includes(option.value)}
              onChange={(e) => onChange(option.value, e.target.checked)}
            />
            {option.label}
          </label>
        ))}
      </div>
    </div>
  );
}