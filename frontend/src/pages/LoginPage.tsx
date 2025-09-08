import { useNavigate } from 'react-router-dom';
import kakaoIcon from 'figma:asset/b58690531dd5cbb1352f8e5c713b25639e76dae9.png';
import AuthLayout from '../widget/auth/AuthLayout';
import styles from './LoginPage.module.css';

export default function LoginPage() {
  const navigate = useNavigate();

  const handleBack = () => navigate('/landing');
  const handleSignup = () => navigate('/signup');
  const handleLoginSuccess = () => navigate('/dashboard');

  const handleKakaoLogin = () => {
    handleLoginSuccess();
  };

  return (
    <AuthLayout
      onBack={handleBack}
      title="간편 로그인"
      subtitle="카카오 계정으로 빠르게 로그인하세요"
      buttonText="카카오 계정으로 로그인"
      alternativeText="계정이 없으신가요?"
      alternativeButtonText="회원가입하기"
      onAlternativeClick={handleSignup}
      onSubmit={handleKakaoLogin}
    >
      <div className={styles.kakaoButtonContainer}>
        <button
          className={styles.kakaoButton}
          onClick={handleKakaoLogin}
        >
          <img src={kakaoIcon} alt="카카오 아이콘" className={styles.kakaoIcon} />
          카카오 계정으로 로그인
        </button>
      </div>
    </AuthLayout>
  );
}