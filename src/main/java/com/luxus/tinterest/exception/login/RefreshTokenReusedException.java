package com.luxus.tinterest.exception.login;

public class RefreshTokenReusedException extends RuntimeException {
    public RefreshTokenReusedException() {
        super("Refresh token reuse has been detected");
    }
}
