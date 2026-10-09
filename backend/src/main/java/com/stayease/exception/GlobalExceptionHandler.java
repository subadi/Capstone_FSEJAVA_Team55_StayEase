package com.stayease.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    record ErrorResponse(String timestamp, String path, String error, String message) {
    }

    private ResponseEntity<ErrorResponse> r(HttpStatus s, String e, String m, HttpServletRequest q) {
        return ResponseEntity.status(s).body(new ErrorResponse(Instant.now().toString(), q.getRequestURI(), e, m));
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> api(ApiException e, HttpServletRequest q) {
        System.err.println("[API ERROR] " + q.getMethod() + " " + q.getRequestURI() + " -> " + e.getError() + ": " + e.getMessage());
        return r(e.getStatus(), e.getError(), e.getMessage(), q);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> val(MethodArgumentNotValidException e, HttpServletRequest q) {
        String m = e.getBindingResult().getFieldErrors().stream().map(x -> x.getField() + ": " + x.getDefaultMessage()).collect(Collectors.joining(", "));
        return r(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", m, q);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> cv(ConstraintViolationException e, HttpServletRequest q) {
        return r(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", e.getMessage(), q);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> all(Exception e, HttpServletRequest q) {
        System.err.println("[GlobalExceptionHandler] Unhandled exception for " + q.getMethod() + " " + q.getRequestURI());
        e.printStackTrace(System.err);
        return r(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error: " + e.getClass().getSimpleName() + " - " + (e.getMessage() == null ? "no message" : e.getMessage()), q);
    }
}