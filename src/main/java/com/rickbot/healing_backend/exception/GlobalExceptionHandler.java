package com.rickbot.healing_backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.rickbot.healing_backend.dto.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String code, Throwable ex) {
        if (ex != null) {
            logger.error("❌ {}: {}", code, message, ex);
        } else {
            logger.warn("⚠ {}: {}", code, message);
        }
        ErrorResponse body = new ErrorResponse(message, code, null);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class, ConstraintViolationException.class, IllegalArgumentException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "VALIDATION_ERROR", ex);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), "METHOD_NOT_ALLOWED", ex);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, jakarta.servlet.http.HttpServletRequest request) {
        String path = request.getRequestURI();
        String msg = ex.getMessage() != null ? ex.getMessage() : "Not found";
        String bodyMsg = String.format("%s (path=%s)", msg, path);
        return build(HttpStatus.NOT_FOUND, bodyMsg, "NOT_FOUND", ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        // Fallback 500
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.", "SERVER_ERROR", ex);
    }
}
