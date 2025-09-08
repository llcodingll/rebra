import { useNavigate } from 'react-router-dom';
import {
  imgVector,
  imgVector1,
  imgVector2,
  imgVector3,
  imgSvg,
  imgSvg1,
  imgSvg2,
  imgSvg3,
  imgSvg4,
  imgSvg5,
  imgSvg6,
  imgImage,
  imgVector4,
  imgVector5,
  imgVector6,
  imgVector7,
  imgVector8,
  imgVector9,
} from '../assets/imports/svg-25rg9';
import styles from './LandingPage.module.css';

export default function LandingPage() {
  const navigate = useNavigate();

  const handleSignup = () => navigate('/signup');
  const handleLogin = () => navigate('/login');

  return (
    <div className={styles.landingPage}>
      {/* Header */}
      <div className={styles.header}>
        <div className={styles.headerContainer}>
          <div className={styles.leftSection}>
            <div className={styles.logo}>
              <div className={styles.logoIcon}>R</div>
              <span className={styles.logoText}>Rebra</span>
            </div>

            <div className={styles.navigation}>
              <a href='#features' className={styles.navItem}>
                기능
              </a>
              <a href='#how-it-works' className={styles.navItem}>
                사용법
              </a>
              <a href='#pricing' className={styles.navItem}>
                요금제
              </a>
              <a href='#testimonials' className={styles.navItem}>
                후기
              </a>
            </div>
          </div>

          <div className={styles.rightSection}>
            <button className={styles.loginButton} onClick={handleLogin}>
              로그인
            </button>
            <button className={styles.signupButton} onClick={handleSignup}>
              무료로 시작하기
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className={styles.main}>
        {/* Hero Section */}
        <section className={styles.heroSection}>
          <div className={styles.heroContainer}>
            <div className={styles.heroContent}>
              <div className={styles.heroBadge}>
                <svg width='10' height='11' viewBox='0 0 10 11' fill='none'>
                  <path
                    d='M1.43899 6.25114C1.3562 6.25142 1.27503 6.22821 1.20491 6.18419C1.13479 6.14018 1.07859 6.07717 1.04286 6.00249C1.00712 5.92781 0.993308 5.84452 1.00302 5.7623C1.01274 5.68008 1.04558 5.60231 1.09774 5.53801L5.42899 1.07551C5.46148 1.03801 5.50576 1.01267 5.55455 1.00365C5.60334 0.994625 5.65375 1.00246 5.6975 1.02586C5.74125 1.04926 5.77575 1.08684 5.79533 1.13244C5.8149 1.17803 5.8184 1.22892 5.80524 1.27676L4.96524 3.91051C4.94047 3.97681 4.93215 4.04812 4.941 4.11833C4.94985 4.18854 4.97559 4.25556 5.01603 4.31364C5.05647 4.37172 5.11039 4.41911 5.17318 4.45177C5.23596 4.48443 5.30572 4.50137 5.37649 4.50114H8.43899C8.52178 4.50086 8.60295 4.52407 8.67307 4.56809C8.7432 4.6121 8.79939 4.67511 8.83513 4.74979C8.87086 4.82447 8.88468 4.90776 8.87496 4.98998C8.86525 5.0722 8.8324 5.14997 8.78024 5.21426L4.44899 9.67676C4.4165 9.71427 4.37223 9.73961 4.32344 9.74863C4.27465 9.75765 4.22424 9.74982 4.18049 9.72642C4.13673 9.70302 4.10224 9.66543 4.08266 9.61984C4.06308 9.57425 4.05958 9.52336 4.07274 9.47551L4.91274 6.84177C4.93751 6.77547 4.94583 6.70416 4.93698 6.63395C4.92814 6.56374 4.90239 6.49672 4.86195 6.43864C4.82151 6.38056 4.76759 6.33316 4.70481 6.30051C4.64203 6.26785 4.57226 6.25091 4.50149 6.25114H1.43899Z'
                    stroke='#155DFC'
                    strokeWidth='0.875'
                    strokeLinecap='round'
                    strokeLinejoin='round'
                  />
                </svg>
                <span>AI 자동 리밸런싱</span>
              </div>

              <h1 className={styles.heroTitle}>
                투자의 미래,
                <br />
                스마트한 선택
              </h1>

              <p className={styles.heroDescription}>
                복잡한 포트폴리오 관리를 AI가 대신합니다.
                <br />
                자동 리밸런싱으로 더 안전하고 수익성 높은 투자를 시작하세요.
              </p>

              <div className={styles.heroButtons}>
                <button className={styles.primaryButton} onClick={handleSignup}>
                  <svg width='14' height='14' viewBox='0 0 14 14' fill='none'>
                    <path
                      d='M1 1H9.16667'
                      stroke='white'
                      strokeWidth='1.16667'
                      strokeLinecap='round'
                      strokeLinejoin='round'
                    />
                    <path
                      d='M1 1L5.08333 5.08333L1 9.16667'
                      stroke='white'
                      strokeWidth='1.16667'
                      strokeLinecap='round'
                      strokeLinejoin='round'
                    />
                  </svg>
                  무료로 시작하기
                </button>
                <button className={styles.secondaryButton}>
                  <svg width='14' height='14' viewBox='0 0 14 14' fill='none'>
                    <path
                      d='M1 1L9.16667 6.25L1 11.5V1Z'
                      stroke='#0A0A0A'
                      strokeWidth='1.16667'
                      strokeLinecap='round'
                      strokeLinejoin='round'
                    />
                  </svg>
                  데모 영상 보기
                </button>
              </div>

              <div className={styles.stats}>
                <div className={styles.stat}>
                  <span className={styles.statNumber}>10만+</span>
                  <span className={styles.statLabel}>누적 사용자</span>
                </div>
                <div className={styles.stat}>
                  <span className={styles.statNumber}>₩2,847억</span>
                  <span className={styles.statLabel}>관리 자산</span>
                </div>
                <div className={styles.stat}>
                  <span className={styles.statNumber}>18.5%</span>
                  <span className={styles.statLabel}>평균 수익률</span>
                </div>
                <div className={styles.stat}>
                  <span className={styles.statNumber}>99.9%</span>
                  <span className={styles.statLabel}>시스템 안정성</span>
                </div>
              </div>
            </div>

            <div className={styles.heroImage}>
              <div className={styles.phoneContainer}>
                <div className={styles.phoneGradient}></div>
                <div className={styles.phoneFrame}></div>
              </div>
            </div>
          </div>
        </section>

        <section className={styles.ctaSection}>
          <div className={styles.ctaContainer}>
            <h2>더 스마트한 투자, 지금 시작하세요</h2>
            <p>14일 무료 체험으로 Rebra의 모든 기능을 경험해보세요</p>
            <div className={styles.ctaButtons}>
              <button className={styles.ctaPrimary} onClick={handleSignup}>
                <svg width='14' height='14' viewBox='0 0 14 14' fill='none'>
                  <path
                    d='M1 1H9.16667'
                    stroke='#155DFC'
                    strokeWidth='1.16667'
                    strokeLinecap='round'
                    strokeLinejoin='round'
                  />
                  <path
                    d='M1 1L5.08333 5.08333L1 9.16667'
                    stroke='#155DFC'
                    strokeWidth='1.16667'
                    strokeLinecap='round'
                    strokeLinejoin='round'
                  />
                </svg>
                무료로 시작하기
              </button>
              <button className={styles.ctaSecondary} onClick={handleLogin}>
                로그인
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}

// export default function LandingPage({ onSignup, onLogin }: LandingPageProps) {
//   return (
//     <div className={styles.landingPage}>
//       {/* Header */}
//       <div className={styles.header}>
//         <div className={styles.headerContainer}>
//           <div className={styles.leftSection}>
//             <div className={styles.logo}>
//               <div className={styles.logoIcon}>R</div>
//               <span className={styles.logoText}>Rebra</span>
//             </div>

//             <div className={styles.navigation}>
//               <a href="#features" className={styles.navItem}>기능</a>
//               <a href="#how-it-works" className={styles.navItem}>사용법</a>
//               <a href="#pricing" className={styles.navItem}>요금제</a>
//               <a href="#testimonials" className={styles.navItem}>후기</a>
//             </div>
//           </div>

//           <div className={styles.rightSection}>
//             <button className={styles.loginButton} onClick={onLogin}>
//               로그인
//             </button>
//             <button className={styles.signupButton} onClick={onSignup}>
//               무료로 시작하기
//             </button>
//           </div>
//         </div>
//       </div>

//       {/* Main Content */}
//       <div className={styles.main}>
//         {/* Hero Section */}
//         <section className={styles.heroSection}>
//           <div className={styles.heroContainer}>
//             <div className={styles.heroContent}>
//               <div className={styles.heroBadge}>
//                 <svg width="10" height="11" viewBox="0 0 10 11" fill="none">
//                   <path d="M1.43899 6.25114C1.3562 6.25142 1.27503 6.22821 1.20491 6.18419C1.13479 6.14018 1.07859 6.07717 1.04286 6.00249C1.00712 5.92781 0.993308 5.84452 1.00302 5.7623C1.01274 5.68008 1.04558 5.60231 1.09774 5.53801L5.42899 1.07551C5.46148 1.03801 5.50576 1.01267 5.55455 1.00365C5.60334 0.994625 5.65375 1.00246 5.6975 1.02586C5.74125 1.04926 5.77575 1.08684 5.79533 1.13244C5.8149 1.17803 5.8184 1.22892 5.80524 1.27676L4.96524 3.91051C4.94047 3.97681 4.93215 4.04812 4.941 4.11833C4.94985 4.18854 4.97559 4.25556 5.01603 4.31364C5.05647 4.37172 5.11039 4.41911 5.17318 4.45177C5.23596 4.48443 5.30572 4.50137 5.37649 4.50114H8.43899C8.52178 4.50086 8.60295 4.52407 8.67307 4.56809C8.7432 4.6121 8.79939 4.67511 8.83513 4.74979C8.87086 4.82447 8.88468 4.90776 8.87496 4.98998C8.86525 5.0722 8.8324 5.14997 8.78024 5.21426L4.44899 9.67676C4.4165 9.71427 4.37223 9.73961 4.32344 9.74863C4.27465 9.75765 4.22424 9.74982 4.18049 9.72642C4.13673 9.70302 4.10224 9.66543 4.08266 9.61984C4.06308 9.57425 4.05958 9.52336 4.07274 9.47551L4.91274 6.84177C4.93751 6.77547 4.94583 6.70416 4.93698 6.63395C4.92814 6.56374 4.90239 6.49672 4.86195 6.43864C4.82151 6.38056 4.76759 6.33316 4.70481 6.30051C4.64203 6.26785 4.57226 6.25091 4.50149 6.25114H1.43899Z" stroke="#155DFC" strokeWidth="0.875" strokeLinecap="round" strokeLinejoin="round"/>
//                 </svg>
//                 <span>AI 자동 리밸런싱</span>
//               </div>

//               <h1 className={styles.heroTitle}>
//                 투자의 미래,<br />
//                 스마트한 선택
//               </h1>

//               <p className={styles.heroDescription}>
//                 복잡한 포트폴리오 관리를 AI가 대신합니다.<br />
//                 자동 리밸런싱으로 더 안전하고 수익성 높은 투자를 시작하세요.
//               </p>

//               <div className={styles.heroButtons}>
//                 <button className={styles.primaryButton} onClick={onSignup}>
//                   <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
//                     <path d="M1 1H9.16667" stroke="white" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
//                     <path d="M1 1L5.08333 5.08333L1 9.16667" stroke="white" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
//                   </svg>
//                   무료로 시작하기
//                 </button>
//                 <button className={styles.secondaryButton}>
//                   <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
//                     <path d="M1 1L9.16667 6.25L1 11.5V1Z" stroke="#0A0A0A" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
//                   </svg>
//                   데모 영상 보기
//                 </button>
//               </div>

//               <div className={styles.stats}>
//                 <div className={styles.stat}>
//                   <span className={styles.statNumber}>10만+</span>
//                   <span className={styles.statLabel}>누적 사용자</span>
//                 </div>
//                 <div className={styles.stat}>
//                   <span className={styles.statNumber}>₩2,847억</span>
//                   <span className={styles.statLabel}>관리 자산</span>
//                 </div>
//                 <div className={styles.stat}>
//                   <span className={styles.statNumber}>18.5%</span>
//                   <span className={styles.statLabel}>평균 수익률</span>
//                 </div>
//                 <div className={styles.stat}>
//                   <span className={styles.statNumber}>99.9%</span>
//                   <span className={styles.statLabel}>시스템 안정성</span>
//                 </div>
//               </div>
//             </div>

//             <div className={styles.heroImage}>
//               <div className={styles.phoneContainer}>
//                 <div className={styles.phoneGradient}></div>
//                 <div className={styles.phoneFrame}>
//                   {/* Phone mockup content */}
//                 </div>
//               </div>
//             </div>
//           </div>
//         </section>

//         {/* Features Section */}
//         <section id="features" className={styles.featuresSection}>
//           <div className={styles.sectionContainer}>
//             <div className={styles.sectionHeader}>
//               <h2>왜 Rebra인가요?</h2>
//               <p>복잡한 투자 관리를 간단하게 만들어주는 스마트한 기능들</p>
//             </div>

//             <div className={styles.featuresGrid}>
//               <div className={styles.featureCard}>
//                 <div className={styles.featureIcon}>
//                   <svg width="21" height="21" viewBox="0 0 21 21" fill="none">
//                     <path d="M10.5 19.25C15.3325 19.25 19.25 15.3325 19.25 10.5C19.25 5.66751 15.3325 1.75 10.5 1.75C5.66751 1.75 1.75 5.66751 1.75 10.5C1.75 15.3325 5.66751 19.25 10.5 19.25Z" stroke="white" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round"/>
//                     <path d="M10.5 15.75C13.3995 15.75 15.75 13.3995 15.75 10.5C15.75 7.60051 13.3995 5.25 10.5 5.25C7.60051 5.25 5.25 7.60051 5.25 10.5C5.25 13.3995 7.60051 15.75 10.5 15.75Z" stroke="white" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round"/>
//                     <path d="M10.5 12.25C11.4665 12.25 12.25 11.4665 12.25 10.5C12.25 9.5335 11.4665 8.75 10.5 8.75C9.5335 8.75 8.75 9.5335 8.75 10.5C8.75 11.4665 9.5335 12.25 10.5 12.25Z" stroke="white" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round"/>
//                   </svg>
//                 </div>
//                 <h3>자동 리밸런싱</h3>
//                 <p>목표 비중을 설정하면 AI가 자동으로 포트폴리오를 균형있게 조정합니다.</p>
//               </div>

//               <div className={styles.featureCard}>
//                 <div className={styles.featureIcon}>
//                   <svg width="21" height="21" viewBox="0 0 21 21" fill="none">
//                     <path d="M2.625 2.625V16.625C2.625 17.0891 2.80937 17.5342 3.13756 17.8624C3.46575 18.1906 3.91087 18.375 4.375 18.375H18.375" stroke="white" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round"/>
//                     <path d="M15.75 14.875V7.875" stroke="white" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round"/>
//                     <path d="M11.375 14.875V4.375" stroke="white" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round"/>
//                     <path d="M7 14.875V12.25" stroke="white" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round"/>
//                   </svg>
//                 </div>
//                 <h3>실시간 분석</h3>
//                 <p>실시간 시장 데이터와 포트폴리오 성과를 한눈에 확인하세요.</p>
//               </div>

//               <div className={styles.featureCard}>
//                 <div className={styles.featureIcon}>
//                   <svg width="21" height="21" viewBox="0 0 21 21" fill="none">
//                     <path d="M17.5 11.375C17.5 15.75 14.4375 17.9375 10.7975 19.2062C10.6069 19.2708 10.3998 19.2677 10.2113 19.1975C6.5625 17.9375 3.5 15.75 3.5 11.375V5.25C3.5 5.01794 3.59219 4.79538 3.75628 4.63128C3.92038 4.46719 4.14294 4.375 4.375 4.375C6.125 4.375 8.3125 3.325 9.835 1.995C10.0204 1.83662 10.2562 1.74961 10.5 1.74961C10.7438 1.74961 10.9796 1.83662 11.165 1.995C12.6963 3.33375 14.875 4.375 16.625 4.375C16.8571 4.375 17.0796 4.46719 17.2437 4.63128C17.4078 4.79538 17.5 5.01794 17.5 5.25V11.375Z" stroke="white" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round"/>
//                   </svg>
//                 </div>
//                 <h3>안전한 투자</h3>
//                 <p>리스크 관리와 분산투자로 더 안전한 자산 운용이 가능합니다.</p>
//               </div>

//               <div className={styles.featureCard}>
//                 <div className={styles.featureIcon}>
//                   <svg width="21" height="21" viewBox="0 0 21 21" fill="none">
//                     <path d="M3.5 12.25C3.33442 12.2506 3.17208 12.2041 3.03183 12.1161C2.89159 12.0281 2.7792 11.9021 2.70773 11.7527C2.63626 11.6033 2.60863 11.4368 2.62806 11.2723C2.64749 11.1079 2.71318 10.9523 2.8175 10.8238L11.48 1.89875C11.545 1.82375 11.6335 1.77306 11.7311 1.75502C11.8287 1.73697 11.9295 1.75264 12.017 1.79944C12.1045 1.84625 12.1735 1.92141 12.2127 2.01259C12.2518 2.10378 12.2588 2.20557 12.2325 2.30125L10.5525 7.56875C10.503 7.70133 10.4863 7.84396 10.504 7.98438C10.5217 8.12481 10.5732 8.25885 10.6541 8.375C10.735 8.49115 10.8428 8.58595 10.9684 8.65127C11.0939 8.71658 11.2335 8.75046 11.375 8.75H17.5C17.6656 8.74944 17.8279 8.79587 17.9682 8.88389C18.1084 8.97192 18.2208 9.09794 18.2923 9.2473C18.3637 9.39666 18.3914 9.56324 18.3719 9.72767C18.3525 9.89211 18.2868 10.0477 18.1825 10.1763L9.52 19.1013C9.45502 19.1763 9.36647 19.2269 9.26889 19.245C9.17131 19.263 9.07049 19.2474 8.98299 19.2006C8.89548 19.1538 8.82649 19.0786 8.78733 18.9874C8.74818 18.8962 8.74118 18.7944 8.7675 18.6988L10.4475 13.4313C10.497 13.2987 10.5137 13.156 10.496 13.0156C10.4783 12.8752 10.4268 12.7412 10.3459 12.625C10.265 12.5089 10.1572 12.4141 10.0316 12.3487C9.90607 12.2834 9.76654 12.2495 9.625 12.25H3.5Z" stroke="white" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round"/>
//                   </svg>
//                 </div>
//                 <h3>빠른 실행</h3>
//                 <p>클릭 한 번으로 리밸런싱을 실행하고 최적의 포트폴리오를 유지하세요.</p>
//               </div>
//             </div>
//           </div>
//         </section>

//         {/* How it works Section */}
//         <section id="how-it-works" className={styles.howItWorksSection}>
//           <div className={styles.sectionContainer}>
//             <div className={styles.sectionHeader}>
//               <h2>3단계로 시작하는 스마트 투자</h2>
//               <p>복잡한 설정 없이 바로 시작할 수 있어요</p>
//             </div>

//             <div className={styles.stepsGrid}>
//               <div className={styles.step}>
//                 <div className={styles.stepNumber}>01</div>
//                 <div className={styles.stepIcon}>
//                   <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
//                     <path d="M14 25.6667C20.4433 25.6667 25.6667 20.4433 25.6667 14C25.6667 7.55668 20.4433 2.33333 14 2.33333C7.55668 2.33333 2.33333 7.55668 2.33333 14C2.33333 20.4433 7.55668 25.6667 14 25.6667Z" stroke="white" strokeWidth="2.33333" strokeLinecap="round" strokeLinejoin="round"/>
//                   </svg>
//                 </div>
//                 <h3>포트폴리오 설정</h3>
//                 <p>보유 주식과 목표 비중을 간단하게 입력하세요.</p>
//               </div>

//               <div className={styles.step}>
//                 <div className={styles.stepNumber}>02</div>
//                 <div className={styles.stepIcon}>
//                   <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
//                     <path d="M3.5 3.5V22.1667C3.5 22.7855 3.74583 23.379 4.18342 23.8166C4.621 24.2542 5.2145 24.5 5.83333 24.5H24.5" stroke="white" strokeWidth="2.33333" strokeLinecap="round" strokeLinejoin="round"/>
//                   </svg>
//                 </div>
//                 <h3>AI 분석</h3>
//                 <p>AI가 시장 상황과 포트폴리오 균형을 실시간으로 분석합니다.</p>
//               </div>

//               <div className={styles.step}>
//                 <div className={styles.stepNumber}>03</div>
//                 <div className={styles.stepIcon}>
//                   <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
//                     <path d="M4.66667 16.3333C4.44589 16.3341 4.22944 16.2722 4.04245 16.1548C3.85546 16.0374 3.70561 15.8694 3.61031 15.6703C3.51501 15.4711 3.47818 15.249 3.50408 15.0298C3.52999 14.8105 3.61758 14.6031 3.75667 14.4317L15.3067 2.53167C15.3933 2.43166 15.5114 2.36408 15.6415 2.34002C15.7716 2.31596 15.906 2.33685 16.0227 2.39926C16.1394 2.46166 16.2313 2.56188 16.2836 2.68346C16.3358 2.80504 16.3451 2.94076 16.31 3.06833L14.07 10.0917C14.0039 10.2684 13.9818 10.4586 14.0054 10.6458C14.0289 10.8331 14.0976 11.0118 14.2054 11.1667C14.3133 11.3215 14.4571 11.4479 14.6245 11.535C14.7919 11.6221 14.978 11.6673 15.1667 11.6667H23.3333C23.5541 11.6659 23.7706 11.7278 23.9576 11.8452C24.1445 11.9626 24.2944 12.1306 24.3897 12.3297C24.485 12.5289 24.5218 12.751 24.4959 12.9702C24.47 13.1895 24.3824 13.3969 24.2433 13.5683L12.6933 25.4683C12.6067 25.5683 12.4886 25.6359 12.3585 25.66C12.2284 25.684 12.094 25.6632 11.9773 25.6007C11.8606 25.5383 11.7687 25.4381 11.7164 25.3165C11.6642 25.195 11.6549 25.0592 11.69 24.9317L13.93 17.9083C13.9961 17.7316 14.0182 17.5414 13.9946 17.3542C13.9711 17.1669 13.9024 16.9882 13.7946 16.8333C13.6867 16.6785 13.5429 16.5521 13.3755 16.465C13.2081 16.3779 13.022 16.3327 12.8333 16.3333H4.66667Z" stroke="white" strokeWidth="2.33333" strokeLinecap="round" strokeLinejoin="round"/>
//                   </svg>
//                 </div>
//                 <h3>자동 실행</h3>
//                 <p>최적의 타이밍에 자동으로 리밸런싱이 실행됩니다.</p>
//               </div>
//             </div>
//           </div>
//         </section>

//         {/* Testimonials Section */}
//         <section id="testimonials" className={styles.testimonialsSection}>
//           <div className={styles.sectionContainer}>
//             <div className={styles.sectionHeader}>
//               <h2>고객들의 이야기</h2>
//               <p>실제 사용자들의 생생한 후기를 확인해보세요</p>
//             </div>

//             <div className={styles.testimonialsGrid}>
//               <div className={styles.testimonialCard}>
//                 <div className={styles.stars}>
//                   {[...Array(5)].map((_, i) => (
//                     <div key={i} className={styles.star}></div>
//                   ))}
//                 </div>
//                 <p>"복잡한 리밸런싱을 자동으로 해주니까 정말 편해요. 수익률도 20% 이상 올랐습니다!"</p>
//                 <div className={styles.testimonialAuthor}>
//                   <div className={styles.avatar}></div>
//                   <div>
//                     <span className={styles.authorName}>김민수</span>
//                     <span className={styles.authorTitle}>직장인 투자자</span>
//                   </div>
//                 </div>
//               </div>

//               <div className={styles.testimonialCard}>
//                 <div className={styles.stars}>
//                   {[...Array(5)].map((_, i) => (
//                     <div key={i} className={styles.star}></div>
//                   ))}
//                 </div>
//                 <p>"백테스트 기능으로 전략을 검증하고 실제 적용했더니 안정적인 수익을 얻고 있어요."</p>
//                 <div className={styles.testimonialAuthor}>
//                   <div className={styles.avatar}></div>
//                   <div>
//                     <span className={styles.authorName}>박지영</span>
//                     <span className={styles.authorTitle}>전업 투자자</span>
//                   </div>
//                 </div>
//               </div>

//               <div className={styles.testimonialCard}>
//                 <div className={styles.stars}>
//                   {[...Array(5)].map((_, i) => (
//                     <div key={i} className={styles.star}></div>
//                   ))}
//                 </div>
//                 <p>"투자 초보자도 쉽게 사용할 수 있어서 좋아요. 이제 전문가처럼 포트폴리오를 관리해요."</p>
//                 <div className={styles.testimonialAuthor}>
//                   <div className={styles.avatar}></div>
//                   <div>
//                     <span className={styles.authorName}>이동현</span>
//                     <span className={styles.authorTitle}>신규 투자자</span>
//                   </div>
//                 </div>
//               </div>
//             </div>
//           </div>
//         </section>

//         {/* Pricing Section */}
//         <section id="pricing" className={styles.pricingSection}>
//           <div className={styles.sectionContainer}>
//             <div className={styles.sectionHeader}>
//               <h2>투자 목표에 맞는 요금제</h2>
//               <p>언제든지 업그레이드나 다운그레이드가 가능해요</p>
//             </div>

//             <div className={styles.pricingGrid}>
//               <div className={styles.pricingCard}>
//                 <h3>Basic</h3>
//                 <div className={styles.price}>무료</div>
//                 <p>개인 투자자를 위한 기본 기능</p>
//                 <ul className={styles.features}>
//                   <li>5개 종목 포트폴리오</li>
//                   <li>월 1회 자동 리밸런싱</li>
//                   <li>기본 성과 분석</li>
//                   <li>이메일 지원</li>
//                 </ul>
//                 <button className={styles.pricingButton}>시작하기</button>
//               </div>

//               <div className={`${styles.pricingCard} ${styles.featured}`}>
//                 <div className={styles.badge}>추천</div>
//                 <h3>Pro</h3>
//                 <div className={styles.price}>₩29,000/월</div>
//                 <p>전문 투자자를 위한 고급 기능</p>
//                 <ul className={styles.features}>
//                   <li>무제한 종목 포트폴리오</li>
//                   <li>실시간 자동 리밸런싱</li>
//                   <li>고급 백테스트 분석</li>
//                   <li>AI 투자 추천</li>
//                   <li>우선 고객 지원</li>
//                   <li>API 연동</li>
//                 </ul>
//                 <button className={styles.pricingButtonPrimary} onClick={onSignup}>시작하기</button>
//               </div>

//               <div className={styles.pricingCard}>
//                 <h3>Enterprise</h3>
//                 <div className={styles.price}>문의</div>
//                 <p>기관 투자자를 위한 엔터프라이즈</p>
//                 <ul className={styles.features}>
//                   <li>모든 Pro 기능</li>
//                   <li>전담 어드바이저</li>
//                   <li>커스텀 전략 개발</li>
//                   <li>온프레미스 설치</li>
//                   <li>SLA 보장</li>
//                 </ul>
//                 <button className={styles.pricingButton}>문의하기</button>
//               </div>
//             </div>
//           </div>
//         </section>

//         {/* CTA Section */}
//         <section className={styles.ctaSection}>
//           <div className={styles.ctaContainer}>
//             <h2>더 스마트한 투자, 지금 시작하세요</h2>
//             <p>14일 무료 체험으로 Rebra의 모든 기능을 경험해보세요</p>
//             <div className={styles.ctaButtons}>
//               <button className={styles.ctaPrimary} onClick={onSignup}>
//                 <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
//                   <path d="M1 1H9.16667" stroke="#155DFC" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
//                   <path d="M1 1L5.08333 5.08333L1 9.16667" stroke="#155DFC" strokeWidth="1.16667" strokeLinecap="round" strokeLinejoin="round"/>
//                 </svg>
//                 무료로 시작하기
//               </button>
//               <button className={styles.ctaSecondary} onClick={onLogin}>
//                 로그인
//               </button>
//             </div>
//           </div>
//         </section>
//       </div>

//       {/* Footer */}
//       <footer className={styles.footer}>
//         <div className={styles.footerContainer}>
//           <div className={styles.footerContent}>
//             <div className={styles.footerBrand}>
//               <div className={styles.footerLogo}>
//                 <div className={styles.logoIcon}>R</div>
//                 <span className={styles.logoText}>Rebra</span>
//               </div>
//               <p>AI 기반 포트폴리오 자동 리밸런싱으로 더 스마트한 투자를 시작하세요.</p>
//             </div>

//             <div className={styles.footerLinks}>
//               <div className={styles.linkGroup}>
//                 <h4>제품</h4>
//                 <ul>
//                   <li><a href="#features">기능</a></li>
//                   <li><a href="#pricing">요금제</a></li>
//                   <li><a href="#api">API</a></li>
//                   <li><a href="#mobile">모바일 앱</a></li>
//                 </ul>
//               </div>

//               <div className={styles.linkGroup}>
//                 <h4>지원</h4>
//                 <ul>
//                   <li><a href="#help">도움말</a></li>
//                   <li><a href="#community">커뮤니티</a></li>
//                   <li><a href="#support">고객센터</a></li>
//                   <li><a href="#feedback">피드백</a></li>
//                 </ul>
//               </div>

//               <div className={styles.linkGroup}>
//                 <h4>회사</h4>
//                 <ul>
//                   <li><a href="#about">소개</a></li>
//                   <li><a href="#careers">채용</a></li>
//                   <li><a href="#blog">블로그</a></li>
//                   <li><a href="#press">언론</a></li>
//                 </ul>
//               </div>
//             </div>
//           </div>

//           <div className={styles.footerBottom}>
//             <span>© 2024 Rebra. All rights reserved.</span>
//             <div className={styles.footerBottomLinks}>
//               <a href="#privacy">개인정보처리방침</a>
//               <a href="#terms">이용약관</a>
//               <a href="#cookies">쿠키정책</a>
//             </div>
//           </div>
//         </div>
//       </footer>
//     </div>
//   );
// }
