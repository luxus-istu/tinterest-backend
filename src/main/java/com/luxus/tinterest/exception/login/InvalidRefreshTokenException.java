package com.luxus.tinterest.exception.login;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Невалидный refresh токен");
    }
}
