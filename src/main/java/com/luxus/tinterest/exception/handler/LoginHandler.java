package com.luxus.tinterest.exception.handler;

import com.luxus.tinterest.exception.ErrorCode;
import com.luxus.tinterest.exception.ErrorResponse;
import com.luxus.tinterest.exception.login.EmailNotVerifiedException;
import com.luxus.tinterest.exception.login.InvalidCredentialsException;
import com.luxus.tinterest.exception.login.UserBlockedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LoginHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ErrorCode.INVALID_CREDENTIALS.name(), ex.getMessage(), null));
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ErrorResponse> handleUserBlocked(UserBlockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ErrorCode.USER_BLOCKED.name(), ex.getMessage(), null));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ErrorCode.EMAIL_NOT_VERIFIED.name(), ex.getMessage(), null));
    }
}
