package com.luxus.tinterest.exception.login;

public class RefreshTokenRevokedException extends RuntimeException {
    public RefreshTokenRevokedException() {
        super("Refresh токен был отозван");
    }
}
