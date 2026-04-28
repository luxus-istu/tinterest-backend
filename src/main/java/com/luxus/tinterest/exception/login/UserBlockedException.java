package com.luxus.tinterest.exception.login;

public class UserBlockedException extends RuntimeException {
    public UserBlockedException() {
        super("User is blocked");
    }
}
