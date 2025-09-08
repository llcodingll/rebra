package com.rebra.exception;

import com.rebra.common.CommonApiResponse;
import com.rebra.dto.common.ValidationErrorDetail;
import com.rebra.dto.common.ValidationErrorResponse;
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
        return ResponseEntity.badRequest().body(CommonApiResponse.fail(400, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(CommonApiResponse.fail(400, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse<Void>> handleServerError(Exception ex) {
        log.error("Internal server error", ex);
        return ResponseEntity.internalServerError()
                .body(CommonApiResponse.fail(500, "Internal Server Error"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleRuntime(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(CommonApiResponse.fail(500, ex.getMessage()));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleUserException(UserException e) {
        log.warn("User exception: {}", e.getMessage());
        return buildCommonResponse(e.getExceptionCode());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonApiResponse<ValidationErrorResponse>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationErrorDetail(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(errors);
        
        log.warn("Validation failed with {} errors", errors.size());
        
        return ResponseEntity.badRequest()
                .body(CommonApiResponse.fail(400, "입력 데이터 검증에 실패했습니다.", errorResponse));
    }

    // 공통 응답 생성 메서드 (CommonApiResponse 사용)
    private ResponseEntity<CommonApiResponse<Void>> buildCommonResponse(ExceptionCode code) {
        return ResponseEntity.status(code.getStatus())
                .body(CommonApiResponse.fail(code.getStatus().value(), code.getMessage()));
    }
}
