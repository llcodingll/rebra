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

export const incomeSourceOptions: Option[] = [
  { value: 'salary', label: '급여' },
  { value: 'business', label: '사업 소득' },
  { value: 'pension', label: '연금' },
  { value: 'other', label: '기타' },
];

export const purposeOptions: Option[] = [
  { value: 'wealth', label: '자산 증식' },
  { value: 'retirement', label: '노후 대비' },
  { value: 'shortTerm', label: '단기 수익' },
  { value: 'education', label: '자녀 교육' },
  { value: 'other', label: '기타' },
];

export const periodOptions: Option[] = [
  { value: 'under1', label: '1년 이하' },
  { value: '1to3', label: '1~3년' },
  { value: '3to5', label: '3~5년' },
  { value: 'over5', label: '5년 이상' },
];

export const experienceOptions: Option[] = [
  { value: 'none', label: '없음' },
  { value: 'under1', label: '1년 미만' },
  { value: '1to3', label: '1~3년' },
  { value: '3to5', label: '3~5년' },
  { value: 'over5', label: '5년 이상' },
];

export const productOptions: Option[] = [
  { value: 'deposit', label: '예금/적금' },
  { value: 'stock', label: '주식' },
  { value: 'bond', label: '채권' },
  { value: 'fund', label: '펀드' },
  { value: 'etf', label: 'ETF' },
  { value: 'derivatives', label: '파생상품' },
  { value: 'crypto', label: '암호화폐' },
];

export const maxLossOptions: Option[] = [
  { value: 'none', label: '손실을 원하지 않음' },
  { value: 'under5', label: '원금의 5%' },
  { value: 'under10', label: '10%' },
  { value: 'under20', label: '20%' },
  { value: 'over30', label: '30% 이상 가능' },
];

export const styleOptions: Option[] = [
  { value: 'stable', label: '안정성 중시' },
  { value: 'balanced', label: '안정성과 수익의 균형' },
  { value: 'aggressive', label: '적극적인 수익 추구' },
  { value: 'highRisk', label: '고위험 고수익 추구' },
];

export const agreements: Agreement[] = [
  { key: 'riskAwareness', label: '투자위험을 충분히 인지했습니다' },
  { key: 'resultAgreement', label: '투자성향 분석 결과에 동의합니다' },
  { key: 'decisionConfirmation', label: '최종 투자결정은 본인 판단임을 확인합니다' },
];