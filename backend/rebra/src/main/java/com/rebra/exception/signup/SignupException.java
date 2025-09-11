package com.rebra.exception.signup;

import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;

public class SignupException extends CustomRuntimeException {

    public SignupException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    public static SignupException nicknameAlreadyExists() {
        return new SignupException(ExceptionCode.NICKNAME_ALREADY_EXISTS);
    }
}