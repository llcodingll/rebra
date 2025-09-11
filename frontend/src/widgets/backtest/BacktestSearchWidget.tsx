import React from 'react';
import { motion } from 'motion/react';
import { Search, Upload, Plus } from 'lucide-react';
import styles from './BacktestSearchWidget.module.css';

interface BacktestSearchWidgetProps {
  onPortfolioModalOpen?: () => void;
  onDirectCreation?: () => void;
}

export default function BacktestSearchWidget({ onPortfolioModalOpen, onDirectCreation }: BacktestSearchWidgetProps) {
  return (
    <motion.div 
      className={styles.searchSection}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6 }}
    >
      <div className={styles.searchCard}>
        {/* 헤더 */}
        <div className={styles.cardHeader}>
          <h3 className={styles.cardTitle}>백테스트 검색</h3>
          <p className={styles.cardDescription}>
            기존 백테스트를 검색하거나 새로운 전략을 생성해보세요
          </p>
        </div>
        
        <div className={styles.searchControls}>
          {/* 검색창 */}
          <div className={styles.searchInputContainer}>
            <div className={styles.searchIcon}>
              <Search className={styles.searchIconSvg} />
            </div>
            <input 
              id="backtest-search"
              name="backtestSearch"
              type="text" 
              placeholder="백테스트 이름을 입력하세요"
              className={styles.searchInput}
              aria-label="백테스트 검색"
            />
          </div>

          {/* 생성 버튼들 */}
          <div className={styles.createButtons}>
            <button 
              className={styles.createFromPortfolio} 
              onClick={onPortfolioModalOpen}
            >
              <Upload className={styles.buttonIcon} />
              내 포트폴리오에서 가져오기
            </button>
            
            <button 
              className={styles.createDirect} 
              onClick={onDirectCreation}
            >
              <Plus className={styles.buttonIcon} />
              백테스트 직접 생성
            </button>
          </div>
        </div>
      </div>
    </motion.div>
  );
}