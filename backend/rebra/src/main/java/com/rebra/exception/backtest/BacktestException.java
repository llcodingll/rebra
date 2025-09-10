package com.rebra.exception.backtest;

import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;

public class BacktestException extends CustomRuntimeException {

    public BacktestException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    public static BacktestException notFound() {
        return new BacktestException(ExceptionCode.BACKTEST_NOT_FOUND);
    }

    public static BacktestException accessDenied() {
        return new BacktestException(ExceptionCode.BACKTEST_ACCESS_DENIED);
    }

    public static BacktestException invalidRequest() {
        return new BacktestException(ExceptionCode.BACKTEST_INVALID_REQUEST);
    }

    public static BacktestException processing() {
        return new BacktestException(ExceptionCode.BACKTEST_PROCESSING);
    }

    public static BacktestException failed() {
        return new BacktestException(ExceptionCode.BACKTEST_FAILED);
    }

    public static BacktestException deleteNotAllowed() {
        return new BacktestException(ExceptionCode.BACKTEST_DELETE_NOT_ALLOWED);
    }
}