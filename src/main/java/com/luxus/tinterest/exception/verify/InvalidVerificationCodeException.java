package com.luxus.tinterest.exception.verify;

public class InvalidVerificationCodeException extends RuntimeException {
    public InvalidVerificationCodeException() {
        super("Verification code is invalid");
    }
}
