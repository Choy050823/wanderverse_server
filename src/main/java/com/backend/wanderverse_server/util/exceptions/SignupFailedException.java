package com.backend.wanderverse_server.util.exceptions;

public class SignupFailedException extends RuntimeException {
    public SignupFailedException(String message) {
        super(message);
    }

    public SignupFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
