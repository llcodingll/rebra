import { useNavigate } from 'react-router-dom';
import kakaoIcon from 'figma:asset/b58690531dd5cbb1352f8e5c713b25639e76dae9.png';
import AuthLayout from '../widget/auth/AuthLayout';
import styles from './LoginPage.module.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const handleLoginSuccess = () => navigate('/dashboard');

  const handleKakaoLogin = () => {
    handleLoginSuccess();
  };

  return (
    <AuthLayout title='간편 로그인' subtitle='카카오 계정으로 빠르게 로그인하세요'>
      <div className={styles.kakaoButtonContainer}>
        <button className={styles.kakaoButton} onClick={handleKakaoLogin}>
          {/* <img src={kakaoIcon} alt='카카오 아이콘' className={styles.kakaoIcon} /> */}
          카카오 계정으로 로그인
        </button>
      </div>
    </AuthLayout>
  );
}
