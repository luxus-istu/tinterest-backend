package com.luxus.tinterest.exception.verify;

public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException() {
        super("Превышено число попыток, сгенерируйте новый код");
    }
}
