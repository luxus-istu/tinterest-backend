package com.luxus.tinterest.exception.login;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Email has not been verified yet");
    }
}
