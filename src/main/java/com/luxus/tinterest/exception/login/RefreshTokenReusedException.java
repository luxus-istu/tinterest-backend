package com.luxus.tinterest.exception.login;

public class RefreshTokenReusedException extends RuntimeException {
    public RefreshTokenReusedException() {
        super("Обнаружена попытка повторного использования refresh токена");
    }
}
