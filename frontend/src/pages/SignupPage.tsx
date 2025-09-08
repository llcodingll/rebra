import { useNavigate } from 'react-router-dom';
import kakaoIcon from 'figma:asset/b58690531dd5cbb1352f8e5c713b25639e76dae9.png';
import AuthLayout from '../widget/auth/AuthLayout';
import styles from './SignupPage.module.css';

export default function SignupPage() {
  const navigate = useNavigate();

  const handleBack = () => navigate('/landing');
  const handleLogin = () => navigate('/login');
  const handleSignupComplete = () => navigate('/dashboard');

  const handleKakaoLogin = () => {
    handleSignupComplete();
  };

  return (
    <AuthLayout
      onBack={handleBack}
      title="간편 가입하기"
      subtitle="카카오 계정으로 빠르고 안전하게 시작하세요"
      buttonText="카카오 계정으로 가입"
      alternativeText="이미 계정이 있으신가요?"
      alternativeButtonText="로그인하기"
      onAlternativeClick={handleLogin}
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