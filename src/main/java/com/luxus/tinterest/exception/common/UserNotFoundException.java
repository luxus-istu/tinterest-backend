package com.luxus.tinterest.exception.common;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User was not found");
    }
}
