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
}

export default function SurveyRadioQuestion({
  label,
  name,
  options,
  value,
  onChange
}: SurveyRadioQuestionProps) {
  return (
    <div className={styles.question}>
      <label className={styles.questionLabel}>{label}</label>
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