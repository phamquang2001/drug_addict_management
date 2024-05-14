package com.system.management.utils.exception;

import com.system.management.model.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(HttpServletRequest request, UnauthorizedException exception) {
        log.error("<Error authorizing> => " + exception.getMessage(), exception);
        return new ResponseEntity<>(
                new ErrorResponse(exception.getStatus(), exception.getMessage(), request.getRequestURI()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(HttpServletRequest request, BadRequestException exception) {
        log.error("<BadRequestException> => {}", exception.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(exception.getStatus(), exception.getMessage(), request.getRequestURI()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(HttpServletRequest request, BadRequestException exception) {
        log.error("<ForbiddenException> => {}", exception.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(exception.getStatus(), exception.getMessage(), request.getRequestURI()),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(HttpServletRequest request,
                                                                                       MissingServletRequestParameterException exception) {
        String name = exception.getParameterName();
        String type = exception.getParameterType();
        String errorMessage = "Tham số " + name + " kiểu dữ liệu " + type + " bắt buộc phải có";
        log.error("<MissingServletRequestParameterException> => {}", errorMessage);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, request.getRequestURI()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(HttpServletRequest request,
                                                                               MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String defaultMessage = Objects.requireNonNull(fieldError).getDefaultMessage();
        log.error("<MethodArgumentNotValidException> => {}", defaultMessage);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), defaultMessage, request.getRequestURI()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpServletRequest request,
                                                                               HttpMessageNotReadableException exception) {
        log.error("<MethodArgumentNotValidException> => " + exception.getCause().getCause().getMessage(), exception);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getCause().getCause().getMessage(), request.getRequestURI()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception exception) {
        log.error("<Exception> => " + exception.getMessage(), exception);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Lỗi hệ thống", request.getRequestURI()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ProcessException.class)
    public ResponseEntity<ErrorResponse> handleProcessException(HttpServletRequest request, ProcessException exception) {
        log.error("<ProcessException> => {}", exception.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(exception.getStatus(), exception.getMessage(), request.getRequestURI()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpServletRequest request,
                                                                                      HttpRequestMethodNotSupportedException exception) {
        log.error("<HttpRequestMethodNotSupportedException> => {}", exception.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage(), request.getRequestURI()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}