import { getApiConfig } from '../../shared/config/apiConfig';
import styles from './KakaoLoginWidget.module.css';

interface KakaoLoginWidgetProps {
  onLoginSuccess: () => void;
}

export default function KakaoLoginWidget({ onLoginSuccess }: KakaoLoginWidgetProps) {
  const handleKakaoLogin = () => {
    const apiConfig = getApiConfig();
    const kakaoOAuthUrl = `${apiConfig.baseURL}/oauth2/authorization/kakao`;

    // 카카오 OAuth 인증 페이지로 리다이렉트
    window.location.href = kakaoOAuthUrl;
  };

  return (
    <div className={styles.kakaoLoginWidget}>
      <button className={styles.kakaoButton} onClick={handleKakaoLogin}>
        카카오 계정으로 로그인
      </button>
    </div>
  );
}
