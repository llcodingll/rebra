import BaseModal from '../../shared/ui/modal/BaseModal';
import { CheckCircle } from 'lucide-react';
import styles from './ApiKeyGuideModal.module.css';

interface ApiKeyGuideModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function ApiKeyGuideModal({
  isOpen,
  onClose
}: ApiKeyGuideModalProps) {
  const guideSteps = [
    {
      number: 1,
      title: "한국투자증권 계좌 개설",
      description: "한국투자증권에서 계좌를 개설합니다.",
      detail: "실계좌 또는 모의계좌를 선택하여 개설할 수 있습니다."
    },
    {
      number: 2,
      title: "KIS Developers 접속",
      description: "한국투자증권 KIS Developers 사이트에 접속합니다.",
      detail: "우측 상단의 'API Key' 버튼을 클릭합니다."
    },
    {
      number: 3,
      title: "휴대폰 인증 및 신청",
      description: "휴대폰 인증을 완료합니다.",
      detail: "인증 후 '신청 현황'에서 '추가 신청하기'를 클릭합니다."
    },
    {
      number: 4,
      title: "API Key 발급 완료",
      description: "신청 후 APP Key와 APP Secret를 확인합니다.",
      detail: "발급받은 키를 복사하여 계좌 등록 시 입력하면 완료됩니다!"
    }
  ];

  return (
    <BaseModal
      isOpen={isOpen}
      onClose={onClose}
      title="API 키 등록 방법"
      size="large"
    >
      <div className={styles.content}>
        <div className={styles.introduction}>
          <p className={styles.introText}>
            한국투자증권 API를 사용하여 안전하게 계좌 정보를 연동하는 방법을 안내드립니다.
          </p>
        </div>

        <div className={styles.stepsContainer}>
          {guideSteps.map((step, index) => (
            <div key={index} className={styles.stepItem}>
              <div className={styles.stepHeader}>
                <div className={styles.stepNumber}>
                  {step.number}
                </div>
                <div className={styles.stepContent}>
                  <h3 className={styles.stepTitle}>{step.title}</h3>
                  <p className={styles.stepDescription}>{step.description}</p>
                  <p className={styles.stepDetail}>{step.detail}</p>
                </div>
              </div>
              {index < guideSteps.length - 1 && (
                <div className={styles.stepConnector}></div>
              )}
            </div>
          ))}
        </div>

        <div className={styles.finalNote}>
          <div className={styles.noteIcon}>
            <CheckCircle className={styles.checkIcon} />
          </div>
          <div className={styles.noteContent}>
            <h4>완료!</h4>
            <p>API 키 발급이 완료되면 계좌 등록 폼에 입력하여 연동을 완료할 수 있습니다.</p>
          </div>
        </div>

        <div className={styles.buttonGroup}>
          <button className={styles.confirmButton} onClick={onClose}>
            확인
          </button>
        </div>
      </div>
    </BaseModal>
  );
}