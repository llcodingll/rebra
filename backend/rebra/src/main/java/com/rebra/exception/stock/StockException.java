package com.rebra.exception.stock;

import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;

public class StockException extends CustomRuntimeException {

    public StockException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    public static StockException stockNotFound() {
        return new StockException(ExceptionCode.STOCK_NOT_FOUND);
    }

    public static StockException stockCodeNotFound() {
        return new StockException(ExceptionCode.STOCK_CODE_NOT_FOUND);
    }

    public static StockException stockNameNotFound() {
        return new StockException(ExceptionCode.STOCK_NAME_NOT_FOUND);
    }

    public static StockException stockHoldingFetchFailed() {
        return new StockException(ExceptionCode.STOCK_HOLDING_FETCH_FAILED);
    }

    public static StockException stockHoldingDetailFetchFailed() {
        return new StockException(ExceptionCode.STOCK_HOLDING_DETAIL_FETCH_FAILED);
    }
}