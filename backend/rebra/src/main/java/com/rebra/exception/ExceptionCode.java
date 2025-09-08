package com.rebra.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {

    // ==== User 관련 ====
    USER_NOT_FOUND(6001, "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS(6003, "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(6004, "접근이 거부되었습니다.", HttpStatus.FORBIDDEN),
    INVALID_USER_FORMAT(6005, "잘못된 사용자 ID 형식입니다.", HttpStatus.BAD_REQUEST),
    NOT_LOGGED_IN(8006, "로그인이 필요합니다.", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ExceptionCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}