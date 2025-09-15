package com.rebra.exception.external;

import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;

public class ExternalApiException extends CustomRuntimeException {

    public ExternalApiException(String message) {
        super(ExceptionCode.EXTERNAL_API_ERROR);
    }

    public ExternalApiException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}