package com.luxus.tinterest.exception;

public class UserAlreadyRegisteredException extends RuntimeException{

    public UserAlreadyRegisteredException() {
        super("User already registered");
    }

}
