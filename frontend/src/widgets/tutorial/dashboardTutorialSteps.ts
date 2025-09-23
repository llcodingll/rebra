export interface TutorialStep {
  id: string;
  title: string;
  content: string;
  targetSelector?: string;
  position?: 'top' | 'bottom' | 'left' | 'right' | 'center';
}

export const dashboardTutorialSteps: TutorialStep[] = [
  {
    id: 'welcome',
    title: '대시보드에 오신 것을 환영합니다!',
    content: '이곳은 여러분의 포트폴리오 현황과 투자 성과를 한눈에 확인할 수 있는 메인 대시보드입니다. 실시간 데이터로 포트폴리오를 관리해보세요!',
    position: 'center'
  },
  {
    id: 'portfolio-overview',
    title: '포트폴리오 개요',
    content: '현재 보유 중인 포트폴리오의 전체 가치와 수익률을 확인할 수 있습니다. 총 자산, 오늘의 수익, 누적 수익률이 실시간으로 업데이트됩니다.',
    targetSelector: '.portfolio-overview-section',
    position: 'bottom'
  },
  {
    id: 'asset-composition',
    title: '자산 구성 차트',
    content: '포트폴리오의 자산 배분 현황을 시각적으로 확인할 수 있습니다. 각 종목별 비중과 섹터별 분산 상태를 파악해보세요.',
    targetSelector: '.asset-composition-chart',
    position: 'bottom'
  },
  {
    id: 'performance-chart',
    title: '수익률 성과 차트',
    content: '시간에 따른 포트폴리오의 수익률 변화를 추적할 수 있습니다. 기간별 성과를 비교하고 투자 전략을 점검해보세요.',
    targetSelector: '.performance-chart-section',
    position: 'top'
  },
  {
    id: 'holdings-list',
    title: '보유 종목 현황',
    content: '현재 보유 중인 모든 종목의 상세 정보를 확인할 수 있습니다. 종목별 수익률, 보유 수량, 현재 가격 등을 실시간으로 모니터링하세요.',
    targetSelector: '.holdings-list-section',
    position: 'top'
  },
  {
    id: 'market-indicators',
    title: '시장 지표',
    content: '코스피, 코스닥 등 주요 시장 지표와 환율 정보를 확인할 수 있습니다. 시장 상황을 파악하여 투자 의사결정에 활용하세요.',
    targetSelector: '.market-indicators-section',
    position: 'bottom'
  },
  {
    id: 'rebalancing-alerts',
    title: '리밸런싱 알림',
    content: '목표 비중에서 벗어난 종목들에 대한 리밸런싱 제안을 확인할 수 있습니다. 정기적인 리밸런싱으로 포트폴리오를 최적화하세요.',
    targetSelector: '.rebalancing-section',
    position: 'top'
  },
  {
    id: 'navigation-tips',
    title: '대시보드 활용 팁',
    content: '• 정기적으로 포트폴리오 현황을 점검하세요\n• 리밸런싱 알림을 확인하여 균형을 유지하세요\n• 시장 지표를 참고하여 투자 타이밍을 결정하세요\n• 백테스트 결과와 실제 성과를 비교해보세요',
    position: 'center'
  }
];