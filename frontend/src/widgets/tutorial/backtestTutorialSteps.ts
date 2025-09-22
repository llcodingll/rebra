export interface TutorialStep {
  id: string;
  title: string;
  content: string;
  targetSelector?: string;
  position?: 'top' | 'bottom' | 'left' | 'right' | 'center';
}

export const backtestTutorialSteps: TutorialStep[] = [
  {
    id: 'welcome',
    title: '백테스트에 오신 것을 환영합니다!',
    content: '이곳은 과거 데이터를 바탕으로 투자 전략을 시뮬레이션해보는 공간입니다. 가상의 자금으로 안전하게 전략을 테스트해보세요!',
    position: 'center'
  },
  {
    id: 'creation-section',
    title: '백테스트 생성 영역',
    content: '여기서 새로운 백테스트를 만들 수 있습니다. 투자 전략, 기간, 초기 자금 등을 설정하여 나만의 백테스트를 생성해보세요.',
    targetSelector: '.backtest-creation-widget',
    position: 'bottom'
  },
  {
    id: 'name-input',
    title: '백테스트 이름 설정',
    content: '백테스트에 구분하기 쉬운 이름을 지어주세요. 예: "삼성전자 장기투자 전략", "코스피 200 분산투자" 등',
    targetSelector: 'input[placeholder*="이름"]',
    position: 'bottom'
  },
  {
    id: 'stocks-selection',
    title: '투자할 주식 선택',
    content: '백테스트에 포함할 주식들을 검색하고 선택하세요. 여러 종목을 선택하여 분산투자 효과도 확인할 수 있습니다.',
    targetSelector: '.stock-search-container',
    position: 'bottom'
  },
  {
    id: 'settings-configuration',
    title: '백테스트 설정',
    content: '투자 기간, 초기 자금, 리밸런싱 주기 등을 설정합니다. 이 설정들이 백테스트 결과에 큰 영향을 미치니 신중히 선택하세요.',
    targetSelector: '.backtest-settings',
    position: 'top'
  },
  {
    id: 'history-section',
    title: '백테스트 히스토리',
    content: '이전에 실행한 백테스트들의 결과를 확인할 수 있습니다. 수익률, 최대 낙폭, 샤프 비율 등의 지표를 비교해보세요.',
    targetSelector: '.backtest-history-widget',
    position: 'top'
  },
  {
    id: 'results-analysis',
    title: '결과 분석하기',
    content: '백테스트가 완료되면 상세한 분석 결과를 확인할 수 있습니다. 수익률 차트, 포트폴리오 구성, 리스크 지표 등을 통해 전략을 평가해보세요.',
    position: 'center'
  },
  {
    id: 'tips',
    title: '백테스트 활용 팁',
    content: '• 다양한 기간으로 테스트해보세요\n• 과거 실적이 미래를 보장하지 않음을 기억하세요\n• 여러 전략을 비교분석해보세요\n• 리스크 관리도 함께 고려하세요',
    position: 'center'
  }
];