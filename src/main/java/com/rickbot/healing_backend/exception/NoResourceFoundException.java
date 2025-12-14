package com.rickbot.healing_backend.exception;

public class NoResourceFoundException extends RuntimeException {
    public NoResourceFoundException() {
        super();
    }

    public NoResourceFoundException(String message) {
        super(message);
    }
}
