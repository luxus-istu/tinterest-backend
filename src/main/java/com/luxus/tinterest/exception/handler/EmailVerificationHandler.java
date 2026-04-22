package com.luxus.tinterest.exception.handler;

import com.luxus.tinterest.exception.ErrorCode;
import com.luxus.tinterest.exception.ErrorResponse;
import com.luxus.tinterest.exception.verify.EmailAlreadyVerifiedException;
import com.luxus.tinterest.exception.verify.InvalidVerificationCodeException;
import com.luxus.tinterest.exception.verify.TooManyAttemptsException;
import com.luxus.tinterest.exception.verify.VerificationCodeExpiredException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EmailVerificationHandler {

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationCode(InvalidVerificationCodeException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                ErrorCode.INVALID_CODE.name(),
                ex.getMessage(),
                null
        ));
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyAttemptsException(TooManyAttemptsException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                ErrorCode.MANY_ATTEMPTS.name(),
                ex.getMessage(),
                null
        ));
    }

    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<ErrorResponse> handleVerificationCodeExpired(VerificationCodeExpiredException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                ErrorCode.CODE_EXPIRED.name(),
                ex.getMessage(),
                null
        ));
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyVerified(EmailAlreadyVerifiedException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                ErrorCode.EMAIL_ALREADY_CONFIRMED.name(),
                ex.getMessage(),
                null
        ));
    }
}
