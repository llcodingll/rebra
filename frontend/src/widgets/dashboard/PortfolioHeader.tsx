import styles from './PortfolioHeader.module.css';
import { type Portfolio } from '../../mocks/portfolio';
import { useConfirmModal } from '../../hooks/useModalState';
import ConfirmModal from '../../shared/ui/modal/ConfirmModal';

interface PortfolioHeaderProps {
  selectedPortfolio: Portfolio | null;
  onPortfolioLinkClick: () => void;
}

export default function PortfolioHeader({ selectedPortfolio, onPortfolioLinkClick }: PortfolioHeaderProps) {


  return (
    <>
      <div className={styles.portfolioHeader}>
        <div className={styles.portfolioTitle}>
          <h2>{selectedPortfolio?.name}</h2>
          <span className={styles.portfolioDesc}>{selectedPortfolio?.description}</span>
        </div>
        <div className={styles.portfolioInfo}>
          <button className={styles.portfolioLink} onClick={onPortfolioLinkClick}>
            다른 포트폴리오 보기
          </button>
        </div>
      </div>
    </>
  );
}