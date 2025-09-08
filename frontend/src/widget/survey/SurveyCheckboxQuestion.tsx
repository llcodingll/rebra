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
}

export default function SurveyCheckboxQuestion({
  label,
  options,
  selectedValues,
  onChange
}: SurveyCheckboxQuestionProps) {
  return (
    <div className={styles.question}>
      <label className={styles.questionLabel}>{label}</label>
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