package com.luxus.tinterest.exception.verify;

public class VerificationCodeExpiredException extends RuntimeException {
    public VerificationCodeExpiredException() {
        super("Verification code has expired, please request a new code");
    }
}
