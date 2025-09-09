import { useState, React } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Check, TrendingUp, Calendar, Layers3 } from 'lucide-react';
import styles from './PortfolioSelectionModal.module.css';

interface PortfolioSelectionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelect: (portfolioId: string) => void;
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

const portfolios: Portfolio[] = [
  {
    id: 'portfolio-1',
    name: '삼성전자 + SK하이닉스 포트폴리오',
    return: '+24.5%',
    stockCount: 3,
    createdDate: '2024-01-15',
    returnPositive: true,
    description: '반도체 대장주 중심'
  },
  {
    id: 'portfolio-2',
    name: '배당 중심 포트폴리오',
    return: '+18.2%',
    stockCount: 5,
    createdDate: '2024-02-10',
    returnPositive: true,
    description: '안정적인 배당 수익'
  },
  {
    id: 'portfolio-3',
    name: '성장주 포트폴리오',
    return: '+32.8%',
    stockCount: 8,
    createdDate: '2024-03-05',
    returnPositive: true,
    description: '고성장 기업 투자'
  },
  {
    id: 'portfolio-4',
    name: '안전자산 포트폴리오',
    return: '+12.1%',
    stockCount: 4,
    createdDate: '2024-01-20',
    returnPositive: true,
    description: '리스크 최소화'
  },
  {
    id: 'portfolio-5',
    name: '테크주 포트폴리오',
    return: '+28.9%',
    stockCount: 6,
    createdDate: '2024-02-28',
    returnPositive: true,
    description: '기술 혁신 기업'
  },
  {
    id: 'portfolio-6',
    name: '글로벌 포트폴리오',
    return: '-5.2%',
    stockCount: 12,
    createdDate: '2024-03-15',
    returnPositive: false,
    description: '해외 주식 분산투자'
  }
];

export default function PortfolioSelectionModal({ isOpen, onClose, onSelect }: PortfolioSelectionModalProps) {
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
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}