package com.luxus.tinterest.exception.verify;

public class EmailAlreadyVerifiedException extends RuntimeException {
    public EmailAlreadyVerifiedException() {
        super("Аккаунт уже подтвержден");
    }
}
