import { motion } from 'motion/react';
import { Plus, TrendingUp, PieChart } from 'lucide-react';
import styles from './NoPortfolioState.module.css';

interface NoPortfolioStateProps {
  onCreatePortfolio: () => void;
}

export default function NoPortfolioState({ onCreatePortfolio }: NoPortfolioStateProps) {
  return (
    <div className={styles.container}>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className={styles.content}
      >
        {/* 아이콘 */}
        <motion.div
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className={styles.iconContainer}
        >
          <PieChart className={styles.mainIcon} />
        </motion.div>

        {/* 메인 메시지 */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className={styles.messageContainer}
        >
          <h2 className={styles.title}>포트폴리오가 없습니다</h2>
          <p className={styles.description}>
            포트폴리오를 등록하여 자산 현황과 수익률을 확인해보세요
          </p>
        </motion.div>

        {/* 기능 설명 */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.4 }}
          className={styles.featureList}
        >
          <div className={styles.feature}>
            <TrendingUp className={styles.featureIcon} />
            <span>실시간 수익률 추적</span>
          </div>
          <div className={styles.feature}>
            <PieChart className={styles.featureIcon} />
            <span>자산 배분 분석</span>
          </div>
          <div className={styles.feature}>
            <Plus className={styles.featureIcon} />
            <span>자동 리밸런싱</span>
          </div>
        </motion.div>

        {/* 등록 버튼 */}
        <motion.button
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5, delay: 0.5 }}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          className={styles.createButton}
          onClick={onCreatePortfolio}
        >
          <Plus className={styles.buttonIcon} />
          포트폴리오 등록하기
        </motion.button>
      </motion.div>
    </div>
  );
}