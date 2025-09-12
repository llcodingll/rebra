import { useState, React } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Check, TrendingUp, Calendar, Layers3 } from 'lucide-react';
import styles from './PortfolioSelectionModal.module.css';

interface PortfolioSelectionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelect: (portfolioId: string) => void;
  portfolios: Portfolio[];
  onCreatePortfolio?: () => void;
}

interface Portfolio {
  id: string;
  name: string;
  return: string;
  stockCount: number;
  createdDate: string;
  returnPositive: boolean;
  description?: string;
}

export default function PortfolioSelectionModal({ isOpen, onClose, onSelect, portfolios, onCreatePortfolio }: PortfolioSelectionModalProps) {
  const [selectedPortfolioId, setSelectedPortfolioId] = useState<string | null>(null);

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

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className={styles.backdrop}
          onClick={handleBackdropClick}
        >
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            transition={{ type: "spring", duration: 0.5 }}
            className={styles.modal}
            onClick={(e) => e.stopPropagation()}
          >
            {/* Header */}
            <div className={styles.header}>
              <h2 className={styles.title}>포트폴리오 선택</h2>
              <p className={styles.subtitle}>포트폴리오를 선택해주세요</p>
            </div>

            {/* Portfolio Grid */}
            <div className={styles.portfolioGrid}>
              <div className={styles.gridContainer}>
                {portfolios.map((portfolio, index) => (
                  <motion.div
                    key={portfolio.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.1 }}
                    className={`${styles.portfolioCard} ${
                      selectedPortfolioId === portfolio.id ? styles.selected : ''
                    }`}
                    onClick={() => handlePortfolioClick(portfolio.id)}
                  >
                    {/* Card Header */}
                    <div className={styles.cardHeader}>
                      <div className={styles.cardHeaderContent}>
                        <h3 className={styles.portfolioName}>
                          {portfolio.name}
                        </h3>
                        {portfolio.description && (
                          <p className={styles.portfolioDescription}>
                            {portfolio.description}
                          </p>
                        )}
                      </div>
                      <motion.div
                        initial={false}
                        animate={{
                          scale: selectedPortfolioId === portfolio.id ? 1 : 0.9,
                        }}
                        className={`${styles.checkbox} ${
                          selectedPortfolioId === portfolio.id ? styles.checked : ''
                        }`}
                      >
                        {selectedPortfolioId === portfolio.id && (
                          <motion.div
                            initial={{ scale: 0 }}
                            animate={{ scale: 1 }}
                            transition={{ type: "spring", duration: 0.3 }}
                          >
                            <Check className={styles.checkIcon} />
                          </motion.div>
                        )}
                      </motion.div>
                    </div>

                    {/* Performance */}
                    <div className={styles.cardContent}>
                      <div className={styles.performanceRow}>
                        <div className={styles.returnSection}>
                          <TrendingUp className={`${styles.trendIcon} ${
                            portfolio.returnPositive ? styles.positive : styles.negative
                          }`} />
                          <span className={`${styles.returnValue} ${
                            portfolio.returnPositive ? styles.positive : styles.negative
                          }`}>
                            {portfolio.return}
                          </span>
                        </div>
                        <div className={styles.stockCountBadge}>
                          <Layers3 className={styles.stockIcon} />
                          <span className={styles.stockCount}>
                            {portfolio.stockCount}개 종목
                          </span>
                        </div>
                      </div>

                      {/* Creation Date */}
                      <div className={styles.metaInfo}>
                        <div className={styles.createdDate}>
                          <Calendar className={styles.calendarIcon} />
                          <span>생성일: {formatDate(portfolio.createdDate)}</span>
                        </div>
                      </div>
                    </div>
                  </motion.div>
                ))}
              </div>
            </div>

            {/* Footer */}
            <div className={styles.footer}>
              <div className={styles.footerLeft}>
                {onCreatePortfolio && (
                  <button 
                    className={styles.createButton}
                    onClick={() => {
                      onCreatePortfolio();
                      onClose();
                    }}
                  >
                    + 포트폴리오 추가하기
                  </button>
                )}
              </div>
              <div className={styles.footerRight}>
                <button 
                  className={styles.cancelButton}
                  onClick={onClose}
                >
                  취소
                </button>
                <button 
                  className={`${styles.selectButton} ${
                    !selectedPortfolioId ? styles.disabled : ''
                  }`}
                  onClick={handleSelectComplete}
                  disabled={!selectedPortfolioId}
                >
                  선택 완료
                </button>
              </div>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}