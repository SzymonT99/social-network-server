package com.server.springboot.exception;

public class LockedException extends RuntimeException {
    public LockedException(String message) {
        super(message);
    }
}
