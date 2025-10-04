package com.rebra.rebalance.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    IDEMPOTENCY_VIOLATION("RE001", "이미 완료된 리밸런싱 작업"),
    CUTOFF_TIME_EXCEEDED("RE002", "15:30 이후 도달한 메시지 - 장 마감"),
    KIS_API_ERROR("RE003", "KIS API 호출 실패"),
    ORDER_EXECUTION_FAILED("RE004", "개별 주문 실행 실패"),
    RECOVERY_FAILED("RE005", "PROCESSING 복구 단계 실패");

    private final String code;
    private final String message;
}
