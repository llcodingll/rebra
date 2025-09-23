package com.rebra.exception.kis;

import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;

public class KisException extends CustomRuntimeException {

    public KisException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    public static KisException mockAccountNotSupported() {
        return new KisException(ExceptionCode.MOCK_ACCOUNT_NOT_SUPPORTED);
    }
}