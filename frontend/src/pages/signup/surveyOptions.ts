export interface Option {
  value: string;
  label: string;
}

export interface Agreement {
  key: string;
  label: string;
}

export const ageOptions: Option[] = [
  { value: '20s', label: '20대' },
  { value: '30s', label: '30대' },
  { value: '40s', label: '40대' },
  { value: '50s', label: '50대' },
  { value: '60s', label: '60대 이상' },
];

export const incomeOptions: Option[] = [
  { value: 'under30', label: '3천만원 미만' },
  { value: '30to60', label: '3천-6천만원' },
  { value: '60to100', label: '6천만원-1억원' },
  { value: 'over100', label: '1억원 이상' },
];

export const assetOptions: Option[] = [
  { value: 'under10', label: '1천만원 미만' },
  { value: '10to50', label: '1천-5천만원' },
  { value: '50to100', label: '5천만원-1억원' },
  { value: 'over100', label: '1억원 이상' },
];

export const investmentRatioOptions: Option[] = [
  { value: 'under10', label: '10% 미만' },
  { value: '10to30', label: '10-30%' },
  { value: '30to50', label: '30-50%' },
  { value: 'over50', label: '50% 이상' },
];

export const emergencyOptions: Option[] = [
  { value: 'sufficient', label: '충분히 보유' },
  { value: 'partial', label: '일부 보유' },
  { value: 'minimal', label: '거의 없음' },
];

export const purposeOptions: Option[] = [
  { value: 'retirement', label: '노후 대비' },
  { value: 'wealth', label: '자산 증식' },
  { value: 'education', label: '자녀 교육비' },
  { value: 'shortTerm', label: '단기 수익' },
];

export const periodOptions: Option[] = [
  { value: 'under1', label: '1년 미만' },
  { value: '1to3', label: '1-3년' },
  { value: '3to5', label: '3-5년' },
  { value: 'over5', label: '5년 이상' },
];

export const experienceOptions: Option[] = [
  { value: 'none', label: '없음' },
  { value: 'under1', label: '1년 미만' },
  { value: '1to3', label: '1-3년' },
  { value: 'over3', label: '3년 이상' },
];

export const productOptions: Option[] = [
  { value: 'deposit', label: '예금/적금만' },
  { value: 'stock', label: '주식' },
  { value: 'fund', label: '펀드/ETF' },
  { value: 'derivatives', label: '해외투자/파생상품' },
];

export const maxLossOptions: Option[] = [
  { value: 'none', label: '손실 절대 불가' },
  { value: 'under5', label: '5% 이하' },
  { value: 'under10', label: '10% 이하' },
  { value: 'over20', label: '20% 이상' },
];

export const styleOptions: Option[] = [
  { value: 'safe', label: '원금보장 최우선' },
  { value: 'stable', label: '안정성 중시' },
  { value: 'balanced', label: '균형 추구' },
  { value: 'aggressive', label: '적극적 수익 추구' },
];

export const declineOptions: Option[] = [
  { value: 'sell', label: '즉시 매도' },
  { value: 'partialSell', label: '일부 매도' },
  { value: 'hold', label: '유지 관망' },
  { value: 'buy', label: '추가 매수' },
];

export const liquidityOptions: Option[] = [
  { value: 'rare', label: '거의 없음' },
  { value: 'sometimes', label: '가끔 있음' },
  { value: 'often', label: '자주 있음' },
];

export const agreements: Agreement[] = [
  { key: 'riskAwareness', label: '투자위험을 충분히 인지했습니다' },
  { key: 'resultAgreement', label: '투자성향 분석 결과에 동의합니다' },
  { key: 'decisionConfirmation', label: '최종 투자결정은 본인 판단임을 확인합니다' },
];