package com.luxus.tinterest.exception.verify;

public class InvalidVerificationCodeException extends RuntimeException {
    public InvalidVerificationCodeException() {
        super("Неверный или недействительный код");
    }
}
