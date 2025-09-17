package com.rebra.exception.token;

import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;

public class TokenException extends CustomRuntimeException {

    public TokenException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    public static TokenException refreshTokenNotFound() {
        return new TokenException(ExceptionCode.REFRESH_TOKEN_NOT_FOUND);
    }

    public static TokenException refreshTokenExpired() {
        return new TokenException(ExceptionCode.REFRESH_TOKEN_EXPIRED);
    }

    public static TokenException tokenGenerationFailed() {
        return new TokenException(ExceptionCode.TOKEN_GENERATION_FAILED);
    }
    
    public static TokenException invalidTempToken() {
        return new TokenException(ExceptionCode.INVALID_TEMP_TOKEN);
    }
    
    public static TokenException expiredTempToken() {
        return new TokenException(ExceptionCode.EXPIRED_TEMP_TOKEN);
    }
    
    public static TokenException invalidTokenType() {
        return new TokenException(ExceptionCode.INVALID_TOKEN_TYPE);
    }
    
    public static TokenException tempTokenParsingFailed() {
        return new TokenException(ExceptionCode.TEMP_TOKEN_PARSING_FAILED);
    }

    public static TokenException invalidToken() {
        return new TokenException(ExceptionCode.INVALID_TOKEN);
    }
}