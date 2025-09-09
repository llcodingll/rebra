package com.rebra.exception.auth;

import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;

public class AuthException extends CustomRuntimeException {

    public AuthException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    public static AuthException missingAuthorizationCode(){
        return new AuthException(ExceptionCode.MISSING_AUTHORIZATION_CODE);
    }

    public static AuthException invalidIdToken(){
        return new AuthException(ExceptionCode.INVALID_ID_TOKEN);
    }

    public static AuthException invalidTempToken(){
        return new AuthException(ExceptionCode.INVALID_TEMP_TOKEN);
    }

    public static AuthException invalidTokenType(){
        return new AuthException(ExceptionCode.INVALID_TOKEN_TYPE);
    }

    public static AuthException expiredTempToken(){
        return new AuthException(ExceptionCode.EXPIRED_TEMP_TOKEN);
    }

    public static AuthException tempTokenParsingFailed(){
        return new AuthException(ExceptionCode.TEMP_TOKEN_PARSING_FAILED);
    }
}