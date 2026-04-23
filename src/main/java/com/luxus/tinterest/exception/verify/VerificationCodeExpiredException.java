package com.luxus.tinterest.exception.verify;

public class VerificationCodeExpiredException extends RuntimeException {
    public VerificationCodeExpiredException() {
        super("Код подтверждения истек, сгенерируйте новый код");
    }
}
