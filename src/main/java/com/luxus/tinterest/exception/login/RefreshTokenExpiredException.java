package com.luxus.tinterest.exception.login;

public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException() {
        super("Refresh token has expired");
    }
}
