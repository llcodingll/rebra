import { motion, AnimatePresence } from 'motion/react';
import { useState } from 'react';
import { X, ChevronLeft, ChevronRight } from 'lucide-react';
import styles from './TutorialOverlay.module.css';
import mascotImage from '../../assets/images/mascots/mascots1.png';

interface TutorialStep {
  id: string;
  title: string;
  content: string;
  targetSelector?: string;
  position?: 'top' | 'bottom' | 'left' | 'right' | 'center';
}

interface TutorialOverlayProps {
  isOpen: boolean;
  onClose: () => void;
  steps: TutorialStep[];
  currentStepIndex?: number;
}

export default function TutorialOverlay({
  isOpen,
  onClose,
  steps,
  currentStepIndex = 0
}: TutorialOverlayProps) {
  const [activeStepIndex, setActiveStepIndex] = useState(currentStepIndex);

  const currentStep = steps[activeStepIndex];

  const handleNext = () => {
    if (activeStepIndex < steps.length - 1) {
      setActiveStepIndex(activeStepIndex + 1);
    }
  };

  const handlePrevious = () => {
    if (activeStepIndex > 0) {
      setActiveStepIndex(activeStepIndex - 1);
    }
  };

  const handleFinish = () => {
    setActiveStepIndex(0);
    onClose();
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Dark overlay */}
          <motion.div
            className={styles.overlay}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.3 }}
            onClick={onClose}
          />

          {/* Tutorial content */}
          <motion.div
            className={styles.tutorialContainer}
            initial={{ opacity: 0, scale: 0.9, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.9, y: 20 }}
            transition={{ duration: 0.3, delay: 0.1 }}
          >
            {/* Close button */}
            <button className={styles.closeButton} onClick={onClose}>
              <X className={styles.closeIcon} />
            </button>

            {/* Mascot */}
            <div className={styles.mascotContainer}>
              <img
                src={mascotImage}
                alt="튜토리얼 마스코트"
                className={styles.mascot}
              />
            </div>

            {/* Speech bubble */}
            <div className={styles.speechBubble}>
              <div className={styles.speechBubbleArrow} />

              {/* Step indicator */}
              <div className={styles.stepIndicator}>
                {activeStepIndex + 1} / {steps.length}
              </div>

              {/* Content */}
              <div className={styles.content}>
                <h3 className={styles.title}>{currentStep?.title}</h3>
                <p className={styles.description}>{currentStep?.content}</p>
              </div>

              {/* Navigation */}
              <div className={styles.navigation}>
                <button
                  className={`${styles.navButton} ${styles.previousButton}`}
                  onClick={handlePrevious}
                  disabled={activeStepIndex === 0}
                >
                  <ChevronLeft className={styles.navIcon} />
                  이전
                </button>

                {activeStepIndex < steps.length - 1 ? (
                  <button
                    className={`${styles.navButton} ${styles.nextButton}`}
                    onClick={handleNext}
                  >
                    다음
                    <ChevronRight className={styles.navIcon} />
                  </button>
                ) : (
                  <button
                    className={`${styles.navButton} ${styles.finishButton}`}
                    onClick={handleFinish}
                  >
                    완료
                  </button>
                )}
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}