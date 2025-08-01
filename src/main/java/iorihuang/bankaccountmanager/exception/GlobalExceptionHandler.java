package iorihuang.bankaccountmanager.exception;

import iorihuang.bankaccountmanager.dto.DTOResponse;
import iorihuang.bankaccountmanager.dto.Empty;
import iorihuang.bankaccountmanager.exception.exception.InsufficientBalanceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountException.class)
    public ResponseEntity<?> handleAccountException(AccountException ex) {
        return buildResponse(ex);
    }

    @ExceptionHandler(AccountError.class)
    public ResponseEntity<?> handleAccountError(AccountError ex) {
        return buildResponse(ex);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<?> handleInsufficient(InsufficientBalanceException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
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
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    public ResponseEntity<?> buildResponse(HttpStatus status, String message) {
        DTOResponse<Empty> body = new DTOResponse<>();
        body.setCode(0);
        body.setError(status.value());
        body.setMsg(message);
        return new ResponseEntity<>(body, status);
    }

    public ResponseEntity<?> buildResponse(int code, String message) {
        DTOResponse<Empty> body = new DTOResponse<>();
        body.setCode(code);
        body.setMsg(message);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    public ResponseEntity<?> buildResponse(CodeE e) {
        DTOResponse<Empty> body = new DTOResponse<>();
        body.setCode(0);
        body.setError(e.getCode());
        body.setMsg(e.getMessage());
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}