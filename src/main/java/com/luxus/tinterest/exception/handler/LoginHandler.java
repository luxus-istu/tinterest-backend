package com.luxus.tinterest.exception.handler;

import com.luxus.tinterest.exception.ErrorCode;
import com.luxus.tinterest.exception.ErrorResponse;
import com.luxus.tinterest.exception.login.*;
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

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ErrorCode.INVALID_REFRESH_TOKEN.name(), ex.getMessage(), null));
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ErrorCode.REFRESH_TOKEN_EXPIRED.name(), ex.getMessage(), null));
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenRevoked(RefreshTokenRevokedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ErrorCode.REFRESH_TOKEN_REVOKED.name(), ex.getMessage(), null));
    }

    @ExceptionHandler(RefreshTokenReusedException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenReused(RefreshTokenReusedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED.name(), ex.getMessage(), null));
    }
}
