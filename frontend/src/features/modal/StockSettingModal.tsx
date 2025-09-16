import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { X, Save, RotateCcw, TrendingUp, Settings } from 'lucide-react';
import styles from './StockSettingModal.module.css';
import { useConfirmModal } from '../../hooks/useModalState';
import ConfirmModal from '../../shared/ui/modal/ConfirmModal';

interface Stock {
  name: string;
  code: string;
  buyPrice: string;
  currentPrice: string;
  quantity: string;
  value: string;
  return: string;
  returnAmount: string;
  currentWeight: string;
  targetWeight: string;
  weight: string;
  threshold: string;
  type: 'registered' | 'unregistered';
}

interface StockSettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  stocks: Stock[];
  onSaveSettings: (updatedStocks: Stock[]) => void;
}

interface StockSettings {
  code: string;
  weight: number;
  threshold: number;
}

export default function StockSettingsModal({
  isOpen,
  onClose,
  stocks,
  onSaveSettings
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
          weight: parseFloat(stock.weight) || 0,
          threshold: parseFloat(stock.threshold) || 0
        }));
      setStockSettings(initialSettings);
      setHasChanges(false);
    }
  }, [isOpen, stocks]);

  // 목표비중 계산
  const calculateTargetWeights = (settings: StockSettings[]) => {
    const totalWeight = settings.reduce((sum, setting) => sum + setting.weight, 0);
    
    if (totalWeight === 0) {
      return settings.map(setting => ({ ...setting, targetWeight: 0 }));
    }
    
    return settings.map(setting => ({
      ...setting,
      targetWeight: (setting.weight / totalWeight) * 100
    }));
  };

  // 설정값 변경 핸들러
  const handleSettingChange = (code: string, field: 'weight' | 'threshold', value: number) => {
    setStockSettings(prev => {
      const updated = prev.map(setting => 
        setting.code === code 
          ? { ...setting, [field]: Math.max(0, value) }
          : setting
      );
      setHasChanges(true);
      return updated;
    });
  };

  // 초기화
  const handleReset = () => {
    const initialSettings = stocks
      .filter(stock => stock.type === 'registered')
      .map(stock => ({
        code: stock.code,
        weight: parseFloat(stock.weight) || 0,
        threshold: parseFloat(stock.threshold) || 0
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

  // 저장
  const handleSave = () => {
    const settingsWithTargets = calculateTargetWeights(stockSettings);
    const updatedStocks = stocks.map(stock => {
      const setting = settingsWithTargets.find(s => s.code === stock.code);
      if (setting && stock.type === 'registered') {
        return {
          ...stock,
          weight: setting.weight.toString(),
          threshold: setting.threshold.toString(),
          targetWeight: setting.targetWeight.toFixed(1)
        };
      }
      return stock;
    });

    onSaveSettings(updatedStocks);
    onClose();
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

            {/* 전체 통계 */}
            <div className={styles.summary}>

              <div className={styles.summaryItem}>
                <span className={styles.summaryLabel}>등록 주식 수</span>
                <span className={styles.summaryValue}>{registeredStocks.length}개</span>
              </div>
            </div>

            {/* 주식 설정 리스트 */}
            <div className={styles.content}>
              <div className={styles.stockList}>
                {registeredStocks.map((stock, index) => {
                  const setting = stockSettings.find(s => s.code === stock.code);
                  const settingWithTarget = settingsWithTargets.find(s => s.code === stock.code);
                  
                  if (!setting) return null;

                  const returnValue = parseFloat(stock.return.replace('%', ''));
                  const isPositive = returnValue >= 0;

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
                              {stock.return}
                            </span>
                          </div>
                        </div>
                        
                        <div className={styles.stockDetails}>
                          <span>현재가: {stock.currentPrice}원</span>
                          <span>보유량: {stock.quantity}</span>
                          <span>평가액: {stock.value}</span>
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
                              onChange={(e) => handleSettingChange(stock.code, 'weight', parseFloat(e.target.value) || 0)}
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
                              value={setting.threshold}
                              onChange={(e) => handleSettingChange(stock.code, 'threshold', parseFloat(e.target.value) || 0)}
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
                  disabled={!hasChanges}
                  className={styles.saveButton}
                >
                  <Save size={16} />
                  설정 저장
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