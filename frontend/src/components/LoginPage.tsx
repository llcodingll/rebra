import kakaoIcon from 'figma:asset/b58690531dd5cbb1352f8e5c713b25639e76dae9.png';
import styles from './LoginPage.module.css';

interface LoginPageProps {
  onBack: () => void;
  onSignup: () => void;
  onLoginSuccess: () => void;
}

export default function LoginPage({ onBack, onSignup, onLoginSuccess }: LoginPageProps) {
  const handleKakaoLogin = () => {
    // 카카오 로그인 처리 (실제로는 카카오 SDK 호출)
    onLoginSuccess();
  };

  return (
    <div className={styles.loginPage}>
      {/* Background */}
      <div className={styles.background}>
        <div className={styles.backgroundDecorations}>
          <div className={styles.decoration1}></div>
          <div className={styles.decoration2}></div>
          <div className={styles.decoration3}></div>
          <div className={styles.decoration4}></div>
        </div>

        <div className={styles.leftContent}>
          <button className={styles.backButton} onClick={onBack}>
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M5.08333 9.16667L1 5.08333L5.08333 1" stroke="white" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M9.16667 1H1" stroke="white" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            홈으로 돌아가기
          </button>

          <div className={styles.brandSection}>
            <div className={styles.logo}>
              <div className={styles.logoIcon}>R</div>
              <span className={styles.logoText}>ReBalance Pro</span>
            </div>

            <div className={styles.heroContent}>
              <h1>투자의 미래를<br />지금 시작하세요</h1>
              <p>AI가 관리하는 스마트한 포트폴리오로<br />더 안전하고 수익성 높은 투자를 경험해보세요</p>
            </div>
          </div>

          <div className={styles.features}>
            <div className={styles.feature}>
              <div className={styles.featureIcon}>
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M2.33333 8.16667C2.22295 8.16704 2.11472 8.13609 2.02122 8.0774C1.92773 8.01872 1.8528 7.93471 1.80515 7.83513C1.75751 7.73556 1.73909 7.62451 1.75204 7.51488C1.765 7.40526 1.80879 7.30156 1.87833 7.21583L7.65333 1.26583C7.69665 1.21583 7.75568 1.18204 7.82074 1.17001C7.88579 1.15798 7.95301 1.16842 8.01134 1.19963C8.06968 1.23083 8.11567 1.28094 8.14178 1.34173C8.16788 1.40252 8.17255 1.47038 8.155 1.53417L7.035 5.04583C7.00197 5.13422 6.99088 5.2293 7.00268 5.32292C7.01447 5.41654 7.0488 5.5059 7.10272 5.58333C7.15664 5.66077 7.22853 5.72397 7.31224 5.76751C7.39595 5.81105 7.48898 5.83364 7.58333 5.83333H11.6667C11.7771 5.83296 11.8853 5.86391 11.9788 5.9226C12.0723 5.98128 12.1472 6.06529 12.1948 6.16487C12.2425 6.26444 12.2609 6.37549 12.248 6.48512C12.235 6.59474 12.1912 6.69844 12.1217 6.78417L6.34667 12.7342C6.30335 12.7842 6.24432 12.818 6.17926 12.83C6.11421 12.842 6.04699 12.8316 5.98866 12.8004C5.93032 12.7692 5.88433 12.7191 5.85822 12.6583C5.83212 12.5975 5.82745 12.5296 5.845 12.4658L6.965 8.95417C6.99803 8.86578 7.00912 8.7707 6.99732 8.67708C6.98553 8.58346 6.9512 8.4941 6.89728 8.41667C6.84336 8.33923 6.77147 8.27603 6.68776 8.23249C6.60405 8.18895 6.51102 8.16636 6.41667 8.16667H2.33333Z" stroke="white" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div>
                <h3>즉시 시작</h3>
                <p>가입 후 바로 AI 리밸런싱 서비스를 이용할 수 있어요</p>
              </div>
            </div>

            <div className={styles.feature}>
              <div className={styles.featureIcon}>
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M11.6667 7.58333C11.6667 10.5 9.625 11.9583 7.19833 12.8042C7.07126 12.8472 6.93323 12.8452 6.8075 12.7983C4.375 11.9583 2.33333 10.5 2.33333 7.58333V3.5C2.33333 3.34529 2.39479 3.19692 2.50419 3.08752C2.61358 2.97813 2.76196 2.91667 2.91667 2.91667C4.08333 2.91667 5.54167 2.21667 6.55667 1.33C6.68025 1.22442 6.83746 1.1664 7 1.1664C7.16254 1.1664 7.31975 1.22442 7.44333 1.33C8.46417 2.2225 9.91667 2.91667 11.0833 2.91667C11.238 2.91667 11.3864 2.97813 11.4958 3.08752C11.6052 3.19692 11.6667 3.34529 11.6667 3.5V7.58333Z" stroke="white" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div>
                <h3>안전한 보안</h3>
                <p>은행급 보안 시스템으로 고객님의 정보를 안전하게 보호합니다</p>
              </div>
            </div>

            <div className={styles.feature}>
              <div className={styles.featureIcon}>
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M12.7173 5.83333C12.9837 7.14076 12.7938 8.5 12.1793 9.68438C11.5649 10.8688 10.5629 11.8067 9.34063 12.3418C8.11833 12.8768 6.74953 12.9767 5.46251 12.6247C4.17548 12.2727 3.04803 11.4901 2.26816 10.4075C1.48829 9.32484 1.10315 8.00756 1.17697 6.67531C1.25079 5.34306 1.7791 4.07639 2.6738 3.08652C3.5685 2.09665 4.77551 1.44342 6.09354 1.23576C7.41157 1.02811 8.76096 1.27859 9.91667 1.94542" stroke="white" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M5.25 6.41667L7 8.16667L12.8333 2.33333" stroke="white" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <div>
                <h3>무료 체험</h3>
                <p>14일 무료 체험으로 모든 기능을 제한 없이 사용해보세요</p>
              </div>
            </div>
          </div>

          <div className={styles.appPreview}>
            <div className={styles.appImage}></div>
          </div>
        </div>
      </div>

      {/* Form Section */}
      <div className={styles.formSection}>
        <div className={styles.formContainer}>
          <div className={styles.formHeader}>
            <h1>간편 로그인</h1>
            <p>카카오 계정으로 빠르게 로그인하세요</p>
          </div>

          <div className={styles.kakaoButtonContainer}>
            <button className={styles.kakaoButton} onClick={handleKakaoLogin}>
              <img src={kakaoIcon} alt="카카오 아이콘" className={styles.kakaoIcon} />
              카카오 계정으로 로그인
            </button>
          </div>

          <p className={styles.terms}>
            로그인하면 <a href="#terms">이용약관</a>과 <a href="#privacy">개인정보처리방침</a>에 동의하는 것으로 간주됩니다.
          </p>

          <div className={styles.signupLink}>
            <span>계정이 없으신가요?</span>
            <button onClick={onSignup} className={styles.linkButton}>
              회원가입하기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}