package com.backend.wanderverse_server.util.exceptions;

public class LoginFailedException extends RuntimeException{
    public LoginFailedException(String message) {
        super(message);
    }

    public LoginFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
