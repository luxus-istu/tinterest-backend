package com.luxus.tinterest.exception.login;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Почта не подтверждена");
    }
}
