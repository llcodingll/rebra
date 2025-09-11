import { useState } from 'react';
import { motion } from 'motion/react';
import { X, CreditCard, Plus, ChevronDown } from 'lucide-react';
import styles from './PortfolioCreateModal.module.css';
import AccountRegisterModal from './AccountRegisterModal';

interface PortfolioCreateModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreatePortfolio: (portfolioData: PortfolioCreateData) => void;
}

interface PortfolioCreateData {
  name: string;
  purpose: string;
  accountNumber: string;
}

interface AccountRegisterData {
  accountNumber: string;
  appKey: string;
  secretKey: string;
}

export default function PortfolioCreateModal({ 
  isOpen, 
  onClose, 
  onCreatePortfolio 
}: PortfolioCreateModalProps) {
  const [portfolioName, setPortfolioName] = useState('');
  const [portfolioPurpose, setPortfolioPurpose] = useState('');
  const [selectedAccount, setSelectedAccount] = useState('');
  const [isAccountRegisterModalOpen, setIsAccountRegisterModalOpen] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!portfolioName.trim() || !portfolioPurpose.trim() || !selectedAccount) {
      alert('모든 필드를 입력해주세요.');
      return;
    }

    onCreatePortfolio({
      name: portfolioName,
      purpose: portfolioPurpose,
      accountNumber: selectedAccount
    });

    // 폼 초기화
    setPortfolioName('');
    setPortfolioPurpose('');
    setSelectedAccount('');
    onClose();
  };

  const handleAccountSelect = () => {
    console.log('계좌 선택 모달 열기');
    // 추후 계좌 선택 모달 연결
  };

  const handleNewAccountRegister = () => {
    setIsAccountRegisterModalOpen(true);
  };

  const handleAccountRegisterModalClose = () => {
    setIsAccountRegisterModalOpen(false);
  };

  const handleAccountRegister = (accountData: AccountRegisterData) => {
    console.log('새 계좌 등록:', accountData);
    // 실제 API 호출 및 계좌 등록 로직 구현 예정
    
    // 등록 성공 시 해당 계좌를 선택된 계좌로 설정
    setSelectedAccount(accountData.accountNumber);
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
          <h2 className={styles.title}>포트폴리오 등록</h2>
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
          {/* 포트폴리오 이름 */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              포트폴리오 이름 <span className={styles.required}>*</span>
            </label>
            <input
              type="text"
              value={portfolioName}
              onChange={(e) => setPortfolioName(e.target.value)}
              placeholder="예: 성장주 중심 포트폴리오"
              className={styles.input}
              maxLength={50}
            />
            <div className={styles.inputHelper}>
              {portfolioName.length}/50
            </div>
          </div>

          {/* 포트폴리오 목적 */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              포트폴리오 목적 <span className={styles.required}>*</span>
            </label>
            <textarea
              value={portfolioPurpose}
              onChange={(e) => setPortfolioPurpose(e.target.value)}
              placeholder="예: 장기 성장을 목표로 한 기술주 중심의 투자 전략"
              className={styles.textarea}
              rows={3}
              maxLength={200}
            />
            <div className={styles.inputHelper}>
              {portfolioPurpose.length}/200
            </div>
          </div>

          {/* 계좌 선택 섹션 */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              계좌 선택 <span className={styles.required}>*</span>
            </label>
            
            {/* 선택된 계좌 표시 또는 선택 버튼 */}
            <div className={styles.accountSection}>
              {selectedAccount ? (
                <div className={styles.selectedAccount}>
                  <CreditCard className={styles.accountIcon} />
                  <div className={styles.accountInfo}>
                    <span className={styles.accountNumber}>{selectedAccount}</span>
                    <span className={styles.accountType}>투자계좌</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => setSelectedAccount('')}
                    className={styles.changeButton}
                  >
                    변경
                  </button>
                </div>
              ) : (
                <div className={styles.accountSelector}>
                  <button
                    type="button"
                    onClick={handleAccountSelect}
                    className={styles.selectAccountButton}
                  >
                    <CreditCard className={styles.buttonIcon} />
                    계좌 선택하기
                    <ChevronDown className={styles.chevronIcon} />
                  </button>
                </div>
              )}
            </div>

            {/* 새 계좌 등록 버튼 */}
            <button
              type="button"
              onClick={handleNewAccountRegister}
              className={styles.newAccountButton}
            >
              <Plus className={styles.buttonIcon} />
              새로운 계좌 등록
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
              포트폴리오 생성
            </button>
          </div>
        </form>

        {/* 계좌 등록 모달 */}
        <AccountRegisterModal
          isOpen={isAccountRegisterModalOpen}
          onClose={handleAccountRegisterModalClose}
          onRegisterAccount={handleAccountRegister}
        />
      </motion.div>
    </div>
  );
}