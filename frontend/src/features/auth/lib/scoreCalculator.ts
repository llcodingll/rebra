interface SurveyData {
  nickname: string;
  age: string;
  incomeSource: string;
  purpose: string;
  period: string;
  experience: string;
  products: string[];
  maxLoss: string;
  style: string;
  riskAwareness: boolean;
  resultAgreement: boolean;
  decisionConfirmation: boolean;
}

export interface ScoreResult {
  investmentPurpose: number;
  investmentExperience: number;
  riskTolerance: number;
}

export class ScoreCalculator {
  private static readonly PURPOSE_SCORES: Record<string, number> = {
    'wealth': 15,      // 자산 증식
    'retirement': 20,  // 노후 대비
    'shortTerm': 5,    // 단기 수익
    'education': 12,   // 자녀 교육
    'other': 5,        // 기타
  };

  private static readonly PERIOD_SCORES: Record<string, number> = {
    'under1': 5,       // 1년 이하
    '1to3': 10,        // 1~3년
    '3to5': 18,        // 3~5년
    'over5': 25,       // 5년 이상
  };

  private static readonly PRODUCT_SCORES: Record<string, number> = {
    'deposit': 2,      // 예금/적금
    'stock': 6,        // 주식
    'bond': 6,         // 채권
    'fund': 6,         // 펀드
    'etf': 6,          // ETF
    'derivatives': 6,  // 파생상품
    'crypto': 6,       // 암호화폐
  };

  private static readonly EXPERIENCE_SCORES: Record<string, number> = {
    'none': 0,         // 없음
    'under1': 5,       // 1년 미만
    '1to3': 10,        // 1~3년
    '3to5': 15,        // 3~5년
    'over5': 20,       // 5년 이상
  };

  private static readonly MAX_LOSS_SCORES: Record<string, number> = {
    'none': 0,         // 손실을 원하지 않음
    'under5': 8,       // 원금의 5%
    'under10': 15,     // 10%
    'under20': 20,     // 20%
    'over30': 25,      // 30% 이상 가능
  };

  private static readonly STYLE_SCORES: Record<string, number> = {
    'stable': 5,       // 안정성 중시
    'balanced': 10,    // 안정성과 수익의 균형
    'aggressive': 15,  // 적극적인 수익 추구
    'highRisk': 20,    // 고위험 고수익 추구
  };

  static calculateScores(surveyData: SurveyData): ScoreResult {
    const investmentPurpose = this.calculateInvestmentPurpose(surveyData);
    const investmentExperience = this.calculateInvestmentExperience(surveyData);
    const riskTolerance = this.calculateRiskTolerance(surveyData);

    return {
      investmentPurpose,
      investmentExperience,
      riskTolerance,
    };
  }

  private static calculateInvestmentPurpose(surveyData: SurveyData): number {
    const purposeScore = this.PURPOSE_SCORES[surveyData.purpose] || 5;
    const periodScore = this.PERIOD_SCORES[surveyData.period] || 5;
    return purposeScore + periodScore;
  }

  private static calculateInvestmentExperience(surveyData: SurveyData): number {
    // 투자 상품 경험 점수 계산
    const productScore = surveyData.products.reduce((total, product) => {
      return total + (this.PRODUCT_SCORES[product] || 0);
    }, 0);

    // 투자 기간 경험 점수
    const experienceScore = this.EXPERIENCE_SCORES[surveyData.experience] || 0;

    return productScore + experienceScore;
  }

  private static calculateRiskTolerance(surveyData: SurveyData): number {
    const maxLossScore = this.MAX_LOSS_SCORES[surveyData.maxLoss] || 0;
    const styleScore = this.STYLE_SCORES[surveyData.style] || 5;
    return maxLossScore + styleScore;
  }

  // 나이를 숫자로 변환하는 유틸리티 함수
  static convertAgeToNumber(age: string): number {
    const ageMap: Record<string, number> = {
      '20s': 25,
      '30s': 35,
      '40s': 45,
      '50s': 55,
      '60s': 65,
    };
    return ageMap[age] || 30;
  }

  // 소득원을 문자열로 변환하는 유틸리티 함수
  static convertIncomeSource(incomeSource: string): string {
    const incomeMap: Record<string, string> = {
      'salary': '급여',
      'business': '사업소득',
      'pension': '연금',
      'other': '기타',
    };
    return incomeMap[incomeSource] || '기타';
  }
}