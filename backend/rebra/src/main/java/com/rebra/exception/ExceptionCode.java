package com.rebra.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {

    // ==== User 관련 ====
    USER_NOT_FOUND(6001, "사용자를 찾을수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS(6003, "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(6004, "접근이 거부되었습니다.", HttpStatus.FORBIDDEN),
    INVALID_USER_FORMAT(6005, "잘못된 사용자 ID 형식입니다.", HttpStatus.BAD_REQUEST),
    NOT_LOGGED_IN(8006, "로그인이 필요합니다.", HttpStatus.BAD_REQUEST),

    // ==== OAuth2 관련 ====
    MISSING_AUTHORIZATION_CODE(7001, "인가코드가 필요합니다.", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_PRINCIPAL_TYPE(7002, "지원하지 않는 principal 타입입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ID_TOKEN(7003, "ID 토큰 검증에 실패했습니다.", HttpStatus.UNAUTHORIZED),

    // ==== JWT/Token 관련 ====
    INVALID_TEMP_TOKEN(8001, "유효하지 않은 임시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TEMP_TOKEN(8002, "만료된 임시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_TYPE(8003, "잘못된 토큰 타입입니다.", HttpStatus.BAD_REQUEST),
    TEMP_TOKEN_PARSING_FAILED(8004, "임시 토큰 파싱에 실패했습니다.", HttpStatus.UNAUTHORIZED),

    // ==== Backtest 관련 ====
    BACKTEST_NOT_FOUND(9001, "백테스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BACKTEST_ACCESS_DENIED(9002, "백테스트 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    BACKTEST_INVALID_REQUEST(9003, "잘못된 백테스트 요청입니다.", HttpStatus.BAD_REQUEST),
    BACKTEST_PROCESSING(9004, "백테스트가 아직 처리 중입니다.", HttpStatus.ACCEPTED),
    BACKTEST_FAILED(9005, "백테스트 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    BACKTEST_DELETE_NOT_ALLOWED(9006, "진행 중인 백테스트는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ExceptionCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}