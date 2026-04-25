package com.luxus.tinterest.exception.login;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Неверный логин или пароль");
    }
}
