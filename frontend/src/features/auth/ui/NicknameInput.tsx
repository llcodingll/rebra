import { useState, useEffect } from 'react';
import { authApi } from '../api/authApi';
import { isOk } from '../../../shared/util/result';
import styles from './NicknameInput.module.css';

interface NicknameInputProps {
  value: string;
  onChange: (value: string) => void;
  onValidityChange: (isValid: boolean) => void;
}

type ValidationStatus = 'none' | 'invalid' | 'valid';
type DuplicateStatus = 'none' | 'checking' | 'available' | 'unavailable';

export default function NicknameInput({ value, onChange, onValidityChange }: NicknameInputProps) {
  const [validationStatus, setValidationStatus] = useState<ValidationStatus>('none');
  const [duplicateStatus, setDuplicateStatus] = useState<DuplicateStatus>('none');
  const [validationMessage, setValidationMessage] = useState('');

  const validateNickname = (nickname: string): boolean => {
    if (!nickname) {
      setValidationMessage('');
      setValidationStatus('none');
      return false;
    }

    if (nickname.length < 2 || nickname.length > 8) {
      setValidationMessage('닉네임은 2~8글자로 입력해주세요.');
      setValidationStatus('invalid');
      return false;
    }

    if (/[^a-zA-Z0-9가-힣]/.test(nickname)) {
      setValidationMessage('특수문자 및 공백은 사용할 수 없습니다.');
      setValidationStatus('invalid');
      return false;
    }

    setValidationMessage('');
    setValidationStatus('valid');
    return true;
  };

  const handleInputChange = (newValue: string) => {
    onChange(newValue);
    setDuplicateStatus('none');
    validateNickname(newValue);
  };

  const handleDuplicateCheck = async () => {
    if (!validateNickname(value)) return;

    setDuplicateStatus('checking');

    const result = await authApi.checkNickname(value);
    if (isOk(result)) {
      setDuplicateStatus(result.data.isDuplicated ? 'unavailable' : 'available');
    } else {
      setDuplicateStatus('none');
      setValidationMessage('중복 확인 중 오류가 발생했습니다.');
      setValidationStatus('invalid');
    }
  };

  useEffect(() => {
    const isValid = validationStatus === 'valid' && duplicateStatus === 'available';
    onValidityChange(isValid);
  }, [validationStatus, duplicateStatus, onValidityChange]);

  const showError = validationStatus === 'invalid' || duplicateStatus === 'unavailable';
  const showSuccess = validationStatus === 'valid' && duplicateStatus === 'available';

  return (
    <div className={styles.nicknameInput}>
      <div className={styles.inputGroup}>
        <input
          type="text"
          value={value}
          onChange={(e) => handleInputChange(e.target.value)}
          placeholder="닉네임을 입력해주세요"
          className={`${styles.input} ${showError ? styles.inputError : ''} ${showSuccess ? styles.inputSuccess : ''}`}
        />
        <button
          type="button"
          onClick={handleDuplicateCheck}
          className={styles.checkButton}
          disabled={validationStatus !== 'valid' || duplicateStatus === 'checking'}
        >
          {duplicateStatus === 'checking' ? '확인 중...' : '중복 확인'}
        </button>
      </div>
      
      {validationMessage && (
        <div className={styles.validationMessage}>
          {validationMessage}
        </div>
      )}
      
      {duplicateStatus === 'available' && (
        <div className={styles.successMessage}>
          사용 가능한 닉네임입니다.
        </div>
      )}
      
      {duplicateStatus === 'unavailable' && (
        <div className={styles.errorMessage}>
          이미 사용 중인 닉네임입니다.
        </div>
      )}
    </div>
  );
}