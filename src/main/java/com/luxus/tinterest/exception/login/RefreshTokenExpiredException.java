package com.luxus.tinterest.exception.login;

public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException() {
        super("Срок действия refresh токена истек");
    }
}
