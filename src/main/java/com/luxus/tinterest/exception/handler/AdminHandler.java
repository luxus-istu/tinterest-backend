package com.luxus.tinterest.exception.handler;

import com.luxus.tinterest.exception.ErrorCode;
import com.luxus.tinterest.exception.ErrorResponse;
import com.luxus.tinterest.exception.admin.InterestAlreadyExistsException;
import com.luxus.tinterest.exception.admin.InterestNotFoundException;
import com.luxus.tinterest.exception.admin.InvalidAdminOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AdminHandler {

    @ExceptionHandler(InvalidAdminOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAdminOperation(InvalidAdminOperationException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                ErrorCode.INVALID_ADMIN_OPERATION.name(),
                ex.getMessage(),
                null
        ));
    }

    @ExceptionHandler(InterestAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleInterestAlreadyExists(InterestAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                ErrorCode.INTEREST_ALREADY_EXISTS.name(),
                ex.getMessage(),
                null
        ));
    }

    @ExceptionHandler(InterestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInterestNotFound(InterestNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                ErrorCode.INTEREST_NOT_FOUND.name(),
                ex.getMessage(),
                null
        ));
    }
}
