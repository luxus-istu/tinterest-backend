package com.luxus.tinterest.exception.verify;

public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException() {
        super("Too many verification attempts, please request a new code");
    }
}
