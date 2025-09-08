package com.rebra.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public BusinessException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }

    public BusinessException(ExceptionCode exceptionCode, String additionalMessage) {
        super(exceptionCode.getMessage() + " " + additionalMessage);
        this.exceptionCode = exceptionCode;
    }
}