package com.luxus.tinterest.exception.registration;

public class UserAlreadyRegisteredException extends RuntimeException{

    public UserAlreadyRegisteredException() {
        super("Пользователь уже зарегестрирован");
    }

}
