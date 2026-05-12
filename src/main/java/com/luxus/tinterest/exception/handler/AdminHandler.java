package com.luxus.tinterest.exception.handler;

import com.luxus.tinterest.exception.ErrorCode;
import com.luxus.tinterest.exception.ErrorResponse;
import com.luxus.tinterest.exception.admin.InvalidAdminOperationException;
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
}
