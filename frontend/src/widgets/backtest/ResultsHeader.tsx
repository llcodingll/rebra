import { useState } from 'react';
import { motion } from 'motion/react';
import { Calendar, TrendingUp, Edit2, Check, X } from 'lucide-react';
import styles from './ResultsHeader.module.css';

interface SummaryCardData {
  label: string;
  value: string;
  subtext?: string;
  highlight?: boolean;
}

interface ResultsHeaderProps {
  title?: string;
  period?: string;
  totalReturn?: string;
  summaryCards?: SummaryCardData[];
  onTitleChange?: (newTitle: string) => void;
}

export default function ResultsHeader({
  title = '월간 리밸런싱 전략',
  period = '2023년 01월 01일 ~ 2023년 12월 31일',
  totalReturn = '+35.2%',
  summaryCards = [
    { label: '초기 자본', value: '10,000,000원' },
    { label: '최종 평가액', value: '13,540,000원', highlight: true },
    { label: '전략 수익률', value: '+35.2%', subtext: '월간 리밸런싱', highlight: true },
    { label: '총 거래횟수', value: '36회' }
  ],
  onTitleChange
}: ResultsHeaderProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [editedTitle, setEditedTitle] = useState(title);

  const handleEdit = () => {
    const confirmEdit = window.confirm('제목을 수정하시겠습니까?');
    if (confirmEdit) {
      setIsEditing(true);
    }
  };

  const handleSave = () => {
    const confirmSave = window.confirm('변경사항을 저장하시겠습니까?');
    if (confirmSave && onTitleChange && editedTitle.trim()) {
      onTitleChange(editedTitle.trim());
      setIsEditing(false);
    } else if (confirmSave) {
      setIsEditing(false);
    }
  };

  const handleCancel = () => {
    setEditedTitle(title);
    setIsEditing(false);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSave();
    } else if (e.key === 'Escape') {
      handleCancel();
    }
  };
  return (
    <>
      <div className={styles.header}>
        <div className={styles.titleSection}>
          <div className={styles.titleContainer}>
            {isEditing ? (
              <div className={styles.titleEditWrapper}>
                <input
                  type="text"
                  value={editedTitle}
                  onChange={(e) => setEditedTitle(e.target.value)}
                  onKeyDown={handleKeyPress}
                  className={styles.titleInput}
                  autoFocus
                />
                <div className={styles.titleEditButtons}>
                  <button 
                    onClick={handleSave}
                    className={styles.saveButton}
                    title="저장"
                  >
                    <Check size={16} />
                  </button>
                  <button 
                    onClick={handleCancel}
                    className={styles.cancelButton}
                    title="취소"
                  >
                    <X size={16} />
                  </button>
                </div>
              </div>
            ) : (
              <div className={styles.titleWrapper}>
                <h1 className={styles.title}>{title}</h1>
                <button 
                  onClick={handleEdit}
                  className={styles.editButton}
                  title="제목 편집"
                >
                  <Edit2 size={20} />
                </button>
              </div>
            )}
          </div>
          <div className={styles.headerMeta}>
            <div className={styles.periodInfo}>
              <Calendar className={styles.periodIcon} />
              <span className={styles.period}>{period}</span>
            </div>
            <div className={styles.returnBadge}>
              <TrendingUp className={styles.returnIcon} />
              {totalReturn}
            </div>
          </div>
        </div>
      </div>

      {/* 상단 요약 카드들 */}
      <div className={styles.summaryCards}>
        {summaryCards.map((card, index) => (
          <motion.div
            key={index}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 * (index + 1) }}
            className={styles.summaryCard}
          >
            <div className={styles.summaryLabel}>{card.label}</div>
            <div className={`${styles.summaryValue} ${card.highlight ? styles.highlight : ''}`}>
              {card.value}
            </div>
            {card.subtext && (
              <div className={styles.summarySubtext}>{card.subtext}</div>
            )}
          </motion.div>
        ))}
      </div>
    </>
  );
}