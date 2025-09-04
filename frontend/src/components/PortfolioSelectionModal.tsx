import { useState } from 'react';
import styles from './PortfolioSelectionModal.module.css';

interface PortfolioSelectionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelect: (portfolioId: string) => void;
}

export default function PortfolioSelectionModal({ isOpen, onClose, onSelect }: PortfolioSelectionModalProps) {
  const [selectedPortfolioId, setSelectedPortfolioId] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  const handlePortfolioClick = (portfolioId: string) => {
    setSelectedPortfolioId(portfolioId);
  };

  const handleSelectComplete = () => {
    if (selectedPortfolioId) {
      onSelect(selectedPortfolioId);
      onClose();
      setSelectedPortfolioId(null);
    }
  };

  return (
    <div className={styles.backdrop} onClick={handleBackdropClick}>
      <div className={styles.modal}>
        <div className={styles.header}>
          <h2 className={styles.title}>포트폴리오 선택</h2>
          <p className={styles.subtitle}>포트폴리오를 선택해주세요</p>
        </div>

        <div className={styles.portfolioList}>
          <div className={`${styles.portfolioItem} ${selectedPortfolioId === 'portfolio-1' ? styles.selected : ''}`} onClick={() => handlePortfolioClick('portfolio-1')}>
            <div className={`${styles.checkbox} ${selectedPortfolioId === 'portfolio-1' ? styles.checked : ''}`}></div>
            <div className={styles.portfolioInfo}>
              <h3 className={styles.portfolioName}>삼성전자 + SK하이닉스 포트폴리오</h3>
              <span className={styles.portfolioReturn}>+24.5%</span>
            </div>
            <div className={styles.portfolioMeta}>
              <span className={styles.stockCount}>3개 종목</span>
              <span className={styles.createdDate}>생성일: 2024-01-15</span>
            </div>
          </div>

          <div className={`${styles.portfolioItem} ${selectedPortfolioId === 'portfolio-2' ? styles.selected : ''}`} onClick={() => handlePortfolioClick('portfolio-2')}>
            <div className={`${styles.checkbox} ${selectedPortfolioId === 'portfolio-2' ? styles.checked : ''}`}></div>
            <div className={styles.portfolioInfo}>
              <h3 className={styles.portfolioName}>배당 중심 포트폴리오</h3>
              <span className={styles.portfolioReturn}>+18.2%</span>
            </div>
            <div className={styles.portfolioMeta}>
              <span className={styles.stockCount}>5개 종목</span>
              <span className={styles.createdDate}>생성일: 2024-02-10</span>
            </div>
          </div>

          <div className={`${styles.portfolioItem} ${selectedPortfolioId === 'portfolio-3' ? styles.selected : ''}`} onClick={() => handlePortfolioClick('portfolio-3')}>
            <div className={`${styles.checkbox} ${selectedPortfolioId === 'portfolio-3' ? styles.checked : ''}`}></div>
            <div className={styles.portfolioInfo}>
              <h3 className={styles.portfolioName}>성장주 포트폴리오</h3>
              <span className={styles.portfolioReturn}>+32.8%</span>
            </div>
            <div className={styles.portfolioMeta}>
              <span className={styles.stockCount}>8개 종목</span>
              <span className={styles.createdDate}>생성일: 2024-03-05</span>
            </div>
          </div>

          <div className={`${styles.portfolioItem} ${selectedPortfolioId === 'portfolio-4' ? styles.selected : ''}`} onClick={() => handlePortfolioClick('portfolio-4')}>
            <div className={`${styles.checkbox} ${selectedPortfolioId === 'portfolio-4' ? styles.checked : ''}`}></div>
            <div className={styles.portfolioInfo}>
              <h3 className={styles.portfolioName}>안전자산 포트폴리오</h3>
              <span className={styles.portfolioReturn}>+12.1%</span>
            </div>
            <div className={styles.portfolioMeta}>
              <span className={styles.stockCount}>4개 종목</span>
              <span className={styles.createdDate}>생성일: 2024-01-20</span>
            </div>
          </div>

          <div className={`${styles.portfolioItem} ${selectedPortfolioId === 'portfolio-5' ? styles.selected : ''}`} onClick={() => handlePortfolioClick('portfolio-5')}>
            <div className={`${styles.checkbox} ${selectedPortfolioId === 'portfolio-5' ? styles.checked : ''}`}></div>
            <div className={styles.portfolioInfo}>
              <h3 className={styles.portfolioName}>테크주 포트폴리오</h3>
              <span className={styles.portfolioReturn}>+28.9%</span>
            </div>
            <div className={styles.portfolioMeta}>
              <span className={styles.stockCount}>6개 종목</span>
              <span className={styles.createdDate}>생성일: 2024-02-28</span>
            </div>
          </div>
        </div>

        <div className={styles.footer}>
          <button className={styles.cancelButton} onClick={onClose}>
            취소
          </button>
          <button 
            className={styles.selectButton} 
            disabled={!selectedPortfolioId}
            onClick={handleSelectComplete}
          >
            선택 완료
          </button>
        </div>
      </div>
    </div>
  );
}