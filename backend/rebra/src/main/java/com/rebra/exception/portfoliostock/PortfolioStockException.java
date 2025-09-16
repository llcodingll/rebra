package com.rebra.exception.portfoliostock;

import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;

public class PortfolioStockException extends CustomRuntimeException {

    public PortfolioStockException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    // 포트폴리오 주식을 찾을 수 없음
    public static PortfolioStockException portfolioStockNotFound() {
        return new PortfolioStockException(ExceptionCode.PORTFOLIO_STOCK_NOT_FOUND);
    }

    // 이미 등록된 주식
    public static PortfolioStockException portfolioStockAlreadyExists() {
        return new PortfolioStockException(ExceptionCode.PORTFOLIO_STOCK_ALREADY_EXISTS);
    }

    // 포트폴리오 주식 접근 권한 없음
    public static PortfolioStockException portfolioStockAccessDenied() {
        return new PortfolioStockException(ExceptionCode.PORTFOLIO_STOCK_ACCESS_DENIED);
    }

    // 목표 비중 합계 초과
    public static PortfolioStockException targetWeightSumExceeded() {
        return new PortfolioStockException(ExceptionCode.TARGET_WEIGHT_SUM_EXCEEDED);
    }
}