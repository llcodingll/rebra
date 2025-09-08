import styles from './Survey.module.css';

interface Agreement {
  key: string;
  label: string;
}

interface AgreementCheckboxesProps {
  agreements: Agreement[];
  values: Record<string, boolean>;
  onChange: (key: string, checked: boolean) => void;
}

export default function AgreementCheckboxes({
  agreements,
  values,
  onChange
}: AgreementCheckboxesProps) {
  return (
    <div className={styles.agreements}>
      {agreements.map((agreement) => (
        <label key={agreement.key} className={styles.agreementLabel}>
          <input
            type='checkbox'
            checked={values[agreement.key] || false}
            onChange={(e) => onChange(agreement.key, e.target.checked)}
          />
          {agreement.label}
        </label>
      ))}
    </div>
  );
}