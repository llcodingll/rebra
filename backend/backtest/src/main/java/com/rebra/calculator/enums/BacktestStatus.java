package com.rebra.calculator.enums;

/**
 * 백테스트 계산 상태를 나타내는 열거형
 * 백테스트 요청부터 완료까지의 전체 생명주기를 관리한다.
 */
public enum BacktestStatus {

    /**
     * 대기 중
     * 백테스트 요청이 접수되었지만 아직 처리를 시작하지 않은 상태
     * Kafka 큐에서 대기 중이거나 다른 작업 완료를 기다리는 상태
     */
    PENDING("대기중", "백테스트 요청이 접수되어 처리 대기 중"),

    /**
     * 처리 중
     * 백테스트 계산이 현재 진행되고 있는 상태
     * 포트폴리오 계산, 리밸런싱 실행, 수익률 분석 등을 수행 중
     */
    PROCESSING("처리중", "백테스트 계산을 수행하고 있음"),

    /**
     * 완료
     * 백테스트 계산이 성공적으로 완료된 상태
     * 모든 결과가 정상적으로 계산되어 메인 서버로 전송됨
     */
    COMPLETED("완료", "백테스트 계산이 성공적으로 완료됨"),

    /**
     * 실패
     * 백테스트 계산 중 오류가 발생하여 실패한 상태
     * 데이터 오류, 계산 오류, 시스템 오류 등으로 인한 실패
     */
    FAILED("실패", "백테스트 계산 중 오류 발생으로 실패"),

    /**
     * 취소됨
     * 사용자나 시스템에 의해 백테스트가 취소된 상태
     * 처리 중인 백테스트를 중단하거나 대기 중인 요청을 취소
     */
    CANCELLED("취소", "백테스트가 사용자 또는 시스템에 의해 취소됨"),

    /**
     * 타임아웃
     * 설정된 제한 시간을 초과하여 처리가 중단된 상태
     * 과도하게 복잡한 계산이나 시스템 부하로 인한 타임아웃
     */
    TIMEOUT("타임아웃", "설정된 제한 시간을 초과하여 처리 중단");

    private final String displayName;
    private final String description;

    /**
     * BacktestStatus 생성자
     * 
     * @param displayName 화면 표시용 이름
     * @param description 상세 설명
     */
    BacktestStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 화면 표시용 이름을 반환한다.
     * 
     * @return 표시용 이름
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 백테스트 상태에 대한 상세 설명을 반환한다.
     * 
     * @return 상세 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 백테스트가 완료 상태(성공 또는 실패)인지 확인한다.
     * 
     * @return 완료 상태면 true
     */
    public boolean isFinished() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == TIMEOUT;
    }

    /**
     * 백테스트가 성공적으로 완료된 상태인지 확인한다.
     * 
     * @return 성공적으로 완료된 상태면 true
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    /**
     * 백테스트가 처리 중인 상태인지 확인한다.
     * 
     * @return 처리 중이면 true
     */
    public boolean isProcessing() {
        return this == PROCESSING;
    }

    /**
     * 백테스트가 오류 상태인지 확인한다.
     * 
     * @return 오류 상태면 true
     */
    public boolean isError() {
        return this == FAILED || this == CANCELLED || this == TIMEOUT;
    }

    /**
     * 다음 상태로 전환이 가능한지 확인한다.
     * 상태 전환 규칙을 검증하여 잘못된 상태 변경을 방지한다.
     * 
     * @param nextStatus 전환하려는 다음 상태
     * @return 전환 가능하면 true
     */
    public boolean canTransitionTo(BacktestStatus nextStatus) {
        if (nextStatus == null) {
            return false;
        }

        switch (this) {
            case PENDING:
                // PENDING에서는 PROCESSING, CANCELLED로만 전환 가능
                return nextStatus == PROCESSING || nextStatus == CANCELLED;

            case PROCESSING:
                // PROCESSING에서는 모든 완료 상태로 전환 가능
                return nextStatus == COMPLETED || nextStatus == FAILED || 
                       nextStatus == CANCELLED || nextStatus == TIMEOUT;

            case COMPLETED:
            case FAILED:
            case CANCELLED:
            case TIMEOUT:
                // 완료 상태에서는 다른 상태로 전환 불가
                return false;

            default:
                return false;
        }
    }

    /**
     * 문자열로부터 BacktestStatus를 찾는다.
     * 
     * @param value 검색할 문자열
     * @return 해당하는 BacktestStatus, 없으면 null
     */
    public static BacktestStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String searchValue = value.trim().toLowerCase();

        for (BacktestStatus status : values()) {
            // enum 이름으로 비교
            if (status.name().toLowerCase().equals(searchValue)) {
                return status;
            }
            // 표시 이름으로 비교
            if (status.displayName.toLowerCase().equals(searchValue)) {
                return status;
            }
        }

        return null;
    }

    /**
     * 기본 백테스트 상태를 반환한다.
     * 
     * @return 기본 상태 (PENDING)
     */
    public static BacktestStatus getDefault() {
        return PENDING;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, description);
    }
}