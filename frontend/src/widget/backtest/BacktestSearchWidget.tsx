import React from 'react';
import styles from './BacktestSearchWidget.module.css';
import { imgFrame, imgFrame1, imgFrame2 } from '../../assets/imports/svg-uh39g';

interface BacktestSearchWidgetProps {
  onPortfolioModalOpen?: () => void;
  onDirectCreation?: () => void;
}

export default function BacktestSearchWidget({ onPortfolioModalOpen, onDirectCreation }: BacktestSearchWidgetProps) {
  return (
    <div className={styles.searchSection}>
      <div className={styles.searchCard}>
        <h3>백테스트 검색</h3>
        
        <div className={styles.searchControls}>
          {/* 검색창 */}
          <div className={styles.searchInput}>
            <div className={styles.searchIcon}>
              <img src={imgFrame2} alt="검색" />
            </div>
            <input 
              id="backtest-search"
              name="backtestSearch"
              type="text" 
              placeholder="백테스트 이름을 입력하세요"
              className={styles.input}
              aria-label="백테스트 검색"
            />
          </div>

          {/* 생성 버튼들 */}
          <div className={styles.createButtons}>
            <button className={styles.createFromPortfolio} onClick={onPortfolioModalOpen}>
              <img src={imgFrame1} alt="업로드" />
              내 포트폴리오에서 가져오기
            </button>
            
            <button className={styles.createDirect} onClick={onDirectCreation}>
              <img src={imgFrame} alt="추가" />
              백테스트 직접 생성
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}