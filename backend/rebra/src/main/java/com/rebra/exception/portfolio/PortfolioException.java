package com.rebra.exception.portfolio;

import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;

public class PortfolioException extends CustomRuntimeException {

    public PortfolioException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

    // 포트폴리오 조회 관련 예외
    public static PortfolioException portfolioNotFound() {
        return new PortfolioException(ExceptionCode.PORTFOLIO_NOT_FOUND);
    }

    public static PortfolioException portfolioAccessDenied() {
        return new PortfolioException(ExceptionCode.PORTFOLIO_ACCESS_DENIED);
    }

    public static PortfolioException accountNotFound() {
        return new PortfolioException(ExceptionCode.PORTFOLIO_ACCOUNT_NOT_FOUND);
    }

    // 포트폴리오 생성 관련 예외
    public static PortfolioException portfolioCreationFailed() {
        return new PortfolioException(ExceptionCode.PORTFOLIO_CREATION_FAILED);
    }

    public static PortfolioException portfolioLimitExceeded() {
        return new PortfolioException(ExceptionCode.PORTFOLIO_LIMIT_EXCEEDED);
    }

    // 포트폴리오 수정 관련 예외
    public static PortfolioException portfolioUpdateFailed() {
        return new PortfolioException(ExceptionCode.PORTFOLIO_UPDATE_FAILED);
    }

    // 포트폴리오 삭제 관련 예외
    public static PortfolioException portfolioDeleteFailed() {
        return new PortfolioException(ExceptionCode.PORTFOLIO_DELETE_FAILED);
    }

    // 포트폴리오 요청 관련 예외
    public static PortfolioException invalidPortfolioRequest() {
        return new PortfolioException(ExceptionCode.PORTFOLIO_INVALID_REQUEST);
    }
}