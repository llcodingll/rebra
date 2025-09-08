import kakaoIcon from 'figma:asset/b58690531dd5cbb1352f8e5c713b25639e76dae9.png';
import AuthLayout from '../widget/auth/AuthLayout';
import styles from './LoginPage.module.css';

interface LoginPageProps {
  onBack: () => void;
  onSignup: () => void;
  onLoginSuccess: () => void;
}

export default function LoginPage({ onBack, onSignup, onLoginSuccess }: LoginPageProps) {
  const handleKakaoLogin = () => {
    onLoginSuccess();
  };

  return (
    <AuthLayout
      onBack={onBack}
      title='간편 로그인'
      subtitle='카카오 계정으로 빠르게 로그인하세요'
      buttonText='카카오 계정으로 로그인'
      alternativeText='계정이 없으신가요?'
      alternativeButtonText='회원가입하기'
      onAlternativeClick={onSignup}
      onSubmit={handleKakaoLogin}
    >
      <div className={styles.kakaoButtonContainer}>
        <button className={styles.kakaoButton} onClick={handleKakaoLogin}>
          <img src={kakaoIcon} alt='카카오 아이콘' className={styles.kakaoIcon} />
          카카오 계정으로 로그인
        </button>
      </div>
    </AuthLayout>
  );
}
