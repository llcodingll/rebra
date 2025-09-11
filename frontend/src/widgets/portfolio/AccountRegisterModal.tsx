import { useState } from 'react';
import { motion } from 'motion/react';
import { X, CreditCard, Key, Eye, EyeOff, HelpCircle } from 'lucide-react';
import styles from './AccountRegisterModal.module.css';

interface AccountRegisterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onRegisterAccount: (accountData: AccountRegisterData) => void;
}

interface AccountRegisterData {
  accountNumber: string;
  appKey: string;
  secretKey: string;
}

export default function AccountRegisterModal({ 
  isOpen, 
  onClose, 
  onRegisterAccount 
}: AccountRegisterModalProps) {
  const [accountNumber, setAccountNumber] = useState('');
  const [appKey, setAppKey] = useState('');
  const [secretKey, setSecretKey] = useState('');
  const [showAppKey, setShowAppKey] = useState(false);
  const [showSecretKey, setShowSecretKey] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!accountNumber.trim() || !appKey.trim() || !secretKey.trim()) {
      alert('모든 필드를 입력해주세요.');
      return;
    }

    // 계좌번호 형식 간단 검증
    if (!/^\d{8,12}$/.test(accountNumber.replace(/-/g, ''))) {
      alert('올바른 계좌번호를 입력해주세요.');
      return;
    }

    onRegisterAccount({
      accountNumber,
      appKey,
      secretKey
    });

    // 폼 초기화
    setAccountNumber('');
    setAppKey('');
    setSecretKey('');
    setShowAppKey(false);
    setShowSecretKey(false);
    onClose();
  };

  const handleKeyRegistrationGuide = () => {
    console.log('키 등록 방법 안내 모달/페이지 열기');
    // 추후 키 등록 방법 안내 모달 또는 페이지 연결
  };

  const formatAccountNumber = (value: string) => {
    // 숫자만 추출
    const numbers = value.replace(/[^\d]/g, '');
    // 계좌번호 형식에 맞게 하이픈 추가 (예: 123-456789-01)
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 9) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 9)}-${numbers.slice(9, 11)}`;
  };

  const handleAccountNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatAccountNumber(e.target.value);
    if (formatted.replace(/[^\d]/g, '').length <= 11) {
      setAccountNumber(formatted);
    }
  };

  if (!isOpen) return null;

  return (
    <div className={styles.overlay}>
      <motion.div
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.8 }}
        transition={{ duration: 0.3 }}
        className={styles.modal}
      >
        {/* 모달 헤더 */}
        <div className={styles.header}>
          <div className={styles.titleSection}>
            <CreditCard className={styles.titleIcon} />
            <h2 className={styles.title}>새로운 계좌 등록</h2>
          </div>
          <button 
            className={styles.closeButton}
            onClick={onClose}
            type="button"
          >
            <X className={styles.closeIcon} />
          </button>
        </div>

        {/* 모달 콘텐츠 */}
        <form onSubmit={handleSubmit} className={styles.form}>
          {/* 안내 메시지 */}
          <div className={styles.infoMessage}>
            <div className={styles.infoIcon}>
              <Key className={styles.keyIcon} />
            </div>
            <div className={styles.infoText}>
              <p>API 키를 통해 안전하게 계좌 정보를 연동합니다</p>
              <span>입력하신 정보는 암호화되어 저장됩니다</span>
            </div>
          </div>

          {/* 계좌번호 */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              계좌번호 <span className={styles.required}>*</span>
            </label>
            <input
              type="text"
              value={accountNumber}
              onChange={handleAccountNumberChange}
              placeholder="123-456789-01"
              className={styles.input}
              maxLength={13}
            />
            <div className={styles.inputHelper}>
              하이픈(-)을 포함하여 입력해주세요
            </div>
          </div>

          {/* 앱키 */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              앱키 (App Key) <span className={styles.required}>*</span>
            </label>
            <div className={styles.passwordContainer}>
              <input
                type={showAppKey ? "text" : "password"}
                value={appKey}
                onChange={(e) => setAppKey(e.target.value)}
                placeholder="앱키를 입력해주세요"
                className={styles.input}
              />
              <button
                type="button"
                onClick={() => setShowAppKey(!showAppKey)}
                className={styles.passwordToggle}
              >
                {showAppKey ? <EyeOff className={styles.eyeIcon} /> : <Eye className={styles.eyeIcon} />}
              </button>
            </div>
          </div>

          {/* 시크릿키 */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              시크릿키 (Secret Key) <span className={styles.required}>*</span>
            </label>
            <div className={styles.passwordContainer}>
              <input
                type={showSecretKey ? "text" : "password"}
                value={secretKey}
                onChange={(e) => setSecretKey(e.target.value)}
                placeholder="시크릿키를 입력해주세요"
                className={styles.input}
              />
              <button
                type="button"
                onClick={() => setShowSecretKey(!showSecretKey)}
                className={styles.passwordToggle}
              >
                {showSecretKey ? <EyeOff className={styles.eyeIcon} /> : <Eye className={styles.eyeIcon} />}
              </button>
            </div>
          </div>

          {/* 키 등록 방법 안내 버튼 */}
          <div className={styles.helpSection}>
            <button
              type="button"
              onClick={handleKeyRegistrationGuide}
              className={styles.helpButton}
            >
              <HelpCircle className={styles.helpIcon} />
              키 등록 방법 알아보기
            </button>
          </div>

          {/* 제출 버튼 */}
          <div className={styles.formActions}>
            <button
              type="button"
              onClick={onClose}
              className={styles.cancelButton}
            >
              취소
            </button>
            <button
              type="submit"
              className={styles.submitButton}
            >
              등록
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  );
}