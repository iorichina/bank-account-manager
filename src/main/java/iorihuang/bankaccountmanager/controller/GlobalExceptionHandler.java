package iorihuang.bankaccountmanager.controller;

import iorihuang.bankaccountmanager.dto.DTOResponse;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.CodeE;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountException.class)
    public ResponseEntity<?> handleAccountException(AccountException ex) {
        return buildResponse(ex);
    }

    @ExceptionHandler(AccountError.class)
    public ResponseEntity<?> handleAccountError(AccountError ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
//        return buildResponse(ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex) {
        if (ex instanceof jakarta.servlet.ServletException servletException) {
            if (null != servletException.getCause()) {
                ex = (Exception) servletException.getCause();
            }
        }
        if (ex instanceof AccountException accountException) {
            return handleAccountException(accountException);
        }
        if (ex instanceof AccountError accountError) {
            return handleAccountError(accountError);
        }
        log.error("Undefined Internal server error: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    public ResponseEntity<?> buildResponse(HttpStatus status, String message) {
        DTOResponse<?> body = new DTOResponse<>();
        body.setCode(0);
        body.setError(status.value());
        body.setMsg(message);
        return new ResponseEntity<>(body, status);
    }

    public ResponseEntity<?> buildResponse(int code, String message) {
        DTOResponse<?> body = new DTOResponse<>();
        body.setCode(code);
        body.setMsg(message);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    public ResponseEntity<?> buildResponse(CodeE e) {
        DTOResponse<?> body = new DTOResponse<>();
        body.setCode(0);
        body.setError(e.getCode());
        body.setMsg(e.getMessage());
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}