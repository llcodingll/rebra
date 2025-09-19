import { useNavigate } from 'react-router-dom';
import { motion } from 'motion/react';
import {
  Scale,
  BarChart3,
  Search,
  TrendingUp,
  Shield,
  Zap,
  Brain,
  CheckCircle,
  ArrowRight
} from 'lucide-react';
import styles from './LandingPage.module.css';

export default function LandingPage() {
  const navigate = useNavigate();

  const handleSignup = () => navigate('/login');
  const handleLogin = () => navigate('/login');

  return (
    <div className={styles.landingPage}>
      {/* Header */}
      <header className={styles.header}>
        <div className={styles.headerContainer}>
          <div className={styles.logo}>
            <div className={styles.logoIcon}>
              <Scale className={styles.logoIconSvg} />
            </div>
            <span className={styles.logoText}>Rebra</span>
          </div>

          <div className={styles.headerButtons}>
            <button className={styles.loginButton} onClick={handleLogin}>
              로그인
            </button>
            <button className={styles.signupButton} onClick={handleSignup}>
              무료로 시작하기
            </button>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className={styles.heroSection}>
        <div className={styles.heroContainer}>
          <motion.div
            className={styles.heroContent}
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
          >
            <div className={styles.heroBadge}>
              <Brain size={16} />
              <span>스마트 포트폴리오 관리</span>
            </div>

            <h1 className={styles.heroTitle}>
              스마트한 투자를 위한
              <br />
              <span className={styles.highlight}>올인원 플랫폼</span>
            </h1>

            <p className={styles.heroDescription}>
              실시간 주식 데이터 검색부터 포트폴리오 백테스트까지.
              <br />
              모든 투자 도구를 한 곳에서 경험하세요.
            </p>

            <div className={styles.heroButtons}>
              <button className={styles.primaryButton} onClick={handleSignup}>
                <ArrowRight size={16} />
                무료로 시작하기
              </button>
              <button className={styles.secondaryButton} onClick={handleLogin}>
                로그인하기
              </button>
            </div>

            <div className={styles.socialProof}>
              <div className={styles.stats}>
                <div className={styles.stat}>
                  <span className={styles.statNumber}>3가지</span>
                  <span className={styles.statLabel}>핵심 기능</span>
                </div>
                <div className={styles.stat}>
                  <span className={styles.statNumber}>실시간</span>
                  <span className={styles.statLabel}>데이터 업데이트</span>
                </div>
                <div className={styles.stat}>
                  <span className={styles.statNumber}>무료</span>
                  <span className={styles.statLabel}>서비스 이용</span>
                </div>
              </div>
            </div>
          </motion.div>

          <motion.div
            className={styles.heroImage}
            initial={{ opacity: 0, x: 30 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.8, delay: 0.2 }}
          >
            <div className={styles.dashboardPreview}>
              <div className={styles.previewHeader}>
                <div className={styles.previewDots}>
                  <div></div>
                  <div></div>
                  <div></div>
                </div>
                <span>Rebra Dashboard</span>
              </div>
              <div className={styles.previewContent}>
                <div className={styles.previewChart}></div>
                <div className={styles.previewStats}>
                  <div className={styles.previewStat}></div>
                  <div className={styles.previewStat}></div>
                  <div className={styles.previewStat}></div>
                </div>
              </div>
            </div>
          </motion.div>
        </div>

      </section>

      {/* Features Section */}
      <section id="features" className={styles.featuresSection}>
          <div className={styles.sectionContainer}>
            <motion.div
              className={styles.sectionHeader}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              viewport={{ once: true }}
            >
              <h2>투자에 필요한 모든 기능을 한 곳에</h2>
              <p>세 가지 핵심 기능으로 더 스마트한 투자를 시작하세요</p>
            </motion.div>

            <div className={styles.featuresGrid}>
              <motion.div
                className={styles.featureCard}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.1 }}
                viewport={{ once: true }}
              >
                <div className={styles.featureIcon}>
                  <BarChart3 size={24} />
                </div>
                <h3>실시간 대시보드</h3>
                <p>포트폴리오 현황과 수익률을 실시간으로 모니터링하고 시각적으로 분석할 수 있습니다.</p>
                <div className={styles.featureHighlights}>
                  <span>• 실시간 데이터</span>
                  <span>• 시각적 차트</span>
                  <span>• 수익률 분석</span>
                </div>
              </motion.div>

              <motion.div
                className={styles.featureCard}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.2 }}
                viewport={{ once: true }}
              >
                <div className={styles.featureIcon}>
                  <Search size={24} />
                </div>
                <h3>스마트 주식 검색</h3>
                <p>실시간 주식 정보와 순위를 확인하고, 원하는 종목을 빠르게 찾아 분석할 수 있습니다.</p>
                <div className={styles.featureHighlights}>
                  <span>• 실시간 순위</span>
                  <span>• 종목 검색</span>
                  <span>• 보유 종목 관리</span>
                </div>
              </motion.div>

              <motion.div
                className={styles.featureCard}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.3 }}
                viewport={{ once: true }}
              >
                <div className={styles.featureIcon}>
                  <TrendingUp size={24} />
                </div>
                <h3>백테스트 시뮬레이션</h3>
                <p>과거 데이터로 투자 전략을 검증하고, 포트폴리오 성과를 미리 확인해보세요.</p>
                <div className={styles.featureHighlights}>
                  <span>• 전략 검증</span>
                  <span>• 성과 분석</span>
                  <span>• 위험 관리</span>
                </div>
              </motion.div>
            </div>
          </div>
        </section>

      {/* How it Works Section */}
      <section id="how-it-works" className={styles.howItWorksSection}>
          <div className={styles.sectionContainer}>
            <motion.div
              className={styles.sectionHeader}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              viewport={{ once: true }}
            >
              <h2>3단계로 시작하는 스마트 투자</h2>
              <p>복잡한 설정 없이 바로 시작할 수 있어요</p>
            </motion.div>

            <div className={styles.stepsContainer}>
              <motion.div
                className={styles.step}
                initial={{ opacity: 0, x: -30 }}
                whileInView={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.1 }}
                viewport={{ once: true }}
              >
                <div className={styles.stepNumber}>01</div>
                <div className={styles.stepIcon}>
                  <Search size={32} />
                </div>
                <h3>종목 검색 & 분석</h3>
                <p>실시간 주식 검색으로 관심 종목을 찾고 상세 정보를 확인하세요.</p>
              </motion.div>

              <motion.div
                className={styles.step}
                initial={{ opacity: 0, x: -30 }}
                whileInView={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.2 }}
                viewport={{ once: true }}
              >
                <div className={styles.stepNumber}>02</div>
                <div className={styles.stepIcon}>
                  <TrendingUp size={32} />
                </div>
                <h3>백테스트 실행</h3>
                <p>선택한 종목들로 포트폴리오를 구성하고 과거 데이터로 전략을 검증하세요.</p>
              </motion.div>

              <motion.div
                className={styles.step}
                initial={{ opacity: 0, x: -30 }}
                whileInView={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.3 }}
                viewport={{ once: true }}
              >
                <div className={styles.stepNumber}>03</div>
                <div className={styles.stepIcon}>
                  <BarChart3 size={32} />
                </div>
                <h3>실시간 모니터링</h3>
                <p>대시보드에서 포트폴리오 성과를 실시간으로 추적하고 관리하세요.</p>
              </motion.div>
            </div>
          </div>
        </section>

      {/* Benefits Section */}
      <section className={styles.benefitsSection}>
          <div className={styles.sectionContainer}>
            <motion.div
              className={styles.sectionHeader}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              viewport={{ once: true }}
            >
              <h2>왜 Rebra를 선택해야 할까요?</h2>
              <p>전문 투자자들이 사용하는 도구를 이제 누구나 쉽게</p>
            </motion.div>

            <div className={styles.benefitsGrid}>
              <motion.div
                className={styles.benefitCard}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.1 }}
                viewport={{ once: true }}
              >
                <Shield size={24} />
                <h3>안전한 투자</h3>
                <p>과거 데이터 기반 백테스트로 리스크를 최소화하고 안정적인 수익을 추구하세요.</p>
              </motion.div>

              <motion.div
                className={styles.benefitCard}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.2 }}
                viewport={{ once: true }}
              >
                <Zap size={24} />
                <h3>빠른 의사결정</h3>
                <p>실시간 데이터와 직관적인 인터페이스로 빠르고 정확한 투자 결정을 내리세요.</p>
              </motion.div>

              <motion.div
                className={styles.benefitCard}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.3 }}
                viewport={{ once: true }}
              >
                <Brain size={24} />
                <h3>데이터 기반 분석</h3>
                <p>과거 데이터 분석을 통해 포트폴리오 성과를 시뮬레이션하고 검증할 수 있습니다.</p>
              </motion.div>
            </div>
          </div>
        </section>

      {/* CTA Section */}
      <section className={styles.ctaSection}>
          <div className={styles.ctaContainer}>
            <motion.div
              className={styles.ctaContent}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              viewport={{ once: true }}
            >
              <h2>지금 바로 시작해보세요</h2>
              <p>지금 바로 Rebra를 체험하고 더 스마트한 투자를 시작하세요</p>

              <div className={styles.ctaButtons}>
                <button className={styles.ctaPrimary} onClick={handleSignup}>
                  <ArrowRight size={16} />
                  지금 시작하기
                </button>
                <button className={styles.ctaSecondary} onClick={handleLogin}>
                  로그인하기
                </button>
              </div>

              <div className={styles.ctaFeatures}>
                <div className={styles.ctaFeature}>
                  <CheckCircle size={16} />
                  <span>간편한 회원가입</span>
                </div>
                <div className={styles.ctaFeature}>
                  <CheckCircle size={16} />
                  <span>실시간 데이터 제공</span>
                </div>
                <div className={styles.ctaFeature}>
                  <CheckCircle size={16} />
                  <span>백테스트 기능 이용</span>
                </div>
              </div>
            </motion.div>
          </div>
        </section>
    </div>
  );
}