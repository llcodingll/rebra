import { useState } from 'react';
import styles from './AssetTable.module.css';
import { useModalState, useConfirmModal } from '../../hooks/useModalState';
import StockSettingModal from '../../features/modal/StockSettingModal';
import ConfirmModal from '../../shared/ui/modal/ConfirmModal';
import { useApiMutation } from '../../shared/hook/useApi';
import { portfolioApi } from '../../features/portfolio/api/portfolioApi';
import type { Stock } from '../../entities/portfolio';

interface AssetTableProps {
  title: string;
  type: 'registered' | 'unregistered';
  data: Stock[];
  portfolioId?: number;
  onStockRegistered?: () => void; // 주식 등록 성공 시 콜백
  onStockRemoved?: () => void; // 주식 삭제 성공 시 콜백
  onStockSettingsUpdated?: () => void; // 주식 설정 업데이트 성공 시 콜백
}

export default function AssetTable({ title, type, data, portfolioId, onStockRegistered, onStockRemoved, onStockSettingsUpdated }: AssetTableProps) {
  const { isOpen: isStockSettingModalOpen, open: openStockSettingModal, close: closeStockSettingModal } = useModalState();

  // 숫자 포맷팅 함수들
  const formatNumber = (num: number): string => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const formatPrice = (price: number): string => {
    return `${formatNumber(price)}`;
  };

  const formatQuantity = (quantity: number): string => {
    return `${quantity}주`;
  };

  const formatValue = (value: number): string => {
    return `${formatNumber(value)}원`;
  };

  const formatReturn = (rate: number): string => {
    return `${rate >= 0 ? '+' : ''}${rate.toFixed(1)}%`;
  };

  const formatReturnAmount = (amount: number): string => {
    return `(${amount >= 0 ? '+' : ''}${formatNumber(amount)}원)`;
  };

  const formatWeight = (weight: number | null): string => {
    if (weight === null || weight === undefined) {
      return '-';
    }
    return `${weight.toFixed(1)}%`;
  };

  const { confirmState, showConfirm, hideConfirm } = useConfirmModal();

  // 주식 등록 mutation
  const { mutate: registerStock, isPending: isRegistering } = useApiMutation({
    apiFunction: (stockCode: string) => {
      if (!portfolioId) {
        return Promise.reject('포트폴리오 ID가 없습니다.');
      }
      return portfolioApi.registerStock(portfolioId, { stockCode });
    },
    onSuccess: (data) => {
      console.log('=== 주식 등록 성공 ===');
      console.log('등록된 주식:', data);
      alert(`${data.stockName} 주식이 포트폴리오에 등록되었습니다.`);
      onStockRegistered?.(); // 성공 시 콜백 호출
    },
    onError: (error) => {
      console.error('=== 주식 등록 실패 ===');
      console.error('에러:', error);
      alert('주식 등록에 실패했습니다. 다시 시도해주세요.');
    }
  });

  // 주식 삭제 mutation
  const { mutate: deleteStock, isPending: isDeleting } = useApiMutation({
    apiFunction: (stockCode: string) => {
      if (!portfolioId) {
        return Promise.reject('포트폴리오 ID가 없습니다.');
      }
      return portfolioApi.deleteStock(portfolioId, { stockCode });
    },
    onSuccess: (response, stockCode) => {
      console.log('=== 주식 삭제 성공 ===');
      console.log('삭제 응답:', response);
      const stockName = data.find(stock => stock.code === stockCode)?.name || '해당 주식';
      alert(`${stockName}이 포트폴리오에서 제거되었습니다.`);
      onStockRemoved?.(); // 성공 시 콜백 호출
    },
    onError: (error) => {
      console.error('=== 주식 삭제 실패 ===');
      console.error('에러:', error);
      alert('주식 삭제에 실패했습니다. 다시 시도해주세요.');
    }
  });

  // 주식 설정 업데이트 mutation
  const { mutate: updateStocks, isPending: isUpdating } = useApiMutation({
    apiFunction: (stocks: Stock[]) => {
      if (!portfolioId) {
        return Promise.reject('포트폴리오 ID가 없습니다.');
      }

      const requestData = {
        stocks: stocks.map(stock => ({
          stockCode: stock.code,
          targetWeight: stock.targetWeight || 0,
          thresholdPercentage: stock.thresholdPercentage || 0
        }))
      };

      return portfolioApi.updateStocks(portfolioId, requestData);
    },
    onSuccess: (data) => {
      console.log('=== 주식 설정 업데이트 성공 ===');
      console.log('업데이트 결과:', data);
      alert('주식 설정이 성공적으로 업데이트되었습니다.');
      closeStockSettingModal();
      onStockSettingsUpdated?.(); // 주식 설정 업데이트 전용 콜백 호출
    },
    onError: (error) => {
      console.error('=== 주식 설정 업데이트 실패 ===');
      console.error('에러:', error);
      alert('주식 설정 업데이트에 실패했습니다. 다시 시도해주세요.');
    }
  });

  const handleSaveSettings = (updatedStocks: Stock[]) => {
    console.log('주식 설정 저장:', updatedStocks);
    updateStocks(updatedStocks);
  };

  const handleAddStock = (stock: Stock) => {
    if (!portfolioId) {
      alert('포트폴리오 정보가 없습니다.');
      return;
    }

    showConfirm({
      title: '주식 추가',
      message: `${stock.name}(${stock.code})을 포트폴리오에 추가하시겠습니까?`,
      onConfirm: () => {
        console.log('주식 추가 확인:', stock);
        registerStock(stock.code);
      },
      type: 'default'
    });
  };

  const handleRemoveStock = (stock: Stock) => {
    console.log('=== handleRemoveStock 호출됨 ===');
    console.log('stock:', stock);
    console.log('portfolioId:', portfolioId);

    if (!portfolioId) {
      alert('포트폴리오 정보가 없습니다.');
      return;
    }

    console.log('확인 모달 표시');
    showConfirm({
      title: '주식 삭제',
      message: `${stock.name}(${stock.code})을 포트폴리오에서 제거하시겠습니까?`,
      onConfirm: () => {
        console.log('주식 삭제 확인:', stock);
        deleteStock(stock.code);
      },
      type: 'danger'
    });
  };

  const registeredColumns = [
    '종목명', '매수가/현재가', '수량/평가금액', '수익률', '현재 비중(%)', '가중치', '목표 비중(%)', '임계값 비중(%)', '제외'
  ];

  const unregisteredColumns = [
    '종목명', '매수가/현재가', '수량/평가금액', '수익률'
  ];

  const columns = type === 'registered' ? registeredColumns : unregisteredColumns;

  return (
    <div className={styles.tableContainer}>
      <div className={styles.tableCard}>
        <div className={styles.tableHeader}>
          <h3>{title}</h3>
          {type === 'registered' && (
            <button className={styles.settingsButton} onClick={openStockSettingModal}>
              비중 설정
            </button>
          )}
        </div>
        
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr className={styles.headerRow}>
                {columns.map((column, index) => (
                  <th key={index} className={styles.headerCell}>
                    {column}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data.map((stock, index) => (
                <tr key={index} className={styles.dataRow}>
                  <td className={styles.dataCell}>
                    <div className={styles.stockName}>
                      <span className={styles.name}>{stock.name}</span>
                      <div className={styles.codeTag}>{stock.code}</div>
                    </div>
                  </td>
                  <td className={styles.dataCell}>
                    <div className={styles.priceInfo}>
                      <span className={styles.buyPrice}>{formatPrice(stock.averagePrice)}</span>
                      <span className={styles.currentPrice}>{formatPrice(stock.currentPrice)}</span>
                    </div>
                  </td>
                  <td className={styles.dataCell}>
                    <div className={styles.quantityInfo}>
                      <span className={styles.quantity}>{formatQuantity(stock.quantity)}</span>
                      <span className={styles.value}>{formatValue(stock.totalValue)}</span>
                    </div>
                  </td>
                  <td className={styles.dataCell}>
                    <div className={styles.returnInfo}>
                      <div className={styles.returnWithIcon}>
                        <svg width="12.591" height="12.591" viewBox="0 0 13 7" fill="none">
                          <path d="M11.4922 1L7.03302 5.45919L4.40997 2.83614L1 6.24611" stroke="#155DFC" strokeWidth="1.04922" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        <span className={styles.returnPercent}>{formatReturn(stock.profitLossRate)}</span>
                      </div>
                      <span className={styles.returnAmount}>{formatReturnAmount(stock.profitLoss)}</span>
                    </div>
                  </td>
                  {type === 'registered' && (
                    <>
                      <td className={styles.dataCell}>
                        <span>{formatWeight(stock.currentPercentage)}</span>
                      </td>
                      <td className={styles.dataCell}>
                        <span>{stock.targetWeight === null ? '-' : stock.targetWeight}</span>
                      </td>
                      <td className={styles.dataCell}>
                        <span>{stock.targetWeight === null ? '-' : formatWeight(stock.targetPercentage)}</span>
                      </td>
                      <td className={styles.dataCell}>
                        <span>{formatWeight(stock.thresholdPercentage)}</span>
                      </td>
                      <td className={styles.dataCell}>
                        <button
                          className={styles.removeButton}
                          onClick={() => handleRemoveStock(stock)}
                        >
                          <svg width="20.385" height="20.385" viewBox="0 0 17 14" fill="none">
                            <path d="M2.05051 3.5H14.3535" stroke="#EF4444" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M6.15152 3.5V2.33333C6.15152 2.08696 6.26625 1.85054 6.47279 1.67426C6.67933 1.49799 6.95634 1.40007 7.24502 1.40007H9.15883C9.44751 1.40007 9.72453 1.49799 9.93107 1.67426C10.1376 1.85054 10.2523 2.08696 10.2523 2.33333V3.5M12.9864 3.5V11.6667C12.9864 11.913 12.8717 12.1494 12.6651 12.3257C12.4585 12.502 12.1815 12.5999 11.8928 12.5999H4.51102C4.22234 12.5999 3.94532 12.502 3.73879 12.3257C3.53225 12.1494 3.41751 11.913 3.41751 11.6667V3.5" stroke="#EF4444" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                        </button>
                      </td>
                    </>
                  )}
                  {type === 'unregistered' && (
                    <td className={styles.dataCell}>
                      <button
                        className={styles.addButton}
                        onClick={() => handleAddStock(stock)}
                      >
                        <svg width="29" height="29" viewBox="0 0 20 20" fill="none">
                          <path d="M10 6v8M6 10h8" stroke="#155DFC" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <StockSettingModal
        isOpen={isStockSettingModalOpen}
        onClose={closeStockSettingModal}
        stocks={data}
        onSaveSettings={handleSaveSettings}
        isSaving={isUpdating}
      />

      <ConfirmModal
        isOpen={confirmState.isOpen}
        title={confirmState.title}
        message={confirmState.message}
        type={confirmState.type}
        confirmText={confirmState.confirmText}
        cancelText={confirmState.cancelText}
        onConfirm={() => {
          confirmState.onConfirm();
          hideConfirm();
        }}
        onCancel={hideConfirm}
      />
    </div>
  );
}