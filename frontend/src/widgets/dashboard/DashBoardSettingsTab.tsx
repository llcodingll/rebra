import { useState, useEffect } from 'react';
import styles from './DashBoardSettingsTab.module.css';
import { useConfirmModal, useModalState } from '../../hooks/useModalState';
import ConfirmModal from '../../shared/ui/modal/ConfirmModal';
import RebalancingPeriodModal from './RebalancingPeriodModal';
import { portfolioApi } from '../../features/portfolio/api/portfolioApi';
import { useApiMutation } from '../../shared/hook/useApi';

interface DashBoardSettingsTabProps {
  portfolioId?: number;
  initialAutoRebalancing?: boolean;
  isLoadingSettings?: boolean;
  onAutoRebalancingChanged?: () => void;
}

export default function DashBoardSettingsTab({ portfolioId, initialAutoRebalancing = false, isLoadingSettings = false, onAutoRebalancingChanged }: DashBoardSettingsTabProps) {
    const { confirmState, showConfirm, hideConfirm } = useConfirmModal();
    const { isOpen: isPeriodModalOpen, open: openPeriodModal, close: closePeriodModal } = useModalState();
    const [isAutoRebalancingEnabled, setIsAutoRebalancingEnabled] = useState(initialAutoRebalancing);

    // API 응답의 autoRebalance 값이 변경될 때 상태 동기화
    useEffect(() => {
        setIsAutoRebalancingEnabled(initialAutoRebalancing);
    }, [initialAutoRebalancing]);

    // 자동 리밸런싱 설정 API 뮤테이션
    const { mutate: setAutoRebalancing, isPending: isSettingAutoRebalancing } = useApiMutation({
        apiFunction: (enabled: boolean) => {
            if (!portfolioId) {
                return Promise.reject('Portfolio ID가 없습니다.');
            }
            return portfolioApi.setAutoRebalancing(portfolioId, { autoRebalancing: enabled });
        },
        onSuccess: (data) => {
            console.log('자동 리밸런싱 설정 성공:', data);
            // 포트폴리오 상세 정보 새로고침으로 실제 서버 상태 동기화
            if (onAutoRebalancingChanged) {
                onAutoRebalancingChanged();
            }
        },
        onError: (error) => {
            console.error('자동 리밸런싱 설정 실패:', error);
            // 에러 시에도 서버 상태와 동기화
            if (onAutoRebalancingChanged) {
                onAutoRebalancingChanged();
            }
        }
    });

    const handleExecuteRebalancing = () => {
        showConfirm({
            title: '리밸런싱 실행',
            message: '지금 리밸런싱을 실행하시겠습니까?\n현재 포트폴리오가 목표 비중에 맞게 조정됩니다.',
            type: 'default',
            confirmText: '실행',
            cancelText: '취소',
            onConfirm: () => {
                console.log('리밸런싱 실행 API 호출');
                // TODO: 리밸런싱 실행 API 호출
            }
        });
    };

    const handleToggleAutoRebalancing = () => {
        const newState = !isAutoRebalancingEnabled;
        const action = newState ? '활성화' : '비활성화';
        const message = newState
            ? '자동 리밸런싱을 활성화하시겠습니까?\n설정된 주기마다 자동으로 포트폴리오가 리밸런싱됩니다.'
            : '자동 리밸런싱을 비활성화하시겠습니까?\n자동 리밸런싱이 중단됩니다.';

        showConfirm({
            title: `자동 리밸런싱 ${action}`,
            message: message,
            type: newState ? 'warning' : 'default',
            confirmText: action,
            cancelText: '취소',
            onConfirm: () => {
                setAutoRebalancing(newState);
            }
        });
    };

    return (
        <>
        {/* 리밸런싱 실행 설정*/}
                    <div className={styles.rebalancingControls}>
                        <div className={styles.controlGroup}>
                            <h3>즉시 실행</h3>
                            <button className={styles.executeButton} onClick={handleExecuteRebalancing}>
                            <svg width="19" height="19" viewBox="0 0 19 19" fill="none">
                                <path d="M4.75589 2.38672L15.7495 9.33007L4.75589 16.2734V2.38672Z" fill="white" stroke="white" strokeWidth="1.38867" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                            지금 리밸런싱 실행
                            </button>
                        </div>

                        <div className={styles.controlGroup}>
                            <h3>자동 리밸런싱</h3>
                            {isLoadingSettings ? (
                                <div className={styles.toggleContainer}>
                                    <div className={`${styles.toggle} ${styles.loading}`}>
                                        <div className={styles.toggleTrack}></div>
                                        <div className={styles.toggleThumb}></div>
                                    </div>
                                    <span className={styles.toggleLabel}>
                                        설정 불러오는 중...
                                    </span>
                                </div>
                            ) : (
                                <div className={styles.toggleContainer}>
                                    <div className={`${styles.toggle} ${isAutoRebalancingEnabled ? styles.active : ''}`} onClick={handleToggleAutoRebalancing}>
                                        <div className={styles.toggleTrack}></div>
                                        <div className={styles.toggleThumb}></div>
                                    </div>
                                    <span className={styles.toggleLabel}>
                                        {isAutoRebalancingEnabled ? '활성화' : '비활성화'}
                                    </span>
                                </div>
                            )}
                        </div>

                        <div className={styles.controlGroup}>
                            <h3>리밸런싱 주기</h3>
                            <button className={styles.executeButton} onClick={openPeriodModal}>
                                리밸런싱 주기 설정
                            </button>
                        </div>
                    </div>

            <ConfirmModal
                isOpen={confirmState.isOpen}
                title={confirmState.title}
                message={confirmState.message}
                type={confirmState.type}
                confirmText={confirmState.confirmText}
                cancelText={confirmState.cancelText}
                onConfirm={confirmState.onConfirm}
                onCancel={hideConfirm}
            />

            <RebalancingPeriodModal
                isOpen={isPeriodModalOpen}
                onClose={closePeriodModal}
            />
                    </>
    )
}