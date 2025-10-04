package com.rebra.rebalance.domain.rebalancing.model;

public enum ExecutionStatus {
    PENDING,      // 최초 진입 또는 주문 실행 전 장애 → 처음부터 재실행
    PROCESSING,   // 매도 실행 직전 전환 → 재진입 시 order_record 복구
    COMPLETED,    // 완료 → 중복 consume 시 스킵
    FAILED        // 15:30 초과 / 복구 불가 → DB 기록 + 메인 서버 동기화
}
