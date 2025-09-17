package com.rebra.service;

import com.rebra.dto.response.RebalancingCheckResponse;
import com.rebra.dto.response.RebalancingExecutionResponse;

public interface PortfolioRebalancingService {

    /**
     * 포트폴리오의 리밸런싱 필요 여부를 확인합니다.
     * @param portfolioId 포트폴리오 ID
     * @return 리밸런싱 체크 결과
     */
    RebalancingCheckResponse checkRebalancingNeeded(Long portfolioId);

    /**
     * 자동 리밸런싱을 실행합니다.
     * @param portfolioId 포트폴리오 ID
     * @return 리밸런싱 실행 결과
     */
    RebalancingExecutionResponse executeAutoRebalancing(Long portfolioId);

    /**
     * 수동 리밸런싱을 실행합니다.
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     * @return 리밸런싱 실행 결과
     */
    RebalancingExecutionResponse executeManualRebalancing(Long userId, Long portfolioId);

    /**
     * 자동 리밸런싱이 활성화된 모든 포트폴리오를 확인하고 필요시 리밸런싱을 실행합니다.
     * 스케줄러에서 호출됩니다.
     */
    void processAllActivePortfolios();

    /**
     * 특정 포트폴리오의 리밸런싱 필요 여부를 확인합니다 (내부용)
     * @param portfolioId 포트폴리오 ID
     * @return 리밸런싱 필요 여부
     */
    boolean isRebalancingNeeded(Long portfolioId);
}