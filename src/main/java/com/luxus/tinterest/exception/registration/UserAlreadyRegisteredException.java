package com.luxus.tinterest.exception.registration;

public class UserAlreadyRegisteredException extends RuntimeException {

    public UserAlreadyRegisteredException() {
        super("User with this email is already registered");
    }
}
