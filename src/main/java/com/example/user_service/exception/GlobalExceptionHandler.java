package com.example.user_service.exception;

import com.example.user_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handlingRuntimeException(
            RuntimeException e) {

        log.error("Lỗi hệ thống (Unhandled Exception): ", e);

        return ResponseEntity.badRequest()
                .body(ApiResponse.<Void>builder()
                        .code(400)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Void>> handlingAppException(
            AppException e) {

        log.warn("AppException: code={}, message={}", e.getErrorCode().getCode(), e.getErrorCode().getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(e.getErrorCode().getCode())
                        .message(e.getErrorCode().getMessage())
                        .build());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException (AccessDeniedException e) {

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());

    }

    @ExceptionHandler(value = BadCredentialsException.class)
    ResponseEntity<ApiResponse> handlingBadCredentialsException(BadCredentialsException e){

        ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = DisabledException.class)
    ResponseEntity<ApiResponse> handlingDisabledException(DisabledException e){

        ErrorCode errorCode = ErrorCode.USER_INACTIVE;

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = LockedException.class)
    ResponseEntity<ApiResponse> handlingLockedException(LockedException e){

        ErrorCode errorCode = ErrorCode.USER_LOCKED;

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation (MethodArgumentNotValidException e){

        log.warn("Validation error: {}", e.getMessage());

        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .code(400)
                        .message(Objects.requireNonNull(e.getFieldError()).getDefaultMessage())
                        .build());
    }

}
