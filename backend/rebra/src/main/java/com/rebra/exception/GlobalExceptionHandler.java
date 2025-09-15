package com.rebra.exception;

import com.rebra.common.CommonApiResponse;
import com.rebra.dto.common.ValidationErrorDetail;
import com.rebra.dto.common.ValidationErrorResponse;
import com.rebra.exception.account.AccountException;
import com.rebra.exception.auth.AuthException;
import com.rebra.exception.backtest.BacktestException;
import com.rebra.exception.signup.SignupException;
import com.rebra.exception.token.TokenException;
import com.rebra.exception.stock.StockException;
import com.rebra.exception.user.UserException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleMissingParams(MissingServletRequestParameterException ex) {
        String message = ex.getParameterName() + " parameter is missing";
        log.warn("Missing parameter: {}", ex.getParameterName());
        return ResponseEntity.badRequest().body(
                CommonApiResponse.error("MISSING_PARAMETER", message, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                CommonApiResponse.error("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse<Void>> handleServerError(Exception ex) {
        log.error("Internal server error", ex);
        return ResponseEntity.internalServerError()
                .body(CommonApiResponse.error("INTERNAL_SERVER_ERROR", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleRuntime(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(CommonApiResponse.error("RUNTIME_ERROR", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleUserException(UserException e) {
        log.warn("User exception: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(CommonApiResponse.error(e));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleAuthException(AuthException e) {
        log.warn("Auth exception: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(CommonApiResponse.error(e));
    }

    @ExceptionHandler(BacktestException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleBacktestException(BacktestException e) {
        log.warn("Backtest exception: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(CommonApiResponse.error(e));
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleTokenException(TokenException e) {
        log.warn("Token exception: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(CommonApiResponse.error(e));
    }

    @ExceptionHandler(SignupException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleSignupException(SignupException e) {
        log.warn("Signup exception: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(CommonApiResponse.error(e));
    }

    @ExceptionHandler(StockException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleStockException(StockException e) {
        log.warn("Stock exception: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(CommonApiResponse.error(e));
    }

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleAccountException(AccountException e) {
        log.warn("Account exception: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(CommonApiResponse.error(e));
    }

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleCustomRuntimeException(CustomRuntimeException e) {
        log.warn("Custom runtime exception: {} - {}", e.getExceptionCode(), e.getMessage());
        return ResponseEntity.status(e.getExceptionCode().getStatus())
                .body(CommonApiResponse.error(e.getExceptionCode().name(), e.getMessage(), e.getExceptionCode().getStatus()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonApiResponse<ValidationErrorResponse>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationErrorDetail(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(errors);
        
        log.warn("Validation failed with {} errors", errors.size());
        
        return ResponseEntity.badRequest()
                .body(CommonApiResponse.error("VALIDATION_FAILED", "입력 데이터 검증에 실패했습니다.", errorResponse, HttpStatus.BAD_REQUEST));
    }

}
