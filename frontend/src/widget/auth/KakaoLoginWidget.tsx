import styles from './KakaoLoginWidget.module.css';

interface KakaoLoginWidgetProps {
  onLoginSuccess: () => void;
}

export default function KakaoLoginWidget({ onLoginSuccess }: KakaoLoginWidgetProps) {
  const handleKakaoLogin = () => {
    // TODO: 서버에서 카카오톡 로그인 절차를 밟고 클라이언트에 결과를 반환
    // 일단은 바로 성공 처리
    onLoginSuccess();
  };

  return (
    <div className={styles.kakaoLoginWidget}>
      <button className={styles.kakaoButton} onClick={handleKakaoLogin}>
        카카오 계정으로 로그인
      </button>
    </div>
  );
}
