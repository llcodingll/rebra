import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { X, Save, RotateCcw, TrendingUp, Settings } from 'lucide-react';
import styles from './StockSettingModal.module.css';
import { useConfirmModal } from '../../hooks/useModalState';
import ConfirmModal from '../../shared/ui/modal/ConfirmModal';

import type { Stock } from '../../entities/portfolio';

interface StockSettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  stocks: Stock[];
  onSaveSettings: (updatedStocks: Stock[]) => void;
  isSaving?: boolean;
}

interface StockSettings {
  code: string;
  weight: number | '';
  threshold: number | null;
}

export default function StockSettingsModal({
  isOpen,
  onClose,
  stocks,
  onSaveSettings,
  isSaving = false
}: StockSettingsModalProps) {
  const [stockSettings, setStockSettings] = useState<StockSettings[]>([]);
  const [hasChanges, setHasChanges] = useState(false);
  const { confirmState, showConfirm, hideConfirm } = useConfirmModal();

  // 초기 설정값 로드
  useEffect(() => {
    if (isOpen && stocks.length > 0) {
      const initialSettings = stocks
        .filter(stock => stock.type === 'registered')
        .map(stock => ({
          code: stock.code,
          weight: stock.targetWeight || 0, // 서버에서 받은 원본 가중치
          threshold: stock.thresholdPercentage
        }));
      setStockSettings(initialSettings);
      setHasChanges(false);
    }
  }, [isOpen, stocks]);

  // 목표비중 계산
  const calculateTargetWeights = (settings: StockSettings[]) => {
    const totalWeight = settings.reduce((sum, setting) => sum + (typeof setting.weight === 'number' ? setting.weight : 0), 0);

    if (totalWeight === 0) {
      return settings.map(setting => ({ ...setting, targetWeight: 0 }));
    }

    return settings.map(setting => ({
      ...setting,
      targetWeight: ((typeof setting.weight === 'number' ? setting.weight : 0) / totalWeight) * 100
    }));
  };

  // 설정값 변경 핸들러
  const handleSettingChange = (code: string, field: 'weight' | 'threshold', value: number | null | '') => {
    setStockSettings(prev => {
      const updated = prev.map(setting => {
        if (setting.code === code) {
          if (field === 'weight') {
            // 가중치는 빈 문자열 허용 (입력 중)
            return { ...setting, weight: value === '' ? '' : Math.max(0, value || 0) };
          } else {
            // 임계값은 null 허용 (사용자가 비우면 null)
            return { ...setting, threshold: value };
          }
        }
        return setting;
      });
      setHasChanges(true);
      return updated;
    });
  };

  // 가중치 포커스 아웃 핸들러 (빈 값을 0으로 변환)
  const handleWeightBlur = (code: string) => {
    setStockSettings(prev => {
      return prev.map(setting =>
        setting.code === code && setting.weight === ''
          ? { ...setting, weight: 0 }
          : setting
      );
    });
  };

  // 초기화
  const handleReset = () => {
    const initialSettings = stocks
      .filter(stock => stock.type === 'registered')
      .map(stock => ({
        code: stock.code,
        weight: stock.targetWeight || 0, // 서버에서 받은 원본 가중치
        threshold: stock.thresholdPercentage
      }));
    setStockSettings(initialSettings);
    setHasChanges(false);
  };

  // 저장 확인
  const handleSaveClick = () => {
    showConfirm({
      title: '설정 저장',
      message: '포트폴리오 설정을 저장하시겠습니까?',
      onConfirm: handleSave,
      type: 'default'
    });
  };

  // 유효성 검사
  const getValidationMessages = () => {
    const errors: Array<{ message: string; type: 'error' | 'warning' }> = [];

    // 임계값이 0~10% 범위인 주식 검사 (오류)
    const stocksWithLowThreshold = stockSettings.filter(setting =>
      setting.threshold !== null && setting.threshold >= 0 && setting.threshold <= 10
    );
    if (stocksWithLowThreshold.length > 0) {
      const stockNames = stocksWithLowThreshold.map(setting => {
        const stock = stocks.find(s => s.code === setting.code);
        return stock?.name || setting.code;
      }).join(', ');
      errors.push({
        message: `다음 주식의 임계값이 너무 낮습니다 (10% 초과 필수): ${stockNames}`,
        type: 'error'
      });
    }

    // 임계값이 10~20% 범위인 주식 검사 (경고)
    const stocksWithMediumThreshold = stockSettings.filter(setting =>
      setting.threshold !== null && setting.threshold > 10 && setting.threshold <= 20
    );
    if (stocksWithMediumThreshold.length > 0) {
      const stockNames = stocksWithMediumThreshold.map(setting => {
        const stock = stocks.find(s => s.code === setting.code);
        return stock?.name || setting.code;
      }).join(', ');
      errors.push({
        message: `다음 주식의 임계값은 20% 이상을 권장합니다: ${stockNames}`,
        type: 'warning'
      });
    }

    return errors;
  };

  // 저장
  const handleSave = () => {
    const validationMessages = getValidationMessages();
    const hasErrors = validationMessages.some(msg => msg.type === 'error');
    if (hasErrors) {
      return; // 오류가 있을 시 저장하지 않음
    }

    const updatedStocks = stocks.map(stock => {
      const setting = stockSettings.find(s => s.code === stock.code);
      if (setting && stock.type === 'registered') {
        return {
          ...stock,
          targetWeight: typeof setting.weight === 'number' ? setting.weight : 0, // 사용자가 입력한 가중치를 전송
          thresholdPercentage: setting.threshold
        };
      }
      return stock;
    });

    onSaveSettings(updatedStocks);
    setHasChanges(false);
  };

  // 취소
  const handleCancel = () => {
    onClose();
    setHasChanges(false);
  };

  const registeredStocks = stocks.filter(stock => stock.type === 'registered');
  const settingsWithTargets = calculateTargetWeights(stockSettings);
  const totalWeight = stockSettings.reduce((sum, setting) => sum + setting.weight, 0);
  const validationMessages = getValidationMessages();
  const hasErrors = validationMessages.some(msg => msg.type === 'error');

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className={styles.overlay}
          onClick={handleCancel}
        >
          {/* 모달 */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            transition={{ type: "spring", duration: 0.5 }}
            className={styles.modal}
            onClick={(e) => e.stopPropagation()}
          >
            {/* 헤더 */}
            <div className={styles.header}>
              <div className={styles.headerTitle}>
                <Settings className={styles.headerIcon} />
                <div>
                  <h2 className={styles.title}>주식 포트폴리오 설정</h2>
                  <p className={styles.subtitle}>
                    가중치와 임계값을 조정하여 포트폴리오를 최적화하세요
                  </p>
                </div>
              </div>
              <button
                className={styles.closeButton}
                onClick={handleCancel}
              >
                <X size={16} />
              </button>
            </div>


            {/* 유효성 검사 메시지 */}
            {validationMessages.length > 0 && (
              <div className={styles.validationMessages}>
                {validationMessages.map((msg, index) => (
                  <div key={index} className={`${styles.message} ${styles[msg.type]}`}>
                    <span className={styles.messageIcon}>
                      {msg.type === 'error' ? '⚠️' : '💡'}
                    </span>
                    {msg.message}
                  </div>
                ))}
              </div>
            )}

            {/* 주식 설정 리스트 */}
            <div className={styles.content}>
              <div className={styles.stockList}>
                {registeredStocks.map((stock, index) => {
                  const setting = stockSettings.find(s => s.code === stock.code);
                  const settingWithTarget = settingsWithTargets.find(s => s.code === stock.code);
                  
                  if (!setting) return null;

                  const isPositive = stock.profitLossRate >= 0;

                  return (
                    <div key={stock.code} className={styles.stockItem}>
                      {/* 주식 정보 */}
                      <div className={styles.stockInfo}>
                        <div className={styles.stockHeader}>
                          <div className={styles.stockName}>
                            <h3>{stock.name}</h3>
                            <span className={styles.stockCode}>{stock.code}</span>
                          </div>
                          <div className={styles.stockReturn}>
                            <TrendingUp 
                              className={`${styles.trendIcon} ${isPositive ? styles.positive : styles.negative}`} 
                            />
                            <span className={`${styles.returnText} ${isPositive ? styles.positive : styles.negative}`}>
                              {stock.profitLossRate >= 0 ? '+' : ''}{stock.profitLossRate.toFixed(1)}%
                            </span>
                          </div>
                        </div>
                        
                        <div className={styles.stockDetails}>
                          <span>현재가: {stock.currentPrice.toLocaleString()}원</span>
                          <span>보유량: {stock.quantity}주</span>
                          <span>평가액: {stock.totalValue.toLocaleString()}원</span>
                        </div>
                      </div>

                      {/* 설정 입력 */}
                      <div className={styles.settingsGrid}>
                        <div className={styles.inputGroup}>
                          <label className={styles.inputLabel}>
                            가중치
                            <span className={styles.inputHint}>포트폴리오 내 비중</span>
                          </label>
                          <div className={styles.inputWrapper}>
                            <input
                              type="number"
                              value={setting.weight}
                              onChange={(e) => handleSettingChange(stock.code, 'weight', e.target.value === '' ? '' : parseFloat(e.target.value) || 0)}
                              onBlur={() => handleWeightBlur(stock.code)}
                              className={styles.numberInput}
                              min="0"
                              max="100"
                              step="0.1"
                            />
                            <span className={styles.inputSuffix}>%</span>
                          </div>
                        </div>

                        <div className={styles.inputGroup}>
                          <label className={styles.inputLabel}>
                            임계값
                            <span className={styles.inputHint}>리밸런싱 기준</span>
                          </label>
                          <div className={styles.inputWrapper}>
                            <input
                              type="number"
                              value={setting.threshold ?? ''}
                              placeholder="임계값 설정 필요"
                              onChange={(e) => handleSettingChange(stock.code, 'threshold', e.target.value ? parseFloat(e.target.value) : null)}
                              className={styles.numberInput}
                              min="0"
                              max="50"
                              step="0.1"
                            />
                            <span className={styles.inputSuffix}>%</span>
                          </div>
                        </div>

                        <div className={styles.inputGroup}>
                          <label className={styles.inputLabel}>
                            목표비중
                            <span className={styles.inputHint}>계산된 비중</span>
                          </label>
                          <div className={styles.targetWeight}>
                            {settingWithTarget ? `${settingWithTarget.targetWeight.toFixed(1)}%` : '0.0%'}
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* 액션 버튼들 */}
            <div className={styles.actions}>
              <div className={styles.actionGroup}>
                <button
                  onClick={handleReset}
                  className={styles.resetButton}
                >
                  <RotateCcw size={16} />
                  초기화
                </button>
              </div>

              <div className={styles.actionGroup}>
                <button
                  onClick={handleCancel}
                  className={styles.cancelButton}
                >
                  취소
                </button>
                <button
                  onClick={handleSaveClick}
                  disabled={!hasChanges || isSaving || hasErrors}
                  className={styles.saveButton}
                >
                  <Save size={16} />
                  {isSaving ? '저장 중...' : '설정 저장'}
                </button>
              </div>
            </div>
          </motion.div>
        </motion.div>
      )}
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
    </AnimatePresence>
  );
}