import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './BacktestPage.module.css';
import { legacyBacktestData } from '../../mocks/backtest';
import PortfolioSelectionModal from '../../widget/portfolio/PortfolioSelectionModal';
import Pagination from '../../widget/common/Pagination';
import BacktestSearchWidget from '../../widget/backtest/BacktestSearchWidget';
import BacktestHistoryWidget from '../../widget/backtest/BacktestHistoryWidget';

export default function BacktestPage() {
  const navigate = useNavigate();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  const totalPages = Math.ceil(legacyBacktestData.length / itemsPerPage);

  const handlePortfolioModalOpen = () => {
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
  };

  const handlePortfolioSelect = (portfolioId: string) => {
    navigate('/dashboard/backtest/create', { state: { portfolioId } });
  };

  const handleDirectCreation = () => {
    navigate('/dashboard/backtest/create');
  };

  const handleBacktestClick = (backtest: any) => {
    navigate(`/dashboard/backtest/results/${backtest.id}`);
  };


  return (
    <div className={styles.backtest}>
      <div className={styles.container}>
        {/* 백테스트 검색/생성 섹션 */}
        <BacktestSearchWidget
          onPortfolioModalOpen={handlePortfolioModalOpen}
          onDirectCreation={handleDirectCreation}
        />

        {/* 백테스트 히스토리 */}
        <BacktestHistoryWidget
          data={legacyBacktestData}
          onBacktestClick={handleBacktestClick}
          currentPage={currentPage}
          itemsPerPage={itemsPerPage}
        />

        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
        />
      </div>

      <PortfolioSelectionModal 
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onSelect={handlePortfolioSelect}
      />
    </div>
  );
}