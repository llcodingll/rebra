import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import styles from './BacktestPage.module.css';
import Pagination from '../../widgets/common/Pagination';
import BacktestHistoryWidget from '../../widgets/backtest/BacktestHistoryWidget';
import TutorialOverlay from '../../widgets/tutorial/TutorialOverlay';
import DeleteConfirmModal from '../../widgets/common/DeleteConfirmModal';
import { backtestTutorialSteps } from '../../widgets/tutorial/backtestTutorialSteps';
import { getBacktestList, deleteBacktest, type BacktestListResponse, type PageResponse } from '../../features/backtest/api/backtestApi';

export default function BacktestPage() {
  const navigate = useNavigate();
  const { tutorialStates, closeTutorial } = useOutletContext<{
    tutorialStates: { dashboard: boolean; search: boolean; backtest: boolean };
    closeTutorial: (page: 'dashboard' | 'search' | 'backtest') => void;
  }>();
  const [currentPage, setCurrentPage] = useState(1);
  const [backtestData, setBacktestData] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [backtestToDelete, setBacktestToDelete] = useState<BacktestListResponse | null>(null);
  const itemsPerPage = 10;

  const handleDirectCreation = () => {
    navigate('/backtest/create');
  };

  // 백테스트 목록 조회 함수
  const fetchBacktestList = async () => {
    try {
      setError(null);
      const result = await getBacktestList(currentPage - 1, itemsPerPage);
      if (result.success) {
        setBacktestData(result.data?.content || []);
        setTotalPages(result.data?.totalPages || 0);
      } else {
        setError(new Error(result.error.message));
      }
    } catch (err) {
      setError(err as Error);
    } finally {
      setIsLoading(false);
    }
  };

  // 3초마다 폴링
  useEffect(() => {
    fetchBacktestList(); // 첫 로드

    const interval = setInterval(fetchBacktestList, 3000);
    return () => clearInterval(interval);
  }, [currentPage]);

  // 백테스트 삭제 mutation
  const deleteMutation = useMutation({
    mutationFn: deleteBacktest,
    onSuccess: () => {
      // 삭제 후 즉시 새로고침
      fetchBacktestList();
    },
  });



  const handleBacktestClick = (backtest: any) => {
    navigate(`/backtest/results/${backtest.id}`);
  };

  const handleBacktestDelete = (backtest: BacktestListResponse, index: number) => {
    setBacktestToDelete(backtest);
    setDeleteModalOpen(true);
  };

  const handleDeleteConfirm = () => {
    if (backtestToDelete) {
      deleteMutation.mutate(backtestToDelete.id);
      setDeleteModalOpen(false);
      setBacktestToDelete(null);
    }
  };

  const handleDeleteCancel = () => {
    setDeleteModalOpen(false);
    setBacktestToDelete(null);
  };

  const handleTutorialClose = useCallback(() => {
    closeTutorial('backtest');
  }, [closeTutorial]);


  return (
    <div className={styles.backtest}>
      <div className={styles.container}>

        {/* 백테스트 히스토리 */}
        <BacktestHistoryWidget
          data={backtestData}
          onBacktestClick={handleBacktestClick}
          onBacktestDelete={handleBacktestDelete}
          onDirectCreation={handleDirectCreation}
          currentPage={currentPage}
          itemsPerPage={itemsPerPage}
          isLoading={isLoading}
          error={error}
        />

        {!isLoading && (
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        )}
      </div>

      {/* Tutorial Overlay */}
      <TutorialOverlay
        isOpen={tutorialStates.backtest}
        onClose={handleTutorialClose}
        steps={backtestTutorialSteps}
      />

      {/* Delete Confirmation Modal */}
      <DeleteConfirmModal
        isOpen={deleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        title={backtestToDelete?.testName || ''}
        message=""
        isLoading={deleteMutation.isPending}
      />
    </div>
  );
}