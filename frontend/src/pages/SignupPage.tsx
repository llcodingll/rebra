import kakaoIcon from 'figma:asset/b58690531dd5cbb1352f8e5c713b25639e76dae9.png';
import AuthLayout from '../components/layout/AuthLayout';
import styles from './SignupPage.module.css';

interface SignupPageProps {
  onBack: () => void;
  onLogin: () => void;
  onSignupComplete: () => void;
}

export default function SignupPage({ onBack, onLogin, onSignupComplete }: SignupPageProps) {
  const handleKakaoLogin = () => {
    onSignupComplete();
  };

  return (
    <AuthLayout
      onBack={onBack}
      title="간편 가입하기"
      subtitle="카카오 계정으로 빠르고 안전하게 시작하세요"
      buttonText="카카오 계정으로 가입"
      alternativeText="이미 계정이 있으신가요?"
      alternativeButtonText="로그인하기"
      onAlternativeClick={onLogin}
      onSubmit={handleKakaoLogin}
    >
      <div className={styles.kakaoButtonContainer}>
        <button
          className={styles.kakaoButton}
          onClick={handleKakaoLogin}
        >
          <img src={kakaoIcon} alt="카카오 아이콘" className={styles.kakaoIcon} />
          카카오 계정으로 가입
        </button>
      </div>
    </AuthLayout>
  );
}