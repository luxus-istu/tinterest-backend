package com.luxus.tinterest.exception.handler;

import com.luxus.tinterest.exception.ErrorCode;
import com.luxus.tinterest.exception.ErrorResponse;
import com.luxus.tinterest.exception.registration.UserAlreadyRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RegistrationHandler {

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyRegistered(UserAlreadyRegisteredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                ErrorCode.EMAIL_ALREADY_EXISTS.name(),
                ex.getMessage(),
                null
        ));
    }

}
