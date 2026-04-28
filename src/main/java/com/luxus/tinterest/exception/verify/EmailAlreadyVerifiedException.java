package com.luxus.tinterest.exception.verify;

public class EmailAlreadyVerifiedException extends RuntimeException {
    public EmailAlreadyVerifiedException() {
        super("Email is already verified");
    }
}
