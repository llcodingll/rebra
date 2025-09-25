import { useState, useEffect } from 'react';
import { motion } from 'motion/react';
import { X, CreditCard, Plus, ChevronDown } from 'lucide-react';
import styles from './PortfolioCreateModal.module.css';
import AccountRegisterModal from './AccountRegisterModal';
import { useApi } from '../../shared/hook/useApi';
import { accountApi } from '../../features/account/api/accountApi';
import { transformAccountData } from '../../features/account/utils/accountTransform';
import type { Account } from '../../mocks/account';
import { useApiMutation } from '../../shared/hook/useApi';
import { portfolioApi } from '../../features/portfolio/api/portfolioApi';

interface PortfolioCreateModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void; // 성공 시 부모에게 알림 (선택적)
}

interface AccountRegisterData {
  accountNumber: string;
  appKey: string;
  secretKey: string;
}

interface Account {
  accountId: number;
  accountNumber: string;
  accountType: 'REAL' | 'MOCK';
  brokerName: string;
  connectionStatus: 'CONNECTED' | 'DISCONNECTED';
  registeredAt: string;
}

export default function PortfolioCreateModal({ 
  isOpen,
  onClose,
  onSuccess
}: PortfolioCreateModalProps) {
  const [portfolioName, setPortfolioName] = useState('');
  const [portfolioPurpose, setPortfolioPurpose] = useState('');
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
  const [isAccountRegisterModalOpen, setIsAccountRegisterModalOpen] = useState(false);
  const [isAccountDropdownOpen, setIsAccountDropdownOpen] = useState(false);

  // 폼 초기화 함수
  const resetForm = () => {
    setPortfolioName('');
    setPortfolioPurpose('');
    setSelectedAccount(null);
    setIsAccountDropdownOpen(false);
  };

  // 포트폴리오 생성 mutation
  const { mutate: createPortfolio, isPending: isCreating } = useApiMutation({
    apiFunction: portfolioApi.createPortfolio,
    onSuccess: (data) => {
      console.log('포트폴리오 생성 성공:', data);
      alert('포트폴리오가 성공적으로 생성되었습니다.');
      resetForm();
      onClose();
      onSuccess?.(); // 부모에게 성공 알림
    },
    onError: (error) => {
      console.error('포트폴리오 생성 실패:', error);
      alert('포트폴리오 생성에 실패했습니다. 다시 시도해주세요.');
    }
  });
  
  // API로 계좌 목록 조회
  const { data: accountData, isLoading: isAccountLoading, refetch: refetchAccounts } = useApi({
    queryKey: ['accounts'],
    apiFunction: () => accountApi.getAccountList(),
    enabled: isOpen, // 모달이 열릴 때만 API 호출
  });

  // API 데이터를 Account 타입으로 변환
  const accounts = accountData?.accounts ? transformAccountData(accountData.accounts) : [];

  // 백업용 목데이터 (API 실패 시)
  const mockAccounts: Account[] = [
    {
      accountId: 1,
      accountNumber: "1234-56-7890**",
      accountType: "REAL",
      brokerName: "한국투자증권",
      connectionStatus: "CONNECTED",
      registeredAt: "2024-08-15T10:30:00Z"
    },
    {
      accountId: 2,
      accountNumber: "9876-54-3210**",
      accountType: "MOCK",
      brokerName: "한국투자증권",
      connectionStatus: "CONNECTED",
      registeredAt: "2024-08-20T14:15:00Z"
    },
    {
      accountId: 3,
      accountNumber: "5555-11-2233**",
      accountType: "REAL",
      brokerName: "키움증권",
      connectionStatus: "CONNECTED",
      registeredAt: "2024-07-10T09:00:00Z"
    },
    {
      accountId: 4,
      accountNumber: "7777-88-9999**",
      accountType: "MOCK",
      brokerName: "미래에셋증권",
      connectionStatus: "DISCONNECTED",
      registeredAt: "2024-06-25T16:45:00Z"
    }
  ];

  // 입력 검증 함수
  const validateForm = () => {
    if (!portfolioName.trim()) {
      alert('포트폴리오 이름을 입력해주세요.');
      return false;
    }

    if (!portfolioPurpose.trim()) {
      alert('투자 목적을 입력해주세요.');
      return false;
    }

    if (!selectedAccount) {
      alert('계좌를 선택해주세요.');
      return false;
    }

    return true;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) return;

    // API 호출
    createPortfolio({
      name: portfolioName,
      description: portfolioPurpose,
      accountId: selectedAccount.accountId
    });
  };

  const handleAccountSelect = () => {
    setIsAccountDropdownOpen(!isAccountDropdownOpen);
  };

  const handleAccountChoice = (account: Account) => {
    setSelectedAccount(account);
    setIsAccountDropdownOpen(false);
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
              placeholder=""
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
              placeholder=""
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
                    <span className={styles.accountNumber}>{selectedAccount.accountNumber}</span>
                    <span className={styles.accountType}>{selectedAccount.brokerName} ({selectedAccount.accountType})</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => setSelectedAccount(null)}
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
                  
                  {/* 계좌 드롭다운 */}
                  {isAccountDropdownOpen && (
                    <div className={styles.accountDropdown}>
                      {accounts.map((account) => (
                        <button
                          key={account.accountId}
                          type="button"
                          onClick={() => handleAccountChoice(account)}
                          className={styles.accountItem}
                        >
                          <div className={styles.accountItemLeft}>
                            <CreditCard className={styles.accountItemIcon} />
                            <div className={styles.accountItemInfo}>
                              <span className={styles.accountItemNumber}>{account.accountNumber}</span>
                              <span className={styles.accountItemBroker}>{account.brokerName}</span>
                            </div>
                          </div>
                          <span className={`${styles.accountItemType} ${account.accountType === 'REAL' ? styles.real : styles.mock}`}>
                            {account.accountType === 'REAL' ? '실제' : '모의'}
                          </span>
                        </button>
                      ))}
                      
                      {accounts.length === 0 && !isAccountLoading && (
                        <div className={styles.noAccounts}>
                          연결된 계좌가 없습니다
                        </div>
                      )}
                    </div>
                  )}
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
              onClick={() => {
                resetForm();
                onClose();
              }}
              className={styles.cancelButton}
            >
              취소
            </button>
            <button
              type="submit"
              className={styles.submitButton}
              disabled={isCreating}
            >
              {isCreating ? '생성 중...' : '포트폴리오 생성'}
            </button>
          </div>
        </form>

        {/* 계좌 등록 모달 */}
        <AccountRegisterModal
          isOpen={isAccountRegisterModalOpen}
          onClose={handleAccountRegisterModalClose}
          onSuccess={() => {
            // 계좌 등록 성공 시 계좌 목록 새로고침
            refetchAccounts();
          }}
        />
      </motion.div>
    </div>
  );
}